package app.propubg.main.broadcasts.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.propubg.currentLanguage
import app.propubg.main.tournaments.model.tournament
import app.propubg.realmApp
import io.realm.Case
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.Sort
import io.realm.mongodb.sync.SyncConfiguration
import org.bson.types.ObjectId

class BroadcastsViewModel:ViewModel() {

    private lateinit var realm: Realm
    val realmReady = MutableLiveData<Boolean>()
    val searchString = MutableLiveData<String>()
    var advertClosed = false

    init {
        val user = realmApp.currentUser()!!
        val config = SyncConfiguration.Builder(user, "news")
            .waitForInitialRemoteData()
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .syncClientResetStrategy { session, error ->
                Log.v("DASD", error.message?:"")
                session.stop()
                session.start()
            }
            .build()
        Realm.getInstanceAsync(config, object : Realm.Callback() {
            override fun onSuccess(realm_: Realm) {
                realm = realm_
                realmReady.postValue(true)
            }
        })
    }

    fun getBroadcastsLive(): OrderedRealmCollection<broadcast?>?{
        return realm.where(broadcast::class.java).isNotNull("title")
            .equalTo("status","Live")
            .sort("date", Sort.DESCENDING).findAllAsync()
    }

    fun getBroadcastsPast(): OrderedRealmCollection<broadcast?>?{
        return realm.where(broadcast::class.java).isNotNull("title")
            .equalTo("status","Past")
            .sort("date", Sort.DESCENDING).findAllAsync()
    }

    fun getBroadcastsUpcoming(): OrderedRealmCollection<broadcast?>?{
        return realm.where(broadcast::class.java).isNotNull("title")
            .equalTo("status","Upcoming")
            .sort("date", Sort.ASCENDING).findAllAsync()
    }

    fun searchBroadcastsLive(text: String): OrderedRealmCollection<broadcast?>?{
        return if (currentLanguage =="ru"){
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Live").findAllAsync().where()
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("teamsList", text, Case.INSENSITIVE)
                .or().contains("stage_ru", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Live").findAllAsync().where()
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("teamsList", text, Case.INSENSITIVE)
                .or().contains("stage_en", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
    }

    fun searchBroadcastsLiveLocal(text: String): List<broadcast>{
        return if (currentLanguage =="ru"){
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Live").findAllAsync()
                .sort("date", Sort.DESCENDING)
                .filter {
                    it.title?.lowercase()?.contains(text.lowercase())?:false
                            || it.stage_ru?.lowercase()?.contains(text.lowercase())?:false
                            || it.teamsList.toString().lowercase().contains(text.lowercase())
                }
        } else {
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Live").findAllAsync()
                .sort("date", Sort.DESCENDING)
                .filter {
                    it.title?.lowercase()?.contains(text.lowercase())?:false
                            || it.stage_en?.lowercase()?.contains(text.lowercase())?:false
                            || it.teamsList.toString().lowercase().contains(text.lowercase())
                }

        }
    }

    fun searchBroadcastsPast(text: String): OrderedRealmCollection<broadcast?>?{
        return if (currentLanguage =="ru"){
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Past").findAllAsync().where()
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("teamsList", text, Case.INSENSITIVE)
                .or().contains("stage_ru", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Past").findAllAsync().where()
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("teamsList", text, Case.INSENSITIVE)
                .or().contains("stage_en", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
    }

    fun searchBroadcastsPastLocal(text: String): List<broadcast>{
        return if (currentLanguage =="ru"){
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Past").findAllAsync()
                .sort("date", Sort.DESCENDING)
                .filter {
                    it.title?.lowercase()?.contains(text.lowercase())?:false
                            || it.stage_ru?.lowercase()?.contains(text.lowercase())?:false
                            || it.teamsList.toString().lowercase().contains(text.lowercase())
                }
        } else {
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Past").findAllAsync()
                .sort("date", Sort.DESCENDING)
                .filter {
                    it.title?.lowercase()?.contains(text.lowercase())?:false
                            || it.stage_en?.lowercase()?.contains(text.lowercase())?:false
                            || it.teamsList.toString().lowercase().contains(text.lowercase())
                }

        }
    }

    fun searchBroadcastsUpcoming(text: String): OrderedRealmCollection<broadcast?>?{
        return if (currentLanguage =="ru"){
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Upcoming").findAllAsync().where()
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("teamsList", text, Case.INSENSITIVE)
                .or().contains("stage_ru", text, Case.INSENSITIVE)
                .sort("date", Sort.ASCENDING).findAllAsync()
        } else {
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Upcoming").findAllAsync().where()
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("teamsList", text, Case.INSENSITIVE)
                .or().contains("stage_en", text, Case.INSENSITIVE)
                .sort("date", Sort.ASCENDING).findAllAsync()
        }
    }

    fun searchBroadcastsUpcomingLocal(text: String): List<broadcast>{
        return if (currentLanguage =="ru"){
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Upcoming").findAllAsync()
                .sort("date", Sort.ASCENDING)
                .filter {
                    it.title?.lowercase()?.contains(text.lowercase())?:false
                            || it.stage_ru?.lowercase()?.contains(text.lowercase())?:false
                            || it.teamsList.toString().lowercase().contains(text.lowercase())
                }
        } else {
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Upcoming").findAllAsync()
                .sort("date", Sort.ASCENDING)
                .filter {
                    it.title?.lowercase()?.contains(text.lowercase())?:false
                            || it.stage_en?.lowercase()?.contains(text.lowercase())?:false
                            || it.teamsList.toString().lowercase().contains(text.lowercase())
                }

        }
    }

    fun getTournamentById(id: ObjectId): tournament? {
        return realm.where(tournament::class.java)
            .equalTo("_id", id).findFirst()
    }

    override fun onCleared() {
        realm.close()
        super.onCleared()
    }

}