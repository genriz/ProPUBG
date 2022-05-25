package app.propubg.main.news.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.propubg.databinding.ReshuffleItemBinding
import app.propubg.main.news.model.ReshuffleItem
import app.propubg.main.news.model.reshuffle

class ReshufflesSearchAdapter(private val listener: OnClickListener):
    ListAdapter<reshuffle, ReshufflesSearchAdapter.ReshuffleViewHolder>(Companion) {

    companion object: DiffUtil.ItemCallback<reshuffle>() {
        override fun areItemsTheSame(oldItem: reshuffle, newItem: reshuffle):
                Boolean = oldItem.equals(newItem)
        override fun areContentsTheSame(oldItem: reshuffle, newItem: reshuffle):
                Boolean = oldItem.equals(newItem)
    }

    class ReshuffleViewHolder(val binding: ReshuffleItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReshuffleViewHolder {
        return ReshuffleViewHolder(ReshuffleItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ReshuffleViewHolder, position: Int) {
        val reshuffleItem = getItem(position)
        holder.binding.reshuffleItem = ReshuffleItem().apply { reshuffle = reshuffleItem }
        holder.binding.dateHeader.visibility = View.GONE
        holder.binding.executePendingBindings()
        holder.itemView.setOnClickListener {
            listener.onItemClick(reshuffleItem)
        }
    }

    interface OnClickListener {
        fun onItemClick(item: reshuffle)
    }
}