package app.propubg.main.menu.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.propubg.*
import app.propubg.databinding.FragmentMenuProfileEditBinding
import app.propubg.main.MainActivity
import com.google.gson.Gson
import io.realm.mongodb.functions.Functions
import org.bson.BsonBoolean
import org.bson.BsonValue
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class FragmentMenuProfileEdit: Fragment() {

    private lateinit var binding: FragmentMenuProfileEditBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu_profile_edit,
            container, false)
        binding.user = currentUser
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val timeSaved = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getLong("nickTime", 0)
        if (timeSaved>0) {
            val messageTime = "${getString(R.string.nick_change_will)}\n" +
                    SimpleDateFormat("dd-MM-yyyy HH:mm",
                        Locale.getDefault()).format(timeSaved)
            binding.errorNickname.text = messageTime
            binding.inputNickname.isEnabled = timeSaved <= System.currentTimeMillis()
        }

        binding.inputPhone.isEnabled = false


        binding.header.btnCancel.setOnClickListener {
            (activity as MainActivity).onBackPressed()
        }

        binding.inputNickname.setOnEditorActionListener { _, actionId, _ ->
            if (actionId==EditorInfo.IME_ACTION_DONE){
                binding.inputNickname.clearFocus()
                checkNick(binding.inputNickname.text.toString())
            }
            false
        }

        binding.inputNickname.setOnFocusChangeListener { _, hasFocus ->
            binding.nickDelete.isVisible =
                hasFocus&&binding.inputNickname.text.isNotEmpty()
        }
        binding.inputNickname.doOnTextChanged { text, _, _, _ ->
            text?.let{
                binding.nickDelete.isVisible =
                    it.isNotEmpty()&&binding.inputNickname.hasFocus()
            }
        }

        binding.nickDelete.setOnClickListener {
            binding.inputNickname.setText("")
        }

        binding.header.btnSave.setOnClickListener {
            binding.inputNickname.clearFocus()
            checkNick(binding.inputNickname.text.toString())
        }

        binding.btnCopyId.setOnClickListener {
            (requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).apply {
                setPrimaryClip(ClipData.newPlainText("", currentUser?.user?._id?.toString()?:""))
                Toast.makeText(requireContext(), requireContext()
                    .getString(R.string.id_copied), Toast.LENGTH_SHORT)
                    .show()
            }
            (activity as MainActivity).mixpanelAPI?.track("CopyProfileIdClick", null)
        }

        binding.btnSupport.setOnClickListener {
            (activity as MainActivity).mixpanelAPI?.track("ContactSupportClick", null)
            appConfig?.supportLinkTelegram?.let {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse(it)
                startActivity(intent)
            }
        }

        binding.btnLogout.setOnClickListener {
            (activity as MainActivity).mixpanelAPI?.track("RemoveAccountClick", null)
            val link = if (currentLanguage=="ru") appConfig?.deleteMyAccount_ru?:""
            else appConfig?.deleteMyAccount_en?:""
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(link)
            startActivity(intent)
        }
    }

    private fun checkNick(nick: String) {
        if (nick.length<3){
            binding.errorNickname.text = getString(R.string.nick_short)
        } else {
            if (nick.length>13){
                binding.errorNickname.text = getString(R.string.nick_long)
            } else {
                binding.errorNickname.text = ""
                changeNick(nick)
            }
        }
    }

    private fun changeNick(nick: String){
        val functionsManager: Functions = realmApp.getFunctions(currentUserRealm)
        val map = HashMap<String,String>()
        map["nickname"] = nick
        map["UID"] = currentUser!!.UID!!
        val args: List<Map<String,String>> = listOf(map)
        functionsManager.callFunctionAsync("setUserNickName",
            args, BsonValue::class.java) { result ->
            if (result.isSuccess){
                if (result.get().isBoolean){
                    val response = result.get() as BsonBoolean
                    if (response.value){
                        currentUser!!.user!!.nickname = nick
                        binding.user = currentUser
                        binding.executePendingBindings()
                        requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
                            .edit().putString("user", Gson().toJson(currentUser)).apply()
                        setNickTime()
                    } else {
                        binding.errorNickname.text = getString(R.string.nick_buzy)
                    }
                }
            } else {
                binding.errorNickname.text = getString(R.string.nick_error)
            }
        }
    }

    private fun setNickTime(){
        val time = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        cal.add(Calendar.DATE,1)
        requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit().putLong("nickTime", cal.timeInMillis).apply()
        val messageTime = "${getString(R.string.nick_change_will)}\n" +
                SimpleDateFormat("dd-MM-yyyy HH:mm",
                    Locale.getDefault()).format(cal.timeInMillis)
        binding.errorNickname.text = messageTime
        binding.inputNickname.isEnabled = false
        binding.nickDelete.isVisible = false
    }

    override fun onResume() {
        super.onResume()
        val json = JSONObject()
        json.put("Screen", "Profile")
        json.put("ObjectID", "No value")
        json.put("Title", "No value")
        json.put("Regions", "No value")
        (activity as MainActivity).mixpanelAPI?.track("ScreenView", json)
    }
}