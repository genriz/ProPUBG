package app.propubg.main.news.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.propubg.databinding.NewsItemBinding
import app.propubg.utils.AppUtils
import app.propubg.main.news.model.NewsItem
import app.propubg.main.news.model.news
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class NewsAdapter(data: OrderedRealmCollection<news?>?,
                  private val listener: OnClick):
    RealmRecyclerViewAdapter<news?,
            NewsAdapter.NewsViewHolder?>(data, true){

    class NewsViewHolder(val binding: NewsItemBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return NewsViewHolder(
            NewsItemBinding
                .inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {

        val news = getItem(position)!!
        val newsItem = NewsItem()
        newsItem.news = news
        holder.binding.newsItem = newsItem
        holder.binding.executePendingBindings()

//        if ((position/2f-position/2)==0f){
//            holder.binding.itemDot.visibility = View.VISIBLE
//        } else holder.binding.itemDot.visibility = View.INVISIBLE

        news.date?.let{
            val dateHeader = AppUtils()
                .getDateHeader(holder.binding.dateTxt.context, it)
            holder.binding.dateTxt.text = dateHeader
            holder.binding.dateHeader.visibility = View.GONE
            if (position == 0){
                holder.binding.dateHeader.visibility = View.VISIBLE
            } else {
                getItem(position-1)?.date?.let{ date ->
                    if (dateHeader == AppUtils()
                            .getDateHeader(holder.binding.dateTxt.context,
                                date))
                        holder.binding.dateHeader.visibility = View.GONE
                    else holder.binding.dateHeader.visibility = View.VISIBLE
                }
            }
        }

        holder.itemView.setOnClickListener {
            listener.onNewsClick(news, holder.binding.newsImage)
        }
    }



    override fun getItemId(index: Int): Long {
        return getItem(index)!!._id!!.date.time
    }

    interface OnClick{
        fun onNewsClick(news: news, imageView: ImageView)
    }
}