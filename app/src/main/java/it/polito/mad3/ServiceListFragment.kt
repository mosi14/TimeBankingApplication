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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.ArrayList


class ServiceListFragment  : Fragment(R.layout.fragment_service_list)  {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv_serviceList = view.findViewById<RecyclerView>(R.id.rv_ServiceList)
        val empty_msg = view.findViewById<TextView>(R.id.empty_view)


        val serviceList = generateServiceList(size = 6)
        if (serviceList.isEmpty()) {
            empty_msg.visibility = View.VISIBLE
        } else {
            empty_msg.visibility = View.GONE
            rv_serviceList.adapter = ServiceAdapter(serviceList)
            rv_serviceList.layoutManager = LinearLayoutManager(this.context)
            rv_serviceList.setHasFixedSize(true)
        }
    }

    private fun generateServiceList(size : Int) : List<ServiceItem> {
        val items = listOf(


            ServiceItem(
                1,
                "Mobile Application",
                "learning android studio skills so fast",
                "Apr 24 2021",
                "14:00",
                "2",
                "Torino",

                ), ServiceItem(
                2,
                "web Application 2",
                "learning android studio skills so fast",
                "Apr 24 2021",
                "12:00",
                "2",
                "Firenze",

                ),
            ServiceItem(
                3,
                "Software Engineering",
                "how to design application",
                "Apr 29 2021",
                "11:00",
                "5",
                "Roma",

                ),
            ServiceItem(
                4,
                "information",
                "",
                "May 4 2021",
                "15:00",
                "3",
                "Como",

                ),
            ServiceItem(
                5,
                "c#",
                "learn programming language",
                "May 6 2021",
                "15:00",
                "4",
                "Pisa",

                ),

        )

        return items

    }


}
