package it.polito.mad3


import android.annotation.SuppressLint
import android.app.PendingIntent.getActivity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.view.ViewTreeObserver.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*


class TimeSlotEditFragment : Fragment() {
    private lateinit var model: TimeSlotItem
    private lateinit var listOfTimeSlots: MutableList<TimeSlotItem>

    private lateinit var imgView: ImageView
    lateinit var title: TextView
    lateinit var description: TextView
    lateinit var date: TextView
    lateinit var time: TextInputEditText
    lateinit var duration: TextInputEditText
    lateinit var location: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val selectedTime = arguments?.getString(getString(R.string.SELECTED_TIME)) ?: ""
        if (selectedTime.isNotEmpty())
            model = Gson().fromJson<TimeSlotItem>(selectedTime, TimeSlotItem::class.java)
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
        title = view.findViewById<TextView>(R.id.etTitle)
        description = view.findViewById<TextView>(R.id.etDescription)
        date = view.findViewById<TextView>(R.id.etDate)
        time = view.findViewById<TextInputEditText>(R.id.etTime)
        duration = view.findViewById<TextInputEditText>(R.id.etDuration)
        location = view.findViewById<TextView>(R.id.etLocation)


//        val timeSlotList = generateTimeSlotList(size = 5)
//
//        model=timeSlotList[0]

        if (this::model.isInitialized) {
           title.text= model.title
           description.text= model.description
            date.text= model.date
            time?.setText(model.time)
            location.text = model.location
            duration?.setText(model.duration)
        }
        handleDate(view)
        handleTime(view)
    }


    private fun setValuesToModel() {
        if (this::model.isInitialized) {
            model.title = title.text.toString()
            model.description = description.text.toString()
            model.date = date.text.toString()
            model.time = time.text.toString()
            model.duration = duration.text.toString()
            model.location = location.text.toString()
        }

    }
    private fun generateTimeSlotList(size: Int): List<TimeSlotItem> {
        val gson = Gson()
        val items = arrayOf(
            TimeSlotItem(
                1,
                "Mobile Application",
                "learning android studio skills so fast",
                "Apr 24 2021",
                "14:00",
                "2",
                "Torino",

            ), TimeSlotItem(
                2,
                "web Application 2",
                "learning android studio skills so fast",
                "Apr 24 2021",
                "12:00",
                "2",
                "Firenze",

            ),
            TimeSlotItem(
                3,
                "Software Engineering",
                "how to design application",
                "Apr 29 2021",
                "11:00",
                "5",
                "Roma",

            ),
            TimeSlotItem(
                4,
                "information",
                "",
                "May 4 2021",
                "15:00",
                "3",
                "Como",

            ),
            TimeSlotItem(
                5,
                "c#",
                "learn programming language",
                "May 6 2021",
                "15:00",
                "4",
                "Pisa",

            )
        )
        val isFilePresent =
            isFilePresent(this.requireActivity(), getString(R.string.TimeSlotListFileName))
        if (isFilePresent) {
            val jsonString = read(this.requireActivity(), getString(R.string.TimeSlotListFileName))
            val sType = object : TypeToken<List<TimeSlotItem>>() {}.type
            listOfTimeSlots = gson.fromJson<MutableList<TimeSlotItem>>(jsonString, sType)

            return listOfTimeSlots
            //do the json parsing here and do the rest of functionality of app
        } else {
            listOfTimeSlots = ArrayList<TimeSlotItem>()
            for (i in 0 until size) {
                listOfTimeSlots.add(items[i])
            }

            saveListToFile()
            return listOfTimeSlots
        }
    }
    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
      //  activity?.menuInflater?.inflate(R.menu.image_context_menu, menu)
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

    /* Load device camera for android version less than M */
    private fun loadFromCameraLessM() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, 1)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    /* Load device camera for android version M and more*/
    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.M)
    fun loadFromCamera() {
        if (this.context != null)
            if (checkSelfPermission(
                    this.requireContext(),
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                loadFromCameraLessM()
            } else {
                val requestCamera = 1
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                    Toast.makeText(
                        this.context,
                        "We need permission for Camera",
                        Toast.LENGTH_LONG
                    ).show()
                }
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), requestCamera
                )
            }
    }

    /*Check for camera permission*/
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    loadFromCameraLessM()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "For camera we need the permission to access",
                        Toast.LENGTH_LONG
                    ).show()
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.

                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    /* Load device Gallery*/
    private fun loadFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        this.startActivityForResult(
            Intent.createChooser(intent, resources.getString(R.string.app_name)),
            2
        )
    }

    /*Load save menu layout */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.save_menu, menu)
    }

    /* on save menu selected*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.saveProfileMenu -> {
                // save information
                val navController = findNavController()
                setValuesToModel()
                setChangesToJsonFile()
                val jsonModel = Gson().toJson(model)
              /*  navController.previousBackStackEntry?.savedStateHandle?.set(
                    getString(R.string.SELECTED_TIME),
                    jsonModel
                )
               navController.popBackStack()*/
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    /* using the image result of camera =1 and gallery =2 for request code*/
    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            // val imageBitmap = data.extras.get("data") as Bitmap
            val imageBitmap = data?.extras?.get("data") as Bitmap
            // Create an image file name
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val storageDir = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val filePath = File.createTempFile(
                "profileS_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            )
            imgView.setImageBitmap(imageBitmap)
            try {
               // model.carPhoto = filePath.path
                if (filePath.canWrite()) {
                    FileOutputStream(filePath).use { out ->
                        imageBitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            100,
                            out
                        )
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (requestCode == 2) {
            saveContentLocally(data)
            val uri = data?.data
            imgView.setImageURI(uri) // handle chosen image
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


    private fun setChangesToJsonFile() {
        readTimeSlotList()
        val foundItem = listOfTimeSlots.find { model.id == it.id }
        if (foundItem != null) {
            val indexOfItem = listOfTimeSlots.indexOf(foundItem)
            if (indexOfItem >= 0)
                listOfTimeSlots[indexOfItem] = model
        } else
            listOfTimeSlots.add(model)
        saveListToFile()
    }


    private fun readTimeSlotList(): List<TimeSlotItem> {
        val gson = Gson()
        val jsonString = read(this.requireActivity(), getString(R.string.TimeSlotListFileName))
        val sType = object : TypeToken<List<TimeSlotItem>>() {}.type
        listOfTimeSlots = gson.fromJson<MutableList<TimeSlotItem>>(jsonString, sType)
        return listOfTimeSlots
    }

    /* write json list to file*/
    private fun saveListToFile() {
        val gson = Gson()
        val isFileCreated = create(
            this.requireActivity(),
            getString(R.string.TimeSlotListFileName),
            gson.toJson(listOfTimeSlots)
        )
        if (isFileCreated) {
            //proceed with storing the first todo  or show ui
        } else {
            //show error or try again.
            // TODO(save to list if else function)
        }
    }

}

