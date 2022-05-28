package it.polito.mad3

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import it.polito.mad3.TimeSlotItem
import com.squareup.picasso.Picasso
import it.polito.mad3.ViewModel.RatingViewModel
import it.polito.mad3.ViewModel.SelectedSkillsViewModel
import it.polito.mad3.ViewModel.UserProfileViewModel
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class Utility {

}
data class FilterItem(val name: String, val value: String, val operatorType: String)

data class ProfileData(
    var fullName: String,
    var nickName:  String,
    var description:  String,
    var email: String,
    var skills: String,
    var location: String,
    var imageUrl: String,
    var id: String,
    var isEditable: Boolean,
    var googleUID: String,
    var temp: String
)

data class BookingData(
    var bookingDate: String,
    var bookingStatus: String,
    var id: String,
    var timeSlotId: String,
    var userId: String
)

fun saveUserDB(userItem: ProfileData) {
    val db = FirebaseFirestore.getInstance()
    // Create a new user with a first and last name
    val user: MutableMap<String, Any> = HashMap()
    user["fullName"] = userItem.fullName
    user["nickName"] = userItem.nickName
    user["email"] = userItem.email
    user["imageUrl"] = userItem.imageUrl
    user["location"] = userItem.location
    user["skills"] = userItem.skills
    user["description"] = userItem.description
    user["googleUID"] = userItem.googleUID
    val ref = db.collection("users").document()
    user["id"] = ref.id

    val query = db.collection("users").whereEqualTo("email", userItem.email)
    query.get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            if (task.result == null || task.result?.documents?.size!! == 0) {
                // Add a new document with a generated ID
                db.collection("users")
                    .document(ref.id)
                    .set(user)
                    .addOnSuccessListener {
                        Log.d(
                            ContentValues.TAG,
                            "DocumentSnapshot added "
                        )
                    }
                    .addOnFailureListener { e ->
                        Log.w(
                            ContentValues.TAG,
                            "Error adding document",
                            e
                        )
                    }
            } else {
                val query1 = db.collection("users").whereEqualTo("email", userItem.email)
                query1.get()
                    .addOnCompleteListener { task1 ->
                        if (task1.isSuccessful) {
                            for (document in task1.result?.documents!!) {
                                //set id of found document
                                user["id"] = document.id
                                // remove found image property in order not to conflict with image upload
                                user.remove("imageUrl")
                                // just update is enough , there is a user that we can update
                                db.collection("users")
                                    .document(document.id)
                                    .update(user)
                                    .addOnSuccessListener { }
                                    .addOnFailureListener { e ->
                                        Log.w(
                                            ContentValues.TAG,
                                            "Error adding document",
                                            e,
                                        )
                                    }
                            }
                        }
                    }
            }
        } else {
            Log.w(ContentValues.TAG, "Error getting documents.", task.exception)
        }
    }
}

/*save changes of edit time slot into Firestore*/
fun saveTimeSlotChangesToFirebase(
    timeSlotItem: TimeSlotItem,
    userItem: ProfileData,
    currentView: View,
    owner: ViewModelStoreOwner,
    viewLifecycleOwner: FragmentActivity
) {
    val db = FirebaseFirestore.getInstance()
    // Create a new timeSlot with a first and last name
    val timeSlot: MutableMap<String, Any> = HashMap()
    timeSlot["id"] = timeSlotItem.id
    timeSlot["userId"] = userItem.id
    timeSlot["title"] = timeSlotItem.title
    timeSlot["location"] = timeSlotItem.location
    timeSlot["skills"] = timeSlotItem.skills
    timeSlot["time"] = timeSlotItem.time
    timeSlot["date"] = timeSlotItem.date
    timeSlot["description"] = timeSlotItem.description
    timeSlot["duration"] = timeSlotItem.duration
    timeSlot["isActive"] = timeSlotItem.isActive.toString()


    val insertQuery = db.collection("TimeSlots").whereEqualTo("id", timeSlotItem.id)
    insertQuery.get().addOnCompleteListener { insertTask ->
        if (insertTask.isSuccessful) {
            ///////////////////////////////////////////////////////////////////////
            // if a document with current timeSlot not fount in the DB
            if (insertTask.result == null || insertTask.result?.documents?.size!! == 0) {
                val ref = db.collection("TimeSlots").document()
                timeSlot["id"] = ref.id
                // Add a new document with a generated ID
                db.collection("TimeSlots")
                    .document(ref.id)
                    .set(timeSlot)
                    .addOnSuccessListener {
                        Snackbar.make(
                            currentView,
                            "New timeSlot added successfully.",
                            Snackbar.LENGTH_SHORT
                        ).setAction("Close") {
                        }.show()
                    }
                    .addOnFailureListener {
                        Snackbar.make(
                            currentView,
                            "Saving data failed!",
                            Snackbar.LENGTH_SHORT
                        ).setAction("Close") {
                        }.show()
                    }


            }
            ///////////////////////////////////////////////////////////////////////
            // if a document with current timeSlot found in the DB
            else {
                val updateQuery = db.collection("TimeSlots").whereEqualTo("id", timeSlotItem.id)
                updateQuery.get()
                    .addOnCompleteListener { UpdateTask ->
                        if (UpdateTask.isSuccessful) {
                            for (document in UpdateTask.result?.documents!!) {
                                db.collection("TimeSlots")
                                    .document(document.id)
                                    .set(timeSlot)
                                    .addOnSuccessListener {
                                        //TODO fix Snackbar
                                    }
                                    .addOnFailureListener { e ->
                                        // TODO fix snakbar
                                    }
                            }
                        }
                    }
            }
        } else {
            Log.w(ContentValues.TAG, "Error getting documents.", insertTask.exception)
        }
    }
}

