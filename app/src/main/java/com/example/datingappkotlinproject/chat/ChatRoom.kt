package com.example.datingappkotlinproject.chat

// 채팅방 데이터 클래스
data class ChatRoom(
    val users: Map<String, Boolean>? = HashMap(),
    val users2: Map<String, Boolean>? = HashMap(),
    var messages: Map<String, Message>? = HashMap()
) :java.io.Serializable


