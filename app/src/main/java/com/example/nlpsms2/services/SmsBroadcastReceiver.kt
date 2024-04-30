package com.example.nlpsms2.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.nlpsms2.misc.log

class SmsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        "RECEIVED!".log()
        p0.toString().log();
        p1.toString().log();
    }
}