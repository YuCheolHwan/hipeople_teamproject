package com.example.datingappkotlinproject.Login

import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

// Firebase Auth의 현재 사용자 얻기
class FBAuth {

    companion object {

        var auth: FirebaseAuth = FirebaseAuth.getInstance()

        fun getUid(): String {

            auth = FirebaseAuth.getInstance()

            return auth.currentUser?.uid.toString()
        }

    }
}