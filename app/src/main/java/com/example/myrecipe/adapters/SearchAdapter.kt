package com.example.myrecipe.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myrecipe.pojo.MealsByCategory
import com.example.myrecipe.databinding.MealItemBinding
import com.example.myrecipe.pojo.Meal
import com.example.myrecipe.pojo.User


class SearchAdapter(): RecyclerView.Adapter<SearchAdapter.SearchMealsViewModel>() {

    var onItemClick : ((Meal)->Unit)? = null



    inner class SearchMealsViewModel(val binding:MealItemBinding):RecyclerView.ViewHolder(binding.root)

    private val diffUtil = object : DiffUtil.ItemCallback<Meal>(){
        override fun areItemsTheSame(oldItem: Meal, newItem: Meal): Boolean {
            return oldItem.idMeal == newItem.idMeal
        }

        override fun areContentsTheSame(
            oldItem: Meal,
            newItem: Meal
        ): Boolean {
            return oldItem==newItem
        }

    }


    val differ = AsyncListDiffer(this,diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchMealsViewModel {
        return SearchMealsViewModel(
            MealItemBinding.inflate(
                LayoutInflater.from(parent.context),parent,false
            )
        )
    }

    override fun onBindViewHolder(holder: SearchMealsViewModel, position: Int) {




        val meal =differ.currentList[position]

        Glide.with(holder.itemView).load(meal.strMealThumb)
            .into(holder.binding.imgMeal)
        holder.binding.tvMealName.text = meal.strMeal


        holder.itemView.setOnClickListener{
            onItemClick!!.invoke(meal)
        }


    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


}