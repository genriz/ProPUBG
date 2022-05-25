package app.propubg.main.menu.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.propubg.databinding.PartnersItemBinding
import app.propubg.main.menu.model.PartnerItem
import app.propubg.main.menu.model.partner

class PartnersSearchAdapter(private val listener: OnClickListener):
    ListAdapter<partner, PartnersSearchAdapter.PartnersViewHolder>(Companion) {

    companion object: DiffUtil.ItemCallback<partner>() {
        override fun areItemsTheSame(oldItem: partner, newItem: partner):
                Boolean = oldItem.equals(newItem)
        override fun areContentsTheSame(oldItem: partner, newItem: partner):
                Boolean = oldItem.equals(newItem)
    }

    class PartnersViewHolder(val binding: PartnersItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartnersViewHolder {
        return PartnersViewHolder(PartnersItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: PartnersViewHolder, position: Int) {
        val partnerItem = getItem(position)
        holder.binding.partnersItem = PartnerItem().apply { partner = partnerItem }
        holder.binding.executePendingBindings()
        holder.itemView.setOnClickListener {
            listener.onPartnerClick(partnerItem)
        }
    }

    interface OnClickListener {
        fun onPartnerClick(partner: partner)
    }
}