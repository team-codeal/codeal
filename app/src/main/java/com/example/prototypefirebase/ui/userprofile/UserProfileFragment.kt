package com.example.prototypefirebase.ui.userprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.prototypefirebase.R

class UserProfileFragment : Fragment() {

    private lateinit var homeViewModel: UserProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(UserProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_userprofile, container, false)
        val textView: TextView = root.findViewById(R.id.text_userprofile)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}