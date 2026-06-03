package com.example.studentapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Status bar dark karo
        window.statusBarColor = Color.parseColor("#0A0A1A")

        val prefs = getSharedPreferences("GSE_PREFS", MODE_PRIVATE)
        if (prefs.getBoolean("dark_mode", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Views
        val logo = findViewById<android.widget.ImageView>(R.id.ivLogo)
        val centerContent = findViewById<LinearLayout>(R.id.centerContent)
        val bottomSection = findViewById<LinearLayout>(R.id.bottomSection)

        // Logo bounce animation
        logo.alpha = 0f
        logo.scaleX = 0.5f
        logo.scaleY = 0.5f
        logo.animate()
            .alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(700)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()

        // Fade in text
        centerContent.alpha = 0f
        centerContent.animate().alpha(1f).setDuration(900).setStartDelay(400).start()

        // Fade in bottom
        bottomSection.alpha = 0f
        bottomSection.animate().alpha(1f).setDuration(600).setStartDelay(800).start()

        Handler(Looper.getMainLooper()).postDelayed({
            when {
                SessionManager.isLoggedIn(this) -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                }
                !prefs.getBoolean("onboarding_seen", false) -> {
                    startActivity(Intent(this, OnboardingActivity::class.java))
                }
                else -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
            finish()
        }, 2500)
    }
}