package com.example.datingappkotlinproject

data class UserInfoData(
    val uid : String? = "",
    // 회원가입 아이디
    val id: String? = "",
    // 회원가입 패스워드
    val pw: String? = "",
    // 회원가입 성별
    val gender: String? = "",
    // 회원가입 목적
    val purpose: String? = "",
    // 회원가입 생년월일
    val birth: String? = "",
    // 회원가입 키
    val height: String? = "",
    // 회원가입 몸무게
    val weight: String? = "",
    // 회원가입 거주지역
    val address : String? = "",
    // 회원가입 학력
    val edu : String? = "",
    // 회원가입 별명
    val nickName: String? = "",
    // 회원가입 프로필
    val picture : String? = "",
    // 회원가입 좋아요
    val like : Int? = 0,
    // 커뮤니티 익명
    val communityNickname: String? =""

):java.io.Serializable
