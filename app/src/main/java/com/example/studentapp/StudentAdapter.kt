package com.example.studentapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(private val list:ArrayList<Student>) :
    RecyclerView.Adapter<StudentAdapter.ViewHolder>() {

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val name:TextView = itemView.findViewById(R.id.studentName)
        val email:TextView = itemView.findViewById(R.id.studentEmail)
        val course:TextView = itemView.findViewById(R.id.studentCourse)

        val deleteBtn:Button = itemView.findViewById(R.id.deleteBtn)
        val editBtn:Button = itemView.findViewById(R.id.editBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.student_item,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val student = list[position]

        holder.name.text = student.user
        holder.email.text = student.skill
        holder.course.text = student.category

        // Delete
        holder.deleteBtn.setOnClickListener {
            list.removeAt(position)
            notifyDataSetChanged()
        }

        // Edit
        holder.editBtn.setOnClickListener {

            val intent = Intent(holder.itemView.context, RegisterActivity::class.java)

            intent.putExtra("user", student.user)
            intent.putExtra("skill", student.skill)
            intent.putExtra("category", student.category)

            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}