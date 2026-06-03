package com.example.studentapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull

data class MySkill(
    val id: Int,
    val title: String,
    val category: String,
    val experience: String,
    val description: String
)

class MySkillAdapter(
    private var list: List<MySkill>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<MySkillAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvMySkillTitle)
        val tvCategory: TextView = view.findViewById(R.id.tvMySkillCategory)
        val tvExp: TextView = view.findViewById(R.id.tvMySkillExp)
        val tvDesc: TextView = view.findViewById(R.id.tvMySkillDesc)
        val tvDelete: TextView = view.findViewById(R.id.tvDeleteSkill)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_skill, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.tvTitle.text = item.title
        holder.tvCategory.text = item.category
        holder.tvExp.text = item.experience
        holder.tvDesc.text = item.description
        holder.tvDelete.setOnClickListener { onDelete(item.id) }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<MySkill>) {
        list = newList
        notifyDataSetChanged()
    }
}

class ProfileActivity : AppCompatActivity() {

    val userId: String get() = SessionManager.getUserId(this@ProfileActivity)
    val baseUrl =  AppConfig.BASE_URL
    val PICK_IMAGE = 100

    lateinit var tvAvatar: TextView
    lateinit var ivAvatar: ImageView
    lateinit var tvName: TextView
    lateinit var tvEmail: TextView
    lateinit var etName: EditText
    lateinit var etLocation: EditText
    lateinit var etSkillTeach: EditText
    lateinit var etSkillLearn: EditText
    lateinit var mySkillAdapter: MySkillAdapter

    var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvAvatar = findViewById(R.id.tvAvatar)
        ivAvatar = findViewById(R.id.ivAvatar)
        tvName = findViewById(R.id.tvProfileName)
        tvEmail = findViewById(R.id.tvProfileEmail)
        etName = findViewById(R.id.etName)
        etLocation = findViewById(R.id.etLocation)
        etSkillTeach = findViewById(R.id.etSkillTeach)
        etSkillLearn = findViewById(R.id.etSkillLearn)

