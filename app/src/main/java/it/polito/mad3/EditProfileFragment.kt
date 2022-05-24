package it.polito.mad3

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import it.polito.mad3.ViewModel.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    private lateinit var googleUID: String
    private lateinit var editFragmentObj: View
    private lateinit var currentActivity: FragmentActivity

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
        currentActivity=this.requireActivity()
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
        googleUID = currentFirebaseUser!!.uid
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
        loadProfileFromFireStore(this)
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
        val inputStream: InputStream? =currentActivity.contentResolver.openInputStream(intent.data!!)
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
                }
                return
            }

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
                saveOnFirestore()
                returnToShowProfileFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    /*just return to show profile */
    private fun returnToShowProfileFragment() {
        findNavController().popBackStack()
    }

    /* The data is serialized using a document and the resulting
    String is saved with the key profile.*/
    private fun saveOnFirestore( ) {
        val db = FirebaseFirestore.getInstance()

        val profileData = ProfileData(
            tvFullNameEdit.text.toString(),
            tvNicknameEdit.text.toString(),
            tvDescriptionEdit.text.toString(),
            tvEmailEdit.text.toString(),
            tvSkillsEdit.text.toString(),
            tvLocationEdit.text.toString(),
            photoURI,
            "",
            true,
            googleUID = googleUID,
            ""
        )

        val userProfile: UserProfileViewModel =
            ViewModelProvider(currentActivity).get(UserProfileViewModel::class.java)

        saveUserDB(profileData)
        saveUserImage(photoURI, userProfile.getUserProfile().value!!)
        userProfile.setIsMyProfile(true)
    }

    private fun loadProfileFromFireStore(fragment: Fragment) {
        if (!this::photoURI.isInitialized) photoURI = ""

        // Load intent String extra received from ShowProfile Activity
        val userProfile: UserProfileViewModel =
            ViewModelProvider(currentActivity).get(UserProfileViewModel::class.java)

        userProfile.getUserProfile().observe(currentActivity) {
            if (it != null) {
                if (it.fullName.isNotEmpty())
                    tvFullNameEdit.setText(it.fullName)
                if (it.nickName.isNotEmpty())
                    tvNicknameEdit.setText(it.nickName)
                if (it.email.isNotEmpty())
                    tvEmailEdit.setText(it.email)
                if (!it.location.isEmpty())
                    tvLocationEdit.setText(it.location)
                if (!it.skills.isEmpty())
                    tvSkillsEdit.setText(it.skills)
                if (!it.description.isEmpty())
                    tvDescriptionEdit.setText(it.description)
                if (it.imageUrl.isNotEmpty()) {
                    setImage(viewImage, it.imageUrl, viewImageWidth, viewImageHeight)
                }
            }
        }
    }

}