fun saveUserImage(imageUrl: String, userItem: ProfileData) {

    if (!imageUrl.isNullOrEmpty() && fileExist(imageUrl)) {
        val storageReference: StorageReference = FirebaseStorage.getInstance().reference
        val db = FirebaseFirestore.getInstance()

        var fileId = UUID.randomUUID().toString()
        if (userItem.imageUrl.isNotEmpty() && userItem.imageUrl.contains("/post_images"))
            fileId =
                userItem.imageUrl.split(
                    "post_images%2F",
                    ".jpg",
                    "?alt=media",
                    ignoreCase = true
                )[1]

        val filepath = storageReference
            .child("post_images")
            .child("$fileId.jpg")


        val uploadTask = filepath.putBytes(read(imageUrl)!!)
        uploadTask.addOnFailureListener {

            Log.w(ContentValues.TAG, "Error getting documents.", it.cause)
        }
        uploadTask.addOnSuccessListener {
            if (it.task.isSuccessful) {
                // so we have got the link to image

                userItem.imageUrl = "https://" + it.task.result.uploadSessionUri?.host +
                        it.task.result.uploadSessionUri?.path +
                        it.task.result.storage.path.replace(
                            "/post_images/",
                            "/post_images%2F",
                        ) + "?alt=media"
                // here we set it to user
                val query = db.collection("users").whereEqualTo("email", userItem.email)
                query.get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result?.documents!!) {
                                db.collection("users").document(document.id)
                                    .update("imageUrl", userItem.imageUrl)
                            }
                        }
                    }
            }
            else{
                Log.w(ContentValues.TAG, "Error getting documents.", it.error)
            }
        }
    }

}
fun isFilePresent(context: Context, fileName: String): Boolean {
    val path = context.filesDir.absolutePath + "/" + fileName
    val file = File(path)
    return file.exists()
}
fun read(context: Context, fileName: String): String? {
    return try {
        val fis: FileInputStream = context.openFileInput(fileName)
        val isr = InputStreamReader(fis)
        val bufferedReader = BufferedReader(isr)
        val sb = StringBuilder()
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            sb.append(line)
        }
        sb.toString()
    } catch (fileNotFound: FileNotFoundException) {
        null
    } catch (ioException: IOException) {
        null
    }
}
fun read(path: String): ByteArray? {
    val file = File(path)
    val size: Int = file.length().toInt()
    val bytes = ByteArray(size)
    try {
        val buf = BufferedInputStream(FileInputStream(file))
        buf.read(bytes, 0, bytes.size)
        buf.close()
        return bytes
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}

fun sendInterestNotification(itUser: ProfileData, timeSlotItem: TimeSlotItem) {
    val db = FirebaseFirestore.getInstance()
    val ref = db.collection("Booking").document()
    val query =
        db.collection("users").whereEqualTo("timeSlotId", timeSlotItem.id).whereEqualTo("userId", itUser.id)
    query.get()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.result == null || task.result?.documents?.size!! == 0) {
                    val booking =
                        BookingData(Date().toString(), "pending", ref.id, timeSlotItem.id, itUser.id)
                    val bookingItem: MutableMap<String, Any> = HashMap()
                    bookingItem["bookingDate"] = booking.bookingDate
                    bookingItem["bookingStatus"] = booking.bookingStatus
                    bookingItem["id"] = booking.id
                    bookingItem["timeSlotId"] = booking.timeSlotId
                    bookingItem["userId"] = booking.userId
                    db.collection("Booking")
                        .document(ref.id)
                        .set(bookingItem)
                        .addOnSuccessListener { }
                        .addOnFailureListener { e ->
                            Log.w(
                                ContentValues.TAG,
                                "Error adding document",
                                e,
                            )
                        }
                } else {
                    for (document in task.result?.documents!!) {
                        val booking = BookingData(
                            Date().toString(),
                            "pending",
                            document.id,
                            timeSlotItem.id,
                            itUser.id
                        )
                        val bookingItem: MutableMap<String, Any> = HashMap()
                        bookingItem["bookingDate"] = booking.bookingDate
                        bookingItem["bookingStatus"] = booking.bookingStatus
                        bookingItem["id"] = booking.id
                        bookingItem["timeSlotId"] = booking.timeSlotId
                        bookingItem["userId"] = booking.userId
                        db.collection("Booking")
                            .document(document.id)
                            .set(bookingItem)
                            .addOnSuccessListener { }
                            .addOnFailureListener { e ->
                                Log.w(
                                    ContentValues.TAG,
                                    "Error adding document",
                                    e,
                                )
                            }

                    }

                }
            }
        }
}

