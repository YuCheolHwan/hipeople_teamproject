package com.example.datingappkotlinproject.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.datingappkotlinproject.ActivityForMain.AppMainActivity
import com.example.datingappkotlinproject.ActivityForRegister.MainActivity
import com.example.datingappkotlinproject.R
import com.example.datingappkotlinproject.UserInfoData
import com.example.datingappkotlinproject.community.PostingDAO
import com.example.datingappkotlinproject.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// 기본 프로필 정보 액티비티(프로필 수정, 로그아웃, 회원탈퇴)
class ProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileBinding
    lateinit var spf: SharedPreferences
    var currentUserUid = Firebase.auth.currentUser!!.uid

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // intent로 현재 사용자의 데이터 얻기
        val userData = intent.getSerializableExtra("dataList") as UserInfoData
        binding.tvNickname.text = userData.nickName
        // 현재 사용자의 사진 로드
        val postingDAO = PostingDAO()
        val pictureRef = postingDAO.storage!!.reference.child("${currentUserUid}.png")
        pictureRef.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e("pictureAdapter", "Success")
                // Glide를 통하여 imageView에 현재 사용자 프로필 사진 로드
                Glide.with(applicationContext).load(it.result).into(binding.circleImageView)
            }
        }
        // 프로필 수정 이벤트 처리
        binding.profileChange.setOnClickListener {
            val intent = Intent(this, ChangeActivity::class.java)
            intent.putExtra("userData", userData)
            startActivity(intent)
        }
        // dialog를 통한 로그아웃 기능 이벤트 처리
        binding.btnSignout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("확인") { dialog, id ->
                    // 로그아웃을 하면 sharedPreference가 해제 되어야 하여 Boolean값 재 등록
                    spf = getSharedPreferences("loginKeep", Context.MODE_PRIVATE)
                    val spfEdit = spf.edit()
                    spfEdit.putBoolean("isLogin", false)
                    spfEdit.apply()
                    try {
                        // Firebase Auth 에서 로그아웃
                        Firebase.auth.signOut()
                        startActivity(Intent(this, MainActivity::class.java))
                        dialog.dismiss()
                        finish()

                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        dialog.dismiss()
                        Toast.makeText(this, "로그아웃 중 오류가 발생하였습니다.", Toast.LENGTH_LONG).show()
                    }
                }.setNegativeButton("취소") {         // 다이얼로그 닫기
                        dialog, id ->
                    dialog.dismiss()
                }
            builder.show()

        }
        // dialog를 통하여 회원탈퇴 이벤트 처리
        binding.btnRemove.setOnClickListener {
            val builder = AlertDialog.Builder(this)
                .setTitle("회원탈퇴")
                .setMessage("정말 회원탈퇴 하시겠습니까?")
                .setPositiveButton("확인") { dialog, id ->
                    // 회원탈퇴를 하면 sharedPreference가 해제 되어야 하여 Boolean값 재 등록
                    spf = getSharedPreferences("loginKeep", Context.MODE_PRIVATE)
                    val spfEdit = spf.edit()
                    spfEdit.putBoolean("isLogin", false)
                    spfEdit.apply()
                    // 회원탈퇴 완료 알림
                    Toast.makeText(this, "그동안 이용해 주셔서 감사합니다.", Toast.LENGTH_SHORT).show()
                    try {
                        // RealTimeDatabase에서 현재 user 정보 삭제
                        Firebase.database.reference.child("User").child("users")
                            .child("$currentUserUid").setValue(null)
                            .addOnCompleteListener { Log.e("ProfileActivity", "회원 탈퇴 성공") }
                            .addOnFailureListener {
                                Log.e("ProfileActivity", "회원 탈퇴 실패  /  ${it.message}")
                            }
                        dialog.dismiss()
                        finish()
                        startActivity(Intent(this, MainActivity::class.java))
                        // Firebase Auth 에서 현재 user 삭제
                        FirebaseAuth.getInstance().currentUser!!.delete()

                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        dialog.dismiss()
                    }
                }.setNegativeButton("취소") {         // 다이얼로그 닫기
                        dialog, id ->
                    dialog.dismiss()
                }
            builder.show()


        }
    }


    override fun onBackPressed() {
        startActivity(Intent(this, AppMainActivity::class.java))
        finish()
    }

}