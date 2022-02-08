package app.propubg.main.content.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId
import java.util.*

class ContentItem {
    var content: content? = null
}

open class content(
    @PrimaryKey var _id: ObjectId? = null,
    var author: String? = null,
    var date: Date? = null,
    var key: String? = null,
    var link: String? = null,
    var typeOfContent: String? = null,
    var imageOfAuthorSrc: String? = null,
    var imageOfContentSrc: String? = null,
    @Required
    var languageOfContent: RealmList<String> = RealmList(),
    var title_en: String? = null,
    var title_ru: String? = null
): RealmObject() {}