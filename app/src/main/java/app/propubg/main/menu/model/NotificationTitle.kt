package app.propubg.main.menu.model

data class NotificationTitle (
    var title: String,
    var items: ArrayList<NotificationItem>,
    var collapsed: Boolean
    )

data class NotificationItem(
    var title: String,
    var isChecked: Boolean,
    var index: Int
    )