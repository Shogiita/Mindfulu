package com.example.mindfulu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mindfulu.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {
    private val vm: LoginRegisterViewModel by viewModels()
    lateinit var binding : FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(layoutInflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.register.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_global_loginFragment)
            Toast.makeText(context, "Register Success", Toast.LENGTH_SHORT).show()
        }

        vm.registerError.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        binding.buttonSignUp.setOnClickListener {
            val username = binding.etUsernameRegister.text.toString().trim()
            val name = binding.etNameRegister.text.toString().trim()
            val email = binding.etEmailRegister.text.toString().trim()
            val password = binding.etPasswordRegister.text.toString()
            val confirmpassword = binding.etCPasswordRegister.text.toString()

            if (username.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmpassword.isEmpty()) {
                Toast.makeText(context, "There's an empty field!", Toast.LENGTH_SHORT).show()
            } else if (!email.contains("@") || !email.contains(".")) {
                Toast.makeText(context, "Invalid email format!", Toast.LENGTH_SHORT).show()
            } else if (password.length < 8) {
                Toast.makeText(context, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            } else if (password != confirmpassword) {
                Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            } else {
                vm.register(username, name, email, password)
            }
        }

        binding.buttonToSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_global_loginFragment)
        }
    }

}