package it.polito.mad3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
       var splashLogo= findViewById<ImageView>(R.id.splashLogo)
        splashLogo.alpha = 0F
        splashLogo.animate().setDuration(1500).alpha(1f).withEndAction {
            val intent = Intent(this,Authentication::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            finish()
        }


    }
}