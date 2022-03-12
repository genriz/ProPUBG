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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import app.propubg.R
import app.propubg.databinding.FragmentPageBroadcastsBinding
import app.propubg.main.MainActivity
import app.propubg.main.broadcasts.adapters.BroadcastsAdapter
import app.propubg.main.broadcasts.model.BroadcastsViewModel
import app.propubg.main.broadcasts.model.broadcast
import org.bson.types.ObjectId

class FragmentBroadcastsUpcoming: Fragment(), BroadcastsAdapter.OnClick {

    private lateinit var binding: FragmentPageBroadcastsBinding
    private val viewModel: BroadcastsViewModel by viewModels()
    private lateinit var adapter: BroadcastsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_page_broadcasts,
            container, false)
        binding.searchBroadcasts.viewModel = viewModel
        binding.searchBroadcasts.lifecycleOwner = this
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.realmReady.observe(viewLifecycleOwner,{
            it?.let{ ready ->
                if (ready){
                    binding.recyclerBroadcasts.setHasFixedSize(true)
                    adapter = BroadcastsAdapter(requireActivity(),
                        viewModel.getBroadcastsUpcoming(), this)
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
                                binding.noLiveBroadcasts.setText(R.string.no_upcoming_broadcasts)
                            } else binding.noLiveBroadcasts.visibility = View.GONE
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
                    adapter.updateData(viewModel.searchBroadcastsUpcoming(searchString))
                    adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
                        override fun onChanged() {
                            super.onChanged()
                            if (adapter.itemCount==0) {
                                binding.noLiveBroadcasts.visibility = View.VISIBLE
                                binding.noLiveBroadcasts.setText(R.string.search_empty)
                            }
                            else binding.noLiveBroadcasts.visibility = View.GONE
                        }
                    })
                } else if (viewModel.realmReady.value == true&&searchString.isEmpty()) {
                    adapter.updateData(viewModel.getBroadcastsUpcoming())
                }
            }
        })

    }

    override fun onWatchClick(broadcast: broadcast) {
        broadcast.link?.let{
            if (URLUtil.isValidUrl(it)) {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse(it)
                startActivity(intent)
            }
        }
    }

    override fun onTeamsClick(broadcast: broadcast) {
        (activity as MainActivity).showBottomSheetTeams(broadcast)
    }

    override fun onMoreClick(broadcast: broadcast) {
        broadcast.objectIDOfTournament?.let{
            if (it.isNotEmpty()&&ObjectId.isValid(broadcast.objectIDOfTournament))
                (activity as MainActivity).showBottomSheetTournament(ObjectId(it))
        }
    }

    override fun isEmpty(isEmpty: Boolean) {
        if (!isEmpty) binding.noLiveBroadcasts.visibility = View.GONE
    }

}