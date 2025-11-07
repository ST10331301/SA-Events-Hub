package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.myapplication.databinding.FragmentEventsBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
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
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        setupSearch()
        observeViewModel()

        // Debug: Check current user state
        println("EventsFragment - Current user: ${viewModel.currentUser.value}")
    }

    private fun setupRecyclerView() {
        eventsAdapter = EventsAdapter(
            onSaveClick = { event ->
                viewModel.currentUser.value?.let { user ->
                    println("EventsFragment - User ${user.username} saving event ${event.id}")
                    if (viewModel.isEventSaved(event.id)) {
                        viewModel.unsaveEvent(user.id, event.id)
                        Toast.makeText(requireContext(), "Event unsaved", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.saveEvent(user.id, event.id)
                        Toast.makeText(requireContext(), "Event saved", Toast.LENGTH_SHORT).show()
                    }
                } ?: run {
                    println("EventsFragment - No user logged in, showing login dialog")
                    // Show login dialog when user is not logged in
                    showLoginDialog()
                }
            },
            isEventSaved = { eventId ->
                viewModel.isEventSaved(eventId)
            }
        )

        binding.eventsRecyclerView.apply {
            adapter = eventsAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    private fun showLoginDialog() {
        val loginDialog = LoginDialogFragment()
        loginDialog.show(parentFragmentManager, "login_dialog")
    }

    private fun setupClickListeners() {
        binding.chipAll.setOnClickListener {
            viewModel.loadAllEvents()
            clearChipSelection()
            binding.chipAll.isChecked = true
        }

        binding.chipMusic.setOnClickListener {
            viewModel.loadEventsByCategory("Music")
            clearChipSelection()
            binding.chipMusic.isChecked = true
        }

        binding.chipSports.setOnClickListener {
            viewModel.loadEventsByCategory("Sports")
            clearChipSelection()
            binding.chipSports.isChecked = true
        }

        binding.chipFood.setOnClickListener {
            viewModel.loadEventsByCategory("Food")
            clearChipSelection()
            binding.chipFood.isChecked = true
        }

        binding.chipArts.setOnClickListener {
            viewModel.loadEventsByCategory("Arts")
            clearChipSelection()
            binding.chipArts.isChecked = true
        }
    }

    private fun clearChipSelection() {
        binding.chipAll.isChecked = false
        binding.chipMusic.isChecked = false
        binding.chipSports.isChecked = false
        binding.chipFood.isChecked = false
        binding.chipArts.isChecked = false
    }

    private fun setupSearch() {
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchEditText.text.toString()
                if (query.isNotEmpty()) {
                    viewModel.searchEvents(query)
                }
                true
            } else {
                false
            }
        }
    }

    private fun observeViewModel() {
        // Use viewLifecycleOwner.lifecycleScope for UI updates
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collect { events ->
                // Check if binding is available before updating UI
                if (_binding != null) {
                    eventsAdapter.submitList(events)
                    binding.emptyState.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collect { results ->
                // Check if binding is available before updating UI
                if (_binding != null && results.isNotEmpty()) {
                    eventsAdapter.submitList(results)
                    binding.emptyState.visibility = View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.savedEvents.collect {
                // Check if binding is available before updating UI
                if (_binding != null) {
                    eventsAdapter.notifyDataSetChanged()
                }
            }
        }

        // Observe current user to refresh UI when login state changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentUser.collect { user ->
                println("EventsFragment - User state changed: $user")
                // Check if binding is available before updating UI
                if (_binding != null) {
                    eventsAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}