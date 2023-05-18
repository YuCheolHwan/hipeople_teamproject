package com.example.datingappkotlinproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.datingappkotlinproject.ActivityForRegister.MainActivity
import com.example.datingappkotlinproject.databinding.ActivitySplashActivtyBinding

class SplashActivty : AppCompatActivity() {
    lateinit var binding: ActivitySplashActivtyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashActivtyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Actionbar 제거
        supportActionBar?.hide()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(Runnable {
            Intent(this, MainActivity::class.java).apply {
                startActivity(this)
                finish()
            }
        }, 3000) // 3초 후(3000) 스플래시 화면을 닫습니다
    }
}