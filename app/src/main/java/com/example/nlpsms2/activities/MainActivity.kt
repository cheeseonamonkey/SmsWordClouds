package com.example.nlpsms2.activities


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.nlpsms2.R
import com.example.nlpsms2.misc.SmsMessageRepository
import com.example.nlpsms2.misc.SmsThreadRepository
import com.example.nlpsms2.misc.WordCloudTask
import java.io.File
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var smsThreadRepository: SmsThreadRepository
    private lateinit var smsMessageRepository: SmsMessageRepository

    private final val CREATE_DOCUMENT_REQUEST_CODE = 1023;
    private final val CHECK_PERMISSIONS_REQUEST_CODE = 467
    private final val REQUEST_CODE_READ_CONTACTS = 2434;



    private fun checkReadContactsPermission() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if(! granted)
            requestReadContactsPermission()
    }

    private fun requestReadContactsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CONTACTS),
            REQUEST_CODE_READ_CONTACTS
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        checkReadContactsPermission()


        val txtGoogleFont = findViewById<TextView>(R.id.txtGoogleFont)
        val txtMinWordLength = findViewById<TextView>(R.id.txtMinWordLength)
        val swchUseLogScale = findViewById<Switch>(R.id.swchUseLogScale)
        swchUseLogScale.setOnCheckedChangeListener { swch, isChecked ->
            if(isChecked)
                swch.text = "Use exponential scaling"
            else
                swch.text = "Use logarithmic scaling"


        }

        txtGoogleFont.hint = listOf( "EB Garamond", "Helvetica", "Rubik", "Alegreya", "Karla", "Platypi", "Lato", "Poppins", "Raleway", "Metal", "Grandstander", "Changa",  "Gruppo",  "Inconsolata", "Roboto", "Arial", "Arial", "Georgia", "Calibri", "Jacques Francois Shadow",  "Kavoon",  "Megrim",  "Uncial Antiqua", "Noto Sans", "Exo 2", "Prompt", "Arvo" ).random()


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
                            text = "${ 
                                if(thread.contactName.isEmpty())
                                    thread.address.takeLast(10)
                                else
                                    thread.contactName}\n ${Date(thread.date).toString().take(10)}",
                            modifier = Modifier
                                .clickable {

                                    if(txtMinWordLength.text.toString().isNullOrEmpty())
                                        txtMinWordLength.text = "4"

                                    if(txtGoogleFont.text.isEmpty())
                                        txtGoogleFont.text = txtGoogleFont.hint.toString()


                                    val messages = smsMessageRepository.getAllMessagesByAddress(thread.address)
                                    val allMessages = messages.joinToString("\n") { it.body }

                                    val wordCloudTask = WordCloudTask(this@MainActivity, allMessages, loadGoogleFonts=txtGoogleFont.text.toString(), fontFamily=txtGoogleFont.text.toString(), useLogScaling=swchUseLogScale.isChecked, minWordLength=txtMinWordLength.text.toString().toInt())
                                    wordCloudTask.execute();

                            }
                                .fillMaxWidth()
                                .padding(2.dp)
                                .shadow(1.dp)
                                .padding(2.dp)



                        )
                    }
                }
            }
        }




    }

    private fun checkPermissions() {
        // Permission check logic
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
