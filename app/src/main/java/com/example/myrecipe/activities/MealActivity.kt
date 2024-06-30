package com.example.myrecipe.activities

import CommentAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myrecipe.R
import com.example.myrecipe.databinding.ActivityMealBinding
import com.example.myrecipe.fragments.HomeFragment
import com.example.myrecipe.pojo.Comment
import com.example.myrecipe.pojo.FirebaseMeal
import com.example.myrecipe.pojo.Meal
import com.example.myrecipe.viewModel.MealViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MealActivity : AppCompatActivity() {

    private lateinit var mealId:String
    private lateinit var mealName:String
    private lateinit var mealThumb:String
    private lateinit var mealCategory:String
    private lateinit var mealArea:String
    private  lateinit var mealIns:String
    private  lateinit var youtubeLink:String
    private lateinit var binding:ActivityMealBinding
    var database = FirebaseDatabase.getInstance().reference
    private lateinit var mealMvvm: MealViewModel
    private lateinit var commentsList: MutableList<Comment>
    private lateinit var recyclerViewComments: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var ingredientsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealBinding.inflate(layoutInflater)

        setContentView(binding.root)

        mealMvvm = ViewModelProviders.of(this)[MealViewModel::class.java]

        getMealInformationFromIntent()//homefragmentten randommeal bilgisini cekme

      //  setInformationInViews()//aldigimiz meal bilgislerini layoutta gosterme


        checkMealInFirebase()



        mealMvvm.getMealDetail(mealId)//viewmodeli kullanarak bu iddeki yemegin diger bilgilerini cekiyoruz.
        observerMealDetailsLiveData()


        //icindekilere tiklandigi zaman asagiya dogru uzamasi icin.
        ingredientsTextView = binding.tvInstructionsSt
        ingredientsTextView.setOnClickListener {
            val layoutParams = ingredientsTextView.layoutParams
            if (layoutParams.height ==ViewGroup.LayoutParams.WRAP_CONTENT ){
                layoutParams.height  = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._150sdp)
            }else{
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT // veya istediğiniz başka bir değer
            }
            ingredientsTextView.layoutParams = layoutParams
        }

