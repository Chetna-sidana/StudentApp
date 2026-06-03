package com.example.studentapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StudentDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_detail)

        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val course = intent.getStringExtra("course")

        val nameText: TextView = findViewById(R.id.detailName)
        val emailText: TextView = findViewById(R.id.detailEmail)
        val courseText: TextView = findViewById(R.id.detailCourse)

        nameText.text = "Name : " + name
        emailText.text = "Email : " + email
        courseText.text = "Course : " + course
    }
}