package com.example.studentapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ForgotPasswordActivity : AppCompatActivity() {

    val baseUrl =  AppConfig.BASE_URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnRecover = findViewById<Button>(R.id.btnRecover)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val resultCard = findViewById<CardView>(R.id.resultCard)
        val tvFoundName = findViewById<TextView>(R.id.tvFoundName)
        val tvFoundPassword = findViewById<TextView>(R.id.tvFoundPassword)
        val btnGoLogin = findViewById<Button>(R.id.btnGoLogin)

        findViewById<TextView>(R.id.tvBack).setOnClickListener { finish() }

        btnRecover.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Please enter your email"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Please enter a valid email"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnRecover.isEnabled = false
            btnRecover.text = "Searching..."
            resultCard.visibility = View.GONE

            val request = object : StringRequest(
                Request.Method.POST, baseUrl + "forgot_password.php",
                { response ->
                    progressBar.visibility = View.GONE
                    btnRecover.isEnabled = true
                    btnRecover.text = "Recover Password"

                    try {
                        val obj = JSONObject(response)
                        when (obj.getString("status")) {
                            "success" -> {
                                tvFoundName.text = "Account found: ${obj.getString("name")}"
                                tvFoundPassword.text = obj.getString("password")
                                resultCard.visibility = View.VISIBLE
                            }
                            "not_found" -> {
                                Toast.makeText(this,
                                    "No account found with this email!",
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    progressBar.visibility = View.GONE
                    btnRecover.isEnabled = true
                    btnRecover.text = "Recover Password"
                    Toast.makeText(this, "Connection error!", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["email"] = email
                    return params
                }
            }
            AppConfig.getQueue(this).add(request)
        }

        btnGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}