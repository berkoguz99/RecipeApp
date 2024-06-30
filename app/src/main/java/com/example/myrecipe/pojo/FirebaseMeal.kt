package com.example.myrecipe.pojo


data class FirebaseMeal(
    val idMeal: String = "",
    val strMeal: String = "",
    val strMealThumb: String = "",
    val strInstructions: String = "",
    val strCategory: String = "",
    val strArea: String = "",
    var favoriteCount: Int = 0,
   var isFavorite: Boolean = false,
    var likedByUsers: List<String> = listOf()

) {
    constructor() : this("", "", "", "", "", "",0, false, listOf())
}

fun convertToFirebaseMealFromAPI(apiMeal: Meal): FirebaseMeal {
    return FirebaseMeal(
        idMeal = apiMeal.idMeal,
        strMeal = apiMeal.strMeal,
        strMealThumb = apiMeal.strMealThumb,
        strInstructions = apiMeal.strInstructions,
        strCategory = apiMeal.strCategory,
        strArea = apiMeal.strArea
    )
}

