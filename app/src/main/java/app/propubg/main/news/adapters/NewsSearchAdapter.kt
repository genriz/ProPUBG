package app.propubg.main.news.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.propubg.databinding.NewsItemBinding
import app.propubg.main.news.model.NewsItem
import app.propubg.main.news.model.news

class NewsSearchAdapter(private val listener: OnClickListener):
    ListAdapter<news, NewsSearchAdapter.NewsViewHolder>(Companion) {

    companion object: DiffUtil.ItemCallback<news>() {
        override fun areItemsTheSame(oldItem: news, newItem: news):
                Boolean = oldItem.equals(newItem)
        override fun areContentsTheSame(oldItem: news, newItem: news):
                Boolean = oldItem.equals(newItem)
    }

    class NewsViewHolder(val binding: NewsItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        return NewsViewHolder(NewsItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsItem = getItem(position)
        holder.binding.newsItem = NewsItem().apply { news = newsItem }
        holder.binding.dateHeader.visibility = View.GONE
        holder.binding.executePendingBindings()
        holder.itemView.setOnClickListener {
            listener.onItemClick(newsItem)
        }
    }

    interface OnClickListener {
        fun onItemClick(item: news)
    }
}