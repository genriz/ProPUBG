package app.propubg.main.tournaments.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.propubg.currentLanguage
import app.propubg.realmApp
import io.realm.Case
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.Sort
import io.realm.mongodb.sync.SyncConfiguration
import org.bson.types.ObjectId

class TournamentsViewModel:ViewModel() {

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

    fun getTournamentsOpen(): OrderedRealmCollection<tournament?>?{
        return realm.where(tournament::class.java).isNotNull("title")
            .equalTo("status","Open")
            .sort("date", Sort.DESCENDING).findAllAsync()
    }

    fun searchTournamentsOpen(text: String): OrderedRealmCollection<tournament?>?{
        return if (currentLanguage =="ru"){
            realm.where(tournament::class.java).isNotNull("title")
                .equalTo("status","Open")
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("mode", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .or().contains("text_ru", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(tournament::class.java).isNotNull("title")
                .equalTo("status","Open")
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("mode", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .or().contains("text_en", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
    }

    fun getTournamentsClosed(): OrderedRealmCollection<tournament?>?{
        return realm.where(tournament::class.java).isNotNull("title")
            .equalTo("status","Closed")
            .sort("date", Sort.DESCENDING).findAllAsync()
    }

    fun searchTournamentsClosed(text: String): OrderedRealmCollection<tournament?>?{
        return if (currentLanguage =="ru"){
            realm.where(tournament::class.java).isNotNull("title")
                .equalTo("status","Closed")
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("mode", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .or().contains("text_ru", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(tournament::class.java).isNotNull("title")
                .equalTo("status","Closed")
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("mode", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .or().contains("text_en", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
    }

    fun getTournamentsUpcoming(): OrderedRealmCollection<tournament?>?{
        return realm.where(tournament::class.java).isNotNull("title")
            .equalTo("status","Upcoming")
            .sort("date", Sort.DESCENDING).findAllAsync()
    }

    fun searchTournamentsUpcoming(text: String): OrderedRealmCollection<tournament?>?{
        return if (currentLanguage =="ru"){
            realm.where(tournament::class.java).isNotNull("title")
                .equalTo("status","Upcoming")
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("mode", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .or().contains("text_ru", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(tournament::class.java).isNotNull("title")
                .equalTo("status","Upcoming")
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("mode", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .or().contains("text_en", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
    }

    fun getTournamentById(id:ObjectId): tournament? {
        return realm.where(tournament::class.java)
            .equalTo("_id", id).findFirst()
    }

    override fun onCleared() {
        realm.close()
        super.onCleared()
    }

}