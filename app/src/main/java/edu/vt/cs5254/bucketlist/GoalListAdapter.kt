package edu.vt.cs5254.bucketlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.bucketlist.databinding.ListItemGoalBinding
import java.util.UUID

class GoalHolder(private val binding: ListItemGoalBinding) : RecyclerView.ViewHolder(binding.root) {
    lateinit var boundGoal: Goal
        private set

    fun bind(goal: Goal, onGoalClicked: (UUID) -> Unit) {
        boundGoal = goal
        binding.root.setOnClickListener {
            onGoalClicked(goal.id)
        }
        val titleToDisplay = if (goal.title.isBlank()) {
            binding.root.context.getString(R.string.default_goal_title) // Use default title string resource
        } else {
            goal.title
        }
        // Set title
        binding.listItemTitle.text = titleToDisplay

        // Set progress count
        val progressCount = goal.notes.count { it.type == GoalNoteType.PROGRESS }
        binding.listItemProgressCount.text = binding.root.context.getString(
            R.string.goal_progress_count, progressCount
        )

        // Set image and visibility based on state
        when {
            goal.isCompleted -> {
                binding.listItemImage.setImageResource(R.drawable.ic_goal_completed)
            }
            goal.isPaused -> {
                binding.listItemImage.setImageResource(R.drawable.ic_goal_paused)
            }
        }
        binding.listItemImage.visibility = if (goal.isCompleted || goal.isPaused) View.VISIBLE else View.GONE

    }
}

class GoalListAdapter(private val goals: List<Goal>,
    private val onGoalClicked: (UUID) -> Unit

) : RecyclerView.Adapter<GoalHolder>() {

//    private val goals = mutableListOf<Goal>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemGoalBinding.inflate(inflater, parent, false)
        return GoalHolder(binding)
    }

    override fun getItemCount(): Int {
        return goals.size
    }

    override fun onBindViewHolder(holder: GoalHolder, position: Int) {
        holder.bind(goals[position], onGoalClicked)
    }


    // Method to get a goal at a specific position
    fun getGoalAtPosition(position: Int): Goal? {
        return if (position in goals.indices) goals[position] else null
    }
}
