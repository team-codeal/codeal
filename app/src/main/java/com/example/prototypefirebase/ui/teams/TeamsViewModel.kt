package com.example.prototypefirebase.ui.teams

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TeamsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the teams fragment"
    }
    val text: LiveData<String> = _text
}