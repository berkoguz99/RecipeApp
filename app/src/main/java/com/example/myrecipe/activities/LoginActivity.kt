package com.example.myrecipe.activities

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myrecipe.R
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var editPassword: EditText
    private lateinit var editEmail: EditText
    private lateinit var loginButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progresbar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editPassword = findViewById(R.id.passwordEditText)
        editEmail = findViewById(R.id.emailEditText)
        loginButton = findViewById(R.id.loginbutton)
        firebaseAuth = FirebaseAuth.getInstance()
        progresbar= findViewById(R.id.progresbar)

        loginButton.setOnClickListener {


            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()


            if (email.isEmpty()) {
                Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            progresbar.visibility = ProgressBar.VISIBLE

            // Firebase Authentication ile giriş yap
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Giriş başarılı ise MainActivity'e yönlendir
                        progresbar.visibility = ProgressBar.INVISIBLE

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Giriş başarısız ise kullanıcıya hata mesajı göster
                        Toast.makeText(
                            this, "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
    //ekranda herhangi bir yere basildiginda klavye otomatik kapanmasini saglar.
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            return true
        }
        return super.onTouchEvent(event)
    }
}
