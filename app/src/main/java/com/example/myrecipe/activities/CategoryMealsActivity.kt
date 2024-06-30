package com.example.myrecipe.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myrecipe.R
import com.example.myrecipe.adapters.CategoryMealsAdapter
import com.example.myrecipe.databinding.ActivityCategoryMealsBinding
import com.example.myrecipe.fragments.HomeFragment
import com.example.myrecipe.viewModel.CategoryMealsViewModel


class CategoryMealsActivity : AppCompatActivity() {
    lateinit var binding: ActivityCategoryMealsBinding
    lateinit var categoryMealsViewModel: CategoryMealsViewModel
    lateinit var categoryMealsAdapter: CategoryMealsAdapter






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryMealsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prepareRecyclerView()

        categoryMealsViewModel = ViewModelProviders.of(this)[CategoryMealsViewModel::class.java]

        categoryMealsViewModel.getMealsByCategory(intent.getStringExtra(HomeFragment.CATEGORY_NAME)!!)

        categoryMealsViewModel.observeMealsLiveData().observe(this, Observer { mealsList ->
            categoryMealsAdapter.setMealsList(mealsList)
            // categoryMealsAdapter.differ.submitList(mealsList)
        })

        binding.categoryScreenName.text = intent.getStringExtra(HomeFragment.CATEGORY_NAME)!!.toString()
        onCategoryClick()

    }


    private fun onCategoryClick() {
        categoryMealsAdapter.onItemClick ={MealsByCategory ->

            val intent = Intent(this,MealActivity::class.java)

            //intentte key olarak homefragmenttaki ayni degeri kullaniyorum cunku mealActivityde bu key ile intent aciliyor.
            intent.putExtra(HomeFragment.MEAL_ID,MealsByCategory.idMeal)
            intent.putExtra(HomeFragment.MEAL_NAME,MealsByCategory.strMeal)
            intent.putExtra(HomeFragment.MEAL_THUMB,MealsByCategory.strMealThumb)

            startActivity(intent)

        }
    }

    private fun prepareRecyclerView() {
        categoryMealsAdapter= CategoryMealsAdapter()

        binding.rvMeals.apply {
            layoutManager=GridLayoutManager(context,1,GridLayoutManager.VERTICAL,false)
            adapter=categoryMealsAdapter
        }
    }
}