data class Rating(
    var comment: String,
    var rating: String,
    var recieverUserId: String,
    var reciverFlag: String,
    var senderUserId: String,
    var timeSlotId: String
)


fun loadOtherProfileDataByIDFirestore(owner: ViewModelStoreOwner, userId: String) {
    val db = FirebaseFirestore.getInstance()
    val selectedTimeSlotViewModel =
        ViewModelProvider(owner).get(SelectedSkillsViewModel::class.java)
    db.collection("users").whereEqualTo("id", userId)
        .addSnapshotListener { value, error ->
            if (error != null)
                throw error

            if (value != null) {
                if (value.documents?.size!! > 0) {
                    for (document in value.documents)
                        selectedTimeSlotViewModel.setTeacherProfile(
                            ProfileData(
                                document["fullName"].toString(),
                                document["nickName"].toString(),
                                document["description"].toString(),
                                document["email"].toString(),
                                document["skills"].toString(),
                                document["location"].toString(),
                                document["imageUrl"].toString(),
                                document.id,
                                false,
                                googleUID = document["googleUID"].toString(),
                                temp = "",
                            ),
                        )

                }
            } else {
                Log.w(ContentValues.TAG, "Error getting documents.", error)
            }
        }
    // load rating average for teacher
    db.collection("Rating")
        .whereEqualTo("recieverUserId", userId)
        .whereEqualTo("reciverFlag", "0") // as teacher
        .addSnapshotListener { value, error ->
            if (error != null)
                throw error

            if (value != null) {
                var ratingList: MutableList<Rating> = arrayListOf()
                for (document in value.documents) {

                    val comment: Any? = document.data!!["comment"]
                    val rating: Any? = document.data!!["rating"]
                    val recieverUserId: Any? = document.data!!["recieverUserId"]
                    val reciverFlag: Any? = document.data!!["reciverFlag"]
                    val senderUserId: Any? = document.data!!["senderUserId"]
                    val timeSlotId: Any? = document.data!!["timeSlotId"]

                    ratingList.add(
                        Rating(
                            comment = comment as String,
                            rating = rating as String,
                            recieverUserId = recieverUserId as String,
                            reciverFlag = reciverFlag as String,
                            senderUserId = senderUserId as String,
                            timeSlotId = timeSlotId as String
                        )
                    )
                }
                var rate = ratingList.map { rate -> rate.rating.toFloat() }.average().toFloat()
                if (rate == null || rate.isNaN()) rate = 0f
                selectedTimeSlotViewModel.setTeacherStarsAsTeacher(rate)
            }
        }
}

