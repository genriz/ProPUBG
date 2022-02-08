package app.propubg.utils

import android.content.Context
import app.propubg.R
import app.propubg.main.menu.model.NotificationItem
import app.propubg.main.menu.model.NotificationTitle

object LocalData {

    private val notificationTitles = ArrayList<NotificationTitle>()

    fun getNotificationTitles(context: Context): ArrayList<NotificationTitle> {
        if (notificationTitles.isEmpty()){
            notificationTitles.addAll(ArrayList<NotificationTitle>().apply {
                add(NotificationTitle(context.getString(R.string.news),
                    ArrayList<NotificationItem>().apply {
                        add(NotificationItem(context.getString(R.string.global_), false))
                        add(NotificationItem(context.getString(R.string.cis), false))
                        add(NotificationItem(context.getString(R.string.eu), false))
                        add(NotificationItem(context.getString(R.string.na), false))
                        add(NotificationItem(context.getString(R.string.mena), false))
                    }, true))
                add(NotificationTitle(context.getString(R.string.reshuffles),
                    ArrayList<NotificationItem>().apply {
                        add(NotificationItem(context.getString(R.string.global_), false))
                        add(NotificationItem(context.getString(R.string.cis), false))
                        add(NotificationItem(context.getString(R.string.eu), false))
                        add(NotificationItem(context.getString(R.string.na), false))
                        add(NotificationItem(context.getString(R.string.mena), false))
                    }, true))
                add(NotificationTitle(context.getString(R.string.tournaments),
                    ArrayList<NotificationItem>().apply {
                        add(NotificationItem(context.getString(R.string.cis), false))
                        add(NotificationItem(context.getString(R.string.eu), false))
                        add(NotificationItem(context.getString(R.string.na), false))
                        add(NotificationItem(context.getString(R.string.mena), false))
                    }, true))
                add(NotificationTitle(context.getString(R.string.broadcasts),
                    ArrayList<NotificationItem>().apply {
                        add(NotificationItem(context.getString(R.string.cis), false))
                        add(NotificationItem(context.getString(R.string.eu), false))
                        add(NotificationItem(context.getString(R.string.na), false))
                        add(NotificationItem(context.getString(R.string.mena), false))
                    }, true))
                add(NotificationTitle(context.getString(R.string.content),
                    ArrayList<NotificationItem>().apply {
                        add(NotificationItem(context.getString(R.string.cis), false))
                        add(NotificationItem(context.getString(R.string.eu), false))
                        add(NotificationItem(context.getString(R.string.na), false))
                        add(NotificationItem(context.getString(R.string.mena), false))
                    }, true))
                add(NotificationTitle(context.getString(R.string.results_tournaments),
                    ArrayList<NotificationItem>().apply {
                        add(NotificationItem(context.getString(R.string.cis), false))
                        add(NotificationItem(context.getString(R.string.eu), false))
                        add(NotificationItem(context.getString(R.string.na), false))
                        add(NotificationItem(context.getString(R.string.mena), false))
                    }, true))
            })
        }
        return notificationTitles
    }

}