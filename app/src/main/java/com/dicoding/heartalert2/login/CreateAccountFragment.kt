package com.dicoding.heartalert2.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.api.RegisterRequest
import com.dicoding.heartalert2.api.RetrofitInstance
import com.dicoding.heartalert2.databinding.FragmentCreateAccountBinding
import kotlinx.coroutines.launch

class CreateAccountFragment : Fragment() {

    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registBtn.setOnClickListener {
            val fullname = binding.fullNameEdt.editText?.text.toString().trim()
            val email = binding.emailEdt.editText?.text.toString().trim()
            val password = binding.passwordEdt.editText?.text.toString().trim()
            val confirmPassword = binding.passwordConfirmEdt.editText?.text.toString().trim()

            // Validasi input
            if (validateInput(fullname, email, password, confirmPassword)) {
                registerUser(fullname, email, password)
            }
        }
    }
    private fun validateInput(
        fullname: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (fullname.isEmpty()) {
            binding.fullNameEdt.error = "Nama lengkap harus diisi"
            isValid = false
        } else {
            binding.fullNameEdt.error = null
        }

        if (email.isEmpty()) {
            binding.emailEdt.error = "Email harus diisi"
            isValid = false
        } else {
            binding.emailEdt.error = null
        }

        if (password.length < 8) {
            binding.passwordEdt.error = "Password minimal 8 karakter"
            isValid = false
        } else {
            binding.passwordEdt.error = null
        }

        if (password != confirmPassword) {
            binding.passwordConfirmEdt.error = "Kata sandi tidak cocok"
            isValid = false
        } else {
            binding.passwordConfirmEdt.error = null
        }

        return isValid
    }

    private fun registerUser(fullname: String, email: String, password: String) {
        lifecycleScope.launch {
            try {
                // Panggil API untuk registrasi
                val response = RetrofitInstance.api.registerUser(RegisterRequest(fullname, email, password))
                if (response.isSuccessful) {
                    // Registrasi berhasil
                    val message = response.body()?.message ?: "Registrasi berhasil"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

                    // Navigasi ke halaman OTP
                    val action = CreateAccountFragmentDirections.actionCreateAccountFragmentToOtpFragment(email)
                    requireActivity().findNavController(R.id.login_nav_host).navigate(action)
                } else {
                    // Jika registrasi gagal
                    Toast.makeText(requireContext(), "Registrasi gagal: ${response.message()}", Toast.LENGTH_LONG).show()
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