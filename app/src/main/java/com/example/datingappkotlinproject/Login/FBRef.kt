package com.example.datingappkotlinproject.Login

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// Firebase RealTimeDatabase의 user 경로 지정
class FBRef {

    companion object {

        private val database = Firebase.database

        val userRef = database.getReference("user")
    }
}