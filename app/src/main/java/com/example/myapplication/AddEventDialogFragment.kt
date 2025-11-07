package com.example.myapplication

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import java.util.Calendar

class AddEventDialogFragment : DialogFragment() {

    // Use activityViewModels to share the same ViewModel instance
    private val viewModel: EventViewModel by activityViewModels {
        val app = requireActivity().application as SAEventHubApplication
        EventViewModelFactory(app.eventRepository)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_add_event, null)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val etLocation = view.findViewById<EditText>(R.id.etLocation)
        val etPrice = view.findViewById<EditText>(R.id.etPrice)
        val spinnerCategory = view.findViewById<Spinner>(R.id.spinnerCategory)

        // Setup category spinner
        val categories = arrayOf("Music", "Sports", "Food", "Arts", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Add New Event")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Add Event") { _, _ ->
                val title = etTitle.text.toString()
                val description = etDescription.text.toString()
                val location = etLocation.text.toString()
                val priceText = etPrice.text.toString()
                val category = spinnerCategory.selectedItem.toString()

                if (title.isBlank() || description.isBlank() || location.isBlank() || priceText.isBlank()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val price = try {
                    priceText.toFloat()
                } catch (e: NumberFormatException) {
                    Toast.makeText(requireContext(), "Please enter a valid price", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Set date to 7 days from now by default
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, 7)
                val date = calendar.timeInMillis

                val event = Event(
                    title = title,
                    description = description,
                    date = date,
                    location = location,
                    category = category,
                    price = price,
                    imageUrl = "",
                    isFeatured = false
                )

                viewModel.addEvent(event)
                Toast.makeText(requireContext(), "Event added successfully!", Toast.LENGTH_SHORT).show()
            }
            .create()
    }
}