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
                .equalTo("status","Open").findAllAsync().where()
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("mode", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .or().contains("text_ru", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(tournament::class.java).isNotNull("title")
                .equalTo("status","Open").findAllAsync().where()
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("mode", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .or().contains("text_en", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
    }

    fun searchTournamentsOpenLocal(text: String): List<tournament>{
        return if (currentLanguage =="ru"){
            realm.where(tournament::class.java).isNotNull("title")
                .equalTo("status","Open").findAllAsync()
                .sort("date", Sort.DESCENDING)
                .filter {
                    it.title?.lowercase()?.contains(text.lowercase())?:false
                            ||it.mode?.lowercase()?.contains(text.lowercase())?:false
                            ||it.regions.toString().lowercase().contains(text.lowercase())
                            ||it.text_ru?.lowercase()?.contains(text.lowercase())?:false
                }
        } else {
            realm.where(tournament::class.java).isNotNull("title")
                .equalTo("status","Open").findAllAsync()
                .sort("date", Sort.DESCENDING)
                .filter {
                    it.title?.lowercase()?.contains(text.lowercase())?:false
                            ||it.mode?.lowercase()?.contains(text.lowercase())?:false
                            ||it.regions.toString().lowercase().contains(text.lowercase())
                            ||it.text_en?.lowercase()?.contains(text.lowercase())?:false
                }
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
                .equalTo("status","Closed").findAllAsync().where()
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("mode", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .or().contains("text_ru", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(tournament::class.java).isNotNull("title")
                .equalTo("status","Closed").findAllAsync().where()
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("mode", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .or().contains("text_en", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
    }

    fun searchTournamentsClosedLocal(text: String): List<tournament>{
        return if (currentLanguage =="ru"){
            realm.where(tournament::class.java).isNotNull("title")
                .equalTo("status","Closed").findAllAsync()
                .sort("date", Sort.DESCENDING)
                .filter {
                    it.title?.lowercase()?.contains(text.lowercase())?:false
                            ||it.mode?.lowercase()?.contains(text.lowercase())?:false
                            ||it.regions.toString().lowercase().contains(text.lowercase())
                            ||it.text_ru?.lowercase()?.contains(text.lowercase())?:false
                }
        } else {
            realm.where(tournament::class.java).isNotNull("title")
                .equalTo("status","Closed").findAllAsync()
                .sort("date", Sort.DESCENDING)
                .filter {
                    it.title?.lowercase()?.contains(text.lowercase())?:false
                            ||it.mode?.lowercase()?.contains(text.lowercase())?:false
                            ||it.regions.toString().lowercase().contains(text.lowercase())
                            ||it.text_en?.lowercase()?.contains(text.lowercase())?:false
                }
        }
    }

    fun getTournamentsUpcoming(): OrderedRealmCollection<tournament?>?{
        return realm.where(tournament::class.java).isNotNull("title")
            .equalTo("status","Upcoming")
            .sort("date", Sort.ASCENDING).findAllAsync()
    }

    fun searchTournamentsUpcoming(text: String): OrderedRealmCollection<tournament?>?{
        return if (currentLanguage =="ru"){
            realm.where(tournament::class.java).isNotNull("title")
                .equalTo("status","Upcoming").findAllAsync()
                .where()
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("mode", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .or().contains("text_ru", text, Case.INSENSITIVE)
                .sort("date", Sort.ASCENDING).findAllAsync()
        } else {
            realm.where(tournament::class.java).isNotNull("title")
                .equalTo("status","Upcoming").findAllAsync()
                .where()
                .contains("title", text, Case.INSENSITIVE)
                .or().contains("mode", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .or().contains("text_en", text, Case.INSENSITIVE)
                .sort("date", Sort.ASCENDING).findAllAsync()
        }
    }

    fun searchTournamentsUpcomingLocal(text: String): List<tournament>{
        return if (currentLanguage =="ru"){
            realm.where(tournament::class.java).isNotNull("title")
                .equalTo("status","Upcoming").findAllAsync()
                .sort("date", Sort.ASCENDING)
                .filter {
                    it.title?.lowercase()?.contains(text.lowercase())?:false
                            ||it.mode?.lowercase()?.contains(text.lowercase())?:false
                            ||it.regions.toString().lowercase().contains(text.lowercase())
                            ||it.text_ru?.lowercase()?.contains(text.lowercase())?:false
                }
        } else {
            realm.where(tournament::class.java).isNotNull("title")
                .equalTo("status","Upcoming").findAllAsync()
                .sort("date", Sort.ASCENDING)
                .filter {
                    it.title?.lowercase()?.contains(text.lowercase())?:false
                            ||it.mode?.lowercase()?.contains(text.lowercase())?:false
                            ||it.regions.toString().lowercase().contains(text.lowercase())
                            ||it.text_en?.lowercase()?.contains(text.lowercase())?:false
                }
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