package it.polito.mad3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val userName = findViewById<TextView>(R.id.userName)
        val userEmail = findViewById<TextView>(R.id.userEmail)
        val userImage = findViewById<ImageView>(R.id.userImage)
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        //displaying current user info
        userName.text = currentUser?.displayName
        userEmail.text = currentUser?.email
        Glide.with(this).load(currentUser?.photoUrl).into(userImage);

        //signOut
        logoutBtn.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(this,Authentication::class.java)
            startActivity(intent)
            finish()
        }

    }
}