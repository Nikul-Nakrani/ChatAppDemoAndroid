package com.example.simplechatappdemo.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.simplechatappdemo.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()

        //shredPreference
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Check if the user is already logged in, then redirect to MainActivity
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }



        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()

            if (email.isNotBlank() && password.isNotBlank()) {
                login(email, password)
                // Retrieve FCM token when the user logs in
            } else {
                Toast.makeText(this, "Please fill All data", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun login(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Save the login status in SharedPreferences
                with(sharedPreferences.edit()) {
                    putBoolean("isLoggedIn", true)
                    apply()
                }

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "User does not exit", Toast.LENGTH_SHORT).show()
            }
        }
    }
}