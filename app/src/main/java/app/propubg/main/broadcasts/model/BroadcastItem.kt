package app.propubg.main.broadcasts.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId
import java.util.*

class BroadcastItem {
    var broadcast: broadcast? = null
}

open class broadcast(
    @PrimaryKey var _id: ObjectId? = null,
    var currency: String? = null,
    var date: Date? = null,
    var dayOfTournament: String? = null,
    var languageOfBroadcast: String? = null,
    var objectIDOfTournament: String? = null,
    var imageSrc: String? = null,
    var key: String? = null,
    var link: String? = null,
    var prizePool: Long? = null,
    var stage_en: String? = null,
    var stage_ru: String? = null,
    var status: String? = null,
    @Required
    var teamsList: RealmList<String> = RealmList(),
    var title: String? = null,
    @Ignore
    var tournamentExist:Boolean = true
): RealmObject()