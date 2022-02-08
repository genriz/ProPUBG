package app.propubg.main.menu.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.propubg.R
import app.propubg.currentUser
import app.propubg.databinding.FragmentMenuBinding
import app.propubg.main.MainActivity
import app.propubg.main.menu.model.MenuViewModel
import org.bson.types.ObjectId

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
        binding.lifecycleOwner = this
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
                                    (activity as MainActivity).openResultsDetails(results)
                                    arguments = null
                                }
                            } else {
                                (activity as MainActivity).openFragmentPartners()
                                val partner = viewModel.getPartnerById(id)
                                partner?.let {
                                    (activity as MainActivity).openPartnerDetails(partner)
                                    arguments = null
                                }
                            }

                        }
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
    }
}