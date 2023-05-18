package com.example.datingappkotlinproject.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.datingappkotlinproject.UserInfoData
import com.example.datingappkotlinproject.databinding.ActivityChangeBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class ChangeActivity : AppCompatActivity() {
    lateinit var binding: ActivityChangeBinding
    lateinit var dataList: MutableList<UserInfoData>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataList = mutableListOf()

        // 현재 user uid
        val userId = Firebase.auth.currentUser?.uid ?: ""
        // RealTimeDatabase 경로 지정
        val database = Firebase.database.reference.child("User").child("users").child(userId)
        val userData = intent.getSerializableExtra("userData") as UserInfoData
        // 초기화
        binding.tvNickname.text = userData.nickName
        binding.tvBirth.setText(userData.birth.toString())
        binding.tvPurpose.setText(userData.purpose.toString())
        binding.tvAddress.setText(userData.address.toString())
        binding.tvEdu.setText(userData.edu.toString())
        binding.tvHeight.setText(userData.height.toString())
        binding.tvWeight.setText(userData.weight.toString())



        // 수정하기 버튼 클릭시 이벤트 처리
        binding.button.setOnClickListener {

            val user = mutableMapOf<String, Any>()

            val birth = binding.tvBirth.text.toString()
            val purpose = binding.tvPurpose.text.toString()
            val address = binding.tvAddress.text.toString()
            val edu = binding.tvEdu.text.toString()
            val height = binding.tvHeight.text.toString()
            val weight = binding.tvWeight.text.toString()

            user["birth"] = birth
            user["purpose"] = purpose
            user["address"] = address
            user["edu"] = edu
            user["height"] = height
            user["weight"] = weight

            // mutableMap 형식으로 업데이트
            database.updateChildren(user)

        }
    }

}
