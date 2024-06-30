package com.example.myrecipe.retrofit

import com.example.myrecipe.pojo.CategoryList
import com.example.myrecipe.pojo.MealList
import com.example.myrecipe.pojo.MealsByCategoryList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApi {
    @GET("random.php")
    fun getRandomMeal(): Call<MealList>


    @GET("lookup.php?")
    fun getMealDetails(@Query("i") id:String) : Call<MealList>

    @GET("categories.php")
    fun getCategories():Call<CategoryList>

    @GET("filter.php")
    fun getMealsByCategory(@Query("c") categoryName: String) : Call<MealsByCategoryList>//buraya yanlis yazmis olabilirtim

    @GET("search.php")
    fun searchMeals(@Query("s") searchQuery:String) : Call<MealList>

}