package com.example.nlpsms2.misc

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.example.nlpsms2.misc.SmsMessage

class SmsMessageRepository(private val context: Context) {

    // Function to get SMS messages by address
    private fun getSmsMessagesByAddress(address: String): List<SmsMessage> {
        val uri = Uri.parse("content://sms/")
        val projection = arrayOf("address", "date", "body")
        val selection = "address = ?"
        val selectionArgs = arrayOf(address)

        val smsMessages = mutableListOf<SmsMessage>()

        val cursor: Cursor? = context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "date ASC"
        )

        cursor?.use {
            val addressIndex = it.getColumnIndexOrThrow("address")
            val dateIndex = it.getColumnIndexOrThrow("date")
            val bodyIndex = it.getColumnIndexOrThrow("body")

            while (it.moveToNext()) {
                val smsAddress = it.getString(addressIndex)
                val smsDate = it.getLong(dateIndex)
                val smsBody = it.getString(bodyIndex)

                if (smsAddress == address) {
                    smsMessages.add(SmsMessage(smsAddress, smsDate, smsBody))
                }
            }
        } ?: Log.e("SmsMessageRepository", "Failed to query SMS messages for address: $address")

        return smsMessages
    }

    // Function to get MMS messages by address
    private fun getMmsMessagesByAddress(address: String): List<SmsMessage> {
        val mmsMessages = mutableListOf<SmsMessage>()

        // Step 1: Query MMS IDs related to the given address
        val mmsUri = Uri.parse("content://mms/")
        val mmsProjection = arrayOf("_id", "date")
        val mmsSelection = "_id IN (SELECT msg_id FROM addr WHERE address = ?)"
        val mmsSelectionArgs = arrayOf(address)

        val mmsCursor: Cursor? = context.contentResolver.query(
            mmsUri,
            mmsProjection,
            mmsSelection,
            mmsSelectionArgs,
            "date ASC"
        )

        mmsCursor?.use {
            val idIndex = it.getColumnIndexOrThrow("_id")
            val dateIndex = it.getColumnIndexOrThrow("date")

            while (it.moveToNext()) {
                val messageId = it.getLong(idIndex)
                val mmsDate = it.getLong(dateIndex)

                // Step 2: Query parts to retrieve text
                val partsUri = Uri.parse("content://mms/$messageId/part")
                val partsProjection = arrayOf("ct", "text")
                val partsSelection = "ct = 'text/plain'"

                val partsCursor: Cursor? = context.contentResolver.query(
                    partsUri,
                    partsProjection,
                    partsSelection,
                    null,
                    null
                )

                partsCursor?.use { parts ->
                    val textIndex = parts.getColumnIndexOrThrow("text")
                    while (parts.moveToNext()) {
                        val body = parts.getString(textIndex)
                        if (!body.isNullOrEmpty()) {
                            mmsMessages.add(SmsMessage(address, mmsDate, body))
                        }
                    }
                } ?: Log.e("SmsMessageRepository", "Failed to retrieve parts for MMS ID: $messageId")
            }
        } ?: Log.e("SmsMessageRepository", "Failed to query MMS messages for address: $address")

        return mmsMessages
    }

    // Function to get both SMS and MMS messages by address
    fun getAllMessagesByAddress(address: String): List<SmsMessage> {
        val smsMessages = getSmsMessagesByAddress(address)
        val mmsMessages = getMmsMessagesByAddress(address)

        // Combine SMS and MMS messages, then sort by date
        val allMessages = mutableListOf<SmsMessage>()
        allMessages.addAll(smsMessages)
        allMessages.addAll(mmsMessages)
        Log.d("TAG", "SMS messages ${smsMessages.size}: ${smsMessages.toString()}")
        Log.d("TAG", "MMS messages ${mmsMessages.size}: ${mmsMessages.toString()}")
        allMessages.sortBy { it.date }

        return allMessages
    }
}