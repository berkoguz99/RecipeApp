package com.example.myrecipe.fragments


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myrecipe.R
import com.example.myrecipe.activities.CategoryMealsActivity
import com.example.myrecipe.activities.MealActivity
import com.example.myrecipe.adapters.CategoryAdapter
import com.example.myrecipe.adapters.PopularMealsAdapter
import com.example.myrecipe.databinding.FragmentHomeBinding
import com.example.myrecipe.pojo.FirebaseMeal
import com.example.myrecipe.pojo.Meal
import com.example.myrecipe.viewModel.HomeViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var homeMvvm:HomeViewModel
    private lateinit var randomMeal:Meal

    var popularMeals: ArrayList<FirebaseMeal> = ArrayList()
    private lateinit var categoryAdapter: CategoryAdapter

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private lateinit var popularItemsAdapter:PopularMealsAdapter

    companion object{

        const val MEAL_ID = "com.example.myrecipe.idMeal"
        const val MEAL_NAME = "com.example.myrecipe.nameMeal"
        const val MEAL_THUMB = "com.example.myrecipe.thumbMeal"
        const val CATEGORY_NAME = "com.example.myrecipe.categoryName"



    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeMvvm= ViewModelProvider(this)[HomeViewModel::class.java]

        popularItemsAdapter= PopularMealsAdapter()
        fillPopulerMeal()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater,container,false)



        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        homeMvvm.getRandomMeal()
        observerRandomMeal()
        onRandomMealClick()//randomemale tiklandiginda details actity olan mealActivitye geciyoruz.


        preparePopularItemsRecyclerView()
        popularItemsAdapter.setMeals(popularMeals)
        onPopularMealClick()


        prepareCategoriesRecyclerView()
        homeMvvm.getCategories()
        observeCategoriesLiveData()
        onCategoryClick()

        //bu fragmentte geri tusuna basildigi zaman bir uyari mesaji verilierek uygulamayi kapatmak.
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)


        onSearchIconClick()


    }

    private fun onPopularMealClick() {

        popularItemsAdapter.onItemClick ={meal ->
            val intent = Intent(activity,MealActivity::class.java)

            intent.putExtra(MEAL_ID,meal.idMeal)
            intent.putExtra(MEAL_NAME,meal.strMeal)
            intent.putExtra(MEAL_THUMB,meal.strMealThumb)
            startActivity(intent)

        }

    }

    private fun preparePopularItemsRecyclerView() {
        binding.recViewPopulerMeals.apply {
            layoutManager = LinearLayoutManager(activity,LinearLayoutManager.HORIZONTAL,false)
            adapter=popularItemsAdapter
        }
    }

    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Exit")
            .setMessage("Are you sure you want to exit the ReciepeApp?")
            .setPositiveButton("Yes") { _, _ ->
                requireActivity().finishAffinity()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun onSearchIconClick() {


        binding.imgSearch.setOnClickListener{
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }

    private fun onCategoryClick() {
        categoryAdapter.onItemClick ={category ->
            val intent = Intent(activity,CategoryMealsActivity::class.java)
            intent.putExtra(CATEGORY_NAME,category.strCategory)
            startActivity(intent)

        }
    }

    private fun prepareCategoriesRecyclerView() {
        categoryAdapter = CategoryAdapter()
        binding.recViewCategories.apply {
            layoutManager=GridLayoutManager(context,3)
            adapter=categoryAdapter
        }    }

    private fun observeCategoriesLiveData() {
        homeMvvm.observeCategoriesLiveData().observe(viewLifecycleOwner, Observer { categories ->
            categories?.let {
                categoryAdapter.setCategoryList(it)
            }
        })    }


    private fun onRandomMealClick() {
        binding.randomMealCard.setOnClickListener{
            val intent = Intent(activity,MealActivity::class.java)


            intent.putExtra(MEAL_ID,randomMeal.idMeal)

            startActivity(intent)
        }
    }

    private fun observerRandomMeal() {
        homeMvvm.observerRandomMealLiveData().observe(viewLifecycleOwner,object : androidx.lifecycle.Observer<Meal>{
            override fun onChanged(value: Meal) {

                Glide.with(this@HomeFragment)
                    .load(value!!.strMealThumb)
                    .into(binding.imgRandomMeal)

                randomMeal =value  //enustte olusturdugutmuz lateinitli degeri tiklanan meale esitliyoruz

            }
        })
    }
    private fun fillPopulerMeal() {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("beğenilen_yemekler")

        ref.orderByChild("favoriteCount").startAt(1.toDouble()).limitToLast(10).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val sortedMeals = mutableListOf<FirebaseMeal>()

                dataSnapshot.children.forEach { mealSnapshot ->
                    val meal = mealSnapshot.getValue(FirebaseMeal::class.java)
                    meal?.let {
                        sortedMeals.add(0, it) // En yüksek fav sayısına sahip yemekleri listenin başına ekle
                    }
                }

                // 10'dan az yemek varsa direkt olarak eklenenleri kullan
                if (sortedMeals.size <= 10) {
                    popularMeals.clear()
                    popularMeals.addAll(sortedMeals)
                } else {
                    // Daha fazla yemek varsa sadece ilk 10 tanesini al
                    popularMeals.clear()
                    popularMeals.addAll(sortedMeals.subList(0, 10))
                }

                // RecyclerView'e veri eklendiği için adapter'a yeniden bildirme
                popularItemsAdapter.setMeals(popularMeals)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Veritabanı işlemi iptal edildi: ${databaseError.message}")
            }
        })
    }




}