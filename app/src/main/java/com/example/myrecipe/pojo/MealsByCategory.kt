package com.example.myrecipe.pojo

data class MealsByCategory(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String
)


fun convertFromFireBaseMealToMealsByCategory(apiMeal: FirebaseMeal): MealsByCategory {
    return MealsByCategory(
        idMeal = apiMeal.idMeal,
        strMeal = apiMeal.strMeal,
        strMealThumb = apiMeal.strMealThumb,
    )
}