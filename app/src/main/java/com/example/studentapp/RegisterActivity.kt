package com.example.studentapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    private var currentStep = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.name)
        val etEmail = findViewById<EditText>(R.id.email)
        val etPassword = findViewById<EditText>(R.id.password)
        val etMobile = findViewById<EditText>(R.id.mobile)
        val etLocation = findViewById<EditText>(R.id.location)
        val etSkillTeach = findViewById<EditText>(R.id.skillTeach)
        val etSkillLearn = findViewById<EditText>(R.id.skillLearn)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        val step1Layout = findViewById<LinearLayout>(R.id.step1Layout)
        val step2Layout = findViewById<LinearLayout>(R.id.step2Layout)
        val step3Layout = findViewById<LinearLayout>(R.id.step3Layout)

        val step1Bar = findViewById<View>(R.id.step1Bar)
        val step2Bar = findViewById<View>(R.id.step2Bar)
        val step3Bar = findViewById<View>(R.id.step3Bar)

        val tvStepLabel = findViewById<TextView>(R.id.tvStepLabel)
        val btnNext = findViewById<Button>(R.id.btnNext)
        val btnBack = findViewById<Button>(R.id.btnBack)

        fun updateStepUI() {
            when (currentStep) {
                1 -> {
                    step1Layout.visibility = View.VISIBLE
                    step2Layout.visibility = View.GONE
                    step3Layout.visibility = View.GONE
                    btnBack.visibility = View.GONE
                    btnNext.text = "Next →"
                    tvStepLabel.text = "Step 1 of 3 — Basic Info"
                    step1Bar.setBackgroundColor(0xFFFFFFFF.toInt())
                    step2Bar.setBackgroundColor(0x40FFFFFF)
                    step3Bar.setBackgroundColor(0x40FFFFFF)
                }
                2 -> {
                    step1Layout.visibility = View.GONE
                    step2Layout.visibility = View.VISIBLE
                    step3Layout.visibility = View.GONE
                    btnBack.visibility = View.VISIBLE
                    btnNext.text = "Next →"
                    tvStepLabel.text = "Step 2 of 3 — Contact Info"
                    step1Bar.setBackgroundColor(0xFFFFFFFF.toInt())
                    step2Bar.setBackgroundColor(0xFFFFFFFF.toInt())
                    step3Bar.setBackgroundColor(0x40FFFFFF)
                }
                3 -> {
                    step1Layout.visibility = View.GONE
                    step2Layout.visibility = View.GONE
                    step3Layout.visibility = View.VISIBLE
                    btnBack.visibility = View.VISIBLE
                    btnNext.text = "Create Account ✓"
                    tvStepLabel.text = "Step 3 of 3 — Your Skills"
                    step1Bar.setBackgroundColor(0xFFFFFFFF.toInt())
                    step2Bar.setBackgroundColor(0xFFFFFFFF.toInt())
                    step3Bar.setBackgroundColor(0xFFFFFFFF.toInt())
                }
            }
        }

        btnBack.setOnClickListener {
            if (currentStep > 1) {
                currentStep--
                updateStepUI()
            }
        }

        btnNext.setOnClickListener {
            when (currentStep) {
                1 -> {
                    val name = etName.text.toString().trim()
                    val email = etEmail.text.toString().trim()
                    val password = etPassword.text.toString().trim()

                    if (name.isEmpty() || name.length < 2) {
                        etName.error = "Enter valid name"
                        etName.requestFocus(); return@setOnClickListener
                    }
                    if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        etEmail.error = "Enter valid email"
                        etEmail.requestFocus(); return@setOnClickListener
                    }
                    if (password.isEmpty() || password.length < 4) {
                        etPassword.error = "Min 4 characters"
                        etPassword.requestFocus(); return@setOnClickListener
                    }
                    currentStep = 2
                    updateStepUI()
                }
                2 -> {
                    val mobile = etMobile.text.toString().trim()
                    val location = etLocation.text.toString().trim()

                    if (mobile.isEmpty() || mobile.length != 10) {
                        etMobile.error = "Enter 10 digit number"
                        etMobile.requestFocus(); return@setOnClickListener
                    }
                    if (location.isEmpty()) {
                        etLocation.error = "Enter your location"
                        etLocation.requestFocus(); return@setOnClickListener
                    }
                    currentStep = 3
                    updateStepUI()
                }
                3 -> {
                    val skillTeach = etSkillTeach.text.toString().trim()
                    val skillLearn = etSkillLearn.text.toString().trim()

                    if (skillTeach.isEmpty()) {
                        etSkillTeach.error = "Enter skill to teach"
                        etSkillTeach.requestFocus(); return@setOnClickListener
                    }
                    if (skillLearn.isEmpty()) {
                        etSkillLearn.error = "Enter skill to learn"
                        etSkillLearn.requestFocus(); return@setOnClickListener
                    }

                    // API Call
                    progressBar.visibility = View.VISIBLE
                    btnNext.isEnabled = false
                    btnNext.text = "Registering..."

                    val name = etName.text.toString().trim()
                    val email = etEmail.text.toString().trim()
                    val password = etPassword.text.toString().trim()
                    val mobile = etMobile.text.toString().trim()
                    val location = etLocation.text.toString().trim()

                    val request = object : StringRequest(
                        Request.Method.POST,
                        AppConfig.BASE_URL + "register.php",
                        { response ->
                            progressBar.visibility = View.GONE
                            btnNext.isEnabled = true
                            btnNext.text = "Create Account ✓"

                            try {
                                val obj = JSONObject(response)
                                val status = obj.getString("status")

                                if (status == "success") {
                                    val intent = Intent(this, CheckEmailActivity::class.java)
                                    intent.putExtra("email", email)
                                    startActivity(intent)
                                    finish()
                                } else if (status == "exists") {
                                    Toast.makeText(this,
                                        "Email already registered! Please login.",
                                        Toast.LENGTH_LONG).show()
                                    currentStep = 1
                                    updateStepUI()
                                } else {
                                    Toast.makeText(this,
                                        "Registration failed! Try again.",
                                        Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        { _ ->
                            progressBar.visibility = View.GONE
                            btnNext.isEnabled = true
                            btnNext.text = "Create Account ✓"
                            Toast.makeText(this,
                                "Connection error! Check internet.",
                                Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        override fun getParams(): MutableMap<String, String> {
                            val params = HashMap<String, String>()
                            params["name"] = name
                            params["email"] = email
                            params["mobile"] = mobile
                            params["location"] = location
                            params["skill_teach"] = skillTeach
                            params["skill_learn"] = skillLearn
                            params["password"] = password
                            return params
                        }

                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()
                            headers["ngrok-skip-browser-warning"] = "true"
                            headers["User-Agent"] = "SkillApp/1.0"
                            return headers
                        }
                    }

                    request.retryPolicy = DefaultRetryPolicy(30000, 0, 1f)
                    AppConfig.getQueue(this).add(request)
                }
            }
        }
    }
}