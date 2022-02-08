package app.propubg.main.tournaments.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.propubg.main.tournaments.ui.FragmentTournamentsClosed
import app.propubg.main.tournaments.ui.FragmentTournamentsOpen
import app.propubg.main.tournaments.ui.FragmentTournamentsUpcoming

class FragmentTournamentsPageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position){
            0 -> {
                FragmentTournamentsClosed()
            }
            1 -> {
                FragmentTournamentsOpen()
            }
            else -> {
                FragmentTournamentsUpcoming()
            }
        }
    }
}