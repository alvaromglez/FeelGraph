package com.example.feelgraph

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel: ViewModel() {
    val userPreference = MutableLiveData<String>()
}
