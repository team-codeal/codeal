package com.example.prototypefirebase

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 36
    lateinit var statusBar : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val loginButton : ImageButton = findViewById(R.id.button_log_in)
//        loginButton.setOnClickListener(View.OnClickListener() {
//            fun onClick(view: View) = logIn(view)
//        })
        loginButton.setOnClickListener {
            val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
            startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(providers).build(), RC_SIGN_IN)
        }
        val logoutButton : ImageButton = findViewById(R.id.button_log_out)
        logoutButton.setOnClickListener {
            AuthUI.getInstance().signOut(this).addOnCompleteListener{
                this.statusBar.text = "SIGNED OUT"
            }
        }

        val statusBar : TextView = findViewById(R.id.text_status)
        this.statusBar = statusBar

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            this.statusBar.text = "User " + user.displayName + " is signed in"
        } else {
            this.statusBar.text = "Please, sign in"
        }

        val procceed_button : Button = findViewById(R.id.button_procceed)

        procceed_button.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                this.statusBar.text = "Sorry, first you have to sign in"
            } else {
                this.statusBar.text = "теперь надо замержить с проектом Матвея"
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // signed in
                val user = FirebaseAuth.getInstance().currentUser
                this.statusBar.text = "User " + user!!.displayName + " is signed in"
                }
            } else {
                this.statusBar.text = "AUTH FAILED"
            }
        }
}
