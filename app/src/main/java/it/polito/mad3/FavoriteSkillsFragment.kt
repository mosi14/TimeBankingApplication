package it.polito.mad3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad3.ViewModel.FavoriteSkillsViewModel


class FavoriteSkillsFragment : Fragment(),FavoriteSkillsAdapter.OnItemClickListener {

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
        return inflater.inflate(R.layout.fragment_favorite_skills_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel(view)
    }

    override fun onItemClick(model: TimeSlotItem) {
        val favoriteSkillsViewModel: FavoriteSkillsViewModel =
            ViewModelProvider(currentActivity).get(FavoriteSkillsViewModel::class.java)

        favoriteSkillsViewModel.setInterestedSkill(model)
        findNavController().navigate(R.id.action_favoriteSkillsFragment_to_timeSlotDetailsFragment)
    }

    private fun observeViewModel(myView: View) {
        val recyclerViewTimeSlotsList = myView.findViewById<RecyclerView>(R.id.rv_InterestedSkillsList)
        val emptyMessage = myView.findViewById<TextView>(R.id.empty_view)
        val favoriteSkillsViewModel: FavoriteSkillsViewModel =
            ViewModelProvider(currentActivity).get(FavoriteSkillsViewModel::class.java)

        favoriteSkillsViewModel.getInterestedSkills().observe(currentActivity, Observer {
            if (it.isEmpty()) {
                emptyMessage?.visibility = View.VISIBLE
                recyclerViewTimeSlotsList?.visibility = View.GONE
            } else {

                if (it != null) {
                    emptyMessage?.visibility = View.GONE
                    recyclerViewTimeSlotsList?.visibility = View.VISIBLE
                    recyclerViewTimeSlotsList?.adapter = FavoriteSkillsAdapter(it,listener = this)
                    recyclerViewTimeSlotsList?.layoutManager = LinearLayoutManager(context)
                    recyclerViewTimeSlotsList?.setHasFixedSize(true)
                }
            }
        })
    }

}