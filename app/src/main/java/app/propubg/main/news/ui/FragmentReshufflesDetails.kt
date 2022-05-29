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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import app.propubg.R
import app.propubg.appConfig
import app.propubg.currentLanguage
import app.propubg.databinding.FragmentReshufflesDetailsBinding
import app.propubg.main.MainActivity
import app.propubg.main.advert.Advert
import app.propubg.main.advert.AdvertViewModel
import app.propubg.main.news.adapters.DetailsImagesAdapter
import app.propubg.main.news.model.NewsViewModel
import app.propubg.main.news.model.ReshuffleItem
import app.propubg.main.news.model.reshuffle
import com.bumptech.glide.Glide
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import org.bson.types.ObjectId
import org.json.JSONObject

class FragmentReshufflesDetails: Fragment() {

    private lateinit var binding: FragmentReshufflesDetailsBinding
    private lateinit var adapter: DetailsImagesAdapter
    private var reshuffle: reshuffle? = null
    private val viewModel: NewsViewModel by viewModels()
    private val advertViewModel: AdvertViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reshuffles_details,
            container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.headerDetails.headerTitle.text = getString(R.string.news)
        binding.advertMain.isVisible = false

        viewModel.realmReady.observe(viewLifecycleOwner, { ready ->
            if (ready){
                reshuffle = viewModel.getReshuffleById(requireArguments()
                    .getSerializable("reshuffleId") as ObjectId)
                reshuffle?.let{ reshuffle_ ->
                    val images = ArrayList<String>()
                    if (currentLanguage=="ru") images.addAll(reshuffle_.imageSrc_ru)
                    else images.addAll(reshuffle_.imageSrc_en)
                    adapter = DetailsImagesAdapter(images)
                    binding.newsItemPager.adapter = adapter

                    binding.dots.visibility =
                        if (images.size>1) View.VISIBLE
                        else View.GONE
                    binding.dots.setViewPager2(binding.newsItemPager)

                    val reshuffleItem = ReshuffleItem()
                    reshuffleItem.reshuffle = reshuffle_
                    binding.reshuffleItem = reshuffleItem
                    binding.executePendingBindings()

                    val title = if (currentLanguage=="ru") reshuffle_.title_ru
                    else reshuffle_.title_en
                    val json = JSONObject()
                    json.put("Screen", "ReshuffleDetails")
                    json.put("ObjectID", reshuffle_._id.toString())
                    json.put("Title", title)
                    json.put("Regions", reshuffle_.getRegionList())
                    (activity as MainActivity).mixpanelAPI?.track("ScreenView", json)

                }
            }
        })

        binding.headerDetails.btnBack.setOnClickListener {
            (activity as MainActivity).closeFragment()
        }

        binding.headerDetails.btnOption.setImageResource(R.drawable.ic_share)
        binding.headerDetails.btnOption.setOnClickListener {
            reshuffle?.let{ reshuffle_ ->
                val link = if (currentLanguage=="ru")
                    reshuffle_.dynamicLink_ru?:""
                else reshuffle_.dynamicLink_en?:""
                val title = if (currentLanguage == "ru") reshuffle_.title_ru!!
                else reshuffle_.title_en!!
                if (link!=""){
                    (activity as MainActivity).shareLink(link,
                        reshuffle_._id.toString(), "Reshuffle",
                        title, reshuffle_.getRegionList())
                } else {
                    Firebase.dynamicLinks.createDynamicLink()
                        .setDomainUriPrefix("https://link.propubg.app")
                        .setLink(Uri.parse("https://propubg.app/?Reshuffle=${reshuffle_._id}"))
                        .setSocialMetaTagParameters(
                            DynamicLink.SocialMetaTagParameters.Builder()
                                .setImageUrl(
                                    if (currentLanguage == "ru") Uri.parse(reshuffle_.imageSrc_ru[0]!!)
                                    else Uri.parse(reshuffle_.imageSrc_en[0]!!)
                                )
                                .setTitle(title).build()
                        )
                        .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
                        .setIosParameters(
                            DynamicLink.IosParameters
                                .Builder("ProPUBG").build()
                        )
                        .buildShortDynamicLink()
                        .addOnSuccessListener {
                            (activity as MainActivity).shareLink(it.shortLink.toString(),
                                reshuffle_._id.toString(), "Reshuffle",
                                title, reshuffle_.getRegionList())
                        }
                        .addOnFailureListener { exception ->
                            Log.v("DASD", exception.toString())
                        }
                }
            }
        }

        binding.btnInstagram.setOnClickListener {
            val json = JSONObject()
            json.put("Screen", "ReshuffleDetails")
            json.put("Social network", "Instagram")
            (activity as MainActivity).mixpanelAPI?.track("SocialButtonClick", json)

            appConfig?.socialLink_Instagram?.let{
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse(it)
                startActivity(intent)
            }
        }

        binding.btnTelegram.setOnClickListener {
            val json = JSONObject()
            json.put("Screen", "ReshuffleDetails")
            json.put("Social network", "Telegram")
            (activity as MainActivity).mixpanelAPI?.track("SocialButtonClick", json)

            appConfig?.socialLink_Telegram?.let {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse(it)
                startActivity(intent)
            }
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
                                val json = JSONObject()
                                json.put("campaign", advertisement.campaign)
                                json.put("screen", "Detail news")
                                (activity as MainActivity).mixpanelAPI!!
                                    .track("AdBannerCloseClick", json)
                                binding.advertMain.isVisible = false
                            }
                        binding.advertMain
                            .findViewById<ImageView>(R.id.advertImage)
                            .setOnClickListener {
                                val json = JSONObject()
                                json.put("campaign", advertisement.campaign)
                                json.put("screen", "Detail news")
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