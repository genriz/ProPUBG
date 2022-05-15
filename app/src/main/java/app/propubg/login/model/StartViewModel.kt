package app.propubg.login.model

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.PhoneAuthProvider

class StartViewModel: ViewModel() {
    var verificationId = "0"
    var phone = ""
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    var code = MutableLiveData<String>()
    var error = MutableLiveData<String>().apply { value = "" }
    var isPhoneNew = true
    var timer = MutableLiveData<Long>().apply { value = -1 }
    private var countDownTimer: CountDownTimer? = null
    var timerStarted = false
    var resendEnabled = false
    var accessError = false

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