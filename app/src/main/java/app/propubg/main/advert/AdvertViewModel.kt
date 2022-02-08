package app.propubg.main.advert

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.propubg.currentLanguage
import app.propubg.realmApp
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where
import io.realm.mongodb.sync.SyncConfiguration

class AdvertViewModel: ViewModel() {

    private lateinit var realm: Realm
    val realmReady = MutableLiveData<Boolean>()
    val _advert = MutableLiveData<advertisement>()

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

    fun getAdvert() {
        val advert = if (currentLanguage =="ru"){
            realm.where<advertisement>()
                .isNotNull("startDate")
                .isNotNull("imageSrc_ru")
                .isNotNull("link_ru")
                .equalTo("isActive", true)
                .isNotNull("typeOfAd")
                .sort("startDate", Sort.DESCENDING)
                .findFirst()
        } else {
            realm.where<advertisement>()
                .isNotNull("startDate")
                .isNotNull("imageSrc_en")
                .isNotNull("link_en")
                .equalTo("isActive", true)
                .isNotNull("typeOfAd")
                .sort("startDate", Sort.DESCENDING)
                .findFirst()
        }
        advert?.let{
            _advert.postValue(it)
        }
    }

    override fun onCleared() {
        realm.close()
        super.onCleared()
    }
}