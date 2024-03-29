package app.propubg.login.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import app.propubg.*
import app.propubg.R
import app.propubg.databinding.ActivityStartBinding
import app.propubg.login.model.StartViewModel
import app.propubg.login.model.UserRealm
import app.propubg.main.MainActivity
import app.propubg.utils.AppUtils
import app.propubg.utils.Currency
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.realm.mongodb.Credentials
import io.realm.mongodb.functions.Functions
import org.bson.BsonValue
import org.bson.Document
import java.util.*
import java.util.concurrent.TimeUnit

class StartActivity : AppCompatActivity(), DialogError.OnBtnClick {

    lateinit var binding: ActivityStartBinding
    lateinit var navController: NavController
    private val viewModel: StartViewModel by viewModels()
    private val dialogLoading by lazy {DialogLoading(this)}
    private var resend = false
    private var timer = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUser = Gson().fromJson(getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getString("user", null), UserRealm::class.java)
        prevPhone = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getString("phone", null)
        verificationId = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getString("verificationId", null)
        verificationId?.let{viewModel.verificationId = it}
        timerSaved = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getInt("timer", -1)
        timeExit = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getLong("timeExit", -1)
        currentTimer = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getInt("currentTimer", 60)
        firstStart = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getBoolean("firstStart", true)

        setLanguage()

        jsonCurrencies = AppUtils().getJsonDataFromAsset(applicationContext, "currencies.json")
        jsonCurrencies?.let{
            val gson = Gson()
            val listPersonType = object : TypeToken<List<Currency>>() {}.type
            currenciesFromJson.clear()
            currenciesFromJson.addAll(gson.fromJson(jsonCurrencies, listPersonType))
        }

        if (currentUser!=null&&realmApp.currentUser()!=null){
            val functionsManager: Functions = realmApp.getFunctions(realmApp.currentUser())
            val uid = currentUser!!.UID
            Log.v("DASD", "${currentUser!!.UID}")
            functionsManager.callFunctionAsync("getUserDateByUID",
                listOf(uid), BsonValue::class.java) { result ->
                if (result.isSuccess) {
                    currentUser = UserRealm().apply {
                        user = Gson().fromJson(result.get().toString(),
                            app.propubg.login.model.user::class.java)
                        UID = uid
                    }
                    getSharedPreferences("prefs", Context.MODE_PRIVATE)
                        .edit().putString("user", Gson().toJson(currentUser)).apply()
                    startMain()
                } else {
                    startMain()
                }
            }
        } else {
            binding = DataBindingUtil
                .setContentView(this, R.layout.activity_start)

            window.statusBarColor = ContextCompat.getColor(this, R.color.black)

            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navController = navHostFragment.navController

            if (timerSaved!=-1) {
                val currentTime = System.currentTimeMillis()
                val delta = ((currentTime - timeExit)/1000).toInt()
                if ((timerSaved - delta)>0)
                    viewModel.startTimer(timerSaved - delta)
                else {
                    viewModel.timer.value = 0
                    viewModel.timerStarted = false
                    viewModel.resendEnabled = true
                }
            }        }

