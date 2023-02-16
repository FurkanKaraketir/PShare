package com.karaketir.pshare

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.karaketir.pshare.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        val loginButton = binding.LoginButton
        val emailEditText = binding.emailLoginEditText
        val passwordEditText = binding.passwordLoginEditText
        val iWanSignUpText = binding.iWantSignUpTextView
        val signUpButton = binding.signUpButton
        val forgotPassword = binding.forgotPasswordText

        forgotPassword.setOnClickListener {
            showDialog()
        }

        signUpButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            this.startActivity(intent)
        }
        iWanSignUpText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            this.startActivity(intent)
        }

        loginButton.setOnClickListener {
            if (emailEditText.text.toString().isNotEmpty()) {
                emailEditText.error = null

                if (passwordEditText.text.toString().isNotEmpty()) {
                    passwordEditText.error = null
                    signIn(emailEditText.text.toString(), passwordEditText.text.toString())

                } else {
                    passwordEditText.error = "Bu Alan Boş Bırakılamaz"
                }
            } else {
                emailEditText.error = "Bu Alan Boş Bırakılamaz"
            }

        }


    }

    private fun showDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Email Adresinizi Girin")

        val input = EditText(this)
        input.hint = "Email"
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        builder.setView(input)

        builder.setPositiveButton("Tamam") { _, _ ->
            // Here you get get input text from the Edittext
            if (input.text.toString().isNotEmpty()) {
                input.error = null
                val email = input.text.toString()
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Şifrenizi Sıfırlamak İçin Mail Başarıyla Gönderilmiştir",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } else {
                input.error = "Bu Alan Boş Bırakılamaz"
            }
        }
        builder.setNegativeButton("İptal") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Toast.makeText(this, "Başarılı!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                this.startActivity(intent)
                finish()

            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(
                    baseContext, "Giriş Başarısız!", Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener {
            println(it.localizedMessage)

        }
    }
}