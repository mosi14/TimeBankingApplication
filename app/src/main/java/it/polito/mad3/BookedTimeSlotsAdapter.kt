package it.polito.mad3

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView


class BookedTimeSlotsAdapter(
    private val bookedTimeSlotsList: List<TimeSlotItem>?,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<BookedTimeSlotsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookedTimeSlotsAdapter.MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.time_slot_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BookedTimeSlotsAdapter.MyViewHolder, position: Int) {
        val currentItem = bookedTimeSlotsList!![position]

        holder.title.text = currentItem.title
        holder.date.text = currentItem.date
        holder.time.text = currentItem.time
        holder.location.text = currentItem.location
        holder.duration.text = currentItem.duration
        holder.description.text = currentItem.description
        holder.skills.text = currentItem.skills

        holder.editButton.visibility = if (currentItem.isEnabled) View.VISIBLE else View.GONE

        if (currentItem.isActive) {
            holder.status.setBackgroundColor(Color.parseColor("#6ab04c"))
        } else {
            holder.status.setBackgroundColor(Color.parseColor("#FF3B30"))
        }
        if(currentItem!=null)
            holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return bookedTimeSlotsList!!.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val title:TextView = itemView.findViewById(R.id.textView_title)
        val description:TextView  = itemView.findViewById(R.id.textView_Description)
        val skills:TextView  = itemView.findViewById(R.id.textView_Skills)
        val date:TextView  = itemView.findViewById(R.id.textView_Date)
        val time:TextView  = itemView.findViewById(R.id.textView_Time)
        val duration:TextView  = itemView.findViewById(R.id.textView_Duration)
        val location:TextView  = itemView.findViewById(R.id.textView_Location)
        val editButton:Button = itemView.findViewById(R.id.btn_edit)
        private val card = itemView.findViewById<CardView>(R.id.card)
        val status: ImageView = itemView.findViewById(R.id.textView_status)



        fun bind(model: TimeSlotItem) {
        }
    }
    interface OnItemClickListener {
        fun onItemClick(model: TimeSlotItem)
    }
}