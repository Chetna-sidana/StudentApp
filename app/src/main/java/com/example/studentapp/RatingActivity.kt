package com.example.studentapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class RatingActivity : AppCompatActivity() {

    val myId by lazy { SessionManager.getUserId(this) }
    val baseUrl =  AppConfig.BASE_URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)

        val ratedId = intent.getIntExtra("rated_id", 0)
        val ratedName = intent.getStringExtra("rated_name") ?: ""

        val tvRateName = findViewById<TextView>(R.id.tvRateName)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val etReview = findViewById<EditText>(R.id.etReview)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitRating)
        val tvBack = findViewById<TextView>(R.id.tvBack)

        tvRateName.text = ratedName
        tvBack.setOnClickListener { finish() }

        btnSubmit.setOnClickListener {
            val stars = ratingBar.rating.toInt()
            val review = etReview.text.toString().trim()

            if (stars == 0) {
                Toast.makeText(this, "Please select stars!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitRating(ratedId, stars, review)
        }
    }

    fun submitRating(ratedId: Int, stars: Int, review: String) {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "add_rating.php",
            { response ->
                when (response.trim()) {
                    "success" -> {
                        Toast.makeText(this, "Rating submitted successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    "already_rated" -> {
                        Toast.makeText(this, "You have already rated this user!", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this, "Something went wrong. Please try again!", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["rater_id"] = myId
                params["rated_id"] = ratedId.toString()
                params["stars"] = stars.toString()
                params["review"] = review
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }
}