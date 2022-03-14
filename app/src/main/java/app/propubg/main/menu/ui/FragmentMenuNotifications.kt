package app.propubg.main.menu.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.propubg.R
import app.propubg.currentLanguage
import app.propubg.databinding.FragmentMenuNotificationsBinding
import app.propubg.main.MainActivity
import app.propubg.main.menu.adapters.NotificationsAdapter
import app.propubg.main.menu.model.NotificationItem
import app.propubg.main.menu.model.NotificationTitle
import app.propubg.utils.LocalData
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject

class FragmentMenuNotifications: Fragment(), NotificationsAdapter.OnClickListener {

    private lateinit var binding: FragmentMenuNotificationsBinding
    private lateinit var adapter: NotificationsAdapter
    private val notificationTitles by lazy { LocalData.getNotificationTitles(requireContext()) }
    private val fcm by lazy { FirebaseMessaging.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_menu_notifications, container,false)
        binding.lifecycleOwner = viewLifecycleOwner
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
        val topic = LocalData.topics.elementAt(notificationItem.index)
        notificationTitles[menuPosition].items[itemPosition].isChecked = notificationItem.isChecked
        requireContext().getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE).edit()
            .putBoolean(topic,
                notificationItem.isChecked).apply()
        if (notificationItem.isChecked)
            fcm.subscribeToTopic("$topic$currentLanguage")
        else fcm.unsubscribeFromTopic("$topic$currentLanguage")
    }

    override fun onDetach() {
        super.onDetach()
        val json = JSONObject()
        LocalData.topics.forEach { topic ->
            val enabled = requireContext().getSharedPreferences("prefs",
                AppCompatActivity.MODE_PRIVATE)
                .getBoolean(topic, true)
            json.put("$topic$currentLanguage", enabled)
        }
        (activity as MainActivity).mixpanelAPI!!.track("Changed subscription", json)
        Log.v("DASD", json.toString())
    }

    override fun onResume() {
        super.onResume()
        val json = JSONObject()
        json.put("Screen", "Notifications")
        json.put("ObjectID", "No value")
        json.put("Title", "No value")
        json.put("Regions", "No value")
        (activity as MainActivity).mixpanelAPI?.track("ScreenView", json)
    }

}