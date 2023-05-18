package com.example.datingappkotlinproject.ActivityForRegister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.datingappkotlinproject.ActivityForMain.AppMainActivity
import com.example.datingappkotlinproject.R
import com.example.datingappkotlinproject.databinding.ActivityEduBinding

class EduActivity : AppCompatActivity() {
    lateinit var binding: ActivityEduBinding
    lateinit var edu: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEduBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인텐트 값으로 회원가입 정보 받기
        val id = intent.getStringExtra("id")
        val pw = intent.getStringExtra("pw")
        val gender = intent.getStringExtra("gender")
        val purpose = intent.getStringExtra("purpose")
        val birth = intent.getStringExtra("birth")
        val height = intent.getStringExtra("height")
        val weight = intent.getStringExtra("weight")
        val address = intent.getStringExtra("address")

        // 버튼 클릭 시 edu값 얻기 및 버튼 활성화 / 비활성화
        binding.btnHighSchool.setOnClickListener {
            edu = "고등학교"
            buttonSelected(edu)
        }

        binding.btnCollege.setOnClickListener {
            edu = "전문대"
            buttonSelected(edu)

        }

        binding.btnUniversity.setOnClickListener {
            edu = "대학교"
            buttonSelected(edu)

        }

        binding.btnGraduateSchool.setOnClickListener {
            edu = "대학원"
            buttonSelected(edu)

        }

        binding.btnEtc.setOnClickListener {
            edu = "기타"
            buttonSelected(edu)

        }

        // 인텐트 값으로 회원가입 정보 넘기기
        binding.btnNext7.setOnClickListener {
            val intent = Intent(this, NickNameActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("pw", pw)
            intent.putExtra("gender", gender)
            intent.putExtra("purpose", purpose)
            intent.putExtra("birth", birth)
            intent.putExtra("height", height)
            intent.putExtra("weight", weight)
            intent.putExtra("address", address)
            intent.putExtra("edu", edu)
            startActivity(intent)
        }
    }
    // 버튼 클릭 시 edu값 얻기 및 버튼 활성화 / 비활성화 함수
    fun buttonSelected(edu : String){
        binding.btnHighSchool.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnCollege.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnUniversity.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnGraduateSchool.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnEtc.setBackgroundColor(getColor(R.color.btncolor))
        when(edu){
            "고등학교" -> binding.btnHighSchool.setBackgroundColor(getColor(R.color.btncilck))
            "전문대" -> binding.btnCollege.setBackgroundColor(getColor(R.color.btncilck))
            "대학교" -> binding.btnUniversity.setBackgroundColor(getColor(R.color.btncilck))
            "대학원" -> binding.btnGraduateSchool.setBackgroundColor(getColor(R.color.btncilck))
            "기타" -> binding.btnEtc.setBackgroundColor(getColor(R.color.btncilck))
        }
    }
}