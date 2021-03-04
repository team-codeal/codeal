package com.example.prototypefirebase.ui.userprofile

import android.annotation.SuppressLint
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
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
        user = CodealUser{ updateUserProfileUI(view) }
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

        updateUserProfileUI(view)

        val ctx = requireContext()

        val goEditButton : Button = view.findViewById(R.id.button_go_edit)
        goEditButton.setOnClickListener(::showEditProfilePopupWindow)

        val logoutButton : Button = view.findViewById(R.id.button_log_out)
        logoutButton.setOnClickListener {
            AuthUI.getInstance().signOut(ctx).addOnCompleteListener{
                Toast.makeText(ctx, "Signed out", Toast.LENGTH_SHORT).show()
                val intent = Intent(ctx, SignInActivity::class.java)
                startActivity(intent)
            }
        }

    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    fun showEditProfilePopupWindow(view : View) {

        val inflater : LayoutInflater =  requireActivity().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.fragment_userprofile_edit, null);

        val focusable = true
        val size = LinearLayout.LayoutParams.MATCH_PARENT
        val popupWindow = PopupWindow(popupView, size, size, focusable)

        updateUserProfileUI(popupView)
        popupWindow.setOnDismissListener{ updateUserProfileUI(this.view) }
        val saveButton : Button = popupView.findViewById(R.id.button_save)
        saveButton.setOnClickListener {
            saveCurrentUserProfile(popupView)
            popupWindow.dismiss()
        }

        popupWindow.animationStyle = android.R.style.Animation_InputMethod

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    private fun updateUserProfileUI(view: View?) {

        val userNameHolder: TextView? = view?.findViewById(R.id.user_name)
        userNameHolder?.text = user.name

        val userBioHolder: TextView? = view?.findViewById(R.id.user_bio)
        userBioHolder?.text = user.bio

    }

    private fun saveCurrentUserProfile(view: View?) {

        val userNameHolder: EditText? = view?.findViewById(R.id.user_name)
        val newUserName = userNameHolder?.text.toString()

        val userBioHolder: EditText? = view?.findViewById(R.id.user_bio)
        val newUserBio = userBioHolder?.text.toString()

        user.change(name = newUserName, bio = newUserBio)

    }

}