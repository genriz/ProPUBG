package app.propubg.main.tournaments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.propubg.databinding.TournamentItemBinding
import app.propubg.main.tournaments.model.TournamentItem
import app.propubg.main.tournaments.model.tournament

class TournamentsSearchAdapter(private val listener: OnClickListener):
    ListAdapter<tournament, TournamentsSearchAdapter.TournamentViewHolder>(Companion) {

    companion object: DiffUtil.ItemCallback<tournament>() {
        override fun areItemsTheSame(oldItem: tournament, newItem: tournament):
                Boolean = oldItem.equals(newItem)
        override fun areContentsTheSame(oldItem: tournament, newItem: tournament):
                Boolean = oldItem.equals(newItem)
    }

    class TournamentViewHolder(val binding: TournamentItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TournamentViewHolder {
        return TournamentViewHolder(TournamentItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: TournamentViewHolder, position: Int) {
        val tournamentItem = getItem(position)
        holder.binding.tournamentItem = TournamentItem()
            .apply { tournament = tournamentItem }
        holder.binding.executePendingBindings()
        holder.itemView.setOnClickListener {
            listener.onTournamentClick(tournamentItem)
        }
    }

    interface OnClickListener {
        fun onTournamentClick(tournament: tournament)
    }
}