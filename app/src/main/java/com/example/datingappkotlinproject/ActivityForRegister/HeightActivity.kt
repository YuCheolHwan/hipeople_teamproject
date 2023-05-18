package com.example.datingappkotlinproject.ActivityForRegister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.datingappkotlinproject.databinding.ActivityHeightBinding

class HeightActivity : AppCompatActivity() {
    lateinit var binding: ActivityHeightBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인텐트 값으로 회원가입 정보 받기
        val id = intent.getStringExtra("id")
        val pw = intent.getStringExtra("pw")
        val gender = intent.getStringExtra("gender")
        val purpose = intent.getStringExtra("purpose")
        val birth = intent.getStringExtra("birth")

        // 인텐트 값으로 회원가입 정보 넘기기
        binding.btnNext5.setOnClickListener {
            val height = binding.edtHeight.text.toString()
            val weight = binding.edtWeight.text.toString()
            val intent = Intent(this, AddressActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("pw", pw)
            intent.putExtra("gender", gender)
            intent.putExtra("purpose", purpose)
            intent.putExtra("birth", birth)
            intent.putExtra("height", height)
            intent.putExtra("weight", weight)
            startActivity(intent)
        }
    }
}