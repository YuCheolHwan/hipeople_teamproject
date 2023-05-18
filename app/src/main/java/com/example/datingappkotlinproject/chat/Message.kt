package com.example.datingappkotlinproject.chat

//채팅방에서 오간 메시지를 저장하는 객체
data class Message(
    var senderUid: String = "",     // 보낸 사람의 uid
    var sendedDate: String = "",    // 보낸 시각
    var content: String = "",       // 메세지 내용
    var confirmed:Boolean = false   // 상대방의 확인 여부
) : java.io.Serializable

