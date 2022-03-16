package app.propubg.main

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import app.propubg.R
import app.propubg.appConfig
import app.propubg.databinding.ActivityUpdateBinding

class UpdateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityUpdateBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_update)

        binding.headerUpdate.btnOption.isVisible = false

        binding.btnUpdate.setOnClickListener {
            appConfig?.googlePlayLink?.let{
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(it)
                startActivity(intent)
                finish()
            }
        }

        binding.headerUpdate.btnBack.setOnClickListener {
            finish()
        }
    }
}