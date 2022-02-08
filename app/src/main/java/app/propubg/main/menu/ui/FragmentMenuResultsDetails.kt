package app.propubg.main.menu.ui

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
import app.propubg.databinding.FragmentMenuResultsDetailsBinding
import app.propubg.main.MainActivity
import app.propubg.main.advert.Advert
import app.propubg.main.advert.AdvertViewModel
import app.propubg.main.menu.model.MenuViewModel
import app.propubg.main.menu.model.ResultsItem
import app.propubg.main.menu.model.resultsOfTournament
import app.propubg.main.news.adapters.DetailsImagesAdapter
import com.bumptech.glide.Glide
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import org.bson.types.ObjectId
import org.json.JSONObject


class FragmentMenuResultsDetails: Fragment() {

    private lateinit var binding: FragmentMenuResultsDetailsBinding
    private lateinit var adapter: DetailsImagesAdapter
    private var resultsOfTournament: resultsOfTournament? = null
    private val images = ArrayList<String>()
    private val viewModel: MenuViewModel by viewModels()
    private val advertViewModel: AdvertViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu_results_details,
            container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.advertMain.isVisible = false

        viewModel.realmReady.observe(viewLifecycleOwner,{
            if (it==true){
                resultsOfTournament = viewModel.getResultsById(
                    requireArguments()
                        .getSerializable("resultsId") as ObjectId)
                val resultsItem = ResultsItem()
                resultsItem.resultsOfTournament = resultsOfTournament
                binding.resultsItem = resultsItem
                binding.executePendingBindings()

                images.clear()
                if (currentLanguage=="ru") images.addAll(resultsOfTournament!!.imageSrc_ru)
                else images.addAll(resultsOfTournament!!.imageSrc_en)
                adapter = DetailsImagesAdapter(images)

                binding.headerDetails.headerTitle.text = resultsOfTournament?.title

                binding.resultsItemPager.adapter = adapter

                binding.dots.visibility =
                    if (images.size>1) View.VISIBLE
                    else View.GONE
                binding.dots.setViewPager2(binding.resultsItemPager)
            }
        })

        binding.headerDetails.btnBack.setOnClickListener {
            (activity as MainActivity).closeFragment()
        }

        binding.headerDetails.btnOption.setImageResource(R.drawable.ic_share)
        binding.headerDetails.btnOption.setOnClickListener {
            resultsOfTournament?.let { results_ ->
                Firebase.dynamicLinks.createDynamicLink()
                    .setDomainUriPrefix("https://link.propubg.app")
                    .setLink(Uri.parse("https://link.propubg.app/?ResultsOfTournament=${results_._id}"))
                    .setSocialMetaTagParameters(DynamicLink.SocialMetaTagParameters.Builder()
                        .setImageUrl(if (currentLanguage=="ru") Uri.parse(results_.imageSrc_ru[0]!!)
                        else Uri.parse(results_.imageSrc_en[0]!!))
                        .setTitle(if (currentLanguage=="ru") "${results_.title} ${results_.stage_ru}"
                        else "${results_.title} ${results_.stage_en}")
                        .setDescription(getString(R.string.results_text)).build())
                    .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
                    .setIosParameters(DynamicLink.IosParameters
                        .Builder("ProPUBG").build())
                    .buildShortDynamicLink()
                    .addOnSuccessListener {
                        (activity as MainActivity).shareLink(it.shortLink.toString())
                    }
                    .addOnFailureListener {
                        Log.v("DASD", it.toString())
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
                                json.put("screen", "Detail results of tournaments")
                                (activity as MainActivity).mixpanelAPI!!
                                    .track("Click banner", json)
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