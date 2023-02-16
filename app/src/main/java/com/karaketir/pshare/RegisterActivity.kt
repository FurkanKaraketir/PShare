@file:Suppress("DEPRECATION")

package com.karaketir.pshare

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.pshare.databinding.ActivityRegisterBinding
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val urlFinal = ""
    private val emptyError = "Bu Alan Boş Bırakılamaz"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val registerButton = binding.RegisterButton
        val emailEditText = binding.emailLoginEditText
        val passwordEditText = binding.passwordRegisterEditText
        val userNameEditText = binding.userNameRegisterEditText



        registerButton.setOnClickListener {
            Toast.makeText(this, "Lütfen Bekleyiniz...", Toast.LENGTH_SHORT).show()
            if (emailEditText.text.toString().isNotEmpty()) {
                emailEditText.error = null

                if (passwordEditText.text.toString().isNotEmpty()) {

                    if (passwordEditText.text.toString().length >= 6) {
                        passwordEditText.error = null

                        if (userNameEditText.text.toString().isNotEmpty()) {
                            userNameEditText.error = null


                            signUp(
                                emailEditText.text.toString(),
                                passwordEditText.text.toString(),
                                userNameEditText.text.toString()
                            )


                        } else {
                            userNameEditText.error = emptyError
                        }
                    } else {
                        passwordEditText.error = "Parola 6 karakterden az olamaz"
                    }
                } else {
                    passwordEditText.error = emptyError
                }

            } else {
                emailEditText.error = emptyError
            }
        }


    }

    private fun signUp(email: String, password: String, userName: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val documentID = auth.uid!!

                val user = hashMapOf(
                    "email" to email,
                    "id" to documentID,
                    "username" to userName,
                    "profileImageURL" to urlFinal,
                )
                db.collection("User").document(documentID).set(user).addOnSuccessListener {

                    val followingID = UUID.randomUUID().toString()
                    val follow = hashMapOf(
                        "main" to email, "followsWho" to email
                    )
                    db.collection("Followings").document(followingID).set(follow)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Başarılı", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, ProfilePictureActivity::class.java)
                            this.startActivity(intent)
                            finish()
                        }

                }
            }

        }
    }


}