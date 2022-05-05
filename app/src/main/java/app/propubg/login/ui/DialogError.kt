package app.propubg.login.ui

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import app.propubg.R

class DialogError(context: Context,
                  private val clickListener: OnBtnClick):
    AlertDialog(context, R.style.DialogTheme) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_error)
        window?.setBackgroundDrawable(AppCompatResources.getDrawable(context,
            R.drawable.dialog_bg))

        findViewById<TextView>(R.id.btnClose)?.setOnClickListener {
            dismiss()
        }

        findViewById<TextView>(R.id.btnSupport)?.setOnClickListener {
            clickListener.onSupportClick()
            dismiss()
        }
    }

    interface OnBtnClick {
        fun onSupportClick()
    }
}