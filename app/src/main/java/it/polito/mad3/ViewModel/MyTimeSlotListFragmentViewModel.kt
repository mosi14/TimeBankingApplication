package it.polito.mad3.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad3.FilterItem
import it.polito.mad3.TimeSlotItem


class MyTimeSlotListFragmentViewModel:ViewModel() {
    private var _myTimeSlotList = MutableLiveData<MutableList<TimeSlotItem>>()
    private var _myTimeSlotFilteredList = MutableLiveData<MutableList<TimeSlotItem>>()
    private var _filterList = MutableLiveData<MutableList<FilterItem>>()
    private var _myTimeSlotCount = MutableLiveData<Int>()

    fun getMyTimeSlotList(): MutableLiveData<MutableList<TimeSlotItem>>{
        return _myTimeSlotList
    }

    fun getMyTimeSlotCount(): MutableLiveData<Int>{
        return  _myTimeSlotCount
    }
    fun setMyTimeSlotCount(input:Int){
            _myTimeSlotCount.value=input
    }
    fun getMyTimeSlotFilteredList (): MutableLiveData<MutableList<TimeSlotItem>>{
        return _myTimeSlotFilteredList
    }
    fun setMyTimeSlotFilteredList ( myFilteredList:MutableList<TimeSlotItem>){
        _myTimeSlotFilteredList.value=myFilteredList
    }
    fun getFilter(): MutableLiveData<MutableList<FilterItem>>{
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

    fun addTimeSlot(timeSlotItem: TimeSlotItem){
        if(_myTimeSlotList.value!=null)
            getMyTimeSlotList().value?.add( timeSlotItem)
        else getMyTimeSlotList().value= mutableListOf(timeSlotItem)
    }
    fun removeTimeSlot(timeSlotItem: TimeSlotItem)
    {
        getMyTimeSlotList().value?.remove(timeSlotItem)
    }
    fun setAllTimeSlots(timeSlotList:MutableList<TimeSlotItem>)
    {
        getMyTimeSlotList().value=timeSlotList
    }

    fun getAllTimeSlots():MutableList<TimeSlotItem>?{
       return getMyTimeSlotList().value
    }
    fun <T> MutableLiveData<T>.forceRefresh() {
        this.value = this.value
    }
}