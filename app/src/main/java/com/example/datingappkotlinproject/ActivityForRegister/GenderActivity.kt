package com.example.datingappkotlinproject.ActivityForRegister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.datingappkotlinproject.R
import com.example.datingappkotlinproject.databinding.ActivityGenderBinding

class GenderActivity : AppCompatActivity() {
    lateinit var binding: ActivityGenderBinding
    lateinit var gender: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 인텐트 값으로 회원가입 정보 받기
        val id = intent.getStringExtra("id")
        val pw = intent.getStringExtra("pw")
        binding = ActivityGenderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 버튼 클릭 시 gender값 얻기 및 버튼 활성화 / 비활성화
        binding.btnMan.setOnClickListener {
            gender = "남자"
            binding.btnMan.setBackgroundColor(getColor(R.color.btncilck))
            binding.btnWoman.setBackgroundColor(getColor(R.color.btncolor))
        }

        binding.btnWoman.setOnClickListener {
            gender = "여자"
            binding.btnMan.setBackgroundColor(getColor(R.color.btncolor))
            binding.btnWoman.setBackgroundColor(getColor(R.color.btncilck))
        }

        // 인텐트 값으로 회원가입 정보 넘기기
        binding.btnNext2.setOnClickListener {
            val intent = Intent(this, PurposeActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("pw", pw)
            intent.putExtra("gender", gender)
            startActivity(intent)
        }
    }
}