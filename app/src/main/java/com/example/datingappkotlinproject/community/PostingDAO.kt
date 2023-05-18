package com.example.datingappkotlinproject.community

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

// FireBase RealTimeDatabase
class PostingDAO {
    var databaseReference : DatabaseReference? = null
    var storage : FirebaseStorage? = null

    init {
        // 실시간 데이터베이스에 연결
        val db = FirebaseDatabase.getInstance()
        databaseReference = db.getReference("posting")
        storage = Firebase.storage
    }
    // insert into user values(_,_,_,_)
    fun pictureInsert(anonyPostingData: AnonyPostingData) : Task<Void> {
        return databaseReference!!.push().setValue(anonyPostingData)
    }
    // select * from user
    fun pictureSelect() : Query?{
        return databaseReference
    }
}