package com.dicoding.intro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.api.RetrofitInstance
import com.dicoding.heartalert2.login.LoginActivity
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var nameTv: TextView
    private lateinit var emailTv: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi TextView dari layout
        nameTv = view.findViewById(R.id.name_tv)
        emailTv = view.findViewById(R.id.email_tv)

        // Mengambil data profil pengguna dari API
        getUserProfile()

        // Mengatur tombol logout
        val logoutBtn: View = view.findViewById(R.id.logout_btn)
        logoutBtn.setOnClickListener {
            logoutUser()
        }
    }

    private fun getUserProfile() {
        lifecycleScope.launch {
            try {
                // Memanggil API untuk mendapatkan data profil
                val response = RetrofitInstance.api.getUserProfile()

                if (response.isSuccessful) {
                    val user = response.body()?.user
                    // Menampilkan data ke TextView jika response berhasil
                    if (user != null) {
                        nameTv.text = user.fullname
                        emailTv.text = user.email
                    }
                } else {
                    // Tangani error jika response tidak sukses
                    nameTv.text = "Error: ${response.code()}"
                    emailTv.text = "Error fetching data."
                }
            } catch (e: HttpException) {
                // Tangani error jaringan atau parsing
                nameTv.text = "Error: ${e.message}"
                emailTv.text = "Error fetching data."
            } catch (e: Exception) {
                // Tangani error lainnya
                nameTv.text = "An unexpected error occurred."
                emailTv.text = "Error fetching data."
            }
        }
    }

    private fun logoutUser() {
        // Menghapus token dari SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("user_token") // Hapus token
            apply()
        }

        // Menampilkan pesan logout
        Toast.makeText(requireContext(), "Logout berhasil", Toast.LENGTH_SHORT).show()

        // Arahkan pengguna kembali ke halaman login
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish() // Tutup ProfileActivity agar tidak kembali ke halaman profile
    }
}