package com.example.datingappkotlinproject.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.datingappkotlinproject.databinding.CommentItemBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// 댓글 어댑터
class CommentAdapter(val data: MutableList<CommentItemData>, val key: String) :
    RecyclerView.Adapter<CommentAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        // 바인딩
        val binding = holder.binding
        // 댓글 작성자와 댓글 내용 보이기
        binding.tvAuthor.text = data.get(position).tvAuthor
        binding.tvComment2.text = data.get(position).tvComment

        // 현재 접속된 user의 uid값 저장
        val userUid = Firebase.auth.currentUser!!.uid
        // 댓글을 작성한 user의 uid값 저장
        val writerUid = data.get(position).tvWriterUid

        // 접속된 uid와 작성한 uid값이 같다면 댓글 삭제 버튼 보이게 하기
        // 작성한 유저만 댓글 삭제 가능케 하기
        if (userUid.equals(writerUid)) {
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.btnDelete.visibility = View.INVISIBLE
        }
        // 삭제 버튼 누를시 댓글 삭제 Dialog 띄우고 이벤트 처리
        binding.btnDelete.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(binding.root.context)
                .setTitle("댓글 삭제")
                .setMessage("삭제 하시겠습니까?")
                .setPositiveButton("확인") { dialog, id ->

                    try {
                        // RealTimeDatabase 에서 댓글 삭제
                        Firebase.database.getReference("posting").child(key).child("comment")
                            .child(data.get(position).key).setValue(null)
                        Toast.makeText(binding.root.context, "댓글 삭제 완료", Toast.LENGTH_SHORT).show()
                        // 완료 후 dialog 닫기
                        dialog.dismiss()

                    } catch (e: java.lang.Exception) {
                        dialog.dismiss()
                    }
                }.setNegativeButton("취소") {         // 다이얼로그 닫기
                        dialog, id ->
                    dialog.dismiss()
                }
            builder.show()
        }
    }

    inner class CustomViewHolder(val binding: CommentItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}