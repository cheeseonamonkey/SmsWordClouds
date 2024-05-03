package com.example.nlpsms2.misc

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log

class SmsThreadRepository(private val context: Context) {
    fun getSmsThreads(): List<SmsThread> {
        val uri = Uri.parse("content://mms-sms/conversations")
        val projection = arrayOf("_id", "address", "date")

        val smsThreads = mutableListOf<SmsThread>()

        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow("_id"))
                val address = it.getString(it.getColumnIndexOrThrow("address")) ?: ""
                val date = it.getLong(it.getColumnIndexOrThrow("date"))
                val contactName = getContactName(address) ?: ""
                Log.d("SmsThreadRepository", "Retrieved thread - ID: $id, Address: $address, Date: $date, Contact Name: $contactName")
                val smsThread = SmsThread(id, address, date, contactName)
                smsThreads.add(smsThread)
            }
        } ?: Log.e("SmsThreadRepository", "Cursor is null while querying SMS threads")

        Log.d("SmsThreadRepository", "Total threads retrieved: ${smsThreads.size}")

        return smsThreads
    }

    private fun getContactName(phoneNumber: String): String? {
        try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

            var contactName = ""

            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {

                    contactName =
                        cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                }
            }

            return contactName
        }catch(e:Exception){
            Log.e("TAG", "getContactName: error getting contact name:\n${e.message}")
            return null;
        }
    }
}
