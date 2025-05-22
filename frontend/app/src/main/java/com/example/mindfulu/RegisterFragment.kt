package com.example.mindfulu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.mindfulu.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

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
            }
        }

        binding.buttonToSignIn.setOnClickListener(){
            findNavController().navigate(R.id.action_global_loginFragment)
        }
    }
}