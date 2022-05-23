package it.polito.mad3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
            signIn()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)

    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        getResult.launch(signInIntent)
    }

    private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

  //     if(it.resultCode == RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)

            try{
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            }catch (e: ApiException){
                Log.d("sign in","failed")
            }
  //      }


    }

    private fun firebaseAuthWithGoogle(idToken : String) {
        val credential = GoogleAuthProvider.getCredential(idToken , null)
        GlobalScope.launch(Dispatchers.IO){
            val auth = auth.signInWithCredential(credential).await()
            val firebaseUser = auth.user

            withContext(Dispatchers.Main){
                updateUI(firebaseUser)
            }
        }

    }

    private fun updateUI(user : FirebaseUser?) {
    //    if(user != null) {
            Toast.makeText(this , "sign in" , Toast.LENGTH_SHORT).show()
            val intent = Intent(this , MainActivity::class.java)
            startActivity(intent)
   //     }

    }
}