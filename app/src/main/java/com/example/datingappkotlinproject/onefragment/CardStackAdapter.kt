package com.example.datingappkotlinproject.onefragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.datingappkotlinproject.ActivityForRegister.MainActivity
import com.example.datingappkotlinproject.R
import com.example.datingappkotlinproject.UserInfoData
import com.example.datingappkotlinproject.chat.ChatRoom
import com.example.datingappkotlinproject.chat.ChatRoomActivity
import com.example.datingappkotlinproject.databinding.ItemPhotoBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class CardStackAdapter(val context: Context, val items: MutableList<UserInfoData>) :
    RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {
    var likeFlag = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val itemsData = items[position]

        // 기본 정보 초기화
        binding.itemName.text = itemsData.nickName
        binding.itemAdress.text = itemsData.address
        // uid값에 따른 사진 로드
        val pictureRef = Firebase.storage.reference.child("${itemsData.uid}.png")
        pictureRef.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e("pictureAdapter", "Success")
                // Glide 를 통하여 imageView에 사진 로드
                Glide.with(context).load(it.result).into(binding.profileImageArea)
            }
        }


        // RealTimeDatabase 경로 지정
        val database =
            Firebase.database.reference.child("User").child("users").child("${itemsData.uid}")
        val currentUserUid = Firebase.auth.currentUser!!.uid

        database.child("likePeople").child(currentUserUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.value == true) {
                        binding.btnLike.setImageResource(R.drawable.favorite_pink)
                    } else {
                        binding.btnLike.setImageResource(R.drawable.baseline_favorite_24)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        // 채팅하기 버튼 누를 시 이벤트 처리
        binding.btnChat.setOnClickListener {
            // 채팅방 이동 함수
            addChatRoom(position)
        }
        // 좋아요 기능 구현
        binding.btnLike.setOnClickListener {
            if (likeFlag == false) {
                binding.btnLike.setImageResource(R.drawable.favorite_pink)
                likeFlag = true
            } else {
                binding.btnLike.setImageResource(R.drawable.baseline_favorite_24)
                likeFlag = false
            }
        }
    }


    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {}

    fun addChatRoom(position: Int) {    // 채팅방 추가
        val opponent = items[position]      // 채팅할 상대방 정보
        val currentUserUid = Firebase.auth.currentUser!!.uid
        val database =
            FirebaseDatabase.getInstance().getReference("ChatRoom") // 넣을 database reference 세팅
        val chatRoom = ChatRoom(        // 추가할 채팅방 정보 세팅
            // 현재 사용자 uid, 상대방 uid, 현재 사용자 + 상대방 uid, 상대방 uid + 현재 사용자 uid
            mapOf(Firebase.auth.currentUser!!.uid!! to (true), opponent.uid!! to (true)),
            mapOf(
                "${currentUserUid}${opponent.uid}"!! to (true),
                "${opponent.uid}${currentUserUid}"!! to (true)
            ),
            null
        )

        database.child("chatRooms")
            .orderByChild("users2/${opponent.uid}${currentUserUid}")
            .equalTo(true) //상대방 Uid가 포함된 채팅방이 있는 지 확인
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) { //채팅방이 없는 경우
                        // 채팅방 새로 생성 후 이동
                        database.child("chatRooms").push().setValue(chatRoom)
                            .addOnSuccessListener {// 채팅방 새로 생성 후 이동

                                goToChatRoom(chatRoom, opponent, "")

                            }
                    } else {
                        context.startActivity(Intent(context, MainActivity::class.java))
                        goToChatRoom(chatRoom, opponent, "")                    //해당 채팅방으로 이동
                    }

                }
            })
    }

    // 채팅방 이동 함수
    fun goToChatRoom(
        chatRoom: ChatRoom,
        opponentUid: UserInfoData,
        chatRoomKey: String
    ) {       //채팅방으로 이동
        var intent = Intent(context, ChatRoomActivity::class.java)
        intent.putExtra("ChatRoom", chatRoom)       //채팅방 정보
        intent.putExtra("Opponent", opponentUid)    //상대방 정보
        intent.putExtra("ChatRoomKey", "") //채팅방 키
        context.startActivity(intent)
    }
}