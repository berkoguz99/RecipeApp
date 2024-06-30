package com.example.myrecipe.fragments

import ProfileSearchAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myrecipe.R
import com.example.myrecipe.activities.MainActivity
import com.example.myrecipe.activities.MealActivity
import com.example.myrecipe.activities.OtherUserActivity
import com.example.myrecipe.adapters.SearchAdapter
import com.example.myrecipe.databinding.FragmentSearchBinding
import com.example.myrecipe.pojo.User
import com.example.myrecipe.viewModel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {
    private lateinit var binding:FragmentSearchBinding
    private lateinit var viewModel:HomeViewModel
    private  var searchRecyclerviewAdapter: SearchAdapter= SearchAdapter()
    private  var searchRecyclerviewAdapter2: ProfileSearchAdapter = ProfileSearchAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel= ViewModelProvider(activity as MainActivity)[HomeViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        observeSearchedMealsLiveData()


        //coroutine kullanarak otomatik arama
        var searchJob: Job? =null
        binding.edSearchBox.addTextChangedListener {  searchQuery ->

            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(1000)

                //@ile baslayan aramalar user aramasidir.
                if (searchQuery.toString().startsWith("@") && searchQuery.toString().length>3){



                    val database = FirebaseDatabase.getInstance()
                    val userRef = database.reference.child("Users")
                    //queryi değiştirerek girilen metini içeren tüm sonuçları getiriyor.
                    val query = userRef.orderByChild("username").startAt(searchQuery.toString().substring(1)).endAt(searchQuery.toString().substring(1) + "\uf8ff")


                    query.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val userList = arrayListOf<User>()
                            userList.clear()
                            val usernameIDMap = mutableMapOf<String, String>()  //intent ile ilgili kullanicinin profiline gidebilmek icin ID tutuyoruz.
                            usernameIDMap.clear()

                            // Sorgu sonuçları burada işlenir
                            for (snapshot in dataSnapshot.children) {
                                val user = snapshot.getValue(User::class.java)




                                if (user != null ) {
                                    val username = user.username
                                    val refId = snapshot.key!!
                                    usernameIDMap[username] = refId

                                    userList.add(user)

                                }

                                prepareRecyclerViewForProfiles()
                                userList.reverse()
                                searchRecyclerviewAdapter2.setUsers(userList)

                                //eger mevcut kullanici kendini arartip tiklarsa direkt profilfragmente gitmesini sitiyoruz.
                                searchRecyclerviewAdapter2.onItemClick ={it ->

                                    val userRefId = usernameIDMap[it.username]

                                    if (FirebaseAuth.getInstance().currentUser?.uid==userRefId){
                                        findNavController().navigate(R.id.action_searchFragment_to_profileFragment)
                                    }else{

                                        val intent = Intent(activity, OtherUserActivity::class.java)



                                        intent.putExtra("userId", userRefId)
                                        startActivity(intent)

                                    }

                                }

                            }


                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Sorgu iptal edildiğinde veya başarısız olduğunda burada işlem yapılabilir
                            println("Sorgu iptal edildi veya başarısız oldu: ${databaseError.message}")
                        }
                    })



                }else{//@ ile baslamayanlar meal aramasidir.

                    prepareRecyclerViewForMeals()
                    viewModel.searchMeals(searchQuery.toString())

                }


            }
        }

        onMealClick()

    }

    private fun observeSearchedMealsLiveData() {

        viewModel.observeSearchedMealsLiveData().observe(viewLifecycleOwner,  Observer{ mealsList ->
            searchRecyclerviewAdapter.differ.submitList(mealsList)

            //burasinin calismamasinin sebebi homeviewmodelde meal tipinde calisirken categorymealsadapterde mealsbycategory tipinde calisiyoiorum

        })


    }





    private fun onMealClick() {
        searchRecyclerviewAdapter.onItemClick ={it ->
            val intent = Intent(activity, MealActivity::class.java)

            //intentte key olarak homefragmenttaki ayni degeri kullaniyorum cunku mealActivityde bu key ile intent aciliyor.
            intent.putExtra(HomeFragment.MEAL_ID,it.idMeal)

            startActivity(intent)


        }
    }




    private fun prepareRecyclerViewForMeals() {


        binding.rvSearchedMeals.apply {

            layoutManager=LinearLayoutManager(context)
            adapter=searchRecyclerviewAdapter

        }

    }

    private fun prepareRecyclerViewForProfiles() {


        binding.rvSearchedMeals.apply {

            layoutManager=LinearLayoutManager(context)
            adapter=searchRecyclerviewAdapter2

        }

    }

}