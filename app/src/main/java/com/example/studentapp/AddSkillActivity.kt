package com.example.studentapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class AddSkillActivity : AppCompatActivity() {

    val baseUrl =  AppConfig.BASE_URL
    val categoryIds = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_skill)

        val etTitle = findViewById<EditText>(R.id.etSkillTitle)
        val etDesc = findViewById<EditText>(R.id.etSkillDesc)
        val spinnerExp = findViewById<Spinner>(R.id.spinnerExperience)
        val spinnerCat = findViewById<Spinner>(R.id.spinnerCategory)
        val btnAdd = findViewById<Button>(R.id.btnAddSkill)

        findViewById<TextView>(R.id.tvBack).setOnClickListener { finish() }

        // Experience levels
        val expLevels = listOf("Beginner", "Intermediate", "Advanced", "Expert")
        spinnerExp.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, expLevels)

        // Categories load karo
        loadCategories(spinnerCat)

        btnAdd.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val exp = spinnerExp.selectedItem.toString()
            val catPosition = spinnerCat.selectedItemPosition

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter skill title!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (categoryIds.isEmpty()) {
                Toast.makeText(this, "Categories loading...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val catId = categoryIds[catPosition]
            addSkill(title, desc, exp, catId)
        }
    }

    fun loadCategories(spinner: Spinner) {
        val url = "http://10.18.118.134 /skillapi/get_categories.php"
        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                val names = ArrayList<String>()
                categoryIds.clear()
                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    names.add(obj.getString("category_name"))
                    categoryIds.add(obj.getInt("id"))
                }
                spinner.adapter = ArrayAdapter(this,
                    android.R.layout.simple_spinner_dropdown_item, names)
            },
            { error ->
                Toast.makeText(this, "Error loading categories!", Toast.LENGTH_SHORT).show()
            }
        )
        AppConfig.getQueue(this).add(request)
    }

    fun addSkill(title: String, desc: String, exp: String, catId: Int) {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "add_skill.php",
            { response ->
                if (response.trim() == "success") {
                    Toast.makeText(this, "Skill added successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to add skill!", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = SessionManager.getUserId(this@AddSkillActivity)
                params["title"] = title
                params["description"] = desc
                params["experience"] = exp
                params["category_id"] = catId.toString()
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }
}