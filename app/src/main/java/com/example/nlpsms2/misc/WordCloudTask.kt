package com.example.nlpsms2.misc

import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class WordCloudTask(
    private val context: Context,
    private val text: String,
) : AsyncTask<Void, Void, Boolean>() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private lateinit var outputFilePath: String

    override fun doInBackground(vararg params: Void?): Boolean {
        val json = JSONObject().apply {
            put("format", "png")
            put("width", 1000)
            put("height", 1000)
            put("fontFamily", "sans-serif")
            put("fontScale", 15)
            put("scale", "linear")
            put("text", text)
        }

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
        val request = Request.Builder()
            .url("https://quickchart.io/wordcloud")
            .post(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.bytes()?.let { responseBody ->


                        val dateFormatter = SimpleDateFormat("MMMdd_mmss", Locale.getDefault())
                        val timestamp = dateFormatter.format(Date())
                        val fileName = "WordCloud_$timestamp.png"


                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val outputFile = File(downloadsDir, fileName)
                        outputFilePath = outputFile.absolutePath

                        // Write the response data to the file
                        outputFile.outputStream().use { outputStream ->
                            outputStream.write(responseBody)
                        }

                        true
                    } ?: false
                } else {
                    false
                }
            }
        } catch (e: IOException) {
            Log.e("CreateWordCloudTask", "Error during POST request", e)
            false
        }
    }

    override fun onPostExecute(result: Boolean) {
        if (result) {
            // Inform the user where the file has been saved
            Toast.makeText(context, "Word cloud saved to:\n${outputFilePath.split("/").takeLast(2).joinToString("/")}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to create word cloud.", Toast.LENGTH_SHORT).show()
        }
    }
}
