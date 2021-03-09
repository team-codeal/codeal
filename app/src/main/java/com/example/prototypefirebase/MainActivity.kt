package com.example.prototypefirebase

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.setContent
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.prototypefirebase.ui.teams.CreateTeamActivity

class MainActivity : AppCompatActivity() {

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
        navController.popBackStack(R.id.navigation_home, true);
        navController.navigate(R.id.navigation_notifications);
    }

    fun openBoard(view: View){
        val boardIntent = Intent(this,BoardActivity::class.java)
        startActivity(boardIntent)
    }
    fun openAddTeam(view: View){
        val teamIntent = Intent(this, CreateTeamActivity::class.java)
        startActivity(teamIntent)
    }
}