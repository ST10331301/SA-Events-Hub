package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.myapplication.databinding.FragmentLanguageSettingsBinding
import java.util.*

class LanguageSettingsFragment : Fragment() {

    private var _binding: FragmentLanguageSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PREFS_NAME = "AppPreferences"
        private const val KEY_LANGUAGE = "app_language"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        setupLanguageSpinner()
    }

    private fun setupLanguageSpinner() {
        val languages = arrayOf("English", "Afrikaans", "Zulu")
        val languageCodes = arrayOf("en", "af", "zu")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.languageSpinner.adapter = adapter

        // Set current language selection
        val savedLanguage = sharedPreferences.getString(KEY_LANGUAGE, "en") ?: "en"
        val position = languageCodes.indexOf(savedLanguage)
        if (position >= 0) {
            binding.languageSpinner.setSelection(position)
        }

        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = languageCodes[position]
                if (sharedPreferences.getString(KEY_LANGUAGE, "en") != selectedLanguage) {
                    saveLanguage(selectedLanguage)
                    requireActivity().recreate() // Restart activity to apply language change
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun saveLanguage(language: String) {
        sharedPreferences.edit().putString(KEY_LANGUAGE, language).apply()
        setAppLanguage(language)
    }

    private fun setAppLanguage(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources: Resources = requireActivity().resources
        val configuration: Configuration = resources.configuration
        configuration.setLocale(locale)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            requireActivity().createConfigurationContext(configuration)
        }

        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}