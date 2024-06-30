package com.example.myrecipe.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.myrecipe.R
import com.example.myrecipe.pojo.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class EditProfileFragment : Fragment() {

    private lateinit var imageView: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var currentUserID: String
    private lateinit var progressBar: ProgressBar
    private lateinit var personalBioTextView: EditText
    private lateinit var email: EditText
    private lateinit var username: EditText
    private lateinit var name: EditText
    private lateinit var location: EditText
    private lateinit var surname: EditText
    private lateinit var saveButton: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        imageView = view.findViewById(R.id.profileImageView)
        progressBar = view.findViewById(R.id.progressBar)
        personalBioTextView = view.findViewById(R.id.bioEditText)
        email = view.findViewById(R.id.emailEditText)
        username = view.findViewById(R.id.usernameEditText)
        name = view.findViewById(R.id.nameEditText)
        location = view.findViewById(R.id.areaEditText)
        surname = view.findViewById(R.id.surnameEditText)
        saveButton = view.findViewById(R.id.save_button)

        // Kullanıcı ID'sini al
        currentUserID = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        imageView.setOnClickListener {
            openGallery()
        }

        saveButton.setOnClickListener {
            saveUserInfo()
        }

        loadProfilePicture()
        loadUserInfo()

        return view
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun loadProfilePicture() {
        // Kullanıcının profil resmini veritabanından al ve ImageView'e yükle
        progressBar.visibility = View.VISIBLE
        val databaseReference = FirebaseDatabase.getInstance().reference
        val userReference = databaseReference.child("Users").child(currentUserID)
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userProfile = dataSnapshot.getValue(User::class.java)
                val ppUrl = userProfile?.pp
                Log.d("editProfileFragment", "Profile picture URL: $ppUrl")
                // ppUrl null değilse yani bir resim URL'si varsa, resmi yükle
                if (!ppUrl.isNullOrEmpty()) {
                    // Resmi Picasso ile yüklemeden önce URI'ye dönüştür
                    val imageUri = Uri.parse(ppUrl)
                    Picasso.get().load(imageUri).into(imageView, object : Callback {
                        override fun onSuccess() {
                            // Resim başarıyla yüklendiğinde yapılacak işlemler

                            progressBar.visibility = View.GONE

                        }

                        override fun onError(e: Exception?) {
                            // Resim yüklenirken bir hata oluştuğunda yapılacak işlemler

                        }
                    })
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Hata durumunda burası çalışır
                Toast.makeText(requireContext(), "Servislerde bir sorun var lütfen daha sonra tekrar deneyin.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadUserInfo() {
        // Kullanıcı bilgilerini Firebase Realtime Database'den al
        val databaseReference = FirebaseDatabase.getInstance().reference
        val userReference = databaseReference.child("Users").child(currentUserID)
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userEmail = dataSnapshot.child("email").getValue(String::class.java)
                val userUsername = dataSnapshot.child("username").getValue(String::class.java)
                val userName = dataSnapshot.child("name").getValue(String::class.java)
                val userLocation = dataSnapshot.child("area").getValue(String::class.java)
                val userSurname = dataSnapshot.child("surname").getValue(String::class.java)

                // TextView'lere kullanıcı bilgilerini yerleştir

                username.setText(userUsername)
                name.setText(userName)
                location.setText(userLocation)
                surname.setText(userSurname)
                email.setText(userEmail)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Hata durumunda burası çalışır
                Toast.makeText(requireContext(), "Servislerde bir sorun var lütfen daha sonra tekrar deneyin.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserInfo() {
        // Kullanıcı bilgilerini al
        val userEmail = email.text.toString()
        val userUsername = username.text.toString()
        val userName = name.text.toString()
        val userLocation = location.text.toString()
        val userSurname = surname.text.toString()
        val userBio = personalBioTextView.text.toString()

        // Firebase Realtime Database'e kaydet
        val databaseReference = FirebaseDatabase.getInstance().reference
        val userReference = databaseReference.child("Users").child(currentUserID)

        val userUpdates = mapOf(
            "email" to userEmail,
            "username" to userUsername,
            "name" to userName,
            "area" to userLocation,
            "surname" to userSurname,
            "bio" to userBio
        )

        userReference.updateChildren(userUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Profil bilgileri güncellendi.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Profil güncelleme başarısız.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
