package app.propubg.main.news.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import app.propubg.R
import app.propubg.databinding.FragmentPageNewsBinding
import app.propubg.main.MainActivity
import app.propubg.main.news.adapters.NewsAdapter
import app.propubg.main.news.model.NewsViewModel
import app.propubg.main.news.model.news
import app.propubg.realmApp
import org.json.JSONObject

class FragmentPageNews: Fragment(), NewsAdapter.OnClick {

    private lateinit var binding: FragmentPageNewsBinding
    private val viewModel: NewsViewModel by viewModels()
    private lateinit var adapter: NewsAdapter
    private var isSearching = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_page_news,
            container, false)
        binding.searchNews.viewModel = viewModel
        binding.searchNews.lifecycleOwner = viewLifecycleOwner
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerNews.setHasFixedSize(true)

        viewModel.realmReady.observe(viewLifecycleOwner,{ ready ->
            if (ready){
                adapter = NewsAdapter(viewModel.getNews(), this)
                binding.recyclerNews.adapter = adapter

                adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        super.onItemRangeInserted(positionStart, itemCount)
                        adapter.notifyItemChanged(positionStart)
                        adapter.notifyItemChanged(positionStart+1)
                    }

                    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                        super.onItemRangeRemoved(positionStart, itemCount)
                        adapter.notifyItemChanged(positionStart-1)
                        adapter.notifyItemChanged(positionStart+1)
                    }

                    override fun onChanged() {
                        super.onChanged()
                        if (adapter.itemCount==0) {
                            binding.noNews.visibility = View.VISIBLE
                            if (isSearching)
                                binding.noNews.setText(R.string.search_empty)
                            else
                                binding.noNews.setText(R.string.no_reshuffles)
                        }
                        else binding.noNews.visibility = View.GONE
                    }
                })

                binding.searchNews.searchCancel.setOnClickListener {
                    viewModel.searchString.value = ""
                    binding.expandLayout.setExpanded(false)
                    requireActivity().currentFocus?.let { focus ->
                        val inputManager: InputMethodManager =
                            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputManager.hideSoftInputFromWindow(
                            focus.windowToken,
                            InputMethodManager.HIDE_NOT_ALWAYS)
                    }
                }

                viewModel.searchString.observe(viewLifecycleOwner,{
                    it?.let{ searchString ->
                        if (searchString.length>1){
                            isSearching = true
                            binding.expandLayout.setExpanded(true)
                            adapter.updateData(viewModel.searchNews(searchString))
                        } else if (searchString.isEmpty()) {
                            isSearching = false
                            adapter.updateData(viewModel.getNews())
                        }
                    }
                })
            }
        })

    }

    override fun onNewsClick(news: news, imageView: ImageView) {
        (activity as MainActivity).openNewsDetails(news)
        realmApp.currentUser()?.let{user ->
            if (!news.listViewers.contains(user.id)){
                viewModel.updateNews(news, user.id)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val json = JSONObject()
        json.put("Screen", "News[Others]")
        json.put("ObjectID", "No value")
        json.put("Title", "No value")
        json.put("Regions", "No value")
        (activity as MainActivity).mixpanelAPI?.track("ScreenView", json)
    }
}