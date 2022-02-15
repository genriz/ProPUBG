package app.propubg.main.menu.model

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

class MenuViewModel: ViewModel() {

    private lateinit var realm: Realm
    val searchString = MutableLiveData<String>().apply { value = "" }
    val realmReady = MutableLiveData<Boolean>()

    val isRussian = MutableLiveData<Boolean>()
    val isEnglish = MutableLiveData<Boolean>()

    var partnersAdvertClosed = false
    var resultsAdvertClosed = false

    init {
        isRussian.value = currentLanguage == "ru"
        isEnglish.value = currentLanguage == "en"

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

    fun setRussian(){
        currentLanguage = "ru"
        isRussian.value = true
        isEnglish.value = false
    }

    fun setEnglish(){
        currentLanguage = "en"
        isRussian.value = false
        isEnglish.value = true
    }

    fun getResults(): OrderedRealmCollection<resultsOfTournament?>?{
        return if (currentLanguage =="ru") {
            realm.where(resultsOfTournament::class.java).isNotNull("title")
                .and().isNotNull("imageSrc_ru")
                .and().isNotNull("stage_ru")
                .and().isNotNull("author")
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(resultsOfTournament::class.java).isNotNull("title")
                .and().isNotNull("imageSrc_en")
                .and().isNotNull("stage_en")
                .and().isNotNull("author")
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
    }

    fun searchResults(text: String): OrderedRealmCollection<resultsOfTournament?>?{
        return if (currentLanguage =="ru"){
            realm.where(resultsOfTournament::class.java).isNotNull("title")
                .contains("title", text, Case.SENSITIVE)
                .or().contains("stage_ru", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .or().contains("text_ru", text, Case.INSENSITIVE)
                .or().contains("author", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        } else {
            realm.where(resultsOfTournament::class.java).isNotNull("title")
                .contains("title", text, Case.SENSITIVE)
                .or().contains("stage_en", text, Case.INSENSITIVE)
                .or().contains("regions", text, Case.INSENSITIVE)
                .or().contains("text_en", text, Case.INSENSITIVE)
                .or().contains("author", text, Case.INSENSITIVE)
                .sort("date", Sort.DESCENDING).findAllAsync()
        }
    }

    fun getResultsById(id: ObjectId): resultsOfTournament? {
        return realm.where(resultsOfTournament::class.java)
            .equalTo("_id", id).findFirst()
    }

    fun getPartners(): OrderedRealmCollection<partner?>?{
        return realm.where(partner::class.java).isNotNull("title")
            .and().isNotNull("imageSrc")
            .and().isNotNull("text")
            .and().isNotNull("descriptionOfPartner")
            .and().isNotNull("link")
            .sort("date", Sort.DESCENDING).findAllAsync()
    }

    fun searchPartners(text: String): OrderedRealmCollection<partner?>?{
        return realm.where(partner::class.java).isNotNull("title")
            .contains("title", text, Case.SENSITIVE)
            .or().contains("text", text, Case.INSENSITIVE)
            .or().contains("descriptionOfPartner", text, Case.INSENSITIVE)
            .sort("date", Sort.DESCENDING).findAllAsync()
    }

    fun getPartnerById(id: ObjectId): partner? {
        return realm.where(partner::class.java)
            .equalTo("_id", id).findFirst()
    }

    override fun onCleared() {
        realm.close()
        super.onCleared()
    }
}