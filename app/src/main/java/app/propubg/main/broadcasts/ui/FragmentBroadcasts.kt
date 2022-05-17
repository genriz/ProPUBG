package app.propubg.main.broadcasts.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import app.propubg.R
import app.propubg.databinding.FragmentBroadcastsBinding
import app.propubg.main.MainActivity
import app.propubg.main.broadcasts.adapters.FragmentBroadcastsPageAdapter
import app.propubg.main.broadcasts.model.BroadcastsViewModel
import com.google.android.material.tabs.TabLayoutMediator

class FragmentBroadcasts: Fragment() {

    private lateinit var binding: FragmentBroadcastsBinding
    private lateinit var adapter: FragmentBroadcastsPageAdapter
    private val viewModel: BroadcastsViewModel by activityViewModels()
    private var currentPage = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_broadcasts, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            (activity as MainActivity).showSheetInfo()
        }
    }
}