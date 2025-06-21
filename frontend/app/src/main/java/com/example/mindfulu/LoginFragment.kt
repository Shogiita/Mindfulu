package com.example.mindfulu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.mindfulu.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
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

        binding.buttonSignIn.setOnClickListener(){
            var username = binding.etUsernameLogin.text.toString()
            var password = binding.etPasswordLogin.text.toString()

            if(username.isEmpty() || password.isEmpty()){
                Toast.makeText(context,"There's an Empty Field",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context,"Login Success",Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonToSIgnUp.setOnClickListener(){
            findNavController().navigate(R.id.action_global_registerFragment)
        }
    }



}