package com.example.studentapp

data class User(

    val name:String,
    val email:String,
    val mobile:String,
    val location:String,
    val skillTeach:String,
    val skillLearn:String,
    val experience: String = "",
    val description: String = ""

)