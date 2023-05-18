package com.example.datingappkotlinproject.ActivityForRegister

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.datingappkotlinproject.databinding.ActivityBirthBinding
import java.time.LocalDateTime

class BirthActivity : AppCompatActivity() {
    lateinit var binding: ActivityBirthBinding
    var yearFlag = false
    var monthFlag = false
    var dayFlag = false
    lateinit var year: String
    lateinit var month: String
    lateinit var day: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBirthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인텐트 값으로 회원가입 정보 받기
        val id = intent.getStringExtra("id")
        val pw = intent.getStringExtra("pw")
        val gender = intent.getStringExtra("gender")
        val purpose = intent.getStringExtra("purpose")
        // 탄생년란에 입력한 후에 다른 곳을 클릭하면
        binding.edtRegisterBirthYear.setOnFocusChangeListener { view, hasFocus ->
            yearFlag = false
            year = binding.edtRegisterBirthYear.text.toString()
            // 탄생년 패턴검사(1900~올해)
            if (year != "") {
                val yearInt = year.toInt()
                var yearIntFlag = false
                val today = LocalDateTime.now()
                val thisYear = today.year
                if (yearInt in 1900..thisYear) {
                    yearIntFlag = true
                }
                if (!hasFocus && year.isNotEmpty() && !yearIntFlag) {
                    Toast.makeText(this, "정확한 탄생년을 입력해 주세요", Toast.LENGTH_SHORT).show()
                    binding.edtRegisterBirthYear.text.clear()
                    // 문제가 없을 경우 통과
                } else if (year.isNotEmpty() && yearIntFlag) {
                    yearFlag = true
                }
            }
        }

        // 탄생월란에 입력한 후에 다른 곳을 클릭하면
        binding.edtRegisterBirthMonth.setOnFocusChangeListener { view, hasFocus ->
            monthFlag = false
            month = binding.edtRegisterBirthMonth.text.toString()
            // 탄생월 패턴 검사(1~12월 이내의 숫자를 입력)
            if (month != "") {
                val monthInt = month.toInt()
                var monthIntFlag = false
                if (monthInt in 1..12) {
                    monthIntFlag = true
                }
                if (!monthIntFlag && month.isNotEmpty()) {
                    Toast.makeText(this, "정확한 탄생월을 입력해 주세요", Toast.LENGTH_SHORT).show()
                    binding.edtRegisterBirthMonth.text.clear()
                    // 문제가 없을 경우 통과
                } else if (monthIntFlag && month.isNotEmpty()) {
                    monthFlag = true
                }
            }
            if (month.length == 1) {
                month = "0$month"
            }
        }

        // 인텐트 값으로 회원가입 정보 넘기기
        binding.btnNext4.setOnClickListener {
            day = binding.edtRegisterBirthDay.text.toString()
            if (day.length == 1) {
                day = "0$day"
            } else {
                day = binding.edtRegisterBirthDay.text.toString()
            }
            val birthDate = "${year}년 ${month}월 ${day}일"
            val intent = Intent(this, HeightActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("pw", pw)
            intent.putExtra("gender", gender)
            intent.putExtra("purpose", purpose)
            intent.putExtra("birth", birthDate)
            startActivity(intent)
        }
    }

    // 월에 따른 날짜 범위 설정
    fun checkBirthDate(monthInt: Int, dayInt: Int): Boolean {
        var returnValue = false
        when (monthInt) {
            1 -> returnValue = checkBirthDateByMonth(3, dayInt)
            2 -> returnValue = checkBirthDateByMonth(1, dayInt)
            3 -> returnValue = checkBirthDateByMonth(3, dayInt)
            4 -> returnValue = checkBirthDateByMonth(2, dayInt)
            5 -> returnValue = checkBirthDateByMonth(3, dayInt)
            6 -> returnValue = checkBirthDateByMonth(2, dayInt)
            7 -> returnValue = checkBirthDateByMonth(3, dayInt)
            8 -> returnValue = checkBirthDateByMonth(3, dayInt)
            9 -> returnValue = checkBirthDateByMonth(2, dayInt)
            10 -> returnValue = checkBirthDateByMonth(3, dayInt)
            11 -> returnValue = checkBirthDateByMonth(2, dayInt)
            12 -> returnValue = checkBirthDateByMonth(3, dayInt)
        }
        return returnValue
    }

    fun checkBirthDateByMonth(type: Int, dayInt: Int): Boolean {
        var returnValue = false
        when (type) {
            1 -> if (dayInt in 1..28) {
                returnValue = true
            }
            2 -> if (dayInt in 1..30) {
                returnValue = true
            }
            3 -> if (dayInt in 1..31) {
                returnValue = true
            }
        }
        return returnValue
    }
}