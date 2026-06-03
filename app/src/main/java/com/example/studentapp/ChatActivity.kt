package com.example.studentapp

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import org.json.JSONArray
import java.util.Timer
import java.util.TimerTask

data class ChatMessage(
    val senderId: Int,
    val senderName: String,
    val message: String,
    val time: String,
    val isDelivered: Int = 0  // 0 = sent (1 tick), 1 = delivered (2 tick)
)

class ChatAdapter(
    private var list: List<ChatMessage>,
    private val myId: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val SENT = 1
    val RECEIVED = 2

    override fun getItemViewType(position: Int) =
        if (list[position].senderId == myId) SENT else RECEIVED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == SENT) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            SentVH(v)
        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            ReceivedVH(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        if (holder is SentVH) {
            holder.tvMessage.text = item.message
            holder.tvTime.text = item.time.takeLast(8).take(5)
            // Tick logic — 1 tick = sent, 2 tick = delivered
            holder.tvTick.text = if (item.isDelivered == 1) "✓✓" else "✓"
            holder.tvTick.setTextColor(
                if (item.isDelivered == 1) 0xFF69F0AE.toInt() else 0xFFCCBBFF.toInt()
            )
        } else if (holder is ReceivedVH) {
            holder.tvMessage.text = item.message
            holder.tvSenderName.text = item.senderName
            holder.tvTime.text = item.time.takeLast(8).take(5)
        }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<ChatMessage>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class SentVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvTick: TextView = view.findViewById(R.id.tvTick)
    }

    inner class ReceivedVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val tvSenderName: TextView = view.findViewById(R.id.tvSenderName)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }
}

class ChatActivity : AppCompatActivity() {

    val myId: Int get() = SessionManager.getUserId(this@ChatActivity).toInt()
    var otherId = 0
    val baseUrl = AppConfig.BASE_URL
    lateinit var adapter: ChatAdapter
    lateinit var rvMessages: RecyclerView
    lateinit var emptyChatState: LinearLayout
    lateinit var tvOnlineStatus: TextView
    lateinit var tvChatAvatar: TextView
    var timer: Timer? = null
    var statusTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        otherId = intent.getIntExtra("other_id", 0)
        val otherName = intent.getStringExtra("other_name") ?: "Chat"

        rvMessages = findViewById(R.id.rvMessages)
        emptyChatState = findViewById(R.id.emptyChatState)
        tvOnlineStatus = findViewById(R.id.tvOnlineStatus)
        tvChatAvatar = findViewById(R.id.tvChatAvatar)

        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<TextView>(R.id.btnSend)
        val tvChatName = findViewById<TextView>(R.id.tvChatName)
        val tvBack = findViewById<TextView>(R.id.tvBack)

        tvChatName.text = otherName
        tvChatAvatar.text = otherName.first().uppercaseChar().toString()
        tvBack.setOnClickListener { finish() }

        adapter = ChatAdapter(emptyList(), myId)
        rvMessages.layoutManager = LinearLayoutManager(this).also {
            it.stackFromEnd = true
        }
        rvMessages.adapter = adapter

        loadMessages()
        updateMyLastSeen()
        loadOtherStatus()

        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() { runOnUiThread { loadMessages() } }
        }, 5000, 5000)

        statusTimer = Timer()
        statusTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    updateMyLastSeen()
                    loadOtherStatus()
                }
            }
        }, 15000, 15000)

        btnSend.setOnClickListener {
            val msg = etMessage.text.toString().trim()
            if (msg.isEmpty()) return@setOnClickListener
            sendMessage(msg)
            etMessage.setText("")
        }
    }

    fun updateMyLastSeen() {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "update_last_seen.php",
            { }, { }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = myId.toString()
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun loadOtherStatus() {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "get_user_status.php",
            { response ->
                try {
                    val obj = org.json.JSONObject(response)
                    val isOnline = obj.getBoolean("online")
                    val lastSeen = obj.optString("last_seen", "")

                    tvOnlineStatus.visibility = View.VISIBLE
                    if (isOnline) {
                        tvOnlineStatus.text = "● Online"
                        tvOnlineStatus.setTextColor(0xFF69F0AE.toInt())
                    } else {
                        if (lastSeen.isNotEmpty() && lastSeen != "null") {
                            val timeOnly = lastSeen.takeLast(8).take(5)
                            tvOnlineStatus.text = "● Last seen $timeOnly"
                        } else {
                            tvOnlineStatus.text = "● Offline"
                        }
                        tvOnlineStatus.setTextColor(0xFFAAAAAA.toInt())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error -> error.printStackTrace() }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = otherId.toString()
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun loadMessages() {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "get_messages.php",
            { response ->
                try {
                    val arr = JSONArray(response)
                    val list = ArrayList<ChatMessage>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(ChatMessage(
                            senderId = obj.getInt("sender_id"),
                            senderName = obj.getString("sender_name"),
                            message = obj.getString("message"),
                            time = obj.getString("created_at"),
                            isDelivered = obj.optInt("is_delivered", 0)
                        ))
                    }
                    adapter.updateList(list)

                    if (list.isEmpty()) {
                        rvMessages.visibility = View.GONE
                        emptyChatState.visibility = View.VISIBLE
                    } else {
                        rvMessages.visibility = View.VISIBLE
                        emptyChatState.visibility = View.GONE
                        rvMessages.scrollToPosition(list.size - 1)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error -> error.printStackTrace() }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = myId.toString()
                params["other_id"] = otherId.toString()
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun sendMessage(msg: String) {
        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "send_message.php",
            { loadMessages() },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["sender_id"] = myId.toString()
                params["receiver_id"] = otherId.toString()
                params["message"] = msg
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        statusTimer?.cancel()
    }
}