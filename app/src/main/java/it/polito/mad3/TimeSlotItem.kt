package it.polito.mad3

data class TimeSlotItem (
    var id: String,
    var userId: String,
    var title:  String,
    var description:  String,
    var date: String,
    var time: String,
    var duration: String,
    var location: String,
    var skills: String,
    var isEnabled: Boolean,
    var isActive: Boolean,
    )

