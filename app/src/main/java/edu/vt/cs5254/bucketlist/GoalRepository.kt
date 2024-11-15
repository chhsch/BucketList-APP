package edu.vt.cs5254.bucketlist

import android.content.Context
import android.provider.CalendarContract.Instances
import androidx.room.Room
import edu.vt.cs5254.bucketlist.database.GoalDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

private const val DATABASE_NAME = "goal-database"

class GoalRepository private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = GlobalScope // Consider passing a ViewModel scope instead
) {

    private val database = Room.databaseBuilder(
        context,
        GoalDatabase::class.java,
        "goal-database"
    )
        .createFromAsset(DATABASE_NAME)
        .build()

    // Fetch all goals and their associated notes as a Flow
    fun getGoals(): Flow<List<Goal>> {
        return database.goalDao().getGoals().map { multiMap ->
            multiMap.keys.map { goal ->
                goal.apply { notes = multiMap.getValue(goal) }
            }
        }
    }

    // Fetch a specific goal with its notes
    suspend fun getGoal(id: UUID): Goal = database.goalDao().getGoalAndNotes(id)

    // Update a goal and its notes
    fun updateGoal(goal: Goal) {
        coroutineScope.launch {
            database.goalDao().updateGoalAndNotes(goal)
        }
    }

    // Delete a goal and its associated notes
    suspend  fun deleteGoal(goal: Goal) {
        database.goalDao().deleteGoalAndNotes(goal)
    }

    // Add a goal and its notes to the database
    suspend fun addGoal(goal: Goal) = database.goalDao().addGoal(goal)

    suspend fun updateGoalAndNotes(goal: Goal) {
        coroutineScope.launch {
            database.goalDao().updateGoalAndNotes(goal)
        }
    }
    // Retrieve all notes for a specific goal
    suspend fun getNotesForGoal(goalId: UUID): List<GoalNote> {
        return database.goalDao().getNotesForGoal(goalId)
    }

    // Delete a specific note
    suspend fun deleteNote(note: GoalNote) {
        database.goalDao().deleteNote(note)
    }


    companion object {
        @Volatile
        private var INSTANCE: GoalRepository? = null

        fun initialize(context: Context) {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = GoalRepository(context)
                }
            }
        }

        fun get(): GoalRepository {
            return checkNotNull(INSTANCE) { "GoalRepository must be initialized!" }
        }
    }
}
