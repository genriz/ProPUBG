package app.propubg.main.news.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.propubg.currentLanguage
import app.propubg.realmApp
import io.realm.Case
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.syncSession
import io.realm.mongodb.sync.SyncConfiguration
import org.bson.types.ObjectId

class NewsViewModel:ViewModel() {

    private lateinit var realm: Realm
    val realmReady = MutableLiveData<Boolean>()
    val searchString = MutableLiveData<String>().apply { value = "" }
    var advertClosed = false
    val reset = MutableLiveData<Boolean>().apply { value = false }

    init {
        val user = realmApp.currentUser()!!
        val config = SyncConfiguration.Builder(user, "news")
            .waitForInitialRemoteData()
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .syncClientResetStrategy { session, error ->
                Log.v("DASD", error.message?:"error")
                reset.postValue(true)
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

    fun getNews(): OrderedRealmCollection<news?>?{
        val news = if (currentLanguage=="ru") {
            realm.where(news::class.java).isNotNull("title_ru")
                .and().isNotNull("imageSrc_ru")
                .and().isNotNull("text_ru")
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(news::class.java).isNotNull("title_en")
                .and().isNotNull("imageSrc_en")
                .and().isNotNull("text_en")
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
        return news
    }

    fun searchNews(text: String): OrderedRealmCollection<news?>?{
        return if (currentLanguage=="ru"){
            realm.where(news::class.java).isNotNull("title_ru")
                .and().isNotNull("imageSrc_ru")
                .and().isNotNull("text_ru")
                .contains("title_ru", text, Case.INSENSITIVE)
                .or().contains("author", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(news::class.java).isNotNull("title_en")
                .and().isNotNull("imageSrc_en")
                .and().isNotNull("text_en")
                .contains("title_en", text, Case.INSENSITIVE)
                .or().contains("author", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
    }

    fun searchNewsLocal(text: String): List<news> {
        return if (currentLanguage=="ru"){
            realm.where(news::class.java).isNotNull("title_ru")
                .and().isNotNull("imageSrc_ru")
                .and().isNotNull("text_ru")
                .sort("date", Sort.DESCENDING).findAllAsync()
                .filter {
                    it.title_ru?.lowercase()?.contains(text.lowercase())?:false
                            || it.text_ru?.lowercase()?.contains(text.lowercase())?:false
                            || it.author?.lowercase()?.contains(text.lowercase())?:false
                            || it.regions.toString().lowercase().contains(text.lowercase())
                }
        } else {
            realm.where(news::class.java).isNotNull("title_en")
                .and().isNotNull("imageSrc_en")
                .and().isNotNull("text_en")
                .sort("date", Sort.DESCENDING).findAllAsync()
                .filter {
                    it.title_en?.lowercase()?.contains(text.lowercase())?:false
                            || it.text_en?.lowercase()?.contains(text.lowercase())?:false
                            || it.author?.lowercase()?.contains(text.lowercase())?:false
                            || it.regions.toString().lowercase().contains(text.lowercase())
                }
        }
    }

    fun updateNews(news: news, id: String){
        realm.executeTransaction { r: Realm ->
            news.listViewers.add(id)
            r.insertOrUpdate(news)
        }
    }


    fun getReshuffles(): OrderedRealmCollection<reshuffle?>?{
        val reshuffles = if (currentLanguage=="ru"){
            realm.where(reshuffle::class.java).isNotNull("title_ru")
                .and().isNotNull("imageSrc_ru")
                .and().isNotEmpty("text_ru")
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(reshuffle::class.java).isNotNull("title_en")
                .and().isNotNull("imageSrc_en")
                .and().isNotEmpty("text_en")
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
        return reshuffles
    }

    fun searchReshuffles(text: String): OrderedRealmCollection<reshuffle?>?{
        return if (currentLanguage=="ru"){
            realm.where(reshuffle::class.java).isNotNull("title_ru")
                .and().isNotNull("imageSrc_ru")
                .and().isNotNull("text_ru")
                .contains("title_ru", text, Case.INSENSITIVE)
                .or().contains("author", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(reshuffle::class.java).isNotNull("title_en")
                .and().isNotNull("imageSrc_en")
                .and().isNotNull("text_en")
                .contains("title_en", text, Case.INSENSITIVE)
                .or().contains("author", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
    }

    fun searchReshufflesLocal(text: String): List<reshuffle> {
        return if (currentLanguage=="ru"){
            realm.where(reshuffle::class.java).isNotNull("title_ru")
                .and().isNotNull("imageSrc_ru")
                .and().isNotNull("text_ru")
                .sort("date", Sort.DESCENDING).findAllAsync()
                .filter {
                    it.title_ru?.lowercase()?.contains(text.lowercase())?:false
                            || it.text_ru?.lowercase()?.contains(text.lowercase())?:false
                            || it.author?.lowercase()?.contains(text.lowercase())?:false
                            || it.regions.toString().lowercase().contains(text.lowercase())
                }
        } else {
            realm.where(reshuffle::class.java).isNotNull("title_en")
                .and().isNotNull("imageSrc_en")
                .and().isNotNull("text_en")
                .sort("date", Sort.DESCENDING).findAllAsync()
                .filter {
                    it.title_en?.lowercase()?.contains(text.lowercase())?:false
                            || it.text_en?.lowercase()?.contains(text.lowercase())?:false
                            || it.author?.lowercase()?.contains(text.lowercase())?:false
                            || it.regions.toString().lowercase().contains(text.lowercase())
                }
        }
    }

    fun getNewsById(id:ObjectId): news? {
        return realm.where(news::class.java).equalTo("_id", id).findFirst()
    }

    fun getReshuffleById(id:ObjectId): reshuffle? {
        return realm.where(reshuffle::class.java).equalTo("_id", id).findFirst()
    }

    override fun onCleared() {
        realm.close()
        super.onCleared()
    }
}