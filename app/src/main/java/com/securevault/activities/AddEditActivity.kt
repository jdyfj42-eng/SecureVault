package com.securevault.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.securevault.databinding.ActivityAddEditBinding
import com.securevault.models.CATEGORIES
import com.securevault.models.CATEGORY_ICONS
import com.securevault.models.PasswordEntry
import com.securevault.services.DatabaseHelper
import com.securevault.services.EncryptionService
import com.google.android.material.chip.Chip

class AddEditActivity : AppCompatActivity() {

    private lateinit var b: ActivityAddEditBinding
    private lateinit var db: DatabaseHelper
    private var editEntry: PasswordEntry? = null
    private var selectedCategory = "General"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(b.root)
        db = DatabaseHelper(this)

        val entryId = intent.getLongExtra("entry_id", -1L)
        if (entryId != -1L) {
            editEntry = db.getById(entryId)
            populateFields()
            b.toolbar.title = "Edit Password"
        } else {
            b.toolbar.title = "Add Password"
        }

        setupCategoryChips()
        setupPasswordWatcher()

        b.toolbar.setNavigationOnClickListener { finish() }
        b.btnSave.setOnClickListener { save() }
        b.btnGenerate.setOnClickListener {
            startActivityForResult(
                Intent(this, GeneratorActivity::class.java).putExtra("pick_mode", true),
                REQUEST_GENERATOR
            )
        }
    }

    private fun populateFields() {
        val e = editEntry ?: return
        b.etTitle.setText(e.title)
        b.etUsername.setText(e.username)
        b.etWebsite.setText(e.website)
        b.etNotes.setText(e.notes)
        selectedCategory = e.category
        try { b.etPassword.setText(EncryptionService.decrypt(e.encryptedPassword)) } catch (_: Exception) {}
    }

    private fun setupCategoryChips() {
        CATEGORIES.forEach { cat ->
            val chip = Chip(this).apply {
                text = "${CATEGORY_ICONS[cat]} $cat"
                isCheckable = true
                isChecked = cat == selectedCategory
                setChipBackgroundColorResource(android.R.color.transparent)
                setTextColor(Color.WHITE)
            }
            b.chipGroupCategory.addView(chip)
            chip.setOnClickListener { selectedCategory = cat }
        }
    }

    private fun setupPasswordWatcher() {
        b.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pwd = s.toString()
                if (pwd.isEmpty()) { b.strengthBar.progress = 0; b.tvStrength.text = ""; return }
                val (label, pct) = EncryptionService.checkStrength(pwd)
                b.strengthBar.progress = (pct * 100).toInt()
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
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b2: Int, c: Int) {}
        })
    }

    private fun save() {
        val title = b.etTitle.text.toString().trim()
        val username = b.etUsername.text.toString().trim()
        val password = b.etPassword.text.toString()

        when {
            title.isEmpty() -> { b.tvError.text = "Title required"; b.tvError.visibility = View.VISIBLE; return }
            username.isEmpty() -> { b.tvError.text = "Username required"; b.tvError.visibility = View.VISIBLE; return }
            password.isEmpty() -> { b.tvError.text = "Password required"; b.tvError.visibility = View.VISIBLE; return }
        }

        val encrypted = EncryptionService.encrypt(password)
        val entry = PasswordEntry(
            id = editEntry?.id ?: 0,
            title = title,
            username = username,
            encryptedPassword = encrypted,
            website = b.etWebsite.text.toString().trim(),
            notes = b.etNotes.text.toString().trim(),
            category = selectedCategory,
            createdAt = editEntry?.createdAt ?: System.currentTimeMillis(),
            isFavorite = editEntry?.isFavorite ?: false
        )

        if (editEntry != null) db.update(entry) else db.insert(entry)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GENERATOR && resultCode == RESULT_OK) {
            b.etPassword.setText(data?.getStringExtra("password") ?: "")
        }
    }

    companion object { const val REQUEST_GENERATOR = 100 }
}
