package it.polito.mad3.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad3.Rating
import java.lang.Exception

class RatingViewModel : ViewModel(){
    // my ratings sent by others when I was driver
    private var _ratingListAsDriver = MutableLiveData<MutableList<Rating>>()
    // my ratings sent by others when I was passenger
    private var _ratingListAsPassenger = MutableLiveData<MutableList<Rating>>()
    // AverageRating of my ratings sent by others when I was driver
    private var _AverageRatingAsDriver = MutableLiveData<Float>()
    // AverageRating of my ratings sent by others when I was passenger
    private var _AverageRatingAsPassenger = MutableLiveData<Float>()
    // another user ratings sent by others when he or she was driver
    private var _ratingListAsDriverOfOtherUsers = MutableLiveData<MutableList<Rating>>()
    // another user ratings sent by others when he or she was passenger
    private var _ratingListAsPassengerOfOtherUsers = MutableLiveData<MutableList<Rating>>()
    // AverageRating of another user ratings sent by others when he or she was driver
    private var _AverageRatingAsDriverOfOtherUsers = MutableLiveData<Float>()
    // AverageRating of another user ratings sent by others when he or she was passenger
    private var _AverageRatingAsPassengerOfOtherUsers = MutableLiveData<Float>()

    // my ratings toOtherUsers
    private var _ratingtoOtherUsers = MutableLiveData<MutableList<Rating>>()

    ////////////////////////////// my rating model //////////////////////////
    fun getRatingListAsDriver(): MutableLiveData<MutableList<Rating>> {
        return _ratingListAsDriver
    }
    fun setRatingListAsDriver(rating:MutableList<Rating>){
        _ratingListAsDriver.value=rating
        try {
            _AverageRatingAsDriver.value= _ratingListAsDriver.value!!.map { it.rating.toInt() }.average().toFloat()
        }
        catch (e:Exception) {

        }
    }
    fun getRatingListAsPassenger(): MutableLiveData<MutableList<Rating>> {
        return _ratingListAsPassenger
    }
    fun setRatingListAsPassenger(rating:MutableList<Rating>){
        _ratingListAsPassenger.value=rating
        try {
            _AverageRatingAsPassenger.value= _ratingListAsPassenger.value!!.map { it.rating.toInt() }.average().toFloat()
        }
        catch (e:Exception) {

        }
    }

    fun getAvePassenger():Float{
        return if(_AverageRatingAsPassenger.value!=null)
            _AverageRatingAsPassenger.value!!
        else 0f
    }
    fun getAveDriver():Float{
        return _AverageRatingAsDriver.value!!
    }
    ////////////////////////////// my rating model //////////////////////////

    ////////////////////////////// other users model //////////////////////////

    fun getRatingListAsDriverOfOtherUsers(): MutableLiveData<MutableList<Rating>> {
        return _ratingListAsDriverOfOtherUsers
    }
    fun setRatingListAsDriverOfOtherUsers(rating:MutableList<Rating>){
        _ratingListAsDriverOfOtherUsers.value=rating
        try {
            _AverageRatingAsDriverOfOtherUsers.value= _ratingListAsDriverOfOtherUsers.value!!.map { it.rating.toInt() }.average().toFloat()
        }
        catch (e:Exception) {

        }
    }
    fun getRatingListAsPassengerOfOtherUsers(): MutableLiveData<MutableList<Rating>> {
        return _ratingListAsPassengerOfOtherUsers
    }
    fun setRatingListAsPassengerOfOtherUsers(rating:MutableList<Rating>){
        _ratingListAsPassengerOfOtherUsers.value=rating
        try {
            _AverageRatingAsPassengerOfOtherUsers.value= _ratingListAsPassengerOfOtherUsers.value!!.map { it.rating.toInt() }.average().toFloat()
        }
        catch (e:Exception) {

        }
    }

    fun getAvePassengerOfOtherUsers():Float{
        return if(_AverageRatingAsPassengerOfOtherUsers.value!=null)
          _AverageRatingAsPassengerOfOtherUsers.value!!
        else 0f
    }
    fun getAveDriverOfOtherUsers():Float{
        return _AverageRatingAsDriverOfOtherUsers.value!!
    }
    ////////////////////////////// other users model //////////////////////////



    fun getRatingtoOtherUsers(): MutableLiveData<MutableList<Rating>> {
        return _ratingtoOtherUsers
    }
    fun setRatingtoOtherUsers(rating:MutableList<Rating>){
        _ratingtoOtherUsers.value=rating
    }

}