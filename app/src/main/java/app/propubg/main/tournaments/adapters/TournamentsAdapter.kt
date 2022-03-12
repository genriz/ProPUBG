package app.propubg.main.tournaments.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.propubg.databinding.TournamentItemBinding
import app.propubg.main.tournaments.model.TournamentItem
import app.propubg.main.tournaments.model.tournament
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class TournamentsAdapter(data: OrderedRealmCollection<tournament?>?,
                         private val listener: OnClick):
    RealmRecyclerViewAdapter<tournament?,
            TournamentsAdapter.TournamentsViewHolder?>(data, true){

    class TournamentsViewHolder(val binding: TournamentItemBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): TournamentsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TournamentsViewHolder(TournamentItemBinding
            .inflate(layoutInflater,parent,false))
    }

    override fun onBindViewHolder(holder: TournamentsViewHolder, position: Int) {

        val tournament = getItem(position)
        val tournamentItem = TournamentItem()
        tournamentItem.tournament = tournament
        holder.binding.tournamentItem = tournamentItem
        holder.binding.executePendingBindings()

//        if ((position/2f-position/2)==0f){
//            holder.binding.itemDot.visibility = View.VISIBLE
//        } else holder.binding.itemDot.visibility = View.INVISIBLE

        holder.itemView.setOnClickListener {
            listener.onTournamentClick(tournament!!)
        }
    }

    override fun getItemId(index: Int): Long {
        return getItem(index)!!._id!!.date.time
    }

    interface OnClick{
        fun onTournamentClick(tournament: tournament)
    }
}