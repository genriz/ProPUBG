package app.propubg.main.tournaments.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import app.propubg.R
import app.propubg.databinding.FragmentPageTournamentsBinding
import app.propubg.main.MainActivity
import app.propubg.main.tournaments.adapters.TournamentsAdapter
import app.propubg.main.tournaments.model.TournamentsViewModel
import app.propubg.main.tournaments.model.tournament
import org.json.JSONObject

class FragmentTournamentsOpen: Fragment(), TournamentsAdapter.OnClick {

    private lateinit var binding: FragmentPageTournamentsBinding
    private val viewModel: TournamentsViewModel by viewModels()
    private lateinit var adapter: TournamentsAdapter
    private var isSearching = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_page_tournaments,
            container, false)
        binding.searchTournament.viewModel = viewModel
        binding.searchTournament.lifecycleOwner = viewLifecycleOwner
        binding.lifecycleOwner = viewLifecycleOwner
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

                    adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
                        override fun onChanged() {
                            super.onChanged()
                            if (adapter.itemCount==0) {
                                binding.noTournaments.visibility = View.VISIBLE
                                if (isSearching)
                                    binding.noTournaments.setText(R.string.search_empty)
                                else
                                    binding.noTournaments.setText(R.string.no_tournaments_opened)
                            }
                            else binding.noTournaments.visibility = View.GONE
                        }
                    })
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
                    isSearching = true
                    adapter.updateData(null)
                    adapter.updateData(viewModel.searchTournamentsOpen(searchString))
                } else if (viewModel.realmReady.value == true&&searchString.isEmpty()) {
                    isSearching = false
                    adapter.updateData(viewModel.getTournamentsOpen())
                }
            }
        })

    }

    override fun onTournamentClick(tournament: tournament) {
        (activity as MainActivity).openTournamentDetails(tournament, "OpenDetails")
    }

    override fun onResume() {
        super.onResume()
        val json = JSONObject()
        json.put("Screen", "Tournaments[Open]")
        json.put("ObjectID", "No value")
        json.put("Title", "No value")
        json.put("Regions", "No value")
        (activity as MainActivity).mixpanelAPI?.track("ScreenView", json)
    }

}