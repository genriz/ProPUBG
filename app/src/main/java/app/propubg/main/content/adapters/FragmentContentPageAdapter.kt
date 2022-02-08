package app.propubg.main.content.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.propubg.main.content.ui.FragmentContentInterview
import app.propubg.main.content.ui.FragmentContentLearn

class FragmentContentPageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position){
            0 -> {
                FragmentContentLearn()
            }
            else -> {
                FragmentContentInterview()
            }
        }
    }
}