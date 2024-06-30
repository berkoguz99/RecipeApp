package com.example.myrecipe.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myrecipe.databinding.PopulerMealItemBinding
import com.example.myrecipe.pojo.FirebaseMeal

class PopularMealsAdapter(): RecyclerView.Adapter<PopularMealsAdapter.PopularMealViewHolder>() {

    private var mealsList = ArrayList<FirebaseMeal>()   //meal tipinde yemek bilgisi aliyor. bunu yemeklerin favori sayisina gore yapacagimiz zaman firebaseMeal tipine cevirmemiz gerekecek
lateinit var onItemClick:((FirebaseMeal)->Unit)
    fun setMeals(mealsList:ArrayList<FirebaseMeal>){
        this.mealsList=mealsList
        notifyDataSetChanged()
    }

    class PopularMealViewHolder( val binding:PopulerMealItemBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularMealViewHolder {

        return   PopularMealViewHolder(PopulerMealItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
return mealsList.size   }

    override fun onBindViewHolder(holder: PopularMealViewHolder, position: Int) {
        Glide.with(holder.itemView)
            .load(mealsList[position].strMealThumb)
            .into(holder.binding.imgPopularMealItem)

        holder.itemView.setOnClickListener{
            onItemClick.invoke(mealsList[position])
        }
    }
}