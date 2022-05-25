package app.propubg.main.content.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.propubg.databinding.ContentItemBinding
import app.propubg.main.content.model.ContentItem
import app.propubg.main.content.model.content

class ContentSearchAdapter(private val listener: OnClickListener):
    ListAdapter<content, ContentSearchAdapter.ContentViewHolder>(Companion) {

    companion object: DiffUtil.ItemCallback<content>() {
        override fun areItemsTheSame(oldItem: content, newItem: content):
                Boolean = oldItem.equals(newItem)
        override fun areContentsTheSame(oldItem: content, newItem: content):
                Boolean = oldItem.equals(newItem)
    }

    class ContentViewHolder(val binding: ContentItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        return ContentViewHolder(ContentItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        val contentItem = getItem(position)
        holder.binding.contentItem = ContentItem().apply { content = contentItem }
        holder.binding.executePendingBindings()

        holder.binding.btnWatch.setOnClickListener {
            listener.onWatchClick(contentItem)
        }
    }

    interface OnClickListener {
        fun onWatchClick(content: content)
    }
}