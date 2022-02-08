package app.propubg.main.broadcasts.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import app.propubg.databinding.BroadcastItemBinding
import app.propubg.main.broadcasts.model.BroadcastItem
import app.propubg.main.broadcasts.model.broadcast
import app.propubg.main.tournaments.model.TournamentsViewModel
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import org.bson.types.ObjectId

class BroadcastsAdapter(context: ViewModelStoreOwner,
                        data: OrderedRealmCollection<broadcast?>?,
                        private val listener: OnClick):
    RealmRecyclerViewAdapter<broadcast?,
            BroadcastsAdapter.TournamentsViewHolder?>(data, true){

    private val viewModel by lazy { ViewModelProvider(context)[TournamentsViewModel::class.java] }

    class TournamentsViewHolder(val binding: BroadcastItemBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): TournamentsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TournamentsViewHolder(BroadcastItemBinding
            .inflate(layoutInflater,parent,false))
    }

    override fun onBindViewHolder(holder: TournamentsViewHolder, position: Int) {

        listener.isEmpty(false)

        val broadcast = getItem(position)!!

//        broadcast.tournamentExist = broadcast.objectIDOfTournament!=null
//                &&broadcast.objectIDOfTournament!!.isNotEmpty()
//                &&ObjectId.isValid(broadcast.objectIDOfTournament)
//                &&viewModel.getTournamentById(ObjectId(broadcast.objectIDOfTournament))!=null
        val broadcastItem = BroadcastItem()
        broadcastItem.broadcast = broadcast
        holder.binding.broadcastItem = broadcastItem
        holder.binding.executePendingBindings()

        holder.binding.btnWatch.setOnClickListener {
            listener.onWatchClick(broadcast)
        }

        holder.binding.btnTeamsList.setOnClickListener {
            listener.onTeamsClick(broadcast)
        }

        holder.binding.txtMore.setOnClickListener {
            listener.onMoreClick(broadcast)
        }
    }

    override fun getItemId(index: Int): Long {
        return getItem(index)!!._id!!.date.time
    }

    interface OnClick{
        fun onWatchClick(broadcast: broadcast)
        fun onTeamsClick(broadcast: broadcast)
        fun onMoreClick(broadcast: broadcast)
        fun isEmpty(isEmpty: Boolean)
    }
}