package com.khansafzh.recipeapp

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.khansafzh.recipeapp.LoginActivity.Companion.getLaunchService
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

        private lateinit var mGoogleSignInClient : GoogleSignInClient
        private lateinit var  gso : GoogleSignInOptions
        private lateinit var firebaseAuth: FirebaseAuth
        private var RC_SIGN_IN : Int = 1

        companion object{
            fun getLaunchService(from : Context) = Intent(from, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        }

        override fun onStart() {
            super.onStart()
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null){
                startActivity(MainActivity.getLaunchService(this))
                finish()
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_login)
            supportActionBar?.hide()

            configurationGoogleSignIn()
            setUpRequestUI()
            firebaseAuth = FirebaseAuth.getInstance()

            btn_sign_in.setOnClickListener {
                val email = et_email.text.toString()
                val password = et_password.text.toString()

                if (email.isEmpty() || password.isEmpty()){
                    Toast.makeText(this, "Insert Email and Password", Toast.LENGTH_SHORT).show()
                }
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (!it.isSuccessful){
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            return@addOnCompleteListener
                        }else{
                            Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }.addOnFailureListener {
//                    Log.d("Main", "LoginFailed : ${it.message}")
//                    Toast.makeText(this, "Email/Password incorrect", Toast.LENGTH_SHORT).show()
                        val progressCheck = ProgressDialog(this, R.style.Theme_MaterialComponents_Light_Dialog)
                        progressCheck.hide()
                    }
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == RC_SIGN_IN){
                val task : Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account)
                }catch(e: ApiException){
                    Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
            val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
            firebaseAuth.signInWithCredential(credential).addOnCompleteListener{
                if (it.isSuccessful){
                    startActivity(MainActivity.getLaunchService(this))
                }else{
                    Toast.makeText(this, "Google sign in is failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun configurationGoogleSignIn() {
            gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("356529986494-a8jfq6vrom36ucqcf4hjh9886k7d1t26.apps.googleusercontent.com")
                .requestEmail()
                .build()
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        }

        private fun setUpRequestUI() {
            btn_google_sign_in.setOnClickListener { signIn() }
        }

        private fun signIn() {
            val intent : Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }