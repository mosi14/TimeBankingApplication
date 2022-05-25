package it.polito.mad3

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad3.ViewModel.MyTimeSlotListFragmentViewModel
import it.polito.mad3.ViewModel.SelectedSkillsViewModel


class TimeSlotListFragment  : Fragment(), TimeSlotAdapter.OnItemClickListener {

    private var doubleBackToExitPressedOnce = false
    lateinit var myTimeSlotsViewModel: MyTimeSlotListFragmentViewModel
    private lateinit var currentActivity: FragmentActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentActivity = requireActivity()
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

        observeViewModel(view)

        val fab: FloatingActionButton = view.findViewById(R.id.M_fab)
        fab.setOnClickListener {
            val selectedSkillsViewModel: SelectedSkillsViewModel =
                ViewModelProvider(currentActivity).get(SelectedSkillsViewModel::class.java)
            selectedSkillsViewModel.setSelectedTimeSlot(
                TimeSlotItem(
                    "", "", "", "",
                    "", "", "", "", "", false,true
                )
            )
            findNavController().navigate(R.id.action_timeSlotListFragment_to_timeSlotEditFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.options_menu, menu)
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
        val selectedSkillsViewModel: SelectedSkillsViewModel =
            ViewModelProvider(currentActivity).get(SelectedSkillsViewModel::class.java)

        selectedSkillsViewModel.setSelectedTimeSlot(model)
        findNavController().navigate(R.id.action_timeSlotListFragment_to_timeSlotDetailsFragment)

    }

    override fun onEditItemClick(model: TimeSlotItem) {
        val selectedSkillsViewModel: SelectedSkillsViewModel =
            ViewModelProvider(currentActivity).get(SelectedSkillsViewModel::class.java)

        selectedSkillsViewModel.setSelectedTimeSlot(model)

        findNavController().navigate(R.id.action_timeSlotListFragment_to_timeSlotEditFragment)
    }


    private fun observeViewModel(myView: View) {
        val recyclerViewTimeSlotsList = myView.findViewById<RecyclerView>(R.id.rv_TimeSlotList)
        val emptyMessage = myView.findViewById<TextView>(R.id.M_empty_view)
        myTimeSlotsViewModel =
            ViewModelProvider(currentActivity).get(MyTimeSlotListFragmentViewModel::class.java)
        myTimeSlotsViewModel.getMyTimeSlotFilteredList().observe(currentActivity, Observer {
            if (myTimeSlotsViewModel.getMyTimeSlotList().value?.isEmpty() == true) {
                emptyMessage?.visibility = View.VISIBLE
                recyclerViewTimeSlotsList?.visibility = View.GONE
            } else {
                val timeSlotList = myTimeSlotsViewModel.getMyTimeSlotFilteredList().value?.toList()
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

}
