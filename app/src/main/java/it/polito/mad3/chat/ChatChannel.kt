package it.polito.mad3.chat

data class ChatChannel(val userIds: MutableList<String>) {
    constructor() : this(mutableListOf())
}