package it.polito.mad3.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad3.ProfileData

class UserProfileViewModel : ViewModel() {
    //my info shows in UI when isMyProfile is true
    private var _userProfile = MutableLiveData<ProfileData?>()
    private var _userEmail = MutableLiveData<String>()
    //my info shows in UI when isMyProfile is false
    private var _othersUserProfile = MutableLiveData<ProfileData?>()
    private var _isMyProfile = MutableLiveData<Boolean?>()

    fun getUserProfile(): MutableLiveData<ProfileData?> {
        return _userProfile
    }

    fun setUserProfile(profileData: ProfileData?) {
        _userProfile.value = profileData
    }
    fun getUserEmail(): MutableLiveData<String> {
        return _userEmail
    }

    fun setUserEmail(profileEmail:  String) {
        _userEmail.value = profileEmail
    }

    fun getOthersUserProfile(): MutableLiveData<ProfileData?> {
        return _othersUserProfile
    }

    fun setOthersUserProfile(email: ProfileData?) {
        _othersUserProfile.value = email
    }

    fun getIsMyProfile(): MutableLiveData<Boolean?> {
        return _isMyProfile
    }
    // if I need my profile true else if other profile false
    fun setIsMyProfile(isMyProfile:Boolean){
        _isMyProfile.value=isMyProfile
    }

}