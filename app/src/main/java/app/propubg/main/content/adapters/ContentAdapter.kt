package app.propubg.main.content.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.propubg.databinding.ContentItemBinding
import app.propubg.main.content.model.ContentItem
import app.propubg.main.content.model.content
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class ContentAdapter(data: OrderedRealmCollection<content?>?,
                     private val listener: OnClick):
    RealmRecyclerViewAdapter<content?,
            ContentAdapter.TournamentsViewHolder?>(data, true){

    class TournamentsViewHolder(val binding: ContentItemBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): TournamentsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TournamentsViewHolder(ContentItemBinding
            .inflate(layoutInflater,parent,false))
    }

    override fun onBindViewHolder(holder: TournamentsViewHolder, position: Int) {

        val content = getItem(position)!!
        val contentItem = ContentItem()
        contentItem.content = content
        holder.binding.contentItem = contentItem
        holder.binding.executePendingBindings()

        holder.binding.btnWatch.setOnClickListener {
            listener.onWatchClick(content)
        }

    }

    override fun getItemId(index: Int): Long {
        return getItem(index)!!._id!!.date.time
    }

    interface OnClick{
        fun onWatchClick(content: content)
    }
}