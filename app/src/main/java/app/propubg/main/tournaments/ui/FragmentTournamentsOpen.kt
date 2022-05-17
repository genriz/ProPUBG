package app.propubg.main.tournaments.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import app.propubg.R
import app.propubg.currentLanguage
import app.propubg.databinding.FragmentPageTournamentsBinding
import app.propubg.main.MainActivity
import app.propubg.main.advert.Advert
import app.propubg.main.advert.AdvertViewModel
import app.propubg.main.tournaments.adapters.TournamentsAdapter
import app.propubg.main.tournaments.adapters.TournamentsSearchAdapter
import app.propubg.main.tournaments.model.TournamentsViewModel
import app.propubg.main.tournaments.model.tournament
import com.bumptech.glide.Glide
import org.json.JSONObject

class FragmentTournamentsOpen: Fragment(), TournamentsAdapter.OnClick,
    TournamentsSearchAdapter.OnClickListener {

    private lateinit var binding: FragmentPageTournamentsBinding
    private val viewModel: TournamentsViewModel by viewModels()
    private lateinit var adapter: TournamentsAdapter
    private lateinit var adapterSearch: TournamentsSearchAdapter
    private var isSearching = false
    private val advertViewModel: AdvertViewModel by viewModels()

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

        binding.advertMain.isVisible = false

        viewModel.realmReady.observe(viewLifecycleOwner,{
            it?.let{ ready ->
                if (ready){
                    binding.recyclerTournaments.setHasFixedSize(true)
                    adapter = TournamentsAdapter(viewModel.getTournamentsOpen(), this)
                    adapterSearch = TournamentsSearchAdapter(this)
                    binding.recyclerTournaments.adapter = adapter
                    binding.recyclerTournamentsSearch.adapter = adapterSearch
                    binding.recyclerTournaments.isVisible = false

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
                    //adapter.updateData(viewModel.searchTournamentsOpen(searchString))
                    binding.recyclerTournaments.isVisible = false
                    binding.recyclerTournamentsSearch.isVisible = true
                    adapterSearch.submitList(viewModel.searchTournamentsClosedLocal(searchString))
                    binding.recyclerTournamentsSearch.postDelayed({
                        if (adapterSearch.currentList.size==0) {
                            binding.noTournaments.visibility = View.VISIBLE
                            binding.noTournaments.setText(R.string.search_empty)
                        } else binding.noTournaments.visibility = View.GONE
                    },100)
                } else if (viewModel.realmReady.value == true&&searchString.isEmpty()) {
                    isSearching = false
                    binding.recyclerTournaments.isVisible = true
                    binding.recyclerTournamentsSearch.isVisible = false
                    adapter.updateData(viewModel.getTournamentsOpen())
                }
            }
        })

        if (!viewModel.advertClosed) {
            advertViewModel.realmReady.observe(viewLifecycleOwner,{ ready ->
                if (ready){
                    advertViewModel._advert.observe(viewLifecycleOwner,{ advertisement ->
                        advertisement?.let {
                            val advertItem = Advert().apply {
                                advert = it
                            }
                            val image = if (currentLanguage =="ru")
                                advertItem.advert!!.imageSrc_ru
                            else advertItem.advert!!.imageSrc_en
                            Glide.with(requireContext()).load(image)
                                .into(binding.advertMain.findViewById(R.id.advertImage))
                            binding.advertMain.isVisible = true
                            binding.advertMain
                                .findViewById<ImageView>(R.id.advertClose)
                                .setOnClickListener {
                                    val json = JSONObject()
                                    json.put("campaign", advertisement.campaign)
                                    json.put("screen", "Tournaments")
                                    (activity as MainActivity).mixpanelAPI!!
                                        .track("AdBannerCloseClick", json)
                                    binding.advertMain.isVisible = false
                                    viewModel.advertClosed = true
                                }
                            binding.advertMain
                                .findViewById<ImageView>(R.id.advertImage)
                                .setOnClickListener {
                                    val json = JSONObject()
                                    json.put("campaign", advertisement.campaign)
                                    json.put("screen", "Tournaments")
                                    (activity as MainActivity).mixpanelAPI!!
                                        .track("AdBannerClick", json)
                                    val link =
                                        if (currentLanguage =="ru")
                                            advertisement.link_ru
                                        else advertisement.link_en
                                    link?.let{
                                        if (URLUtil.isValidUrl(link)) {
                                            val intent = Intent()
                                            intent.action = Intent.ACTION_VIEW
                                            intent.data = Uri.parse(link)
                                            startActivity(intent)
                                        }
                                    }
                                }
                        }
                    })

                    advertViewModel.getAdvert()
                }
            })
        }
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