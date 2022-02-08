package app.propubg.main.broadcasts.model

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
        val user = realmApp.currentUser()
        val config = SyncConfiguration.Builder(user, "news")
            .waitForInitialRemoteData()
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
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
            .sort("date", Sort.DESCENDING).findAllAsync()
    }

    fun searchBroadcastsLive(text: String): OrderedRealmCollection<broadcast?>?{
        return if (currentLanguage =="ru"){
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Live")
                .contains("title", text, Case.SENSITIVE)
                .or().contains("teamsList", text, Case.INSENSITIVE)
                .or().contains("stage_ru", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Live")
                .contains("title", text, Case.SENSITIVE)
                .or().contains("teamsList", text, Case.INSENSITIVE)
                .or().contains("stage_en", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
    }

    fun searchBroadcastsPast(text: String): OrderedRealmCollection<broadcast?>?{
        return if (currentLanguage =="ru"){
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Past")
                .contains("title", text, Case.SENSITIVE)
                .or().contains("teamsList", text, Case.INSENSITIVE)
                .or().contains("stage_ru", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Past")
                .contains("title", text, Case.SENSITIVE)
                .or().contains("teamsList", text, Case.INSENSITIVE)
                .or().contains("stage_en", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
    }

    fun searchBroadcastsUpcoming(text: String): OrderedRealmCollection<broadcast?>?{
        return if (currentLanguage =="ru"){
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Upcoming")
                .contains("title", text, Case.SENSITIVE)
                .or().contains("teamsList", text, Case.INSENSITIVE)
                .or().contains("stage_ru", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(broadcast::class.java).isNotNull("title")
                .equalTo("status","Upcoming")
                .contains("title", text, Case.SENSITIVE)
                .or().contains("teamsList", text, Case.INSENSITIVE)
                .or().contains("stage_en", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
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