fun compareDatetime(
    startDateString: String,
    endDateString: String,
    startTimeString: String,
    endTimeString: String
): Int {
    try {
        val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)
        val startDate: Date = dateFormatter.parse(startDateString)!!
        val startTime: Date = timeFormatter.parse(startTimeString)!!
        val endDate: Date = dateFormatter.parse(endDateString)!!
        val endTime: Date = timeFormatter.parse(endTimeString)!!
        val start = combineDateTime(startDate, startTime)!!
        val end = combineDateTime(endDate, endTime)!!
        return end.compareTo(start)

    } catch (ex: Exception) {
        return 0
    }
}
fun loadProfileDataFirestore(
    db: FirebaseFirestore,
    userEmail: String,
    owner: ViewModelStoreOwner,
    isMyProfile: Boolean,
    googleUID: String
) {
    val userProfile: UserProfileViewModel =
        ViewModelProvider(owner).get(UserProfileViewModel::class.java)
    db.collection("users").whereEqualTo("email", userEmail)
        .addSnapshotListener { value, error ->
            if (error != null)
                throw error

            if (value != null) {
                if (value.documents?.size!! > 0) {
                    for (document in value.documents)
                        userProfile.setUserProfile(
                            ProfileData(
                                document["fullName"].toString(),
                                document["nickName"].toString(),
                                "",
                                userEmail,
                                "",
                                document["location"].toString(),
                                document["imageUrl"].toString(),
                                document.id,
                                isMyProfile,
                                googleUID = document["googleUID"].toString(),
                                temp = "",
                            ),
                        )
                } else if (value.documents?.size!! == 0) {
                    // if it does not exist create it
                    if (userProfile.getUserProfile().value == null) {
                        val userProfileItem = ProfileData(
                            userEmail.split("@")[0],
                            userEmail.split("@")[0],
                            "",
                            userEmail,
                            "",
                            "Please update your location",
                            "",
                            "",
                            true,
                            googleUID = googleUID,
                            temp = "",
                        )
                        saveUserDB(userProfileItem)
                        userProfile.setUserProfile(userProfileItem)

                    }
                }
                //  userProfile.setUserBackupProfile(userProfile.getUserProfile().value!!)

            } else {
                Log.w(ContentValues.TAG, "Error getting documents.", error)
            }
        }
}

private fun combineDateTime(date: Date, time: Date): Date? {
    val calendarA = Calendar.getInstance()
    calendarA.time = date
    val calendarB = Calendar.getInstance()
    calendarB.time = time
    calendarA[Calendar.HOUR_OF_DAY] = calendarB[Calendar.HOUR_OF_DAY]
    calendarA[Calendar.MINUTE] = calendarB[Calendar.MINUTE]
    calendarA[Calendar.SECOND] = calendarB[Calendar.SECOND]
    calendarA[Calendar.MILLISECOND] = calendarB[Calendar.MILLISECOND]
    return calendarA.time
}

/// get the rating of the my user or other users
fun getRatingFromServer(owner: ViewModelStoreOwner, userId: String, isMine: Boolean) {
    var ratingViewModel: RatingViewModel =
        ViewModelProvider(owner).get(RatingViewModel::class.java)
    val db = FirebaseFirestore.getInstance()
    db.collection("Rating")
        .whereEqualTo("recieverUserId", userId)
        .addSnapshotListener { value, error ->
            if (error != null)
                throw error

            if (value != null) {
                var ratingList: MutableList<Rating> = arrayListOf()
                for (document in value.documents) {

                    val comment: Any? = document.data!!["comment"]
                    val rating: Any? = document.data!!["rating"]
                    val recieverUserId: Any? = document.data!!["recieverUserId"]
                    val reciverFlag: Any? = document.data!!["reciverFlag"]
                    val senderUserId: Any? = document.data!!["senderUserId"]
                    val timeSlotId: Any? = document.data!!["timeSlotId"]

                    ratingList.add(
                        Rating(
                            comment = comment as String,
                            rating = rating as String,
                            recieverUserId = recieverUserId as String,
                            reciverFlag = reciverFlag as String,
                            senderUserId = senderUserId as String,
                            timeSlotId = timeSlotId as String
                        )
                    )
                }
                if (isMine) {
                    // if flag is 0 , the receiver is Teacher
                    ratingViewModel.setRatingListAsTeacher(ratingList.filter { rating -> rating.reciverFlag == "0" }
                        .toMutableList())
                    // if flag is 1 , the receiver is Student
                    ratingViewModel.setRatingListAsStudent(ratingList.filter { rating -> rating.reciverFlag == "1" }
                        .toMutableList())
                } else {
                    // if flag is 0 , the receiver is Teacher
                    ratingViewModel.setRatingListAsTeacherOfOtherUsers(ratingList.filter { rating -> rating.reciverFlag == "0" }
                        .toMutableList())
                    // if flag is 1 , the receiver is Passenger
                    ratingViewModel.setRatingListAsStudentOfOtherUsers(ratingList.filter { rating -> rating.reciverFlag == "1" }
                        .toMutableList())
                }
            }
        }
}
/// get list of time slots I rated
fun loadRatedTimeSlots(owner: ViewModelStoreOwner, userId: String) {
    var ratingViewModel: RatingViewModel =
        ViewModelProvider(owner).get(RatingViewModel::class.java)
    val db = FirebaseFirestore.getInstance()
    db.collection("Rating")
        .whereEqualTo("senderUserId", userId)
        .addSnapshotListener { value, error ->
            if (error != null)
                throw error

            if (value != null) {
                var ratingList: MutableList<Rating> = arrayListOf()
                for (document in value.documents) {

                    val comment: Any? = document.data!!["comment"]
                    val rating: Any? = document.data!!["rating"]
                    val recieverUserId: Any? = document.data!!["recieverUserId"]
                    val reciverFlag: Any? = document.data!!["reciverFlag"]
                    val senderUserId: Any? = document.data!!["senderUserId"]
                    val timeSlotId: Any? = document.data!!["timeSlotId"]

                    ratingList.add(
                        Rating(
                            comment = comment as String,
                            rating = rating as String,
                            recieverUserId = recieverUserId as String,
                            reciverFlag = reciverFlag as String,
                            senderUserId = senderUserId as String,
                            timeSlotId = timeSlotId as String
                        )
                    )
                }
                ratingViewModel.setRatingtoOtherUsers(ratingList)
            }
        }
}

