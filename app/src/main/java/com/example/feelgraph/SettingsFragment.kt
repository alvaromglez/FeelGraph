package com.example.feelgraph

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.feelgraph.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var vibrationIntensity = 50


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadIntensity()
        setupButtons()
        updateIntensity()

        binding.returnButton.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_principalFragment)
        }

    }

    private fun setupButtons(){
        binding.increaseButton.setOnClickListener{
            if (vibrationIntensity < 100){
                vibrationIntensity += 10
                saveIntensity()
                updateIntensity()
            }
        }

        binding.decreaseButton.setOnClickListener{
            if (vibrationIntensity > 10){
                vibrationIntensity -= 10
                saveIntensity()
                updateIntensity()
            }
        }
    }

    private fun updateIntensity(){
        binding.intensityText.text = "Intensidad: $vibrationIntensity%"
        VibrationManager.setIntensity(vibrationIntensity)
    }

    private fun saveIntensity(){
        val sharedPrefs = activity?.getSharedPreferences("VibrationSettings", Context.MODE_PRIVATE)
        sharedPrefs?.edit()?.putInt("VibrationIntensity", vibrationIntensity)?.apply()
    }
    private fun loadIntensity(){
        val sharedPrefs = activity?.getSharedPreferences("VibrationSettings", Context.MODE_PRIVATE)
        vibrationIntensity = sharedPrefs?.getInt("VibrationIntensity", 50) ?: 50

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}