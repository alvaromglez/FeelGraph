package com.example.feelgraph

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.feelgraph.databinding.FragmentSelectionBinding


class SelectionFragment : Fragment() {

    private var _binding: FragmentSelectionBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vibrationButton.setOnClickListener{
            sharedViewModel.userPreference.value = "Vibration"
            findNavController().navigate(R.id.action_selectionFragment_to_principalFragment)
        }

        binding.soundButton.setOnClickListener{
            sharedViewModel.userPreference.value = "Sound"
            findNavController().navigate(R.id.action_selectionFragment_to_principalFragment)
        }
    }

    override fun onDestroyView(){
        super.onDestroyView()
        _binding = null
    }

}