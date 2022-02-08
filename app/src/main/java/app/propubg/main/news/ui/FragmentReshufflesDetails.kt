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
        binding.lifecycleOwner = this
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

                val images = ArrayList<String>()
                if (currentLanguage=="ru") images.addAll(reshuffle!!.imageSrc_ru)
                else images.addAll(reshuffle!!.imageSrc_en)
                adapter = DetailsImagesAdapter(images)
                binding.newsItemPager.adapter = adapter

                val reshuffleItem = ReshuffleItem()
                reshuffleItem.reshuffle = reshuffle
                binding.reshuffleItem = reshuffleItem
                binding.executePendingBindings()

            }
        })

        binding.headerDetails.btnBack.setOnClickListener {
            (activity as MainActivity).closeFragment()
        }

        binding.headerDetails.btnOption.setImageResource(R.drawable.ic_share)
        binding.headerDetails.btnOption.setOnClickListener {
            reshuffle?.let{ reshuffle_ ->
                Firebase.dynamicLinks.createDynamicLink()
                    .setDomainUriPrefix("https://link.propubg.app")
                    .setLink(Uri.parse("https://link.propubg.app/?Reshuffle=${reshuffle_._id}"))
                    .setSocialMetaTagParameters(
                        DynamicLink.SocialMetaTagParameters.Builder()
                            .setImageUrl(if (currentLanguage=="ru") Uri.parse(reshuffle_.imageSrc_ru[0]!!)
                            else Uri.parse(reshuffle_.imageSrc_en[0]!!))
                            .setTitle(if (currentLanguage=="ru") reshuffle_.title_ru!!
                            else reshuffle_.title_en!!).build())
                    .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
                    .setIosParameters(DynamicLink.IosParameters
                        .Builder("ProPUBG").build())
                    .buildShortDynamicLink()
                    .addOnSuccessListener { link ->
                        (activity as MainActivity).shareLink(link.shortLink.toString())
                    }
                    .addOnFailureListener { exception ->
                        Log.v("DASD", exception.toString())
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