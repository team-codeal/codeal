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
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.prototypefirebase.R
import com.example.prototypefirebase.SignInActivity
import com.example.prototypefirebase.codeal.CodealUser
import com.example.prototypefirebase.codeal.factories.CodealUserFactory
import com.firebase.ui.auth.AuthUI
import de.hdodenhof.circleimageview.CircleImageView


class UserProfileFragment : Fragment() {

    private var user: CodealUser? = null

    private val gifUris: List<Uri> = listOf(
        "https://64.media.tumblr.com/ec90e55981cd01a3e1a69e724967a31c/tumblr_ntb1nzAguK1qc4uvwo1_500.gifv",
        "https://64.media.tumblr.com/tumblr_md923niK1p1qc4uvwo1_400.gifv",
        "https://64.media.tumblr.com/2df037db5c351a7efd53be9336e84a4d/tumblr_nchjzfYiQ41qc4uvwo1_500.gifv",
        "https://64.media.tumblr.com/b0fac2120f36d47ceab265b33dbddfb9/tumblr_ntrtdpT1XK1qc4uvwo1_r1_500.gifv",
        "https://64.media.tumblr.com/d0bf998f5ee219e3ee2739c8ac1d103f/tumblr_pxe84jIwi01qc4uvwo1_500.jpg",
        "https://64.media.tumblr.com/d9e91c42fe8548f6d1d84c5bb7a2d6c8/tumblr_oelq32ypdd1qc4uvwo1_500.gifv",
        "https://64.media.tumblr.com/14d80d4fc0dfe2a75a9e62ec4a324c38/tumblr_okxk4uqVb31qc4uvwo1_500.gifv"
    ).map { Uri.parse(it) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_userprofile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val goEditButton : Button = view.findViewById(R.id.button_go_edit)
        goEditButton.setOnClickListener(::showEditProfilePopupWindow)

    }

    override fun onStart() {
        super.onStart()
        val userAvatarHolder: CircleImageView = requireView().findViewById(R.id.user_avatar)
        CodealUserFactory.get().addOnReady { user ->
            this.user = user
            updateUserProfileUI(view, user)
            loadUserAvatarToView(userAvatarHolder)
        }
        val motivationHolder: ImageView = requireView().findViewById(R.id.motivational_gif)
        loadMotivationalGif(motivationHolder)
        motivationHolder.setOnClickListener { loadMotivationalGif(motivationHolder) }

        val heartReactionHolder: ImageView = requireView().findViewById(R.id.heart_reaction)
        CodealUserFactory.get().incomingReactionCallback = {
            heartReactionHolder.visibility = View.VISIBLE
            heartReactionHolder.animate()
                .scaleYBy(2f)
                .scaleXBy(2f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(1000)
                .withEndAction {
                    heartReactionHolder.visibility = View.INVISIBLE
                    heartReactionHolder.animate()
                        .scaleXBy(-2f)
                        .scaleYBy(-2f)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .duration = 0
                }
        }
    }

    private fun loadMotivationalGif(motivationHolder: ImageView) {
        Glide.with(requireContext())
            .load(gifUris.random())
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

        val ctx = requireContext()

        val logoutButton : Button = popupView.findViewById(R.id.button_log_out)
        logoutButton.setOnClickListener {
            AuthUI.getInstance().signOut(ctx).addOnCompleteListener{
                Toast.makeText(ctx, "Signed out", Toast.LENGTH_SHORT).show()
                val intent = Intent(ctx, SignInActivity::class.java)
                requireActivity().finish()
                startActivity(intent)
            }
        }


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