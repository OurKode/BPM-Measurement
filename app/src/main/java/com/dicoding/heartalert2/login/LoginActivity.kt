package com.dicoding.heartalert2.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.dicoding.heartalert2.MainActivity
import com.dicoding.heartalert2.R

class LoginActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cek apakah pengguna sudah login
        if (isUserLoggedIn()) {
            // Jika sudah login, langsung pindah ke MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // Jika belum login, tampilkan LoginActivity
            setContentView(R.layout.activity_login)

            // Set NavHostFragment dan NavController
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.login_nav_host) as NavHostFragment
            navController = navHostFragment.navController
        }
    }

    // Fungsi untuk memeriksa status login dari SharedPreferences
    private fun isUserLoggedIn(): Boolean {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("user_token", null)
        return !token.isNullOrEmpty()
    }
}