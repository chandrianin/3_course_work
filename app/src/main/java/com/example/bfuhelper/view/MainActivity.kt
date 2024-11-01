package com.example.bfuhelper.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.bfuhelper.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navController = navHostFragment.navController
        bottomNavView.setupWithNavController(navController)


        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.scheduleFragment, R.id.sportFragment, R.id.emailFragment)
        )
        toolbar.setupWithNavController(navController, appBarConfiguration)
    }
}