fun sendRatingToServer(rating: Rating) {
    val db = FirebaseFirestore.getInstance()
    val ref = db.collection("Rating").document()
    val query =
        db.collection("Rating").whereEqualTo("timeSlotId", rating.timeSlotId)
            .whereEqualTo("senderUserId", rating.senderUserId)
    query.get()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.result == null || task.result?.documents?.size!! == 0) {

                    val ratingItem: MutableMap<String, Any> = HashMap()
                    ratingItem["comment"] = rating.comment
                    ratingItem["rating"] = rating.rating
                    ratingItem["recieverUserId"] = rating.recieverUserId
                    ratingItem["reciverFlag"] = rating.reciverFlag
                    ratingItem["senderUserId"] = rating.senderUserId
                    ratingItem["timeSlotId"] = rating.timeSlotId

                    db.collection("Rating")
                        .document(ref.id)
                        .set(ratingItem)
                        .addOnSuccessListener { }
                        .addOnFailureListener { e ->
                            Log.w(
                                ContentValues.TAG,
                                "Error adding document",
                                e,
                            )
                        }
                } else {
                    for (document in task.result?.documents!!) {

                        val ratingItem: MutableMap<String, Any> = HashMap()
                        ratingItem["comment"] = rating.comment
                        ratingItem["rating"] = rating.rating
                        ratingItem["recieverUserId"] = rating.recieverUserId
                        ratingItem["reciverFlag"] = rating.reciverFlag
                        ratingItem["senderUserId"] = rating.senderUserId
                        ratingItem["timeSlotId"] = rating.timeSlotId

                        db.collection("Rating")
                            .document(document.id)
                            .set(ratingItem)
                            .addOnSuccessListener { }
                            .addOnFailureListener { e ->
                                Log.w(
                                    ContentValues.TAG,
                                    "Error adding document",
                                    e,
                                )
                            }

                    }

                }
            }
        }
}

fun updateBookingState(interestedPerson: ProfileData, timeSlotItem: TimeSlotItem, isAccepted: Boolean) {

    val db = FirebaseFirestore.getInstance()
    val isAvailable: Boolean = timeSlotItem.isActive

    var resultlist: MutableList<String>? = null
    val query = db.collection("Booking").whereEqualTo("timeSlotId", timeSlotItem.id).whereEqualTo(
        "userId",
        interestedPerson.id
    )
    query.get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            for (document in task.result?.documents!!) {
                if (document["bookingStatus"].toString() != "Accepted" || document["bookingStatus"] != "Rejected")
                    db.collection("Booking").document(document.id)
                        .update("bookingStatus", if (isAccepted) "Accepted" else "Rejected")
                        .addOnSuccessListener {
                            if (isAccepted)
                                db.collection("TimeSlots").document(timeSlotItem.id)
                                    .update("isAvailable", (!isAvailable).toString())
                        }
                        .addOnFailureListener {
                            print(it)
                        }
                //update available seat

            }
        } else {
            Log.w(ContentValues.TAG, "Error getting documents.", task.exception)

        }
    }

}

