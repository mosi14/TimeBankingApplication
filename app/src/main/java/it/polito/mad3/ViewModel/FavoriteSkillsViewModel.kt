package it.polito.mad3.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad3.TimeSlotItem

class FavoriteSkillsViewModel: ViewModel() {
    private  var _interestedSkill = MutableLiveData<TimeSlotItem>()
    var interestedSkillsListLive: MutableLiveData<MutableList<TimeSlotItem>> = MutableLiveData()

    fun getInterestedSkills(): MutableLiveData<MutableList<TimeSlotItem>> {
       return interestedSkillsListLive
    }

    fun setInterestedSkillsList(timeSlotList: MutableList<TimeSlotItem>) {
        interestedSkillsListLive.value = timeSlotList
    }
    fun removeInterestedSkillsList(TimeSlotItem: TimeSlotItem)
    {
        val list = ArrayList<TimeSlotItem>()
        list.remove(TimeSlotItem)
        interestedSkillsListLive.value = list
    }
    fun setInterestedSkill(timeSlot: TimeSlotItem){
        _interestedSkill.value=timeSlot
    }
}
