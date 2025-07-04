package com.example.mindfulu.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mindfulu.viewmodel.LoginRegisterViewModel
import com.example.mindfulu.R
import com.example.mindfulu.databinding.FragmentRegisterBinding
import com.example.mindfulu.App // Import App class

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val vm: LoginRegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observer untuk register sukses dari ViewModel
        vm.registerResult.observe(viewLifecycleOwner) { response ->
            Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_global_loginFragment)
        }

        // Observer untuk error
        vm.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }


        // Tombol Register
        binding.buttonSignUp.setOnClickListener {
            val username = binding.etUsernameRegister.text.toString().trim()
            val name = binding.etNameRegister.text.toString().trim()
            val email = binding.etEmailRegister.text.toString().trim()
            val password = binding.etPasswordRegister.text.toString()
            val confirmPassword = binding.etCPasswordRegister.text.toString()

            // Validasi input
            when {
                username.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    Toast.makeText(context, "All fields are required!", Toast.LENGTH_SHORT).show()
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Toast.makeText(context, "Invalid email format!", Toast.LENGTH_SHORT).show()
                }
                password.length < 8 -> {
                    Toast.makeText(context, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // [DIUBAH] Hash password sebelum mengirimkannya ke ViewModel
                    val hashedPassword = App.hashPassword(password)
                    val hashedCpassword = App.hashPassword(confirmPassword) // Ini hanya untuk memastikan mereka sama setelah hash jika Anda mau, tapi yang penting passwordnya yang dihash.

                    // Panggil register dengan password yang sudah di-hash
                    vm.register(username, name, email, hashedPassword, hashedCpassword)
                }
            }
        }

        // Tombol ke halaman login
        binding.buttonToSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_global_loginFragment)
        }
    }
}