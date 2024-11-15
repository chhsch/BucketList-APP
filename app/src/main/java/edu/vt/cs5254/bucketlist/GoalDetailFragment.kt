package edu.vt.cs5254.bucketlist
import androidx.activity.result.contract.ActivityResultContracts

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.bucketlist.databinding.FragmentGoalDetailBinding
import kotlinx.coroutines.launch
import java.io.File

class GoalDetailFragment : Fragment() {
    // Name: Chih-Hsing Chen
    // VT Username: hathawayc080119

    private val arg: GoalDetailFragmentArgs by navArgs()
    private val vm: GoalDetailViewModel by viewModels {
        GoalDetailViewModelFactory(arg.goalId)
    }
//    private lateinit var adapter: GoalNoteListAdapter
    private var _binding: FragmentGoalDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) { "FragmentGoalDetailBinding is NULL!" }

    private lateinit var goalNoteListAdapter: GoalNoteListAdapter

    private val photoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ){
        tookPicture ->
        Log.w("!!!GDF!!!", "Took picture:$tookPicture")
        if (tookPicture) {
            vm.goal.value?.let {
                binding.goalPhoto.tag = null
                updatePhoto(it)
            }
        }
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_goal_detail, menu)
                val photoIntent = photoLauncher.contract.createIntent(
                    requireContext(),
                    Uri.EMPTY
                )
                menu.findItem(R.id.take_photo_menu).isVisible =
                    canResolveIntent(photoIntent)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.take_photo_menu -> {
                        vm.goal.value?.let {
                            val photoFile = File(
                                requireContext().applicationContext.filesDir,
                                it.photoFileName
                            )
                            val photoUri = FileProvider.getUriForFile(
                                requireContext(),
                                "edu.vt.cs5254.bucketlist.fileprovider",
                                photoFile
                            )
                            photoLauncher.launch(photoUri)
                        }
                        true
                    }
                    R.id.share_goal_menu -> {
                        // Call shareGoal when the user selects "Share Goal" menu item
                        vm.goal.value?.let { shareGoal(it) }
                        true
                    }
                    R.id.action_delete_goal -> {  // Add this condition
                        deleteCurrentGoal()       // Call delete here
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)




        _binding = FragmentGoalDetailBinding.inflate(inflater, container, false)
        goalNoteListAdapter = GoalNoteListAdapter()
        binding.goalNoteRecyclerView.apply {
            adapter = goalNoteListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        return binding.root
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("GoalDetailFragment", "onOptionsItemSelected called with item: ${item.itemId}")
        return when (item.itemId) {
            R.id.action_delete_goal -> {
                deleteCurrentGoal()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun deleteCurrentGoal() {
        vm.goal.value?.let { goal ->
            Log.d("GoalDetailFragment", "Deleting goal with ID: ${goal.id}")
            vm.deleteGoal(goal)
            findNavController().popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        getItemTouchHelper().attachToRecyclerView(binding.goalNoteRecyclerView)
        binding.goalPhoto.setOnClickListener {
            vm.goal.value?.let {
                findNavController().navigate(
                    GoalDetailFragmentDirections.showImageDetail(it.photoFileName)
                )
            }
        }
        binding.goalNoteRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.goalNoteRecyclerView.adapter = goalNoteListAdapter
        // Observe changes to the goal state
        observeGoalUpdates()
        binding.addProgressButton.setOnClickListener {
            if (vm.goal.value?.isCompleted == false) {
                findNavController().navigate(GoalDetailFragmentDirections.addProgress())
            }
        }
        // Set up listeners for user interactions
        setupListeners()

        // Listen for results from ProgressDialogFragment
        setFragmentResultListener(ProgressDialogFragment.REQUEST_KEY) { _, bundle ->
            val resultText = bundle.getString(ProgressDialogFragment.BUNDLE_KEY)
            if (!resultText.isNullOrBlank()) {
                // Update the ViewModel and UI with the new progress note
                vm.updateGoal { oldGoal ->
                    oldGoal.copy().apply {
                        notes = oldGoal.notes + GoalNote(
                            type = GoalNoteType.PROGRESS,
                            text = resultText,
                            goalId = oldGoal.id
                        )
                    }
                }
            }
        }


    }
    private fun getItemTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, 0) {

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val noteHolder = viewHolder as? GoalNoteHolder
                val swipedNote = noteHolder?.boundNote

                // Allow swipe only if the note type is Progress
                return if (swipedNote?.type == GoalNoteType.PROGRESS) {
                    ItemTouchHelper.LEFT
                } else {
                    0  // Disable swipe for non-Progress notes
                }
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false // No move operation

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val noteHolder = viewHolder as? GoalNoteHolder
                    val swipedNote = noteHolder?.boundNote

                    if (swipedNote != null && swipedNote.type == GoalNoteType.PROGRESS) {
                        // Update ViewModel to remove only the swiped PROGRESS note
                        vm.removeNoteFromGoal(swipedNote)

                        // Notify the adapter to update the RecyclerView
                        goalNoteListAdapter.removeGoalAt(position)
                    }
                }
            }
        })
    }



    private fun observeGoalUpdates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.goal.collect { goal ->
                    goal?.let { updateView(it) }
                }
            }
        }
    }

    private fun shareGoal(goal: Goal) {
        val progressText = goal.notes.filter { it.type == GoalNoteType.PROGRESS }
            .joinToString("\n") { " * ${it.text}" }

        val summaryText = buildString {
            append(goal.title)
            append("\n")
            append(DateFormat.format("'Last updated' yyyy-MM-dd 'at' hh:mm:ss a", goal.lastUpdated))
            if (progressText.isNotBlank()) {
                append("\nProgress:\n")
                append(progressText)
            }
            when {
                goal.isPaused -> append("\nThis goal has been Paused.\n")
                goal.isCompleted -> append("\nThis goal has been Completed.\n")
            }
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, goal.title)
            putExtra(Intent.EXTRA_TEXT, summaryText)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_goal)))
    }


    private fun setupListeners() {
        binding.apply {
            pausedCheckbox.setOnClickListener {
                vm.updateGoal { oldGoal ->
                    val updatedNotes = oldGoal.notes.toMutableList()

                    if (oldGoal.isPaused) {
                        // Remove 'PAUSED' note
                        updatedNotes.removeAll { it.type == GoalNoteType.PAUSED }
                    } else {
                        // Add 'PAUSED' note
                        updatedNotes.add(
                            GoalNote(
                                type = GoalNoteType.PAUSED,
                                goalId = oldGoal.id
                            )
                        )
                    }

                    oldGoal.copy().apply {
                        notes = updatedNotes
                    }
                }
            }

            completedCheckbox.setOnClickListener {
                vm.updateGoal { oldGoal ->
                    val updatedNotes = oldGoal.notes.toMutableList()

                    if (oldGoal.isCompleted) {
                        // Remove 'COMPLETED' note
                        updatedNotes.removeAll { it.type == GoalNoteType.COMPLETED }
                    } else {
                        // Add 'COMPLETED' note
                        updatedNotes.add(
                            GoalNote(
                                type = GoalNoteType.COMPLETED,
                                goalId = oldGoal.id
                            )
                        )
                    }

                    oldGoal.copy().apply {
                        notes = updatedNotes
                    }
                }
            }

            titleText.doOnTextChanged { text, _, _, _ ->
                vm.updateGoal { oldGoal ->
                    oldGoal.copy(title = text.toString()).apply {
                        // No change to notes, just update title
                        notes = oldGoal.notes
                    }
                }
            }
        }
    }



    private fun updateView(goal: Goal) {
        with(binding) {
            if (titleText.text.toString() != goal.title) {
                titleText.setText(goal.title)
            }

            lastUpdatedText.text = DateFormat.format(
                "'Last updated' yyyy-MM-dd 'at' hh:mm:ss a",
                goal.lastUpdated
            )

            pausedCheckbox.isChecked = goal.isPaused
            completedCheckbox.isChecked = goal.isCompleted
            pausedCheckbox.isEnabled = !goal.isCompleted
            completedCheckbox.isEnabled = !goal.isPaused

            // Show or hide the FAB based on the goal's completion status
            if (goal.isCompleted) {
                addProgressButton.hide()
            } else {
                addProgressButton.show()
            }
            val filteredNotes = goal.notes.map { note ->
                note.copy(
                    text = if (note.text.isBlank()) note.type.name else note.text
                )
            }

            // Update the notes in the RecyclerView adapter
            goalNoteListAdapter.updateNotes(filteredNotes)
        }
        updatePhoto(goal)
    }

    private fun updatePhoto(goal:Goal) {
        if (binding.goalPhoto.tag != goal.photoFileName) {
            val photoFile = File(
                requireContext().applicationContext.filesDir,
                goal.photoFileName
            )
            if (photoFile.exists()) {
                Log.w("!!!GDF!!!", "Photo exists; scaling to ImageView")
                binding.goalPhoto.doOnLayout { imageView ->
                    val scaledBitmap = getScaledBitmap(
                        photoFile.path,
                        imageView.width,
                        imageView.height
                    )
                    binding.goalPhoto.setImageBitmap(scaledBitmap)
                    binding.goalPhoto.tag = goal.photoFileName
                    binding.goalPhoto.isEnabled =true
                }

            } else {
                Log.w("!!!GDF!!!", "Photo NOT present")
                binding.goalPhoto.setImageBitmap(null)
                binding.goalPhoto.tag = null
                binding.goalPhoto.isEnabled = false
            }
        }else{
            Log.w("!!!GDF!!!", "Photo found: no scaling required")
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun canResolveIntent(intent: Intent): Boolean{
        return requireActivity().packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        ) != null
    }


}
