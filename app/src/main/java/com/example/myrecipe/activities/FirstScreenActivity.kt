package com.example.myrecipe.activities



import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myrecipe.R
import com.example.myrecipe.databinding.ActivityFirstScreenBinding
import com.example.myrecipe.databinding.ActivityMealBinding


class FirstScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFirstScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityFirstScreenBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.loginButton.setOnClickListener {

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.signinButton.setOnClickListener {

            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        }

    }
}