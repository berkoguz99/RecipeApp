package com.example.myrecipe.activities
import CommentAdapter
import OtherUserCommentsAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myrecipe.R
import com.example.myrecipe.adapters.PopularMealsAdapter
import com.example.myrecipe.fragments.HomeFragment
import com.example.myrecipe.pojo.Comment
import com.example.myrecipe.pojo.FirebaseMeal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class OtherUserActivity : AppCompatActivity() {

    private lateinit var userId: String

    // Kullanıcı özellikleri için TextView'ler
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var nameTextView: TextView
    private lateinit var areaTextView: TextView
    private lateinit var personalBioTextView: TextView
    private lateinit var profilePictureImageView: ImageView
    private lateinit var usernameLikes: TextView
    private lateinit var recyclerViewLikes: RecyclerView
    private lateinit var recyclerViewComments: RecyclerView
    private lateinit var likedRecipesAdapter :PopularMealsAdapter
    private lateinit var commentsAdapter :OtherUserCommentsAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_user)
        userId = intent.getStringExtra("userId") ?: ""
        // XML dosyasındaki TextView'leri ve ImageView'i bağlama
        usernameTextView = findViewById(R.id.otherusernm)
        emailTextView = findViewById(R.id.otheremail)
        nameTextView = findViewById(R.id.othername)
        areaTextView = findViewById(R.id.otherloc)
        profilePictureImageView = findViewById(R.id.otheruserpp)
        personalBioTextView =findViewById(R.id.personal_info)
        usernameLikes=findViewById(R.id.likes)
        recyclerViewLikes = findViewById(R.id.rec_view_likes)
        recyclerViewComments= findViewById(R.id.rec_view_your_comments)

        // Veritabanından kullanıcıyı çek
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Kullanıcının özelliklerini al
                val username = snapshot.child("username").getValue(String::class.java) ?: ""
                val email = snapshot.child("email").getValue(String::class.java) ?: ""
                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val area = snapshot.child("area").getValue(String::class.java) ?: ""
                val profilePictureUrl = snapshot.child("pp").getValue(String::class.java) ?: ""

                // TextView'lerde göster
                usernameTextView.text = "@$username"
                emailTextView.text = "Email: $email"
                nameTextView.text = "Name: $name"
                areaTextView.text = "Area: $area"
                usernameLikes.text="$username isimli kullanıcının beğendiği yemekler:"


                // Profil resmini yükle
                Glide.with(this@OtherUserActivity)
                    .load(profilePictureUrl)
                    .into(profilePictureImageView)
            }
            override fun onCancelled(error: DatabaseError) {
                // Veritabanı hatası durumunda yapılacak işlemler
                Log.e("OtherUserActivity", "Error getting user data: ${error.message}")
            }
        })

        prepareLikedRecyclerView()
        getLikedMeals()
        onFavMealClick()

        prepareCommentsRecyclerView()
        onClickComments()

// RecyclerView'ı bul ve adapter'ı ayarla

//        val recyclerViewLikes: RecyclerView = findViewById(R.id.rec_view_likes)
//        recyclerViewLikes.adapter = likedRecipesAdapter

