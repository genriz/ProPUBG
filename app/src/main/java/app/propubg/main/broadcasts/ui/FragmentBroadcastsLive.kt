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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import app.propubg.R
import app.propubg.currentLanguage
import app.propubg.databinding.FragmentPageBroadcastsBinding
import app.propubg.main.MainActivity
import app.propubg.main.broadcasts.adapters.BroadcastsAdapter
import app.propubg.main.broadcasts.model.BroadcastsViewModel
import app.propubg.main.broadcasts.model.broadcast
import org.bson.types.ObjectId
import org.json.JSONObject


class FragmentBroadcastsLive: Fragment(), BroadcastsAdapter.OnClick {

    private lateinit var binding: FragmentPageBroadcastsBinding
    private val viewModel: BroadcastsViewModel by viewModels()
    private lateinit var adapter: BroadcastsAdapter
    private var isSearching = false

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

        viewModel.realmReady.observe(viewLifecycleOwner,{
            it?.let{ ready ->
                if (ready){
                    binding.recyclerBroadcasts.setHasFixedSize(true)
                    adapter = BroadcastsAdapter(requireActivity(),
                        viewModel.getBroadcastsLive(), this)
                    adapter.data?.forEach { broadcast ->
                        broadcast?.let{
                            broadcast.tournamentExist = broadcast.objectIDOfTournament!=null
                                    &&broadcast.objectIDOfTournament!!.isNotEmpty()
                                    &&ObjectId.isValid(broadcast.objectIDOfTournament)
                                    &&viewModel.getTournamentById(ObjectId(broadcast.objectIDOfTournament))!=null
                        }
                    }
                    binding.recyclerBroadcasts.adapter = adapter

                    adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
                        override fun onChanged() {
                            super.onChanged()
                            if (adapter.itemCount==0) {
                                binding.noLiveBroadcasts.visibility = View.VISIBLE
                                if (isSearching)
                                    binding.noLiveBroadcasts.setText(R.string.search_empty)
                                else
                                    binding.noLiveBroadcasts.setText(R.string.no_live_broadcasts)
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
                    adapter.updateData(viewModel.searchBroadcastsLive(searchString))
                } else if (viewModel.realmReady.value == true&&searchString.isEmpty()) {
                    isSearching = false
                    adapter.updateData(viewModel.getBroadcastsLive())
                }
            }
        })

    }

    override fun onWatchClick(broadcast: broadcast) {
        broadcast.link?.let{
            val stage = if (currentLanguage=="ru") broadcast.stage_ru
            else broadcast.stage_en
            val json = JSONObject()
            json.put("ObjectID", broadcast._id)
            json.put("Stage", stage)
            json.put("Title", broadcast.title)
            json.put("Day of tournament", broadcast.dayOfTournament)
            json.put("Status of broadcast", "Live")
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
        val json = JSONObject()
        json.put("ObjectID", broadcast._id)
        json.put("Stage", stage)
        json.put("Title", broadcast.title)
        json.put("Day of tournament", broadcast.dayOfTournament)
        json.put("Status of broadcast", "Live")
        (activity as MainActivity).mixpanelAPI?.track("ShowTeamsListClick", json)

        (activity as MainActivity).showBottomSheetTeams(broadcast)
    }

    override fun onMoreClick(broadcast: broadcast) {
        val stage = if (currentLanguage =="ru") broadcast.stage_ru
        else broadcast.stage_en
        val json = JSONObject()
        json.put("ObjectID", broadcast._id)
        json.put("Stage", stage)
        json.put("Title", broadcast.title)
        json.put("Day of tournament", broadcast.dayOfTournament)
        json.put("Status of broadcast", "Live")
        (activity as MainActivity).mixpanelAPI?.track("ShowTournamentDetailsClick", json)

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
        json.put("Screen", "Broadcasts[Live]")
        json.put("ObjectID", "No value")
        json.put("Title", "No value")
        json.put("Regions", "No value")
        (activity as MainActivity).mixpanelAPI?.track("ScreenView", json)
    }
}