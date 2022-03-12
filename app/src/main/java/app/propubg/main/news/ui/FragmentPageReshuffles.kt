package app.propubg.main.news.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import app.propubg.R
import app.propubg.databinding.FragmentPageReshufflesBinding
import app.propubg.main.MainActivity
import app.propubg.main.news.adapters.ReshufflesAdapter
import app.propubg.main.news.model.NewsViewModel
import app.propubg.main.news.model.reshuffle

class FragmentPageReshuffles: Fragment(), ReshufflesAdapter.OnClick {

    private lateinit var binding: FragmentPageReshufflesBinding
    private val viewModel: NewsViewModel by viewModels()
    private lateinit var adapter: ReshufflesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_page_reshuffles,
            container, false)
        binding.searchReshuffles.viewModel = viewModel
        binding.searchReshuffles.lifecycleOwner = this
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerReshuffles.setHasFixedSize(true)

        viewModel.realmReady.observe(viewLifecycleOwner,{ ready ->
            if (ready){
                adapter = ReshufflesAdapter(viewModel.getReshuffles(), this)
                binding.recyclerReshuffles.adapter = adapter

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
                        if (adapter.itemCount==0)
                            binding.noReshuffles.visibility = View.VISIBLE
                        else binding.noReshuffles.visibility = View.GONE
                    }
                })

                binding.searchReshuffles.searchCancel.setOnClickListener {
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
                            adapter.updateData(viewModel.searchReshuffles(searchString))
                            adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
                                override fun onChanged() {
                                    super.onChanged()
                                    if (adapter.itemCount==0) {
                                        binding.noReshuffles.visibility = View.VISIBLE
                                        binding.noReshuffles.setText(R.string.search_empty)
                                    }
                                    else binding.noReshuffles.visibility = View.GONE
                                }
                            })
                        } else if (searchString.isEmpty()) {
                            adapter.updateData(viewModel.getReshuffles())
                        }
                    }
                })

            }
        })
    }

    override fun onReshufflesClick(reshuffle: reshuffle) {
        (activity as MainActivity).openReshufflesDetails(reshuffle)
    }
}