package app.propubg.main.news.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import app.propubg.databinding.FragmentPageNewsBinding
import app.propubg.main.MainActivity
import app.propubg.main.advert.Advert
import app.propubg.main.advert.AdvertViewModel
import app.propubg.main.news.adapters.NewsAdapter
import app.propubg.main.news.adapters.NewsSearchAdapter
import app.propubg.main.news.model.NewsViewModel
import app.propubg.main.news.model.news
import app.propubg.realmApp
import com.bumptech.glide.Glide
import org.json.JSONObject

class FragmentPageNews: Fragment(), NewsAdapter.OnClick, NewsSearchAdapter.OnClickListener {

    private lateinit var binding: FragmentPageNewsBinding
    private val viewModel: NewsViewModel by viewModels()
    private lateinit var adapter: NewsAdapter
    private lateinit var adapterSearch: NewsSearchAdapter
    private var isSearching = false
    private val advertViewModel: AdvertViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_page_news,
            container, false)
        binding.searchNews.viewModel = viewModel
        binding.searchNews.lifecycleOwner = viewLifecycleOwner
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.advertMain.isVisible = false
        binding.recyclerNews.setHasFixedSize(true)

        viewModel.realmReady.observe(viewLifecycleOwner,{ ready ->
            if (ready){
                adapter = NewsAdapter(viewModel.getNews(), this)
                adapterSearch = NewsSearchAdapter(this)
                binding.recyclerNews.adapter = adapter
                binding.recyclerNewsSearch.adapter = adapterSearch
                binding.recyclerNewsSearch.isVisible = false

                adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        super.onItemRangeInserted(positionStart, itemCount)
                        adapter.notifyItemChanged(positionStart)
                        adapter.notifyItemChanged(positionStart+1)
                    }

                    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                        super.onItemRangeRemoved(positionStart, itemCount)
                        adapter.notifyItemChanged(positionStart-1)
                        adapter.notifyItemChanged(positionStart+1)
                    }

                    override fun onChanged() {
                        super.onChanged()
                        if (adapter.itemCount==0) {
                            binding.noNews.visibility = View.VISIBLE
                            if (isSearching)
                                binding.noNews.setText(R.string.search_empty)
                            else
                                binding.noNews.setText(R.string.no_reshuffles)
                        }
                        else binding.noNews.visibility = View.GONE
                    }
                })

                binding.searchNews.searchCancel.setOnClickListener {
                    viewModel.searchString.value = ""
                    binding.expandLayout.setExpanded(false)
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
                            binding.expandLayout.setExpanded(true)
                            //adapter.updateData(viewModel.searchNews(searchString))
                            binding.recyclerNews.isVisible = false
                            binding.recyclerNewsSearch.isVisible = true
                            adapterSearch.submitList(viewModel.searchNewsLocal(searchString))
                            binding.recyclerNewsSearch.postDelayed({
                                if (adapterSearch.currentList.size==0) {
                                    binding.noNews.visibility = View.VISIBLE
                                    binding.noNews.setText(R.string.search_empty)
                                } else binding.noNews.visibility = View.GONE
                            },100)
                        } else if (viewModel.realmReady.value == true) {
                            isSearching = false
                            binding.recyclerNews.isVisible = true
                            binding.recyclerNewsSearch.isVisible = false
                            adapter.updateData(viewModel.getNews())
                        }
                    }
                })
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
                            Log.v("DASD", "${advertItem.advert!!.typeOfAd!!} - $image")
                            if (advertItem.advert!!.typeOfAd=="image")
                                Glide.with(requireContext()).load(image)
                                    .into(binding.advertMain.findViewById(R.id.advertImage))
                            else if (advertItem.advert!!.typeOfAd=="gif")
                                Glide.with(requireContext()).asGif().load(image)
                                    .into(binding.advertMain.findViewById(R.id.advertImage))
                            binding.advertMain.isVisible = true
                            binding.advertMain
                                .findViewById<ImageView>(R.id.advertClose)
                                .setOnClickListener {
                                    val json = JSONObject()
                                    json.put("campaign", advertisement.campaign)
                                    json.put("screen", "News[Others]")
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
                                    json.put("screen", "News[Others]")
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

    override fun onNewsClick(news: news, imageView: ImageView) {
        (activity as MainActivity).openNewsDetails(news)
        realmApp.currentUser()?.let{user ->
            if (!news.listViewers.contains(user.id)){
                viewModel.updateNews(news, user.id)
            }
        }
    }

    override fun onItemClick(item: news) {
        (activity as MainActivity).openNewsDetails(item)
        realmApp.currentUser()?.let{user ->
            if (!item.listViewers.contains(user.id)){
                viewModel.updateNews(item, user.id)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val json = JSONObject()
        json.put("Screen", "News[Others]")
        json.put("ObjectID", "No value")
        json.put("Title", "No value")
        json.put("Regions", "No value")
        (activity as MainActivity).mixpanelAPI?.track("ScreenView", json)
    }
}