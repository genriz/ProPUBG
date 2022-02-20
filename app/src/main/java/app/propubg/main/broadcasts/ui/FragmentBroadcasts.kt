package app.propubg.main.broadcasts.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.viewpager2.widget.ViewPager2
import app.propubg.R
import app.propubg.currentLanguage
import app.propubg.databinding.FragmentBroadcastsBinding
import app.propubg.main.MainActivity
import app.propubg.main.advert.Advert
import app.propubg.main.advert.AdvertViewModel
import app.propubg.main.broadcasts.adapters.FragmentBroadcastsPageAdapter
import app.propubg.main.broadcasts.model.BroadcastsViewModel
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import org.json.JSONObject

class FragmentBroadcasts: Fragment() {

    private lateinit var binding: FragmentBroadcastsBinding
    private lateinit var adapter: FragmentBroadcastsPageAdapter
    private val viewModel: BroadcastsViewModel by activityViewModels()
    private val advertViewModel: AdvertViewModel by viewModels()
    private var currentPage = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_broadcasts, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.advertMain.isVisible = false
        binding.header.headerTitle.text = getString(R.string.broadcasts)
        binding.header.btnBack.visibility = View.INVISIBLE

        adapter = FragmentBroadcastsPageAdapter(this)
        binding.pagerBroadcasts.adapter = adapter
        TabLayoutMediator(binding.tabsBroadcasts, binding.pagerBroadcasts) { tab, position ->
            tab.text = when (position){
                0 -> getString(R.string.broadcasts_past)
                1 -> getString(R.string.broadcasts_live)
                else -> getString(R.string.broadcasts_upcoming)
            }
        }.attach()

        arguments?.let{
            currentPage = it.get("page") as Int
        }

        binding.pagerBroadcasts.setCurrentItem(currentPage, false)

        binding.pagerBroadcasts.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (currentPage!=position) {
                    currentPage = position
                    viewModel.searchString.value = ""
                }
            }
        })

        binding.header.btnOption.setOnClickListener {

        }

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
                                    binding.advertMain.isVisible = false
                                    viewModel.advertClosed = true
                                }
                            binding.advertMain
                                .findViewById<ImageView>(R.id.advertImage)
                                .setOnClickListener {
                                    val json = JSONObject()
                                    json.put("campaign", advertisement.campaign)
                                    json.put("screen", "Broadcasts")
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
}