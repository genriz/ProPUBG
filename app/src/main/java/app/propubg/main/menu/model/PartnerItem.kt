package app.propubg.main.menu.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId
import java.io.Serializable
import java.util.*

class PartnerItem {
    var partner: partner? = null
}

open class partner(
    @PrimaryKey var _id: ObjectId? = null,
    var countViews: Long? = null,
    var date: Date? = null,
    var deepLink: String? = null,
    var descriptionOfPartner: String? = null,
    var imageSrc: String? = null,
    var key: String? = null,
    var link: String? = null,
    @Required
    var listOfViewers: RealmList<String> = RealmList(),
    @Required
    var regions: RealmList<String> = RealmList(),
    var text: String? = null,
    var title: String? = null
): RealmObject(), Serializable {
    fun getRegionList(): String {
        val list = ArrayList<String>()
        list.addAll(regions)
        return list.toString().substring(1, list.toString().length-1)
    }
}