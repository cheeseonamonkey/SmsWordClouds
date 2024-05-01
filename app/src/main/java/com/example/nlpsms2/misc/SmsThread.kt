package com.example.nlpsms2.misc

data class SmsThread(
    val threadId: Long,
    val address: String,
    val date: Long,
    val contactName: String = ""
)


data class SmsMessage(
    val address: String,
    val date: Long,
    val body: String
)
