package com.dicoding.heartalert2.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.dicoding.heartalert2.MainActivity
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.api.LoginRequest
import com.dicoding.heartalert2.api.RetrofitInstance
import com.dicoding.heartalert2.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginBtn.setOnClickListener {
            val email = binding.emailEdt.editText?.text.toString().trim()
            val password = binding.passwordEdt.editText?.text.toString().trim()

            // Validasi input
            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        binding.forgotPassBtn.setOnClickListener {
            // Navigasi ke ForgotPasswordFragment
            requireActivity().findNavController(R.id.login_nav_host)
                .navigate(LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment())
        }

        binding.createAccBtn.setOnClickListener {
            // Navigasi ke CreateAccountFragment
            requireActivity().findNavController(R.id.login_nav_host)
                .navigate(LoginFragmentDirections.actionLoginFragmentToCreateAccountFragment())
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.emailEdt.error = "Email harus diisi"
            isValid = false
        } else {
            binding.emailEdt.error = null
        }

        if (password.isEmpty()) {
            binding.passwordEdt.error = "Password harus diisi"
            isValid = false
        } else {
            binding.passwordEdt.error = null
        }

        return isValid
    }

    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.loginUser(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Login berhasil"
                    val token = response.body()?.token ?: ""

                    // Simpan token ke SharedPreferences atau gunakan sesuai kebutuhan
                    saveToken(token)

                    // Tampilkan pesan sukses
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

                    // Pindah ke MainActivity
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    // Jika login gagal
                    Toast.makeText(
                        requireContext(),
                        "Login gagal: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                // Tangani error
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveToken(token: String) {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_token", token)
            apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}