package com.example.datingappkotlinproject.onefragment

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FBRef {

    companion object {

        private val database = Firebase.database

        val userRef = database.getReference("User/users")
    }
}