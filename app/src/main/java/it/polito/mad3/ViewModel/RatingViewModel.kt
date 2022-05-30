package it.polito.mad3.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad3.Rating
import java.lang.Exception
import kotlin.math.roundToInt

class RatingViewModel : ViewModel(){
    // my ratings sent by others when I was teacher
    private var _ratingListAsTeacher = MutableLiveData<MutableList<Rating>>()
    // my ratings sent by others when I was student
    private var _ratingListAsStudent = MutableLiveData<MutableList<Rating>>()
    // AverageRating of my ratings sent by others when I was teacher
    private var _AverageRatingAsTeacher = MutableLiveData<Float>()
    // AverageRating of my ratings sent by others when I was student
    private var _AverageRatingAsStudent = MutableLiveData<Float>()
    // another user ratings sent by others when he or she was teacher
    private var _ratingListAsTeacherOfOtherUsers = MutableLiveData<MutableList<Rating>>()
    // another user ratings sent by others when he or she was student
    private var _ratingListAsStudentOfOtherUsers = MutableLiveData<MutableList<Rating>>()
    // AverageRating of another user ratings sent by others when he or she was teacher
    private var _AverageRatingAsTeacherOfOtherUsers = MutableLiveData<Float>()
    // AverageRating of another user ratings sent by others when he or she was student
    private var _AverageRatingAsStudentOfOtherUsers = MutableLiveData<Float>()

    // my ratings toOtherUsers
    private var _ratingtoOtherUsers = MutableLiveData<MutableList<Rating>>()

    ////////////////////////////// my rating model //////////////////////////
    fun getRatingListAsTeacher(): MutableLiveData<MutableList<Rating>> {
        return _ratingListAsTeacher
    }
    fun setRatingListAsTeacher(rating:MutableList<Rating>){
        _ratingListAsTeacher.value=rating
        try {
            _AverageRatingAsTeacher.value= _ratingListAsTeacher.value!!.map { it.rating.toInt() }.average().toFloat()
        }
        catch (e:Exception) {

        }
    }
    fun getRatingListAsStudent(): MutableLiveData<MutableList<Rating>> {
        return _ratingListAsStudent
    }
    fun setRatingListAsStudent(rating:MutableList<Rating>){
        _ratingListAsStudent.value=rating
        try {
            _AverageRatingAsStudent.value= _ratingListAsStudent.value!!.map { it.rating.toFloat().roundToInt() }.average().toFloat()
        }
        catch (e:Exception) {

        }
    }

    fun getAveStudent():Float{
        return if(_AverageRatingAsStudent.value!=null)
            _AverageRatingAsStudent.value!!
        else 0f
    }
    fun getAveTeacher():Float{
        return _AverageRatingAsTeacher.value!!
    }
    ////////////////////////////// my rating model //////////////////////////

    ////////////////////////////// other users model //////////////////////////

    fun getRatingListAsTeacherOfOtherUsers(): MutableLiveData<MutableList<Rating>> {
        return _ratingListAsTeacherOfOtherUsers
    }
    fun setRatingListAsTeacherOfOtherUsers(rating:MutableList<Rating>){
        _ratingListAsTeacherOfOtherUsers.value=rating
        try {
            _AverageRatingAsTeacherOfOtherUsers.value= _ratingListAsTeacherOfOtherUsers.value!!.map { it.rating.toInt() }.average().toFloat()
        }
        catch (e:Exception) {

        }
    }
    fun getRatingListAsStudentOfOtherUsers(): MutableLiveData<MutableList<Rating>> {
        return _ratingListAsStudentOfOtherUsers
    }
    fun setRatingListAsStudentOfOtherUsers(rating:MutableList<Rating>){
        _ratingListAsStudentOfOtherUsers.value=rating
        try {
            _AverageRatingAsStudentOfOtherUsers.value= _ratingListAsStudentOfOtherUsers.value!!.map { it.rating.toInt() }.average().toFloat()
        }
        catch (e:Exception) {

        }
    }

    fun getAveStudentOfOtherUsers():Float{
        return if(_AverageRatingAsStudentOfOtherUsers.value!=null)
          _AverageRatingAsStudentOfOtherUsers.value!!
        else 0f
    }
    fun getAveTeacherOfOtherUsers():Float{
        return _AverageRatingAsTeacherOfOtherUsers.value!!
    }
    ////////////////////////////// other users model //////////////////////////



    fun getRatingtoOtherUsers(): MutableLiveData<MutableList<Rating>> {
        return _ratingtoOtherUsers
    }
    fun setRatingtoOtherUsers(rating:MutableList<Rating>){
        _ratingtoOtherUsers.value=rating
    }

}