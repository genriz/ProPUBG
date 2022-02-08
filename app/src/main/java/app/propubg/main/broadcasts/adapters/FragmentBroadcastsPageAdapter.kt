package app.propubg.main.broadcasts.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.propubg.main.broadcasts.ui.FragmentBroadcastsLive
import app.propubg.main.broadcasts.ui.FragmentBroadcastsPast
import app.propubg.main.broadcasts.ui.FragmentBroadcastsUpcoming

class FragmentBroadcastsPageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position){
            0 -> {
                FragmentBroadcastsPast()
            }
            1 -> {
                FragmentBroadcastsLive()
            }
            else -> {
                FragmentBroadcastsUpcoming()
            }
        }
    }
}