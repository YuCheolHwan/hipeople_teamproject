package com.example.datingappkotlinproject.ActivityForRegister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.datingappkotlinproject.R
import com.example.datingappkotlinproject.databinding.ActivityPurposeBinding

class PurposeActivity : AppCompatActivity() {
    lateinit var binding: ActivityPurposeBinding
    lateinit var purpose: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurposeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인텐트 값으로 회원가입 정보 받기
        val id = intent.getStringExtra("id")
        val pw = intent.getStringExtra("pw")
        val gender = intent.getStringExtra("gender")

        // 버튼 클릭 시 purpose값 얻기 및 버튼 활성화 / 비활성화
        binding.btnFriend.setOnClickListener {
            purpose = "친구 찾기"
            binding.btnFriend.setBackgroundColor(getColor(R.color.btncilck))
            binding.btnCasual.setBackgroundColor(getColor(R.color.btncolor))
            binding.btnLove.setBackgroundColor(getColor(R.color.btncolor))
        }
        binding.btnCasual.setOnClickListener {
            purpose = "캐쥬얼한 관계 찾기"
            binding.btnFriend.setBackgroundColor(getColor(R.color.btncolor))
            binding.btnCasual.setBackgroundColor(getColor(R.color.btncilck))
            binding.btnLove.setBackgroundColor(getColor(R.color.btncolor))
        }

        binding.btnLove.setOnClickListener {
            purpose = "인연 찾기"
            binding.btnFriend.setBackgroundColor(getColor(R.color.btncolor))
            binding.btnCasual.setBackgroundColor(getColor(R.color.btncolor))
            binding.btnLove.setBackgroundColor(getColor(R.color.btncilck))
        }

        // 인텐트 값으로 회원가입 정보 넘기기
        binding.btnNext3.setOnClickListener {
            val intent = Intent(this, BirthActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("pw", pw)
            intent.putExtra("gender", gender)
            intent.putExtra("purpose", purpose)
            startActivity(intent)
        }
    }
}