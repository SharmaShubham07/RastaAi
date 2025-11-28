package com.example.rastaai.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rastaai.R
import com.example.rastaai.data.local.db.CourseEntity

class CourseAdapter(
    private val onClick: (CourseEntity) -> Unit,
    private val onDeleteClick: (CourseEntity) -> Unit
) : ListAdapter<CourseEntity, CourseAdapter.Holder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CourseEntity>() {
            override fun areItemsTheSame(a: CourseEntity, b: CourseEntity) = a.id == b.id
            override fun areContentsTheSame(a: CourseEntity, b: CourseEntity) = a == b
        }
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.title)
        private val description = itemView.findViewById<TextView>(R.id.description)
        private val category = itemView.findViewById<TextView>(R.id.category)
        private val score = itemView.findViewById<TextView>(R.id.score)
        private val btnDelete = itemView.findViewById<ImageView>(R.id.btnDelete)

        fun bind(item: CourseEntity) {
            title.text = item.title
            description.text = item.description
            category.text = item.categoryName ?: "Uncategorized"
            score.text = "Score: ${item.score}"

            itemView.setOnClickListener { onClick(item) }
            btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) =
        holder.bind(getItem(position))
}
