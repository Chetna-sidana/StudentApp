package com.example.studentapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject

class VerifyEmailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_email)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Deep link se token lo
        val uri = intent.data
        val token = uri?.getQueryParameter("token")

        if (token == null) {
            tvStatus.text = "Invalid verification link!"
            return
        }

        // API call karo
        progressBar.visibility = View.VISIBLE
        tvStatus.text = "Verifying..."

        val url = "AppConfig.BASE_URLverify_email.php?token=$token"

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                progressBar.visibility = View.GONE
                try {
                    val obj = JSONObject(response)
                    val status = obj.getString("status")

                    if (status == "success") {
                        tvStatus.text = "✅ Email Verified Successfully!"
                        // 2 second baad login pe bhejo
                        tvStatus.postDelayed({
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }, 2000)
                    } else {
                        tvStatus.text = "❌ Link expired or invalid!"
                    }
                } catch (e: Exception) {
                    tvStatus.text = "Something went wrong!"
                }
            },
            { error ->
                progressBar.visibility = View.GONE
                tvStatus.text = "Connection error!"
            }
        )

        AppConfig.getQueue(this).add(request)
    }
}