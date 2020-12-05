package com.example.prototypefirebase.ui.userprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.prototypefirebase.R
import com.example.prototypefirebase.codeal.CodealUser
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileFragment : Fragment() {

    private lateinit var userProfileViewModel: UserProfileViewModel

    private lateinit var user: CodealUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CodealUser(::updateUserProfileUI)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        userProfileViewModel =
            ViewModelProvider(this).get(UserProfileViewModel::class.java)

        return inflater.inflate(R.layout.fragment_userprofile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUserProfileUI()

        val saveButton: Button? = view.findViewById(R.id.button_send_to_db)

        saveButton?.setOnClickListener {
            saveCurrentUserProfile()
            // updateUserProfileUI()
        }

    }

    private fun updateUserProfileUI() {

        val userNameHolder: EditText? = view?.findViewById(R.id.user_name)
        userNameHolder?.setText(user.name)

        val userBioHolder: EditText? = view?.findViewById(R.id.user_bio)
        userBioHolder?.setText(user.bio)

    }

    private fun saveCurrentUserProfile() {

        val userNameHolder: EditText? = view?.findViewById(R.id.user_name)
        val newUserName = userNameHolder?.text.toString()

        val userBioHolder: EditText? = view?.findViewById(R.id.user_bio)
        val newUserBio = userBioHolder?.text.toString()

        user.change(name = newUserName, bio = newUserBio)

    }

}