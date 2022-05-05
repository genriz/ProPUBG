package app.propubg.utils

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import app.propubg.R
import app.propubg.main.menu.model.NotificationItem
import app.propubg.main.menu.model.NotificationTitle

object LocalData {

    private val notificationTitles = ArrayList<NotificationTitle>()

    fun getNotificationTitles(context: Context): ArrayList<NotificationTitle> {
        notificationTitles.clear()
        notificationTitles.addAll(ArrayList<NotificationTitle>().apply {
            add(NotificationTitle(context.getString(R.string.reshuffles),
                ArrayList<NotificationItem>().apply {
                    add(NotificationItem(context.getString(R.string.cis), getPref(context, 0),0 ))
                    add(NotificationItem(context.getString(R.string.eu), getPref(context, 1), 1))
                    add(NotificationItem(context.getString(R.string.global_), getPref(context, 2), 2))
                }, true))
            add(NotificationTitle(context.getString(R.string.news),
                ArrayList<NotificationItem>().apply {
                    add(NotificationItem(context.getString(R.string.game), getPref(context, 3), 3))
                    add(NotificationItem(context.getString(R.string.cis), getPref(context, 4), 4))
                    add(NotificationItem(context.getString(R.string.eu), getPref(context, 5), 5))
                    add(NotificationItem(context.getString(R.string.global_), getPref(context, 6),6 ))
                }, true))
            add(NotificationTitle(context.getString(R.string.tournaments),
                ArrayList<NotificationItem>().apply {
                    add(NotificationItem(context.getString(R.string.cis), getPref(context, 7), 7))
                    add(NotificationItem(context.getString(R.string.eu), getPref(context, 8), 8))
                    add(NotificationItem(context.getString(R.string.global_), getPref(context, 9), 9))
                }, true))
            add(NotificationTitle(context.getString(R.string.broadcasts),
                ArrayList<NotificationItem>().apply {
                    add(NotificationItem(context.getString(R.string.cis), getPref(context, 10), 10))
                    add(NotificationItem(context.getString(R.string.eu), getPref(context, 11), 11))
                    add(NotificationItem(context.getString(R.string.global_), getPref(context, 12), 12))
                }, true))
            add(NotificationTitle(context.getString(R.string.content),
                ArrayList<NotificationItem>().apply {
                    add(NotificationItem(context.getString(R.string.content_learn), getPref(context, 13), 13))
                    add(NotificationItem(context.getString(R.string.content_interview), getPref(context, 14), 14))
                }, true))
            add(NotificationTitle(context.getString(R.string.results_tournaments),
                ArrayList<NotificationItem>().apply {
                    add(NotificationItem(context.getString(R.string.cis), getPref(context, 15), 15))
                    add(NotificationItem(context.getString(R.string.eu), getPref(context, 16), 16))
                    add(NotificationItem(context.getString(R.string.global_), getPref(context, 17), 17))
                }, true))
            add(NotificationTitle(context.getString(R.string.partners),
                ArrayList<NotificationItem>().apply {
                    add(NotificationItem(context.getString(R.string.cis), getPref(context, 18), 18))
                    add(NotificationItem(context.getString(R.string.eu), getPref(context, 19), 19))
                    add(NotificationItem(context.getString(R.string.global_), getPref(context, 20), 20))
                }, true))
        })
        return notificationTitles
    }

    private fun getPref(context: Context, index: Int):Boolean{
        Log.v("DASD", "${topics.elementAt(index)} - ${context.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
            .getBoolean(topics.elementAt(index), true)}")
        return context.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
            .getBoolean(topics.elementAt(index), true)
    }

    var topics = mutableSetOf("reshufflesCIS_","reshufflesEU_","reshufflesOthers_",
        "newsGame_","newsCIS_","newsEU_","newsOthers_",
        "tournamentsCIS_","tournamentsEU_","tournamentsOthers_",
        "broadcastsCIS_","broadcastsEU_","broadcastsOthers_",
        "contentEducational_","contentInterview_","resultsOfTournamentsCIS_",
        "resultsOfTournamentsEU_","resultsOfTournamentsOthers_",
        "discordPartnersCIS_","discordPartnersEU_","discordPartnersOthers_")

}