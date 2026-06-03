package com.example.studentapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val ivLogo = findViewById<ImageView>(R.id.ivLogo)
        ivLogo.outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
        ivLogo.clipToOutline = true

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false
            btnLogin.text = "Logging in..."

            val request = object : StringRequest(
                Request.Method.POST,
                AppConfig.BASE_URL + "login.php",
                { response ->
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    btnLogin.text = "LOGIN"

                    try {
                        val obj = JSONObject(response)
                        val status = obj.getString("status")

                        if (status == "success") {
                            val prefs = getSharedPreferences("GSE_PREFS", Context.MODE_PRIVATE)
                            val editor = prefs.edit()
                            editor.putString("user_id", obj.getString("id"))
                            editor.putString("user_name", obj.getString("name"))
                            editor.putString("user_email", obj.getString("email"))
                            editor.apply()

                            Toast.makeText(this, "Welcome ${obj.getString("name")}!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        } else if (status == "not_verified") {
                            Toast.makeText(this, "Please verify your email first!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Invalid email or password!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    btnLogin.text = "LOGIN"
                    Toast.makeText(this, "Network Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["ngrok-skip-browser-warning"] = "true"
                    headers["User-Agent"] = "SkillApp/1.0"
                    return headers
                }

                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["email"] = email
                    params["password"] = password
                    return params
                }
            }
            AppConfig.getQueue(this).add(request)
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }
}