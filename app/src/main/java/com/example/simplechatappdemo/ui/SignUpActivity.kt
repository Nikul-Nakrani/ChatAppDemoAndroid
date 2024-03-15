package com.example.simplechatappdemo.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.simplechatappdemo.User
import com.example.simplechatappdemo.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //hide the toolbar
        supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()

        //sharedPreference Class
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSignUpSignUp.setOnClickListener {
            val name = binding.edtNameSignUp.text.toString()
            val email = binding.edtEmailSignUp.text.toString()
            val password = binding.edtPasswordSignUp.text.toString()

            if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                signUp(name, email, password)
            } else {
                Toast.makeText(this, "Please Fill All Data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signUp(name: String, email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Save data in SharedPreferences
                with(sharedPreferences.edit()) {
                    putBoolean("isLoggedIn", true)
                    apply()
                }

                // Move to MainActivity and store info in the database
                addUserToDatabase(name, email, mAuth.currentUser?.uid)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addUserToDatabase(name: String, email: String, uid: String?) {
        databaseReference = FirebaseDatabase.getInstance().getReference("user")

        // Use the constructor with parameters to create a User object
        val user = User(name, email, uid!!, "") // Pass an empty string or null for fcmToken

        databaseReference.child(uid).setValue(user).addOnSuccessListener {
            Toast.makeText(this, "Your Info Is Saved Successfully ", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show()
        }
    }


}