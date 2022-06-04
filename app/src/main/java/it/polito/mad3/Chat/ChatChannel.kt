package it.polito.mad3.Chat


data class ChatChannel(val userIds: MutableList<String>) {
    constructor() : this(mutableListOf())
}