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
import it.polito.mad3.R
import it.polito.mad3.TextMessageItem
import it.polito.mad3.TextMessageItemAdaptor
import kotlinx.android.synthetic.main.fragment_chat.*
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

        //
        FirestoreUtil.getCurrentUser {
            currentUser = it
        }

        FirestoreUtil.getOrCreateChatChannel(otherUserId) { channelId ->
            currentChannelId = channelId

            messagesListenerRegistration =
                FirestoreUtil.addChatMessagesListener(channelId , requireContext(), this::updateRecyclerView)

            imageView_send.setOnClickListener {
                val messageToSend =
                    TextMessage(
                            editText_message.text.toString() , Calendar.getInstance().time ,
                            FirebaseAuth.getInstance().currentUser !!.uid ,
                            otherUserId , currentUser.name
                    )
                editText_message.setText("")
                FirestoreUtil.sendMessage(messageToSend , channelId)
            }
        }
    }

    private fun updateRecyclerView(message : List<TextMessageItem>){
        Toast.makeText( context,"OnMesssageChangedRunning" , Toast.LENGTH_SHORT).show()
    }
}