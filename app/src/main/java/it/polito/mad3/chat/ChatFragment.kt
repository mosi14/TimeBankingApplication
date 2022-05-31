package it.polito.mad3.chat

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.auth.User
import it.polito.mad3.*

import java.util.*
import kotlin.collections.ArrayList

class ChatFragment: Fragment(R.layout.fragment_chat) {


    private lateinit var currentChannelId : String
    private lateinit var currentUser : User
    private lateinit var otherUserId : String
    private lateinit var chatRecyclerView : RecyclerView
    private lateinit var messageBox : TextInputEditText
    private lateinit var sendButton: ImageView
    private lateinit var messagesListenerRegistration : ListenerRegistration
    private lateinit var messageList: ArrayList<TextMessage>
    private lateinit var messaggeAdaptor : TextMessageItemAdaptor
    private var shouldInitRecyclerView = true

    override fun onViewCreated(view : View , savedInstanceState : Bundle?) {

        chatRecyclerView = view.findViewById(R.id.recycler_view_messages)
        messageBox = view.findViewById(R.id.editText_message)
        sendButton = view.findViewById(R.id.imageView_send)
        messageList = ArrayList()
        messaggeAdaptor = TextMessageItemAdaptor(requireContext(),messageList)



        getCurrentUser {
            currentUser = it
        }


        otherUserId = "" //it should be the Id of the person that we click on its advertisement
        getOrCreateChatChannel(otherUserId) { channelId ->
            currentChannelId = channelId

            messagesListenerRegistration =
                addChatMessagesListener(channelId , requireContext(), this::updateRecyclerView)

            sendButton.setOnClickListener {
                val messageToSend =
                    TextMessage(
                            messageBox.text.toString() , Calendar.getInstance().time ,
                            FirebaseAuth.getInstance().currentUser !!.uid ,
                            otherUserId
                    )
                messageBox.setText("")
                sendMessage(messageToSend , channelId)
            }
        }
    }

    private fun updateRecyclerView(message : List<TextMessageItem>){
        Toast.makeText( context,"OnMesssageChangedRunning" , Toast.LENGTH_SHORT).show()
    }
}