package com.example.datingappkotlinproject.ActivityForRegister

import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.datingappkotlinproject.Login.FBAuth
import com.example.datingappkotlinproject.Login.FBRef
import com.example.datingappkotlinproject.UserInfoData
import com.example.datingappkotlinproject.databinding.ActivityNickNameBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

import java.io.ByteArrayOutputStream

class NickNameActivity : AppCompatActivity() {
    lateinit var binding: ActivityNickNameBinding
    var key: String? = null
    var userFlag = false
    lateinit var id: String
    lateinit var pw: String
    lateinit var gender: String
    lateinit var purpose: String
    lateinit var birth: String
    lateinit var height: String
    lateinit var weight: String
    lateinit var address: String
    lateinit var edu: String
    var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNickNameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 인텐트 값으로 회원가입 정보 받기
        id = intent.getStringExtra("id")!!
        pw = intent.getStringExtra("pw")!!
        gender = intent.getStringExtra("gender")!!
        purpose = intent.getStringExtra("purpose")!!
        birth = intent.getStringExtra("birth")!!
        height = intent.getStringExtra("height")!!
        weight = intent.getStringExtra("weight")!!
        address = intent.getStringExtra("address")!!
        edu = intent.getStringExtra("edu")!!

        // 회원가입에 필요한 사진 정보를 받기 위하여 ImageView 클릭 이벤트로 사진 정보 얻기
        binding.imageView2.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"

            startActivityForResult(intent, 1)
        }
        // 닉네임의 중복확인을 위하여 글자 입력시마다 발생되는 콜백함수로 RealTimeDatabase에서 실시간 확인
        binding.edtRegisterNickName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                // 입력값이 Null 또는 Blank가 아니여야 하고 지정한 패턴이 맞아야 중복확인 함수로 이동
                if (!(s.isNullOrBlank()) && s!!.matches("^[가-힣ㄱ-ㅎa-zA-Z0-9._-]{2,}\$".toRegex())) {
                    // 닉네임 중복확인 함수
                    nickNameCheckFirebase(s.toString())
                } else {
                    // 기본값으로 사용 가능한 타입 안내 TextView 보이기
                    binding.tvAno2.text = "한글/영어/숫자/밑줄을 사용할 수 있습니다."
                    binding.tvAno2.setTextColor(Color.BLACK)
                    binding.btnCompleteRegister.isEnabled = false
                }
            }
        })

        // 회원가입 최종 버튼 클릭 이벤트, 인텐트로 받아온 정보와 입력한 닉네임을 통하여 signUp 함수 이용
        binding.btnCompleteRegister.setOnClickListener {
            val id = intent.getStringExtra("id")
            val pw = intent.getStringExtra("pw")
            val nickName = binding.edtRegisterNickName.text.toString()
            val key = FBRef.userRef.push().key.toString()
            this.key = key

            // image 값을 필수로 받기 위한 조건문
            if (imageUri == null) {
                Toast.makeText(this, "프로필 사진을 선택해주세요.", Toast.LENGTH_SHORT).show()
                binding.btnCompleteRegister.isEnabled = false
            } else {
                signUp(id!!, pw!!, nickName)
            }
        }
    }

    // Firebase의 Storage에 사용자 프로필 사진을 업로드 하는 함수
    private fun userPictureUpload(uid: String) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("$uid.png")

        binding.imageView2.isDrawingCacheEnabled = true
        binding.imageView2.buildDrawingCache()
        val drawable = binding.imageView2.drawable
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            var uploadTask = mountainsRef.putBytes(data)
            uploadTask.addOnFailureListener {
            }.addOnSuccessListener { taskSnapshot ->
            }
        }
    }

    // image 업로드의 결과값을 얻어 회원가입 완료 버튼을 보이게 하는 함수
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 1) {
            binding.imageView2.setImageURI(data?.data)
            imageUri = data?.data
            binding.btnCompleteRegister.isEnabled = true
        }
    }

    // 회원 가입 실행 함수
    private fun signUp(email: String, password: String, name: String) {
        FBAuth.auth.createUserWithEmailAndPassword(email, password)
            // FirebaseAuth에 회원가입 성공 시
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {        // 회원가입 성공 시
                    // 익명 게시판에 활용할 익명 닉네임 부여
                    val communityNickName = "익명${(Math.random() * 100).toInt()}"
                    try {
                        val uid = FBAuth.getUid()
                        val nickName = binding.edtRegisterNickName.text.toString()
                        // intent로 받아온 모든 값을 UserInfoData 클래스 형식에 맞게 RealTimeDatabase에 기입
                        FirebaseDatabase.getInstance().getReference("User").child("users")
                            .child(uid.toString()).setValue(
                                UserInfoData(
                                    uid,
                                    id,
                                    pw,
                                    gender,
                                    purpose,
                                    birth,
                                    height,
                                    weight,
                                    address,
                                    edu,
                                    nickName,
                                    "",
                                    0,
                                    communityNickName
                                )
                            )       // Firebase RealtimeDatabase에 User 정보 추가
                        Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@NickNameActivity, MainActivity::class.java)
                        startActivity(intent)
                        // Firebase Storage에 자신의 uid값으로 사진 저장하는 함수
                        userPictureUpload(uid)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "화면 이동 중 문제 발생", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // 닉네임 중복확인 하는 함수
    fun nickNameCheckFirebase(nickName: String) {
        // Firebase RealTimeDatabase의 경로값 안의 userNickname에서 현재 입력한 닉네임이 있는지 확인
        Firebase.database.reference.child("User").child("users").orderByChild("userNickname")
            .equalTo(nickName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) { //닉네임이 없는 경우
                        userFlag = true
                        binding.tvAno2.visibility = View.VISIBLE
                        binding.tvAno2.text = "한글/영어/숫자/밑줄을 사용할 수 있습니다."
                        binding.tvAno2.setTextColor(Color.BLUE)
                        binding.btnCompleteRegister.isEnabled = true
                    } else if (snapshot.value != null) { // 닉네임이 있는 경우
                        binding.tvAno2.visibility = View.VISIBLE
                        binding.tvAno2.text = "중복된 닉네임입니다."
                        binding.tvAno2.setTextColor(Color.RED)
                        userFlag = false
                        binding.btnCompleteRegister.isEnabled = false
                    } else { // 기본값
                        binding.tvAno2.visibility = View.INVISIBLE
                        binding.tvAno2.text = "한글/영어/숫자/밑줄을 사용할 수 있습니다."
                        binding.tvAno2.setTextColor(Color.BLACK)
                        userFlag = false
                        binding.btnCompleteRegister.isEnabled = false
                    }
                }
            })
    }

}