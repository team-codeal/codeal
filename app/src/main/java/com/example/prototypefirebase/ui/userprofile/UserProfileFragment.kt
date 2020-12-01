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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

//TODO this code needs a drying, especially unifying the database names, or
// possibly carrying database-related stuff in some other helper class

class UserProfileFragment : Fragment() {

    private lateinit var userProfileViewModel: UserProfileViewModel

    private lateinit var user: FirebaseUser

    private var userName: String? = "default_user_name"
    private var userBio: String? = "default_user_bio"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        user = FirebaseAuth.getInstance().currentUser!! // assertion comes from an assumption that
                                                        // the user has already signed it

        val db = FirebaseFirestore.getInstance()

        db.collection("user_profiles").document(user.uid).get()
            .addOnSuccessListener { profile ->
                userName = profile?.get("name") as String? ?:
                                          (user.displayName ?: "Your future user name")
                userBio = profile?.get("bio") as String? ?: ""
                updateUserProfileUI()
            }
            .addOnFailureListener { _ ->
                userName = "Your future user name"
                userBio = ""
                updateUserProfileUI()
            }

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
        userNameHolder?.setText(userName)

        val userBioHolder: EditText? = view?.findViewById(R.id.user_bio)
        userBioHolder?.setText(userBio)

    }

    private fun saveCurrentUserProfile() {

        val userNameHolder: EditText? = view?.findViewById(R.id.user_name)
        val newUserName = userNameHolder?.text.toString()

        val userBioHolder: EditText? = view?.findViewById(R.id.user_bio)
        val newUserBio = userBioHolder?.text.toString()

        val userData = hashMapOf(
            "name" to newUserName,
            "bio" to newUserBio
        )

        val usersDB = FirebaseFirestore.getInstance().collection("user_profiles")

        usersDB.document(user.uid).set(userData)

    }

}