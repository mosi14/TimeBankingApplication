package it.polito.mad3

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        //set our toolbar as the action bar
        setSupportActionBar(toolbar)


        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // setting title according to fragment
        navView.setupWithNavController(navController)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        loadProfileInfoJson()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear()
        menuInflater.inflate(R.menu.nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        val navController = findNavController(R.id.nav_host_fragment)
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }


    override fun onSupportNavigateUp(): Boolean {
        loadProfileInfoJson()
        val navController = findNavController(R.id.nav_host_fragment)
        val nav = navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        return nav
    }

    /* load Json data from shared preferences */
    private fun loadProfileInfoJson() {
        val navigationView = this.findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)

        val sharedPref = this.getSharedPreferences(
            getString(R.string.PreferencesFilename), Context.MODE_PRIVATE
        )
        val json = sharedPref?.getString(getString(R.string.Profile_Pref), "")
        var fullName: String? = null
        var email: String? = null
        var imageUri: String? = null
        if (!json.equals("", ignoreCase = true)) {
            try {
                val jsonobj = Gson().fromJson(json, ProfileData::class.java)
                fullName = jsonobj.fullName
                email = jsonobj.email
                imageUri = jsonobj.imageUrl
            } catch (e: Exception) {
            }
        }
        if (!fullName.isNullOrEmpty()) {
            val fullNameTxt = headerView.findViewById<TextView>(R.id.navbar_profileName)
            fullNameTxt.text = fullName
        }
        if (!email.isNullOrEmpty()) {
            val emailTxt = headerView.findViewById<TextView>(R.id.navbar_profileEmail)
            emailTxt.text = email
        }
        if (!imageUri.isNullOrEmpty()) {
            val imagevu = headerView.findViewById<ImageView>(R.id.navbar_profileImageView)
            setImage(imagevu, imageUri, 70, 70)
        }
    }
}