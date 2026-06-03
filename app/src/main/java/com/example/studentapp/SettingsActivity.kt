package com.example.studentapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class SettingsActivity : AppCompatActivity() {

    val baseUrl =  AppConfig.BASE_URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode)

        // Saved dark mode state load karo
        val prefs = getSharedPreferences("GSE_PREFS", MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", false)
        switchDarkMode.isChecked = isDark

        // Dark mode toggle
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Toast.makeText(this, "🌙 Dark Mode ON", Toast.LENGTH_SHORT).show()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Toast.makeText(this, "☀️ Dark Mode OFF", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<TextView>(R.id.tvBack).setOnClickListener { finish() }

        findViewById<LinearLayout>(R.id.llChangePassword).setOnClickListener {
            showChangePasswordDialog()
        }

        findViewById<LinearLayout>(R.id.llEditProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.llLogout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    SessionManager.logout(this)
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrent = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
        val etNew = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirm = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val current = etCurrent.text.toString().trim()
                val new = etNew.text.toString().trim()
                val confirm = etConfirm.text.toString().trim()

                if (current.isEmpty() || new.isEmpty() || confirm.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (new != confirm) {
                    Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (new.length < 4) {
                    Toast.makeText(this, "Password must be at least 4 characters!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                changePassword(current, new)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun changePassword(current: String, new: String) {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "change_password.php",
            { response ->
                when (response.trim()) {
                    "success" -> Toast.makeText(this,
                        "Password changed successfully!", Toast.LENGTH_SHORT).show()
                    "wrong_password" -> Toast.makeText(this,
                        "Current password is incorrect!", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(this,
                        "Something went wrong!", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = SessionManager.getUserId(this@SettingsActivity)
                params["current_password"] = current
                params["new_password"] = new
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }
}