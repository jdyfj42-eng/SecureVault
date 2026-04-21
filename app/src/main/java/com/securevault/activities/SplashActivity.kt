package com.securevault.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.securevault.R
import com.securevault.databinding.ActivitySplashBinding
import com.securevault.services.EncryptionService

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        binding.root.startAnimation(anim)

        Handler(Looper.getMainLooper()).postDelayed({
            val next = if (EncryptionService.isMasterPasswordSet(this))
                LockActivity::class.java
            else
                SetupActivity::class.java
            startActivity(Intent(this, next))
            finish()
        }, 2000)
    }
}
