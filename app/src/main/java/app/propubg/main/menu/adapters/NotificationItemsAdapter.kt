package app.propubg.main.menu.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.propubg.databinding.NotificationSubtitleItemBinding
import app.propubg.main.menu.model.NotificationItem

class NotificationItemsAdapter(private val menuPosition: Int,
                               private val listener: OnClickListener):
    ListAdapter<NotificationItem, NotificationItemsAdapter.NotificationsViewHolder>(Companion){

    companion object: DiffUtil.ItemCallback<NotificationItem>() {
        override fun areItemsTheSame(oldItem: NotificationItem, newItem: NotificationItem):
                Boolean = oldItem.title == newItem.title
        override fun areContentsTheSame(oldItem: NotificationItem, newItem: NotificationItem):
                Boolean = oldItem == newItem
    }

    class NotificationsViewHolder(val binding: NotificationSubtitleItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsViewHolder {
        return NotificationsViewHolder(NotificationSubtitleItemBinding
            .inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: NotificationsViewHolder, position: Int) {
        val notificationItem = getItem(position)
        holder.binding.item = notificationItem
        holder.binding.executePendingBindings()
        holder.binding.switchItem.setOnCheckedChangeListener { _, isChecked ->
            notificationItem.isChecked = isChecked
            listener.onItemClick(menuPosition, notificationItem)
        }
    }

    interface OnClickListener {
        fun onItemClick(menuPosition: Int, notificationItem: NotificationItem)
    }
}

