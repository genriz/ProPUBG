package app.propubg.utils

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import app.propubg.R
import app.propubg.currenciesFromJson
import com.google.firebase.messaging.FirebaseMessaging
import java.io.IOException
import java.util.*

class AppUtils {

    fun getDateHeader(context: Context, date: Date): String {
        return if (DateUtils.isToday(date.time)){
            context.getString(R.string.today_header)
        } else if (DateUtils.isToday(date.time + DateUtils.DAY_IN_MILLIS)){
            context.getString(R.string.yesterday_header)
        } else {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY,0)
            cal.set(Calendar.MINUTE,0)
            cal.set(Calendar.SECOND,0)
            cal.set(Calendar.MILLISECOND,0)
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            if (date.time>cal.timeInMillis){
                context.getString(R.string.week_header)
            } else context.getString(R.string.earlier_header)
        }
    }

    fun resubscribeTopicsFCM(context: Context, prevLang: String, newLang: String){
        val fcm = FirebaseMessaging.getInstance()
        LocalData.topics.forEach { topic ->
            if (context.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
                    .getBoolean(topic, true)){
                fcm.unsubscribeFromTopic("$topic$prevLang")
                    .addOnSuccessListener {
                        Log.v("DASD", "unsubscribed $topic$prevLang")
                    }
                    .addOnFailureListener {
                        Log.v("DASD", "unsubscribe failed $topic$prevLang")
                    }
                fcm.subscribeToTopic("$topic$newLang")
                    .addOnSuccessListener {
                        Log.v("DASD", "subscribed $topic$newLang")
                    }
                    .addOnFailureListener {
                        Log.v("DASD", "subscribe failed $topic$newLang")
                    }
            }
        }
    }

    fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    fun getCurrencySymbolFromJson(code: String): String{
        var cs = ""
        currenciesFromJson.forEach {
            if (it.alpha==code) cs = it.symbol
        }
        return cs
    }
}