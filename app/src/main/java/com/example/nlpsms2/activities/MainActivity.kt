package com.example.nlpsms2.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.nlpsms2.R
import com.example.nlpsms2.misc.openWordCloudInBrowser
import java.util.ArrayList

val PERMISSION_REQUEST_CODE = 121
val DEFAULT_CHAT_REQUEST_CODE = 122

data class SmsThread(
    val threadId: Long,
    val address: String,
    val date: Long
)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check permissions on launch
        checkPermissions()

        val threads = getSmsThreads(applicationContext)

        if (threads.isEmpty()) {
            Log.w("MainActivity", "No SMS threads found.")
            Toast.makeText(this, "No SMS threads found.", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("MainActivity", "threads: $threads")

            findViewById<ComposeView>(R.id.compose_view).setContent {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Display threads and handle click events
                    threads.forEach { thread ->
                        BasicText(
                            text = "Thread ID: ${thread.threadId}, Address: ${thread.address}, Date: ${thread.date}",
                            modifier = Modifier.clickable {
                                Log.d("MainActivity", "Clicked thread: ${thread.threadId}")
                                logMessagesByAddress(applicationContext, thread.address)
                            }
                        )
                    }
                }
            }
        }
    }




    fun logMessagesByAddress(context: Context, address: String) {
        val uri = Uri.parse("content://sms/") // URI to access SMS messages
        val projection = arrayOf("address", "date", "body") // Fields to retrieve from the SMS messages

        val selection = "address = ?" // Query only messages with the given address
        val selectionArgs = arrayOf(address) // Arguments for the selection clause

        val cursor: Cursor? = context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "date ASC" // Sort messages in ascending order by date
        )

        if (cursor == null) {
            Log.e("MainActivity", "Cursor is null while querying SMS messages for address: $address")
            return
        }

        var messagesConcatOut = ""

        cursor.use {
            while (it.moveToNext()) {
                val date = it.getLong(it.getColumnIndexOrThrow("date"))
                val body = it.getString(it.getColumnIndexOrThrow("body"))
                messagesConcatOut += body + "\n";
                Log.d("MainActivity", "SMS from address '$address', Date: $date, Body: $body")
            }
        }

        Log.d("TAG", "messagesConcatOut: ${messagesConcatOut}")

        openWordCloudInBrowser(this, messagesConcatOut.toString())

    }


    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.RECEIVE_MMS,
            Manifest.permission.READ_CONTACTS
        )

        val listPermissionsNeeded = ArrayList<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(applicationContext, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission)
            }
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }
}

fun getSmsThreads(context: Context): List<SmsThread> {
    val uri = Uri.parse("content://mms-sms/conversations")
    val projection = arrayOf("_id", "address", "date")

    val smsThreads = mutableListOf<SmsThread>()

    val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)

    cursor?.use {
        while (it.moveToNext()) {
            val id = it.getLong(it.getColumnIndexOrThrow("_id"))
            val address = it.getString(it.getColumnIndexOrThrow("address")) ?: "NULL!"
            val date = it.getLong(it.getColumnIndexOrThrow("date"))
            Log.d("TAG", "${id} ${address} ${date}")
            val smsThread = SmsThread(id, address, date)
            smsThreads.add(smsThread)
        }
    } ?: Log.e("MainActivity", "Cursor is null while querying SMS threads")

    Log.d("MainActivity", "Total threads retrieved: ${smsThreads.size}")

    return smsThreads
}
