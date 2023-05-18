package com.example.datingappkotlinproject.ActivityForRegister

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.datingappkotlinproject.ActivityForMain.AppMainActivity
import com.example.datingappkotlinproject.R
import com.example.datingappkotlinproject.RestFragment
import com.example.datingappkotlinproject.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// 앱 실행시 로딩화면 후 첫 메인화면(로그인 및 회원가입 담당)
class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityMainBinding
    lateinit var auth: FirebaseAuth
    lateinit var spf: SharedPreferences
    private var mLayout: View? = null // Snackbar 사용하기 위해서는 View가 필요합니다.

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) // 외부 저장소

    // 구글맵 퍼미션 정의
    companion object {
        private val TAG = "googlemap_example"
        private val GPS_ENABLE_REQUEST_CODE = 2001
        private val UPDATE_INTERVAL_MS = 1000 // 1초
        private val FASTEST_UPDATE_INTERVAL_MS = 500 // 0.5초
        private val PERMISSIONS_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mLayout = binding.mainActivity
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
            hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED
        ) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    REQUIRED_PERMISSIONS[0]
                )
            ) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(
                    mLayout!!, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("확인") { // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions(
                            this, REQUIRED_PERMISSIONS,
                            PERMISSIONS_REQUEST_CODE
                        )
                    }.show()
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }

        // 한번 로그인 후 자동로그인 기능을 위한 SharedPreference 정의
        // SharedPreference에 저장된 Boolean 값이 true 라면 자동 로그인 후 AppMainActivity로 이동
        auth = FirebaseAuth.getInstance()       // Firebase 계정 관련 변수
        spf = getSharedPreferences("loginKeep", Context.MODE_PRIVATE)
        if (spf.getBoolean("isLogin", false)) {
            val intent = Intent(this, AppMainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.btnSignUp.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnLogin -> {
                // 로그인 함수
                signInWithEmailAndPassword()
            }
            R.id.btnSignUp -> {
                // 회원가입을 위한 액티비티로 이동
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }
        }
    }

    // 로그인 함수
    private fun signInWithEmailAndPassword() {
        try {
            // 아이디 또는 패스워드가 입력되었는지 체크
            if (binding.edtId.text.toString().isNullOrBlank() && binding.edtPassword.text.toString()
                    .isNullOrBlank()
            ) {
                Toast.makeText(this, "아이디 또는 패스워드를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // Firebase Auth의 함수를 이용하여 Id값과 Pw가 일치하는지 확인
                auth.signInWithEmailAndPassword(
                    binding.edtId.text.toString(),
                    binding.edtPassword.text.toString()
                )
                        // 성공시 콜백함수
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "성공", Toast.LENGTH_SHORT).show()
                            Log.e("MainActivity", "로그인 성공")
                            // 자동 로그인을 위한 SharedPreference에 Boolean값 저장 로그인이 성공하면 true 아니면 false
                            val spfEdit = spf.edit()
                            spfEdit.putBoolean("isLogin", true)
                            spfEdit.apply()
                            val intent = Intent(this, AppMainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // 로그인 실패 시 아이디가 잘못 입력 되었는지, 패스워드가 잘못 입력되었는지 Firebase RealtimeDatabase에서 확인
                            val id = binding.edtId.text.toString()
                            Firebase.database.reference.child("user").orderByChild("userEmail")
                                .equalTo(id)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.value != null) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "패스워드를 확인해주세요.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "가입된 정보가 없습니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }
            }
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, "아이디 또는 패스워드를 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}