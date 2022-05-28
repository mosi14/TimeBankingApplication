package it.polito.mad3

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.DateFormat
import java.text.SimpleDateFormat


class SearchFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleDate(view)
    }

    @SuppressLint("SimpleDateFormat")
    private val dateFormatter: DateFormat = SimpleDateFormat("MMMM dd, yyyy")


    @SuppressLint("ClickableViewAccessibility")
    private fun handleDate(view: View) {
        val arrivalText = view.findViewById<TextInputEditText>(R.id.textView_Date_Se)
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date!")
            .build()

        arrivalText.setOnTouchListener { v, event ->
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
            arrivalText.setText(dateFormatter.format(it))
            arrivalText.error = null
        }
    }

}