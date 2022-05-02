package it.polito.mad3

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import org.json.JSONObject
import java.io.File


class ShowProfileFragment : Fragment() {
    private lateinit var imgProfile: ImageView
    private lateinit var tvFullName: TextView
    private lateinit var tvNickname: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvSkills: TextView
    private lateinit var tvDescription: TextView
    private lateinit var photoURI: String
    private var viewImageHeight = 0
    private var viewImageWidth = 0

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.profile_menu, menu)

    }

    /* on edit profile selected */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.editProfileMenu -> {
                this.loadEditProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_show_profile, container, false)
        setHasOptionsMenu(true)
        // Init Activity elements
        imgProfile = view.findViewById<ImageView>(R.id.imageFilterView)
        tvFullName = view.findViewById<TextView>(R.id.tvFullName)
        tvNickname = view.findViewById<TextView>(R.id.tvNickname)
        tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        tvLocation = view.findViewById<TextView>(R.id.tvLocation)
        tvSkills = view.findViewById<TextView>(R.id.tvSkills)
        tvDescription = view.findViewById<TextView>(R.id.tvDescription)

        // Load Json shared preferences
        loadJson(this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val vto = imgProfile.viewTreeObserver
        vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                imgProfile.viewTreeObserver.removeOnPreDrawListener(this)
                viewImageHeight = imgProfile.measuredHeight
                viewImageWidth = imgProfile.measuredWidth
                if (photoURI.isNotEmpty())
                    setImage(imgProfile, photoURI, viewImageWidth, viewImageHeight)
                return true
            }
        })
    }

    /* load Json data from shared preferences */
    private fun loadJson(fragment: Fragment) {
        if (!this::photoURI.isInitialized) photoURI = ""

        val sharedPref = fragment.activity?.getSharedPreferences(
            getString(R.string.PreferencesFilename), Context.MODE_PRIVATE
        )
        val json = sharedPref?.getString(getString(R.string.Profile_Pref), "")
        var fullName: String? = null
        var nickName: String? = null
        var email: String? = null
        var location: String? = null
        var skills: String? = null
        var description: String? = null
        var imageUri: String? = null
        if (!json.equals("", ignoreCase = true)) {
            try {
                val jsonobj = Gson().fromJson(json, ProfileData::class.java)
                fullName = jsonobj.fullName
                nickName = jsonobj.nickName
                email = jsonobj.email
                location = jsonobj.location
                skills = jsonobj.skills
                description = jsonobj.description
                imageUri = jsonobj.imageUrl
            } catch (e: Exception) {
            }
        }
        if (!fullName.isNullOrEmpty())
            tvFullName.text = fullName
        if (!nickName.isNullOrEmpty())
            tvNickname.text = nickName
        if (!email.isNullOrEmpty())
            tvEmail.text = email
        if (!location.isNullOrEmpty())
            tvLocation.text = location
        if (!skills.isNullOrEmpty())
            tvSkills.text = skills
        if (!description.isNullOrEmpty())
            tvDescription.text = description
        if (!imageUri.isNullOrEmpty()) {
            photoURI = imageUri
            val imgUri: Uri = Uri.parse(imageUri)
            imgProfile.setImageURI(null)
            imgProfile.setImageURI(imgUri)
        }

    }



    /* put string extra profile data to send to edit profile activity */
    private fun loadEditProfile() {
        if (!this::photoURI.isInitialized)
            photoURI = ""
        val bundle = Bundle()
        bundle.putString(getString(R.string.FULL_NAME_Pref), tvFullName.text.toString())
        bundle.putString(getString(R.string.Nick_NAME_Pref), tvNickname.text.toString())
        bundle.putString(getString(R.string.EMAIL_Pref), tvEmail.text.toString())
        bundle.putString(getString(R.string.LOCATION_Pref), tvLocation.text.toString())
        bundle.putString(getString(R.string.SKILLS_Pref), tvSkills.text.toString())
        bundle.putString(getString(R.string.DESCRIPTIONS_Pref), tvDescription.text.toString())
        bundle.putString(getString(R.string.IMAGE_URI_Pref), photoURI)
        this.arguments = bundle

        findNavController().navigate(R.id.action_showProfileFragment_to_editProfileFragment, bundle)
    }

    /* get string extra profile data from edit profile activity */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            readStringExtras()
        }
    }

    /* load StringExtra to load exchanged data from ShowProfile activity */
    private fun readStringExtras() {
        // Load intent String extra received from ShowProfile Activity
        val defaultValue = ""
        val bundle = this.arguments
        if (bundle != null) {
            val fullName = bundle.getString(getString(R.string.FULL_NAME_Pref), defaultValue)
            val nickName = bundle.getString(getString(R.string.Nick_NAME_Pref), defaultValue)
            val email = bundle.getString(getString(R.string.EMAIL_Pref), defaultValue)
            val location = bundle.getString(getString(R.string.LOCATION_Pref), defaultValue)
            val skills = bundle.getString(getString(R.string.SKILLS_Pref), defaultValue)
            val description = bundle.getString(getString(R.string.DESCRIPTIONS_Pref), defaultValue)
            val imageUri = bundle.getString(getString(R.string.IMAGE_URI_Pref), defaultValue)

            // start to set values to ui elements
            if (!fullName.isNullOrEmpty())
                tvFullName.text = fullName
            if (!nickName.isNullOrEmpty())
                tvNickname.text = nickName
            if (!email.isNullOrEmpty())
                tvEmail.text = email
            if (!location.isNullOrEmpty())
                tvLocation.text = location
            if (!skills.isNullOrEmpty())
                tvSkills.text = email
            if (!description.isNullOrEmpty())
                tvDescription.text = location
            if (!imageUri.isNullOrEmpty()) {
                photoURI = imageUri
            }
        }
    }

}