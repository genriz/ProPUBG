package app.propubg.main.menu.ui

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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import app.propubg.R
import app.propubg.currentLanguage
import app.propubg.databinding.FragmentMenuPartnersBinding
import app.propubg.main.MainActivity
import app.propubg.main.advert.Advert
import app.propubg.main.advert.AdvertViewModel
import app.propubg.main.menu.adapters.PartnersAdapter
import app.propubg.main.menu.model.MenuViewModel
import app.propubg.main.menu.model.partner
import com.bumptech.glide.Glide
import org.json.JSONObject

class FragmentMenuPartners:Fragment(), PartnersAdapter.OnClick {

    private lateinit var binding: FragmentMenuPartnersBinding
    private val viewModel: MenuViewModel by activityViewModels()
    private lateinit var adapter: PartnersAdapter
    private val advertViewModel: AdvertViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu_partners,
            container, false)
        binding.searchPartners.viewModel = viewModel
        binding.searchPartners.lifecycleOwner = this
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.advertMain.isVisible = false
        binding.header.headerTitle.text = getString(R.string.partners)
        binding.header.btnBack.setOnClickListener {
            (activity as MainActivity).onBackPressed()
        }

        viewModel.realmReady.observe(viewLifecycleOwner,{
            it?.let { ready ->
                if (ready){
                    adapter = PartnersAdapter(viewModel.getPartners(), this)
                    binding.recyclerPartners.setHasFixedSize(true)
                    binding.recyclerPartners.adapter = adapter
                }
            }
        })

        binding.searchPartners.searchCancel.setOnClickListener {
            binding.expandLayout.setExpanded(false)
            viewModel.searchString.value = ""
            requireActivity().currentFocus?.let { focus ->
                val inputManager: InputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE)
                            as InputMethodManager
                inputManager.hideSoftInputFromWindow(
                    focus.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }

        viewModel.searchString.observe(viewLifecycleOwner,{
            it?.let{ searchString ->
                if (searchString.length>1){
                    adapter.updateData(viewModel.searchPartners(searchString))
                } else if (viewModel.realmReady.value == true&&searchString.isEmpty()) {
                    adapter.updateData(viewModel.getPartners())
                }
            }
        })

        if (!viewModel.partnersAdvertClosed) {
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
                                    viewModel.partnersAdvertClosed = true
                                }
                            binding.advertMain
                                .findViewById<ImageView>(R.id.advertImage)
                                .setOnClickListener {
                                    val json = JSONObject()
                                    json.put("campaign", advertisement.campaign)
                                    json.put("screen", "Discord partners")
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

    override fun onPartnerClick(partner: partner) {
        (activity as MainActivity).openPartnerDetails(partner)
    }

    override fun isEmpty(isEmpty: Boolean) {

    }
}