// Kullanıcının beğendiği leeringly ID'lerini çek

        personalBioTextView.setOnClickListener {
            val layoutParams = personalBioTextView.layoutParams
            if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT ){
                layoutParams.height  = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._90sdp)
            }else{
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT // veya istediğiniz başka bir değer
            }
            personalBioTextView.layoutParams = layoutParams
        }
    }

    private fun onClickComments() {
        commentsAdapter.onItemClick ={comment ->
            //val intent = Intent(this@MealActivity,OtherUserActivity::class.java)
            val intent = Intent(this, MealActivity::class.java)
            intent.putExtra(HomeFragment.MEAL_ID,comment.mealId)

            startActivity(intent)
        }
    }
    private fun getComments(): ArrayList<Comment> {
        val commentsList: ArrayList<Comment> = ArrayList()
        val commentsRef = FirebaseDatabase.getInstance().reference.child("comments")
        commentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (commentSnapshot in snapshot.children) {
                    val commentUserId = commentSnapshot.child("userId").getValue(String::class.java)

                    // Kullanıcının yaptığı yorumları bul ve listeye ekle
                    if (commentUserId == userId) {
                        val comment = commentSnapshot.getValue(Comment::class.java)
                        comment?.let {
                            // Yorumun mealThumb'ini al
                            val mealId = comment.mealId
                            val mealThumbRef = mealId?.let { it1 ->
                                FirebaseDatabase.getInstance().reference.child("beğenilen_yemekler").child(
                                    it1
                                )
                            }
                            if (mealThumbRef != null) {
                                mealThumbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(mealSnapshot: DataSnapshot) {
                                        val mealName = mealSnapshot.child("strMeal").getValue(String::class.java)
                                        val mealThumb = mealSnapshot.child("strMealThumb").getValue(String::class.java)
                                        comment.mealThubm = mealThumb
                                        comment.mealName = mealName
                                        commentsList.add(comment)
                                        commentsAdapter.notifyDataSetChanged()
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        // Hata durumunda burası çalışır
                                        Log.e("OtherUserActivity", "Error getting meal thumb: ${error.message}")
                                    }
                                })
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumunda burası çalışır
                Log.e("OtherUserActivity", "Error getting comments: ${error.message}")
            }
        })
        return commentsList
    }
    private fun prepareCommentsRecyclerView() {
        commentsAdapter= OtherUserCommentsAdapter(getComments())
        recyclerViewComments.apply {
            layoutManager = LinearLayoutManager(this@OtherUserActivity, LinearLayoutManager.VERTICAL,false)
            adapter=commentsAdapter
        }    }

    private fun onFavMealClick() {
        likedRecipesAdapter.onItemClick ={meal ->
            val intent = Intent(this@OtherUserActivity,MealActivity::class.java)

            intent.putExtra(HomeFragment.MEAL_ID,meal.idMeal)
            intent.putExtra(HomeFragment.MEAL_NAME,meal.strMeal)
            intent.putExtra(HomeFragment.MEAL_THUMB,meal.strMealThumb)
            startActivity(intent)

        }
    }
    private fun getLikedMeals() {
        val likedRecipesRef = FirebaseDatabase.getInstance().reference.child("beğenilen_yemekler")
        val likedRecipeIds: MutableList<String> = mutableListOf()

        likedRecipesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (likedRecipeSnapshot in snapshot.children) {
                    val likedByUsers = likedRecipeSnapshot.child("likedByUsers").getValue(object : GenericTypeIndicator<List<String>>() {})


                    if (likedByUsers != null && likedByUsers.contains(userId)) {
                        likedRecipeIds.add(likedRecipeSnapshot.key ?: "")
                    }
                }
                Log.d("OtherUserActivity", "Liked Recipes: $likedRecipeIds")

                // Beğenilen yemeklerin ID'leriyle Firebase'den yemek bilgilerini al
                val likedMealsList: ArrayList<FirebaseMeal> = ArrayList()
                for (likedRecipeId in likedRecipeIds) {
                    val mealRef = FirebaseDatabase.getInstance().reference.child("beğenilen_yemekler").child(likedRecipeId)
                    mealRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(mealSnapshot: DataSnapshot) {
                            val meal = mealSnapshot.getValue(FirebaseMeal::class.java)
                            if (meal != null) {
                                likedMealsList.add(meal)
                                Log.d("OtherUserActivity", "Liked meals: $likedMealsList")
                                // Tüm yemekler eklendiğinde adapter'a set et

                                likedRecipesAdapter.setMeals(likedMealsList)

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("OtherUserActivity", "Error getting meal data: ${error.message}")
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("OtherUserActivity", "Error getting liked recipes: ${error.message}")
            }
        })    }

    private fun prepareLikedRecyclerView() {
        likedRecipesAdapter= PopularMealsAdapter()
        recyclerViewLikes.apply {
            layoutManager = LinearLayoutManager(this@OtherUserActivity, LinearLayoutManager.HORIZONTAL,false)
            adapter=likedRecipesAdapter
        }
    }
}




