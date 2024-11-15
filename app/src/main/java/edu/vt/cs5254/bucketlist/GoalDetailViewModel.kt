package edu.vt.cs5254.bucketlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.GlobalScope

class GoalDetailViewModel(private val goalId: UUID) : ViewModel() {

    private val repository = GoalRepository.get()

    private val _goal = MutableStateFlow<Goal?>(null)
    val goal = _goal.asStateFlow()

fun removeNoteFromGoal(note: GoalNote) {
    viewModelScope.launch {
        // Delete the note from the database
        repository.deleteNote(note)

        // Reload the updated notes from the database
        val updatedNotes = repository.getNotesForGoal(note.goalId)

        // Update the goal's notes in the ViewModel state if _goal is not null
        _goal.update { currentGoal ->
            currentGoal?.copy().apply {
                this?.notes = updatedNotes // Safely update notes if currentGoal is non-null
                isModified = true          // Mark as modified to save if needed
            }
        }
    }
}

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // Track if the goal has been modified
    private var isModified = false

    init {
        // Load the goal from the repository
        viewModelScope.launch {
            val fetchedGoal = repository.getGoal(goalId)
            _goal.value = fetchedGoal
        }
    }


    // Update goal details using a provided update function
    fun updateGoal(onUpdate: (Goal) -> Goal) {
        _goal.update { oldGoal ->
            oldGoal?.let { currentGoal ->
                val updatedGoal = onUpdate(currentGoal)

                // Check if goal or notes have changed
                if (updatedGoal != currentGoal || updatedGoal.notes != currentGoal.notes) {
                    isModified = true // Mark as modified
                    updatedGoal.copy(lastUpdated = Date()).apply {
                        notes = updatedGoal.notes
                    }
                } else {
                    currentGoal
                }
            }
        }
    }


    // Save changes to the database before the ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        _goal.value?.let { goal ->
            if (isModified) { // Only save if modifications were made
                GlobalScope.launch {
                    repository.updateGoal(goal)
                }
            }
        }
    }
}

// Factory class for creating GoalDetailViewModel
class GoalDetailViewModelFactory(private val goalId: UUID) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GoalDetailViewModel(goalId) as T
    }
}