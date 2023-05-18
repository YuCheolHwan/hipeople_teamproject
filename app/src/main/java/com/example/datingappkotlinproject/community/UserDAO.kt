package com.example.datingappkotlinproject.community

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query

class UserDAO {
    private var databaseReference: DatabaseReference? = null

    init {
        // 실시간 데이터베이스에 연결
        val db = FirebaseDatabase.getInstance()
        databaseReference = db.getReference("posting")

    }



    fun userSelectComment(postingPath: String): Query? {
        return databaseReference!!.child("$postingPath").child("comment")
    }


}