fun addToBookedTimeSlotList(user: ProfileData, timeSlotItem: TimeSlotItem) {
    val db = FirebaseFirestore.getInstance()
    val query = db.collection("Booking").whereEqualTo("timeSlotId", timeSlotItem.id).whereEqualTo(
        "userId",
        user.id
    )
    query.get().addOnCompleteListener { task ->
        try {

            if (task.isSuccessful) {
                for (document in task.result?.documents!!) {
                    if (document!=null && document["bookingStatus"].toString() == "Accepted") {
                        db.collection("Booking").document(document.id)
                    }
                }
            } else {
                Log.w(ContentValues.TAG, "Error getting documents.", task.exception)

            }

        }catch (e:java.lang.Exception){

        }
    }
}

fun create(context: Context, fileName: String, jsonString: String?): Boolean {

    return try {
        val fos: FileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        if (jsonString != null) {
            fos.write(jsonString.toByteArray())
        }
        fos.close()
        true
    } catch (fileNotFound: FileNotFoundException) {
        false
    } catch (ioException: IOException) {
        false
    }
}

fun fileExist(fileName: String): Boolean {
    return try {
        val file = File(fileName)
        file.exists()
    } catch (fileNotFound: FileNotFoundException) {
        false
    } catch (ioException: IOException) {
        false
    }

}

fun Bitmap.fixBitmapRotation(currentPhotoPath: String): Bitmap {
    val ei = ExifInterface(currentPhotoPath)
    val orientation: Int = ei.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_UNDEFINED
    )

    var rotatedBitmap: Bitmap? = null
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(this, 90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(this, 180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(this, 270f)
        ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = this
        else -> rotatedBitmap = this
    }
    return this
}
fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(
        source, 0, 0, source.width, source.height,
        matrix, true
    )
}

/* load the image on image view and
             * resize it with respect to the
            * layout size  */
fun setImage(
    imageView: ImageView,
    currentPhotoPath: String,
    viewImageWidth: Int = 150,
    viewImageHeight: Int = 150,
    isSquare: Boolean = true
) {
    if (!URLUtil.isValidUrl(currentPhotoPath)) {
        if (fileExist(currentPhotoPath)) {
            imageSetBase(currentPhotoPath, viewImageHeight, viewImageWidth, isSquare)
            imageView.setBackgroundResource(0)
            imageView.setImageBitmap(
                imageSetBase(
                    currentPhotoPath,
                    viewImageHeight,
                    viewImageWidth
                )
            )
        }
    } else {
        Picasso.with(imageView.context).load(currentPhotoPath).fit().centerCrop()
            .into(imageView)
    }
}

fun imageSetBase(
    currentPhotoPath: String,
    viewImageWidth: Int = 150,
    viewImageHeight: Int = 150,
    IsSquared: Boolean = true
): Bitmap? {
    // Get the dimensions of the View
    val targetW: Int = viewImageWidth
    val targetH: Int = viewImageHeight

    // Get the dimensions of the bitmap
    val bmOptions = BitmapFactory.Options()
    bmOptions.inJustDecodeBounds = true
    BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
    val photoW = bmOptions.outWidth
    val photoH = bmOptions.outHeight

    // Determine how much to scale down the image
    val scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH))

    // Decode the image file into a Bitmap sized to fill the View
    bmOptions.inJustDecodeBounds = false
    bmOptions.inSampleSize = scaleFactor
    bmOptions.inPurgeable = true
    var bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
    bitmap = bitmap.fixBitmapRotation(currentPhotoPath)
    if (IsSquared) {
        if (bitmap.width >= bitmap.height) {
            bitmap = Bitmap.createBitmap(
                bitmap,
                bitmap.width / 2 - bitmap.getHeight() / 2,
                0,
                bitmap.height,
                bitmap.height
            )
        } else {
            bitmap = Bitmap.createBitmap(
                bitmap,
                0,
                bitmap.height / 2 - bitmap.getWidth() / 2,
                bitmap.width,
                bitmap.width
            )
        }
    }
    return bitmap
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}




