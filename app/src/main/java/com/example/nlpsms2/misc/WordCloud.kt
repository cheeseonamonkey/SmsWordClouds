package com.example.nlpsms2.misc

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

fun openWordCloudInBrowser(context: Context, wordsText: String) {
    try {
        val url = "https://quickchart.io/wordcloud?text=${wordsText}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        // Ensure there's a browser app that can handle this intent
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Log.e("MainActivity", "No browser app available to open the URL")
        }
    } catch (e: Exception) {
        Log.e("MainActivity", "Error opening word cloud URL", e)
    }
}
