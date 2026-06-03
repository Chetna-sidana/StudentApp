package com.example.studentapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

data class OnboardingItem(
    val emoji: String,
    val title: String,
    val desc: String
)

class OnboardingAdapter(private val items: List<OnboardingItem>) :
    RecyclerView.Adapter<OnboardingAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView = view.findViewById(R.id.tvEmoji)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.tvEmoji.text = items[position].emoji
        holder.tvTitle.text = items[position].title
        holder.tvDesc.text = items[position].desc
    }

    override fun getItemCount() = items.size
}

class OnboardingActivity : AppCompatActivity() {

    lateinit var viewPager: ViewPager2
    lateinit var btnNext: TextView
    lateinit var tvSkip: TextView
    lateinit var dotsLayout: LinearLayout

    val items = listOf(
        OnboardingItem(
            emoji = "🌍",
            title = "Welcome to Global Skill Exchange",
            desc = "A platform where you can share your skills\nand learn new ones for free!"
        ),
        OnboardingItem(
            emoji = "🔄",
            title = "How It Works",
            desc = "Teach what you know.\nLearn what you want.\nConnect with people who match your skills!"
        ),
        OnboardingItem(
            emoji = "🚀",
            title = "Ready to Get Started?",
            desc = "Join thousands of learners and teachers.\nCreate your profile and start exchanging skills today!"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        tvSkip = findViewById(R.id.tvSkip)
        dotsLayout = findViewById(R.id.dotsLayout)

        viewPager.adapter = OnboardingAdapter(items)

        setupDots(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setupDots(position)
                if (position == items.size - 1) {
                    btnNext.text = "Get Started"
                } else {
                    btnNext.text = "Next"
                }
            }
        })

        btnNext.setOnClickListener {
            if (viewPager.currentItem < items.size - 1) {
                viewPager.currentItem += 1
            } else {
                goToLogin()
            }
        }

        tvSkip.setOnClickListener {
            goToLogin()
        }
    }

    fun setupDots(position: Int) {
        dotsLayout.removeAllViews()
        for (i in items.indices) {
            val dot = View(this)
            val size = if (i == position) 28 else 16
            val params = LinearLayout.LayoutParams(
                (size * resources.displayMetrics.density).toInt(),
                (8 * resources.displayMetrics.density).toInt()
            )
            params.setMargins(6, 0, 6, 0)
            dot.layoutParams = params

            val bg = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 20f
                setColor(if (i == position) 0xFF5C35C9.toInt() else 0xFFCCCCCC.toInt())
            }
            dot.background = bg
            dotsLayout.addView(dot)
        }
    }

    fun goToLogin() {
        // Mark onboarding as seen
        getSharedPreferences("GSE_PREFS", Context.MODE_PRIVATE)
            .edit().putBoolean("onboarding_seen", true).apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}