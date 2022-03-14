package app.propubg

import android.app.Application
import app.propubg.login.model.UserRealm
import app.propubg.login.model.configuration
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.User




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
var firstStart = true
var appConfig: configuration? = null


class AppPubg: Application() {

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)

    }

}