package com.example.studentapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton

class StudentListActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var studentList: ArrayList<Student>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_list)

        recyclerView = findViewById(R.id.recyclerView)

        val addBtn = findViewById<FloatingActionButton>(R.id.addStudent)

        recyclerView.layoutManager = LinearLayoutManager(this)

        studentList = ArrayList()

        val url = "http://10.18.118.134/studentapi/fetch_students.php"

        val request = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,

            { response ->

                for (i in 0 until response.length()) {

                    val obj = response.getJSONObject(i)

                    val student = Student(
                        obj.getString("name"),
                        obj.getString("email"),
                        obj.getString("course")
                    )

                    studentList.add(student)
                }

                recyclerView.adapter = StudentAdapter(studentList)

            },

            { error ->

                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()

            })

        AppConfig.getQueue(this).add(request)

        addBtn.setOnClickListener {

            startActivity(Intent(this, RegisterActivity::class.java))

        }

    }
}
