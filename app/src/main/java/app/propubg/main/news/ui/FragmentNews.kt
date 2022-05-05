package app.propubg.main.news.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.viewpager2.widget.ViewPager2
import app.propubg.R
import app.propubg.currentLanguage
import app.propubg.databinding.FragmentNewsBinding
import app.propubg.main.MainActivity
import app.propubg.main.advert.Advert
import app.propubg.main.advert.AdvertViewModel
import app.propubg.main.news.adapters.FragmentNewsPageAdapter
import app.propubg.main.news.model.NewsViewModel
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import org.bson.types.ObjectId
import org.json.JSONObject

class FragmentNews: Fragment() {

    private lateinit var binding: FragmentNewsBinding
    private lateinit var adapter: FragmentNewsPageAdapter
    private val viewModel: NewsViewModel by activityViewModels()
    private val advertViewModel by lazy { ViewModelProvider(this)[AdvertViewModel::class.java] }
    private var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_news, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.advertMain.isVisible = false
        binding.header.headerTitle.text = getString(R.string.news)
        binding.header.btnBack.visibility = View.INVISIBLE

        viewModel.realmReady.observe(viewLifecycleOwner,{
            if (it){
                arguments?.let{
                    currentPage = requireArguments().getInt("page")
                    val id = if (currentPage==1) requireArguments()
                        .getSerializable("newsId") as ObjectId?
                    else requireArguments()
                        .getSerializable("reshuffleId") as ObjectId?
                    id?.let{
                        if (currentPage==1){
                            val news = viewModel.getNewsById(id)
                            news?.let {
                                (activity as MainActivity).openNewsDetails(news)
                                arguments = null
                            }
                        } else {
                            val reshuffle = viewModel.getReshuffleById(id)
                            reshuffle?.let {
                                (activity as MainActivity).openReshufflesDetails(reshuffle)
                                arguments = null
                            }
                        }

                    }
                }

                adapter = FragmentNewsPageAdapter(this)
                binding.pagerNews.adapter = adapter
                TabLayoutMediator(binding.tabsNews, binding.pagerNews) { tab, position ->
                    tab.text = when (position){
                        0 -> getString(R.string.reshuffles)
                        else -> getString(R.string.global)
                    }
                }.attach()
                binding.pagerNews.offscreenPageLimit = 2

                binding.pagerNews.setCurrentItem(currentPage, false)

                binding.pagerNews.registerOnPageChangeCallback(object:ViewPager2.OnPageChangeCallback(){
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        if (currentPage!=position) {
                            currentPage = position
                            viewModel.searchString.value = ""
                        }
                    }
                })

                binding.header.btnOption.setOnClickListener {
                    (activity as MainActivity).showSheetInfo()
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
                                    json.put("screen", "News")
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
                                    json.put("screen", "News")
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

        viewModel.reset.observe(viewLifecycleOwner,{
            if (it) Toast.makeText(requireContext(), "Client reset", Toast.LENGTH_SHORT).show()
        })
    }

    fun setPage(i: Int) {
        currentPage = i
        binding.pagerNews.setCurrentItem(currentPage, false)
    }
}