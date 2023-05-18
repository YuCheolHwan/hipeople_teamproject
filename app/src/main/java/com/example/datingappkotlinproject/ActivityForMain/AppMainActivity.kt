package com.example.datingappkotlinproject.ActivityForMain

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.datingappkotlinproject.*
import com.example.datingappkotlinproject.ActivityForRegister.MainActivity
import com.example.datingappkotlinproject.chat.ChatFragment
import com.example.datingappkotlinproject.community.CommunityFragment
import com.example.datingappkotlinproject.databinding.ActivityAppMainBinding
import com.example.datingappkotlinproject.databinding.UsertabButtonBinding
import com.example.datingappkotlinproject.onefragment.OneFragment
import com.example.datingappkotlinproject.profile.ProfileActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// 앱 실행 / 로그인 완료 후 진행되는 액티비티
class AppMainActivity : AppCompatActivity() {
    lateinit var binding: ActivityAppMainBinding
    lateinit var customAdapter: CustomAdapter
    lateinit var tabTitleList: MutableList<String>
    lateinit var userInfoData: UserInfoData
    var exitFlag = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 현재 로그인 한 계정의 uid 얻기
        var currentUserUid = Firebase.auth.currentUser!!.uid

        //1. 액션바대신에 툴바로 대체한다.
        setSupportActionBar(binding.toolbar)

        // 현재 로그인 한 계정의 정보를 Firebase Realtime Database 에서 UserInfoData 클래스 형식으로 얻기
        Firebase.database.reference.child("User").child("users").orderByChild("uid")
            .equalTo("$currentUserUid")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        userInfoData = data.getValue(UserInfoData::class.java)!!
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        // Fragment와 ViewPager 연동
        tabTitleList = mutableListOf("이성", "채팅", "맛집", "커뮤니티")
        customAdapter = CustomAdapter(this)
        customAdapter.addListFragment(OneFragment())
        customAdapter.addListFragment(ChatFragment())
        customAdapter.addListFragment(RestFragment())
        customAdapter.addListFragment(CommunityFragment())
        binding.viewPager2.adapter = customAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.setCustomView(tabCustomView(position))
        }.attach()
        // ViewPager2에서 스와이프 동작이 비활성화
        binding.viewPager2.isUserInputEnabled = false
    }
    // 프로필 설정 메뉴 연동
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.navi_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // 프로필 설정 메뉴 연동
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("dataList", userInfoData)
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Fragment 탭 포지션 지정
    fun tabCustomView(position: Int): View {
        val binding = UsertabButtonBinding.inflate(layoutInflater)
        when (position) {
            0 -> binding.ivIcon.setImageResource(R.drawable.tablayout1)
            1 -> binding.ivIcon.setImageResource(R.drawable.tablayout2)
            2 -> binding.ivIcon.setImageResource(R.drawable.tablayout3)
            3 -> binding.ivIcon.setImageResource(R.drawable.tablayout4)
        }
        return binding.root
    }

    // Back버튼으로 인하여 앱 종료 방지(1.5초 안에 한번 더 누르면 종료)
    override fun onBackPressed() {
        if (exitFlag) {
            finishAffinity()
        } else {
            Toast.makeText(this, "뒤로 버튼을 한번 더 누르시면 종료 됩니다.", Toast.LENGTH_SHORT).show()
            exitFlag = true
            runDelayed(1500) {
                exitFlag = false
            }
        }
    }

    // onBackPressed에서 딜레이 주는 함수
    fun runDelayed(millis: Long, function: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed(function, millis)
    }
}
