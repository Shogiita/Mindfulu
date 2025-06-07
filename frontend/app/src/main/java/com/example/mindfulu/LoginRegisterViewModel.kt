package com.example.mindfulu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginRegisterViewModel : ViewModel() {

    private val _register = MutableLiveData<String>()
    val register: LiveData<String> get() = _register

    private val _registerError = MutableLiveData<String>()
    val registerError: LiveData<String> get() = _registerError

    private val _login = MutableLiveData<String>()
    val login: LiveData<String> get() = _login

    private val _loginError = MutableLiveData<String>()
    val loginError: LiveData<String> get() = _loginError

    fun login(username: String, password: String) {
        val user = MockDB.users.find { it.username == username && it.password == password }
        if (user != null) {
            _login.value = "Login  as ${user.username}"
        } else {
            _loginError.value = "Incorrect username or password"
        }
    }

    fun register(username: String, name: String, email: String, password: String) {
        val existingUser = MockDB.users.find {
            it.username == username || it.email == email
        }

        if (existingUser != null) {
            _registerError.value = "Email or username already in use"
        } else {
            val newUser = User(username, name, email, password)
            MockDB.addUser(newUser)
            _register.value = "Registration $username success1"
        }
    }
}
