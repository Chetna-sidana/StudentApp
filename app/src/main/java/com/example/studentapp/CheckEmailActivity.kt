package com.example.studentapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CheckEmailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_email)

        val email = intent.getStringExtra("email") ?: ""

        val tvEmail = findViewById<TextView>(R.id.tvEmailAddress)
        tvEmail.text = email

        // Login screen pe le jao
        val btnGoToLogin = findViewById<Button>(R.id.btnGoToLogin)
        btnGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}