package app.propubg.main.advert

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId
import java.util.*

class Advert {
    var advert: advertisement? = null
}

open class advertisement(
    @PrimaryKey var _id: ObjectId? = null,
    var campaign: String? = null,
    var clientName: String? = null,
    var endDate: Date? = null,
    var imageSrc_en: String? = null,
    var imageSrc_ru: String? = null,
    var isActive: Boolean? = null,
    var key: String? = null,
    var link_en: String? = null,
    var link_ru: String? = null,
    var startDate: Date? = null,
    var typeOfAd: String? = null
): RealmObject()