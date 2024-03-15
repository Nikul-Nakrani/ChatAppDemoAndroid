package com.example.simplechatappdemo.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.simplechatappdemo.R
import com.example.simplechatappdemo.User
import com.example.simplechatappdemo.adapter.UserAdapter
import com.example.simplechatappdemo.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var userList = ArrayList<User>()
    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        //sharedPreference
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        databaseReference = FirebaseDatabase.getInstance().reference

        //set recyclerView
        val adapter = UserAdapter(this, userList)
        binding.recyclerViewUser.adapter = adapter

        databaseReference.child("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //list clear
                userList.clear()
                for (postSnapShot in snapshot.children) {
                    val currentUser = postSnapShot.getValue(User::class.java)
                    //this condition for when user login . this time show the list of all other user
                    if (mAuth.currentUser?.uid != currentUser?.uid) {
                        userList.add(currentUser!!)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        //getToken for login user
        getFCMToken()
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("TAG", "getFCMToken: $token")

                // Update the FCM token in the database
                updateFCMTokenInDatabase(token)
            }
        }
    }

     private fun updateFCMTokenInDatabase(token: String?) {
         // Check if the user is logged in
         mAuth.currentUser?.uid?.let { userId ->
             // Update the FCM token in the "user" node in the database
             val userRef = databaseReference.child("user").child(userId)
             userRef.child("fcmToken").setValue(token).addOnSuccessListener {
                 Log.d("TAG", "FCM token updated in the database.")
             }.addOnFailureListener { e ->
                 Log.e("TAG", "Error updating FCM token in the database: ${e.message}")
             }
         }
     }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            mAuth.signOut()

            // Delete the FCM token
            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "FCM token deleted.")
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    Log.e("TAG", "Error deleting FCM token: ${task.exception?.message}")
                }

                // Clear the login status in SharedPreferences
                with(sharedPreferences.edit()) {
                    putBoolean("isLoggedIn", false)
                    apply()
                }


            }
            return true
        }
        return true
    }
}