package app.propubg.main.content.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import app.propubg.R
import app.propubg.currentLanguage
import app.propubg.databinding.FragmentPageContentBinding
import app.propubg.main.MainActivity
import app.propubg.main.content.adapters.ContentAdapter
import app.propubg.main.content.model.ContentViewModel
import app.propubg.main.content.model.content
import org.json.JSONObject

class FragmentContentLearn: Fragment(), ContentAdapter.OnClick {

    private lateinit var binding: FragmentPageContentBinding
    private val viewModel: ContentViewModel by viewModels()
    private lateinit var adapter: ContentAdapter
    private var isSearching = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_page_content,
            container, false)
        binding.searchContent.viewModel = viewModel
        binding.searchContent.lifecycleOwner = viewLifecycleOwner
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.realmReady.observe(viewLifecycleOwner,{
            it?.let{ ready ->
                if (ready){
                    binding.recyclerContent.setHasFixedSize(true)
                    adapter = ContentAdapter(viewModel.getContentInformative(), this)
                    binding.recyclerContent.adapter = adapter

                    adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
                        override fun onChanged() {
                            super.onChanged()
                            if (adapter.itemCount==0) {
                                binding.noContent.visibility = View.VISIBLE
                                if (isSearching)
                                    binding.noContent.setText(R.string.search_empty)
                                else
                                    binding.noContent.setText(R.string.no_content_learn)
                            }
                            else binding.noContent.visibility = View.GONE
                        }
                    })
                }
            }
        })

        binding.searchContent.searchCancel.setOnClickListener {
            binding.expandLayout.setExpanded(false)
            viewModel.searchString.value = ""
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
                    adapter.updateData(viewModel.searchContentInformative(searchString))
                } else if (viewModel.realmReady.value == true&&searchString.isEmpty()) {
                    isSearching = false
                    adapter.updateData(viewModel.getContentInformative())
                }
            }
        })

    }

    override fun onWatchClick(content: content) {
        content.link?.let{
            val title = if (currentLanguage =="ru") content.title_ru
            else content.title_en
            val json = JSONObject()
            json.put("ObjectID", content._id)
            json.put("Type of content", "Informative")
            json.put("Title", title)
            (activity as MainActivity).mixpanelAPI?.track("WatchContentClick", json)

            if (URLUtil.isValidUrl(it)) {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse(it)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val json = JSONObject()
        json.put("Screen", "Content[Informative]")
        json.put("ObjectID", "No value")
        json.put("Title", "No value")
        json.put("Regions", "No value")
        (activity as MainActivity).mixpanelAPI?.track("ScreenView", json)
    }

}