package com.example.datingappkotlinproject.community

import java.io.Serializable

// 게시판 구현을 위한 데이터 클래스, 객체를 넘겨주기 위하여 Serializable 상속
data class AnonyPostingData(
    var key: String = "",
    var writer : String= "",
    var nickName: String = "",
    var title: String = "",
    var content: String = "",
    var tvLike: Int = 0,
    var tvComment: Int = 0,
    var tvHits: Int = 0,
    var tvDate: String = "",
) : Serializable