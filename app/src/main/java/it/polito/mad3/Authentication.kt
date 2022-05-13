package it.polito.mad3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Authentication : AppCompatActivity() {

    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var gso : GoogleSignInOptions
    private lateinit var googleSignInClient : GoogleSignInClient
    private val TAG = "SignInActivity"
    private lateinit var auth : FirebaseAuth
    private val RC_SIGN_IN : Int = 0



    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.authentication)
        val signInButton = findViewById<Button>(R.id.googleSignIn)


        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this , gso)

        auth = Firebase.auth

        signInButton.setOnClickListener {
            //signIn()
            val intent = Intent(this , MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)

    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent , RC_SIGN_IN)
    }


    override fun onActivityResult(requestCode : Int , resultCode : Int , data : Intent?) {
        super.onActivityResult(requestCode , resultCode , data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java) !!
                Log.d(TAG , "firebaseAuthWithGoogle" + account.id)
                firebaseAuthWithGoogle(account.idToken !!)
            } catch (e : ApiException) {
                Log.w(TAG , "google sign in failed" , e)
            }

        }

    }

    private fun firebaseAuthWithGoogle(idToken : String) {
        val credential = GoogleAuthProvider.getCredential(idToken , null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("TAG" , "sign in with credential:success")
                    val user = auth.currentUser
                } else {
                    Log.w("TAG" , "sign in with credential:failur" , task.exception)
                    Toast.makeText(this , "Authentication failed" , Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }


            }

    }

    private fun updateUI(user : FirebaseUser?) {
        if(user != null) {
            Toast.makeText(this , "sign in" , Toast.LENGTH_SHORT).show()
//            val intent = Intent(this , MainActivity::class.java)
//            startActivity(intent)
        }

    }
}