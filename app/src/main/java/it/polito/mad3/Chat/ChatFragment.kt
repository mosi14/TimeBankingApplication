package it.polito.mad3.Chat

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import it.polito.mad3.*
import it.polito.mad3.ViewModel.SelectedSkillsViewModel
import it.polito.mad3.ViewModel.UserProfileViewModel
import java.util.*

class ChatFragment: Fragment(R.layout.fragment_chat) {
    private lateinit var currentChannelId : String
    private lateinit var currentUser : ProfileData
    private lateinit var otherUserId : String
    private lateinit var chatRecyclerView : RecyclerView
    private lateinit var messageBox : TextInputEditText
    private lateinit var sendButton: ImageView
    private lateinit var messagesListenerRegistration : ListenerRegistration
    private var messageList: ArrayList<TextMessage> = ArrayList()
    private lateinit var messaggeAdaptor : TextMessageItemAdaptor
    lateinit var model: TimeSlotItem
    private lateinit var currentActivity: FragmentActivity
    private var shouldInitRecyclerView = true



    override fun onViewCreated(view : View , savedInstanceState : Bundle?) {
        currentActivity=requireActivity()
        chatRecyclerView = view.findViewById(R.id.recycler_view_messages)
        chatRecyclerView.layoutManager = LinearLayoutManager(this.context)
        messaggeAdaptor = TextMessageItemAdaptor(requireContext(),messageList)
        chatRecyclerView.adapter = messaggeAdaptor
        messageBox = view.findViewById(R.id.editText_message)

        sendButton = view.findViewById(R.id.imageView_send)
        //messageList = ArrayList()
        //messaggeAdaptor = TextMessageItemAdaptor(requireContext(),messageList)
        var userProfile: UserProfileViewModel =
            ViewModelProvider(this).get(UserProfileViewModel::class.java)

        userProfile.getUserProfile().observe(currentActivity) {
            if(it!=null)
            currentUser=it!!
        }

        val myTimeSlotsViewModel: SelectedSkillsViewModel =
            ViewModelProvider(currentActivity).get(SelectedSkillsViewModel::class.java)

        myTimeSlotsViewModel.getSelectedTimeSlotUserId().observe(currentActivity) {
            if(it!=null) {
                otherUserId = it!!.toString()
                getOrCreateChatChannel(otherUserId) { channelId ->
                    currentChannelId = channelId

                    messagesListenerRegistration =
                        addChatMessagesListener(
                            channelId,
                            this.requireContext(),
                            this::updateRecyclerView
                        )

                    sendButton.setOnClickListener {
                        val messageToSend =
                            TextMessage(
                                messageBox.text.toString(), Calendar.getInstance().time,
                                FirebaseAuth.getInstance().currentUser!!.uid,
                                otherUserId
                            )
                        messageBox.setText("")
                        sendMessage(messageToSend, channelId)
                    }
                }
            }
        }
    }

    private fun updateRecyclerView(message : List<TextMessage>){
        messageList.clear()
        messageList.addAll(message.toList())
   }

    fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && context != null) {
            operation(requireContext())
        }
    }



}

