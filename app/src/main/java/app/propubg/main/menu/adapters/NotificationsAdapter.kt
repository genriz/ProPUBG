package app.propubg.main.menu.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.propubg.databinding.NotificationTitleItemBinding
import app.propubg.main.menu.model.NotificationItem
import app.propubg.main.menu.model.NotificationTitle

class NotificationsAdapter(private val listener: OnClickListener):
    ListAdapter<NotificationTitle, NotificationsAdapter.NotificationsViewHolder>(Companion),
    NotificationItemsAdapter.OnClickListener {

    private lateinit var adapter: NotificationItemsAdapter

    companion object: DiffUtil.ItemCallback<NotificationTitle>() {
        override fun areItemsTheSame(oldItem: NotificationTitle, newItem: NotificationTitle):
                Boolean = oldItem.title == newItem.title
        override fun areContentsTheSame(oldItem: NotificationTitle, newItem: NotificationTitle):
                Boolean = oldItem == newItem
    }

    class NotificationsViewHolder(val binding: NotificationTitleItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsViewHolder {
        return NotificationsViewHolder(NotificationTitleItemBinding
            .inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: NotificationsViewHolder, position: Int) {
        val notificationTitle = getItem(position)
        holder.binding.title = notificationTitle

        adapter = NotificationItemsAdapter(position, this)
        adapter.submitList(notificationTitle.items)
        holder.binding.notificationSubtitleRecycler.adapter = adapter

        holder.binding.executePendingBindings()
        holder.itemView.setOnClickListener {
            listener.onTitleClick(notificationTitle)
        }
    }

    interface OnClickListener {
        fun onTitleClick(notificationTitle: NotificationTitle)
        fun onItemClick(menuPosition:Int, notificationItem: NotificationItem)
    }

    override fun onItemClick(menuPosition:Int, notificationItem: NotificationItem) {
        listener.onItemClick(menuPosition, notificationItem)
    }
}

