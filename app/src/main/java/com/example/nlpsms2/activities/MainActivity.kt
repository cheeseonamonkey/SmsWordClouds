package com.example.nlpsms2.activities


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.nlpsms2.R
import com.example.nlpsms2.misc.SmsMessageRepository
import com.example.nlpsms2.misc.SmsThreadRepository
import com.example.nlpsms2.misc.WordCloudTask
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var smsThreadRepository: SmsThreadRepository
    private lateinit var smsMessageRepository: SmsMessageRepository

    private final val CREATE_DOCUMENT_REQUEST_CODE = 1023;
    private final val CHECK_PERMISSIONS_REQUEST_CODE = 467
            ;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the repositories
        smsThreadRepository = SmsThreadRepository(applicationContext)
        smsMessageRepository = SmsMessageRepository(applicationContext)

        // Check permissions on launch
        checkPermissions()

        val threads = smsThreadRepository.getSmsThreads()

        if (threads.isEmpty()) {
            Log.w("MainActivity", "No SMS threads found.")
            Toast.makeText(this, "No SMS threads found.", Toast.LENGTH_SHORT).show()
        } else {
            findViewById<ComposeView>(R.id.compose_view).setContent {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .verticalScroll(ScrollState(0))
                ) {
                    // Display threads and handle click events
                    threads.forEach { thread ->
                        BasicText(
                            text = "#${thread.threadId}: ${thread.contactName}\n Address: ${thread.address}, Last message: ${thread.date}",
                            modifier = Modifier
                                .clickable {

                                    val messages = smsMessageRepository.getAllMessagesByAddress(thread.address)
                                    val allMessages = messages.joinToString("\n") { it.body }

                                    val wordCloudTask = WordCloudTask(this@MainActivity, allMessages)
                                    wordCloudTask.execute();

                            }
                                .padding(2.dp)
                                .shadow(1.dp)
                                .padding(3.dp)


                        )
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        // Permission check logic
    }



    fun createOutputFile(context: Context, fileName: String): Uri {
        val file = File(context.filesDir, fileName)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                // Use the uri to save the image data
                saveImageToUri(uri)
            }



        }
    }

    private fun saveImageToUri(uri: Uri) {
        val inputStream = this.contentResolver.openInputStream(uri)
        val outputStream =this.contentResolver.openOutputStream(uri)
        inputStream?.use { input ->
            outputStream?.use { output ->
                input.copyTo(output)
            }
        }
    }


}
