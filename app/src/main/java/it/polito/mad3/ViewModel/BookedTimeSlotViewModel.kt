package it.polito.mad3.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad3.TimeSlotItem


class BookedTimeSlotViewModel: ViewModel() {
    private  var _bookedTimeSlot = MutableLiveData<TimeSlotItem>()
    private var bookedTimeSlotsListLive: MutableLiveData<MutableList<TimeSlotItem>> = MutableLiveData()
    private var bookedTimeSlotsNotRatedList: MutableLiveData<MutableList<TimeSlotItem>> = MutableLiveData()
    fun getBookedTimeSlots(): MutableLiveData<MutableList<TimeSlotItem>> {
        return bookedTimeSlotsListLive
    }

    fun setBookedTimeSlotsList(timeSlotList: MutableList<TimeSlotItem>) {
        bookedTimeSlotsListLive.value = timeSlotList
    }
    fun getNotRatedTimeSlots(): MutableLiveData<MutableList<TimeSlotItem>> {
        return bookedTimeSlotsNotRatedList
    }
    fun setNotRatedTimeSlots(timeSlotList: MutableList<TimeSlotItem>) {
        bookedTimeSlotsNotRatedList.value = timeSlotList
    }
    fun removeBookedTimeSlotsList(timeSlotItem: TimeSlotItem)
    {
        val list = ArrayList<TimeSlotItem>()
        list.remove(timeSlotItem)
        bookedTimeSlotsListLive.value = list
    }
    fun setBookedTimeSlot(timeSlot: TimeSlotItem){
        _bookedTimeSlot.value=timeSlot
    }
}
