package it.polito.mad3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ServiceAdapter(private val serviceList:List<ServiceItem>):RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {
    class ServiceViewHolder(v:View):RecyclerView.ViewHolder(v){
        val skill:TextView =v.findViewById(R.id.textView_Skills)

    }

    override fun onCreateViewHolder(parent : ViewGroup , viewType : Int) : ServiceViewHolder {
       val vg= LayoutInflater
            .from(parent.context)
            .inflate(R.layout.service_item,parent,false)
        return ServiceViewHolder(vg)
    }

    override fun onBindViewHolder(holder : ServiceViewHolder , position : Int) {
        holder.skill.text=serviceList[position].skill
    }

    override fun getItemCount() : Int {
        return serviceList.size
    }
}