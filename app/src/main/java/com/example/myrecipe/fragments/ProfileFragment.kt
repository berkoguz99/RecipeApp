package com.example.myrecipe.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myrecipe.R
import com.example.myrecipe.pojo.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private lateinit var imageView: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var currentUserID: String
    private lateinit var progressBar: ProgressBar
    private lateinit var personalBioTextView: TextView
    private lateinit var email: TextView
    private lateinit var username: TextView
    private lateinit var name: TextView
    private lateinit var location: TextView
    private lateinit var editProfile: TextView
    private lateinit var logout: TextView
    private lateinit var privacy: TextView
    private lateinit var contactUs: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        imageView = view.findViewById(R.id.otheruserpp)
        progressBar = view.findViewById(R.id.progressBar)
        personalBioTextView = view.findViewById(R.id.personal_info)
        email = view.findViewById(R.id.myemail)
        username = view.findViewById(R.id.myusername)
        name = view.findViewById(R.id.myname)
        location = view.findViewById(R.id.myarea)
        editProfile = view.findViewById(R.id.text_edit_profile)
        logout = view.findViewById(R.id.text_log_out)
        privacy = view.findViewById(R.id.text_privacy)
        contactUs = view.findViewById(R.id.text_contact_us)

        currentUserID = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        imageView.setOnClickListener {
            openGallery()
        }

        loadProfilePicture()

        personalBioTextView.setOnClickListener {
            val layoutParams = personalBioTextView.layoutParams
            if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                layoutParams.height = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._90sdp)
            } else {
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            personalBioTextView.layoutParams = layoutParams
        }

        loadUserInfo()

        editProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        logout.setOnClickListener {
            showExitConfirmationDialog()
        }

        privacy.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_privacyFragment)
        }

        contactUs.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_contactFragment)
        }

        return view
    }

    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Exit")
            .setMessage("Are you sure you want to logout the ReciepeApp?")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                activity?.finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun loadUserInfo() {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val userReference = databaseReference.child("Users").child(currentUserID)
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                email.text = user?.email
                username.text = "@${user?.username}"
                name.text = user?.name
                location.text = user?.area
                personalBioTextView.text = user?.bio
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Servislerde bir sorun var lütfen daha sonra tekrar deneyin.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadProfilePicture() {
        progressBar.visibility = View.VISIBLE
        val databaseReference = FirebaseDatabase.getInstance().reference
        val userReference = databaseReference.child("Users").child(currentUserID)
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userProfile = dataSnapshot.getValue(User::class.java)
                val ppUrl = userProfile?.pp
                Log.d("ProfileFragment", "Profile picture URL: $ppUrl")
                if (!ppUrl.isNullOrEmpty()) {
                    val imageUri = Uri.parse(ppUrl)
                    Picasso.get().load(imageUri).into(imageView, object : Callback {
                        override fun onSuccess() {
                            progressBar.visibility = View.GONE
                        }

                        override fun onError(e: Exception?) {
                            progressBar.visibility = View.GONE
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Servislerde bir sorun var lütfen daha sonra tekrar deneyin.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            progressBar.visibility = View.VISIBLE
            val imageUri = data.data
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid
            if (userId != null && imageUri != null) {
                val storageRef = FirebaseStorage.getInstance().reference
                val imagesRef = storageRef.child("profile_images/$userId")

                imagesRef.putFile(imageUri)
                    .addOnSuccessListener { taskSnapshot ->
                        imagesRef.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            val databaseReference = FirebaseDatabase.getInstance().reference
                            databaseReference.child("Users").child(userId).child("pp").setValue(imageUrl)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Profil resmi başarıyla yüklendi!", Toast.LENGTH_SHORT).show()
                                    imageView.setImageURI(imageUri)
                                    progressBar.visibility = View.GONE
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Yükleme işlemi başarısız oldu!", Toast.LENGTH_SHORT).show()
                                    progressBar.visibility = View.GONE
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Servislerde bir sorun var lütfen daha sonra tekrar deneyin.", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                    }
            }
        }
    }
}
