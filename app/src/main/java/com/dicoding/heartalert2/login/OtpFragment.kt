package com.dicoding.heartalert2.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.api.OtpRequest
import com.dicoding.heartalert2.api.RetrofitInstance
import com.dicoding.heartalert2.databinding.FragmentOtpBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.shape.MaterialShapeUtils
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!

    // Email yang digunakan saat registrasi
    private lateinit var email: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil email yang dikirim dari CreateAccountFragment (gunakan arguments jika ada)
        val args = OtpFragmentArgs.fromBundle(requireArguments())
        email = args.email

        // Handle klik tombol verifikasi OTP
        binding.verifBtn.setOnClickListener {
            val otp = binding.otpEdt.editText?.text.toString().trim()
            if (otp.isNotEmpty()) {
                verifyOtp(email, otp)
            } else {
                Toast.makeText(requireContext(), "Masukkan kode OTP", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle klik tombol resend OTP
        binding.btnResendOtp.setOnClickListener {
            resendOtp(email)
        }
    }

    private fun verifyOtp(email: String, otp: String) {
        lifecycleScope.launch {
            try {
                val request = OtpRequest(email, otp)
                val response = RetrofitInstance.api.verifyOtp(request)
                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Verifikasi berhasil"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                    // Navigasi ke halaman login setelah verifikasi berhasil
                    val action = OtpFragmentDirections.actionOtpFragmentToLoginFragment()
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(requireContext(), "Verifikasi gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resendOtp(email: String) {
        lifecycleScope.launch {
            try {
                val request = mapOf("email" to email)
                val response = RetrofitInstance.api.resendOtp(request)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "OTP telah dikirim ulang ke $email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal mengirim ulang OTP", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}