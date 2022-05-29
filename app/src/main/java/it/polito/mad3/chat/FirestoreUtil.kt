package it.polito.mad3.chat

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.model.mutation.Precondition.exists
import it.polito.mad3.TextMessageItem


object FirestoreUtil {
    private val firestoreInstance:FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val chatChannelsCollectionRef = firestoreInstance.collection("chatChannels")

    fun getOrCreateChatChannel(otherUserId: String,
                               onComplete: (channelId: String) -> Unit) {
        currentUserDocRef.collection("engagedChatChannels")
            .document(otherUserId).get().addOnSuccessListener {
                if (it.exists()) {
                    onComplete(it["channelId"] as String)
                    return@addOnSuccessListener
                }

                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

                val newChannel = chatChannelsCollectionRef.document()
                newChannel.set(ChatChannel(mutableListOf(currentUserId, otherUserId)))

                currentUserDocRef
                    .collection("engagedChatChannels")
                    .document(otherUserId)
                    .set(mapOf("channelId" to newChannel.id))

                firestoreInstance.collection("users").document(otherUserId)
                    .collection("engagedChatChannels")
                    .document(currentUserId)
                    .set(mapOf("channelId" to newChannel.id))

                onComplete(newChannel.id)
            }
    }


    fun addChatMessagesListener(channelId: String , context: Context ,
                                onListen: (List<TextMessageItem>) -> Unit): ListenerRegistration {
        return chatChannelsCollectionRef.document(channelId).collection("messages")
            .orderBy("time")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE" , "ChatMessagesListener error." , firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<TextMessageItem>()
                querySnapshot!!.documents.forEach {
                    items.add(TextMessageItem(it.toObject(TextMessage::class.java)!! , context))
                    return@forEach
                }
                onListen(items)
            }
    }

    fun sendMessage(message: TextMessage, channelId: String) {
        chatChannelsCollectionRef.document(channelId)
            .collection("messages")
            .add(message)
    }
}