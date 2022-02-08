package app.propubg.main.menu.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.propubg.R
import app.propubg.databinding.FragmentMenuPasswordChangeBinding
import app.propubg.main.MainActivity

class FragmentMenuPasswordChange: Fragment() {

    private lateinit var binding: FragmentMenuPasswordChangeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu_password_change,
            container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.header.headerTitle.text = getString(R.string.password_changing)

        binding.header.btnBack.setOnClickListener {
            (activity as MainActivity).onBackPressed()
        }
    }
}