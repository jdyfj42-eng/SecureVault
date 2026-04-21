package com.securevault.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.securevault.databinding.ActivityLockBinding
import com.securevault.services.EncryptionService

class LockActivity : AppCompatActivity() {

    private lateinit var b: ActivityLockBinding
    private var attempts = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLockBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnUnlock.setOnClickListener { tryUnlock() }
        b.btnBiometric.setOnClickListener { tryBiometric() }

        val bio = BiometricManager.from(this)
        if (bio.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS) {
            b.btnBiometric.visibility = View.VISIBLE
            tryBiometric()
        }
    }

    private fun tryUnlock() {
        val pwd = b.etPassword.text.toString()
        if (pwd.isEmpty()) return
        if (EncryptionService.verifyMasterPassword(this, pwd)) {
            goHome()
        } else {
            attempts++
            val shake = AnimationUtils.loadAnimation(this, android.R.anim.cycle_interpolator)
            b.etPassword.startAnimation(shake)
            b.etPassword.text?.clear()
            b.tvError.text = "Wrong password${if (attempts >= 3) " ($attempts attempts)" else ""}"
            b.tvError.visibility = View.VISIBLE
        }
    }

    private fun tryBiometric() {
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                goHome()
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock SecureVault")
            .setSubtitle("Use biometric to unlock")
            .setNegativeButtonText("Use Password")
            .build()
        prompt.authenticate(info)
    }

    private fun goHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
