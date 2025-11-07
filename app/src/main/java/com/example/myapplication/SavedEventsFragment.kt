package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentSavedEventsBinding
import kotlinx.coroutines.launch

class SavedEventsFragment : Fragment() {

    private var _binding: FragmentSavedEventsBinding? = null
    private val binding get() = _binding!!

    // Use activityViewModels to share the same ViewModel instance with MainActivity
    private val viewModel: EventViewModel by activityViewModels {
        val app = requireActivity().application as SAEventHubApplication
        EventViewModelFactory(app.eventRepository)
    }

    private lateinit var eventsAdapter: EventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        // Load saved events for current user
        viewModel.currentUser.value?.let { user ->
            println("SavedEventsFragment - Loading saved events for user: ${user.username}")
            viewModel.loadSavedEvents(user.id)
        } ?: run {
            println("SavedEventsFragment - No user logged in")
        }
    }

    private fun setupRecyclerView() {
        eventsAdapter = EventsAdapter(
            onSaveClick = { event ->
                viewModel.currentUser.value?.let { user ->
                    println("SavedEventsFragment - User ${user.username} unsaving event ${event.id}")
                    viewModel.unsaveEvent(user.id, event.id)
                }
            },
            isEventSaved = { eventId ->
                true // All events in this fragment are saved
            }
        )

        binding.savedEventsRecyclerView.apply {
            adapter = eventsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.savedEvents.collect { events ->
                println("SavedEventsFragment - Saved events updated: ${events.size} events")
                // Check if binding is available before updating UI
                if (_binding != null) {
                    eventsAdapter.submitList(events)
                    binding.emptyState.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        // Observe current user to reload saved events when user changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentUser.collect { user ->
                println("SavedEventsFragment - User state changed: $user")
                // Check if binding is available before updating UI
                if (_binding != null) {
                    user?.let {
                        viewModel.loadSavedEvents(it.id)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}