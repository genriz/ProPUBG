package app.propubg.main.news.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.propubg.databinding.ReshuffleItemBinding
import app.propubg.utils.AppUtils
import app.propubg.main.news.model.ReshuffleItem
import app.propubg.main.news.model.reshuffle
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class ReshufflesAdapter(data: OrderedRealmCollection<reshuffle?>?,
                        private val listener: OnClick):
    RealmRecyclerViewAdapter<reshuffle?,
            ReshufflesAdapter.ReshufflesViewHolder?>(data, true){

    class ReshufflesViewHolder(val binding: ReshuffleItemBinding):
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ReshufflesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ReshufflesViewHolder(ReshuffleItemBinding
            .inflate(layoutInflater,parent,false))
    }

    override fun onBindViewHolder(holder: ReshufflesViewHolder, position: Int) {

        val reshuffle = getItem(position)!!
        val reshuffleItem = ReshuffleItem()
        reshuffleItem.reshuffle = reshuffle
        holder.binding.reshuffleItem = reshuffleItem
        holder.binding.executePendingBindings()

//        if ((position/2f-position/2)==0f){
//            holder.binding.itemDot.visibility = View.VISIBLE
//        } else holder.binding.itemDot.visibility = View.INVISIBLE

        reshuffle.date?.let{
            val dateHeader = AppUtils()
                .getDateHeader(holder.binding.dateTxt.context, it)
            holder.binding.dateTxt.text = dateHeader
            holder.binding.dateHeader.visibility = View.GONE
            if (position == 0){
                holder.binding.dateHeader.visibility = View.VISIBLE
            } else {
                getItem(position-1)?.date?.let{ date ->
                    if (dateHeader== AppUtils()
                            .getDateHeader(holder.binding.dateTxt.context,
                                date))
                        holder.binding.dateHeader.visibility = View.GONE
                    else holder.binding.dateHeader.visibility = View.VISIBLE
                }
            }
        }

        holder.itemView.setOnClickListener {
            listener.onReshufflesClick(reshuffle)
        }
    }

    override fun getItemId(index: Int): Long {
        return getItem(index)!!._id!!.date.time
    }

    interface OnClick{
        fun onReshufflesClick(reshuffle: reshuffle)
    }
}