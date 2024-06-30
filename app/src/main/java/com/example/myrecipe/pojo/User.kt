package com.example.myrecipe.pojo

class User(

    val name: String,
    val surname: String,
    val username: String,
    val email: String,
    val pp: String, //base64 enedir araştır
    val area: String,
    val bio: String? = null


) {
    constructor() : this( "", "", "", "", "","")
}

data class Comment(
    val comment: String?=null,
    val mealId: String?=null,
    val userId:String?=null,
    val username: String?=null,
    var mealThubm: String?=null,
    var mealName: String?=null,
    val profilePictureUrl: String? = null) {// Profil resminin URL'si{
    constructor() : this("", "", "", "","","","")}

