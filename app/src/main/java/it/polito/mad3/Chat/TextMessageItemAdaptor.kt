package it.polito.mad3.Chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import it.polito.mad3.Chat.TextMessage
import it.polito.mad3.R
import java.text.SimpleDateFormat




class TextMessageItemAdaptor (val context : Context,val messageList:ArrayList<TextMessage>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    val ITEM_RECEIVE = 1
    val ITEM_SEND = 2
    override fun onCreateViewHolder(parent : ViewGroup , viewType : Int) : RecyclerView.ViewHolder {
        if(viewType == ITEM_SEND){
            //inflate receive
            val view : View = LayoutInflater
                .from(context)
                .inflate(R.layout.item_text_message_send,parent,false)
            return SendViewHolder(view)

        }else{
            val view : View = LayoutInflater
                .from(context)
                .inflate(R.layout.item_text_message_receive,parent,false)
            return ReceiveViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder : RecyclerView.ViewHolder , position : Int) {
        val currentMessage = messageList[position]
        if(holder.javaClass == SendViewHolder::class.java){

            //do the stuff for send view holder
            val viewHolder = holder as SendViewHolder
            holder.sendMessage.text = currentMessage.text.orEmpty()

        }else{
            //do the stuff for receive view holder
            val viewHolder = holder as ReceiveViewHolder
            holder.receiveMessage.text = currentMessage.text.orEmpty()

        }
    }

    override fun getItemCount() : Int {
        return messageList.size
    }

    class SendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var sendMessage:TextView
        init{
            sendMessage = itemView.findViewById<TextView>(R.id.textView_send_message_text)
        }

    }
    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val receiveMessage :TextView
        init{
            receiveMessage = itemView.findViewById<TextView>(R.id.textView_receive_message_text)
        }

    }

    override fun getItemViewType(position : Int) : Int {
        val currentMessage = messageList[position]
        if(FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)){
            return ITEM_SEND
        }
        else{
            return ITEM_RECEIVE
        }
    }

}