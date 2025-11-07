package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentNotificationsBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notificationsAdapter: NotificationsAdapter

    private val notificationsList = mutableListOf<NotificationItem>()
    private var notificationId = 0

    companion object {
        private const val CHANNEL_ID = "event_app_channel"
        private const val PREFS_NAME = "NotificationPrefs"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_NOTIFICATION_ID = "notification_id"
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        notificationManager = NotificationManagerCompat.from(requireContext())

        setupNotificationChannel()
        setupRecyclerView()
        loadSavedData()
        setupClickListeners()

        // Check and request notification permission if needed
        checkAndRequestNotificationPermission()
    }

    private fun checkAndRequestNotificationPermission() {
        // Notification permission is required only for Android 13 (API 33) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    Toast.makeText(requireContext(), "Notification permission granted", Toast.LENGTH_SHORT).show()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Explain why permission is needed
                    showPermissionRationale()
                }
                else -> {
                    // Request the permission
                    requestNotificationPermission()
                }
            }
        } else {
            // Below Android 13, no runtime permission required for notifications
            Toast.makeText(requireContext(), "Notifications are enabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPermissionRationale() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Notification Permission Needed")
            .setMessage("This app needs notification permission to show you notifications when you create them.")
            .setPositiveButton("Grant") { _, _ ->
                requestNotificationPermission()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Notification permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Event App Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for event app notifications"
            }

            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupRecyclerView() {
        notificationsAdapter = NotificationsAdapter()
        binding.notificationsRecyclerView.apply {
            adapter = notificationsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.sendNotificationButton.setOnClickListener {
            sendNotification()
        }

        binding.clearAllButton.setOnClickListener {
            clearAllNotifications()
        }
    }

    private fun sendNotification() {
        val message = binding.notificationEditText.text.toString().trim()

        if (message.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }

        // Create notification item
        val notificationItem = NotificationItem(
            id = notificationId++,
            message = message,
            timestamp = Date()
        )

        // Add to list and update RecyclerView
        notificationsList.add(0, notificationItem)
        notificationsAdapter.submitList(ArrayList(notificationsList))
        updateEmptyState()

        // Save to SharedPreferences
        saveNotifications()

        // Show system notification (with permission check)
        showSystemNotification(notificationItem)

        // Clear input
        binding.notificationEditText.text?.clear()

        Toast.makeText(requireContext(), "Notification added to history!", Toast.LENGTH_SHORT).show()
    }

    private fun showSystemNotification(notificationItem: NotificationItem) {
        // Check if we have permission to show notifications
        if (!hasNotificationPermission()) {
            Toast.makeText(requireContext(), "Notification permission required to show system notifications", Toast.LENGTH_LONG).show()
            return
        }

        try {
            val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Event Hub")
                .setContentText(notificationItem.message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(notificationItem.id, notification)
            Toast.makeText(requireContext(), "System notification shown!", Toast.LENGTH_SHORT).show()

        } catch (e: SecurityException) {
            // Handle the SecurityException gracefully
            Toast.makeText(requireContext(), "Cannot show notification: Permission denied", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // Handle any other exceptions
            Toast.makeText(requireContext(), "Error showing notification: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For devices below Android 13, notification permission is granted by default
            true
        }
    }

    private fun clearAllNotifications() {
        if (notificationsList.isNotEmpty()) {
            notificationsList.clear()
            notificationsAdapter.submitList(ArrayList(notificationsList))
            updateEmptyState()
            saveNotifications()
            Toast.makeText(requireContext(), "All notifications cleared", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEmptyState() {
        binding.emptyStateText.visibility = if (notificationsList.isEmpty()) View.VISIBLE else View.GONE
        binding.clearAllButton.visibility = if (notificationsList.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun saveNotifications() {
        val gson = Gson()
        val json = gson.toJson(notificationsList)
        sharedPreferences.edit()
            .putString(KEY_NOTIFICATIONS, json)
            .putInt(KEY_NOTIFICATION_ID, notificationId)
            .apply()
    }

    private fun loadSavedData() {
        // Load notifications
        val gson = Gson()
        val json = sharedPreferences.getString(KEY_NOTIFICATIONS, null)
        val type = object : TypeToken<List<NotificationItem>>() {}.type

        if (json != null) {
            val savedNotifications: List<NotificationItem> = gson.fromJson(json, type)
            notificationsList.clear()
            notificationsList.addAll(savedNotifications)
            notificationsAdapter.submitList(ArrayList(notificationsList))
        }

        // Load notification ID
        notificationId = sharedPreferences.getInt(KEY_NOTIFICATION_ID, 0)

        updateEmptyState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}