package app.propubg.main.menu.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.propubg.databinding.ResultTournamentItemBinding
import app.propubg.main.menu.model.ResultsItem
import app.propubg.main.menu.model.resultsOfTournament

class ResultsSearchAdapter(private val listener: OnClickListener):
    ListAdapter<resultsOfTournament, ResultsSearchAdapter.ResultsViewHolder>(Companion) {

    companion object: DiffUtil.ItemCallback<resultsOfTournament>() {
        override fun areItemsTheSame(oldItem: resultsOfTournament, newItem: resultsOfTournament):
                Boolean = oldItem.equals(newItem)
        override fun areContentsTheSame(oldItem: resultsOfTournament, newItem: resultsOfTournament):
                Boolean = oldItem.equals(newItem)
    }

    class ResultsViewHolder(val binding: ResultTournamentItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsViewHolder {
        return ResultsViewHolder(ResultTournamentItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ResultsViewHolder, position: Int) {
        val resultsItem = getItem(position)
        holder.binding.resultsItem = ResultsItem().apply { resultsOfTournament = resultsItem }
        holder.binding.executePendingBindings()
        holder.itemView.setOnClickListener {
            listener.onResultsClick(resultsItem)
        }
    }

    interface OnClickListener {
        fun onResultsClick(resultsOfTournament: resultsOfTournament)
    }
}