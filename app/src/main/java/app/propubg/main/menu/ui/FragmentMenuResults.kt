package app.propubg.main.menu.ui

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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import app.propubg.R
import app.propubg.currentLanguage
import app.propubg.databinding.FragmentMenuResultsBinding
import app.propubg.main.MainActivity
import app.propubg.main.advert.Advert
import app.propubg.main.advert.AdvertViewModel
import app.propubg.main.menu.adapters.ResultsAdapter
import app.propubg.main.menu.model.MenuViewModel
import app.propubg.main.menu.model.resultsOfTournament
import com.bumptech.glide.Glide
import org.json.JSONObject

class FragmentMenuResults:Fragment(), ResultsAdapter.OnClick {

    private lateinit var binding: FragmentMenuResultsBinding
    private val viewModel: MenuViewModel by activityViewModels()
    private lateinit var adapter: ResultsAdapter
    private val advertViewModel: AdvertViewModel by viewModels()
    private var isSearching = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu_results,
            container, false)
        binding.searchResults.viewModel = viewModel
        binding.searchResults.lifecycleOwner = viewLifecycleOwner
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.advertMain.isVisible = false
        binding.header.headerTitle.text = getString(R.string.results_tournaments)
        binding.header.btnBack.setOnClickListener {
            (activity as MainActivity).onBackPressed()
        }

        viewModel.realmReady.observe(viewLifecycleOwner,{
            it?.let { ready ->
                if (ready){
                    adapter = ResultsAdapter(viewModel.getResults(), this)
                    binding.recyclerResults.setHasFixedSize(true)
                    binding.recyclerResults.adapter = adapter

                    adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
                        override fun onChanged() {
                            super.onChanged()
                            if (adapter.itemCount==0) {
                                binding.noResults.visibility = View.VISIBLE
                                if (isSearching)
                                    binding.noResults.setText(R.string.search_empty)
                                else
                                    binding.noResults.setText(R.string.no_results)
                            }
                            else binding.noResults.visibility = View.GONE
                        }
                    })
                }
            }
        })

        binding.searchResults.searchCancel.setOnClickListener {
            binding.expandLayout.setExpanded(false)
            viewModel.searchString.value = ""
            requireActivity().currentFocus?.let { focus ->
                val inputManager: InputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE)
                            as InputMethodManager
                inputManager.hideSoftInputFromWindow(
                    focus.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }

        viewModel.searchString.observe(viewLifecycleOwner,{
            it?.let{ searchString ->
                if (searchString.length>1){
                    isSearching = true
                    adapter.updateData(viewModel.searchResults(searchString))
                } else if (viewModel.realmReady.value == true&&searchString.isEmpty()) {
                    isSearching = false
                    adapter.updateData(viewModel.getResults())
                }
            }
        })

        if (!viewModel.resultsAdvertClosed) {
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
                                    json.put("screen", "Results of tournaments")
                                    (activity as MainActivity).mixpanelAPI!!
                                        .track("AdBannerCloseClick", json)
                                    binding.advertMain.isVisible = false
                                    viewModel.resultsAdvertClosed = true
                                }
                            binding.advertMain
                                .findViewById<ImageView>(R.id.advertImage)
                                .setOnClickListener {
                                    val json = JSONObject()
                                    json.put("campaign", advertisement.campaign)
                                    json.put("screen", "Results of tournaments")
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

    override fun onResultsClick(resultsOfTournament: resultsOfTournament) {
        (activity as MainActivity).openResultsDetails(resultsOfTournament)
    }

    override fun onResume() {
        super.onResume()
        val json = JSONObject()
        json.put("Screen", "Tournaments Results")
        json.put("ObjectID", "No value")
        json.put("Title", "No value")
        json.put("Regions", "No value")
        (activity as MainActivity).mixpanelAPI?.track("ScreenView", json)
    }
}