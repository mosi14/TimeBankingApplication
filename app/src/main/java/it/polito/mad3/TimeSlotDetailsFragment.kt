package it.polito.mad3

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.math.BigDecimal
import java.util.ArrayList

class TimeSlotDetailsFragment : Fragment() {
    lateinit var model: TimeSlotItem
    private var timeSlotBundle: Bundle? = Bundle()
    private lateinit var listOfTimeSlots: MutableList<TimeSlotItem>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timeSlotBundle = arguments
        val selectedTime = arguments?.getString(getString(R.string.SELECTED_TIME)) ?: ""
        if (selectedTime.isNotEmpty())
            model = Gson().fromJson<TimeSlotItem>(selectedTime, TimeSlotItem::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_time_slot_details, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val timeSlotList = readOrGenerateTimeSlotList(5)
        model=timeSlotList[0]
        if (this::model.isInitialized) {
            listOfTimeSlots.find { it.id == model.id }?.also { model = it }
        }
        val title = view.findViewById<TextView>(R.id.tvTitle)
        val date = view.findViewById<TextView>(R.id.tvDateTime)
        val time = view.findViewById<TextView>(R.id.tvTime)
        val location = view.findViewById<TextView>(R.id.tvLocation)
        val duration = view.findViewById<TextView>(R.id.tvDuration)
        val description = view.findViewById<TextView>(R.id.tvDescription)

        if (this::model.isInitialized) {
            title.text = model.title
            description.text = model.description
            date.text = model.date
            time.text = model.time
            duration.text = model.duration
            location.text = model.location
        }
    }


    /* load edit profile menu layout*/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.edit_time_slot_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.editTimeSlotActionBtn -> {
                findNavController().navigate(
                    R.id.action_timeSlotDetailsFragment_to_timeSlotEditFragment
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun readOrGenerateTimeSlotList(size: Int): List<TimeSlotItem> {
        val gson = Gson()
        val isFilePresent =
            isFilePresent(this.requireActivity(), getString(R.string.TimeSlotListFileName))
        if (isFilePresent) {
            val jsonString = read(this.requireActivity(), getString(R.string.TimeSlotListFileName))
            val sType = object : TypeToken<List<TimeSlotItem>>() {}.type
            listOfTimeSlots = gson.fromJson<MutableList<TimeSlotItem>>(jsonString, sType)

            return listOfTimeSlots

        } else {
            listOfTimeSlots = ArrayList<TimeSlotItem>()
            for (i in 0 until size) {
                val item = TimeSlotItem(
                    i.toLong(),
                    "info system",
                    "teaching",
                    "2021/02/03",
                    "14:00",
                    "3",
                    "Turin",
                )
                listOfTimeSlots.add(item)
            }
            return listOfTimeSlots
        }
    }
    /* load edit profile menu layout*/


}