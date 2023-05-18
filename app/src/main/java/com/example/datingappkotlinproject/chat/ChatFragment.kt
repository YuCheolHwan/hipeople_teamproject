package com.example.datingappkotlinproject.chat

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datingappkotlinproject.ActivityForMain.AppMainActivity
import com.example.datingappkotlinproject.databinding.FragmentChatBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class ChatFragment : Fragment() {
    lateinit var binding: FragmentChatBinding
    lateinit var appMainActivity: AppMainActivity
    lateinit var recylcerChatroom: RecyclerView
    lateinit var adapter : RecyclerChatRoomsAdapter
    override fun onAttach(context: Context) {
        super.onAttach(context)
        appMainActivity = context as AppMainActivity // mainActivity context 얻기
    }

    // Fragment의 생명주기를 이용하여 계속 새로운 값을 업데이트 하기
    override fun onResume() {
        super.onResume()
        setupRecycler()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater)
        initializeView()
        setupRecycler()
        return binding.root
    }

    private fun initializeView() {      // 뷰 초기화
        try {
            // Firebase RealTimeDatabase 경로 설정
            recylcerChatroom = binding.recyclerChatroom
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(appMainActivity, "화면 초기화 중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecycler() {
        // Fragment에 리사이클러뷰 연결
        adapter = RecyclerChatRoomsAdapter(appMainActivity)
        recylcerChatroom.adapter = adapter
        recylcerChatroom.layoutManager = LinearLayoutManager(appMainActivity)
    }

}