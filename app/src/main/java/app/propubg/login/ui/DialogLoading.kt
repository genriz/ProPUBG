package app.propubg.login.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import app.propubg.R

class DialogLoading(context: Context): AlertDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_loading)
        window?.setBackgroundDrawable(AppCompatResources.getDrawable(context, R.drawable.dialog_bg))
    }

}