package com.example.nlpsms2.misc


import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileSaver(private val context: Context) {

    // Function to save text to a file in the downloads folder
    fun saveTextToDownload(text: String, fileName: String) {
        try {
            // Get the downloads directory
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            // Create the file in the downloads directory
            val file = File(downloadsDir, fileName)

            // Write the text data to the file
            FileOutputStream(file).use { fos ->
                fos.write(text.toByteArray())
            }

            // Show a toast notification to indicate the file has been saved
            Toast.makeText(context, "File saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Log.e("FileSaver", "Failed to save text to downloads", e)
            Toast.makeText(context, "Error saving file", Toast.LENGTH_LONG).show()
        }
    }
}
