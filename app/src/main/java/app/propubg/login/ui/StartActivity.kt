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
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.iosParameters
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import io.realm.mongodb.Credentials
import io.realm.mongodb.functions.Functions
import org.bson.BsonValue
import org.bson.Document
import java.util.*
import java.util.concurrent.TimeUnit

class StartActivity : AppCompatActivity(), DialogError.OnBtnClick {

    lateinit var binding: ActivityStartBinding
    private lateinit var navController: NavController
    private val viewModel: StartViewModel by viewModels()
    private val dialogLoading by lazy {DialogLoading(this)}
    private var resend = false

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

        if (currentUser!=null&&realmApp.currentUser()!=null){
            startMain()
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
            }
        }

    }

    private fun startMain(){
        val mainIntent = Intent(this, MainActivity::class.java)
        var needAuth = false
        intent.extras?.let{
            it.keySet().forEach { key ->
                Log.v("DASD", key)
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
            if (dialogLoading.isShowing) dialogLoading.hide()
            val code = credential.smsCode
            code?.let{
                viewModel.code.postValue(it)
            }
//            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            dialogLoading.hide()
            when {
                e.message.toString().lowercase(Locale.getDefault()).contains("invalid format") -> {
                    viewModel.error.postValue(getString(R.string.phone_wrong))
                }
                e.message.toString().lowercase(Locale.getDefault()).contains("sms verification code") -> {
                    viewModel.error.postValue(getString(R.string.wrong_sms))
                }
                e.message.toString().lowercase(Locale.getDefault()).contains("error 403") -> {
                    showErrorDialog()
                }
                else -> viewModel.error.postValue(e.message?:"Server error")
            }

            viewModel.code.postValue("")

        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken) {
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

    fun verifyNumber(){
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

    private fun openStartFragment(){
        navController.popBackStack()
        navController.navigate(R.id.fragmentStart)
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        dialogLoading.show()
        if (resend){
            viewModel.resendToken?.let {
                resendSms()
            }
        } else {
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = task.result.user
                        user?.let {
                            loginRealm(it.uid, it.phoneNumber!!)
                        }
                        resetValues()
                    } else {
                        dialogLoading.hide()
                        viewModel.code.value = ""
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            viewModel.error.postValue(task.exception!!.message)
                        }
                    }
                }
                .addOnFailureListener {
                    dialogLoading.hide()
                    when {
                        it.message.toString().lowercase(Locale.getDefault())
                            .contains("sms verification code") ->
                            viewModel.error.postValue(getString(R.string.wrong_sms))
                        it.message.toString().lowercase(Locale.getDefault())
                            .contains("code has expired") -> {
                            viewModel.resendToken?.let {
                                resendSms()
                            }
                        }
                        it.message.toString().lowercase(Locale.getDefault())
                            .contains("network") -> {
                            viewModel.error.postValue(it.message)
                        }
                        else -> viewModel.error.postValue(it.message)
                    }
                    viewModel.code.value = ""
                }
        }
    }

    private fun resendSms(){
        val auth = FirebaseAuth.getInstance()
        auth.useAppLanguage()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(viewModel.phone)
            //.setPhoneNumber("+380501234567")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .setForceResendingToken(viewModel.resendToken!!)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        resend = false
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
                dialogLoading.hide()
                viewModel.code.value = ""
                it.error.message?.let{ message ->
                    if (message.contains("stream was reset: PROTOCOL_ERROR"))
                        showErrorDialog()
                    else viewModel.error.postValue(message)
                }
            }
        }
    }

    private fun showErrorDialog() {
        resend = true
        DialogError(this, this).show()
    }

    override fun onBackPressed() {
        viewModel.error.value = ""
        super.onBackPressed()
    }

    override fun onDestroy() {
        getSharedPreferences("prefs", MODE_PRIVATE).edit()
            .putInt("timer", (viewModel.timer.value!!/1000).toInt()).apply()
        getSharedPreferences("prefs", MODE_PRIVATE).edit()
            .putLong("timeExit", System.currentTimeMillis()).apply()
        getSharedPreferences("prefs", MODE_PRIVATE).edit()
            .putInt("currentTimer", currentTimer).apply()
        setResult(RESULT_CANCELED)
        finish()
        super.onDestroy()
    }

    override fun onSupportClick() {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse("https://t.me/propubg_app_creator")
        startActivity(intent)
    }

}