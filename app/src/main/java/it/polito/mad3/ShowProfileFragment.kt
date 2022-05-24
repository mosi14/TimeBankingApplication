package it.polito.mad3

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import it.polito.mad3.ViewModel.UserProfileViewModel


class ShowProfileFragment : Fragment() {
    private lateinit var imgProfile: ImageView
    private lateinit var tvFullName: TextView
    private lateinit var tvNickname: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvLocation: TextView
    private lateinit var  emailLayout:RelativeLayout
    private lateinit var  locationLayout:RelativeLayout
    //private lateinit var tvSkills: TextView
   // private lateinit var tvDescription: TextView
    private lateinit var photoURI: String
    private var viewImageHeight = 0
    private var viewImageWidth = 0
    private lateinit var currentActivity: FragmentActivity

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        var userProfile: UserProfileViewModel =
            ViewModelProvider(currentActivity).get(UserProfileViewModel::class.java)
        userProfile.getUserProfile().observe(viewLifecycleOwner) {
            if (userProfile.getIsMyProfile().value == true) {
                menu.clear()
                inflater.inflate(R.menu.profile_menu, menu)
            }
        }
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
        currentActivity=requireActivity()
        val view = inflater.inflate(R.layout.fragment_show_profile, container, false)
        setHasOptionsMenu(true)
        // Init Activity elements
        imgProfile = view.findViewById<ImageView>(R.id.navbar_profileImageView)
        tvFullName = view.findViewById<TextView>(R.id.edit_fullName)
        tvNickname = view.findViewById<TextView>(R.id.edit_nickName)
        tvEmail = view.findViewById<TextView>(R.id.edit_email)
        tvLocation = view.findViewById<TextView>(R.id.edit_location)
//        tvSkills = view.findViewById<TextView>(R.id.tvSkills)
//        tvDescription = view.findViewById<TextView>(R.id.tvDescription)

        emailLayout = view.findViewById<RelativeLayout>(R.id.Relative_email)
        locationLayout = view.findViewById<RelativeLayout>(R.id.Relative_location)

        loadProfileFromFireStore(this)

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vto = imgProfile.viewTreeObserver
        if (!this::photoURI.isInitialized)
            photoURI = ""
        vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {

                imgProfile.viewTreeObserver.removeOnPreDrawListener(this)
                viewImageHeight = imgProfile.measuredHeight
                viewImageWidth = imgProfile.measuredWidth
                if (photoURI != null && photoURI.isNotEmpty())
                    setImage(imgProfile, photoURI, viewImageWidth, viewImageHeight)
                return true
            }
        })
    }

    /* load Json data from shared preferences */
    private fun loadProfileFromFireStore(fragment: Fragment) {
        if (!this::photoURI.isInitialized) photoURI = ""

        var userProfile: UserProfileViewModel =
            ViewModelProvider(currentActivity).get(UserProfileViewModel::class.java)

        userProfile.getOthersUserProfile().observe(currentActivity) {
            if (it != null && userProfile.getIsMyProfile().value != true) {
                setModelToView(it, true)
            }
        }

        userProfile.getUserProfile().observe(currentActivity) {
            if (it != null && userProfile.getIsMyProfile().value == true) {
                setModelToView(it, false)
            }
        }
    }

    private fun setModelToView(profileData:ProfileData, isOthersProfile:Boolean){

        var fullName: String? = null
        var nickName: String? = null
        var email: String? = null
        var location: String? = null
        var skills: String? = null
        var description: String? = null
        var imageUri: String? = null
        if (profileData != null) {
            fullName = profileData.fullName
            nickName = profileData.nickName
            email = profileData.email
            location = profileData.location
            skills = profileData.email
            description = profileData.description
            photoURI = profileData.imageUrl

            if(isOthersProfile)
            { emailLayout

                emailLayout.visibility=View.GONE
                locationLayout.visibility=View.GONE
            }
            else{
                emailLayout.visibility=View.VISIBLE
                locationLayout.visibility=View.VISIBLE
            }


            if (!fullName.isNullOrEmpty())
                tvFullName.text = fullName
            if (!nickName.isNullOrEmpty())
                tvNickname.text = nickName
            if (!email.isNullOrEmpty() && !isOthersProfile)
                tvEmail.text = email
            if (!location.isNullOrEmpty() && !isOthersProfile)
                tvLocation.text = location
//            if (!skills.isNullOrEmpty())
//                tvSkills.text = skills
//            if (!description.isNullOrEmpty())
//                tvDescription.text = description
            if (!photoURI.isNullOrEmpty()) {
                val imgUri: Uri = Uri.parse(photoURI)
                imgProfile.setImageURI(null)
                imgProfile.setImageURI(imgUri)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if(this::currentActivity.isInitialized){
            var userProfile: UserProfileViewModel =
                ViewModelProvider(currentActivity).get(UserProfileViewModel::class.java)
            userProfile.setIsMyProfile(true)
        }
    }

    /* put string extra profile data to send to edit profile activity */
    private fun loadEditProfile() {
        if (!this::photoURI.isInitialized)
            photoURI = ""

        findNavController().navigate(R.id.action_showProfileFragment_to_editProfileFragment)
    }

}