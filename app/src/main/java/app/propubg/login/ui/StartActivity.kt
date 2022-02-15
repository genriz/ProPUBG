package app.propubg.login.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
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
import com.google.gson.Gson
import io.realm.Realm
import io.realm.mongodb.Credentials
import io.realm.mongodb.functions.Functions
import org.bson.BsonValue
import org.bson.Document
import java.util.*
import java.util.concurrent.TimeUnit

class StartActivity : AppCompatActivity() {

    lateinit var binding: ActivityStartBinding
    private lateinit var navController: NavController
    private val viewModel: StartViewModel by viewModels()
    private val dialogLoading by lazy {DialogLoading(this)}

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

    fun startMain(){
        intent.extras?.let{
            if (it.getBoolean("needAuth")) {
                setResult(RESULT_OK)
                finish()
            }
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
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

    fun verifyNumber(number: String){
        dialogLoading.show()
        val auth = FirebaseAuth.getInstance()
//        auth.firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber("+380501234567",
//        "123456")
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)
            //.setPhoneNumber("+380501234567")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun openSmsFragment(){
        if (!viewModel.timerStarted&&viewModel.resendEnabled)
            verifyNumber(viewModel.phone)
        else navController.navigate(R.id.fragmentSms)
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        dialogLoading.show()
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result.user
                    user?.let{
                        loginRealm(it.uid, it.phoneNumber!!)
                    }
                    resetValues()
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        dialogLoading.hide()
                        viewModel.error.postValue(task.exception!!.message)
                    }
                }
            }
            .addOnFailureListener {
                dialogLoading.hide()
                if (it.message.toString().lowercase(Locale.getDefault())
                        .contains("sms verification code"))
                    viewModel.error.postValue(getString(R.string.wrong_sms))
                else viewModel.error.postValue(it.message)
                viewModel.code.value = ""
            }
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
                viewModel.error.postValue(it.error.message)
            }
        }
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

}