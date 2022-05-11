package it.polito.mad3

import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.ArrayList


class TimeSlotListFragment  : Fragment(), TimeSlotAdapter.OnItemClickListener {

    private lateinit var listOfTimeSlots: MutableList<TimeSlotItem>
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This callback will only be called when MyFragment is at least Started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    if (doubleBackToExitPressedOnce) {
                        activity?.finish()
                        return
                    }
                    doubleBackToExitPressedOnce = true
                    Toast.makeText(context, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
                    Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time_slot_list, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv_timeSlotList = view.findViewById<RecyclerView>(R.id.rv_ServiceList)
        val empty_msg = view.findViewById<TextView>(R.id.empty_view)

        val timeSlotList = generateTimeSlotList(size = 5)
        if (timeSlotList.isEmpty()) {
            empty_msg.visibility = View.VISIBLE
        } else {
            empty_msg.visibility = View.GONE
            rv_timeSlotList.adapter = TimeSlotAdapter(timeSlotList, listener = this)
            rv_timeSlotList.layoutManager = LinearLayoutManager(context)
            rv_timeSlotList.setHasFixedSize(true)
        }
        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            val bundle = Bundle()
            val item = TimeSlotItem(
                listOfTimeSlots.count().toLong(), "", "", "", "", "", "",

            )
            bundle.putString(getString(R.string.SELECTED_TIME), Gson().toJson(item))
            findNavController().navigate(R.id.action_timeSlotListFragment_to_timeSlotEditFragment, bundle)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.nav_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Handle item selection
        return when (item.itemId) {
            R.id.editProfileMenu -> {

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(model: TimeSlotItem) {

        val bundle = Bundle()
        val jsonModel = Gson().toJson(model)
        bundle.putString(getString(R.string.SELECTED_TIME), jsonModel)
        // Navigate to detail fragment of that item
        // To Do: data should pass through bundle
        findNavController().navigate(R.id.action_timeSlotListFragment_to_timeSlotDetailsFragment, bundle)

    }

    override fun onEditItemClick(model: TimeSlotItem) {
        val bundle = Bundle()
        bundle.putString(getString(R.string.SELECTED_TIME), Gson().toJson(model))
        findNavController().navigate(R.id.action_timeSlotListFragment_to_timeSlotEditFragment, bundle)
    }

    private fun generateTimeSlotList(size: Int): List<TimeSlotItem> {
        val gson = Gson()
        val items = arrayOf(
            TimeSlotItem(
                1,
                "Mobile Application",
                "learning android studio skills so fast",
                "Apr 24 2021",
                "14:00",
                "2",
                "Torino",

                ), TimeSlotItem(
                2,
                "web Application 2",
                "learning android studio skills so fast",
                "Apr 24 2021",
                "12:00",
                "2",
                "Firenze",

                ),
            TimeSlotItem(
                3,
                "Software Engineering",
                "how to design application",
                "Apr 29 2021",
                "11:00",
                "5",
                "Roma",

                ),
            TimeSlotItem(
                4,
                "information",
                "",
                "May 4 2021",
                "15:00",
                "3",
                "Como",

                ),
            TimeSlotItem(
                5,
                "c#",
                "learn programming language",
                "May 6 2021",
                "15:00",
                "4",
                "Pisa",

                )
        )
        val isFilePresent =
            isFilePresent(this.requireActivity(), getString(R.string.TimeSlotListFileName))
        if (isFilePresent) {
            val jsonString = read(this.requireActivity(), getString(R.string.TimeSlotListFileName))
            val sType = object : TypeToken<List<TimeSlotItem>>() {}.type
            listOfTimeSlots = gson.fromJson<MutableList<TimeSlotItem>>(jsonString, sType)

            return listOfTimeSlots
            //do the json parsing here and do the rest of functionality of app
        } else {
            listOfTimeSlots = ArrayList<TimeSlotItem>()
            for (i in 0 until size) {
                listOfTimeSlots.add(items[i])
            }

            saveListToFile()
            return listOfTimeSlots
        }
    }

    fun saveListToFile() {
        val gson = Gson()
        val isFileCreated = create(
            this.requireActivity(),
            getString(R.string.TimeSlotListFileName),
            gson.toJson(listOfTimeSlots)
        )
        if (isFileCreated) {
            //proceed with storing the first todo  or show ui
        } else {
            //show error or try again.
        }
    }

}
