package com.example.datingappkotlinproject.ActivityForRegister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.datingappkotlinproject.R
import com.example.datingappkotlinproject.databinding.ActivityAddressBinding

class AddressActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddressBinding
    lateinit var address: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 인텐트 값으로 회원가입 정보 받기
        val id = intent.getStringExtra("id")
        val pw = intent.getStringExtra("pw")
        val gender = intent.getStringExtra("gender")
        val purpose = intent.getStringExtra("purpose")
        val birth = intent.getStringExtra("birth")
        val height = intent.getStringExtra("height")
        val weight = intent.getStringExtra("weight")

        // 버튼 클릭 시 address값 얻기 및 버튼 활성화 / 비활성화
        binding.btnSeoul.setOnClickListener {
            address = "서울"
            buttonSelected(address)
        }

        binding.btnBusan.setOnClickListener {
            address = "부산"
            buttonSelected(address)

        }

        binding.btnDaegu.setOnClickListener {
            address = "대구"
            buttonSelected(address)

        }

        binding.btnIncheon.setOnClickListener {
            address = "인천"
            buttonSelected(address)

        }

        binding.btnGwanju.setOnClickListener {
            address = "광주"
            buttonSelected(address)

        }

        binding.btnDaejeon.setOnClickListener {
            address = "대전"
            buttonSelected(address)

        }

        binding.btnUlsan.setOnClickListener {
            address = "울산"
            buttonSelected(address)

        }

        binding.btnGyeongi.setOnClickListener {
            address = "경기"
            buttonSelected(address)

        }

        binding.btnGangwon.setOnClickListener {
            address = "강원"
            buttonSelected(address)

        }

        binding.btnChungbuk.setOnClickListener {
            address = "충북"
            buttonSelected(address)

        }

        binding.btnChungnam.setOnClickListener {
            address = "충남"
            buttonSelected(address)

        }

        binding.btnSejong.setOnClickListener {
            address = "세종"
            buttonSelected(address)

        }

        binding.btnJeonbuk.setOnClickListener {
            address = "전북"
            buttonSelected(address)

        }

        binding.btnJeonnam.setOnClickListener {
            address = "전남"
            buttonSelected(address)

        }

        binding.btnGyeonbuk.setOnClickListener {
            address = "경북"
            buttonSelected(address)

        }

        binding.btnGyeonnam.setOnClickListener {
            address = "경남"
            buttonSelected(address)

        }

        binding.btnJeju.setOnClickListener {
            address = "제주"
            buttonSelected(address)

        }

        // 인텐트 값으로 회원가입 정보 넘기기
        binding.btnNext6.setOnClickListener {
            val intent = Intent(this, EduActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("pw", pw)
            intent.putExtra("gender", gender)
            intent.putExtra("purpose", purpose)
            intent.putExtra("birth", birth)
            intent.putExtra("height", height)
            intent.putExtra("weight", weight)
            intent.putExtra("address", address)
            startActivity(intent)
        }
    }

    // 버튼 클릭 시 address값 얻기 및 버튼 활성화 / 비활성화 함수
    fun buttonSelected(address: String) {
        binding.btnSeoul.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnBusan.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnDaegu.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnIncheon.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnGwanju.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnDaejeon.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnUlsan.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnGyeongi.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnGangwon.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnChungbuk.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnChungnam.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnSejong.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnJeonbuk.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnJeonnam.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnGyeonbuk.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnGyeonnam.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnJeju.setBackgroundColor(getColor(R.color.btncolor))
        when (address) {
            "서울" -> binding.btnSeoul.setBackgroundColor(getColor(R.color.btncilck))
            "부산" -> binding.btnBusan.setBackgroundColor(getColor(R.color.btncilck))
            "대구" -> binding.btnDaegu.setBackgroundColor(getColor(R.color.btncilck))
            "인천" -> binding.btnIncheon.setBackgroundColor(getColor(R.color.btncilck))
            "광주" -> binding.btnGwanju.setBackgroundColor(getColor(R.color.btncilck))
            "대전" -> binding.btnDaejeon.setBackgroundColor(getColor(R.color.btncilck))
            "울산" -> binding.btnUlsan.setBackgroundColor(getColor(R.color.btncilck))
            "경기" -> binding.btnGyeongi.setBackgroundColor(getColor(R.color.btncilck))
            "강원" -> binding.btnGangwon.setBackgroundColor(getColor(R.color.btncilck))
            "충북" -> binding.btnChungbuk.setBackgroundColor(getColor(R.color.btncilck))
            "충남" -> binding.btnChungnam.setBackgroundColor(getColor(R.color.btncilck))
            "세종" -> binding.btnSejong.setBackgroundColor(getColor(R.color.btncilck))
            "전북" -> binding.btnJeonbuk.setBackgroundColor(getColor(R.color.btncilck))
            "전남" -> binding.btnJeonnam.setBackgroundColor(getColor(R.color.btncilck))
            "경북" -> binding.btnGyeonbuk.setBackgroundColor(getColor(R.color.btncilck))
            "경남" -> binding.btnGyeonnam.setBackgroundColor(getColor(R.color.btncilck))
            "제주" -> binding.btnJeju.setBackgroundColor(getColor(R.color.btncilck))

        }
    }
}