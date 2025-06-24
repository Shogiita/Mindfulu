package com.example.mindfulu.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavController
import com.example.mindfulu.R
import com.example.mindfulu.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController // [BARU] Deklarasi navController
    private var userEmail: String? = null // [BARU] Simpan email pengguna

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

        // [BARU] Ambil email dari Intent yang dikirim dari MoodInsertActivity
        userEmail = intent.getStringExtra("user_email_key")

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerHome) as NavHostFragment
        navController = navHostFragment.navController // [DIUBAH] Inisialisasi navController

        binding.navigationHome.setOnItemSelectedListener {
            val bundle = Bundle().apply {
                putString("user_email_key", userEmail) // Selalu teruskan email ke fragment
            }

            when (it.itemId) {
                R.id.mi_home_home -> navController.navigate(R.id.action_global_homeFragment, bundle)
                R.id.mi_overview_home -> navController.navigate(R.id.action_global_overviewFragment, bundle)
                R.id.mi_history_home -> navController.navigate(R.id.action_global_historyFragment, bundle)
                R.id.mi_setting_home -> navController.navigate(R.id.action_global_settingFragment, bundle)
            }
            true
        }
        // [BARU] Set item default yang terpilih agar bundle awal terkirim
        binding.navigationHome.selectedItemId = R.id.mi_home_home
    }
}