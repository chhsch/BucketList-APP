package edu.vt.cs5254.bucketlist
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.bucketlist.databinding.FragmentGoalListBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


class GoalListFragment: Fragment(){
    private var _binding: FragmentGoalListBinding? = null
    private val binding
        get() = checkNotNull(_binding){"FragmentGoalListBinding is null!!!"}

    private val vm: GoalListViewModel by viewModels()
    private lateinit var adapter: GoalListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        requireActivity().addMenuProvider(object : MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_goal_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId){
                    R.id.new_goal -> {
                        showNewGoal()
                        true
                    }
                    else -> false
                }
            }

        }, viewLifecycleOwner)

        _binding = FragmentGoalListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Attach ItemTouchHelper for swipe-to-delete functionality
        getItemTouchHelper().attachToRecyclerView(binding.goalRecyclerView)
        binding.goalRecyclerView.layoutManager = LinearLayoutManager(context)

        // Set onClickListener for the "no goal" button to add a new goal
        binding.noGoalButton.setOnClickListener {
            showNewGoal()  // Calls the helper function to add a new goal
        }
        // Observe the goals from the ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.goals.collect { goals ->

                    // Set visibility of RecyclerView and "no goals" message/button based on list contents
                    if (goals.isEmpty()) {
                        binding.goalRecyclerView.visibility = View.GONE
                        binding.noGoalText.visibility = View.VISIBLE
                        binding.noGoalButton.visibility = View.VISIBLE
                    } else {
                        binding.goalRecyclerView.visibility = View.VISIBLE
                        binding.noGoalText.visibility = View.GONE
                        binding.noGoalButton.visibility = View.GONE
                    }
                    // Update adapter with new goals list
                    binding.goalRecyclerView.adapter = GoalListAdapter(goals) { goalId ->
                        findNavController().navigate(GoalListFragmentDirections.showDetail(goalId))
                    }
                }
            }
        }
    }

    private fun showNewGoal() {
// save it to the db
    viewLifecycleOwner.lifecycleScope.launch {
        val goal = Goal()
        vm.addGoal(goal)
        findNavController().navigate(GoalListFragmentDirections.showDetail(goal.id))
    }

    }
    private fun deletePhotoFile(photoFileName: String) {
        val photoFile = File(requireContext().filesDir, photoFileName)
        if (photoFile.exists()) {
            photoFile.delete()
        }
    }

private fun getItemTouchHelper(): ItemTouchHelper {
    return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val goalHolder = viewHolder as? GoalHolder
            val swipedGoal = goalHolder?.boundGoal

            if (swipedGoal != null) {
                // Delete the goal from ViewModel and update adapter
                vm.deleteGoal(swipedGoal)
                deletePhotoFile(swipedGoal.photoFileName)

            }
        }


    })
}
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
