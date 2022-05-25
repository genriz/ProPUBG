package app.propubg.main.broadcasts.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.propubg.databinding.BroadcastItemBinding
import app.propubg.main.broadcasts.model.BroadcastItem
import app.propubg.main.broadcasts.model.broadcast

class BroadcastSearchAdapter(private val listener: OnClickListener):
    ListAdapter<broadcast, BroadcastSearchAdapter.BroadcastViewHolder>(Companion) {

    companion object: DiffUtil.ItemCallback<broadcast>() {
        override fun areItemsTheSame(oldItem: broadcast, newItem: broadcast):
                Boolean = oldItem.equals(newItem)
        override fun areContentsTheSame(oldItem: broadcast, newItem: broadcast):
                Boolean = oldItem.equals(newItem)
    }

    class BroadcastViewHolder(val binding: BroadcastItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BroadcastViewHolder {
        return BroadcastViewHolder(BroadcastItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BroadcastViewHolder, position: Int) {
        val broadcastItem = getItem(position)
        holder.binding.broadcastItem = BroadcastItem().apply { broadcast = broadcastItem }
        holder.binding.executePendingBindings()

        holder.binding.btnWatch.setOnClickListener {
            listener.onWatchClick(broadcastItem)
        }

        holder.binding.btnTeamsList.setOnClickListener {
            listener.onTeamsClick(broadcastItem)
        }

        holder.binding.txtMore.setOnClickListener {
            listener.onMoreClick(broadcastItem)
        }
    }

    interface OnClickListener {
        fun onWatchClick(broadcast: broadcast)
        fun onTeamsClick(broadcast: broadcast)
        fun onMoreClick(broadcast: broadcast)
    }
}