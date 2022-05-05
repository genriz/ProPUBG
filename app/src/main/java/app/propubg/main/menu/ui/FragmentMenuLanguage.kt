package app.propubg.main.menu.ui

import android.app.Activity
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.propubg.R
import app.propubg.databinding.FragmentMenuLanguageBinding
import app.propubg.main.MainActivity
import app.propubg.main.menu.model.MenuViewModel
import app.propubg.utils.AppUtils
import org.json.JSONObject
import java.util.*

class FragmentMenuLanguage: Fragment() {

    private lateinit var binding: FragmentMenuLanguageBinding
    private val viewModel by viewModels<MenuViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu_language,
            container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.header.headerTitle.text = getString(R.string.language)
        binding.executePendingBindings()

        binding.langEnglish.setOnClickListener {
            viewModel.setEnglish()
            requireContext().getSharedPreferences("prefs", Activity.MODE_PRIVATE)
                .edit().putString("language", "en").apply()
            val resources: Resources = resources
            val dm: DisplayMetrics = resources.displayMetrics
            val config: Configuration = resources.configuration
            config.setLocale(Locale("en"))
            resources.updateConfiguration(config, dm)
            (activity as MainActivity).recreate()
            (activity as MainActivity).navController.navigateUp()
            (activity as MainActivity).navController.navigate(R.id.fragmentMenuLanguage)
            AppUtils().resubscribeTopicsFCM(requireContext(), "ru", "en")
        }

        binding.langRussian.setOnClickListener {
            viewModel.setRussian()
            requireContext().getSharedPreferences("prefs", Activity.MODE_PRIVATE)
                .edit().putString("language", "ru").apply()
            val resources: Resources = resources
            val dm: DisplayMetrics = resources.displayMetrics
            val config: Configuration = resources.configuration
            config.setLocale(Locale("ru"))
            resources.updateConfiguration(config, dm)
            (activity as MainActivity).recreate()
            (activity as MainActivity).navController.navigateUp()
            (activity as MainActivity).navController.navigate(R.id.fragmentMenuLanguage)
            AppUtils().resubscribeTopicsFCM(requireContext(), "en", "ru")
        }

        binding.header.btnBack.setOnClickListener {
            (activity as MainActivity).onBackPressed()
        }

        binding.header.btnOption.setOnClickListener {
            (activity as MainActivity).showSheetInfo()
        }

    }

    override fun onResume() {
        super.onResume()
        val json = JSONObject()
        json.put("Screen", "Language")
        json.put("ObjectID", "No value")
        json.put("Title", "No value")
        json.put("Regions", "No value")
        (activity as MainActivity).mixpanelAPI?.track("ScreenView", json)
    }
}