        findViewById<TextView>(R.id.tvBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Photo click - options dialog
        findViewById<TextView>(R.id.tvChangePhoto).setOnClickListener {
            val options = arrayOf("📷 Change Photo", "🗑️ Remove Photo")
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Profile Photo")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            val intent = Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            )
                            startActivityForResult(intent, PICK_IMAGE)
                        }

                        1 -> {
                            // Confirm dialog pehle
                            androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Remove Photo")
                                .setMessage("Are you sure you want to remove your profile photo?")
                                .setPositiveButton("Remove") { _, _ -> removePhoto() }
                                .setNegativeButton("Cancel", null)
                                .show()
                        }
                    }
                }
                .show()
        }

        // My Chats button
        findViewById<TextView>(R.id.tvOpenChats).setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }

        // My Skills RecyclerView
        val rvMySkills = findViewById<RecyclerView>(R.id.rvMySkills)
        mySkillAdapter = MySkillAdapter(emptyList()) { skillId ->
            deleteSkill(skillId)
        }
        rvMySkills.layoutManager = LinearLayoutManager(this)
        rvMySkills.adapter = mySkillAdapter

        findViewById<TextView>(R.id.tvAddNewSkill).setOnClickListener {
            startActivity(Intent(this, AddSkillActivity::class.java))
        }

        loadProfile()
        loadRatings()
        loadMySkills()

        findViewById<Button>(R.id.btnUpdate).setOnClickListener {
            updateProfile()
        }
    }

    override fun onResume() {
        super.onResume()
        loadMySkills()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let {
                tvAvatar.visibility = View.GONE
                ivAvatar.visibility = View.VISIBLE
                Glide.with(this).load(it).circleCrop().into(ivAvatar)
                uploadPhoto(it)
            }
        }
    }

    fun uploadPhoto(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return
            inputStream.close()

            val fileName = "profile_$userId.jpg"

            val client = OkHttpClient()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_id", userId)
                .addFormDataPart(
                    "photo", fileName,
                    RequestBody.create("image/jpeg".toMediaTypeOrNull(), bytes)
                )
                .build()

            val request = okhttp3.Request.Builder()
                .url(baseUrl + "upload_photo.php")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Upload failed: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string() ?: ""
                    runOnUiThread {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Photo uploaded!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun loadProfile() {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "get_profile.php",
            { response ->
                try {
                    val obj = JSONObject(response)
                    val name = obj.getString("name")
                    val email = obj.getString("email")
                    val location = obj.optString("location", "")
                    val teach = obj.optString("skill_teach", "")
                    val learn = obj.optString("skill_learn", "")
                    val photo = obj.optString("profile_photo", "")

                    tvName.text = name
                    tvEmail.text = email
                    tvAvatar.text = name.first().uppercaseChar().toString()
                    etName.setText(name)
                    etLocation.setText(location)
                    etSkillTeach.setText(teach)
                    etSkillLearn.setText(learn)

                    if (photo.isNotEmpty() && photo != "null") {
                        tvAvatar.visibility = View.GONE
                        ivAvatar.visibility = View.VISIBLE
                        // Cache bypass karo taaki removed photo na dikhe
                        Glide.with(this)
                            .load("${baseUrl}${photo}?t=${System.currentTimeMillis()}")
                            .circleCrop()
                            .placeholder(android.R.color.darker_gray)
                            .error(android.R.color.darker_gray)
                            .into(ivAvatar)
                    } else {
                        // Koi photo nahi — letter avatar dikhao
                        ivAvatar.visibility = View.GONE
                        ivAvatar.setImageDrawable(null)
                        tvAvatar.visibility = View.VISIBLE
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
                params["user_id"] = userId
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun loadRatings() {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "get_ratings.php",
            { response ->
                try {
                    val obj = JSONObject(response)
                    val avg = obj.getDouble("average")
                    val count = obj.getInt("count")
                    findViewById<TextView>(R.id.tvAvgRating).text = avg.toString()
                    findViewById<TextView>(R.id.tvRatingCount).text = "$count ratings"
                    findViewById<RatingBar>(R.id.profileRatingBar).rating = avg.toFloat()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error -> error.printStackTrace() }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun loadMySkills() {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "get_user_skills.php",
            { response ->
                try {
                    val arr = JSONArray(response)
                    val list = ArrayList<MySkill>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(
                            MySkill(
                                id = obj.getInt("id"),
                                title = obj.getString("title"),
                                category = obj.optString("category_name", "General"),
                                experience = obj.optString("experience", ""),
                                description = obj.optString("description", "")
                            )
                        )
                    }
                    mySkillAdapter.updateList(list)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error -> error.printStackTrace() }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun deleteSkill(skillId: Int) {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "delete_skill.php",
            { response ->
                if (response.trim() == "success") {
                    Toast.makeText(this, "Skill deleted!", Toast.LENGTH_SHORT).show()
                    loadMySkills()
                }
            },
            { error -> error.printStackTrace() }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["skill_id"] = skillId.toString()
                params["user_id"] = userId
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun updateProfile() {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "update_profile.php",
            { response ->
                if (response.trim() == "success") {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                    loadProfile()
                    loadRatings()
                } else {
                    Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId
                params["name"] = etName.text.toString()
                params["location"] = etLocation.text.toString()
                params["skill_teach"] = etSkillTeach.text.toString()
                params["skill_learn"] = etSkillLearn.text.toString()
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun removePhoto() {
        // Debug toast
        Toast.makeText(this, "UserID: $userId", Toast.LENGTH_LONG).show()

        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "remove_photo.php",
            { response ->
                try {
                    val obj = JSONObject(response)
                    if (obj.getString("status") == "success") {
                        Glide.with(this).clear(ivAvatar)
                        ivAvatar.setImageDrawable(null)
                        ivAvatar.visibility = View.GONE
                        tvAvatar.visibility = View.VISIBLE
                        Toast.makeText(this, "Photo removed!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Remove failed: ${obj.optString("msg")}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "RAW: $response", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Network Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }
}