package com.example.studentapp

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

class MatchesActivity : AppCompatActivity() {

    val baseUrl =  AppConfig.BASE_URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matches)

        val rvMatches = findViewById<RecyclerView>(R.id.rvMatches)
        val tvBack = findViewById<TextView>(R.id.tvBack)
        val emptyState = findViewById<LinearLayout>(R.id.emptyState)

        tvBack.setOnClickListener { finish() }

        val adapter = SkillAdapter(emptyList())
        rvMatches.layoutManager = LinearLayoutManager(this)
        rvMatches.adapter = adapter

        loadMatches(adapter, rvMatches, emptyState)
    }

    fun loadMatches(adapter: SkillAdapter, rvMatches: RecyclerView, emptyState: LinearLayout) {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "get_matches.php",
            { response ->
                try {
                    val arr = JSONArray(response)
                    val list = ArrayList<SkillUser>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(SkillUser(
                            id = obj.getInt("id"),
                            name = obj.getString("name"),
                            skillTeach = obj.getString("skill_teach"),
                            skillLearn = obj.getString("skill_learn"),
                            location = obj.getString("location")
                        ))
                    }
                    adapter.updateList(list)

                    // Empty State check
                    if (list.isEmpty()) {
                        rvMatches.visibility = View.GONE
                        emptyState.visibility = View.VISIBLE
                    } else {
                        rvMatches.visibility = View.VISIBLE
                        emptyState.visibility = View.GONE
                    }

                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = SessionManager.getUserId(this@MatchesActivity)
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }
}