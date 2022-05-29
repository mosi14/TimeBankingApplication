package it.polito.mad3.chat

import java.util.*

data class TextMessage(val text:String , val time: Date ,val senderId:String,val ownerId:String) {
    constructor(): this("",Date(0),"","")
}