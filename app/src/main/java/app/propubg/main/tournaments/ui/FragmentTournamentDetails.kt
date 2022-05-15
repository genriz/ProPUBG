package app.propubg.main.tournaments.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.drawable.Drawable
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
import androidx.fragment.app.viewModels
import app.propubg.R
import app.propubg.appConfig
import app.propubg.currentLanguage
import app.propubg.databinding.FragmentTournamentDetailsBinding
import app.propubg.main.MainActivity
import app.propubg.main.advert.Advert
import app.propubg.main.advert.AdvertViewModel
import app.propubg.main.news.adapters.DetailsImagesAdapter
import app.propubg.main.tournaments.model.TournamentItem
import app.propubg.main.tournaments.model.TournamentsViewModel
import app.propubg.main.tournaments.model.tournament
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import org.bson.types.ObjectId
import org.json.JSONObject

class FragmentTournamentDetails: Fragment() {

    private lateinit var binding: FragmentTournamentDetailsBinding
    private var tournament: tournament? = null
    private val images = ArrayList<String>()
    private lateinit var adapter: DetailsImagesAdapter
    private val viewModel: TournamentsViewModel by viewModels()
    private val advertViewModel: AdvertViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tournament_details,
            container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.advertMain.isVisible = false

        val type = requireArguments().getString("type")?:"none"

        viewModel.realmReady.observe(viewLifecycleOwner,{
            if (it==true){
                tournament = viewModel.getTournamentById(
                    requireArguments()
                        .getSerializable("tournamentId") as ObjectId)
                images.clear()
                tournament?.let{tournament_ ->
                    binding.headerDetails.headerTitle.text = tournament_.title

                    images.addAll(tournament_.imageSrc)
                    adapter = DetailsImagesAdapter(images)
                    binding.tournamentItemPager.adapter = adapter

                    binding.dots.visibility =
                        if (images.size>1) View.VISIBLE
                        else View.GONE
                    binding.dots.setViewPager2(binding.tournamentItemPager)

                    val tournamentItem = TournamentItem()
                    tournamentItem.tournament = tournament_
                    binding.tournamentItem = tournamentItem
                    binding.executePendingBindings()

                    val title = tournament_.title
                    val json = JSONObject()
                    json.put("Screen", type)
                    json.put("ObjectID", tournament_._id.toString())
                    json.put("Title", title)
                    json.put("Regions", tournament_.getRegionList())
                    (activity as MainActivity).mixpanelAPI?.track("ScreenView", json)

                }
            }
        })

        binding.headerDetails.btnBack.setOnClickListener {
            (activity as MainActivity).closeFragment()
        }

        binding.headerDetails.btnOption.setImageResource(R.drawable.ic_share)
        binding.headerDetails.btnOption.setOnClickListener {
            tournament?.let{ tournament_ ->
                val link = if (currentLanguage=="ru")
                    tournament_.dynamicLink_ru?:""
                else tournament_.dynamicLink_en?:""
                if (link!=""){
                    (activity as MainActivity).shareLink(link,
                        tournament_._id.toString(), "Tournament",
                        tournament_.title!!, tournament_.getRegionList())
                } else {
                    Firebase.dynamicLinks.createDynamicLink()
                        .setDomainUriPrefix("https://link.propubg.app")
                        .setLink(Uri.parse("https://link.propubg.app/?Tournaments=${tournament_._id}"))
                        .setSocialMetaTagParameters(
                            DynamicLink.SocialMetaTagParameters.Builder()
                                .setImageUrl(Uri.parse(tournament_.imageSrc[0]!!))
                                .setTitle(tournament_.title!!)
                                .build()
                        )
                        .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
                        .setIosParameters(
                            DynamicLink.IosParameters
                                .Builder("ProPUBG").build()
                        )
                        .buildShortDynamicLink()
                        .addOnSuccessListener {
                            (activity as MainActivity).shareLink(it.shortLink.toString(),
                                tournament_._id.toString(), "Tournaments",
                                tournament_.title!!, tournament_.getRegionList())
                        }
                        .addOnFailureListener {
                            Log.v("DASD", it.toString())
                        }
                }
            }
        }

        binding.btnRegister.setOnClickListener {
            tournament?.link?.let{
                val json = JSONObject()
                json.put("ObjectID", tournament!!._id)
                json.put("Status of tournament", tournament!!.status)
                json.put("Title", tournament!!.title)
                json.put("Regions", tournament!!.getRegionList())
                (activity as MainActivity).mixpanelAPI?.track("RegisterTournamentClick", json)

                if (URLUtil.isValidUrl(it)) {
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.data = Uri.parse(it)
                    startActivity(intent)
                }
            }
        }

        binding.btnInstagram.setOnClickListener {
            val json = JSONObject()
            json.put("Screen", type)
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
            json.put("Screen", type)
            json.put("Social network", "Telegram")
            (activity as MainActivity).mixpanelAPI?.track("SocialButtonClick", json)

            appConfig?.socialLink_Telegram?.let {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse(it)
                startActivity(intent)
            }
        }

        binding.txtCopyLink.setOnClickListener {
            (requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).apply {
                setPrimaryClip(ClipData.newPlainText("", tournament?.link?:""))
                Toast.makeText(requireContext(), requireContext()
                    .getString(R.string.link_copied), Toast.LENGTH_SHORT)
                    .show()
            }
            val json = JSONObject()
            json.put("ObjectID", tournament!!._id)
            json.put("Status of tournament", tournament!!.status)
            json.put("Title", tournament!!.title)
            json.put("Regions", tournament!!.getRegionList())
            (activity as MainActivity).mixpanelAPI?.track("CopyTournamentLinkClick", json)
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
                                json.put("screen", "Detail tournaments")
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
                                json.put("screen", "Detail tournaments")
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