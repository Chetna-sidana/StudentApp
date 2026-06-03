package com.example.studentapp

import android.content.Intent
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

data class ChatListItem(
    val userId: Int,
    val name: String,
    val lastMessage: String,
    val lastTime: String
)

class ChatListAdapter(
    private var list: List<ChatListItem>,
    private val onClick: (ChatListItem) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatar: TextView = view.findViewById(R.id.tvChatAvatar)
        val tvName: TextView = view.findViewById(R.id.tvChatName)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = view.findViewById(R.id.tvChatTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_list, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.tvAvatar.text = item.name.first().uppercaseChar().toString()
        holder.tvName.text = item.name
        holder.tvLastMessage.text = item.lastMessage
        holder.tvTime.text = item.lastTime.takeLast(8).take(5)
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<ChatListItem>) {
        list = newList
        notifyDataSetChanged()
    }
}

class ChatListActivity : AppCompatActivity() {

    val baseUrl = AppConfig.BASE_URL
    lateinit var adapter: ChatListAdapter
    lateinit var emptyState: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        val rvChatList = findViewById<RecyclerView>(R.id.rvChatList)
        emptyState = findViewById(R.id.emptyState)
        val tvBack = findViewById<TextView>(R.id.tvBack)
        val tvChatCount = findViewById<TextView>(R.id.tvChatCount)

        tvBack.setOnClickListener { finish() }

        adapter = ChatListAdapter(emptyList()) { item ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("other_id", item.userId)
            intent.putExtra("other_name", item.name)
            startActivity(intent)
        }

        rvChatList.layoutManager = LinearLayoutManager(this)
        rvChatList.adapter = adapter

        loadChatList(tvChatCount)
    }

    fun loadChatList(tvChatCount: TextView) {
        val userId = SessionManager.getUserId(this)

        val request = object : StringRequest(
            Request.Method.POST, baseUrl + "get_chat_list.php",
            { response ->
                try {
                    val arr = JSONArray(response)
                    val list = ArrayList<ChatListItem>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(ChatListItem(
                            userId = obj.getInt("other_user_id"),
                            name = obj.getString("other_user_name"),
                            lastMessage = obj.getString("last_message"),
                            lastTime = obj.getString("last_time")
                        ))
                    }
                    adapter.updateList(list)
                    tvChatCount.text = "${list.size} conversations"

                    if (list.isEmpty()) {
                        rvChatList.visibility = View.GONE
                        emptyState.visibility = View.VISIBLE
                    } else {
                        rvChatList.visibility = View.VISIBLE
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

    // rvChatList reference for empty state toggle
    private val rvChatList get() = findViewById<RecyclerView>(R.id.rvChatList)
}