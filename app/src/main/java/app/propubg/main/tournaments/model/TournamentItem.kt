package app.propubg.main.tournaments.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId
import java.io.Serializable
import java.util.*

class TournamentItem {
    var tournament: tournament? = null
}

open class tournament(
    @PrimaryKey var _id: ObjectId? = null,
    var countTeams: Long? = null,
    @Ignore
    var countViews: Long? = null,
    var currency: String? = null,
    var date: Date? = null,
    var dynamicLink_en: String? = null,
    var dynamicLink_ru: String? = null,
    @Required
    var imageSrc: RealmList<String> = RealmList(),
    var invitedTeams: Boolean? = null,
    var key: String? = null,
    var link: String? = null,
    var mode: String? = null,
    var prizePool: Long? = null,
    @Required
    var regions: RealmList<String> = RealmList(),
    var status: String? = null,
    var text_en: String? = null,
    var text_ru: String? = null,
    var title: String? = null,
    var verification: String? = null,
    var willClose_en: String? = null,
    var willClose_ru: String? = null,
    var willOpen_en: String? = null,
    var willOpen_ru: String? = null
): RealmObject(), Serializable {
    fun getRegionList(): String{
        val list = ArrayList<String>()
        list.addAll(regions)
        return list.toString().substring(1, list.toString().length-1)
    }
}