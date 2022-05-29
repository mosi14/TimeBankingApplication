package it.polito.mad3

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import it.polito.mad3.chat.TextMessage
import java.text.SimpleDateFormat


data class TextMessageItem(val textMessage:TextMessage,val context:Context)



class TextMessageItemAdaptor (val context : Context,val messageList:ArrayList<TextMessage>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    val ITEM_RECEIVE = 1
    val ITEM_SEND = 2
    override fun onCreateViewHolder(parent : ViewGroup , viewType : Int) : RecyclerView.ViewHolder {
         if(viewType == 1){
             //inflate receive
             val view : View = LayoutInflater
                 .from(context)
                 .inflate(R.layout.item_text_message_send,parent,false)
             return ReceiveViewHolder(view)

         }else{
             val view : View = LayoutInflater
                 .from(context)
                 .inflate(R.layout.item_text_message_receive,parent,false)
             return SendViewHolder(view)
         }
    }

    override fun onBindViewHolder(holder : RecyclerView.ViewHolder , position : Int) {
        if(holder.javaClass == SendViewHolder::class.java){
            //do the stuff for send view holder
            val viewHolder = holder as SendViewHolder

        }else{
            //do the stuff for receive view holder
            val viewHolder = holder as ReceiveViewHolder

        }
    }

//    override fun onBindViewHolder(holder : ItemViewHolder , position : Int) {
//        holder.text.text=data[position].textMessage.text
//        setTimeText(holder)
//        //setMessageRootGravity(holder)
//
//    }

    override fun getItemCount() : Int {
        return messageList.size
    }


//    private fun setTimeText(viewHolder: ItemViewHolder) {
//        val dateFormat = SimpleDateFormat
//            .getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
//        viewHolder.time.text= dateFormat.format(TextMessage.time)
//    }


    class SendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val sendMessage = itemView.findViewById<TextView>(R.id.textView_send_message_text)

    }
    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val receiveMessage = itemView.findViewById<TextView>(R.id.textView_receive_message_text)

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



//    private fun setMessageRootGravity(viewHolder: ItemViewHolder) {
//        if (TextMessage.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
//            viewHolder.message_root.apply {
//                backgroundResource = R.drawable.rect_round_white
//                val lParams = FrameLayout.LayoutParams(wrapContent , wrapContent , Gravity.END)
//                this.layoutParams = lParams
//            }
//        }
//        else {
//            viewHolder.message_root.apply {
//                backgroundResource = R.drawable.rect_round_primary_color
//                val lParams = FrameLayout.LayoutParams(wrapContent, wrapContent, Gravity.START)
//                this.layoutParams = lParams
//            }
//        }
//    }
}