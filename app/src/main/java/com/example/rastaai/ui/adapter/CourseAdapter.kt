package com.example.rastaai.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rastaai.R
import com.example.rastaai.data.local.db.CourseEntity
import com.google.android.material.button.MaterialButton  // Add this import
import com.google.android.material.chip.Chip  // Add this import

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
        private val category = itemView.findViewById<Chip>(R.id.category)  // Changed to Chip
        private val score = itemView.findViewById<Chip>(R.id.score)        // Changed to Chip
        private val btnDelete = itemView.findViewById<MaterialButton>(R.id.btnDelete)  // Changed to MaterialButton

        fun bind(item: CourseEntity) {
            title.text = item.title
            description.text = item.description
            category.text = item.categoryName ?: "Uncategorized"  // Set chip text
            score.text = item.score?.toString() ?: "0"  // Set chip text (removed "Score: " prefix)

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