package com.example.studentapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject

class UserDetailActivity : AppCompatActivity() {

    val baseUrl =  AppConfig.BASE_URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)

        val receiverId = intent.getIntExtra("receiver_id", 0)
        val name = intent.getStringExtra("name") ?: ""
        val teach = intent.getStringExtra("skill_teach") ?: ""
        val learn = intent.getStringExtra("skill_learn") ?: ""
        val location = intent.getStringExtra("location") ?: ""

        findViewById<TextView>(R.id.tvDetailName).text = name
        findViewById<TextView>(R.id.tvDetailLocation).text = "📍 $location"
        findViewById<TextView>(R.id.tvDetailTeach).text = teach
        findViewById<TextView>(R.id.tvDetailLearn).text = learn
        findViewById<TextView>(R.id.tvDetailAvatar).text =
            name.first().uppercaseChar().toString()

        findViewById<TextView>(R.id.tvBack).setOnClickListener { finish() }

        loadUserRating(receiverId)
        loadUserStats(receiverId)

        // Swap Request
        findViewById<Button>(R.id.btnSendSwap).setOnClickListener {
            sendSwapRequest(
                senderId = SessionManager.getUserId(this).toInt(),
                receiverId = receiverId
            )
        }

        // Chat
        findViewById<Button>(R.id.btnChat).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("other_id", receiverId)
            intent.putExtra("other_name", name)
            startActivity(intent)
        }

        // Rate
        findViewById<Button>(R.id.btnRate).setOnClickListener {
            val intent = Intent(this, RatingActivity::class.java)
            intent.putExtra("rated_id", receiverId)
            intent.putExtra("rated_name", name)
            startActivity(intent)
        }

        // Session - receiverId aur name pass ho raha hai ab
        findViewById<Button>(R.id.btnSession).setOnClickListener {
            val intent = Intent(this, SessionsActivity::class.java)
            intent.putExtra("other_user_id", receiverId)
            intent.putExtra("other_user_name", name)
            startActivity(intent)
        }

        // Share Profile
        findViewById<Button>(R.id.btnShare).setOnClickListener {
            val shareText = "Check out $name on Global Skill Exchange!\n" +
                    "🎓 Teaches: $teach\n" +
                    "📚 Wants to learn: $learn\n" +
                    "📍 Location: $location\n\n" +
                    "Download the app to connect!"

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
            startActivity(Intent.createChooser(shareIntent, "Share Profile via"))
        }
    }

    fun loadUserRating(userId: Int) {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "get_ratings.php",
            { response ->
                try {
                    val obj = JSONObject(response)
                    val avg = obj.getDouble("average").toFloat()
                    val count = obj.getInt("count")

                    findViewById<RatingBar>(R.id.userRatingBar).rating = avg
                    findViewById<TextView>(R.id.tvUserRatingCount).text = "($count ratings)"
                    findViewById<TextView>(R.id.tvRatingAvg).text =
                        String.format("%.1f", avg.toDouble())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error -> error.printStackTrace() }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["ngrok-skip-browser-warning"] = "true"
                headers["User-Agent"] = "SkillApp/1.0"
                return headers
            }
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId.toString()
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun loadUserStats(userId: Int) {
        val skillsRequest = object : StringRequest(
            Request.Method.POST, baseUrl + "get_all_skills.php",
            { response ->
                try {
                    val arr = org.json.JSONArray(response)
                    var count = 0
                    for (i in 0 until arr.length()) {
                        if (arr.getJSONObject(i).getInt("user_id") == userId) count++
                    }
                    findViewById<TextView>(R.id.tvSkillsCount).text = count.toString()
                } catch (e: Exception) { e.printStackTrace() }
            },
            { }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["ngrok-skip-browser-warning"] = "true"
                headers["User-Agent"] = "SkillApp/1.0"
                return headers
            }
            override fun getParams() = hashMapOf("category_id" to "")
        }
        AppConfig.getQueue(this).add(skillsRequest)

        val swapsRequest = object : StringRequest(
            Request.Method.POST, baseUrl + "get_my_requests.php",
            { response ->
                try {
                    val arr = org.json.JSONArray(response)
                    findViewById<TextView>(R.id.tvSwapsCount).text = arr.length().toString()
                } catch (e: Exception) { e.printStackTrace() }
            },
            { }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["ngrok-skip-browser-warning"] = "true"
                headers["User-Agent"] = "SkillApp/1.0"
                return headers
            }
            override fun getParams() = hashMapOf("sender_id" to userId.toString())
        }
        AppConfig.getQueue(this).add(swapsRequest)
    }

    fun sendSwapRequest(senderId: Int, receiverId: Int) {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "send_swap.php",
            { response ->
                when (response.trim()) {
                    "success" -> Toast.makeText(this,
                        "Swap Request Sent! ✅", Toast.LENGTH_SHORT).show()
                    "already_sent" -> Toast.makeText(this,
                        "Request already sent!", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(this,
                        "Something went wrong!", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
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
                params["sender_id"] = senderId.toString()
                params["receiver_id"] = receiverId.toString()
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }
}