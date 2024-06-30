package com.example.myrecipe.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myrecipe.R
import com.example.myrecipe.fragments.ProfileFragment

class OtherProfile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_profile)


        // ProfileFragment'i container'a ekle
            supportFragmentManager.beginTransaction().apply {
            replace(R.id.profile_fragment_container, ProfileFragment())
            commit()

        }
    }
}