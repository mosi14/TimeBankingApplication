package it.polito.mad3

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class Model : ViewModel() {
    private val _courese = MutableLiveData<List<Course>>()
    val course : LiveData<List<Course>> = _courese
    private val db : FirebaseFirestore

    private val l : ListenerRegistration

    init {
        db = FirebaseFirestore.getInstance()
        l = db.collection("course").addSnapshotListener { v , e ->
            if (e == null) {
                _courese.value = v !!.mapNotNull { d -> d.toCourse() }
            } else {
                _courese.value = emptyList()
            }
        }
    }

    fun create() {
        db
            .collection("course")
            .document("System Programming")
            .set(mapOf("title" to "how to code systems"))
            .addOnSuccessListener { Log.d("Firebase" , "success") }
            .addOnFailureListener { Log.d("Firebase" , "Error") }

        db
            .collection("course")
            .document("Mobile App")
            .set(mapOf("title" to "how to code mobile apps"))
            .addOnSuccessListener { Log.d("Firebase" , "success") }
            .addOnFailureListener { Log.d("Firebase" , "Error") }

        db
            .collection("course")
            .document("Mobile App")
            .set(mapOf("year" to 1995))
            .addOnSuccessListener { Log.d("Firebase" , "success") }
            .addOnFailureListener { Log.d("Firebase" , "Error") }

        db
            .collection("course")
            .document()
            .set(mapOf("title" to "how to code systems"))
            .addOnSuccessListener { Log.d("Firebase" , "success") }
            .addOnFailureListener { Log.d("Firebase" , "Error") }

    }

    override fun onCleared() {
        super.onCleared()
        l.remove()
    }

    data class Course(
            val id : String = "" ,
            val title : String = "" ,
            val year : Long ,
            val toc : List<String> = listOf() ,
            val groups : Map<String , List<String>> = mapOf() ,
    )

    fun DocumentSnapshot.toCourse() : Course? {
        return try {
            val title = get("title") as String
            val year = get("year") as Long
            val groups = get("groups") as Map<String , List<String>>
            val toc = get("toc") as List<String>
            Course(id , title , year , toc , groups)
        } catch (e : Exception) {
            e.printStackTrace()
            null
        }
    }

}



