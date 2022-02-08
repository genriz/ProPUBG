package app.propubg.main.menu.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.util.Linkify
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
import androidx.lifecycle.ViewModelProvider
import app.propubg.R
import app.propubg.currentLanguage
import app.propubg.databinding.FragmentMenuPartnerDetailsBinding
import app.propubg.main.MainActivity
import app.propubg.main.advert.Advert
import app.propubg.main.advert.AdvertViewModel
import app.propubg.main.menu.model.MenuViewModel
import app.propubg.main.menu.model.PartnerItem
import app.propubg.main.menu.model.partner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import org.bson.types.ObjectId
import org.json.JSONObject


class FragmentMenuPartnersDetails: Fragment() {

    private lateinit var binding: FragmentMenuPartnerDetailsBinding
    private var partner: partner? = null
    private val viewModel: MenuViewModel by viewModels()
    private val advertViewModel: AdvertViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu_partner_details,
            container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.advertMain.isVisible = false

        viewModel.realmReady.observe(viewLifecycleOwner,{
            if (it==true){
                partner = viewModel.getPartnerById(
                    requireArguments()
                        .getSerializable("partnerId") as ObjectId)

                val partnerItem = PartnerItem()
                partnerItem.partner = partner
                binding.partnerItem = partnerItem
                binding.executePendingBindings()

                binding.headerDetails.headerTitle.text = partner?.title

                if (partner!!.imageSrc!="") {
                    binding.itemWait.postDelayed({
                        Glide.with(binding.itemWait).asGif().load(R.drawable.wait)
                            .into(binding.itemWait)
                    }, 200)

                    Glide.with(binding.partnerImage).load(partner!!.imageSrc)
                        .addListener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                binding.itemWait.visibility = View.GONE
                                return false
                            }
                        }).signature(ObjectKey(partner!!.imageSrc!!))
                        .into(binding.partnerImage)
                } else {
                    Glide.with(binding.partnerImage).load(R.drawable.app_logo)
                        .into(binding.partnerImage)
                }

                Linkify.addLinks(binding.partnerText, Linkify.ALL)
            }
        })

        binding.headerDetails.btnBack.setOnClickListener {
            (activity as MainActivity).closeFragment()
        }

        binding.btnOpenServer.setOnClickListener {
            partner?.link?.let{
                if (URLUtil.isValidUrl(it)) {
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.data = Uri.parse(it)
                    startActivity(intent)
                }
            }
        }

        binding.txtCopyLink.setOnClickListener {
            (requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).apply {
                setPrimaryClip(ClipData.newPlainText("", partner?.link?:""))
                Toast.makeText(requireContext(), requireContext()
                    .getString(R.string.link_copied), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.headerDetails.btnOption.setImageResource(R.drawable.ic_share)
        binding.headerDetails.btnOption.setOnClickListener {
            partner?.let { partner_ ->
                Firebase.dynamicLinks.createDynamicLink()
                    .setDomainUriPrefix("https://link.propubg.app")
                    .setLink(Uri.parse("https://link.propubg.app/?Partner=${partner_._id}"))
                    .setSocialMetaTagParameters(DynamicLink.SocialMetaTagParameters.Builder()
                        .setImageUrl(Uri.parse(partner_.imageSrc?:""))
                        .setTitle(partner_.title?:"")
                        .setDescription(partner_.descriptionOfPartner?:"").build())
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
                                json.put("screen", "Detail discord partners")
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