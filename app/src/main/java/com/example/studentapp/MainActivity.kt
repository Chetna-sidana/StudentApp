package com.example.studentapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // RegisterActivity open करना
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)

        finish()
    }
}