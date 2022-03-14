package app.propubg.main.tournaments.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.viewpager2.widget.ViewPager2
import app.propubg.R
import app.propubg.currentLanguage
import app.propubg.databinding.FragmentTournamentsBinding
import app.propubg.main.MainActivity
import app.propubg.main.advert.Advert
import app.propubg.main.advert.AdvertViewModel
import app.propubg.main.tournaments.adapters.FragmentTournamentsPageAdapter
import app.propubg.main.tournaments.model.TournamentsViewModel
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import org.bson.types.ObjectId
import org.json.JSONObject

class FragmentTournaments: Fragment() {

    private lateinit var binding: FragmentTournamentsBinding
    private lateinit var adapter: FragmentTournamentsPageAdapter
    private val viewModel: TournamentsViewModel by activityViewModels()
    private val advertViewModel: AdvertViewModel by viewModels()
    private var currentPage = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tournaments, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.advertMain.isVisible = false
        binding.header.headerTitle.text = getString(R.string.tournaments)
        binding.header.btnBack.visibility = View.INVISIBLE

        viewModel.realmReady.observe(viewLifecycleOwner,{ ready ->
            if (ready){
                adapter = FragmentTournamentsPageAdapter(this)
                binding.pagerTournaments.adapter = adapter
                TabLayoutMediator(binding.tabsTournaments, binding.pagerTournaments) { tab, position ->
                    tab.text = when (position){
                        0 -> getString(R.string.tournaments_closed)
                        1 -> getString(R.string.tournaments_open)
                        else -> getString(R.string.tournaments_upcoming)
                    }
                }.attach()
                binding.pagerTournaments.offscreenPageLimit = 2

                arguments?.let{
                    currentPage = requireArguments().getInt("page")
                    val id = requireArguments()
                        .getSerializable("tournamentId") as ObjectId?
                    id?.let{
                        val tournament = viewModel.getTournamentById(id)
                        tournament?.let {
                            val type = when (currentPage){
                                0 -> "ClosedDetails"
                                1 -> "OpenDetails"
                                else -> "UpcomingDetails"
                            }
                            (activity as MainActivity).openTournamentDetails(it, type)
                            arguments = null
                        }
                        if (tournament==null) Toast.makeText(requireContext(), "wrong ID",
                            Toast.LENGTH_LONG).show()
                    }
                }

                binding.pagerTournaments.setCurrentItem(currentPage, false)

                binding.pagerTournaments.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        if (currentPage!=position) {
                            currentPage = position
                            viewModel.searchString.value = ""
                        }
                    }
                })
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
                                    json.put("screen", "Tournaments")
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