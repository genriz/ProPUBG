package app.propubg.main.tournaments.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import app.propubg.R
import app.propubg.currentLanguage
import app.propubg.databinding.FragmentTournamentsBinding
import app.propubg.main.MainActivity
import app.propubg.main.tournaments.adapters.FragmentTournamentsPageAdapter
import app.propubg.main.tournaments.model.TournamentsViewModel
import com.google.android.material.tabs.TabLayoutMediator
import org.bson.types.ObjectId
import org.json.JSONObject

class FragmentTournaments: Fragment() {

    private lateinit var binding: FragmentTournamentsBinding
    private lateinit var adapter: FragmentTournamentsPageAdapter
    private val viewModel: TournamentsViewModel by activityViewModels()
    private var currentPage = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tournaments, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.header.headerTitle.text = getString(R.string.tournaments)
        binding.header.btnBack.visibility = View.INVISIBLE

        viewModel.realmReady.observe(viewLifecycleOwner,{ ready ->
            if (ready){
                adapter = FragmentTournamentsPageAdapter(this)
                binding.pagerTournaments.adapter = adapter
                TabLayoutMediator(binding.tabsTournaments, binding.pagerTournaments) { tab, position ->
                    tab.text = when (position){
                        0 -> getString(R.string.tournaments_closed)
                        1 -> getString(R.string.tournaments_open)
                        else -> getString(R.string.tournaments_upcoming)
                    }
                }.attach()
                binding.pagerTournaments.offscreenPageLimit = 2

                arguments?.let{
                    currentPage = requireArguments().getInt("page")
                    val id = requireArguments()
                        .getSerializable("tournamentId") as ObjectId?
                    id?.let{
                        val tournament = viewModel.getTournamentById(id)
                        tournament?.let {
                            val type = when (currentPage){
                                0 -> "ClosedDetails"
                                1 -> "OpenDetails"
                                else -> "UpcomingDetails"
                            }
                            if (tournament.title!=null)
                                    (activity as MainActivity).openTournamentDetails(it, type)
                            val json = JSONObject()
                            json.put("ObjectID", id.toString())
                            json.put("Type", "Tournament")
                            json.put("Title", tournament.title)
                            json.put("Regions", tournament.getRegionList())
                            (activity as MainActivity)
                                .mixpanelAPI?.track("DeepLinkOpened", json)
                        }
                    }
                    arguments = null
                }

                binding.pagerTournaments.setCurrentItem(currentPage, false)

                binding.pagerTournaments.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        if (currentPage!=position) {
                            currentPage = position
                            viewModel.searchString.value = ""
                        }
                    }
                })
            }
        })

        binding.header.btnOption.setOnClickListener {
            (activity as MainActivity).showSheetInfo()
        }
    }
}