package app.propubg.main.news.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import app.propubg.R
import app.propubg.databinding.FragmentNewsBinding
import app.propubg.main.MainActivity
import app.propubg.main.news.adapters.FragmentNewsPageAdapter
import app.propubg.main.news.model.NewsViewModel
import com.google.android.material.tabs.TabLayoutMediator
import org.bson.types.ObjectId

class FragmentNews: Fragment() {

    private lateinit var binding: FragmentNewsBinding
    private lateinit var adapter: FragmentNewsPageAdapter
    private val viewModel: NewsViewModel by activityViewModels()
    private var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_news, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.header.headerTitle.text = getString(R.string.news)
        binding.header.btnBack.visibility = View.INVISIBLE

        viewModel.realmReady.observe(viewLifecycleOwner,{
            if (it){
                arguments?.let{
                    currentPage = requireArguments().getInt("page")
                    val id = if (currentPage==1) requireArguments()
                        .getSerializable("newsId") as ObjectId?
                    else requireArguments()
                        .getSerializable("reshuffleId") as ObjectId?
                    id?.let{
                        if (currentPage==1){
                            val news = viewModel.getNewsById(id)
                            news?.let {
                                (activity as MainActivity).openNewsDetails(news)
                                arguments = null
                            }
                        } else {
                            val reshuffle = viewModel.getReshuffleById(id)
                            reshuffle?.let {
                                (activity as MainActivity).openReshufflesDetails(reshuffle)
                                arguments = null
                            }
                        }

                    }
                }

                adapter = FragmentNewsPageAdapter(this)
                binding.pagerNews.adapter = adapter
                TabLayoutMediator(binding.tabsNews, binding.pagerNews) { tab, position ->
                    tab.text = when (position){
                        0 -> getString(R.string.reshuffles)
                        else -> getString(R.string.global)
                    }
                }.attach()
                binding.pagerNews.offscreenPageLimit = 2

                binding.pagerNews.setCurrentItem(currentPage, false)

                binding.pagerNews.registerOnPageChangeCallback(object:ViewPager2.OnPageChangeCallback(){
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
        })


        viewModel.reset.observe(viewLifecycleOwner,{
            if (it) Toast.makeText(requireContext(), "Client reset", Toast.LENGTH_SHORT).show()
        })
    }

    fun setPage(i: Int) {
        currentPage = i
        binding.pagerNews.setCurrentItem(currentPage, false)
    }
}