//
       // recyclerViewComments = findViewById(R.id.recyclerViewComments)
        recyclerViewComments = binding.recyclerViewComments

        commentsList = mutableListOf()



        onYoutubeClick()


        // Meals ID'sine göre yorumları dinleme
        listenComments()




        // GÖNDER BUTONUNA BASINCA OLACKALAR
        binding.btnSendcomment.setOnClickListener {


            val comment = binding.etComment.text.toString().trim()
            if (comment.isNotEmpty()) {
                sendCommentToDatabase(comment)
                // Yorum gönderildikten sonra EditText'i temizleme (isteğe bağlı)
                binding.etComment.text.clear()
            } else {
                // Kullanıcı boş bir yorum göndermeye çalıştığında gerekli işlemler yapılabilir
            }
        }

        clickFavoriteButton()

        }

    private fun clickFavoriteButton() {
        binding.favButton.setOnClickListener {
            val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

            currentUserID?.let { userID ->
                val mealsRef =
                    FirebaseDatabase.getInstance().reference.child("beğenilen_yemekler")

                val firebaseMeal = FirebaseMeal(
                    idMeal = mealId,
                    strMeal = mealName,
                    strMealThumb = mealThumb,
                    strInstructions = mealIns,
                    strCategory = mealCategory,
                    strArea = mealArea,

                    )

                firebaseMeal.idMeal?.let { mealID ->
                    mealsRef.child(mealID).addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val meal = snapshot.getValue(FirebaseMeal::class.java)

                            var favNumberNow = binding.favNumber.text.toString().toInt()

                            if (meal != null) {//yemek databaseye daha once kaydolmus
                                if (meal.likedByUsers.contains(userID)) {   // kullanici yemegi daha once begenmis ve suan dislike yapiyor.
                                    meal.favoriteCount--
                                    meal.likedByUsers =
                                        meal.likedByUsers.filter { it != userID }
                                    meal.isFavorite = false
                                    binding.favButton.setImageResource(R.drawable.ic_favorite)
                                    //fav sayisini viewde bir azaltmak icin
                                    binding.favNumber.text = (favNumberNow-1).toString()
                                } else {    //kullanici yemegi favoriye ekliyor
                                    meal.favoriteCount++
                                    meal.likedByUsers += userID
                                    binding.favButton.setImageResource(R.drawable.ic_favorite_filled)
                                    meal.isFavorite = true
                                    binding.favNumber.text = (favNumberNow+1).toString()    //fav sayisini viewde bir arttirmak icin


                                }
                                // Fav count'u güncelle
                                mealsRef.child(mealID).setValue(meal)
                            } else {
                                // Yemek daha önce beğenilmemişse
                                firebaseMeal.favoriteCount++
                                firebaseMeal.likedByUsers = listOf(userID)
                                firebaseMeal.isFavorite = true
                                mealsRef.child(mealID).setValue(firebaseMeal)
                                binding.favButton.setImageResource(R.drawable.ic_favorite_filled)
                                binding.favNumber.text = (favNumberNow+1).toString()     //fav sayisini viewde bir arttirmak icin

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Hata durumunda yapılacak işlemler
                            Log.d("MealActivity", "Database error: ${error.message}")
                        }
                    })
                }
            }
        }
    }

    private fun listenComments() {

        // Firebase veritabanı referansı
        val databaseRef = FirebaseDatabase.getInstance().reference.child("comments")

        mealId?.let { mealId ->
            databaseRef.orderByChild("mealId").equalTo(mealId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        commentsList.clear()

                        for (commentSnapshot in snapshot.children) {
                            val comment = commentSnapshot.getValue(Comment::class.java)
                            comment?.let {
                                // Kullanıcı verilerini çek
                                val userId = it.userId
                                val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(
                                    userId.toString()
                                )
                                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(userSnapshot: DataSnapshot) {
                                        val username = userSnapshot.child("username").value.toString()
                                        val profilePictureUrl = userSnapshot.child("pp").value.toString()
                                        // Yorum nesnesine kullanıcı verilerini ekle
                                        val commentWithUserData = it.copy(username = username, profilePictureUrl = profilePictureUrl)
                                        commentsList.add(commentWithUserData)

                                        // Adapterı başlat
                                        commentAdapter = CommentAdapter(commentsList)
                                        recyclerViewComments.adapter = commentAdapter
                                        recyclerViewComments.layoutManager = LinearLayoutManager(this@MealActivity)
                                        onClickListenerComments()
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        // Hata durumunda yapılacak işlemler
                                        Log.e("MealActivity", "Error getting user data: ${error.message}")
                                    }
                                })
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Hata durumunda yapılacak işlemler
                        Log.e("MealActivity", "Failed to read value.", error.toException())
                    }
                })
        }
    }

    private fun onYoutubeClick() {
        binding.imgYoutube.setOnClickListener {
            val intent  = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeLink))
            startActivity(intent)
        }    }

    private fun onClickListenerComments() {
        commentAdapter.onItemClick ={comment ->
            //val intent = Intent(this@MealActivity,OtherUserActivity::class.java)

            if (comment.userId == FirebaseAuth.getInstance().currentUser?.uid){
                       val intent = Intent(this, OtherProfile::class.java)
                       startActivity(intent)
            }else{
                val intent = Intent(this, OtherUserActivity::class.java)
                        intent.putExtra("userId", comment.userId)
                        startActivity(intent)
            }


        }
    }

    private fun observerMealDetailsLiveData() {
            mealMvvm.observerMealDetailsLiveData().observe(this, object : Observer<Meal> {
                override fun onChanged(value: Meal) {
                    Glide.with(applicationContext)
                        .load(value.strMealThumb)
                        .into(binding.imgMealDetail)
                    binding.collapsingToolbar.title = value.strMeal
                    binding.collapsingToolbar.setCollapsedTitleTextColor(resources.getColor(R.color.white))
                    binding.collapsingToolbar.setExpandedTitleColor(resources.getColor(R.color.white))

                    binding.tvCategory.text = "Category : ${value.strCategory}"
                    binding.tvArea.text = "Area : ${value.strArea}"


                    binding.tvInstructionsSt.text = value.strInstructions
                    mealArea = value.strArea
                    mealCategory = value.strCategory
                    mealIns = value.strInstructions

                    mealName=value.strMeal
                    mealThumb = value.strMealThumb
                    youtubeLink = value.strYoutube

                }
            })
        }

        //cektigimiz meal bilgisini viewdeki nesnelere veriyoruz ve detail activtyde gostermis oluyoruz.
        private fun setInformationInViews() {
            Glide.with(applicationContext)
                .load(mealThumb)
                .into(binding.imgMealDetail)
            binding.collapsingToolbar.title = mealName
            binding.collapsingToolbar.setCollapsedTitleTextColor(resources.getColor(R.color.white))
            binding.collapsingToolbar.setExpandedTitleColor(resources.getColor(R.color.white))
        }

        private fun getMealInformationFromIntent() {
            val intent = intent

            mealId = intent.getStringExtra(HomeFragment.MEAL_ID)!!
           // mealName = intent.getStringExtra(HomeFragment.MEAL_NAME)!!
           // mealThumb = intent.getStringExtra(HomeFragment.MEAL_THUMB)!!
        }

        //favoriye eklenip eklenmedigini sorgulamak icin databaseden sorgu yapan fonksiyon.
        private fun checkMealInFirebase() {
            val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
            currentUserID?.let { userID ->
                val mealsRef = FirebaseDatabase.getInstance().reference.child("beğenilen_yemekler")
                mealsRef.child(mealId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val meal = snapshot.getValue(FirebaseMeal::class.java)

                        meal?.let { it->
                            binding.favNumber.text =it.favoriteCount.toString()  //favori sayisi
                        }


                        if (meal != null && meal.isFavorite && meal.likedByUsers.contains(userID)) {
                            // Eğer yemek veritabanında varsa ve favori olarak işaretlenmişse
                            // Butonu aktif hale getir
                            binding.favButton.setImageResource(R.drawable.ic_favorite_filled)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Hata durumunda yapılacak işlemler
                        Log.d("MealActivity", "Database error: ${error.message}")
                    }
                })
            }
        }

        private fun sendCommentToDatabase(comment: String) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.let { user ->
                val userId = user.uid

                val usernameRef =
                    FirebaseDatabase.getInstance().reference.child("Users").child(userId)
                        .child("username")
                usernameRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val username = snapshot.value.toString()
                       // val profilePictureUrl = snapshot.child("pp").value.toString() // Profil resminin URL'si
                        val commentsRef = FirebaseDatabase.getInstance().reference.child("comments")

                       // val newComment = Comment(comment, mealId, userId, username, profilePictureUrl)
                        val newComment = Comment(comment = comment,mealId= mealId, userId=userId, username=username)

                        // Yeni yorumu "comments" düğümüne eyeliner
                        commentsRef.push().setValue(newComment)
                            .addOnSuccessListener {
                                // Yorum başarıyla eklendi
                                // Burada isterseniz bir Toast mesajı gösterebilirsiniz
                            }
                            .addOnFailureListener { e ->
                                // Yorum eklenirken bir hata oluştu
                                // Hata durumunda kullanıcıya bilgi vermek için gerekli işlemler yapılabilir
                                Log.e(
                                    "MealActivity",
                                    "Error adding comment to database: ${e.message}"
                                )
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Kullanıcı adı alınırken bir hata oluştu
                        // Hata durumunda kullanıcıya bilgi vermek için gerekli işlemler yapılabilir
                        Log.e("MealActivity", "Error getting username: ${error.message}")
                    }
                })
            }

        }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            return true
        }
        return super.onTouchEvent(event)
    }

         }
    










