package com.example.datingappkotlinproject.chat

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.datingappkotlinproject.R
import com.example.datingappkotlinproject.UserInfoData
import com.example.datingappkotlinproject.community.PostingDAO
import com.example.datingappkotlinproject.databinding.ListChatroomItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class RecyclerChatRoomsAdapter(val context: Context) :
    RecyclerView.Adapter<RecyclerChatRoomsAdapter.ViewHolder>() {
    var chatRooms: ArrayList<ChatRoom> = arrayListOf()      // 채팅방 목록
    var chatRoomKeys: ArrayList<String> = arrayListOf()     // 채팅방 키 목록
    val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()      // 현재 사용자 Uid

    init {
        try {
            setupAllUserList()
        } catch (e: Exception) {
            Log.e("RecyclerChatRoomsAdapter", "${e.message}")
            Log.e("RecyclerChatRoomsAdapter", "${e.printStackTrace()}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_chatroom_item, parent, false)
        return ViewHolder(ListChatroomItemBinding.bind(view))
    }

    override fun getItemCount(): Int {
        return chatRooms.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var userIdList = chatRooms[position].users!!.keys    //채팅방에 포함된 사용자 키 목록
        var opponent2 = userIdList.last { !it.equals(myUid) } // 상대방 아이디
        val pictureRef = Firebase.storage!!.reference.child("${opponent2}.png")
        pictureRef.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e("pictureAdapter", "Success")
                Glide.with(context).load(it.result).into(holder.ivProfile)
            }
        }
        try {
            val chatUserList: MutableList<UserInfoData> = mutableListOf()
            FirebaseDatabase.getInstance().getReference("User").child("users")
                .orderByChild("uid")
                .equalTo(opponent2)//상대방 사용자 키를 포함하는 채팅방 불러오기
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (data in snapshot.children) {
                            holder.chatRoomKey = data.key.toString()!!             //채팅방 키 초기화
                            val chatUser = data.getValue<UserInfoData>()!!
                            chatUserList.add(chatUser)
                            holder.opponentChatUser = chatUser      //상대방 정보 초기화
                            holder.tvName.text = chatUser.nickName.toString() //상대방 이름 초기화

                        }
                    }
                })
        } catch (e: java.lang.Exception) {
            Log.e("RecyclerChatRoomsAdapter2", "${e.message}")
            Log.e("RecyclerChatRoomsAdapter2", "${e.printStackTrace()}")
        }
        holder.background.setOnClickListener()               //채팅방 항목 선택 시
        {
            try {
                var intent = Intent(context, ChatRoomActivity::class.java)
                intent.putExtra("ChatRoom", chatRooms.get(position))      //채팅방 정보
                intent.putExtra("Opponent", holder.opponentChatUser)          //상대방 사용자 정보
                intent.putExtra("ChatRoomKey", chatRoomKeys[position])     //채팅방 키 정보
                context.startActivity(intent)                            //해당 채팅방으로 이동
            } catch (e: Exception) {
                Log.e("채팅문제1", "${e.message}")
                Log.e("채팅문제2", "${e.printStackTrace()}")
                e.printStackTrace()
                Toast.makeText(
                    context,
                    "채팅방 이동 중 문제가 발생하였습니다. ${e.printStackTrace()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (chatRooms[position].messages!!.size > 0) {         //채팅방 메시지가 존재하는 경우
            setupLastMessageAndDate(holder, position)        //마지막 메시지 및 시각 초기화
            setupMssageCount(holder, position)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupLastMessageAndDate(
        holder: RecyclerChatRoomsAdapter.ViewHolder,
        position: Int
    ) {
        try {
            // 메세지 목록에서 시각을 비교하여 가장 마지막 메세지 가져오기
            var lastMessage =
                chatRooms[position].messages!!.values.sortedWith(compareBy({ it.sendedDate }))
                    .last()
            holder.tvMessage.text = lastMessage.content     // 마지막 메세지 표시
            holder.tvDate.text =
                getLastMessageTimeString(lastMessage.sendedDate) // 마지막으로 전송된 시각 표시
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun setupMssageCount(holder: ViewHolder, position: Int) {
        // 확인되지 않은 메세지 개수 표시
        try {
            var unconfirmedCount = chatRooms[position].messages!!.filter {
                !it.value.confirmed && !it.value.senderUid.equals(myUid)
            }.size
            if (unconfirmedCount > 0) {         // 확인되지 않은 메세지가 있을 경우
                holder.tvChatCount.visibility = View.VISIBLE        // 개수 표시
                holder.tvChatCount.text = unconfirmedCount.toString()
            } else {
                holder.tvChatCount.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            holder.tvChatCount.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLastMessageTimeString(lastTimeString: String): String {      // 마지막 메세지가 전송된 시각 구하기
        try {
            var currentTime = LocalDateTime.now().atZone(TimeZone.getDefault().toZoneId()) // 현재 시각
            var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            var messageMonth = lastTimeString.substring(4, 6).toInt() // 마지막 메세지 시각 월, 일, 시, 분
            var messageDate = lastTimeString.substring(6, 8).toInt()
            var messageHour = lastTimeString.substring(8, 10).toInt()
            var messageMinute = lastTimeString.substring(10, 12).toInt()
            var formattedCurrentTimeString = currentTime.format(dateTimeFormatter) // 현 시각 월,일,시,분
            var currentMonth = formattedCurrentTimeString.substring(4, 6).toInt()
            var currentDate = formattedCurrentTimeString.substring(6, 8).toInt()
            var currentHour = formattedCurrentTimeString.substring(8, 10).toInt()
            var currentMinute = formattedCurrentTimeString.substring(10, 12).toInt()
            var monthAgo = currentMonth - messageMonth      // 현 시각과 마지막 메세지 시각과의 차이. 월,일,시,분
            var dayAgo = currentDate - messageDate
            var hourAgo = currentHour - messageHour
            var minuteAgo = currentMinute - messageMinute
            if (monthAgo > 0) {     // 1개월 이상 차이 나는 경우
                return monthAgo.toString() + "개월 전"
            } else {
                if (dayAgo > 0) {     // 1일 이상 차이 나는 경우
                    if (dayAgo == 1) {
                        return "어제"
                    } else {
                        return dayAgo.toString() + "일 전"
                    }
                } else {
                    if (hourAgo > 0) {      // 1시간 이상 차이 나는 경우
                        return hourAgo.toString() + "시간 전"
                    } else {
                        if (minuteAgo > 0) {        // 1분 이상 차이 나는 경우
                            return minuteAgo.toString() + "분 전"
                        } else {
                            return "방금"
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return ""
        }
    }

    inner class ViewHolder(itemView: ListChatroomItemBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var opponentChatUser = UserInfoData()
        var chatRoomKey = ""
        var background = itemView.background
        var tvName = itemView.tvName
        var tvMessage = itemView.tvMessage
        var tvDate = itemView.tvDate
        var tvChatCount = itemView.tvChatCount
        var ivProfile = itemView.ivProfile

    }


    fun setupAllUserList() {     //전체 채팅방 목록 초기화 및 업데이트
        FirebaseDatabase.getInstance().getReference("ChatRoom").child("chatRooms")
            .orderByChild("users/$myUid").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatRooms.clear()
                    for (data in snapshot.children) {
                        Log.e("snapshot---------", "$data")
                        chatRooms.add(data.getValue<ChatRoom>()!!)
                        Log.e("snapshot------chatRooms", "$chatRooms")
                        chatRoomKeys.add(data.key!!)
                    }
                    notifyDataSetChanged()
                }
            })

    }
}