package app.propubg.main.content.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import app.propubg.R
import app.propubg.databinding.FragmentContentBinding
import app.propubg.main.MainActivity
import app.propubg.main.content.adapters.FragmentContentPageAdapter
import app.propubg.main.content.model.ContentViewModel
import com.google.android.material.tabs.TabLayoutMediator

class FragmentContent: Fragment() {

    private lateinit var binding: FragmentContentBinding
    private lateinit var adapter: FragmentContentPageAdapter
    private val viewModel: ContentViewModel by activityViewModels()
    private var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_content, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.header.headerTitle.text = getString(R.string.content)
        binding.header.btnBack.visibility = View.INVISIBLE

        adapter = FragmentContentPageAdapter(this)
        binding.pagerContent.adapter = adapter
        TabLayoutMediator(binding.tabsContent, binding.pagerContent) { tab, position ->
            tab.text = when (position){
                1 -> getString(R.string.content_learn)
                else -> getString(R.string.content_interview)
            }
        }.attach()

        arguments?.let{
            currentPage = it.get("page") as Int
            arguments = null
        }
        binding.pagerContent.setCurrentItem(currentPage,false)

        binding.pagerContent.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
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