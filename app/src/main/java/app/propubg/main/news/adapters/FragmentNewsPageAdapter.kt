package app.propubg.main.news.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.propubg.main.news.ui.FragmentPageNews
import app.propubg.main.news.ui.FragmentPageReshuffles

class FragmentNewsPageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position){
            0 -> {
                FragmentPageReshuffles()
            }
            else -> {
                FragmentPageNews()
            }
        }
    }
}