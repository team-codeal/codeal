package com.example.prototypefirebase

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prototypefirebase.codeal.CodealUser
import com.example.prototypefirebase.codeal.factories.CodealUserFactory
import com.firebase.ui.auth.AuthUI

class SignInActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 36
    private var user: CodealUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide();
        try {
            // TODO this violates the rules, but for checking auth we have to actually
            //  rebuild the user object
            user = CodealUser()
            logInSuccessful()
        } catch (_: IllegalStateException) {}

        setContentView(R.layout.activity_signin)

        val loginButton : ImageButton = findViewById(R.id.button_log_in)
        loginButton.setOnClickListener {
            val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
            startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(providers).build(), RC_SIGN_IN)
        }

    }

    private fun logInSuccessful() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

            if (resultCode == Activity.RESULT_OK) {
                // signed in
                try {
                    CodealUserFactory.get().addOnReady {
                        user = it
                        Toast.makeText(this, "Hello ${user?.name}!", Toast.LENGTH_SHORT).show()
                    }
                    logInSuccessful()
                } catch (_: java.lang.IllegalStateException) {
                    Toast.makeText(this, "Weird error during signing in...", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error occurred during signing in", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
