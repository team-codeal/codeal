package com.example.prototypefirebase.ui.userprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.prototypefirebase.R

class UserProfileFragment : Fragment() {

    private lateinit var userProfileViewModel: UserProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        userProfileViewModel =
            ViewModelProvider(this).get(UserProfileViewModel::class.java)

        return inflater.inflate(R.layout.fragment_userprofile, container, false)
    }
}