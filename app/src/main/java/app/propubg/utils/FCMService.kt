package app.propubg.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService: FirebaseMessagingService() {
    override fun onNewToken(p0: String) {

    }

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.v("DASD keys", p0.data.keys.toString())
        Log.v("DASD data", p0.data.entries.toString())
        Log.v("DASD keys", p0.notification?.body?:"empty")
    }
}