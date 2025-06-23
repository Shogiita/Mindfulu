package com.example.mindfulu.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.mindfulu.R
import com.example.mindfulu.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerHome) as NavHostFragment
        val navController = navHostFragment.navController

        binding.navigationHome.setOnItemSelectedListener {
            if (it.itemId == R.id.mi_home_home){
                navController.navigate(R.id.action_global_homeFragment)
            } else if (it.itemId == R.id.mi_overview_home){
                navController.navigate(R.id.action_global_overviewFragment)
            } else if (it.itemId == R.id.mi_history_home){
                navController.navigate(R.id.action_global_historyFragment)
            } else if (it.itemId == R.id.mi_setting_home){
                navController.navigate(R.id.action_global_settingFragment)
            }
            true
        }
    }
}