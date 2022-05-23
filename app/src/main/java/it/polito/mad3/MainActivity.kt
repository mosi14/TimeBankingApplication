package it.polito.mad3

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import it.polito.mad3.ViewModel.UserProfileViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var userEmail: String
    private lateinit var mAuth: FirebaseAuth
    private var popUpedOnce: Boolean = true
    private var TAG: String = "MyActivity"


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        //set our toolbar as the action bar
        setSupportActionBar(toolbar)
        userEmail = intent.extras?.getString(getString(R.string.EMAIL_Pref)).toString()


        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // setting title according to fragment
        navView.setupWithNavController(navController)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val navigationView = this.findViewById<NavigationView>(R.id.nav_view)

        loadProfileInfo()
        fillModels()
        mAuth = FirebaseAuth.getInstance()

        //showMessageForUnratedTrips()
        //loadProfileInfoJson()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear()
        menuInflater.inflate(R.menu.options_menu, menu)
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
    private fun loadProfileInfo() {
        val navigationView = this.findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        var userProfile: UserProfileViewModel =
            ViewModelProvider(this).get(UserProfileViewModel::class.java)

        var fullName: String? = null
        var nickName: String? = null
        var email: String? = null
        var location: String? = null
        var skills: String? = null
        var description: String? = null
        var imageUri: String? = null

//        val signoutbtn = headerView.findViewById<Button>(R.id.Signout)
//        signoutbtn.setOnClickListener {
//            mAuth.signOut()
//            val intent = Intent(this, SignIn::class.java)
//            startActivity(intent)
//            finish()
//        }

        userProfile.getUserProfile().observe(this) {
            if (it != null) {
                fullName = it.fullName
                nickName = it.nickName
                email = it.email
                location = it.location
                skills = it.skills
                description = it.description
                imageUri = it.imageUrl

                if (!fullName.isNullOrEmpty()) {
                    val fullNameTxt = headerView.findViewById<TextView>(R.id.navbar_profileName)
                    fullNameTxt.text = fullName
                }
                if (!nickName.isNullOrEmpty()) {
                    val profileNickName =
                        headerView.findViewById<TextView>(R.id.navbar_profileNickname)
                    profileNickName.text = nickName
                }
                if (!imageUri.isNullOrEmpty()) {
                    val imgUri: Uri = Uri.parse(imageUri)
                    val imagevu = headerView.findViewById<ImageView>(R.id.navbar_profileImageView)
                    setImage(imagevu, imageUri.toString(), 70, 70)

                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fillModels() {
        val db = FirebaseFirestore.getInstance()
       // val googleUID = FirebaseAuth.getInstance().currentUser!!.uid
        ////////////////// get current user data
        var userProfile: UserProfileViewModel =
            ViewModelProvider(this).get(UserProfileViewModel::class.java)
        userProfile.setUserEmail(userEmail)
        loadProfileDataFirestore(db, userEmail, this, true, "")
    }

    fun loadProfileDataFirestore(
        db: FirebaseFirestore,
        userEmail: String,
        owner: ViewModelStoreOwner,
        isMyProfile: Boolean,
        googleUID: String
    ) {
        val userProfile: UserProfileViewModel =
            ViewModelProvider(owner).get(UserProfileViewModel::class.java)
        db.collection("users").whereEqualTo("email", userEmail)
            .addSnapshotListener { value, error ->
                if (error != null)
                    throw error

                if (value != null) {
                    if (value.documents?.size!! > 0) {
                        for (document in value.documents)
                            userProfile.setUserProfile(
                                ProfileData(
                                    document["fullName"].toString(),
                                    document["nickName"].toString(),
                                    "",
                                    userEmail,
                                    "",
                                    document["location"].toString(),
                                    document["imageUrl"].toString(),
                                    document.id,
                                    isMyProfile,
                                    googleUID = document["googleUID"].toString(),
                                    temp = "",
                                ),
                            )
                    } else if (value.documents?.size!! == 0) {
                        // if it does not exist create it
                        if (userProfile.getUserProfile().value == null) {
                            val userProfileItem = ProfileData(
                                userEmail.split("@")[0],
                                userEmail.split("@")[0],
                                "",
                                userEmail,
                                "",
                                "Please update your location",
                                "",
                                "",
                                true,
                                googleUID = googleUID,
                                temp = "",
                            )
                            saveUserDB(userProfileItem)
                            userProfile.setUserProfile(userProfileItem)

                        }
                    }
                    //  userProfile.setUserBackupProfile(userProfile.getUserProfile().value!!)

                } else {
                    Log.w(ContentValues.TAG, "Error getting documents.", error)
                }
            }
    }
    /* load Json data from shared preferences */
    private fun loadProfileInfoJson() {
        val db = FirebaseFirestore.getInstance()
        val behi=db.collection("Booking").document("behi").get()
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

        if (!imageUri.isNullOrEmpty()) {
            val imagevu = headerView.findViewById<ImageView>(R.id.navbar_profileImageView)
            setImage(imagevu, imageUri, 70, 70)
        }
    }
}