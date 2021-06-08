package com.example.prototypefirebase

import android.os.Bundle
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.prototypefirebase.codeal.CodealEntity
import com.example.prototypefirebase.codeal.CodealUser
import com.example.prototypefirebase.codeal.suppliers.CodealUserSupplier

class MainActivity : AppCompatActivity() {

    private lateinit var userInfoListener: CodealEntity<CodealUser>.CodealListener
    private lateinit var userNameHolder: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        supportActionBar?.hide();
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navController.popBackStack(R.id.navigation_home, true)
        navController.navigate(R.id.navigation_notifications)

        userNameHolder = findViewById(R.id.user_name)
    }

    override fun onResume() {
        super.onResume()
        userInfoListener = CodealUserSupplier.get().addListener { possiblyUpdatedUser ->
            userNameHolder.text = possiblyUpdatedUser.name
        }
    }

    override fun onStop() {
        super.onStop()
        userInfoListener.remove()
    }
}