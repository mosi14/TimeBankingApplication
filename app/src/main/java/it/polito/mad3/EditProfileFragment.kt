package it.polito.mad3

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class EditProfileFragment : Fragment() {

    private lateinit var tvFullNameEdit: TextView
    private lateinit var tvNicknameEdit: TextView
    private lateinit var tvEmailEdit: TextView
    private lateinit var tvLocationEdit: TextView
    private lateinit var tvSkillsEdit: TextView
    private lateinit var tvDescriptionEdit: TextView
    private lateinit var imButton: ImageButton
    private lateinit var photoURI: String
    private lateinit var viewImage: ImageView

    lateinit var editFragmentObj :View
    // for resizing the camera input image height
    var viewImageHeight = 0

    // for resizing the camera input image width
    var viewImageWidth = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //Init Activity elements

        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editFragmentObj=view
        tvFullNameEdit = editFragmentObj.findViewById<EditText>(R.id.txtEditFullName)
        tvNicknameEdit = editFragmentObj.findViewById<EditText>(R.id.txtEditNickname)
        tvEmailEdit = editFragmentObj.findViewById<EditText>(R.id.txtEditEmail)
        tvLocationEdit = editFragmentObj.findViewById<EditText>(R.id.txtEditLocation)
        tvSkillsEdit = editFragmentObj.findViewById<EditText>(R.id.txtEditSkills)
        tvDescriptionEdit = editFragmentObj.findViewById<EditText>(R.id.txtEditDescription)
        viewImage = editFragmentObj.findViewById<ImageView>(R.id.imageView)
        imButton = editFragmentObj.findViewById<ImageButton>(R.id.imageButton)
        photoURI = ""
        loadStates()
        initImageLoadPopup()
        val vto = viewImage.viewTreeObserver
        vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                viewImage.viewTreeObserver.removeOnPreDrawListener(this)
                viewImageHeight = viewImage.measuredHeight
                viewImageWidth = viewImage.measuredWidth
                if (!photoURI.isNullOrEmpty())
                    setImage(viewImage, photoURI, viewImageWidth, viewImageHeight)
                return true
            }
        })
    }

    /* Configs only pop up menu for loading image
    *  from camera and gallery*/
    private fun initImageLoadPopup() {

        imButton.setOnClickListener {
            registerForContextMenu(imButton)
            activity?.openContextMenu(imButton)
            unregisterForContextMenu(imButton)
        }
    }
    /* create a menu as a floating context menu*/
    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activity?.menuInflater?.inflate(R.menu.image_context_menu, menu);
    }



    /* after returning back from gallery or camera we need to take some actions*/
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.loadFromCameraId -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    loadFromCamera()
                else loadFromCameraLessM()
                true
            }
            R.id.loadFromGalleryId -> {
                loadFromGallery()
                true
            }
            else -> false
        }
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
    @RequiresApi(Build.VERSION_CODES.M)
    fun loadFromCamera() {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            loadFromCameraLessM()
        else {
            val REQUEST_CAMERA = 1
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                Toast.makeText(
                    requireContext(),
                    "We need permission for Camera",
                    Toast.LENGTH_LONG
                ).show()
            }
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQUEST_CAMERA
            )
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

    /* using the image result of camera =1 and gallery =2 for request code*/
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
            // viewImage.setImageBitmap(imageBitmap)
            try {
                photoURI = filePath.path
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
            // val uri = data?.data
            //   viewImage.setImageURI(uri) // handle chosen image

        }
        setImage(viewImage, photoURI, viewImageWidth, viewImageHeight)
    }

    /* save a copy of a file from gallery to application specific directory */
    private fun saveContentLocally(intent: Intent?): Boolean {
        if (intent == null || intent.data == null) {
            return false
        }
        val inputStream: InputStream? =requireActivity().contentResolver.openInputStream(intent.data!!)
        try {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val storageDir = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val snapFilePath = File.createTempFile(
                "profileS_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            )
            copyFile(inputStream, snapFilePath)
            photoURI = snapFilePath.path
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
                saveJSON(this.requireActivity())
                returnToShowProfileActivity()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /*save data in Extra strings of intent */
    private fun returnToShowProfileActivity() {
        val bundle = Bundle()
        bundle.putString(getString(R.string.FULL_NAME_Pref), tvFullNameEdit.text.toString())
        bundle.putString(getString(R.string.Nick_NAME_Pref), tvNicknameEdit.text.toString())
        bundle.putString(getString(R.string.EMAIL_Pref), tvEmailEdit.text.toString())
        bundle.putString(getString(R.string.LOCATION_Pref), tvLocationEdit.text.toString())
        bundle.putString(getString(R.string.SKILLS_Pref), tvSkillsEdit.text.toString())
        bundle.putString(getString(R.string.DESCRIPTIONS_Pref), tvDescriptionEdit.text.toString())
        if (this::photoURI.isInitialized) {
            bundle.putString(getString(R.string.IMAGE_URI_Pref), photoURI.toString())
            Toast.makeText(requireContext(), "Information Saved!", Toast.LENGTH_LONG)
                .show()
        }

        this.arguments = bundle
        findNavController().popBackStack()
    }

    /* for laout rotation and etc saves the state of view */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("group07.lab1.FULL_NAME", tvFullNameEdit.text.toString())
        outState.putString("group07.lab1.Nick_NAME", tvNicknameEdit.text.toString())
        outState.putString("group07.lab1.EMAIL_SHOW", tvEmailEdit.text.toString())
        outState.putString("group07.lab1.LOCATION_SHOW", tvLocationEdit.text.toString())
        outState.putString("group07.lab1.SKILLS_SHOW", tvSkillsEdit.text.toString())
        outState.putString("group07.lab1.DESCRIPTION_SHOW", tvDescriptionEdit.text.toString())
        if (this::photoURI.isInitialized) {
            outState.putString("group07.lab1.IMAGE_URI", photoURI)
        }
    }

    /* for laout rotation and etc loads the state of view */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            loadInstanceStates(savedInstanceState)
        }
    }

    /* load the state of app after returning to activity*/
    private fun loadInstanceStates(savedInstanceState: Bundle) {
        val fullName = savedInstanceState.getString(getString(R.string.FULL_NAME_Pref))
        val nickName = savedInstanceState.getString(getString(R.string.Nick_NAME_Pref))
        val email = savedInstanceState.getString(getString(R.string.EMAIL_Pref))
        val location = savedInstanceState.getString(getString(R.string.LOCATION_Pref))
        val skills = savedInstanceState.getString(getString(R.string.SKILLS_Pref))
        val description = savedInstanceState.getString(getString(R.string.DESCRIPTIONS_Pref))
        val imageURI = savedInstanceState.getString(getString(R.string.IMAGE_URI_Pref))
        if (!fullName.isNullOrEmpty())
            tvFullNameEdit.setText(fullName)
        if (!nickName.isNullOrEmpty())
            tvNicknameEdit.setText(nickName)
        if (!email.isNullOrEmpty())
            tvEmailEdit.setText(email)
        if (!location.isNullOrEmpty())
            tvLocationEdit.setText(location)
        if (!skills.isNullOrEmpty())
            tvSkillsEdit.setText(skills)
        if (!description.isNullOrEmpty())
            tvDescriptionEdit.setText(description)
        if (!imageURI.isNullOrEmpty()) {
            if (this::photoURI.isInitialized)
                photoURI = imageURI
        }
    }

    /* Init Activity elements and load string extra from show layout*/
    private fun loadStates( ) {
        val bundle =this.arguments
        if(bundle!=null) {
            val fullName =
                bundle.getString(getString(R.string.FULL_NAME_Pref), tvFullNameEdit.text.toString())
            val nickName =
                bundle.getString(getString(R.string.Nick_NAME_Pref), tvNicknameEdit.text.toString())
            val email = bundle.getString(getString(R.string.EMAIL_Pref), tvEmailEdit.text.toString())
            val location =
                bundle.getString(getString(R.string.LOCATION_Pref), tvLocationEdit.text.toString())

            val skills = bundle.getString(getString(R.string.SKILLS_Pref), tvSkillsEdit.text.toString())
            val description =
                bundle.getString(getString(R.string.DESCRIPTIONS_Pref), tvDescriptionEdit.text.toString())


            val imageUri = bundle.getString(getString(R.string.IMAGE_URI_Pref), photoURI.toString())

            if (!fullName.isNullOrEmpty())
                tvFullNameEdit.setText(fullName)
            if (!nickName.isNullOrEmpty())
                tvNicknameEdit.setText(nickName)
            if (!email.isNullOrEmpty())
                tvEmailEdit.setText(email)
            if (!location.isNullOrEmpty())
                tvLocationEdit.setText(location)
            if (!skills.isNullOrEmpty())
                tvSkillsEdit.setText(skills)
            if (!description.isNullOrEmpty())
                tvDescriptionEdit.setText(description)
            if (!imageUri.isNullOrEmpty() && this::photoURI.isInitialized)
                photoURI = imageUri
        }
    }

    /* The data is serialized using a JSONObject and the resulting
        String is saved with the key profile.*/
    private fun saveJSON(activity: Context) {
        val sharedPref = activity.getSharedPreferences(
            getString(R.string.PreferencesFilename), Context.MODE_PRIVATE
        )
        val profileData = ProfileData(
            tvFullNameEdit.text.toString(),
            tvNicknameEdit.text.toString(),
            tvDescriptionEdit.text.toString(),
            tvEmailEdit.text.toString(),
            tvSkillsEdit.text.toString(),
            tvLocationEdit.text.toString(),
            photoURI
        )
        val prefsEditor = sharedPref.edit()
        val json = Gson().toJson(profileData)
        prefsEditor.putString(getString(R.string.Profile_Pref), json)
        prefsEditor.commit()
        prefsEditor.apply()

    }




}
