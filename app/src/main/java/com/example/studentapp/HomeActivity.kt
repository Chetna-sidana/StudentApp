package com.example.studentapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONException

data class SkillUser(
    val id: Int,
    val name: String,
    val skillTeach: String,
    val skillLearn: String,
    val location: String,
    val experience: String = "",
    val description: String = ""
)

class SkillAdapter(private var list: List<SkillUser>) :
    RecyclerView.Adapter<SkillAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvTeach: TextView = view.findViewById(R.id.tvTeach)
        val tvLearn: TextView = view.findViewById(R.id.tvLearn)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvAvatar: TextView = view.findViewById(R.id.tvAvatar)
        val tvExperience: TextView = view.findViewById(R.id.tvExperience)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_skill, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.tvName.text = item.name
        holder.tvTeach.text = item.skillTeach
        holder.tvLearn.text = item.skillLearn
        holder.tvLocation.text = item.location
        holder.tvAvatar.text = item.name.first().uppercaseChar().toString()
        holder.tvExperience.text = item.experience
        holder.tvDescription.text = item.description

        if (item.description.isEmpty()) {
            holder.tvDescription.visibility = View.GONE
        } else {
            holder.tvDescription.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, UserDetailActivity::class.java)
            intent.putExtra("receiver_id", item.id)
            intent.putExtra("name", item.name)
            intent.putExtra("skill_teach", item.skillTeach)
            intent.putExtra("skill_learn", item.skillLearn)
            intent.putExtra("location", item.location)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<SkillUser>) {
        list = newList
        notifyDataSetChanged()
    }
}

class HomeActivity : AppCompatActivity() {

    lateinit var rvSkills: RecyclerView
    lateinit var etSearch: EditText
    lateinit var adapter: SkillAdapter
    lateinit var bottomNav: BottomNavigationView
    lateinit var filterLayout: LinearLayout
    lateinit var emptyState: LinearLayout
    lateinit var swipeRefresh: SwipeRefreshLayout
    lateinit var shimmerLayout: ShimmerFrameLayout

    val allSkills = ArrayList<SkillUser>()
    var selectedCategoryId = ""
    val categoryIds = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        rvSkills = findViewById(R.id.rvSkills)
        etSearch = findViewById(R.id.etSearch)
        bottomNav = findViewById(R.id.bottomNav)
        filterLayout = findViewById(R.id.filterLayout)
        emptyState = findViewById(R.id.emptyState)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        shimmerLayout = findViewById(R.id.shimmerLayout)

        swipeRefresh.setColorSchemeColors(
            0xFF5C35C9.toInt(),
            0xFF7B5CE0.toInt(),
            0xFFE91E63.toInt()
        )

        swipeRefresh.setOnRefreshListener {
            fetchSkills(selectedCategoryId)
            updateStats()
        }

        adapter = SkillAdapter(allSkills)
        rvSkills.layoutManager = LinearLayoutManager(this)
        rvSkills.adapter = adapter

        rvSkills.layoutAnimation = android.view.animation.AnimationUtils
            .loadLayoutAnimation(this, R.anim.layout_animation)

        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        tvUserName.text = SessionManager.getUserName(this)

