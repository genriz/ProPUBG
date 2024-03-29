package app.propubg.login.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.propubg.BuildConfig
import app.propubg.R
import app.propubg.databinding.FragmentSmsCodeBinding
import app.propubg.login.model.StartViewModel
import com.google.firebase.auth.PhoneAuthProvider
import com.mixpanel.android.mpmetrics.MixpanelAPI
import java.text.SimpleDateFormat
import java.util.*

class FragmentSmsCode: Fragment() {

    private lateinit var binding: FragmentSmsCodeBinding
    private val viewModel: StartViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sms_code, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtPhone.text = viewModel.phone
        binding.btnConfirmCode.isEnabled = false

        viewModel.timer.observe(viewLifecycleOwner) {
            it?.let { time ->
                if (time > 1000) {
                    binding.smsInfo.text = requireContext().getString(R.string.code_repeat)
                    binding.smsInfo.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.text_gray1
                        )
                    )
                    binding.smsInfo.setOnClickListener {}
                    binding.timer.text = SimpleDateFormat(
                        "mm:ss",
                        Locale.getDefault()
                    ).format(time)
                } else if (time > -1) {
                    binding.timer.text = ""
                    binding.smsInfo.text = requireContext().getString(R.string.code_not_received)
                    binding.smsInfo.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.orange
                        )
                    )
                    binding.smsInfo.setOnClickListener {
                        (activity as StartActivity)
                            .verifyNumber()
                    }
                }
            }
        }

        viewModel.code.observe(viewLifecycleOwner) {
            it?.let {
                if (it == "") {
                    clearCode()
                    binding.btnConfirmCode.isEnabled = false
                } else {
                    setCode(it)
                    binding.btnConfirmCode.isEnabled = true
                    binding.btnConfirmCode.setOnClickListener {
                        checkCode()
                    }
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) {
            it?.let {
                binding.error.text = it
            }
        }

        binding.codeEdit1.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.length==1) {
                    binding.codeEdit1.isSelected = true
                    binding.codeEdit2.requestFocus()
                }
                if (p0.isEmpty()) {
                    binding.btnConfirmCode.isEnabled = false
                }
            }
        })

        binding.codeEdit2.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.length==1) {
                    binding.codeEdit2.isSelected = true
                    binding.codeEdit3.requestFocus()
                }
                if (p0.isEmpty()) {
                    binding.codeEdit2.isSelected = false
                    binding.btnConfirmCode.isEnabled = false
                }
            }
        })

        binding.codeEdit3.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.length==1) {
                    binding.codeEdit3.isSelected = true
                    binding.codeEdit4.requestFocus()
                }
                if (p0.isEmpty()) {
                    binding.codeEdit3.isSelected = false
                    binding.btnConfirmCode.isEnabled = false
                }
            }
        })

        binding.codeEdit4.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.length==1) {
                    binding.codeEdit4.isSelected = true
                    binding.codeEdit5.requestFocus()
                }
                if (p0.isEmpty()) {
                    binding.codeEdit4.isSelected = false
                    binding.btnConfirmCode.isEnabled = false
                }
            }
        })

        binding.codeEdit5.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.length==1) {
                    binding.codeEdit5.isSelected = true
                    binding.codeEdit6.requestFocus()
                }
                if (p0.isEmpty()) {
                    binding.codeEdit5.isSelected = false
                    binding.btnConfirmCode.isEnabled = false
                }
            }
        })

        binding.codeEdit6.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.length==1) {
                    binding.codeEdit6.isSelected = true
                    binding.codeEdit6.clearFocus()
                    val inputMethodManager = ContextCompat
                        .getSystemService(requireContext(),
                            InputMethodManager::class.java)!!
                    inputMethodManager.hideSoftInputFromWindow(binding.codeEdit6.windowToken, 0)
                    binding.btnConfirmCode.isEnabled = true
                }
                if (p0.isEmpty()) {
                    binding.codeEdit6.isSelected = false
                    binding.btnConfirmCode.isEnabled = false
                }
            }
        })


        binding.codeEdit2.setOnKeyListener { _, key, event ->
            if(key == KeyEvent.KEYCODE_DEL&&event.action== KeyEvent.ACTION_UP) {
                binding.codeEdit1.text.clear()
                binding.codeEdit2.isSelected = false
                binding.codeEdit1.isSelected = false
                binding.codeEdit1.requestFocus()
            } else {
                if (binding.codeEdit1.text.isEmpty()){
                    binding.codeEdit1.requestFocus()
                }
            }
            false
        }
        binding.codeEdit3.setOnKeyListener { _, key, event ->
            if(key == KeyEvent.KEYCODE_DEL&&event.action== KeyEvent.ACTION_UP) {
                binding.codeEdit2.text.clear()
                binding.codeEdit3.isSelected = false
                binding.codeEdit2.isSelected = false
                binding.codeEdit2.requestFocus()
            }
            false
        }
        binding.codeEdit4.setOnKeyListener { _, key, event ->
            if(key == KeyEvent.KEYCODE_DEL&&event.action== KeyEvent.ACTION_UP) {
                binding.codeEdit3.text.clear()
                binding.codeEdit4.isSelected = false
                binding.codeEdit3.isSelected = false
                binding.codeEdit3.requestFocus()
            }
            false
        }
        binding.codeEdit5.setOnKeyListener { _, key, event ->
            if(key == KeyEvent.KEYCODE_DEL&&event.action== KeyEvent.ACTION_UP) {
                binding.codeEdit4.text.clear()
                binding.codeEdit5.isSelected = false
                binding.codeEdit4.isSelected = false
                binding.codeEdit4.requestFocus()
            }
            false
        }
        binding.codeEdit6.setOnKeyListener { _, key, event ->
            if(key == KeyEvent.KEYCODE_DEL&&event.action== KeyEvent.ACTION_UP) {
                binding.codeEdit5.text.clear()
                binding.codeEdit6.isSelected = false
                binding.codeEdit5.isSelected = false
                binding.codeEdit5.requestFocus()
            }
            false
        }


        binding.codeEdit1.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                for (i in 1..5) {
                    if ((binding.inputCode.getChildAt(i) as EditText).text.isNotEmpty()) {
                        binding.inputCode.getChildAt(i).requestFocus()
                    }
                }
            }
        }
        binding.codeEdit2.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus&&binding.codeEdit1.text.isEmpty()) {
                binding.codeEdit1.requestFocus()
            }
            if (hasFocus) {
                for (i in 2..5) {
                    if ((binding.inputCode.getChildAt(i) as EditText).text.isNotEmpty()) {
                        binding.inputCode.getChildAt(i).requestFocus()
                    }
                }
            }
        }
        binding.codeEdit3.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                for (i in 1 downTo 0) {
                    if ((binding.inputCode.getChildAt(i) as EditText).text.isEmpty()) {
                        binding.inputCode.getChildAt(i).requestFocus()
                    }
                }
                for (i in 3..5) {
                    if ((binding.inputCode.getChildAt(i) as EditText).text.isNotEmpty()) {
                        binding.inputCode.getChildAt(i).requestFocus()
                    }
                }
            }
        }
        binding.codeEdit4.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                for (i in 2 downTo 0) {
                    if ((binding.inputCode.getChildAt(i) as EditText).text.isEmpty()) {
                        binding.inputCode.getChildAt(i).requestFocus()
                    }
                }
                for (i in 4..5) {
                    if ((binding.inputCode.getChildAt(i) as EditText).text.isNotEmpty()) {
                        binding.inputCode.getChildAt(i).requestFocus()
                    }
                }
            }
        }
        binding.codeEdit5.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                for (i in 3 downTo 0) {
                    if ((binding.inputCode.getChildAt(i) as EditText).text.isEmpty()) {
                        binding.inputCode.getChildAt(i).requestFocus()
                    }
                }
                if ((binding.inputCode.getChildAt(5) as EditText).text.isNotEmpty()) {
                    binding.inputCode.getChildAt(5).requestFocus()
                }
            }
        }
        binding.codeEdit6.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                for (i in 4 downTo 0) {
                    if ((binding.inputCode.getChildAt(i) as EditText).text.isEmpty()) {
                        binding.inputCode.getChildAt(i).requestFocus()
                    }
                }
            }
        }


        binding.btnConfirmCode.setOnClickListener {
            checkCode()
        }

        binding.codeProblemSend.setOnClickListener {
            MixpanelAPI.getInstance(requireContext(), BuildConfig.MIX_TOKEN)
                .track("LoginIssueWriteSupport", null)
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse("https://t.me/propubg_app_creator")
            startActivity(intent)
        }

        binding.btnBack.setOnClickListener {
            (activity as StartActivity).onBackPressed()
        }
    }

    private fun clearCode(){
        binding.inputCode.children.forEach {
            (it as EditText).setText("")
            it.isSelected = false
        }
    }

    private fun setCode(code: String){
        for (i in code.indices){
            (binding.inputCode.getChildAt(i) as EditText).setText(code[i].toString())
        }
    }

    private fun checkCode(){
        val sb = StringBuilder()
        binding.inputCode.children.forEach {
            sb.append((it as EditText).text.toString())
        }
        if (sb.isEmpty()) sb.append("0")
        viewModel.error.value = ""
        val credential = PhoneAuthProvider
            .getCredential(viewModel.verificationId, sb.toString())
        (activity as StartActivity).signInWithPhoneAuthCredential(credential)
    }

}