package app.propubg.main.menu.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.propubg.databinding.ResultTournamentItemBinding
import app.propubg.main.menu.model.ResultsItem
import app.propubg.main.menu.model.resultsOfTournament
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class ResultsAdapter(data: OrderedRealmCollection<resultsOfTournament?>?,
                     private val listener: OnClick):
    RealmRecyclerViewAdapter<resultsOfTournament?,
            ResultsAdapter.ResultsViewHolder?>(data, true){

    class ResultsViewHolder(val binding: ResultTournamentItemBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ResultsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ResultsViewHolder(ResultTournamentItemBinding
            .inflate(layoutInflater,parent,false))
    }

    override fun onBindViewHolder(holder: ResultsViewHolder, position: Int) {

        val results = getItem(position)!!
        val resultsItem = ResultsItem()
        resultsItem.resultsOfTournament = results
        holder.binding.resultsItem = resultsItem
        holder.binding.executePendingBindings()

//        if ((position/2f-position/2)==0f){
//            holder.binding.itemDot.visibility = View.VISIBLE
//        } else holder.binding.itemDot.visibility = View.INVISIBLE

        holder.itemView.setOnClickListener {
            listener.onResultsClick(results)
        }
    }

    override fun getItemId(index: Int): Long {
        return getItem(index)!!._id!!.date.time
    }

    interface OnClick{
        fun onResultsClick(resultsOfTournament: resultsOfTournament)
    }
}