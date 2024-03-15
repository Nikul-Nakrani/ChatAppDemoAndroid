package com.example.simplechatappdemo.adapter


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simplechatappdemo.User
import com.example.simplechatappdemo.databinding.UserLayoutBinding
import com.example.simplechatappdemo.ui.ChatActivity

class UserAdapter(private val context: Context, private val userList: ArrayList<User>) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(var binding: UserLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(UserLayoutBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.binding.tvName.text = currentUser.name

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)

            intent.putExtra("name", currentUser.name)
            intent.putExtra("uId", currentUser.uid)
            intent.putExtra("fcmToken", currentUser.fcmToken)
            context.startActivity(intent)
        }
    }
}