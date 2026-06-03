package com.example.studentapp

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
import java.text.SimpleDateFormat
import java.util.Locale

data class NotifItem(
    val message: String,
    val time: String,
    val isRead: Int
)

class NotifAdapter(private var list: List<NotifItem>) :
    RecyclerView.Adapter<NotifAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvDot: View = view.findViewById(R.id.tvDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return VH(v)
    }

    fun convertTo12Hr(dateTimeStr: String): String {
        return try {
            val clean = dateTimeStr
                .removePrefix("Date & Time: ")
                .removePrefix("Time: ")
                .trim()
            val inputFormat = SimpleDateFormat("yyyy-M-d HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(clean)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateTimeStr
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        val parts = item.message.split(" | ")

        val tvTopic = holder.itemView.findViewById<TextView>(R.id.tvTopic)
        val tvDateTime = holder.itemView.findViewById<TextView>(R.id.tvDateTime)
        val tvJoinLink = holder.itemView.findViewById<TextView>(R.id.tvJoinLink)

        if (parts.size >= 3) {
            holder.tvMessage.text = parts[0]
            tvTopic.text = "📌 " + parts[1]
            tvTopic.visibility = View.VISIBLE

            // 12hr format mein convert karo
            val timeStr = parts[2].removePrefix("Date & Time: ").removePrefix("Time: ").trim()
            tvDateTime.text = "📅 " + convertTo12Hr(timeStr)
            tvDateTime.visibility = View.VISIBLE

            // Link extract karo — Join: ya Link: dono handle karo
            val rawPart = parts.getOrNull(3) ?: ""
            val link = rawPart
                .removePrefix("Join: ")
                .removePrefix("Link: ")
                .trim()

            tvJoinLink.visibility = View.VISIBLE
            holder.itemView.setOnClickListener {
                if (link.isNotEmpty() && link.startsWith("http")) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    holder.itemView.context.startActivity(intent)
                } else {
                    Toast.makeText(
                        holder.itemView.context,
                        "Session link not available",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            holder.tvMessage.text = item.message
            tvTopic.visibility = View.GONE
            tvDateTime.visibility = View.GONE
            tvJoinLink.visibility = View.GONE
            holder.itemView.setOnClickListener(null)
        }

        // created_at time bhi 12hr mein
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(item.time)
            holder.tvTime.text = outputFormat.format(date!!)
        } catch (e: Exception) {
            holder.tvTime.text = item.time
        }

        holder.tvDot.visibility = if (item.isRead == 0) View.VISIBLE else View.INVISIBLE
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<NotifItem>) {
        list = newList
        notifyDataSetChanged()
    }
}

class NotificationsActivity : AppCompatActivity() {

    val userId: String get() = SessionManager.getUserId(this@NotificationsActivity)
    val baseUrl = AppConfig.BASE_URL
    lateinit var adapter: NotifAdapter
    lateinit var rvNotifications: RecyclerView
    lateinit var emptyState: LinearLayout
    lateinit var tvMarkRead: TextView
    lateinit var tvBack: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        rvNotifications = findViewById(R.id.rvNotifications)
        emptyState = findViewById(R.id.emptyState)
        tvBack = findViewById(R.id.tvBack)
        tvMarkRead = findViewById(R.id.tvMarkRead)

        adapter = NotifAdapter(emptyList())
        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter

        tvBack.setOnClickListener { finish() }

        tvMarkRead.setOnClickListener {
            markAllRead()
            Toast.makeText(this, "All notifications marked as read!", Toast.LENGTH_SHORT).show()
        }

        loadNotifications()
        markAllRead()
    }

    fun loadNotifications() {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "get_notifications.php",
            { response ->
                try {
                    val arr = JSONArray(response)
                    val list = ArrayList<NotifItem>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(NotifItem(
                            message = obj.getString("message"),
                            time = obj.getString("created_at"),
                            isRead = obj.getInt("is_read")
                        ))
                    }
                    adapter.updateList(list)

                    if (list.isEmpty()) {
                        rvNotifications.visibility = View.GONE
                        emptyState.visibility = View.VISIBLE
                    } else {
                        rvNotifications.visibility = View.VISIBLE
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
                params["user_id"] = userId
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun markAllRead() {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "mark_read.php",
            { }, { }
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