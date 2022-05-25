package app.propubg.main.menu.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.propubg.R
import app.propubg.currentLanguage
import app.propubg.currentUser
import app.propubg.databinding.FragmentMenuBinding
import app.propubg.main.MainActivity
import app.propubg.main.menu.model.MenuViewModel
import org.bson.types.ObjectId
import org.json.JSONObject

class FragmentMenu: Fragment() {

    private lateinit var binding: FragmentMenuBinding
    private val viewModel by lazy { ViewModelProvider(requireParentFragment())[MenuViewModel::class.java] }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu,
            container, false)
        binding.user = currentUser
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.realmReady.observe(viewLifecycleOwner,{
            it?.let { ready ->
                if (ready){
                    arguments?.let{
                        val menu = requireArguments().getInt("menu")
                        val id = if (menu==0) requireArguments()
                            .getSerializable("resultsId") as ObjectId?
                        else requireArguments()
                            .getSerializable("partnerId") as ObjectId?
                        id?.let{
                            if (menu==0){
                                (activity as MainActivity).openFragmentResults()
                                val results = viewModel.getResultsById(id)
                                results?.let {
                                    val resultsCorrect = if (currentLanguage=="ru"){
                                        results.title!=null&&results.stage_ru!=null
                                                &&results.author!=null
                                                &&results.dayOfTournament!=null
                                    } else {
                                        results.title!=null&&results.stage_en!=null
                                                &&results.author!=null
                                                &&results.dayOfTournament!=null
                                    }
                                    if (resultsCorrect)
                                        (activity as MainActivity).openResultsDetails(results)
                                    arguments = null
                                    val json = JSONObject()
                                    json.put("ObjectID", id.toString())
                                    json.put("Type", "ResultsOfTournament")
                                    json.put("Title", results.title)
                                    json.put("Regions", results.getRegionList())
                                    (activity as MainActivity)
                                        .mixpanelAPI?.track("DeepLinkOpened", json)
                                }
                            } else {
                                (activity as MainActivity).openFragmentPartners()
                                val partner = viewModel.getPartnerById(id)
                                partner?.let {
                                    val partnerCorrect = if (currentLanguage=="ru"){
                                        partner.title!=null&&partner.text_ru!=null
                                                &&partner.descriptionOfPartner_ru!=null
                                                &&partner.link!=null
                                    } else {
                                        partner.title!=null&&partner.text_en!=null
                                                &&partner.descriptionOfPartner_en!=null
                                                &&partner.link!=null
                                    }
                                    if (partnerCorrect)
                                        (activity as MainActivity).openPartnerDetails(partner)
                                    arguments = null
                                    val json = JSONObject()
                                    json.put("ObjectID", id.toString())
                                    json.put("Type", "DiscordPartner")
                                    json.put("Title", partner.title)
                                    json.put("Regions", partner.getRegionList())
                                    (activity as MainActivity)
                                        .mixpanelAPI?.track("DeepLinkOpened", json)
                                }
                            }
                        }
                        if (id==null) {
                            if (menu==0)
                                (activity as MainActivity).openFragmentResults()
                            else
                                (activity as MainActivity).openFragmentPartners()
                        }
                        arguments = null
                    }
                }
            }
        })

        binding.header.btnEdit.setOnClickListener {
            (activity as MainActivity).openFragmentProfileEdit()
        }

        binding.menuPassword.setOnClickListener {
            (activity as MainActivity).openFragmentPassword()
        }

        binding.menuNotifications.setOnClickListener {
            (activity as MainActivity).openFragmentNotifications()
        }

        binding.menuLanguage.setOnClickListener {
            (activity as MainActivity).openFragmentLanguage()
        }

        binding.menuResults.setOnClickListener {
            (activity as MainActivity).openFragmentResults()
        }

        binding.menuPartners.setOnClickListener {
            (activity as MainActivity).openFragmentPartners()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.user = currentUser
        binding.executePendingBindings()

        val json = JSONObject()
        json.put("Screen", "Menu")
        json.put("ObjectID", "No value")
        json.put("Title", "No value")
        json.put("Regions", "No value")
        (activity as MainActivity).mixpanelAPI?.track("ScreenView", json)
    }
}