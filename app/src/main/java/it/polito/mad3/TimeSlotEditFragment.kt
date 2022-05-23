package it.polito.mad3


import android.annotation.SuppressLint
import android.app.PendingIntent.getActivity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.ViewTreeObserver.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import  it.polito.mad3.ViewModel.SelectedSkillsViewModel
import  it.polito.mad3.ViewModel.UserProfileViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
class TimeSlotEditFragment : Fragment() {
    private lateinit var currentView: View
    private lateinit var model: TimeSlotItem
    private var currentTimeSlotId = ""
    lateinit var date: TextView
    lateinit var time: TextView
    lateinit var location: TextView
    lateinit var timeSlotStatus: SwitchMaterial
    private var durationValue = 0
    private lateinit var currentActivity: FragmentActivity
    private lateinit var selectedSkillsViewModel: SelectedSkillsViewModel

    lateinit var title: TextView
    lateinit var description: TextView
    lateinit var skills: TextView
    lateinit var duration: TextInputEditText

    private lateinit var currentContext: Context
    private var TAG: String = "MyActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time_slot_edit, container, false)
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun handleDate(view: View) {
        val dateBuilder: MaterialDatePicker.Builder<*> = MaterialDatePicker.Builder.datePicker()
        val vText = view.findViewById<TextInputEditText>(R.id.etDate)
        val datePicker = dateBuilder
            .setTitleText("Select Date!")
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .build()

        vText.setOnTouchListener { v, event ->
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
            vText.setText(datePicker.headerText)
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun handleTime(view: View) {
        val departureTimeText = view.findViewById<TextInputEditText>(R.id.etTime)
        val timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Select Departure Time!")
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build()

        departureTimeText.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_UP -> activity?.let { it1 ->
                    timePicker.show(
                        it1.supportFragmentManager,
                        timePicker.toString()
                    )
                }
            }
            v?.onTouchEvent(event) ?: true
        }

        timePicker.addOnPositiveButtonClickListener {
            val newHour: Int = timePicker.hour
            val newMinute: Int = timePicker.minute
            departureTimeText.setText("${newHour}:${newMinute}")
        }
    }

    @SuppressLint("CutPasteId", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        currentView = view
        currentActivity = requireActivity()
        currentContext = requireContext()
        title = view.findViewById(R.id.etTitle)
        description = view.findViewById(R.id.etDescription)
        date = view.findViewById(R.id.etDate)
        time = view.findViewById<TextInputEditText>(R.id.etTime)
        skills = view.findViewById(R.id.etSkills)
        description = view.findViewById(R.id.etDescription)
        duration = view.findViewById<TextInputEditText>(R.id.etDuration)
        location = view.findViewById(R.id.etLocation)


        selectedSkillsViewModel.getSelectedTimeSlot().observe(currentActivity) {
            model = it
            currentTimeSlotId = it.id
            if (this::model.isInitialized) {

                timeSlotStatus.isChecked = model.isActive
                date.text = model.date
                time.text = model.time
                location.text= model.location
                skills.text = model.skills
                description.text = model.description

               // if (!currentTimeSlotId.isNullOrEmpty())

            }
        }


        handleDate(view)
        handleTime(view)
    }

    private val dateFormatter: DateFormat = SimpleDateFormat("MMMM dd, yyyy")

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activity?.menuInflater?.inflate(R.menu.image_context_menu, menu)
    }

    /* after returning back from gallery or camera we need to take some actions*/
    override fun onContextItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.loadFromCameraId -> {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//                    loadFromCamera()
//                else loadFromCameraLessM()
//                true
//            }
//            R.id.loadFromGalleryId -> {
//                loadFromGallery()
//                true
//            }
//            else -> false
//        }
        return false
    }

    private fun setValuesToModel() {
        val title = title.text.toString()
        val duration = duration.text.toString()
        val date = date.text.toString()
        val time = time.text.toString()
        val location = location.text.toString()
        val skills = skills.text.toString()
        val description = description.text.toString()
        val isActive = timeSlotStatus.isChecked

        val userProfile: UserProfileViewModel =
            ViewModelProvider(currentActivity).get(UserProfileViewModel::class.java)
        userProfile.getUserProfile().observe(currentActivity) {
            model = TimeSlotItem(
                currentTimeSlotId,
                userProfile.getUserProfile().value?.id.toString(),
                title,
                description,
                date,time,
                duration,
                location,
                skills,
                false,
                isActive,
            )

        }
    }

    /*Load save menu layout */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.save_menu, menu)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.saveProfileMenu -> {

                // validate fields
                if (!validateFields()) {
                    return false
                }

                // model information
                setValuesToModel()

                val userProfile: UserProfileViewModel =
                    ViewModelProvider(currentActivity).get(UserProfileViewModel::class.java)

                userProfile.getUserProfile().observe(currentActivity) {

                    // save information
                    if (it != null) {
                        saveTimeSlotChangesToFirebase(
                            model,
                            it,
                            currentView,
                            currentActivity,
                            currentActivity
                        )
                    }
                }
                val selectedSkillsViewModel =
                    ViewModelProvider(currentActivity).get(SelectedSkillsViewModel::class.java)
                val navController = findNavController()
                navController.popBackStack()
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        //////////////////////Date////////////////////
        if (date.text.toString().isEmpty()) {
            date.error = "Date can not be empty";
            isValid = false
        } else
            date.error = null
        //////////////////////departureTime////////////////////
        if (time.text.toString().isEmpty()) {
            time.error = "Time can not be empty";
            isValid = false
        } else
            time.error = null
        //////////////////////arrivalLocation////////////////////
        if (location.text.toString().isEmpty()) {
            location.error = "location can not be empty";
            isValid = false
        }

        return isValid
    }

    /* using the image result of camera =1 and gallery =2 for request code*/
    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            if (data?.extras != null) {
                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            }
        } else if (requestCode == 2) {
            if (data?.data != null) {
                saveContentLocally(data)
            }
        }
    }

    /* save a copy of a file from gallery to application specific directory */
    @SuppressLint("SimpleDateFormat")
    private fun saveContentLocally(intent: Intent?): Boolean {
        if (intent == null || intent.data == null) {
            return false
        }
        val inputStream: InputStream? = activity?.contentResolver?.openInputStream(intent.data!!)
        try {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val storageDir = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val snapFilePath = File.createTempFile(
                "carimgS_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            )
            copyFile(inputStream, snapFilePath)
           // model.carPhoto = snapFilePath.path
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Failed save file locally", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    @Throws(IOException::class)
    private fun copyFile(inputStream: InputStream?, file: File) {
        val buffer = ByteArray(1024)
        var length: Int
        FileOutputStream(file).use { outputStream ->
            while (inputStream!!.read(buffer).also { length = it } != -1) {
                outputStream.write(buffer, 0, length)
            }
        }
    }

}

