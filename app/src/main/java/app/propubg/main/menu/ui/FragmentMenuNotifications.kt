package app.propubg.main.menu.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.propubg.R
import app.propubg.databinding.FragmentMenuNotificationsBinding
import app.propubg.main.MainActivity
import app.propubg.main.menu.adapters.NotificationsAdapter
import app.propubg.main.menu.model.NotificationItem
import app.propubg.main.menu.model.NotificationTitle
import app.propubg.utils.LocalData

class FragmentMenuNotifications: Fragment(), NotificationsAdapter.OnClickListener {

    private lateinit var binding: FragmentMenuNotificationsBinding
    private lateinit var adapter: NotificationsAdapter
    private val notificationTitles by lazy { LocalData.getNotificationTitles(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_menu_notifications, container,false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.header.headerTitle.text = getString(R.string.notifications)

        adapter = NotificationsAdapter(this)
        adapter.submitList(notificationTitles)
        binding.notificationSubtitleRecycler.adapter = adapter

        binding.header.btnBack.setOnClickListener {
            (activity as MainActivity).onBackPressed()
            notificationTitles.forEach {
                it.collapsed = true
            }
        }

    }

    override fun onTitleClick(notificationTitle: NotificationTitle) {
        notificationTitle.collapsed = !notificationTitle.collapsed
        adapter.notifyItemChanged(adapter.currentList.indexOf(notificationTitle))
        if (!notificationTitle.collapsed)
            binding.notificationSubtitleRecycler
                .scrollToPosition(notificationTitles.indexOf(notificationTitle))
    }

    override fun onItemClick(menuPosition:Int, notificationItem: NotificationItem) {
        val itemPosition = notificationTitles[menuPosition].items.indexOf(notificationItem)
        notificationTitles[menuPosition].items[itemPosition].isChecked = notificationItem.isChecked
    }

}