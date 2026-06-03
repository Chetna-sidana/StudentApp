package com.example.studentapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import org.json.JSONArray

data class SwapRequest(
    val id: Int,
    val name: String,
    val skillTeach: String,
    val skillLearn: String,
    val location: String,
    val status: String,
    val showButtons: Boolean = false
)

class RequestAdapter(
    private var list: MutableList<SwapRequest>,
    private val onAccept: (Int) -> Unit,
    private val onReject: (Int) -> Unit
) : RecyclerView.Adapter<RequestAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvReqName)
        val tvTeach: TextView = view.findViewById(R.id.tvReqTeach)
        val tvLearn: TextView = view.findViewById(R.id.tvReqLearn)
        val tvLocation: TextView = view.findViewById(R.id.tvReqLocation)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val layoutButtons: LinearLayout = view.findViewById(R.id.layoutButtons)
        val btnAccept: TextView = view.findViewById(R.id.btnAccept)
        val btnReject: TextView = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.tvName.text = item.name
        holder.tvTeach.text = item.skillTeach
        holder.tvLearn.text = item.skillLearn
        holder.tvLocation.text = "📍 ${item.location}"
        holder.tvStatus.text = item.status.uppercase()

        holder.itemView.findViewById<TextView>(R.id.tvReqAvatar).text =
            item.name.firstOrNull()?.uppercase() ?: "?"

        val statusBg = android.graphics.drawable.GradientDrawable().apply {
            cornerRadius = 30f
        }
        when (item.status) {
            "pending" -> {
                statusBg.setColor(0xFFFF9800.toInt())
                holder.tvStatus.background = statusBg
                holder.tvStatus.setTextColor(0xFFFFFFFF.toInt())
            }
            "accepted" -> {
                statusBg.setColor(0xFF4CAF50.toInt())
                holder.tvStatus.background = statusBg
                holder.tvStatus.setTextColor(0xFFFFFFFF.toInt())
            }
            "rejected" -> {
                statusBg.setColor(0xFFF44336.toInt())
                holder.tvStatus.background = statusBg
                holder.tvStatus.setTextColor(0xFFFFFFFF.toInt())
            }
        }

        if (item.showButtons && item.status == "pending") {
            holder.layoutButtons.visibility = View.VISIBLE
            holder.btnAccept.setOnClickListener { onAccept(item.id) }
            holder.btnReject.setOnClickListener { onReject(item.id) }
        } else {
            holder.layoutButtons.visibility = View.GONE
        }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<SwapRequest>) {
        list = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun removeItem(requestId: Int) {
        val index = list.indexOfFirst { it.id == requestId }
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun getCurrentList(): MutableList<SwapRequest> = list
}

class MyRequestsActivity : AppCompatActivity() {

    lateinit var adapter: RequestAdapter
    lateinit var rvRequests: RecyclerView
    lateinit var emptyState: LinearLayout
    lateinit var tabSent: TextView
    lateinit var tabReceived: TextView
    var currentTab = "sent"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_requests)

        rvRequests = findViewById(R.id.rvRequests)
        emptyState = findViewById(R.id.emptyState)
        tabSent = findViewById(R.id.tabSent)
        tabReceived = findViewById(R.id.tabReceived)

        val tvBack = findViewById<TextView>(R.id.tvBack)
        tvBack.setOnClickListener { finish() }

        adapter = RequestAdapter(
            mutableListOf(),
            onAccept = { requestId -> updateRequest(requestId, "accepted") },
            onReject = { requestId -> updateRequest(requestId, "rejected") }
        )
        rvRequests.layoutManager = LinearLayoutManager(this)
        rvRequests.adapter = adapter

        setupTabs()
        loadSentRequests()
    }

    fun setupTabs() {
        tabSent.setOnClickListener {
            currentTab = "sent"
            setActiveTab(tabSent, tabReceived)
            loadSentRequests()
        }

        tabReceived.setOnClickListener {
            currentTab = "received"
            setActiveTab(tabReceived, tabSent)
            loadReceivedRequests()
        }

        setActiveTab(tabSent, tabReceived)
    }

    fun setActiveTab(active: TextView, inactive: TextView) {
        val activeBg = android.graphics.drawable.GradientDrawable().apply {
            setColor(0xFF5C35C9.toInt())
            cornerRadius = 30f
        }
        val inactiveBg = android.graphics.drawable.GradientDrawable().apply {
            setColor(0xFFEDE8FF.toInt())
            cornerRadius = 30f
        }
        active.background = activeBg
        active.setTextColor(0xFFFFFFFF.toInt())
        inactive.background = inactiveBg
        inactive.setTextColor(0xFF5C35C9.toInt())
    }

    fun loadSentRequests() {
        val request = object : StringRequest(
            Request.Method.POST,
            AppConfig.BASE_URL + "get_requests.php",
            { response ->
                try {
                    val arr = JSONArray(response)
                    val list = ArrayList<SwapRequest>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(SwapRequest(
                            id = obj.getInt("id"),
                            name = obj.getString("name"),
                            skillTeach = obj.getString("skill_teach"),
                            skillLearn = obj.getString("skill_learn"),
                            location = obj.getString("location"),
                            status = obj.getString("status"),
                            showButtons = false
                        ))
                    }
                    adapter.updateList(list)
                    showEmptyIfNeeded(list)
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
                params["sender_id"] = SessionManager.getUserId(this@MyRequestsActivity)
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun loadReceivedRequests() {
        AppConfig.getQueue(this).cache.clear()
        val request = object : StringRequest(
            Request.Method.POST,
            AppConfig.BASE_URL + "get_received_requests.php",
            { response ->
                android.util.Log.d("RECEIVED_RESPONSE", response)
                try {
                    val arr = JSONArray(response)
                    val list = ArrayList<SwapRequest>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val status = obj.getString("status")
                        if (status == "pending") {
                            list.add(SwapRequest(
                                id = obj.getInt("id"),
                                name = obj.getString("name"),
                                skillTeach = obj.getString("skill_teach"),
                                skillLearn = obj.getString("skill_learn"),
                                location = obj.getString("location"),
                                status = status,
                                showButtons = true
                            ))
                        }
                    }
                    adapter.updateList(list)
                    showEmptyIfNeeded(list)
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
                params["receiver_id"] = SessionManager.getUserId(this@MyRequestsActivity)
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun updateRequest(requestId: Int, status: String) {
        val request = object : StringRequest(
            Request.Method.POST,
            AppConfig.BASE_URL + "update_request.php",
            { response ->
                android.util.Log.d("UPDATE_RESPONSE", response)
                val msg = if (status == "accepted") "Request Accepted! ✅" else "Request Rejected ❌"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

                adapter.removeItem(requestId)
                showEmptyIfNeeded(adapter.getCurrentList())
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["request_id"] = requestId.toString()
                params["status"] = status
                return params
            }
        }
        AppConfig.getQueue(this).add(request)
    }

    fun showEmptyIfNeeded(list: List<SwapRequest>) {
        if (list.isEmpty()) {
            rvRequests.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            rvRequests.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }
}