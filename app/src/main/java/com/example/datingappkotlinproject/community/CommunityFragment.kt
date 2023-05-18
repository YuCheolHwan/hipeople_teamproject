package com.example.datingappkotlinproject.community

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.datingappkotlinproject.ActivityForMain.AppMainActivity
import com.example.datingappkotlinproject.ActivityForRegister.MainActivity
import com.example.datingappkotlinproject.databinding.FragmentCommunityBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class CommunityFragment : Fragment() {
    lateinit var mainActivity: AppMainActivity
    lateinit var binding: FragmentCommunityBinding
    lateinit var postingData: MutableList<AnonyPostingData>
    lateinit var adapter: PictureAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as AppMainActivity
    }

    // Fragment의 생명주기를 이용하여 계속 새로운 값을 업데이트 하기
    override fun onResume() {
        super.onResume()
        connentAdapter()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommunityBinding.inflate(inflater)
        // 사진 게시판의 정보 얻기
        pictureDataLoading()
        // FloatButton을 이용하여 새로운 글 작성하기
        binding.floatingActionButton2.setOnClickListener {
            val intent = Intent(mainActivity.applicationContext, InputActivity::class.java)
            startActivity(intent)
        }
        // postingData MutableList 객체 생성
        postingData = mutableListOf()
        // Fragment와 리사이클러 뷰 어댑터 연결
        connentAdapter()
        return binding.root

    }

    // 사진 게시판의 정보 얻기
    private fun pictureDataLoading() {
        val postingDAO = PostingDAO()
        postingDAO.pictureSelect()?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postingData.clear()
                postingData.reverse()

                for (data in snapshot.children) {
                    val pictureData = data.getValue(AnonyPostingData::class.java)
                    if (pictureData != null) {
                        postingData.add(pictureData)
                        adapter.notifyDataSetChanged()
                    }
                    postingData.reverse()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PictureActivity", "${error.message}")
            }
        })
    }

    // 위에서 얻은 사진 게시판의 정보를 어댑터에 연결
    fun connentAdapter() {
        adapter = PictureAdapter(mainActivity, postingData)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(binding.root.context)
    }
}