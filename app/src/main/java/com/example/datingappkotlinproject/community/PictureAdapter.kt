package com.example.datingappkotlinproject.community

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.datingappkotlinproject.R
import com.example.datingappkotlinproject.databinding.PictureLayoutBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class PictureAdapter(val context: Context, val pictureList: MutableList<AnonyPostingData>) :
    RecyclerView.Adapter<PictureAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding =
            PictureLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount() = pictureList.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        // 바인딩
        val binding = holder.binding
        // mutableList에서 해당 포지션값 저장
        val pictureData = pictureList.get(position)

        // RealTimeDatabase의 경로 지정
        val database = Firebase.database.reference.child("posting").child("${pictureData.key}")
        val currentUserUid = Firebase.auth.currentUser!!.uid

        // 현재 user의 uid가 좋아요를 눌렀는지 확인
        database.child("likePeople").child(currentUserUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.value == true) {
                        // 눌린 상태면 좋아요 on
                        binding.ivLike.setImageResource(R.drawable.favorite_pink)
                    } else {
                        // 아니면 off
                        binding.ivLike.setImageResource(R.drawable.baseline_favorite_24)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        // 화면 초기화 진행
        binding.tvAuthor.text = pictureData.nickName
        binding.tvTitle.text = pictureData.title
        binding.tvContent.text = pictureData.content
        binding.tvLike.text = pictureData.tvLike.toString()
        binding.tvEye.text = pictureData.tvHits.toString()
        binding.tvDate.text = getLastMessageTimeString(pictureData.tvDate)
        // 댓글 수 가져오기(RealTimeDatabase에 등록된 댓글 수)
        val commentCountRef =
            Firebase.database.reference.child("posting").child(pictureData.key).child("comment")
        commentCountRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount.toInt()
                val commentCountMap = mutableMapOf<String, Any>()
                commentCountMap[pictureData.key] = count
                binding.tvComment.text = count.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PictureAdapter", "Failed to read comment count.", error.toException())
            }
        })
        // 이미지는 로드가 되지 않고 파이어베이스에 저장된 스토리지 이미지명만 가져온 상태임. (pictureData.docId)
        val postingDAO = PostingDAO()
        val pictureRef = postingDAO.storage!!.reference.child("images/${pictureData.key}.png")
        pictureRef.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e("pictureAdapter", "Success")
                // Glide를 통하여 imageView에 이미지 띄우기
                Glide.with(context).load(it.result).into(binding.ivPicture3)
            }
        }
        // 아이템을 클릭시 이벤트 처리
        binding.root.setOnClickListener {
            // 클릭 시 좋아요 기능 처리
            // 좋아요 mutableMap 객체 생성
            val tvHits = mutableMapOf<String, Any>()
            // 현재 등록된 좋아요 수에서 +1 해서 저장하기
            val hits = pictureData.tvHits + 1
            tvHits["tvHits"] = hits
            // RealTimeDatabase에 업데이트
            database.updateChildren(tvHits)

            // 상세화면 액티비티로 이동
            val intent = Intent(binding.root.context, PostingActivity::class.java)
            // 객체 전달 Serializable 이용
            intent.putExtra("dataList", pictureList as ArrayList<Serializable>)
            intent.putExtra("position", position)
            intent.putExtra("key", pictureData.key)
            binding.root.context.startActivity(intent)

        }

        // 좋아요 버튼 클릭 이벤트 처리
        binding.ivLike.setOnClickListener {
            val database = Firebase.database.reference.child("posting").child("${pictureData.key}")
            val currentUserUid = Firebase.auth.currentUser!!.uid

            // 현재 user의 uid 값으로 좋아요 누른 기록이 있으면 -1 없으면 +1
            database.child("likePeople").child(currentUserUid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists() && snapshot.value == true) {
                            database.child("likePeople").child(currentUserUid).setValue(false)
                            binding.ivLike.setImageResource(R.drawable.baseline_favorite_24)
                            database.child("tvLike").setValue(pictureData.tvLike - 1)
                        } else {
                            database.child("likePeople").child(currentUserUid).setValue(true)
                            binding.ivLike.setImageResource(R.drawable.favorite_pink)
                            database.child("tvLike").setValue(pictureData.tvLike + 1)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLastMessageTimeString(lastTimeString: String): String {   // 글 작성 시간 구하기
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

    class CustomViewHolder(val binding: PictureLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}