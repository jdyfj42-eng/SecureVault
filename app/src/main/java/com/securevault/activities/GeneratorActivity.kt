package com.securevault.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.securevault.databinding.ActivityGeneratorBinding
import com.securevault.services.EncryptionService

class GeneratorActivity : AppCompatActivity() {

    private lateinit var b: ActivityGeneratorBinding
    private var generated = ""
    private var pickMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityGeneratorBinding.inflate(layoutInflater)
        setContentView(b.root)

        pickMode = intent.getBooleanExtra("pick_mode", false)
        b.toolbar.setNavigationOnClickListener { finish() }

        b.seekLength.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, user: Boolean) {
                b.tvLength.text = (progress + 8).toString()
                generate()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        b.switchUpper.setOnCheckedChangeListener { _, _ -> generate() }
        b.switchLower.setOnCheckedChangeListener { _, _ -> generate() }
        b.switchNumbers.setOnCheckedChangeListener { _, _ -> generate() }
        b.switchSymbols.setOnCheckedChangeListener { _, _ -> generate() }

        b.btnRegenerate.setOnClickListener { generate() }
        b.btnCopy.setOnClickListener {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("password", generated))
            com.google.android.material.snackbar.Snackbar.make(b.root, "Password copied!", 2000)
                .setBackgroundTint(Color.parseColor("#00E5FF"))
                .setTextColor(Color.BLACK)
                .show()
        }

        if (pickMode) {
            b.btnUse.visibility = android.view.View.VISIBLE
            b.btnUse.setOnClickListener {
                setResult(RESULT_OK, Intent().putExtra("password", generated))
                finish()
            }
        }

        generate()
    }

    private fun generate() {
        val len = b.seekLength.progress + 8
        generated = EncryptionService.generatePassword(
            length = len,
            upper = b.switchUpper.isChecked,
            lower = b.switchLower.isChecked,
            nums = b.switchNumbers.isChecked,
            symbols = b.switchSymbols.isChecked
        )
        b.tvPassword.text = generated

        val (label, pct) = EncryptionService.checkStrength(generated)
        b.tvStrength.text = label
        b.strengthBar.progress = (pct * 100).toInt()
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
}
