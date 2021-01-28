package com.example.prototypefirebase.ui.userprofile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.prototypefirebase.R
import com.example.prototypefirebase.SignInActivity
import com.example.prototypefirebase.codeal.CodealUser
import com.firebase.ui.auth.AuthUI

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

        val saveButton: Button = view.findViewById(R.id.button_send_to_db)

        saveButton.setOnClickListener {
            saveCurrentUserProfile()
            // updateUserProfileUI()
        }

        val ctx = requireContext()

        val logoutButton : Button = view.findViewById(R.id.button_log_out)
        logoutButton.setOnClickListener {
            AuthUI.getInstance().signOut(ctx).addOnCompleteListener{
                Toast.makeText(ctx, "Signed out", Toast.LENGTH_SHORT).show()
                val intent = Intent(ctx, SignInActivity::class.java)
                startActivity(intent)
            }
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