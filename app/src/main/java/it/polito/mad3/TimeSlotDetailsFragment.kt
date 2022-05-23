package it.polito.mad3

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.Navigation.findNavController

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad3.ViewModel.SelectedSkillsViewModel
import it.polito.mad3.ViewModel.UserProfileViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class TimeSlotDetailsFragment : Fragment() {
    lateinit var model: TimeSlotItem
    private lateinit var userData: ProfileData
    private var editable = false
    private lateinit var fab: FloatingActionButton
    private var TAG = "MyActivity"
    private lateinit var currentActivity: FragmentActivity
    private lateinit var selectedSkillsViewModel: SelectedSkillsViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_time_slot_details, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        currentActivity = requireActivity()
        super.onViewCreated(view, savedInstanceState)
        selectedSkillsViewModel =
            ViewModelProvider(currentActivity).get(SelectedSkillsViewModel::class.java)
        val userProfile: UserProfileViewModel =
            ViewModelProvider(currentActivity).get(UserProfileViewModel::class.java)

        val db = FirebaseFirestore.getInstance()
        val linearLayoutInterestedPeople =
            view.findViewById<LinearLayout>(R.id.LinearLayout_listOfIntrested)
        val recyclerViewInterestedPeople = view.findViewById<RecyclerView>(R.id.rv_PeopleList)
        recyclerViewInterestedPeople.layoutManager = LinearLayoutManager(context)

        val title = view.findViewById<TextView>(R.id.textView_title)
        val date = view.findViewById<TextView>(R.id.textView_Date)
        val time = view.findViewById<TextView>(R.id.textView_Time)
        val location = view.findViewById<TextView>(R.id.textView_Location)
        val duration = view.findViewById<TextView>(R.id.textView_Duration)
        val skills = view.findViewById<TextView>(R.id.textView_Skills)
        val description = view.findViewById<TextView>(R.id.textView_Description)
        val rate = view.findViewById<TextView>(R.id.textView_Rate)
        fab = view.findViewById(R.id.fab)

        fab.setOnClickListener {
            sendInterestNotification(userData, model)
            Snackbar.make(
                it,
                "Notification  sent successfully.",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        selectedSkillsViewModel.getSelectedTimeSlot()
            .observe(viewLifecycleOwner) { timeSlotItem ->
                model = timeSlotItem
                //Log.i(TAG, "model 124: ${model}")
                editable = timeSlotItem.isEnabled
                /////////////////////////////////////////////////////////////////////////////////////////////////
                if (this::model.isInitialized) {
                    date.text = model.date
                    time.text = model.time
                    location.text = model.location
                    duration.text = model.duration
                    skills.text = model.skills
                    description.text = model.description
                }
                /////////////////////////////////////////////////////////////////////////////////////////////////
                db.collection("Booking")
                    .whereEqualTo("timeSlotId", timeSlotItem.id)
                    .get()
                    .addOnSuccessListener { documents ->
                        val result: MutableList<BookingData> = arrayListOf()
                        for (bookingDocument in documents) {
                            result.add(
                                BookingData(
                                    bookingDate =
                                    bookingDocument.data["bookingDate"] as String,
                                    bookingStatus =
                                    bookingDocument.data["bookingStatus"] as String,
                                    id =
                                    bookingDocument.data["id"] as String,
                                    timeSlotId =
                                    bookingDocument.data["timeSlotId"] as String,
                                    userId = bookingDocument.data["userId"] as String
                                )
                            )
                        }
                        if (result.size > 0) {
                            selectedSkillsViewModel.setInterestedPeopleInSkill(mutableListOf())
                            selectedSkillsViewModel.setInterestedPeopleInSkillBookings(result.toMutableList())
                        }
                    }
                    .addOnFailureListener { exception ->
                        throw exception
                    }
                selectedSkillsViewModel.getInterestedPeopleInSkillBookings()
                    .observe(viewLifecycleOwner) { interestedUsersBookings ->
                        for (interestedUsersBooking in interestedUsersBookings) {
                            db.collection("users")
                                .whereEqualTo("id", interestedUsersBooking?.userId.toString())
                                .get()
                                .addOnSuccessListener { value ->
                                    for (interestedUsersBookingItem in value?.documents!!) {
                                        selectedSkillsViewModel.removeByIdInterestedPersonInSkill(
                                            interestedUsersBookingItem["id"] as String
                                        )
                                        selectedSkillsViewModel.addInterestedPeopleInSkill(
                                            ProfileData(
                                                interestedUsersBookingItem["fullName"] as String,
                                                interestedUsersBookingItem["nickName"] as String,
                                                interestedUsersBookingItem["description"] as String,
                                                interestedUsersBookingItem["email"] as String,
                                                interestedUsersBookingItem["skills"] as String,
                                                interestedUsersBookingItem["location"] as String,
                                                interestedUsersBookingItem["imageUrl"] as String,
                                                interestedUsersBookingItem["id"] as String,
                                                interestedUsersBooking?.bookingStatus != "Accepted" &&
                                                        interestedUsersBooking?.bookingStatus != "Rejected",
                                                interestedUsersBookingItem["googleUID"] as String,
                                                interestedUsersBooking?.bookingStatus.toString()
                                            )
                                        )
                                    }

                                }
                        }
                    }

                selectedSkillsViewModel.getInterestedPeopleInSkill().observe(viewLifecycleOwner) {
                    recyclerViewInterestedPeople.adapter = ItemAdapter(
                        it.toList() as List<ProfileData>,
                        currentActivity,
                        currentActivity,
                        this
                    )
                }

                /////////////////////////////////////////////////////////////////////////////////////////////////
                userProfile.getUserProfile().observe(currentActivity) { userProfile ->
                    if (userProfile != null) {
                        userData = userProfile
                        fab.visibility =
                            if (userProfile.id != timeSlotItem.userId) View.VISIBLE else View.GONE
                    } else fab.visibility = View.GONE
                }
                /////////////////////////////////////////////////////////////////////////////////////////////////
                loadOtherProfileDataByIDFirestore(currentActivity, timeSlotItem.userId)
                selectedSkillsViewModel.getDriverProfile()
                    .observe(currentActivity) { teacherProfile ->
                        if (teacherProfile != null) {
                            title.text = teacherProfile.fullName
                            selectedSkillsViewModel.getDriverStarsAsDriver()
                                .observe(currentActivity) { driverStars ->
                                    title.text = driverStars.toString()
                                }

                            title.text = teacherProfile.fullName

                        }
                    }
                ////////////////////////////////////////////////////////////////////////////////////////////////
                linearLayoutInterestedPeople.visibility =
                    if (timeSlotItem.isEnabled) View.VISIBLE else View.GONE
            }
    }

    /* load edit profile menu layout*/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        var userProfile: UserProfileViewModel =
            ViewModelProvider(this.requireActivity()).get(UserProfileViewModel::class.java)
        userProfile.getUserProfile().observe(this.requireActivity(), {
            if (it != null && it.isEditable) {
                menu.clear()
            }
            if (editable) {
                inflater.inflate(R.menu.edit_time_slot_menu, menu)
                super.onCreateOptionsMenu(menu, inflater)
            }
        })
    }
    /* on edit profile selected */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.editTimeSlotActionBtn -> {
                findNavController().navigate(
                    R.id.action_timeSlotDetailsFragment_to_timeSlotEditFragment,
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    class ItemAdapter(
        private val interestedPeopleList: List<ProfileData>,
        val owner: ViewModelStoreOwner,
        val fragmentActivity: FragmentActivity,
        val recfragment: Fragment
    ) :
        RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
        inner class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            private val nameView: Button = v.findViewById<Button>(R.id.button_Name)
            private val acceptBtn: Button = v.findViewById<Button>(R.id.Button_Accept)
            private val rejectBtn: Button = v.findViewById<Button>(R.id.Button_Reject)

            @SuppressLint("SetTextI18n")
            fun bind(interestedPerson: ProfileData) {
                nameView.text = interestedPerson.fullName
                acceptBtn.isEnabled = interestedPerson.isEditable
                rejectBtn.isEnabled = interestedPerson.isEditable
                //////////////////////////////////////////////////////////////////////////////////
                if (interestedPerson.temp.toLowerCase(Locale.ROOT) == "rejected") {
                    rejectBtn.text = "Rejected"
                    acceptBtn.visibility = View.GONE


                } else if (interestedPerson.temp.toLowerCase(Locale.ROOT) == "accepted") {
                    acceptBtn.text = "Accepted"
                    rejectBtn.visibility = View.GONE
                }
                //////////////////////////////////////////////////////////////////////////////////
                nameView.setOnClickListener {
                    val userProfile: UserProfileViewModel =
                        ViewModelProvider(owner).get(UserProfileViewModel::class.java)
                    // set interested person profile in order to show in show profile fragment
                    userProfile.setOthersUserProfile(interestedPerson)
                    userProfile.setIsMyProfile(false)
                    getRatingFromServer(recfragment, interestedPerson.id, false)
                    findNavController(recfragment.requireView()).navigate(R.id.action_timeSlotDetailsFragment_to_showProfileFragment)
                }
                val selectedTripViewModel =
                    ViewModelProvider(fragmentActivity).get(SelectedSkillsViewModel::class.java)
                selectedTripViewModel.getSelectedTimeSlot()
                    .observe(fragmentActivity) { timeSlotItem ->
                        acceptBtn.setOnClickListener {
                            var model = timeSlotItem
                            addToBookedTimeSlotList(interestedPerson, model)
                            updateBookingState(interestedPerson, timeSlotItem, true)
                            acceptBtn.text = "Accepted"
                            rejectBtn.visibility = View.GONE
                            acceptBtn.isEnabled = false
                            Toast.makeText(
                                recfragment.requireContext(),
                                "You accepted user's booking!",
                                Toast.LENGTH_LONG
                            ).show()

                        }
                        rejectBtn.setOnClickListener {

                            updateBookingState(interestedPerson, timeSlotItem, false)
                            acceptBtn.isEnabled = false
                            rejectBtn.isEnabled = false
                            rejectBtn.text = "Rejected"
                            acceptBtn.visibility = View.GONE

                            Toast.makeText(
                                recfragment.requireContext(),
                                "You rejected user's interest!",
                                Toast.LENGTH_LONG
                            ).show()

                        }
                    }
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.interested_people_item, parent, false)
            return ItemViewHolder(layout)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.bind(interestedPeopleList[position])
        }

        override fun getItemCount(): Int {
            return interestedPeopleList.size
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::currentActivity.isInitialized) {
            var userProfile: UserProfileViewModel =
                ViewModelProvider(currentActivity).get(UserProfileViewModel::class.java)
            userProfile.setIsMyProfile(true)
        }
    }

}