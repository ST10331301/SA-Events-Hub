package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.FragmentProfileBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Use activityViewModels to share the same ViewModel instance with MainActivity
    private val viewModel: EventViewModel by activityViewModels {
        val app = requireActivity().application as SAEventHubApplication
        EventViewModelFactory(app.eventRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setupClickListeners()

        // Debug: Check current user state
        println("ProfileFragment - Current user: ${viewModel.currentUser.value}")
    }
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentUser.collect { user ->
                println("ProfileFragment - User state changed: $user")
                // Check if binding is available before updating UI
                if (_binding != null) {
                    if (user != null) {
                        showUserProfile(user)
                    } else {
                        showLoginSection()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.savedEvents.collect { savedEvents ->
                // Check if binding is available before updating UI
                if (_binding != null) {
                    binding.txtSavedCount.text = savedEvents.size.toString()
                    println("ProfileFragment - Saved events count: ${savedEvents.size}")
                }
            }
        }
    }

    private fun showUserProfile(user: User) {
        binding.userName.text = user.username
        binding.userEmail.text = user.email
        binding.loginSection.visibility = View.GONE
        binding.profileSection.visibility = View.VISIBLE
        binding.txtSavedCount.text = viewModel.savedEvents.value.size.toString()

        // Debug log
        println("ProfileFragment - Showing user profile: ${user.username}")
    }

    private fun showLoginSection() {
        binding.loginSection.visibility = View.VISIBLE
        binding.profileSection.visibility = View.GONE
        binding.userName.text = "Guest User"
        binding.userEmail.text = "Please login to continue"
        binding.txtSavedCount.text = "0"

        // Debug log
        println("ProfileFragment - Showing login section")
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            showLoginDialog()
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
        }

        // Add test user creation for demo
        binding.btnTestLogin.setOnClickListener {
            createTestUserAndLogin()
        }
    }

    private fun showLoginDialog() {
        val loginDialog = LoginDialogFragment()
        loginDialog.show(parentFragmentManager, "login_dialog")
    }


    private fun createTestUserAndLogin() {
        // Create a test user if it doesn't exist, then login
        viewModel.registerUser("testuser", "test@example.com", "password123") { success ->
            if (success) {
                // Login with the test user
                viewModel.loginUser("test@example.com", "password123") { user ->
                    if (user != null) {
                        Toast.makeText(requireContext(), "Test user created and logged in!", Toast.LENGTH_SHORT).show()
                        println("Test user logged in: $user")
                    }
                }
            } else {
                // If user already exists, just login
                viewModel.loginUser("test@example.com", "password123") { user ->
                    if (user != null) {
                        Toast.makeText(requireContext(), "Logged in with test user!", Toast.LENGTH_SHORT).show()
                        println("Existing test user logged in: $user")
                    } else {
                        Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show()
                        println("Login failed for test user")
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