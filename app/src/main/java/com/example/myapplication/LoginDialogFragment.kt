package com.example.myapplication

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton

class LoginDialogFragment : DialogFragment() {

    // Use activityViewModels to share the same ViewModel instance with the activity
    private val viewModel: EventViewModel by activityViewModels {
        val app = requireActivity().application as SAEventHubApplication
        EventViewModelFactory(app.eventRepository)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_login, null)

        val etUsername = view.findViewById<android.widget.EditText>(R.id.etUsername)
        val etEmail = view.findViewById<android.widget.EditText>(R.id.etEmail)
        val etPassword = view.findViewById<android.widget.EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<MaterialButton>(R.id.btnLogin)
        val btnRegister = view.findViewById<MaterialButton>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.loginUser(email, password) { user ->
                if (user != null) {
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Login failed. Check credentials.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val username = etUsername.text.toString()

            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.registerUser(username, email, password) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Registration successful! You can now login.", Toast.LENGTH_SHORT).show()
                    // Auto-login after registration
                    viewModel.loginUser(email, password) { user ->
                        if (user != null) {
                            Toast.makeText(requireContext(), "Auto-login successful!", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Registration failed. Email or username may already exist.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Login / Register")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }
}