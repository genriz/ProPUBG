package app.propubg.main.content.ui

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
import app.propubg.databinding.FragmentPageContentBinding
import app.propubg.main.MainActivity
import app.propubg.main.advert.Advert
import app.propubg.main.advert.AdvertViewModel
import app.propubg.main.content.adapters.ContentAdapter
import app.propubg.main.content.adapters.ContentSearchAdapter
import app.propubg.main.content.model.ContentViewModel
import app.propubg.main.content.model.content
import com.bumptech.glide.Glide
import org.json.JSONObject

class FragmentContentInterview: Fragment(), ContentAdapter.OnClick,
    ContentSearchAdapter.OnClickListener {

    private lateinit var binding: FragmentPageContentBinding
    private val viewModel: ContentViewModel by viewModels()
    private lateinit var adapter: ContentAdapter
    private lateinit var adapterSearch: ContentSearchAdapter
    private var isSearching = false
    private val advertViewModel: AdvertViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_page_content,
            container, false)
        binding.searchContent.viewModel = viewModel
        binding.searchContent.lifecycleOwner = viewLifecycleOwner
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.advertMain.isVisible = false

        viewModel.realmReady.observe(viewLifecycleOwner,{
            it?.let{ ready ->
                if (ready){
                    binding.recyclerContent.setHasFixedSize(true)
                    adapter = ContentAdapter(viewModel.getContentInterview(), this)
                    adapterSearch = ContentSearchAdapter(this)
                    binding.recyclerContent.adapter = adapter
                    binding.recyclerContentSearch.adapter = adapterSearch
                    binding.recyclerContentSearch.isVisible = false

                    adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
                        override fun onChanged() {
                            super.onChanged()
                            if (adapter.itemCount==0) {
                                binding.noContent.visibility = View.VISIBLE
                                if (isSearching)
                                    binding.noContent.setText(R.string.search_empty)
                                else
                                    binding.noContent.setText(R.string.no_content_interview)
                            }
                            else binding.noContent.visibility = View.GONE
                        }
                    })
                }
            }
        })

        binding.searchContent.searchCancel.setOnClickListener {
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
                    //adapter.updateData(viewModel.searchContentInterview(searchString))
                    binding.recyclerContent.isVisible = false
                    binding.recyclerContentSearch.isVisible = true
                    adapterSearch.submitList(viewModel.searchContentInterviewLocal(searchString))
                    binding.recyclerContentSearch.postDelayed({
                        if (adapterSearch.currentList.size==0) {
                            binding.noContent.visibility = View.VISIBLE
                            binding.noContent.setText(R.string.search_empty)
                        } else binding.noContent.visibility = View.GONE
                    },100)
                } else if (viewModel.realmReady.value == true) {
                    isSearching = false
                    binding.recyclerContent.isVisible = true
                    binding.recyclerContentSearch.isVisible = false
                    adapter.updateData(viewModel.getContentInterview())
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
                                    json.put("screen", "Content[Interview]")
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
                                    json.put("screen", "Content[Interview]")
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

    override fun onWatchClick(content: content) {
        content.link?.let{
            val title = if (currentLanguage=="ru") content.title_ru
            else content.title_en
            val json = JSONObject()
            json.put("ObjectID", content._id)
            json.put("Type of content", "Interview")
            json.put("Title", title)
            (activity as MainActivity).mixpanelAPI?.track("WatchContentClick", json)

            if (URLUtil.isValidUrl(it)) {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse(it)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val json = JSONObject()
        json.put("Screen", "Content[Interview]")
        json.put("ObjectID", "No value")
        json.put("Title", "No value")
        json.put("Regions", "No value")
        (activity as MainActivity).mixpanelAPI?.track("ScreenView", json)
    }

}