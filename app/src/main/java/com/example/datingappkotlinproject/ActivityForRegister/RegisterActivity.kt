package com.example.datingappkotlinproject.ActivityForRegister

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.example.datingappkotlinproject.databinding.ActivityRegisterBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding
    var id: String? = null
    var pw: String? = null
    var emailFlag = false
    var passwordFlag = false
    var flag = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 아이디의 중복확인을 위하여 글자 입력시마다 발생되는 콜백함수로 RealTimeDatabase에서 실시간 확인
        binding.edtRegisterId.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 패스워드 입력 칸을 기본적으로 안보이게 설정
                binding.llPassword.visibility = View.INVISIBLE
                // email 형식이 맞는지 확인하는 함수
                checkRegisterEmail(s.toString())
                // email 중복확인 하는 함수
                idCheckFirebase(s.toString())
                // 위의 두 함수에서 얻어온 flag 값으로 조건문을 통한 진행
                if (emailFlag == true && flag == true) {
                    binding.tvAno.visibility = View.VISIBLE
                    binding.tvAno.text = "사용 가능한 아이디 입니다."
                    binding.llPassword.visibility = View.VISIBLE
                    binding.tvAno.setTextColor(Color.BLUE)

                } else if (emailFlag == false) {
                    binding.tvAno.visibility = View.VISIBLE
                    binding.tvAno.text = """아이디 형식이 올바르지 않습니다. 
                | ex) XXXXX@gmail.com""".trimMargin()
                    binding.tvAno.setTextColor(Color.RED)
                    binding.llPassword.visibility = View.INVISIBLE

                } else if (flag == false) {
                    binding.llPassword.visibility = View.INVISIBLE
                }
            }
        })

        // 패스워드의 패턴 맞춤을 위하여 글자 입력시마다 발생되는 콜백함수로 확인
        binding.edtRegisterPw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                // 입력값과 패턴 검색 값으로 조건문 진행
                if (s!!.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&.])[A-Za-z[0-9]$@$!%*#?&.]{8,15}$".toRegex())) {
                    passwordFlag = true
                    binding.tvAnot.setTextColor(Color.BLACK)
                } else {
                    binding.tvAnot.setTextColor(Color.RED)
                }
                // 최종적으로 입력된 패스워드 값을 저장
                pw = s.toString()
            }
        })

        // 현재 입력된 패스워드값과 재확인 패스워드의 값이 일치하는지 글자 입력시마다 발생되는 콜백함수로 확인
        binding.edtRegisterRePw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                // 저장된 패스워드와 현재 입력중인 값과 일치하는지 확인
                if (pw!!.matches(s.toString().toRegex()) && passwordFlag == true) {
                    binding.btnNext.visibility = View.VISIBLE
                    binding.tvAno3.visibility = View.INVISIBLE
                } else {
                    binding.btnNext.visibility = View.INVISIBLE
                    binding.tvAno3.visibility = View.VISIBLE
                    binding.tvAno3.setTextColor(Color.RED)

                }
            }
        })

        // 입력된 값을 인텐트로 넘기기
        binding.btnNext.setOnClickListener {
            id = binding.edtRegisterId.text.toString()
            pw = binding.edtRegisterPw.text.toString()
            val intent = Intent(this, GenderActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("pw", pw)
            startActivity(intent)

        }
    }



    fun idCheckFirebase(id: String) {
        Firebase.database.reference.child("User").child("users").orderByChild("id")
            .equalTo(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) { //채팅방이 없는 경우
                        flag = true
                    } else if (snapshot.value != null) {
                        flag = false
                        binding.llPassword.visibility = View.INVISIBLE
                        binding.tvAno.visibility = View.VISIBLE
                        binding.tvAno.text = "중복된 아이디입니다."
                        binding.tvAno.setTextColor(Color.RED)
                    } else {
                        binding.tvAno.visibility = View.INVISIBLE
                        binding.tvAno.text = "아이디 입력중"
                    }
                }
            })
    }

    fun checkRegisterEmail(email: String) {
        emailFlag = false
        if (email.isNotEmpty() && !email.contains("@") && !(email.length > 15)) {
            emailFlag = false
            binding.tvAno.visibility = View.VISIBLE
            binding.tvAno.text = """아이디 형식이 올바르지 않습니다. 
                | ex) XXXXX@gmail.com""".trimMargin()
            binding.tvAno.setTextColor(Color.RED)
        } else if (email.isNotEmpty() && email.contains("@") && email.length > 15) {
            binding.tvAno.visibility = View.VISIBLE
            emailFlag = true
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }
}