package app.propubg.main.menu.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId
import java.io.Serializable
import java.util.*

class ResultsItem {
    var resultsOfTournament: resultsOfTournament? = null
}

open class resultsOfTournament(
    @PrimaryKey var _id: ObjectId? = null,
    var author: String? = null,
    var countViews: Long? = null,
    var date: Date? = null,
    var dayOfTournament: String? = null,
    var deepLink: String? = null,
    @Required
    var imageSrc_en: RealmList<String> = RealmList(),
    @Required
    var imageSrc_ru: RealmList<String> = RealmList(),
    var key: String? = null,
    @Required
    var listOfViewers: RealmList<String> = RealmList(),
    @Required
    var regions: RealmList<String> = RealmList(),
    var stage_en: String? = null,
    var stage_ru: String? = null,
    var text_en: String? = null,
    var text_ru: String? = null,
    var title: String? = null
): RealmObject(), Serializable {
    fun getRegionList(): String {
        val list = ArrayList<String>()
        list.addAll(regions)
        return list.toString().substring(1, list.toString().length-1)
    }
}