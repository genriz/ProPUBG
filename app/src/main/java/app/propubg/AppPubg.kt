package app.propubg

import android.app.Application
import android.content.Context
import app.propubg.login.model.UserRealm
import com.google.gson.Gson
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.User
import com.mixpanel.android.mpmetrics.MixpanelAPI




val realmApp by lazy {App(AppConfiguration.Builder(BuildConfig.MONGODB_REALM_APP_ID)
    .build())}
var currentLanguage = ""
var currentUser: UserRealm? = null
var currentUserRealm: User? = null
var prevPhone: String? = null
var verificationId: String? = null
var timerSaved = -1
var timeExit = -1L
var currentTimer = 60


class AppPubg: Application() {

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)

    }

}