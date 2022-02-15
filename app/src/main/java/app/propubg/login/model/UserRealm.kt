package app.propubg.login.model

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId

class UserRealm {
    var user: user? = null
    var UID: String? = null
}

open class user(
    @PrimaryKey var _id: ObjectId? = null,
    var access: String? = null,
    var email: String? = null,
    var key: String? = null,
    var nickname: String? = null,
    var password: String? = null,
    @Ignore
    var phoneNumber: String? = null,
    @Ignore
    var avatarUrl: String? = null,
    var role: String? = null,
    var userId: String? = null
): RealmObject()