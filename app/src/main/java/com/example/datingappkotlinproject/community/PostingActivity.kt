package com.example.datingappkotlinproject.community

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.datingappkotlinproject.UserInfoData
import com.example.datingappkotlinproject.databinding.ActivityPostingBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// 게시글 상세 화면 액티비티
class PostingActivity : AppCompatActivity() {
    lateinit var binding: ActivityPostingBinding
    lateinit var commentList: MutableList<CommentItemData>
    lateinit var pictureDataList: MutableList<AnonyPostingData>
    lateinit var dataList: MutableList<UserInfoData>
    lateinit var key: String

    lateinit var adapter: CommentAdapter

    val currentUser = Firebase.auth.currentUser!!.uid
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 인텐트로 해당 게시글의 key값 받아오기
        key = intent.getStringExtra("key").toString()
        // 받아온 key값으로 해당 게시글의 정보 띄우기
        getBoard(key)
        val dataList2 = intent.getSerializableExtra("dataList") as ArrayList<AnonyPostingData>
        val position = intent.getIntExtra("position", -1)

        // mutableList 객체 생성
        dataList = mutableListOf()
        commentList = mutableListOf()
        pictureDataList = mutableListOf()
        Firebase.database.reference.child("User").child("users").orderByChild("uid")
            .equalTo("$currentUser")//현재 사용자 키를 포함하는 채팅방 불러오기
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val user = data.getValue(UserInfoData::class.java)!!
                        dataList.add(user)
                    }
                }
            })
        // 현재 게시물의 key 값에 등록된 댓글 가져오기
        getFireBaseCommentList(key)

        // 댓글 작성 버튼 클릭 이벤터 처리
        binding.btnComment.setOnClickListener {
            val postingDAO = PostingDAO()
            val comment = binding.edtComment.text.toString()
            // 댓글 등록 key값 저장
            val postingKey = postingDAO.databaseReference!!.child(comment)!!.push().key
            // commentItemData 클래스 형식에 맞게 저장
            val commentData = CommentItemData(
                postingKey.toString(),
                dataList.get(0).communityNickname!!,
                comment, currentUser
            )
            // RealTimeDatabase에 댓글 저장
            postingDAO.databaseReference!!.child(key).child("comment").child("$postingKey")
                .setValue(commentData).addOnSuccessListener { // 성공시
                    Toast.makeText(this, "파이어베이스 comment에 등록 성공", Toast.LENGTH_SHORT).show()
                    binding.edtComment.text.clear()
                }.addOnFailureListener { // 실패시
                    Toast.makeText(this, "파이어베이스 comment에 등록 실패", Toast.LENGTH_SHORT).show()
                }
        }

        // 게시물 삭제 이벤트 처리 (dialog 이용 이벤트 처리)
        binding.ivDelete.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("게시물 삭제")
                .setMessage("삭제 하시겠습니까?")
                .setPositiveButton("확인") { dialog, id ->
                    try {
                        Firebase.database.getReference("posting").child(key).removeValue()
                        Toast.makeText(this, "게시글 삭제 완료", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        finish()

                    } catch (e: java.lang.Exception) {
                        dialog.dismiss()
                    }
                }.setNegativeButton("취소") {         // 다이얼로그 닫기
                        dialog, id ->
                    dialog.dismiss()
                }
            builder.show()
        }


        // 댓글 정보 리스트에서 화면 초기화
        binding.tvMainTitle.text = dataList2.get(position).title
        binding.tvMainContent.text = dataList2.get(position).content
        val postingDAO = PostingDAO()
        val pictureRef = postingDAO.storage!!.reference.child("images/${key}.png")
        pictureRef.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e("pictureAdapter", "Success")
                Glide.with(applicationContext).load(it.result).into(binding.ivPicture4)
            }
        }

        // 리사이클러뷰에 댓글 어댑터 연결
        adapter = CommentAdapter(commentList, key)
        binding.recyclerComment.adapter = adapter
        binding.recyclerComment.layoutManager = LinearLayoutManager(this)
    }

    // 게시물 정보 얻는 함수
    // 현재 user uid와 작성자 uid와 비교
    fun getBoard(key: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(AnonyPostingData::class.java)

                val userUid = Firebase.auth.currentUser!!.uid
                val writerUid = data?.writer

                if (userUid.equals(writerUid)) {
                    binding.ivDelete.visibility = View.VISIBLE
                } else {
                    binding.ivDelete.visibility = View.INVISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("BoardListActivity", error.toException().toString())
            }
        }

        Firebase.database.getReference("posting").child(key).addValueEventListener(postListener)
    }

    // 댓글을 CommentItemData 클래스 형식에 맞게 얻기
    private fun getFireBaseCommentList(key: String) {
        val userDAO = UserDAO()
        userDAO.userSelectComment(key)?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentList.clear()
                for (dataSnapShot in snapshot.children) {
                    val comment = dataSnapShot.getValue(CommentItemData::class.java)
                    comment?.key = dataSnapShot.key.toString()
                    if (comment != null) {
                        commentList.add(comment)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ListActivity", "파이어베이스에서 데이터 로딩 실패 ${error.toString()}")
                Toast.makeText(
                    applicationContext,
                    "파이어베이스에서 데이터 로딩 실패 ${error.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}