package com.example.datingappkotlinproject.community

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.datingappkotlinproject.databinding.ActivityInputBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import com.bumptech.glide.Glide
import com.example.datingappkotlinproject.UserInfoData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


// 게시물 작성 액티비티
class InputActivity : AppCompatActivity() {
    lateinit var binding: ActivityInputBinding
    var imageUri: Uri? = null
    lateinit var dataList: MutableList<UserInfoData>
    val currentUser = Firebase.auth.currentUser!!.uid

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataList = mutableListOf()

        Firebase.database.reference.child("User").child("users").orderByChild("uid")
            .equalTo("$currentUser") // 현재 유저의 정보 얻기
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val user = data.getValue(UserInfoData::class.java)!!
                        dataList.add(user)
                    }
                }
            })
        // 첨부한 이미지 띄우기
        val requestLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                imageUri = it.data?.data
                Glide.with(applicationContext).load(imageUri).into(binding.ivAddPicture)
            }
        }
        // 사진 첨부를 위하여 imageView 클릭 시 이벤트 접근
        binding.ivAddPicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            requestLauncher.launch(intent)
        }
        // 게시물 저장 버튼 클릭 이벤트
        binding.btnSave.setOnClickListener {
            binding.btnSave.isEnabled = false
            if (binding.edtTitle.text.isNotEmpty() && binding.edtContent.text.isNotEmpty() && imageUri != null) {
                val postingDAO = PostingDAO()
                // 게시물 저장 시 uid값 얻기
                val docID = postingDAO.databaseReference?.push()?.key
                val author: String? = dataList.get(0).communityNickname
                val title = binding.edtTitle.text.toString()
                val content = binding.edtContent.text.toString().trim()
                val date = getDateTimeString()
                val anonyPostingData =
                    AnonyPostingData(docID!!, currentUser, author!!, title, content, 0, 0, 0, date)

                // firebase realtimeDatabase의 picture 테이블에 클래스 저장
                postingDAO.databaseReference?.child(docID!!)?.setValue(anonyPostingData)
                    ?.addOnSuccessListener { // 성공시
                        Log.e("PictureAddActivity", "이미지 정보 업로드 성공")
                        // 사진값 저장
                        val pictureRef =
                            postingDAO.storage?.reference?.child("images/${docID}.png")
                        // storage에 이미지 업로드
                        pictureRef!!.putFile(imageUri!!).addOnSuccessListener {// 성공시
                            Toast.makeText(
                                applicationContext,
                                "게시글이 등록되었습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("PictureAddActivity", "이미지 업로드 성공")
                            finish()
                        }.addOnFailureListener {// 실패시
                            Toast.makeText(
                                applicationContext,
                                "게시글 등록 실패하였습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("PictureAddActivity", "이미지 업로드 실패")
                            binding.btnSave.isEnabled = true
                        }
                    }?.addOnFailureListener {// 실패시
                        Log.e("PictureAddActivity", "이미지 정보 업로드 실패")
                        binding.btnSave.isEnabled = true
                    }
            } else {
                // 사진 첨부가 안되었을 시
                Toast.makeText(applicationContext, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                binding.btnSave.isEnabled = true
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDateTimeString(): String {       // 메세지 보낸 시각 정보 반환
        try {
            var localDateTime = LocalDateTime.now()
            localDateTime.atZone(TimeZone.getDefault().toZoneId())
            var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            return localDateTime.format(dateTimeFormatter).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            throw java.lang.Exception("getTimeError")
        }
    }
}