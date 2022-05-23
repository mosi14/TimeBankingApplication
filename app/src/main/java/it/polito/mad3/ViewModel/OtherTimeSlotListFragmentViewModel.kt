package it.polito.mad3.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad3.FilterItem
import it.polito.mad3.TimeSlotItem


class OtherTimeSlotListFragmentViewModel:ViewModel() {
    private var _othersTimeSlotList = MutableLiveData<MutableList<TimeSlotItem>>()
    private var _othersTimeSlotFilteredList = MutableLiveData<MutableList<TimeSlotItem>>()
    private var _filterList = MutableLiveData<MutableList<FilterItem>>()

    fun getOthersTimeSlotList(): MutableLiveData<MutableList<TimeSlotItem>>{
        return _othersTimeSlotList
    }
    fun getOthersTimeSlotFilteredList (): MutableLiveData<MutableList<TimeSlotItem>>{
        return _othersTimeSlotFilteredList
    }
    fun setOthersTimeSlotFilteredList ( othersTimeSlotFilteredList:MutableList<TimeSlotItem>){
        _othersTimeSlotFilteredList.value=othersTimeSlotFilteredList
    }
    fun getFilters(): MutableLiveData<MutableList<FilterItem>>{
        return _filterList
    }
    fun setFilters(arrivalfilter:MutableList<FilterItem>){
         _filterList.value=arrivalfilter
    }
    fun clearFilters(){
        _filterList.value?.clear()
        _filterList.value= _filterList.value
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun addTimeSlot(TimeSlotItem: TimeSlotItem){
        if(_othersTimeSlotList.value!=null)
            getOthersTimeSlotList().value?.add( TimeSlotItem)
        else getOthersTimeSlotList().value= mutableListOf(TimeSlotItem)
    }
    fun removeTimeSlot(TimeSlotItem: TimeSlotItem)
    {
        getOthersTimeSlotList().value?.remove(TimeSlotItem)
    }
    fun setAllTimeSlots(timeSlotList:MutableList<TimeSlotItem>)
    {
        getOthersTimeSlotList().value=timeSlotList
    }

    fun getAllTimeSlots():MutableList<TimeSlotItem>?{
        return getOthersTimeSlotList().value
    }

    fun <T> MutableLiveData<T>.forceRefresh() {
        this.value = this.value
    }
}