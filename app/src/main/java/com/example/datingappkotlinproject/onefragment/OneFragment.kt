package com.example.datingappkotlinproject.onefragment

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.datingappkotlinproject.ActivityForMain.AppMainActivity
import com.example.datingappkotlinproject.R
import com.example.datingappkotlinproject.UserInfoData
import com.example.datingappkotlinproject.databinding.FragmentOneBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction

// 이성추천 Fragment
class OneFragment : Fragment() {
    lateinit var binding: FragmentOneBinding
    lateinit var mainActivity: AppMainActivity
    lateinit var cardStackAdapter: CardStackAdapter
    lateinit var datalist: MutableList<UserInfoData>
    lateinit var manager: CardStackLayoutManager
    var currentUserUid = Firebase.auth.currentUser!!.uid
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as AppMainActivity // mainActivity context 얻기
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_one, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // cardStackView 저장
        val cardStackView = getView()?.findViewById<CardStackView>(R.id.cardStackView)
        manager = CardStackLayoutManager(requireActivity(), object : CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {}

            override fun onCardSwiped(direction: Direction?) {}

            override fun onCardRewound() {}

            override fun onCardCanceled() {}

            override fun onCardAppeared(view: View?, position: Int) {}

            override fun onCardDisappeared(view: View?, position: Int) {}
        })
        // mutableList 객체 생성
        datalist = mutableListOf()
        // 어댑터 연결
        cardStackAdapter = CardStackAdapter(requireActivity(), datalist)
        cardStackView?.layoutManager = manager
        cardStackView?.adapter = cardStackAdapter
        // 이성만을 추천하기 위하여 현재 접속한 user의 성별 얻기
        getUserGender()
    }

    // 이성만을 추천하기 위하여 현재 접속한 user의 성별 얻기 함수
    private fun getUserGender() {
        FBRef.userRef.orderByChild("uid").equalTo(currentUserUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // 현재 유저의 정보를 UserInfoData 클래스 형식에 맞게 얻기
                    for (data in snapshot.children) {
                        val user = data.getValue(UserInfoData::class.java)
                        var gender = user!!.gender.toString()
                        getUserDataList(gender)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    // 현재 user의 성별을 이용하여 반대 성별에 대한 데이터 얻기 함수
    private fun getUserDataList(gender: String) {
        var genderFlag = ""
        if (gender == "남자") {
            genderFlag = "여자"

        } else if (gender == "여자") {
            genderFlag = "남자"

        } else {
            Log.e("OneFragment - 이성 추천(성별)", "gender 오류")
        }
        FBRef.userRef.orderByChild("gender").equalTo("$genderFlag")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (dataModel in dataSnapshot.children) {
                        val user = dataModel.getValue(UserInfoData::class.java)
                        datalist.add(user!!)
                    }
                    cardStackAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
    }
}