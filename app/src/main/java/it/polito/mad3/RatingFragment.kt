package it.polito.mad3

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad3.ViewModel.BookedTimeSlotViewModel
import it.polito.mad3.ViewModel.UserProfileViewModel
import java.math.BigDecimal


class RatingFragment : Fragment() {

    private lateinit var currentActivity: FragmentActivity


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rating, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentActivity = requireActivity()
        observeViewModel(view)
    }


    private fun observeViewModel(myView: View) {
        val recyclerViewTimeSlotsList = myView.findViewById<RecyclerView>(R.id.rv_not_rated)
        val emptyMessage = myView.findViewById<TextView>(R.id.empty_view_not_rated)
        val rateLayout = myView.findViewById<ConstraintLayout>(R.id.rateLayout)

        val bookedTimeSlotsViewModel: BookedTimeSlotViewModel =
            ViewModelProvider(currentActivity).get(BookedTimeSlotViewModel::class.java)

        bookedTimeSlotsViewModel.getNotRatedTimeSlots().observe(currentActivity, Observer {
            if (it.isEmpty()) {
                emptyMessage?.visibility = View.VISIBLE
                recyclerViewTimeSlotsList?.visibility = View.GONE
            } else {

                if (it != null) {
                    emptyMessage?.visibility = View.GONE
                    recyclerViewTimeSlotsList?.visibility = View.VISIBLE
                    rateLayout.visibility = View.GONE
                    recyclerViewTimeSlotsList?.adapter = RatingAdapter(it, currentActivity, myView)
                    recyclerViewTimeSlotsList?.layoutManager = LinearLayoutManager(context)
                    recyclerViewTimeSlotsList?.setHasFixedSize(true)
                }
            }
        })
    }


}

class RatingAdapter(
    private val ratingList: List<TimeSlotItem>?, val owner: ViewModelStoreOwner,
    val adview: View
) : RecyclerView.Adapter<RatingAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RatingAdapter.MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.rating_time_slot_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RatingAdapter.MyViewHolder, position: Int) {
        val currentItem = ratingList!![position]

        holder.title.text = currentItem.title
        holder.date.text = currentItem.date
        holder.time.text = currentItem.time
        holder.location.text = currentItem.location
        holder.duration.text = currentItem.duration
        holder.skills.text = currentItem.skills
        holder.description.text = currentItem.description
        if (currentItem.isActive) {
            holder.status.setBackgroundColor(Color.parseColor("#6ab04c"))
        } else {
            holder.status.setBackgroundColor(Color.parseColor("#FF3B30"))
        }
        var userProfile: UserProfileViewModel =
            ViewModelProvider(owner).get(UserProfileViewModel::class.java)
        val rateLayout = adview.findViewById<ConstraintLayout>(R.id.rateLayout)
        val rateTitle = adview.findViewById<TextView>(R.id.lbl_rateTitle)
        val commentTextEdit = adview.findViewById<EditText>(R.id.edit_comment)
        val rateBar = adview.findViewById<RatingBar>(R.id.ratingBarComment)
        val rateButton = adview.findViewById<Button>(R.id.btn_rate)
        holder.card.setOnClickListener {
            rateLayout.visibility = View.VISIBLE
            rateTitle.text= ""
        }
        val isReceiverTripOrganizer=if(currentItem.userId==userProfile.getUserProfile().value?.id!!) "0" else "1"
        rateButton.setOnClickListener {
            sendRatingToServer(Rating(commentTextEdit.text.toString(),rateBar.rating.toString(),currentItem.userId,
                isReceiverTripOrganizer,userProfile.getUserProfile().value?.id!!,currentItem.id))
            rateLayout.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return ratingList!!.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val title: TextView  = itemView.findViewById(R.id.textView_title)
        val date: TextView  = itemView.findViewById(R.id.textView_Date)
        val time: TextView  = itemView.findViewById(R.id.textView_Time)
        val location: TextView  = itemView.findViewById(R.id.textView_Location)
        val duration : TextView = itemView.findViewById(R.id.textView_Duration)
        val skills : TextView = itemView.findViewById(R.id.textView_Skills)
        val description: TextView  = itemView.findViewById(R.id.textView_Description)
        val status: ImageView = itemView.findViewById(R.id.textView_status)
        val card: CardView = itemView.findViewById(R.id.card)
    }
}