package com.example.simplechatappdemo.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.simplechatappdemo.Message
import com.example.simplechatappdemo.adapter.MessageAdapter
import com.example.simplechatappdemo.databinding.ActivityChatBinding
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.storage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var databaseReference: DatabaseReference
    private var FILE_PICK_REQUEST_CODE = 1
    private lateinit var storageReference: StorageReference
    private lateinit var name: String

    private var receiverRoom: String? = null
    private var senderRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase initialize
        FirebaseApp.initializeApp(this)

        //get data through intent
        name = intent.getStringExtra("name").toString()
        val receiverUid = intent.getStringExtra("uId")
        val fcmToken = intent.getStringExtra("fcmToken")

        //database
        databaseReference = FirebaseDatabase.getInstance().reference

        //storage reference
        storageReference = Firebase.storage.reference

        val senderUid = FirebaseAuth.getInstance().currentUser?.uid

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        //set actionbar name as user name
        supportActionBar?.title = name

        //adapter set
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)
        binding.recyclerViewChat.adapter = messageAdapter

        //set recyclerview according data
        databaseReference.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnapShot in snapshot.children) {
                        val message = postSnapShot.getValue(Message::class.java)
                        messageList.add(message!!)
                        Log.d("TAG", "onDataChange: ${message.fileName}")
                    }
                    messageAdapter.notifyDataSetChanged()

                    Log.d("TAG", "onDataChange: Data Fetch")
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Something Went Wrong", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        binding.ivSend.setOnClickListener {
            val message = binding.edtMessage.text.toString()
            val imageUrl: String? = null
            val fileName: String? = null
            val fileExtension: String? = null

            val messageObject =
                Message(
                    message,
                    senderUid!!,
                    imageUrl.toString(),
                    fileName.toString(),
                    fileExtension.toString()
                )

            Log.d("TAG", "onCreate: $message + $senderUid +$messageObject")

            // Push the message to the sender's room
            if (message.isNotBlank()) {
                databaseReference.child("chats").child(senderRoom!!).child("messages").push()
                    .setValue(messageObject).addOnSuccessListener {
                        // After sending the message to the sender, also push it to the receiver's room
                        databaseReference.child("chats").child(receiverRoom!!).child("messages")
                            .push().setValue(messageObject)
                        /*  throw RuntimeException("Test Crash")*/
                        sendNotification(message, name)
                    }
                binding.edtMessage.text.clear()
            } else {
                Toast.makeText(this, "message Is empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivUpload.setOnClickListener {
            openFilePicker()
        }
    }

    //function for open the storage
    private fun openFilePicker() {
        val intent = Intent()
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), FILE_PICK_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_PICK_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val fileUri: Uri = data.data!!
            uploadFileAndImage(fileUri)
        }
    }

    //function for upload file and image in chat
    private fun uploadFileAndImage(fileUri: Uri) {
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        val storageReference: StorageReference = FirebaseStorage.getInstance().reference
            .child("chat_images")
            .child(System.currentTimeMillis().toString() + "." + getFileExtension(fileUri))

        val uploadTask: UploadTask = storageReference.putFile(fileUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val fileUri1 = downloadUri!!
                sendMessage(fileUri1, senderUid)
            } else {
                // Handle the error
                Toast.makeText(this, "upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessage(fileUri: Uri, senderUid: String?) {
        val message = binding.edtMessage.text.toString()
        val fileName = getFileName(fileUri)
        val fileExtension = getFileExtension(fileUri)

        if (fileName.isNotBlank()) {
            val messageObject =
                Message(message, senderUid!!, fileUri.toString(), fileName, fileExtension)
            databaseReference.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObject).addOnSuccessListener {
                    // After sending the message to the sender, also push it to the receiver's room
                    databaseReference.child("chats").child(receiverRoom!!).child("messages")
                        .push()
                        .setValue(messageObject)

                }
            sendNotification(message, name)
        } else {
            // Handle the case where the file name is null or blank
            Toast.makeText(this, "File name is null or blank", Toast.LENGTH_SHORT).show()
        }
    }

    //send notification
    private fun sendNotification(message: String, username: String) {

        val jsonObject = JSONObject()

        // Prepare the FCM payload
        val notificationData = JSONObject()
        notificationData.put("title", "New Message $username")
        notificationData.put("body", message)

        val dataObj = JSONObject()
        dataObj.put("userId", notificationData)


        jsonObject.put("notification", notificationData)
        jsonObject.put("data", dataObj)

        // Call the function to send the FCM payload
        callApi(jsonObject)

    }

    //function for call api
    private fun callApi(jsonObject: JSONObject) {
        val url = "https://fcm.googleapis.com/fcm/send"

        val client = OkHttpClient()
        val requestBody =
            RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header(
                "Authorization",
                "Bearer AAAAvy3j6fs:APA91bGJTaOnB9iT0MrJmbJx3CJwL--zWLiTfU1xOnsNsdmtkhkEGETLGxrzvYuERjp6wVyeERbhA3fYvirhDo3jRss5ymEHkTFr7iorHvloLlIoBW1qKZlFLWQWVP7PcZnrRz-Xhz7k"
            )
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Handle successful response
                    // Process the API response as needed
                } else {
                    // Handle error response
                    runOnUiThread {
                        Toast.makeText(this@ChatActivity, "API call failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                runOnUiThread {
                    Toast.makeText(
                        this@ChatActivity,
                        "API call failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun getFileExtension(uri: Uri): String {
        val contentResolver = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)).toString()
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndex("_display_name"))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "example.pdf"  // Default file name if retrieval fails
    }
}

