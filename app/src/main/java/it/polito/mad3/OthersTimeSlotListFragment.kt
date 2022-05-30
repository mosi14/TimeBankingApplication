package it.polito.mad3

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad3.ViewModel.OtherTimeSlotListFragmentViewModel
import it.polito.mad3.ViewModel.SelectedSkillsViewModel
import it.polito.mad3.ViewModel.UserProfileViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat


class OthersTimeSlotListFragment : Fragment(), TimeSlotAdapter.OnItemClickListener {
    private var doubleBackToExitPressedOnce = false
    lateinit var otherTimeSlotsViewModel: OtherTimeSlotListFragmentViewModel
    lateinit var cancelSearchItem: MenuItem
    private var TAG = "MyActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        // This callback will only be called when MyFragment is at least Started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    if (doubleBackToExitPressedOnce) {
                        activity?.finish()
                        return
                    }
                    doubleBackToExitPressedOnce = true
                    Toast.makeText(context, "Please click BACK again to exit", Toast.LENGTH_SHORT)
                        .show()
                    Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun observeViewModel(view: View) {
        val recyclerViewTimeSlotsList = view.findViewById<RecyclerView>(R.id.O_rv_TimeSlotsList)
        val emptyMessage = view.findViewById<TextView>(R.id.O_empty_view)
        otherTimeSlotsViewModel =
            ViewModelProvider(this.requireActivity()).get(OtherTimeSlotListFragmentViewModel::class.java)
        otherTimeSlotsViewModel.getOthersTimeSlotFilteredList().observe(this.requireActivity(), Observer {
            if (otherTimeSlotsViewModel.getOthersTimeSlotList().value?.isEmpty() == true) {
                emptyMessage?.visibility = View.VISIBLE
                recyclerViewTimeSlotsList?.visibility = View.GONE
            } else {
                val timeSlotList = otherTimeSlotsViewModel.getOthersTimeSlotFilteredList().value?.toList()
                if (timeSlotList != null) {
                    emptyMessage?.visibility = View.GONE
                    recyclerViewTimeSlotsList?.visibility = View.VISIBLE
                    recyclerViewTimeSlotsList?.adapter = TimeSlotAdapter(timeSlotList, listener = this)
                    recyclerViewTimeSlotsList?.layoutManager = LinearLayoutManager(context)
                    recyclerViewTimeSlotsList?.setHasFixedSize(true)
                }
            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_others_time_slot_list, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel(view)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        var userProfile: UserProfileViewModel =
            ViewModelProvider(this.requireActivity()).get(UserProfileViewModel::class.java)
        userProfile.getUserProfile().observe(this.requireActivity()) {
            if (it != null && it.isEditable) {
                menu.clear()
                // Inflate the menu; this adds items to the action bar if it is present.
                inflater.inflate(R.menu.searchmenu, menu)
                val searchItem: MenuItem = menu.findItem(R.id.app_bar_search)
                cancelSearchItem = menu.findItem(R.id.cancel_app_bar_search)
                cancelSearchItem.isVisible = false
                otherTimeSlotsViewModel.getFilters().observe(this.requireActivity()) {
                    cancelSearchItem.isVisible =
                        otherTimeSlotsViewModel.getFilters().value?.count()!! > 0
                }
                cancelSearchItem.setOnMenuItemClickListener {
                    otherTimeSlotsViewModel.setFilters(mutableListOf())
                    true
                }
            }
        }
    }

    @SuppressLint("InflateParams", "CutPasteId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.app_bar_search -> {
                val popupView: View = layoutInflater.inflate(R.layout.fragment_search, null)

                val popupWindow = PopupWindow(
                    popupView,
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
                )
                popupWindow.isFocusable = true;
                popupWindow.setBackgroundDrawable(ColorDrawable())
                popupWindow.showAtLocation(
                    this.view, Gravity.NO_GRAVITY,
                    0, 0
                );

                val title: TextView =
                    popupView.findViewById(R.id.textView_Title_Se) as TextView
                val date: TextView =
                    popupView.findViewById(R.id.textView_Date_Se) as TextView
                val location: TextView =
                    popupView.findViewById(R.id.textView_Location_Se) as TextView
                val time: TextView =
                    popupView.findViewById(R.id.textView_Time_Se) as TextView
                val skill: TextView =
                    popupView.findViewById(R.id.textView_Skills_Se) as TextView

                val searchButton = popupView.findViewById(R.id.SearchButton) as Button
                val closeButton = popupView.findViewById(R.id.closeSearch) as Button

                //------------------------------Handle date and time pickers---------------------------------------------------
                val textviewDateEdit =
                    popupView.findViewById<TextInputEditText>(R.id.textView_Date_Se)
                handleDate(textviewDateEdit!!)
                //----------------------------------------------------------
                val timeTextEdit =
                    popupView.findViewById<TextInputEditText>(R.id.textView_Time_Se)
                handleTime(timeTextEdit)
                //----------------------------------------------------------

                if (otherTimeSlotsViewModel.getFilters().value != null)
                    for (field in otherTimeSlotsViewModel.getFilters().value!!) {
                        when (field.name) {
                            "date" -> {
                                date.text = field.value
                            }
                            "location" -> {
                                location.text = field.value
                            }
                            "time" -> {
                                time.text = field.value
                            }
                            "title" -> {
                                title.text= field.value
                            }
                            "skills" -> {
                                skill.text= field.value
                            }
                        }
                    }




                searchButton.setOnClickListener {
                    val listOfFilters: MutableList<FilterItem> = mutableListOf()

                    if (location.text.toString().isNotEmpty())
                        listOfFilters.add(
                            FilterItem(
                                "location",
                                location.text.toString(),
                                "and"
                            )
                        )
                    if (date.text.toString().isNotEmpty())
                        listOfFilters.add(
                            FilterItem(
                                "date",
                                date.text.toString(),
                                "and"
                            )
                        )
                    if (skill.text.toString().isNotEmpty())
                        listOfFilters.add(
                            FilterItem(
                                "skills",
                                skill.text.toString(),
                                "and"
                            )
                        )

                    if (title.text.toString().isNotEmpty())
                        listOfFilters.add(
                            FilterItem(
                                "title",
                                title.text.toString(),
                                "and"
                            )
                        )

                    if (time.text.toString().isNotEmpty())
                        listOfFilters.add(
                            FilterItem(
                                "time",
                                time.text.toString(),
                                "and"
                            )
                        )

                    otherTimeSlotsViewModel.setFilters(listOfFilters)

                    popupWindow.dismiss()
                }
                closeButton.setOnClickListener {
                    popupWindow.dismiss()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(model: TimeSlotItem) {
        // Navigate to detail fragment of that item
        val selectedSkillViewModel: SelectedSkillsViewModel =
            ViewModelProvider(this.requireActivity()).get(SelectedSkillsViewModel::class.java)
        selectedSkillViewModel.setSelectedTimeSlot(model)
        findNavController().navigate(R.id.action_othersTimeSlotListFragment_to_timeSlotDetailsFragment)

    }

    override fun onEditItemClick(model: TimeSlotItem) {
        val selectedSkillsViewModel: SelectedSkillsViewModel =
            ViewModelProvider(this.requireActivity()).get(SelectedSkillsViewModel::class.java)
        selectedSkillsViewModel.setSelectedTimeSlot(model)
        findNavController().navigate(R.id.action_othersTimeSlotListFragment_to_timeSlotEditFragment)
    }

    //----------------------------------Handle pickers-------------------------------------
    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun handleTime(timeText: TextInputEditText) {
        timeText.setText("09:00")
        val timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Select Time!")
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build()

        timeText.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_UP -> activity?.let { it1 ->
                    try {
                        timePicker.show(
                            it1.supportFragmentManager,
                            timePicker.toString()
                        )
                    } catch (e: Exception) {
                        // handler
                    }


                }
            }
            v?.onTouchEvent(event) ?: true
        }

        timePicker.addOnPositiveButtonClickListener {
            val newHour: Int = timePicker.hour
            val newMinute: Int = timePicker.minute
            timeText.setText("${newHour}:${newMinute}")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleDate(dateText: TextInputEditText) {
        val dateBuilder: MaterialDatePicker.Builder<*> = MaterialDatePicker.Builder.datePicker()

        val datePicker = dateBuilder
            .setTitleText("Select Date!")
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .build()

        dateText.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_UP -> activity?.let { it1 ->
                    datePicker.show(
                        it1.supportFragmentManager,
                        datePicker.toString()
                    )
                }
            }
            v?.onTouchEvent(event) ?: true
        }

        datePicker.addOnPositiveButtonClickListener {
            dateText.setText(datePicker.headerText)
        }
    }

    //--------------------------------------------------------------------------------------

}
