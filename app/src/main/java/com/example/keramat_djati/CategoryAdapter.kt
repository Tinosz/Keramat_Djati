package com.example.keramat_djati

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.keramat_djati.com.example.keramat_djati.Category

// Data class for Category
class CategoryAdapter(
    private var categories: List<Category>,
    private val onEditClick: (Category) -> Unit,
    private val onDeleteClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryName: TextView = view.findViewById(R.id.category_name)
        val editButton: ImageView = view.findViewById(R.id.edit_category)
        val deleteButton: ImageView = view.findViewById(R.id.delete_category)

        fun bind(category: Category) {
            categoryName.text = category.name

            // Use itemView.context to launch the edit activity
            editButton.setOnClickListener {
                val intent = Intent(itemView.context, CategoryEditForm::class.java)
                intent.putExtra("categoryType", category.type)
                intent.putExtra("categoryId", category.id)
                intent.putExtra("categoryName", category.name)
                itemView.context.startActivity(intent)
            }

            deleteButton.setOnClickListener {
                onDeleteClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    fun updateData(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}
