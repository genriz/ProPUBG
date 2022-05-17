package app.propubg.main.broadcasts.ui

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
import app.propubg.databinding.FragmentPageBroadcastsBinding
import app.propubg.main.MainActivity
import app.propubg.main.advert.Advert
import app.propubg.main.advert.AdvertViewModel
import app.propubg.main.broadcasts.adapters.BroadcastSearchAdapter
import app.propubg.main.broadcasts.adapters.BroadcastsAdapter
import app.propubg.main.broadcasts.model.BroadcastsViewModel
import app.propubg.main.broadcasts.model.broadcast
import com.bumptech.glide.Glide
import org.bson.types.ObjectId
import org.json.JSONObject

class FragmentBroadcastsUpcoming: Fragment(), BroadcastsAdapter.OnClick,
    BroadcastSearchAdapter.OnClickListener {

    private lateinit var binding: FragmentPageBroadcastsBinding
    private val viewModel: BroadcastsViewModel by viewModels()
    private lateinit var adapter: BroadcastsAdapter
    private lateinit var adapterSearch: BroadcastSearchAdapter
    private var isSearching = false
    private val advertViewModel: AdvertViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_page_broadcasts,
            container, false)
        binding.searchBroadcasts.viewModel = viewModel
        binding.searchBroadcasts.lifecycleOwner = viewLifecycleOwner
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.advertMain.isVisible = false

        viewModel.realmReady.observe(viewLifecycleOwner,{
            it?.let{ ready ->
                if (ready){
                    binding.recyclerBroadcasts.setHasFixedSize(true)
                    adapter = BroadcastsAdapter(requireActivity(),
                        viewModel.getBroadcastsUpcoming(), this)
                    adapterSearch = BroadcastSearchAdapter(this)
                    adapter.data?.forEach { broadcast ->
                        broadcast?.let{
                            broadcast.tournamentExist = broadcast.objectIDOfTournament!=null
                                    &&broadcast.objectIDOfTournament!!.isNotEmpty()
                                    &&ObjectId.isValid(broadcast.objectIDOfTournament)
                                    &&viewModel.getTournamentById(ObjectId(broadcast.objectIDOfTournament))!=null
                        }
                    }
                    adapterSearch.currentList.forEach { broadcast ->
                        broadcast?.let{
                            broadcast.tournamentExist = broadcast.objectIDOfTournament!=null
                                    &&broadcast.objectIDOfTournament!!.isNotEmpty()
                                    &&ObjectId.isValid(broadcast.objectIDOfTournament)
                                    &&viewModel.getTournamentById(ObjectId(broadcast.objectIDOfTournament))!=null
                        }
                    }
                    binding.recyclerBroadcasts.adapter = adapter
                    binding.recyclerBroadcastsSearch.adapter = adapterSearch
                    binding.recyclerBroadcastsSearch.isVisible = false

                    adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
                        override fun onChanged() {
                            super.onChanged()
                            if (adapter.itemCount==0) {
                                binding.noLiveBroadcasts.visibility = View.VISIBLE
                                if (isSearching)
                                    binding.noLiveBroadcasts.setText(R.string.search_empty)
                                else
                                    binding.noLiveBroadcasts.setText(R.string.no_upcoming_broadcasts)
                            }
                            else binding.noLiveBroadcasts.visibility = View.GONE
                        }
                    })
                }
            }
        })

        binding.searchBroadcasts.searchCancel.setOnClickListener {
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
                    //adapter.updateData(viewModel.searchBroadcastsUpcoming(searchString))
                    binding.recyclerBroadcasts.isVisible = false
                    binding.recyclerBroadcastsSearch.isVisible = true
                    adapterSearch.submitList(viewModel.searchBroadcastsPastLocal(searchString))
                    binding.recyclerBroadcastsSearch.postDelayed({
                        if (adapterSearch.currentList.size==0) {
                            binding.noLiveBroadcasts.visibility = View.VISIBLE
                            binding.noLiveBroadcasts.setText(R.string.search_empty)
                        } else binding.noLiveBroadcasts.visibility = View.GONE
                    },100)
                } else if (viewModel.realmReady.value == true&&searchString.isEmpty()) {
                    isSearching = false
                    binding.recyclerBroadcasts.isVisible = true
                    binding.recyclerBroadcastsSearch.isVisible = false
                    adapter.updateData(viewModel.getBroadcastsUpcoming())
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
                                    json.put("screen", "Broadcasts[Upcoming]")
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
                                    json.put("screen", "Broadcasts[Upcoming]")
                                    (activity as MainActivity).mixpanelAPI!!
                                        .track("AdBannerClick", json)
                                    val link =
                                        if (currentLanguage=="ru")
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

    override fun onWatchClick(broadcast: broadcast) {
        broadcast.link?.let{
            val stage = if (currentLanguage =="ru") broadcast.stage_ru
            else broadcast.stage_en
            val day = "${requireContext().getString(R.string.day)} ${broadcast.dayOfTournament}"
            val json = JSONObject()
            json.put("ObjectID", broadcast._id)
            json.put("Stage", stage)
            json.put("Title", broadcast.title)
            json.put("Day of tournament", day)
            json.put("Status of broadcast", "Upcoming")
            (activity as MainActivity).mixpanelAPI?.track("WatchBroadcastClick", json)

            if (URLUtil.isValidUrl(it)) {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse(it)
                startActivity(intent)
            }
        }
    }

    override fun onTeamsClick(broadcast: broadcast) {
        val stage = if (currentLanguage =="ru") broadcast.stage_ru
        else broadcast.stage_en
        val day = "${requireContext().getString(R.string.day)} ${broadcast.dayOfTournament}"
        val json = JSONObject()
        json.put("ObjectID", broadcast._id)
        json.put("Stage", stage)
        json.put("Title", broadcast.title)
        json.put("Day of tournament", day)
        json.put("Status of broadcast", "Upcoming")
        (activity as MainActivity).mixpanelAPI?.track("“ShowTeamsListClick", json)

        (activity as MainActivity).showBottomSheetTeams(broadcast)
    }

    override fun onMoreClick(broadcast: broadcast) {
        val stage = if (currentLanguage =="ru") broadcast.stage_ru
        else broadcast.stage_en
        val day = "${requireContext().getString(R.string.day)} ${broadcast.dayOfTournament}"
        val json = JSONObject()
        json.put("ObjectID", broadcast._id)
        json.put("Stage", stage)
        json.put("Title", broadcast.title)
        json.put("Day of tournament", day)
        json.put("Status of broadcast", "Upcoming")
        (activity as MainActivity).mixpanelAPI?.track("“ShowTournamentDetailsClick", json)

        broadcast.objectIDOfTournament?.let{
            if (it.isNotEmpty()&&ObjectId.isValid(broadcast.objectIDOfTournament))
                (activity as MainActivity).showBottomSheetTournament(ObjectId(it))
        }
    }

    override fun isEmpty(isEmpty: Boolean) {
        if (!isEmpty) binding.noLiveBroadcasts.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        val json = JSONObject()
        json.put("Screen", "Broadcasts[Upcoming]")
        json.put("ObjectID", "No value")
        json.put("Title", "No value")
        json.put("Regions", "No value")
        (activity as MainActivity).mixpanelAPI?.track("ScreenView", json)
    }

}