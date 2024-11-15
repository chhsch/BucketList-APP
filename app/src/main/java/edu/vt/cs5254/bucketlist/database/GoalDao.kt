package edu.vt.cs5254.bucketlist.database
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import edu.vt.cs5254.bucketlist.Goal
import edu.vt.cs5254.bucketlist.GoalNote
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface GoalDao {

    @Query(
        "SELECT * FROM goal g LEFT JOIN goal_note n ON g.id = n.goalId ORDER BY g.lastUpdated DESC"
    )
    fun getGoals(): Flow<Map<Goal, List<GoalNote>>>

    @Query("SELECT * FROM goal WHERE id = :id")
    suspend fun internalGetGoal(id: UUID): Goal

    @Query("SELECT * FROM goal_note WHERE goalId = :goalId")
    suspend fun internalGetNotesForGoal(goalId: UUID): List<GoalNote>

    @Update
    suspend fun internalUpdateGoal(goal: Goal)

    @Insert
    suspend fun internalInsertGoalNote(goalNote: GoalNote)

    @Query("DELETE FROM goal_note WHERE goalId = :goalId")
    suspend fun internalDeleteNotesFromGoal(goalId: UUID)

    // Fetch goal and its notes as a transaction
    @Transaction
    suspend fun getGoalAndNotes(id: UUID): Goal {
        val goal = internalGetGoal(id)
        val notes = internalGetNotesForGoal(id)
        return goal.apply { this.notes = notes }
    }

    // Update goal and its notes as a transaction
    @Transaction
    suspend fun updateGoalAndNotes(goal: Goal) {
        internalDeleteNotesFromGoal(goal.id)
        goal.notes.forEach { internalInsertGoalNote(it) }
        internalUpdateGoal(goal)
    }

    @Insert
    suspend fun internalInsertGoal(goal: Goal)

    @Transaction
    suspend fun addGoal(goal: Goal){
        internalInsertGoal(goal)
        goal.notes.forEach{internalInsertGoalNote(it)}
    }

    @Delete
    suspend fun internalDeleteGoal(goal: Goal)

    @Transaction
    suspend fun deleteGoalAndNotes(goal: Goal){
        internalDeleteNotesFromGoal(goal.id)
        internalDeleteGoal(goal)
    }

    // Fetch all notes associated with a specific goal
    @Query("SELECT * FROM goal_note WHERE goalId = :goalId")
    suspend fun getNotesForGoal(goalId: UUID): List<GoalNote>

    // Delete a specific note
    @Delete
    suspend fun deleteNote(note: GoalNote)

}
