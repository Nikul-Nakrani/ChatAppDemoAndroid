package com.example.simplechatappdemo.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.simplechatappdemo.Message
import com.example.simplechatappdemo.databinding.ReceiveBinding
import com.example.simplechatappdemo.databinding.SendBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


class MessageAdapter(val context: Context, private var messageList: ArrayList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_RECEIVE = 1
    val ITEM_SENT = 2

    class SentViewHolder(val bindingSend: SendBinding) : RecyclerView.ViewHolder(bindingSend.root)

    class ReceiveViewHolder(val bindingReceive: ReceiveBinding) :
        RecyclerView.ViewHolder(bindingReceive.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_RECEIVE) {
            //inflate receive
            ReceiveViewHolder(
                ReceiveBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        } else {
            //inflate send
            SentViewHolder(
                SendBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if (Firebase.auth.currentUser?.uid.equals(currentMessage.senderId)) {
            ITEM_SENT
        } else {
            return ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]

        if (holder is SentViewHolder) {
            if (!currentMessage.message.isNullOrBlank()) {
                // Show message view
                holder.bindingSend.tvSentMessage.visibility = View.VISIBLE
                holder.bindingSend.tvSentMessage.text = currentMessage.message
                Log.d("TAG", "onBindViewHolder: Message Send Display")
            } else if (!currentMessage.fileName.isNullOrBlank()) {
                // Show file name view based on file extension
                val fileExtension = getFileExtension(currentMessage.fileName!!)
                if ((isImageFile(fileExtension))) {
                    // Display image view
                    holder.bindingSend.ivSendImage.visibility = View.VISIBLE
                    Glide.with(context).load(currentMessage.imageUrl)
                        .into(holder.bindingSend.ivSendImage)
                    Log.d("TAG", "onBindViewHolder: Image Send Display")
                } else {
                    // Display file name view
                    holder.bindingSend.tvFileNameSend.visibility = View.VISIBLE
                    holder.bindingSend.tvFileNameSend.text = currentMessage.fileName
                    Log.d("TAG", "onBindViewHolder: File Name Send Display")
                }
            }
        } else if (holder is ReceiveViewHolder) {
            if (!currentMessage.message.isNullOrBlank()) {
                // Show message view
                holder.bindingReceive.tvReceiveMessage.visibility = View.VISIBLE
                holder.bindingReceive.tvReceiveMessage.text = currentMessage.message
                Log.d("TAG", "onBindViewHolder: Message Receive Display")
            } else if (!currentMessage.fileName.isNullOrBlank()) {
                // Show file name view based on file extension
                val fileExtension = getFileExtension(currentMessage.fileName!!)
                if ((isImageFile(fileExtension))) {
                    // Display image view
                    holder.bindingReceive.ivReceiveImage.visibility = View.VISIBLE
                    Glide.with(context).load(currentMessage.imageUrl)
                        .into(holder.bindingReceive.ivReceiveImage)
                    Log.d("TAG", "onBindViewHolder: Image Receive Display")
                } else {
                    // Display file name view
                    holder.bindingReceive.tvFileNameReceive.visibility = View.VISIBLE
                    holder.bindingReceive.tvFileNameReceive.text = currentMessage.fileName
                    Log.d("TAG", "onBindViewHolder: File Name Receive Display")
                }
            }
        }
    }

    // Helper function to get the file extension from the file name
    private fun getFileExtension(fileName: String): String {
        val regex = "(?<=\\.)[a-zA-Z]+$".toRegex()
        val matchResult = regex.find(fileName)
        return matchResult?.value?.toLowerCase() ?: ""
    }

    // Helper function to check if the file is an image file based on the extension
    private fun isImageFile(extension: String?): Boolean {
        return extension == "jpg" || extension == "jpeg"
    }


}




