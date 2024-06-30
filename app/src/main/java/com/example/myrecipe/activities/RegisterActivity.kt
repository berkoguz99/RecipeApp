package com.example.myrecipe.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.myrecipe.databinding.ActivityRegisterBinding
import com.example.myrecipe.pojo.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.registerLinkTv.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.saveButton.setOnClickListener {
            val name = binding.regName.text.toString().trim()
            val surname = binding.regSurname.text.toString().trim()
            val username = binding.regUsername.text.toString().trim()
            val email = binding.regEmail.text.toString().trim()
            val password = binding.regPassword.text.toString().trim()
            val confirmPassword = binding.regPasswordConfirm.text.toString().trim()


            if (name.isEmpty() || surname.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Şifreler eşleşmiyor", Toast.LENGTH_SHORT).show()
            } else {
                signUp(email, password, name, surname, username)
            }
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

    private fun signUp(email: String, password: String, name: String, surname: String, username: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = firebaseAuth.currentUser
                    val userId = currentUser?.uid
                    if (userId != null) {
                        val profilePictureUrl ="https://www.jing.fm/clipimg/full/135-1353096_simple-avatar-icon-person-icon-png-free-transparent.png"
                        val user = User(name, surname, username, email,profilePictureUrl,"Türkiye")
                        // Kullanıcı ID'sini kullanarak kullanıcıyı veritabanına kaydet
                        database.reference.child("Users").child(userId).setValue(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Kayıt Başarılı", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Kayıt sırasında bir hata oluştu: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Kullanıcı ID alınamadı", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
