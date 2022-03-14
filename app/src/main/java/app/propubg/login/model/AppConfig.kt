package app.propubg.login.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId

class AppConfig {
    var config: configuration? = null
}

open class configuration(
    @PrimaryKey var _id: ObjectId? = null,
    var appStoreLink: String? = null,
    var currentVersionAndroid: String? = null,
    var currentVersionIOS: String? = null,
    var deleteMyAccount_en: String? = null,
    var deleteMyAccount_ru: String? = null,
    var googlePlayLink: String? = null,
    var key: String? = null,
    var ourWebSite: String? = null,
    var socialLinkCreator_Instagram: String? = null,
    var socialLink_Instagram: String? = null,
    var socialLink_Telegram: String? = null,
    var supportLinkTelegram: String? = null
): RealmObject() {}