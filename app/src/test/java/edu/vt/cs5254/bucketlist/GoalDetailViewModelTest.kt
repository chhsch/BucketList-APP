package edu.vt.cs5254.bucketlist

import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class GoalDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: GoalDetailViewModel
    private lateinit var mockRepository: GoalRepository
    private val sampleGoalId = UUID.randomUUID()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock GoalRepository.get() singleton
        mockRepository = mockk(relaxed = true)
        mockkObject(GoalRepository.Companion)
        every { GoalRepository.get() } returns mockRepository
        // ✅ Define sampleGoal FIRST
        val sampleGoal = Goal(
            id = sampleGoalId,
            title = "Sample Goal",
            lastUpdated = Date()
        ).apply {
            notes = listOf()
        }
        // Provide sample goal to return from repo
        val goal = Goal(
            id = sampleGoalId,
            title = "Delete Me",
            lastUpdated = Date()
        ).apply {
            notes = listOf()
        }
        val note = GoalNote(
            id = UUID.randomUUID(),
            text = "Sample Note",
            type = GoalNoteType.PROGRESS,
            goalId = sampleGoalId
        )


        coEvery { mockRepository.getGoal(sampleGoalId) } returns sampleGoal

        // Initialize ViewModel
        viewModel = GoalDetailViewModel(sampleGoalId)
        testDispatcher.scheduler.advanceUntilIdle() // trigger coroutines
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial goal is loaded correctly`() = runTest {
        val goal = viewModel.goal.value
        assertNotNull(goal)
        assertEquals("Sample Goal", goal?.title)
    }

    @Test
    fun `deleteGoal should call repository`() = runTest {
        val goal = Goal(
            id = sampleGoalId,
            title = "Delete Me",
            lastUpdated = Date()
        ).apply {
            notes = listOf()
        }

        viewModel.deleteGoal(goal)
        advanceUntilIdle()

        coVerify { mockRepository.deleteGoal(goal) }
    }


    @Test
    fun `removeNoteFromGoal should update notes`() = runTest {
        val note = GoalNote(
            id = UUID.randomUUID(),
            text = "Test Note",
            type = GoalNoteType.COMPLETED,
            goalId = sampleGoalId
        )

        val updatedNotes = listOf<GoalNote>() // mock return from repo
        coEvery { mockRepository.getNotesForGoal(sampleGoalId) } returns updatedNotes

        viewModel.removeNoteFromGoal(note)
        advanceUntilIdle()

        coVerify { mockRepository.deleteNote(note) }
        coVerify { mockRepository.getNotesForGoal(sampleGoalId) }

        assertEquals(updatedNotes, viewModel.goal.value?.notes)
    }

}
