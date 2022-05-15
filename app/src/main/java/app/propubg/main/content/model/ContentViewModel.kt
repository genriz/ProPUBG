package app.propubg.main.content.model

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

class ContentViewModel:ViewModel() {

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

    fun getContentInformative(): OrderedRealmCollection<content?>?{
        return if (currentLanguage =="ru"){
            realm.where(content::class.java).isNotNull("title_ru")
                .equalTo("typeOfContent","Informative")
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(content::class.java).isNotNull("title_en")
                .equalTo("typeOfContent","Informative")
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
    }

    fun getContentInterview(): OrderedRealmCollection<content?>?{
        return if (currentLanguage =="ru"){
            realm.where(content::class.java).isNotNull("title_ru")
                .equalTo("typeOfContent","Interview")
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(content::class.java).isNotNull("title_en")
                .equalTo("typeOfContent","Interview")
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
    }

    fun searchContentInformative(text: String): OrderedRealmCollection<content?>?{
        return if (currentLanguage =="ru"){
            realm.where(content::class.java).isNotNull("title_ru")
                .equalTo("typeOfContent","Informative")
                .findAllAsync().where()
                .contains("title_ru", text, Case.INSENSITIVE)
                .or().contains("author", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(content::class.java).isNotNull("title_en")
                .equalTo("typeOfContent","Informative")
                .findAllAsync().where()
                .contains("title_en", text, Case.INSENSITIVE)
                .or().contains("author", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
    }

    fun searchContentInterview(text: String): OrderedRealmCollection<content?>?{
        return if (currentLanguage =="ru"){
            realm.where(content::class.java).isNotNull("title_ru")
                .equalTo("typeOfContent","Interview")
                .findAllAsync().where()
                .contains("title_ru", text, Case.INSENSITIVE)
                .or().contains("author", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(content::class.java).isNotNull("title_en")
                .equalTo("typeOfContent","Interview")
                .findAllAsync().where()
                .contains("title_en", text, Case.INSENSITIVE)
                .or().contains("author", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
    }

    override fun onCleared() {
        realm.close()
        super.onCleared()
    }

}