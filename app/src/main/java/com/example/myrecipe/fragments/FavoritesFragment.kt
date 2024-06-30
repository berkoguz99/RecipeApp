package com.example.myrecipe.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myrecipe.activities.MealActivity
import com.example.myrecipe.adapters.CategoryMealsAdapter
import com.example.myrecipe.databinding.FragmentFavoritesBinding
import com.example.myrecipe.pojo.FirebaseMeal
import com.example.myrecipe.pojo.MealsByCategory
import com.example.myrecipe.pojo.convertFromFireBaseMealToMealsByCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FavoritesFragment : Fragment() {

    private lateinit var mealAdapter: CategoryMealsAdapter
    private lateinit var binding: FragmentFavoritesBinding


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentFavoritesBinding.inflate(inflater,container,false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        prepareRecyclerView()
        fetchUserFavoriteMeals()
        onMealClick()
    }

    //favoriden details ekranina gidip favoriyi kaldirinca halen orda gozukuyordsu.(fragmente yenilenmediginden) bu method ile tekrar calismasini sagladim.
    override fun onResume() {
        super.onResume()

        fetchUserFavoriteMeals()
    }

    private fun fetchUserFavoriteMeals() {
        // FirebaseAuth ile oturum açmış olan kullanıcının UID'sini al
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

        // Firebase veritabanı referansını oluştur
        val databaseRef = FirebaseDatabase.getInstance().reference.child("beğenilen_yemekler")

        // Kullanıcı ID'sini kontrol et
        currentUserID?.let { userID ->
            // "beğenilen_yemekler" düğümündeki tüm yemekleri al
            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {

                //databaseden cektigimiz verinin tipi adaptorle calismak icin uyumsuz oldugundan bu listeyi olustutup converter fonksiyon ile
                //asagida istedigimiz tipe donusturuyoruz.
                var listOfLikeCategoryMeals = mutableListOf<MealsByCategory>()


                override fun onDataChange(snapshot: DataSnapshot) {
                    val userFavoriteMeals = mutableListOf<FirebaseMeal>()
                    for (data in snapshot.children) {
                        val meal = data.getValue(FirebaseMeal::class.java)
                        meal?.let {
                            // Yemek beğeniler listesinde kullanıcının ID'si varsa, yemeği kullanıcının favori yemeği olarak ekle
                            if (it.likedByUsers.contains(userID)) {
                                userFavoriteMeals.add(it)

                                //istenen tipe cevirip listeye ekledik.
                                var likeCategoryMeals: MealsByCategory = convertFromFireBaseMealToMealsByCategory(it)
                                listOfLikeCategoryMeals.add(likeCategoryMeals)
                            }
                        }
                    }
                    // Adapter'a verileri aktar
                  //  mealAdapter.updateMeals(userFavoriteMeals)
                    Log.d("FavoritesFragment", "Firebase'den alınan veri sayısı: ${userFavoriteMeals.size}")

                    mealAdapter.setMealsList(listOfLikeCategoryMeals)

                }

                override fun onCancelled(error: DatabaseError) {
                    // Hata durumunda yapılacak işlemler
                    Log.e("FavoritesFragment", "Failed to read value.", error.toException())
                }
            })
        }
    }

    private fun onMealClick() {
        mealAdapter.onItemClick ={ MealsByCategory ->

            val intent = Intent(activity, MealActivity::class.java)

            //intentte key olarak homefragmenttaki ayni degeri kullaniyorum cunku mealActivityde bu key ile intent aciliyor.
            intent.putExtra(HomeFragment.MEAL_ID,MealsByCategory.idMeal)
            intent.putExtra(HomeFragment.MEAL_NAME,MealsByCategory.strMeal)
            intent.putExtra(HomeFragment.MEAL_THUMB,MealsByCategory.strMealThumb)

            startActivity(intent)

        }
    }

    private fun prepareRecyclerView() {
        mealAdapter= CategoryMealsAdapter()

        binding.favRec.apply {
            layoutManager= GridLayoutManager(context,1, GridLayoutManager.VERTICAL,false)
            adapter=mealAdapter
        }
    }

}

