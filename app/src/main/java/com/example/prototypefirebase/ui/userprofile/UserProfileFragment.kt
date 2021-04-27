package com.example.prototypefirebase.ui.userprofile

import android.annotation.SuppressLint
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.prototypefirebase.R
import com.example.prototypefirebase.SignInActivity
import com.example.prototypefirebase.codeal.CodealUser
import com.firebase.ui.auth.AuthUI
import de.hdodenhof.circleimageview.CircleImageView


class UserProfileFragment : Fragment() {

    private var user: CodealUser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_userprofile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


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

    override fun onStart() {
        super.onStart()
        val userAvatarHolder: CircleImageView = requireView().findViewById(R.id.user_avatar)
        CodealUser { user ->
            this.user = user
            updateUserProfileUI(view, user)
            loadUserAvatarToView(userAvatarHolder)
        }
        val motivationHolder: ImageView = requireView().findViewById(R.id.motivational_gif)
        Glide.with(requireContext())
            .load(Uri
                .parse("https://64.media.tumblr.com/" +
                        "ec90e55981cd01a3e1a69e724967a31c/" +
                        "tumblr_ntb1nzAguK1qc4uvwo1_500.gifv"))
            .apply(
                RequestOptions()
                    .fitCenter()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .override(Target.SIZE_ORIGINAL)
            )
            .into(motivationHolder)
    }

    private fun loadUserAvatarToView(avatarHolder: CircleImageView) {
        Glide.with(requireContext()).load(user?.photoURL)
            .apply(
                RequestOptions()
                    .fitCenter()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .override(Target.SIZE_ORIGINAL)
            )
            .into(avatarHolder)
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    fun showEditProfilePopupWindow(view: View) {

        val inflater : LayoutInflater =  requireActivity().getSystemService(LAYOUT_INFLATER_SERVICE)
                as LayoutInflater
        val popupView = inflater.inflate(R.layout.fragment_userprofile_edit, null);

        val userAvatarHolder: CircleImageView = popupView.findViewById(R.id.user_avatar)
        loadUserAvatarToView(userAvatarHolder)


        val focusable = true
        val size = LinearLayout.LayoutParams.MATCH_PARENT
        val popupWindow = PopupWindow(popupView, size, size, focusable)

        updateUserProfileUI(popupView, user)
        popupWindow.setOnDismissListener{ updateUserProfileUI(this.view, user) }
        val saveButton : Button = popupView.findViewById(R.id.button_save)
        saveButton.setOnClickListener {
            saveCurrentUserProfile(popupView, user)
            popupWindow.dismiss()
        }

        popupWindow.animationStyle = android.R.style.Animation_InputMethod

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    private fun updateUserProfileUI(view: View?, user: CodealUser?) {

        val userNameHolder: TextView? = view?.findViewById(R.id.user_name)
        userNameHolder?.text = user?.name

        val userBioHolder: TextView? = view?.findViewById(R.id.user_bio)
        userBioHolder?.text = user?.bio

        val userStatusHolder: TextView? = view?.findViewById(R.id.user_status)
        userStatusHolder?.text = user?.status

    }

    private fun saveCurrentUserProfile(view: View?, user: CodealUser?) {

        val userNameHolder: EditText? = view?.findViewById(R.id.user_name)
        val newUserName = userNameHolder?.text.toString()

        val userBioHolder: EditText? = view?.findViewById(R.id.user_bio)
        val newUserBio = userBioHolder?.text.toString()

        val userStatusHolder: EditText? = view?.findViewById(R.id.user_status)
        val newUserStatus = userStatusHolder?.text.toString()

        user?.change(name = newUserName, bio = newUserBio, status = newUserStatus)

    }

}