package com.example.myrecipe.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myrecipe.pojo.Category
import com.example.myrecipe.pojo.CategoryList
import com.example.myrecipe.pojo.Meal
import com.example.myrecipe.pojo.MealList
import com.example.myrecipe.retrofit.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel():ViewModel() {

    private var randomMealLiveData=MutableLiveData<Meal>()

    private var categoriesLiveData = MutableLiveData<List<Category>>()
    private val searchedMealsLiveData = MutableLiveData<List<Meal>>()


    fun getRandomMeal(){
        RetrofitInstance.api.getRandomMeal().enqueue(object : Callback<MealList> {
            override fun onResponse(call: Call<MealList>, response: Response<MealList>) {

                if(response.body()!=null){
                    //bu kod eger apiden basarili bir sekilde random meal alirsak meali livedataya atip fragmente pasliyacak
                    val randomMeal: Meal =response.body()!!.meals[0]
                    randomMealLiveData.value=randomMeal

                }else{
                    return
                }

            }

            override fun onFailure(call: Call<MealList>, t: Throwable) {
                Log.d("HomeFragment",t.message.toString())
            }
        })

    }




    //class disi degeri okumak icin get fonksiyonu
    fun observerRandomMealLiveData(): LiveData<Meal> {
        return randomMealLiveData
    }




    fun getCategories(){
        RetrofitInstance.api.getCategories().enqueue(object : Callback<CategoryList>{
            override fun onResponse(call: Call<CategoryList>, response: Response<CategoryList>) {
                response.body()?.let { categoryList ->
                    categoriesLiveData.postValue(categoryList.categories)
                }
            }

            override fun onFailure(call: Call<CategoryList>, t: Throwable) {
                Log.e("HomeViewModel", t.message.toString())
            }
        })
    }
    fun observeCategoriesLiveData():LiveData<List<Category>>{
        return categoriesLiveData
    }


    fun searchMeals(searchQuery:String) = RetrofitInstance.api.searchMeals(searchQuery).enqueue(
        object : Callback<MealList>{
            override fun onResponse(call: Call<MealList>, response: Response<MealList>) {
                val mealsList = response.body()?.meals
                mealsList?.let{
                    searchedMealsLiveData.postValue(it)
                }
            }

            override fun onFailure(call: Call<MealList>, t: Throwable) {
                Log.e("HomeViewModel", t.message.toString())
            }

        }

    )

    fun observeSearchedMealsLiveData(): LiveData<List<Meal>> = searchedMealsLiveData


}