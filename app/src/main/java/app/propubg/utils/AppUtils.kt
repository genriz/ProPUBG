package app.propubg.utils

import android.content.Context
import android.text.format.DateUtils
import androidx.appcompat.app.AppCompatActivity
import app.propubg.R
import app.propubg.currentLanguage
import com.google.firebase.messaging.FirebaseMessaging
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
                fcm.subscribeToTopic("$topic$newLang")
            }
        }
    }
}