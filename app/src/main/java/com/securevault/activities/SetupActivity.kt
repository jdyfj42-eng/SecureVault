package com.securevault.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.securevault.databinding.ActivitySetupBinding
import com.securevault.services.EncryptionService

class SetupActivity : AppCompatActivity() {

    private lateinit var b: ActivitySetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateStrength(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b2: Int, c: Int) {}
        })

        b.btnCreate.setOnClickListener {
            val pwd = b.etPassword.text.toString()
            val confirm = b.etConfirm.text.toString()

            when {
                pwd.isEmpty() -> showError("Enter a master password")
                pwd.length < 8 -> showError("Minimum 8 characters")
                pwd != confirm -> showError("Passwords do not match")
                else -> {
                    EncryptionService.setMasterPassword(this, pwd)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun updateStrength(pwd: String) {
        if (pwd.isEmpty()) { b.strengthBar.progress = 0; b.tvStrength.text = ""; return }
        val (label, percent) = EncryptionService.checkStrength(pwd)
        b.strengthBar.progress = (percent * 100).toInt()
        b.tvStrength.text = label
        val color = when (label) {
            "Weak" -> Color.RED
            "Fair" -> Color.parseColor("#FF9800")
            "Good" -> Color.YELLOW
            "Strong" -> Color.parseColor("#8BC34A")
            else -> Color.parseColor("#00E5FF")
        }
        b.tvStrength.setTextColor(color)
        b.strengthBar.progressTintList = android.content.res.ColorStateList.valueOf(color)
    }

    private fun showError(msg: String) {
        b.tvError.text = msg
        b.tvError.visibility = android.view.View.VISIBLE
    }
}
