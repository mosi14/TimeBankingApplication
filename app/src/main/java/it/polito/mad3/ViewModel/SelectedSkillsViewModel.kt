package it.polito.mad3.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad3.BookingData
import it.polito.mad3.ProfileData
import it.polito.mad3.TimeSlotItem

class SelectedSkillsViewModel  : ViewModel() {
    private  var _selectedTimeSlot = MutableLiveData<TimeSlotItem>()
    private var _DriverProfile = MutableLiveData<ProfileData?>()
    //  getdrivers stars
    private var _DriverStarsAsDriver = MutableLiveData<Float>()
    // list of interested people in selected skill
    private var _interestedPeopleInSkill = MutableLiveData<MutableList<ProfileData?>>()
    // list of interested people in selected skill
    private var _interestedPeopleInSkillBookings = MutableLiveData<MutableList<BookingData?>>()

    fun getSelectedTimeSlot(): MutableLiveData<TimeSlotItem> {
        return _selectedTimeSlot
    }
    fun setSelectedTimeSlot(timeSlot: TimeSlotItem){
        _selectedTimeSlot.value=timeSlot
    }
    fun refereshSelectedTimeSlot(){
        _selectedTimeSlot.value=_selectedTimeSlot.value
    }

    fun getDriverProfile(): MutableLiveData<ProfileData?> {
        return _DriverProfile
    }

    fun setDriverProfile(profileData: ProfileData?) {
        _DriverProfile.value = profileData
    }


    fun getDriverStarsAsDriver(): MutableLiveData<Float> {
        return _DriverStarsAsDriver
    }
    fun setDriverStarsAsDriver(driverStarsAsDriver:Float){
        _DriverStarsAsDriver.value=driverStarsAsDriver
    }
    fun getInterestedPeopleInSkill(): MutableLiveData<MutableList<ProfileData?>> {
        return _interestedPeopleInSkill
    }
    fun clearInterestedPeopleInSkill() {
        _interestedPeopleInSkill.value?.clear()
    }
    fun removeByIdInterestedPersonInSkill(userid:String){
        _interestedPeopleInSkill.value?.remove(_interestedPeopleInSkill?.value?.find { pd -> pd?.id==userid })
    }
    fun setInterestedPeopleInSkill(interestedProfileDataList: MutableList<ProfileData?>) {
        _interestedPeopleInSkill.value = interestedProfileDataList
    }
    fun addInterestedPeopleInSkill(interestedProfileData: ProfileData) {
        _interestedPeopleInSkill.value?.add(interestedProfileData)
        _interestedPeopleInSkill.value= _interestedPeopleInSkill.value
    }
    fun notContainInterestedPersonInSkill(userid:String):Boolean{
       return _interestedPeopleInSkill?.value?.find { pd -> pd?.id==userid }!=null
    }
    fun getInterestedPeopleInSkillBookings(): MutableLiveData<MutableList<BookingData?>> {
        return _interestedPeopleInSkillBookings
    }

    fun setInterestedPeopleInSkillBookings(interestedProfileDataList: MutableList<BookingData?>) {
        _interestedPeopleInSkillBookings.value = interestedProfileDataList
    }
}