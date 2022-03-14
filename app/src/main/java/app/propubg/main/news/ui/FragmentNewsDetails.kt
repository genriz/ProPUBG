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
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.propubg.R
import app.propubg.currentLanguage
import app.propubg.databinding.FragmentNewsDetailsBinding
import app.propubg.main.MainActivity
import app.propubg.main.advert.Advert
import app.propubg.main.advert.AdvertViewModel
import app.propubg.main.news.adapters.DetailsImagesAdapter
import app.propubg.main.news.model.NewsItem
import app.propubg.main.news.model.NewsViewModel
import app.propubg.main.news.model.news
import com.bumptech.glide.Glide
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import org.bson.types.ObjectId
import org.json.JSONObject


class FragmentNewsDetails: Fragment() {

    private lateinit var binding: FragmentNewsDetailsBinding
    private lateinit var adapter: DetailsImagesAdapter
    private var news: news? = null
    private val images = ArrayList<String>()
    private val viewModel: NewsViewModel by viewModels()
    private val advertViewModel: AdvertViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_news_details,
            container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.advertMain.isVisible = false
        binding.headerDetails.headerTitle.text = getString(R.string.news)

        viewModel.realmReady.observe(viewLifecycleOwner,{
            if (it==true){
                news = viewModel.getNewsById(
                    requireArguments().getSerializable("newsId") as ObjectId)
                images.clear()
                news?.let{ news_->
                    if (currentLanguage=="ru") images.addAll(news_.imageSrc_ru)
                    else images.addAll(news_.imageSrc_en)
                    adapter = DetailsImagesAdapter(images)

                    binding.newsItemPager.adapter = adapter

                    binding.dots.visibility =
                        if (images.size>1) View.VISIBLE
                        else View.GONE
                    binding.dots.setViewPager2(binding.newsItemPager)

                    val newsItem = NewsItem()
                    newsItem.news = news_
                    binding.newsItem = newsItem
                    binding.executePendingBindings()

                    val title = if (currentLanguage=="ru") news_.title_ru
                    else news_.title_en
                    val json = JSONObject()
                    json.put("Screen", "OthersDetails")
                    json.put("ObjectID", news_._id.toString())
                    json.put("Title", title)
                    json.put("Regions", news_.getRegionList())
                    (activity as MainActivity).mixpanelAPI?.track("ScreenView", json)
                }
            }
        })

        binding.headerDetails.btnBack.setOnClickListener {
            (activity as MainActivity).closeFragment()
        }

        binding.headerDetails.btnOption.setImageResource(R.drawable.ic_share)
        binding.headerDetails.btnOption.setOnClickListener {
            news?.let { news_ ->
                val link = if (currentLanguage=="ru")
                    news_.dynamicLink_ru?:""
                else news_.dynamicLink_en?:""
                if (link!=""){
                    (activity as MainActivity).shareLink(link)
                } else {
                    Firebase.dynamicLinks.createDynamicLink()
                        .setDomainUriPrefix("https://link.propubg.app")
                        .setLink(Uri.parse("https://link.propubg.app/?News=${news_._id}"))
                        .setSocialMetaTagParameters(
                            DynamicLink.SocialMetaTagParameters.Builder()
                                .setImageUrl(
                                    if (currentLanguage == "ru") Uri.parse(news_.imageSrc_ru[0]!!)
                                    else Uri.parse(news_.imageSrc_en[0]!!)
                                )
                                .setTitle(
                                    if (currentLanguage == "ru") news_.title_ru!!
                                    else news_.title_en!!
                                ).build()
                        )
                        .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
                        .setIosParameters(
                            DynamicLink.IosParameters
                                .Builder("ProPUBG").build()
                        )
                        .buildShortDynamicLink()
                        .addOnSuccessListener {
                            (activity as MainActivity).shareLink(it.shortLink.toString())
                        }
                        .addOnFailureListener {
                            Log.v("DASD", it.toString())
                        }
                }
            }

        }

        binding.btnInstagram.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse("https://www.instagram.com/propubg.app")
            startActivity(intent)
        }

        binding.btnTelegram.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse("https://t.me/propubg_app")
            startActivity(intent)
        }

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
                                binding.advertMain.isVisible = false
                            }
                        binding.advertMain
                            .findViewById<ImageView>(R.id.advertImage)
                            .setOnClickListener {
                                val json = JSONObject()
                                json.put("campaign", advertisement.campaign)
                                json.put("screen", "Detail news")
                                (activity as MainActivity).mixpanelAPI!!
                                    .track("Click banner", json)
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