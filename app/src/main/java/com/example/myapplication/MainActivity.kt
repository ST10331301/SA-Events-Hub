package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    // Get the application instance
    private val app by lazy { application as SAEventHubApplication }

    // Add ViewModel to MainActivity
    private val viewModel: EventViewModel by viewModels {
        EventViewModelFactory(app.eventRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the toolbar as the action bar
        setSupportActionBar(binding.toolbar)

        // Initialize sample data first
        initializeSampleData()

        // Setup navigation
        setupNavigation()

        // Debug navigation setup
        debugNavigationSetup()
    }

    private fun setupNavigation() {
        // Use NavHostFragment directly
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // UPDATE: Include ALL fragments in AppBarConfiguration
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.eventsFragment,
                R.id.savedEventsFragment,
                R.id.profileFragment,
                R.id.notificationsFragment,        // ADD THIS
                R.id.languageSettingsFragment      // ADD THIS
            ),
            binding.drawerLayout
        )

        // Setup action bar with nav controller
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Setup navigation view - THIS IS CRITICAL
        binding.navView.setupWithNavController(navController)

        // ADD MANUAL NAVIGATION LISTENER AS BACKUP
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            Log.d("Navigation", "Manual navigation triggered for: ${menuItem.title} (ID: ${menuItem.itemId})")

            // Handle the navigation manually
            when (menuItem.itemId) {
                R.id.eventsFragment -> {
                    Log.d("Navigation", "Navigating to EventsFragment")
                    if (navController.currentDestination?.id != R.id.eventsFragment) {
                        navController.navigate(R.id.eventsFragment)
                    }
                }
                R.id.savedEventsFragment -> {
                    Log.d("Navigation", "Navigating to SavedEventsFragment")
                    if (navController.currentDestination?.id != R.id.savedEventsFragment) {
                        navController.navigate(R.id.savedEventsFragment)
                    }
                }
                R.id.profileFragment -> {
                    Log.d("Navigation", "Navigating to ProfileFragment")
                    if (navController.currentDestination?.id != R.id.profileFragment) {
                        navController.navigate(R.id.profileFragment)
                    }
                }
                // ADD THESE NEW CASES:
                R.id.notificationsFragment -> {
                    Log.d("Navigation", "Navigating to NotificationsFragment")
                    if (navController.currentDestination?.id != R.id.notificationsFragment) {
                        navController.navigate(R.id.notificationsFragment)
                    }
                }
                R.id.languageSettingsFragment -> {
                    Log.d("Navigation", "Navigating to LanguageSettingsFragment")
                    if (navController.currentDestination?.id != R.id.languageSettingsFragment) {
                        navController.navigate(R.id.languageSettingsFragment)
                    }
                }
            }

            // Close the drawer
            binding.drawerLayout.closeDrawer(binding.navView)
            true
        }

        // Add hamburger icon
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        binding.fabAddEvent.setOnClickListener {
            showAddEventDialog()
        }

        // Debug: Log the current setup
        Log.d("Navigation", "NavController graph: ${navController.graph}")
        Log.d("Navigation", "Current destination: ${navController.currentDestination?.id}")
    }

    private fun debugNavigationSetup() {
        try {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            Log.d("Navigation", "=== NAVIGATION DEBUG ===")
            Log.d("Navigation", "Graph ID: ${navController.graph.id}")

            // Log all destinations
            navController.graph.forEach { destination ->
                Log.d("Navigation", "Destination: ${destination.label} (ID: ${destination.id})")
            }

            // Check if menu items match fragment IDs
            val menu = binding.navView.menu
            Log.d("Navigation", "=== MENU ITEMS ===")
            for (i in 0 until menu.size()) {
                val item = menu.getItem(i)
                Log.d("Navigation", "Menu item: ${item.title} (ID: ${item.itemId})")

                // Check if menu item ID exists in navigation graph
                val destinationExists = navController.graph.findNode(item.itemId) != null
                Log.d("Navigation", "  - Destination exists: $destinationExists")
            }

            Log.d("Navigation", "=== CURRENT STATE ===")
            Log.d("Navigation", "Current destination: ${navController.currentDestination?.id}")
            Log.d("Navigation", "=== END DEBUG ===")

        } catch (e: Exception) {
            Log.e("Navigation", "Error in debugNavigationSetup: ${e.message}")
        }
    }

    private fun initializeSampleData() {
        val scope = CoroutineScope(Dispatchers.IO)
        val initializer = DatabaseInitializer(applicationContext, scope)
        initializer.initializeSampleData()
    }

    private fun showAddEventDialog() {
        // Check if user is logged in using ViewModel
        if (viewModel.currentUser.value == null) {
            Toast.makeText(this, "Please login to add events", Toast.LENGTH_SHORT).show()
            return
        }

        // For now, let any logged-in user add events
        val addEventDialog = AddEventDialogFragment()
        addEventDialog.show(supportFragmentManager, "add_event_dialog")
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
            binding.drawerLayout.closeDrawer(binding.navView)
        } else {
            super.onBackPressed()
        }
    }
}