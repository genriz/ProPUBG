package app.propubg.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationManager

import android.app.NotificationChannel
import android.content.Context

import android.os.Build
import android.app.PendingIntent

import android.content.Intent
import android.app.Notification

import androidx.core.app.NotificationCompat
import app.propubg.R
import app.propubg.login.ui.StartActivity
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import java.io.InputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL


class FCMService: FirebaseMessagingService() {
    override fun onNewToken(p0: String) {

    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.v("DASD keys", message.data.keys.toString())
        Log.v("DASD data", message.data.entries.toString())
        Log.v("DASD title", message.notification?.title?:"empty")
        Log.v("DASD body", message.notification?.body?:"empty")

        createChannel()
        sendNotification(message)
    }

    private fun createChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel =
                NotificationChannel("propubg", "propubg_fcm", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun sendNotification(message: RemoteMessage) {
        val i = Intent(this, StartActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, i, 0)
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, "propubg")
                .setContentTitle(message.data["title"])
                .setContentText(message.data["text"])
                .setStyle(NotificationCompat.BigPictureStyle()
                    .bigPicture(getBitmapFromUrl(message.data["image"])))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setSmallIcon(R.drawable.app_logo)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1241, builder.build())
    }

    private fun getBitmapFromUrl(imageUrl: String?): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}