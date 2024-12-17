package com.dicoding.heartalert2.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.api.RetrofitInstance
import com.dicoding.heartalert2.databinding.FragmentForgotPasswordBinding
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol konfirmasi untuk mengirim email
        binding.confirmButton.setOnClickListener {
            val email = binding.emailResetEdt.editText?.text.toString().trim()
            if (validateEmail(email)) {
                sendResetPasswordRequest(email)
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return if (email.isEmpty()) {
            binding.emailResetEdt.error = "Email harus diisi"
            false
        } else {
            binding.emailResetEdt.error = null
            true
        }
    }

    private fun sendResetPasswordRequest(email: String) {
        lifecycleScope.launch {
            try {
                // Panggil API untuk reset password
                val response = RetrofitInstance.api.forgotPassword(mapOf("email" to email))
                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Email berhasil dikirim"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

                    // Tampilkan popup sederhana menggunakan Toast
                    Toast.makeText(
                        requireContext(),
                        "Link untuk mengatur ulang kata sandi anda sudah dikirim ke email anda.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Navigasi kembali ke halaman login
                    requireActivity().findNavController(R.id.login_nav_host)
                        .navigate(ForgotPasswordFragmentDirections.actionForgotPasswordFragmentToLoginFragment())
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal mengirim email: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}