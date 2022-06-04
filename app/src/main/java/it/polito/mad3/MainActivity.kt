package it.polito.mad3

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import it.polito.mad3.ViewModel.*
import java.text.SimpleDateFormat
import java.util.*

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

        showMessageForUnratedTimeSlots()
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    fun makeListOfTimeSlotsNotRated(owner: ViewModelStoreOwner, fragmentActivity: FragmentActivity) {
        var ratingViewModel: RatingViewModel =
            ViewModelProvider(owner).get(RatingViewModel::class.java)
        val bookedTimeSlotViewModel: BookedTimeSlotViewModel =
            ViewModelProvider(this).get(BookedTimeSlotViewModel::class.java)
        ratingViewModel.getRatingtoOtherUsers().observe(fragmentActivity) { rating ->
            if (rating != null) {
                val listofRatedTimeSlots = rating.map { rate -> rate.timeSlotId }
                bookedTimeSlotViewModel.getBookedTimeSlots()
                    .observe(fragmentActivity) { bookedTimeSlot ->
                        if (bookedTimeSlot != null) {
                            val sdfD = SimpleDateFormat("MMMM dd, yyyy")
                            val currentDate = sdfD.format(Date())
                            val sdfT = SimpleDateFormat("hh:mm")
                            val currentTime = sdfT.format(Date())

                            bookedTimeSlotViewModel.setNotRatedTimeSlots(bookedTimeSlot

                                .filter { timeSlotItem -> timeSlotItem.id !in listofRatedTimeSlots }

                                .filter { timeSlotItem ->
                                    compareDatetime(
                                        currentDate,
                                        timeSlotItem.date,
                                        currentTime,
                                        timeSlotItem.time
                                    ) <= 0
                                }.toMutableList()
                            )
                        }
                    }
            }
        }
    }


    private fun showMessageForUnratedTimeSlots() {
        val bookedTimeSlotViewModel: BookedTimeSlotViewModel =
            ViewModelProvider(this).get(BookedTimeSlotViewModel::class.java)
        bookedTimeSlotViewModel.getNotRatedTimeSlots().observe(this) {
            // check if time size is not zero
            if (it.size > 0 && popUpedOnce) {
                popUpedOnce = false
                val alertDialog: AlertDialog = AlertDialog.Builder(this) //set icon
                    .setIcon(android.R.drawable.ic_dialog_alert) //set title
                    .setTitle("You have unrated timeSlots") //set message
                    .setMessage("Would you like to rate it now?") //set positive button
                    .setPositiveButton(
                        "Yes",
                        DialogInterface.OnClickListener { dialogInterface, i -> //set what would happen when positive button is clicked
                            try {
                                findNavController(R.id.nav_host_fragment).navigate(R.id.action_othersTimeSlotListFragment_to_ratingFragment)
                            } catch (e: Exception) {
                                try {
                                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_timeSlotListFragment_to_ratingFragment)
                                } catch (e: Exception) {
                                        try {
                                            findNavController(R.id.nav_host_fragment).navigate(R.id.action_timeSlotDetailsFragment_to_ratingFragment)
                                        } catch (e: Exception) {
                                            try {
                                                findNavController(R.id.nav_host_fragment).navigate(R.id.action_favoriteSkillsFragment_to_ratingFragment)
                                            } catch (e: Exception) {
                                                try {
                                                    findNavController(R.id.nav_host_fragment).navigate(
                                                        R.id.action_bookedTimeSlotsListFragment_to_ratingFragment
                                                    )
                                                } catch (e: Exception) {
                                                    try {
                                                        findNavController(R.id.nav_host_fragment).navigate(
                                                            R.id.action_showProfileFragment_to_ratingFragment
                                                        )
                                                    } catch (e: Exception) {
                                                        try {
                                                            findNavController(R.id.nav_host_fragment).navigate(
                                                                R.id.action_editProfileFragment_to_ratingFragment
                                                            )
                                                        } catch (e: Exception) {
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                }
                            }
                        }) //set negative button
                    .setNegativeButton(
                        "No",
                        DialogInterface.OnClickListener { dialogInterface, i -> //set what should happen when negative button is clicked
                            Toast.makeText(
                                applicationContext,
                                "You can access Rating from the time slot",
                                Toast.LENGTH_LONG
                            ).show()
                        })
                    .show()
            } else {
                popUpedOnce = false
            }
        }

    }


    override fun onSupportNavigateUp(): Boolean {

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

        val signoutbtn = headerView.findViewById<Button>(R.id.Signout)
        signoutbtn.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(this, Authentication::class.java)
            startActivity(intent)
            finish()
        }

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
         val googleUID = FirebaseAuth.getInstance().currentUser!!.uid
        ////////////////// get current user data
        var userProfile: UserProfileViewModel =
            ViewModelProvider(this).get(UserProfileViewModel::class.java)
        loadProfileInfo()
        userProfile.setUserEmail(userEmail)
        userProfile.setIsMyProfile(true)
        loadProfileDataFirestore(db, userEmail, this, true, googleUID)

        var otherTimeSlotViewModel: OtherTimeSlotListFragmentViewModel =
            ViewModelProvider(this).get(OtherTimeSlotListFragmentViewModel::class.java)

        val favoriteSkillsViewModel: FavoriteSkillsViewModel =
            ViewModelProvider(this).get(FavoriteSkillsViewModel::class.java)

        val bookedTimeSlotViewModel: BookedTimeSlotViewModel =
            ViewModelProvider(this).get(BookedTimeSlotViewModel::class.java)
        /////////////// Get other TimeSlot //////////////////////////////

        userProfile.getUserProfile().observe(this) {
            if (it != null) {
                db.collection("TimeSlots")
                    .whereNotEqualTo("userId", userProfile.getUserProfile().value?.id)
                    .addSnapshotListener { value, error ->
                        if (error != null)
                            throw error

                        if (value != null) {
                            var result: MutableList<TimeSlotItem> = arrayListOf()
                            for (document in value.documents) {

                                val timeSlotId: Any? = document.data!!["id"]
                                val timeSlotUserId: Any? = document.data!!["userId"]
                                val timeSlotTitle: Any? = document.data!!["title"]
                                val timeSlotLocation: Any? = document.data!!["location"]
                                val timeSlotSkills: Any? = document.data!!["skills"]
                                val timeSlotTime: Any? = document.data!!["time"]
                                val timeSlotDate: Any? = document.data!!["date"]
                                val timeSlotStatus: Any? = document.data!!["status"]
                                val timeSlotDescription: Any? = document.data!!["description"]
                                val timeSlotDuration: Any? = document.data!!["duration"]
                                val timeSlotIsActive: Any? = document.data!!["isActive"]


                                if (timeSlotIsActive.toString().toBoolean())
                                    result.add(
                                        TimeSlotItem(
                                            timeSlotId as String,
                                            timeSlotUserId as String,
                                            timeSlotTitle as String,
                                            timeSlotDescription as String,
                                            timeSlotDate as String,
                                            timeSlotTime as String,
                                            timeSlotDuration as String,
                                            timeSlotLocation as String,
                                            timeSlotSkills as String,
                                            false,
                                            timeSlotIsActive.toString().toBoolean(),
                                        )
                                    )

                            }


                            otherTimeSlotViewModel.setAllTimeSlots(result)

                            otherTimeSlotViewModel.getAllTimeSlots()
                            if (!otherTimeSlotViewModel.getFilters().value.isNullOrEmpty())
                                result =
                                    filterTimeSlots(
                                        result,
                                        otherTimeSlotViewModel.getFilters().value!!
                                    )
                            otherTimeSlotViewModel.setOthersTimeSlotFilteredList(result)

                            ///////////////////////// get interested timeSlots //////////////////////////////////////
                            db.collection("Booking")
                                .whereEqualTo("userId", userProfile.getUserProfile().value?.id)
                                .addSnapshotListener { value, error ->
                                    if (error != null)
                                        throw error

                                    if (value != null) {
                                        var interestedBookResult: MutableList<BookingData> =
                                            mutableListOf()
                                        var interestedTimeSlotIdResult: MutableList<String> =
                                            mutableListOf()
                                        for (document in value.documents) {

                                            val bookId: Any? = document.data!!["id"]
                                            val bookUserId: Any? = document.data!!["userId"]
                                            val bookTimeSlotId: Any? = document.data!!["timeSlotId"]
                                            val bookStatus: Any? = document.data!!["bookingStatus"]
                                            val bookDate: Any? = document.data!!["bookingDate"]
                                            interestedTimeSlotIdResult.add(bookTimeSlotId.toString())
                                            interestedBookResult.add(
                                                BookingData(
                                                    bookingDate = bookDate.toString(),
                                                    bookingStatus = bookStatus.toString(),
                                                    id = bookId.toString(),
                                                    timeSlotId = bookTimeSlotId.toString(),
                                                    userId = bookUserId.toString()
                                                )
                                            )
                                        }
                                        // find the list of interests in booking collection
                                        val bookedTimeSLotIds =
                                            interestedBookResult.filter { bookData -> bookData.bookingStatus == "Accepted" }
                                                .map { it.timeSlotId }
                                        // filter list of other time slots that their booking is not accepted as interested time slots
                                        favoriteSkillsViewModel.setInterestedSkillsList(result.filter { t -> t.id in interestedTimeSlotIdResult }
                                            .filter { it.id !in bookedTimeSLotIds }
                                            .toMutableList())
// filter                              // list of other time Slots that their booking is accepted as booked time slots
                                        bookedTimeSlotViewModel.setBookedTimeSlotsList(
                                            result.filter { t -> t.id in interestedTimeSlotIdResult }
                                                .filter { it.id in bookedTimeSLotIds }
                                                ?.toMutableList()!!
                                        )
                                    }
                                }
                            ///////////////////////// get favorite time slots //////////////////

                        } else {
                            Log.w(ContentValues.TAG, "Error getting documents.")

                        }
                    }

                ////////////////// get ratings from server //////////////////////////////
                loadRatedTimeSlots(this, userProfile.getUserProfile().value?.id!!)
                getRatingFromServer(this, userProfile.getUserProfile().value?.id!!, true)
                ////////////////// get ratings from server //////////////////////////////
                makeListOfTimeSlotsNotRated(this, this)
            }
        }

        /////////////////Get Other Time Slots ///////////////////////////

        /////////////// Get My Time Slots //////////////////////////////
        userProfile.getUserProfile().observe(this) {
            if (it != null) {
                val myTimeSlotsViewModel: MyTimeSlotListFragmentViewModel =
                    ViewModelProvider(this).get(MyTimeSlotListFragmentViewModel::class.java)

                db.collection("TimeSlots")
                    .whereEqualTo("userId", userProfile.getUserProfile().value?.id)
                    .addSnapshotListener { value, error ->
                        if (error != null)
                            throw error

                        var result: MutableList<TimeSlotItem> = arrayListOf()
                        for (document in value?.documents!!) {
                            val timeSlotId: Any? = document.data!!["id"]
                            val timeSlotUserId: Any? = document.data!!["userId"]
                            val timeSlotTitle: Any? = document.data!!["title"]
                            val timeSlotLocation: Any? = document.data!!["location"]
                            val timeSlotSkills: Any? = document.data!!["skills"]
                            val timeSlotTime: Any? = document.data!!["time"]
                            val timeSlotDate: Any? = document.data!!["date"]
                            val timeSlotDescription: Any? = document.data!!["description"]
                            val timeSlotDuration: Any? = document.data!!["duration"]
                            val timeSlotStatus: Any? = document.data!!["status"]
                            val timeSlotIsActive: Any? = document.data!!["isActive"]

                            result.add(
                                TimeSlotItem(
                                    timeSlotId as String,
                                    timeSlotUserId as String,
                                    timeSlotTitle as String,
                                    timeSlotDescription as String,
                                    timeSlotDate as String,
                                    timeSlotTime as String,
                                    timeSlotDuration as String,
                                    timeSlotLocation as String,
                                    timeSlotSkills as String,
                                    true,
                                    timeSlotIsActive.toString().toBoolean(),

                                    )
                            )
                        }

                        myTimeSlotsViewModel.setMyTimeSlotCount(result.count())
                        myTimeSlotsViewModel.setAllTimeSlots(result)
                        myTimeSlotsViewModel.getAllTimeSlots()

                        if (!myTimeSlotsViewModel.getFilter().value.isNullOrEmpty())
                            result = filterTimeSlots(result, myTimeSlotsViewModel.getFilter().value!!)
                        myTimeSlotsViewModel.setMyTimeSlotFilteredList(result)
                    }
            }
        }
        /////////////// Get My Time Slots //////////////////////////////

        ///////////////// filter and load Time Slots ////////////////////////////////
        otherTimeSlotViewModel.getFilters().observe(this) {
           var filters= otherTimeSlotViewModel.getFilters().value
            var result = otherTimeSlotViewModel.getAllTimeSlots()
            if (!filters.isNullOrEmpty() && result != null) {
                result = filterTimeSlots(result!!, filters!!)
                otherTimeSlotViewModel.setOthersTimeSlotFilteredList(result!!)
            } else {
                otherTimeSlotViewModel.setOthersTimeSlotFilteredList(result!!)
            }
        }
        ///////////////// filer and load Time slots //////////////////////////////////


    }
    private fun filterTimeSlots(
        result: MutableList<TimeSlotItem>,
        filterList: MutableList<FilterItem>
    ): MutableList<TimeSlotItem> {
        var lastResult = result
        for (filter in filterList) {
            when (filter.name) {
                "date" -> {
                    if (filter.operatorType.equals("and", true))
                        lastResult =
                            lastResult.filter {
                                it.date.contains(
                                    filter.value,
                                    true
                                )
                            }
                                .toMutableList()
                    else lastResult = lastResult.union(lastResult.filter {
                        it.date.contains(
                            filter.value,
                            true
                        )
                    }).toMutableList()
                }
                "time" -> {
                    if (filter.operatorType.equals("and", true))
                        lastResult =
                            lastResult.filter {
                                it.time.contains(
                                    filter.value,
                                    true
                                )
                            }
                                .toMutableList()
                    else lastResult = lastResult.union(lastResult.filter {
                        it.time.contains(
                            filter.value,
                            true
                        )
                    }).toMutableList()
                }
                "location" -> {
                    if (filter.operatorType.equals("and", true))
                        lastResult =
                            lastResult.filter { it.location.contains(filter.value, true) }
                                .toMutableList()
                    else lastResult = lastResult.union(lastResult.filter {
                        it.location.contains(
                            filter.value,
                            true
                        )
                    }).toMutableList()
                }
                "title" -> {
                    if (filter.operatorType.equals("and", true))
                        lastResult =
                            lastResult.filter { it.title.contains(filter.value, true) }
                                .toMutableList()
                    else lastResult = lastResult.union(lastResult.filter {
                        it.title.contains(
                            filter.value,
                            true
                        )
                    }).toMutableList()
                }
                "skills" -> {
                    if (filter.operatorType.equals("and", true))
                        lastResult =
                            lastResult.filter { it.skills.contains(filter.value, true) }
                                .toMutableList()
                    else lastResult = lastResult.union(lastResult.filter {
                        it.skills.contains(
                            filter.value,
                            true
                        )
                    }).toMutableList()
                }
                "description" -> {
                    if (filter.operatorType.equals("and", true))
                        lastResult =
                            lastResult.filter { it.description.contains(filter.value, true) }
                                .toMutableList()
                    else lastResult = lastResult.union(lastResult.filter {
                        it.description.contains(
                            filter.value,
                            true
                        )
                    }).toMutableList()
                }
                "duration" -> {
                    if (filter.operatorType.equals("and", true))
                        lastResult =
                            lastResult.filter { it.duration.contains(filter.value, true) }
                                .toMutableList()
                    else lastResult = lastResult.union(lastResult.filter {
                        it.duration.contains(
                            filter.value,
                            true
                        )
                    }).toMutableList()
                }

            }

        }

        return lastResult
    }


}
