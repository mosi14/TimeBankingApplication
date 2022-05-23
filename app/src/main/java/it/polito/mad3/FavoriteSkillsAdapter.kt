package it.polito.mad3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView


class FavoriteSkillsAdapter(
    private val interestedTimeSlotsList: List<TimeSlotItem>?,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<FavoriteSkillsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteSkillsAdapter.MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.time_slot_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FavoriteSkillsAdapter.MyViewHolder, position: Int) {
        val currentItem = interestedTimeSlotsList!![position]
        //setImage(holder.pPhoto, currentItem.pPhoto, 150, 150)

        holder.title.text = currentItem.title
        holder.date.text = currentItem.date
        holder.time.text = currentItem.time
        holder.location.text = currentItem.location
        holder.duration.text = currentItem.duration
        holder.description.text = currentItem.description
        holder.skills.text = currentItem.skills


     //   holder.editButton.visibility = if (currentItem.isEnabled) View.VISIBLE else View.GONE

//        if (currentItem.isActive) {
//            holder.status.setBackgroundColor(Color.parseColor("#6ab04c"))
//        } else {
//            holder.status.setBackgroundColor(Color.parseColor("#FF3B30"))
//        }
        if(currentItem!=null)
            holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
    return interestedTimeSlotsList!!.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //val pPhoto: ImageView = itemView.findViewById(R.id.pPhoto)
        val title = itemView.findViewById<TextView>(R.id.textView_title)
        val description = itemView.findViewById<TextView>(R.id.textView_Description)
        val skills = itemView.findViewById<TextView>(R.id.textView_Skills)
        val date = itemView.findViewById<TextView>(R.id.textView_Date)
        val time = itemView.findViewById<TextView>(R.id.textView_Time)
        val duration = itemView.findViewById<TextView>(R.id.textView_Duration)
        val location = itemView.findViewById<TextView>(R.id.textView_Location)
        val editButton = itemView.findViewById<Button>(R.id.btn_edit)
        private val card = itemView.findViewById<CardView>(R.id.card)

        fun bind(model: TimeSlotItem) {
        }
    }

    interface OnItemClickListener {
        fun onItemClick(model: TimeSlotItem)
    }

}