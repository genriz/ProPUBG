package app.propubg.main.tournaments.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import app.propubg.R
import app.propubg.databinding.FragmentPageTournamentsBinding
import app.propubg.main.MainActivity
import app.propubg.main.tournaments.adapters.TournamentsAdapter
import app.propubg.main.tournaments.model.TournamentsViewModel
import app.propubg.main.tournaments.model.tournament

class FragmentTournamentsOpen: Fragment(), TournamentsAdapter.OnClick {

    private lateinit var binding: FragmentPageTournamentsBinding
    private val viewModel: TournamentsViewModel by viewModels()
    private lateinit var adapter: TournamentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_page_tournaments,
            container, false)
        binding.searchTournament.viewModel = viewModel
        binding.searchTournament.lifecycleOwner = this
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.realmReady.observe(viewLifecycleOwner,{
            it?.let{ ready ->
                if (ready){
                    binding.recyclerTournaments.setHasFixedSize(true)
                    adapter = TournamentsAdapter(viewModel.getTournamentsOpen(), this)
                    binding.recyclerTournaments.adapter = adapter
                }
            }
        })

        binding.searchTournament.searchCancel.setOnClickListener {
            binding.expandLayout.setExpanded(false)
            viewModel.searchString.value = ""
            requireActivity().currentFocus?.let { focus ->
                val inputManager: InputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(
                    focus.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }

        viewModel.searchString.observe(viewLifecycleOwner,{
            it?.let{ searchString ->
                if (searchString.length>1){
                    adapter.updateData(viewModel.searchTournamentsOpen(searchString))
                } else if (viewModel.realmReady.value == true&&searchString.isEmpty()) {
                    adapter.updateData(viewModel.getTournamentsOpen())
                }
            }
        })

    }

    override fun onTournamentClick(tournament: tournament) {
        (activity as MainActivity).openTournamentDetails(tournament)
    }

    override fun isEmpty(isEmpty: Boolean) {
        if (!isEmpty) binding.noTournaments.visibility = View.GONE
    }

}