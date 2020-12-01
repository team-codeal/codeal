package com.example.prototypefirebase.ui.userprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserProfileViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the user profile fragment"
    }
    val text: LiveData<String> = _text
}