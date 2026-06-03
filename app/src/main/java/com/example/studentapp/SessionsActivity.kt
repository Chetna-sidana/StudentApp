package com.example.studentapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class Session(
    val id: Int,
    val title: String,
    val userAName: String,
    val userBName: String,
    val scheduledTime: String,
    val meetLink: String,
    val status: String
)

class SessionAdapter(
    private var list: List<Session>,
    private val myId: Int
) : RecyclerView.Adapter<SessionAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvSessionTitle)
        val tvWith: TextView = view.findViewById(R.id.tvSessionWith)
        val tvTime: TextView = view.findViewById(R.id.tvSessionTime)
        val tvStatus: TextView = view.findViewById(R.id.tvSessionStatus)
        val btnJoin: Button = view.findViewById(R.id.btnJoinSession)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.tvTitle.text = item.title
        holder.tvWith.text = "With: ${if (myId.toString() == item.userAName) item.userBName else item.userAName}"

        // 24hr to 12hr format convert karo
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(item.scheduledTime)
            holder.tvTime.text = "📅 ${outputFormat.format(date!!)}"
        } catch (e: Exception) {
            holder.tvTime.text = "📅 ${item.scheduledTime}"
        }

        holder.tvStatus.text = item.status.uppercase()

        when (item.status) {
            "pending" -> holder.tvStatus.setBackgroundColor(0xFFFF9800.toInt())
            "completed" -> holder.tvStatus.setBackgroundColor(0xFF4CAF50.toInt())
            else -> holder.tvStatus.setBackgroundColor(0xFF5C35C9.toInt())
        }

        holder.btnJoin.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.meetLink))
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<Session>) {
        list = newList
        notifyDataSetChanged()
    }
}

class SessionsActivity : AppCompatActivity() {

    val baseUrl = AppConfig.BASE_URL
    lateinit var adapter: SessionAdapter
    var selectedDateTime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sessions)

        val rvSessions = findViewById<RecyclerView>(R.id.rvSessions)
        val tvBack = findViewById<TextView>(R.id.tvBack)
        val tvNewSession = findViewById<TextView>(R.id.tvNewSession)

        val otherUserId = intent.getIntExtra("other_user_id", 0)
        val otherUserName = intent.getStringExtra("other_user_name") ?: ""

        val myId = SessionManager.getUserId(this).toInt()
        adapter = SessionAdapter(emptyList(), myId)
        rvSessions.layoutManager = LinearLayoutManager(this)
        rvSessions.adapter = adapter

        tvBack.setOnClickListener { finish() }

        tvNewSession.setOnClickListener {
            showNewSessionDialog(otherUserId, otherUserName)
        }

        if (otherUserId != 0) {
            showNewSessionDialog(otherUserId, otherUserName)
        }

        loadSessions()
    }

    fun loadSessions() {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "get_sessions.php",
            { response ->
                try {
                    val arr = JSONArray(response)
                    val list = ArrayList<Session>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(Session(
                            id = obj.getInt("id"),
                            title = obj.getString("title"),
                            userAName = obj.getString("user_a_name"),
                            userBName = obj.getString("user_b_name"),
                            scheduledTime = obj.getString("scheduled_time"),
                            meetLink = obj.getString("meet_link"),
                            status = obj.getString("status")
                        ))
                    }
                    adapter.updateList(list)

                    if (list.isEmpty()) {
                        Toast.makeText(this,
                            "No sessions yet! Create one with + New",
                            Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
                params["user_id"] = SessionManager.getUserId(this@SessionsActivity)
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun showNewSessionDialog(otherUserId: Int = 0, otherUserName: String = "") {
        val dialogView = layoutInflater.inflate(R.layout.dialog_new_session, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etSessionTitle)
        val etUserId = dialogView.findViewById<EditText>(R.id.etOtherUserId)
        val tvWithUser = dialogView.findViewById<TextView>(R.id.tvWithUser)
        val tvDateTime = dialogView.findViewById<TextView>(R.id.tvSelectedDateTime)
        val btnPickDateTime = dialogView.findViewById<Button>(R.id.btnPickDateTime)

        if (otherUserId != 0) {
            etUserId.setText(otherUserId.toString())
            tvWithUser.text = "👤 With: $otherUserName"
        } else {
            tvWithUser.text = "👤 With: Unknown User"
        }

        btnPickDateTime.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                TimePickerDialog(this, { _, h, min ->
                    selectedDateTime = "$y-${m+1}-$d $h:$min:00"
                    // 12hr format mein dikhao
                    val amPm = if (h < 12) "AM" else "PM"
                    val hour12 = if (h == 0) 12 else if (h > 12) h - 12 else h
                    tvDateTime.text = "$d/${m+1}/$y at $hour12:${min.toString().padStart(2,'0')} $amPm"
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        AlertDialog.Builder(this)
            .setTitle("📅 Schedule Session")
            .setView(dialogView)
            .setPositiveButton("Create Session") { _, _ ->
                val title = etTitle.text.toString().trim()
                val userId = etUserId.text.toString().trim()

                if (title.isEmpty() || userId.isEmpty() || selectedDateTime.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                createSession(title, userId, selectedDateTime)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun createSession(title: String, otherUserId: String, dateTime: String) {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "create_session.php",
            { response ->
                try {
                    val obj = JSONObject(response)
                    if (obj.getString("status") == "success") {
                        val link = obj.getString("meet_link")
                        AlertDialog.Builder(this)
                            .setTitle("✅ Session Created!")
                            .setMessage("Meet Link:\n$link\n\nDono log is link se join kar sakte hain!")
                            .setPositiveButton("Join Now") { _, _ ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                startActivity(intent)
                            }
                            .setNegativeButton("Later") { _, _ ->
                                loadSessions()
                            }
                            .show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
                params["user_a_id"] = SessionManager.getUserId(this@SessionsActivity)
                params["user_b_id"] = otherUserId
                params["title"] = title
                params["scheduled_time"] = dateTime
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }
}