        findViewById<TextView>(R.id.tvNotifBell).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        loadCategories()
        fetchSkills("")
        updateStats()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterSkills(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_matches -> {
                    startActivity(Intent(this, MatchesActivity::class.java))
                    true
                }
                R.id.nav_requests -> {
                    startActivity(Intent(this, MyRequestsActivity::class.java))
                    true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        bottomNav.selectedItemId = R.id.nav_home
    }

    override fun onResume() {
        super.onResume()
        loadUnreadBadge()
        updateLastSeen()
        fetchSkills(selectedCategoryId)
        updateStats()
    }

    fun loadUnreadBadge() {
        val userId = SessionManager.getUserId(this)
        val request = object : com.android.volley.toolbox.StringRequest(
            Request.Method.POST,
            AppConfig.BASE_URL + "get_unread_count.php",
            { response ->
                try {
                    val obj = org.json.JSONObject(response)
                    val count = obj.getInt("count")
                    val badge = bottomNav.getOrCreateBadge(R.id.nav_notifications)
                    if (count > 0) {
                        badge.isVisible = true
                        badge.number = count
                        badge.backgroundColor = Color.RED
                        badge.badgeTextColor = Color.WHITE
                    } else {
                        badge.isVisible = false
                    }
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
                params["user_id"] = userId
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun updateStats() {
        val tvSkillCount = findViewById<TextView>(R.id.tvSkillCount)
        val tvUserCount = findViewById<TextView>(R.id.tvUserCount)
        val tvCategoryCount = findViewById<TextView>(R.id.tvCategoryCount)

        val request = object : com.android.volley.toolbox.StringRequest(
            Request.Method.GET,
            AppConfig.BASE_URL + "get_stats.php",
            { response ->
                try {
                    val obj = org.json.JSONObject(response)
                    tvSkillCount.text = obj.getString("skills")
                    tvUserCount.text = obj.getString("users")
                    tvCategoryCount.text = obj.getString("categories")
                } catch (e: Exception) { e.printStackTrace() }
            },
            { error -> error.printStackTrace() }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["ngrok-skip-browser-warning"] = "true"
                headers["User-Agent"] = "SkillApp/1.0"
                return headers
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun updateLastSeen() {
        val request = object : com.android.volley.toolbox.StringRequest(
            Request.Method.POST,
            AppConfig.BASE_URL + "update_last_seen.php",
            { }, { }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["ngrok-skip-browser-warning"] = "true"
                headers["User-Agent"] = "SkillApp/1.0"
                return headers
            }
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = SessionManager.getUserId(this@HomeActivity)
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun loadCategories() {
        val request = object : JsonArrayRequest(
            Request.Method.GET,
            AppConfig.BASE_URL + "get_categories.php",
            null,
            { response ->
                filterLayout.removeAllViews()
                categoryIds.clear()
                addFilterButton("All", -1, true)
                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    val id = obj.getInt("id")
                    val name = obj.getString("category_name")
                    categoryIds.add(id)
                    addFilterButton(name, id, false)
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
        }
        AppConfig.getQueue(this).add(request)
    }

    fun addFilterButton(name: String, id: Int, isSelected: Boolean) {
        val btn = TextView(this)
        btn.text = name
        btn.textSize = 13f
        btn.setPadding(32, 16, 32, 16)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(6, 0, 6, 0)
        btn.layoutParams = params

        val selectedBg = android.graphics.drawable.GradientDrawable().apply {
            setColor(0xFF5C35C9.toInt())
            cornerRadius = 50f
        }
        val normalBg = android.graphics.drawable.GradientDrawable().apply {
            setColor(0xFFEDE8FF.toInt())
            cornerRadius = 50f
        }

        if (isSelected) {
            btn.background = selectedBg
            btn.setTextColor(android.graphics.Color.WHITE)
        } else {
            btn.background = normalBg
            btn.setTextColor(0xFF5C35C9.toInt())
        }

        btn.setOnClickListener {
            for (i in 0 until filterLayout.childCount) {
                val child = filterLayout.getChildAt(i) as TextView
                val bg = android.graphics.drawable.GradientDrawable().apply {
                    setColor(0xFFEDE8FF.toInt())
                    cornerRadius = 50f
                }
                child.background = bg
                child.setTextColor(0xFF5C35C9.toInt())
            }
            val activeBg = android.graphics.drawable.GradientDrawable().apply {
                setColor(0xFF5C35C9.toInt())
                cornerRadius = 50f
            }
            btn.background = activeBg
            btn.setTextColor(android.graphics.Color.WHITE)
            selectedCategoryId = if (id == -1) "" else id.toString()
            fetchSkills(selectedCategoryId)
        }

        filterLayout.addView(btn)
    }

    fun fetchSkills(categoryId: String) {
        if (!swipeRefresh.isRefreshing) {
            shimmerLayout.visibility = View.VISIBLE
            shimmerLayout.startShimmer()
            swipeRefresh.visibility = View.GONE
        }
        emptyState.visibility = View.GONE

        val request = object : com.android.volley.toolbox.StringRequest(
            Request.Method.POST,
            AppConfig.BASE_URL + "get_all_skills.php",
            { response ->
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
                swipeRefresh.isRefreshing = false

                try {
                    val arr = org.json.JSONArray(response)
                    allSkills.clear()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        allSkills.add(
                            SkillUser(
                                id = obj.getInt("user_id"),
                                name = obj.getString("user_name"),
                                skillTeach = obj.getString("title"),
                                skillLearn = obj.getString("category_name"),
                                location = obj.getString("location"),
                                experience = obj.optString("experience", ""),
                                description = obj.optString("description", "")
                            )
                        )
                    }
                    adapter.updateList(allSkills.toList())

                    if (allSkills.isEmpty()) {
                        swipeRefresh.visibility = View.GONE
                        emptyState.visibility = View.VISIBLE
                    } else {
                        swipeRefresh.visibility = View.VISIBLE
                        emptyState.visibility = View.GONE
                        rvSkills.scheduleLayoutAnimation()
                    }

                } catch (e: JSONException) { e.printStackTrace() }
            },
            { error ->
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
                swipeRefresh.visibility = View.VISIBLE
                swipeRefresh.isRefreshing = false
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
                params["category_id"] = categoryId
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun filterSkills(query: String) {
        val filtered = allSkills.filter {
            it.name.lowercase().contains(query.lowercase()) ||
                    it.skillTeach.lowercase().contains(query.lowercase()) ||
                    it.skillLearn.lowercase().contains(query.lowercase())
        }
        adapter.updateList(filtered)

        if (filtered.isEmpty()) {
            rvSkills.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            rvSkills.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }
}