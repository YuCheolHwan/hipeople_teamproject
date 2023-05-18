package com.example.datingappkotlinproject.chat

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.datingappkotlinproject.ActivityForRegister.MainActivity
import com.example.datingappkotlinproject.UserInfoData
import com.example.datingappkotlinproject.databinding.ActivityChatRoomBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone

class ChatRoomActivity : AppCompatActivity() {
    val binding by lazy { ActivityChatRoomBinding.inflate(layoutInflater) }
    lateinit var btnExit: ImageButton
    lateinit var btnSubmit: Button
    lateinit var tvTitle: TextView
    lateinit var edtMessage: TextView
    lateinit var firebaseDatabase: DatabaseReference
    lateinit var recyclerTalks: RecyclerView
    lateinit var chatRoom: ChatRoom
    lateinit var opponentChatUser: UserInfoData
    lateinit var chatRoomKey: String
    lateinit var myUid: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // 입력값이 공백일때 데이터가 전송되지 않게 콜백 함수를 이용하여 전송 버튼 막기
        binding.edtMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString() != null && s.toString() != "") {
                    binding.btnSubmit.isEnabled = true
                } else {
                    binding.btnSubmit.isEnabled = false
                }

            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        // 기본값 설정
        initializeProperty()
        initializeView()
        initializeListener()
        setupChatRooms()
    }

    private fun initializeProperty() {
        myUid = FirebaseAuth.getInstance().currentUser?.uid!!
        firebaseDatabase = FirebaseDatabase.getInstance().reference!!
        chatRoom = (intent.getSerializableExtra("ChatRoom")) as ChatRoom    // 채팅방 정보
        chatRoomKey = intent.getStringExtra("ChatRoomKey")!!    // 채팅방 키
        opponentChatUser = (intent.getSerializableExtra("Opponent")) as UserInfoData    // 상대방 유저 정보
        Log.e("getChatRoomKey", chatRoomKey)
    }

    private fun initializeView() {
        btnExit = binding.imgbtnQuit
        edtMessage = binding.edtMessage
        recyclerTalks = binding.recyclerMessage
        btnSubmit = binding.btnSubmit
        tvTitle = binding.tvTitle
        tvTitle.text = opponentChatUser!!.nickName ?: ""
        // 상대방의 프로필 사진 보이게 하기 위해 storage 연동
        val pictureRef = Firebase.storage!!.reference.child("${opponentChatUser.uid}.png")
        // storage에서 url 받기 성공했을 시 콜백함수
        pictureRef.downloadUrl.addOnCompleteListener {
            // 결과가 성공했다면 진행
            if (it.isSuccessful) {
                Log.e("pictureAdapter", "Success")
                // Glide 함수를 이용하여 프로필 사진 imageView에 띄우기
                Glide.with(this).load(it.result).into(binding.ivPicture)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeListener() {      // 버튼 클릭 시 리스너 초기화
        btnSubmit.setOnClickListener {
            putMessage()
        }

    }

    private fun setupChatRooms() {      // 채팅방 목록 초기화 및 표시
        if (chatRoomKey.isNullOrBlank()) {
            setupChatRoomKey()
        } else {
            setupRecycler()
        }
    }

    fun setupChatRoomKey() {            //chatRoomKey 없을 경우 초기화 후 목록 초기화
        FirebaseDatabase.getInstance().getReference("ChatRoom")
            .child("chatRooms")
            .orderByChild("users2/${opponentChatUser.uid}${Firebase.auth.currentUser!!.uid}")
            .equalTo(true)    //상대방의 Uid가 포함된 목록이 있는지 확인
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        chatRoomKey = data.key!! //chatRoomKey 초기화
                        setupRecycler()
                        break
                    }
                }
            })
    }

    // 메세지 전송 함수
    @RequiresApi(Build.VERSION_CODES.O)
    fun putMessage() {
        try {
            // message 값을 데이터 클래스 형식에 맞게 저장
            var message =
                Message(myUid, getDateTimeString(), edtMessage.text.toString())
            // Firebase RealTimeDatabase에 현재 채팅방의 uid값의 경로에 messeage 값 저장
            FirebaseDatabase.getInstance().getReference("ChatRoom").child("chatRooms")
                .child(chatRoomKey).child("messages")
                .push().setValue(message).addOnSuccessListener {
                    // 저장이 성공했다면 editText 입력값 초기화
                    Log.i("putMessage", "메시지 전송에 성공하였습니다.")
                    edtMessage.text = ""
                }.addOnCanceledListener {
                    Log.i("putMessage", "메시지 전송에 실패하였습니다")
                }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("putMessage", "메시지 전송 중 오류가 발생하였습니다.")
        }
    }


    // 메세지 전송 시간을 얻기 위한 현재시간 얻는 함수
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDateTimeString(): String {       // 메세지 보낸 시각 정보 반환
        try {
            // 현재 시간 얻기
            var localDateTime = LocalDateTime.now()
            localDateTime.atZone(TimeZone.getDefault().toZoneId())
            // 시간 형식을 정하여 얻기
            var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            return localDateTime.format(dateTimeFormatter).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            throw java.lang.Exception("getTimeError")
        }
    }

    private fun setupRecycler() {       // 목록 초기화 및 업데이트
        recyclerTalks.layoutManager = LinearLayoutManager(this)
        recyclerTalks.adapter = RecyclerMessagesAdapter(this, chatRoomKey)
    }

    // 뒤로가기 버튼 클릭시 종료
    override fun onBackPressed() {
        finish()
    }
}