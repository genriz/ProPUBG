package app.propubg.login.model

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.PhoneAuthProvider

class StartViewModel: ViewModel() {
    var verificationId = ""
    var phone = ""
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    var code = MutableLiveData<String>()
    var error = MutableLiveData<String>().apply { value = "" }
    var isPhoneNew = true
    var timer = MutableLiveData<Long>().apply { value = -1 }
    private var countDownTimer: CountDownTimer? = null
    var timerStarted = false
    var resendEnabled = false

    fun startTimer(time: Int){
        timer.value = (time)*1000L
        if (countDownTimer!=null) countDownTimer!!.cancel()
        countDownTimer = object: CountDownTimer(timer.value!!, 1000){
            override fun onTick(millisUntilFinished: Long) {
                timer.postValue(millisUntilFinished)
            }
            override fun onFinish() {
                timerStarted = false
                resendEnabled = true
            }
        }
        countDownTimer!!.start()
        timerStarted = true
        resendEnabled = false
    }
}