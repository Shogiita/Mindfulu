package com.example.mindfulu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
<<<<<<< Updated upstream
=======
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
>>>>>>> Stashed changes
import androidx.navigation.fragment.findNavController
import com.example.mindfulu.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {
<<<<<<< Updated upstream

    lateinit var binding : FragmentRegisterBinding
=======
    private val vm: LoginRegisterViewModel by viewModels()
    private lateinit var binding: FragmentRegisterBinding
>>>>>>> Stashed changes

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

<<<<<<< Updated upstream
        binding.buttonSignUp.setOnClickListener(){

            var username = binding.etUsernameRegister.text.toString()
            var name = binding.etNameRegister.text.toString()
            var email = binding.etEmailRegister.text.toString()
            var password = binding.etPasswordRegister.text.toString()
            var confirmpassword = binding.etCPasswordRegister.text.toString()

            if(username.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmpassword.isEmpty()){
                Toast.makeText(context, "There's An Empty Field!!",Toast.LENGTH_SHORT).show()
            }else{
                if(!email.contains("@") || !email.contains(".")){
                    Toast.makeText(context, "Invalid Email!!", Toast.LENGTH_SHORT).show()
                }else{
                    if(password.length < 8 || confirmpassword.length < 8){
                        Toast.makeText(context, "Password too short!!", Toast.LENGTH_SHORT).show()
                    }else{
                        if(password != confirmpassword){
                            Toast.makeText(context, "Invalid Password!!", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(context, "Register Success!!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
=======
        // Observer untuk register sukses dari ViewModel yang baru
        vm.registerResult.observe(viewLifecycleOwner) { response ->
            Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
            // Langsung arahkan ke halaman login setelah registrasi berhasil
            findNavController().navigate(R.id.action_global_loginFragment)
        }

        // Observer untuk error dari ViewModel yang baru
        vm.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }

        // Observer untuk loading state
        vm.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarRegister.isVisible = isLoading
        }

        binding.buttonSignUp.setOnClickListener {
            val username = binding.etUsernameRegister.text.toString().trim()
            val name = binding.etNameRegister.text.toString().trim()
            val email = binding.etEmailRegister.text.toString().trim()
            val password = binding.etPasswordRegister.text.toString()
            val confirmpassword = binding.etCPasswordRegister.text.toString()

            // Validasi input (client-side)
            if (username.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmpassword.isEmpty()) {
                Toast.makeText(context, "All fields are required!", Toast.LENGTH_SHORT).show()
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(context, "Invalid email format!", Toast.LENGTH_SHORT).show()
            } else if (password.length < 8) {
                Toast.makeText(context, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            } else if (password != confirmpassword) {
                Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            } else {
                // PERBAIKAN: Panggil fungsi register dengan 5 argumen
                vm.register(username, name, email, password, confirmpassword)
>>>>>>> Stashed changes
            }
        }

        binding.buttonToSignIn.setOnClickListener(){
            findNavController().navigate(R.id.action_global_loginFragment)
        }
    }
}