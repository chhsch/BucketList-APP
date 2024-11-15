package edu.vt.cs5254.bucketlist
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.bucketlist.databinding.ListItemGoalNoteBinding

class GoalNoteHolder(private val binding: ListItemGoalNoteBinding) : RecyclerView.ViewHolder(binding.root) {
    lateinit var boundNote: GoalNote
        private set
    fun bind(note: GoalNote) {
        boundNote = note
        // Set the text on the button
        binding.goalNoteButton.text = note.text

        // Set background color based on note type
        binding.goalNoteButton.setBackgroundColor(
            when (note.type) {
                GoalNoteType.PAUSED -> Color.parseColor("#ff8f53") // Orange for PAUSED
                GoalNoteType.PROGRESS -> Color.parseColor("#97d2e7") // Light Blue for PROGRESS
                GoalNoteType.COMPLETED -> Color.parseColor("#bbd36b") // Light Green for COMPLETED
            }
        )

        // Ensure button is disabled as per layout requirement
        binding.goalNoteButton.isEnabled = false
    }
}

class GoalNoteListAdapter : RecyclerView.Adapter<GoalNoteHolder>() {

    private val notes = mutableListOf<GoalNote>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalNoteHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemGoalNoteBinding.inflate(inflater, parent, false)
        return GoalNoteHolder(binding)
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun onBindViewHolder(holder: GoalNoteHolder, position: Int) {
        holder.bind(notes[position])
    }



    fun updateNotes(newNotes: List<GoalNote>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged() // Notify the adapter to refresh
    }
    fun removeGoalAt(position: Int) {
        if (position in notes.indices) {
            notes.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getNoteAtPosition(position: Int): GoalNote? {
        return if (position in notes.indices) notes[position] else null
    }
}
