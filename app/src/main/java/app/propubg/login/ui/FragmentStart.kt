package app.propubg.login.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.propubg.R
import app.propubg.currentTimer
import app.propubg.databinding.FragmentStartBinding
import app.propubg.login.model.StartViewModel
import app.propubg.prevPhone

class FragmentStart: Fragment() {

    private lateinit var binding: FragmentStartBinding
    private val viewModel: StartViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_start, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGetCode.setOnClickListener {
            if (binding.inputPhoneText.text!!.length>1){
                viewModel.error.value = ""
                viewModel.phone = binding.inputPhoneText.text!!.toString()
                prevPhone?.let{
                    viewModel.isPhoneNew = prevPhone != viewModel.phone
                }
                prevPhone = viewModel.phone
                requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    .edit().putString("phone", prevPhone).apply()
                if (viewModel.isPhoneNew) {
                    currentTimer = 60
                    (activity as StartActivity).verifyNumber()
                } else {
                    if (!viewModel.timerStarted&&viewModel.resendEnabled) {
                        currentTimer+=300
                        (activity as StartActivity).verifyNumber()
                    } else if (viewModel.accessError)
                        (activity as StartActivity).verifyNumber()
                    else (activity as StartActivity).openSmsFragment()
                }
            } else {
                viewModel.error.value = getString(R.string.phone_empty)
            }
        }

        viewModel.error.observe(viewLifecycleOwner,{
            it?.let{
                binding.error.text = it
            }
        })
    }

}