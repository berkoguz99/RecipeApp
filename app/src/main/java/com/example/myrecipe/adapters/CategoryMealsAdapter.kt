package com.example.myrecipe.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myrecipe.R
import com.example.myrecipe.databinding.MealItemBinding
import com.example.myrecipe.pojo.Category
import com.example.myrecipe.pojo.Meal
import com.example.myrecipe.pojo.MealList
import com.example.myrecipe.pojo.MealsByCategory

class CategoryMealsAdapter : RecyclerView.Adapter<CategoryMealsAdapter.CategoryMealsViewModel>(){
    private var mealList = ArrayList<MealsByCategory>()

    var onItemClick : ((MealsByCategory)->Unit)? = null

    fun setMealsList(mealsList:List<MealsByCategory>){
        this.mealList =mealsList as ArrayList<MealsByCategory>
        notifyDataSetChanged()
    }

    inner class CategoryMealsViewModel(val binding:MealItemBinding):RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryMealsViewModel {
        return CategoryMealsViewModel(
            MealItemBinding.inflate(
                LayoutInflater.from(parent.context)

            )
        )
    }

    override fun onBindViewHolder(holder: CategoryMealsViewModel, position: Int) {
        Glide.with(holder.itemView).load(mealList[position].strMealThumb)
            .into(holder.binding.imgMeal)
        holder.binding.tvMealName.text = mealList[position].strMeal

        holder.itemView.setOnClickListener{
            onItemClick!!.invoke(mealList[position])
        }
    }

    override fun getItemCount(): Int {
        return mealList.size
    }
}