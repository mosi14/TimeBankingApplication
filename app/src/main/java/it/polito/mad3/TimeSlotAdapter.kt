package it.polito.mad3

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.math.BigDecimal


class
TimeSlotAdapter(
    private val timeSlotList: List<TimeSlotItem>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.time_slot_item, parent, false)
        return TimeSlotViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val currentItem = timeSlotList[position]
        holder.id = position
        holder.title.text = currentItem.title
        holder.date.text = currentItem.date
        holder.time.text = currentItem.time
        holder.location.text = currentItem.location
        holder.duration.text = currentItem.duration
        holder.description.text = currentItem.description

        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return timeSlotList.size
    }

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val title = itemView.findViewById<TextView>(R.id.textView_title)
        val description = itemView.findViewById<TextView>(R.id.textView_Description)
        val date = itemView.findViewById<TextView>(R.id.textView_Date)
        val time = itemView.findViewById<TextView>(R.id.textView_Time)
        val duration = itemView.findViewById<TextView>(R.id.textView_Duration)
        val location = itemView.findViewById<TextView>(R.id.textView_Location)
        val editButton = itemView.findViewById<Button>(R.id.btn_edit)
        val card = itemView.findViewById<CardView>(R.id.card)
        var id = 0

        fun bind(model: TimeSlotItem) {
            card.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    listener.onItemClick(model)
            }

            editButton.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    listener.onEditItemClick(model)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(model: TimeSlotItem)
        fun onEditItemClick(model: TimeSlotItem)
    }

}