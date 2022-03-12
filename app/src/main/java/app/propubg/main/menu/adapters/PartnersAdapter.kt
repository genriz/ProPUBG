package app.propubg.main.menu.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.propubg.databinding.PartnersItemBinding
import app.propubg.main.menu.model.PartnerItem
import app.propubg.main.menu.model.partner
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class PartnersAdapter(data: OrderedRealmCollection<partner?>?,
                      private val listener: OnClick):
    RealmRecyclerViewAdapter<partner?,
            PartnersAdapter.PartnersViewHolder?>(data, true){

    class PartnersViewHolder(val binding: PartnersItemBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): PartnersViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PartnersViewHolder(PartnersItemBinding
            .inflate(layoutInflater,parent,false))
    }

    override fun onBindViewHolder(holder: PartnersViewHolder, position: Int) {

        val partner = getItem(position)!!
        val partnerItem = PartnerItem()
        partnerItem.partner = partner
        holder.binding.partnersItem = partnerItem
        holder.binding.executePendingBindings()

//        if ((position/2f-position/2)==0f){
//            holder.binding.itemDot.visibility = View.VISIBLE
//        } else holder.binding.itemDot.visibility = View.INVISIBLE

        holder.itemView.setOnClickListener {
            listener.onPartnerClick(partner)
        }
    }

    override fun getItemId(index: Int): Long {
        return getItem(index)!!._id!!.date.time
    }

    interface OnClick{
        fun onPartnerClick(partner: partner)
    }
}