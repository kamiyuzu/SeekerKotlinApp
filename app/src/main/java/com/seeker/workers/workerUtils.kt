package com.seeker.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.seeker.R

val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence = "Seeker JWT Notifications"
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION = "Shows notifications whenever work starts"
val NOTIFICATION_TITLE: CharSequence = "Seeker JWT"
const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
const val NOTIFICATION_ID = 1

fun makeStatusNotification(message: String, context: Context, valid: Boolean) {
    Log.println(Log.DEBUG, "makeStatusNotification", "Starting...")
    // Make a channel if necessary
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Log.println(Log.DEBUG, "makeStatusNotification", "${Build.VERSION.SDK_INT} greater than: ${Build.VERSION_CODES.O}...")
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = VERBOSE_NOTIFICATION_CHANNEL_NAME
        val description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description

        // Add the channel
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        notificationManager?.createNotificationChannel(channel)
    }

    // Create the notification
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.security_svgrepo_com)
        .setContentTitle(NOTIFICATION_TITLE)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVibrate(LongArray(0))

    // Show the notification
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
        Log.println(Log.DEBUG, "makeStatusNotification", "Launching notification...")
        if (valid) NotificationManagerCompat
            .from(context)
            .notify(
            NOTIFICATION_ID,
            builder.setColor(ContextCompat.getColor(context, R.color.teal_200)).build()
        )
        else NotificationManagerCompat
            .from(context)
            .notify(
                NOTIFICATION_ID,
                builder.setColor(ContextCompat.getColor(context, R.color.red)).build()
            )
    }
    else Log.println(Log.DEBUG, "makeStatusNotification", "NOT LAUNCHING...")
}