        viewModel.timer.observe(this,{
            timer = it
        })
    }

    private fun startMain(){
        val mainIntent = Intent(this, MainActivity::class.java)
        var needAuth = false
        intent.extras?.let{
            it.keySet().forEach { key ->
                Log.v("DASD", "$key - ${it[key].toString()}")
            }
            if (it.containsKey("screen")){
                mainIntent.putExtra("screen", it["screen"].toString())
            } else mainIntent.putExtra("screen", "none")
            if (it.containsKey("title")){
                mainIntent.putExtra("title", it["title"].toString())
            } else mainIntent.putExtra("title", "none")
            if (it.containsKey("text")){
                mainIntent.putExtra("text", it["text"].toString())
            } else mainIntent.putExtra("text", "none")
            if (it.getBoolean("needAuth", false)) {
                needAuth = true
                setResult(RESULT_OK, mainIntent)
                finish()
            }
        }
        if (!needAuth) {
            startActivity(mainIntent)
            finish()
        }
    }

    private fun setLanguage() {
        var langPref = getSharedPreferences("prefs", Activity.MODE_PRIVATE)
            .getString("language", null)
        langPref?.let{
            currentLanguage = it
        }
        if (langPref==null){
            langPref = Locale.getDefault().language
            currentLanguage = langPref
            getSharedPreferences("prefs", Activity.MODE_PRIVATE)
                .edit().putString("language", langPref).apply()
        }
        val resources: Resources = resources
        val dm: DisplayMetrics = resources.displayMetrics
        val config: Configuration = resources.configuration
        config.setLocale(Locale(langPref!!))
        resources.updateConfiguration(config, dm)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.v("DASD", "onVerificationCompleted")
            if (resend) {
                resend = false
                signInWithPhoneAuthCredential(credential)
            }
            if (dialogLoading.isShowing) dialogLoading.hide()
            val code = credential.smsCode
            code?.let{
                viewModel.code.postValue(it)
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.v("DASD", "onVerificationFailed")
            Log.v("DASD", e.message.toString())
            dialogLoading.hide()
            viewModel.accessError = false
            when {
                e.message.toString().lowercase(Locale.getDefault()).contains("invalid format") -> {
                    viewModel.error.postValue(getString(R.string.phone_wrong))
                    return
                }
                e.message.toString().lowercase(Locale.getDefault()).contains("sms verification code") -> {
                    viewModel.error.postValue(getString(R.string.wrong_sms))
                    viewModel.code.postValue("")
                    return
                }
                e.message.toString().contains("verifyPhoneNumber")
                        || e.message.toString().lowercase(Locale.getDefault())
                    .contains("reset by peer")-> {
                    viewModel.accessError = true
                    showErrorDialog(getString(R.string.error_phone))
                    return
                }
                e.message.toString().lowercase(Locale.getDefault())
                    .contains("error 403")
                        || e.message.toString().lowercase(Locale.getDefault())
                    .contains("forbidden") -> {
                    viewModel.accessError = true
                    showErrorDialog(getString(R.string.error_code))
                    return
                }
                else -> {
                    viewModel.accessError = true
                    viewModel.error.postValue(e.message?:"Server error")
                }
            }

        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken) {
            Log.v("DASD", "onCodeSent")
            if (!resend) {
                dialogLoading.hide()
                viewModel.verificationId = verificationId
                viewModel.resendToken = token
                viewModel.code.postValue("")
                if (viewModel.isPhoneNew){
                    viewModel.startTimer(currentTimer)
                } else {
                    viewModel.startTimer(currentTimer)
                }
                getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    .edit().putString("verificationId", verificationId).apply()
                openSmsFragment()
            }
        }
    }

    fun verifyNumber(){
        viewModel.accessError = false
        dialogLoading.show()
        val auth = FirebaseAuth.getInstance()
        auth.useAppLanguage()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(viewModel.phone)
            //.setPhoneNumber("+380501234567")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun openSmsFragment(){
        if (!viewModel.timerStarted&&viewModel.resendEnabled)
            verifyNumber()
        else navController.navigate(R.id.fragmentSms)
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        dialogLoading.show()
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnSuccessListener {result ->
                Log.v("DASD", "signInWithCredential success")
                val user = result.user
                user?.let {
                    loginRealm(it.uid, it.phoneNumber!!)
                }
                resetValues()
            }
            .addOnFailureListener {
                Log.v("DASD", "signInWithCredential failed")
                dialogLoading.hide()
                Log.v("DASD", it.message.toString())
                when {
                    it.message.toString().lowercase(Locale.getDefault())
                        .contains("sms verification code") ->
                        viewModel.error.postValue(getString(R.string.wrong_sms))
                    it.message.toString().lowercase(Locale.getDefault())
                        .contains("code has expired") -> {
                        //viewModel.error.postValue(getString(R.string.wrong_sms))
                        resendSms()
                    }
                    it.message.toString().lowercase(Locale.getDefault())
                        .contains("network") -> {
                        viewModel.error.postValue(it.message)
                    }
                    it.message.toString()
                        .contains("verifyPhoneNumber") -> {
                        showErrorDialog(getString(R.string.error_phone))
                    }
                    else -> viewModel.error.postValue(it.message)
                }
                viewModel.code.value = ""
            }
    }

    private fun resendSms(){
        resend = true
        dialogLoading.show()
        val auth = FirebaseAuth.getInstance()
        auth.useAppLanguage()
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(viewModel.phone)
            //.setPhoneNumber("+380501234567")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
        viewModel.resendToken?.let{
            optionsBuilder.setForceResendingToken(it)
        }
        val options = optionsBuilder.build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resetValues() {
        getSharedPreferences("prefs", MODE_PRIVATE).edit()
            .putInt("timer", -1).apply()
        getSharedPreferences("prefs", MODE_PRIVATE).edit()
            .putLong("timeExit", 0).apply()
        getSharedPreferences("prefs", MODE_PRIVATE).edit()
            .putInt("currentTimer", 60).apply()
        //viewModel.code.value = ""
        //viewModel.verificationId = ""
        //viewModel.phone = ""
        viewModel.error.value = ""
    }

    private fun loginRealm(uid: String, phoneNumber: String){
        val customFunctionCredentials:
                Credentials = Credentials
            .customFunction(Document("UID", uid))
        realmApp.loginAsync(customFunctionCredentials) {
            if (it.isSuccess) {
                Log.v("DASD", "loginRealm success")
                currentUserRealm = it.get()
                val functionsManager: Functions = realmApp.getFunctions(currentUserRealm)
                val map = HashMap<String,String>()
                map["UID"] = uid
                map["phoneNumber"] = phoneNumber
                val args: List<Map<String,String>> = listOf(map)
                functionsManager.callFunctionAsync("FirebasePhoneAuth",
                    args, BsonValue::class.java) { result ->
                    if (result.isSuccess) {
                        functionsManager.callFunctionAsync("getUserDateByUID",
                            listOf(uid), BsonValue::class.java) { result2 ->
                            if (result2.isSuccess) {
                                currentUser = UserRealm().apply {
                                    user = Gson().fromJson(result2.get().toString(),
                                        app.propubg.login.model.user::class.java)
                                    UID = uid
                                }
                                getSharedPreferences("prefs", Context.MODE_PRIVATE)
                                    .edit().putString("user", Gson().toJson(currentUser)).apply()
                                startMain()
                            } else {
                                startMain()
                            }
                        }
                    } else {
                        startMain()
                    }
                }
            } else {
                Log.v("DASD", "loginRealm failed")
                dialogLoading.hide()
                it.error.message?.let{ message ->
                    Log.v("DASD", message)
                    when {
                        message.contains("stream was reset: PROTOCOL_ERROR") -> showErrorDialog(getString(R.string.error_code))
                        message.contains("unexpected end of stream") -> showErrorDialog(getString(R.string.error_code))
                        else -> viewModel.error.postValue(message)
                    }
                }
            }
        }
    }

    private fun showErrorDialog(message: String) {
        val dialog = DialogError(this, this)
        dialog.setMessage(message)
        dialog.show()
    }

    override fun onBackPressed() {
        viewModel.error.value = ""
        super.onBackPressed()
    }

    override fun onPause() {
        getSharedPreferences("prefs", MODE_PRIVATE).edit()
            .putInt("timer", (timer/1000).toInt()).apply()
        getSharedPreferences("prefs", MODE_PRIVATE).edit()
            .putLong("timeExit", System.currentTimeMillis()).apply()
        getSharedPreferences("prefs", MODE_PRIVATE).edit()
            .putInt("currentTimer", currentTimer).apply()
        super.onPause()
    }

    override fun onDestroy() {
        setResult(RESULT_CANCELED)
        super.onDestroy()
    }

    override fun onSupportClick() {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse("https://t.me/propubg_app_creator")
        startActivity(intent)
    }

}