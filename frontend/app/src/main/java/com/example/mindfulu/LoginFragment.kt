package com.example.mindfulu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.mindfulu.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private val vm : LoginRegisterViewModel by viewModels()
    lateinit var binding : FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.login.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            Toast.makeText(context, "berhasil login", Toast.LENGTH_SHORT).show()
        }

        vm.loginError.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        binding.buttonSignIn.setOnClickListener {
            val username = binding.etUsernameLogin.text.toString().trim()
            val password = binding.etPasswordLogin.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "There's an empty field!", Toast.LENGTH_SHORT).show()
            } else {
                vm.login(username, password)
            }
        }

        binding.buttonToSIgnUp.setOnClickListener {
            findNavController().navigate(R.id.action_global_registerFragment)
        }
    }




}