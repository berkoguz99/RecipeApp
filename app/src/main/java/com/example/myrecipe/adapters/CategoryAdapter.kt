package com.example.myrecipe.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myrecipe.databinding.CategoryItemBinding
import com.example.myrecipe.pojo.Category

class CategoryAdapter(): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    private var categoriesList = ArrayList<Category>()
    var onItemClick : ((Category)->Unit)? = null

    fun setCategoryList(categoriesList:List<Category>){
        this.categoriesList = categoriesList as ArrayList<Category>
        notifyDataSetChanged()
    }
    inner class CategoryViewHolder(val binding: CategoryItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)

    }
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        Glide.with(holder.itemView).load(categoriesList[position].strCategoryThumb).into(holder.binding.imgCategory)
        holder.binding.categoryName.text = categoriesList[position].strCategory

        //istenen kategoriye tiklandiginda oaraya gitmek icin
        holder.itemView.setOnClickListener{
            onItemClick!!.invoke(categoriesList[position])
        }
    }
    override fun getItemCount(): Int {
        return categoriesList.size
    }
}