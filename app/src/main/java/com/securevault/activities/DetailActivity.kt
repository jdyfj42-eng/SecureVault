package com.securevault.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.securevault.databinding.ActivityDetailBinding
import com.securevault.models.CATEGORY_ICONS
import com.securevault.services.DatabaseHelper
import com.securevault.services.EncryptionService
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {

    private lateinit var b: ActivityDetailBinding
    private lateinit var db: DatabaseHelper
    private var entryId = -1L
    private var decryptedPassword = ""
    private var showingPassword = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(b.root)
        db = DatabaseHelper(this)

        entryId = intent.getLongExtra("entry_id", -1L)
        if (entryId == -1L) { finish(); return }

        b.toolbar.setNavigationOnClickListener { finish() }
        b.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                com.securevault.R.id.action_edit -> {
                    startActivity(Intent(this, AddEditActivity::class.java)
                        .putExtra("entry_id", entryId))
                    true
                }
                com.securevault.R.id.action_delete -> { confirmDelete(); true }
                else -> false
            }
        }
        loadEntry()
    }

    override fun onResume() { super.onResume(); loadEntry() }

    private fun loadEntry() {
        val entry = db.getById(entryId) ?: return

        b.tvTitle.text = entry.title
        b.tvCategoryIcon.text = CATEGORY_ICONS[entry.category] ?: "🔑"
        b.tvCategory.text = entry.category
        b.tvUsername.text = entry.username
        b.tvWebsite.text = if (entry.website.isNotEmpty()) entry.website else "—"
        b.tvNotes.text = if (entry.notes.isNotEmpty()) entry.notes else "—"
        b.tvFavorite.text = if (entry.isFavorite) "⭐" else "☆"

        try { decryptedPassword = EncryptionService.decrypt(entry.encryptedPassword) } catch (_: Exception) {}

        val fmt = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        b.tvCreated.text = fmt.format(Date(entry.createdAt))
        b.tvUpdated.text = fmt.format(Date(entry.updatedAt))

        // Strength
        if (decryptedPassword.isNotEmpty()) {
            val (label, pct) = EncryptionService.checkStrength(decryptedPassword)
            b.tvStrength.text = label
            b.strengthBar.progress = (pct * 100).toInt()
        }

        b.btnTogglePassword.setOnClickListener {
            showingPassword = !showingPassword
            b.tvPassword.text = if (showingPassword) decryptedPassword else "••••••••••••"
            b.btnTogglePassword.text = if (showingPassword) "Hide" else "Show"
        }

        b.btnCopyUsername.setOnClickListener { copy(entry.username, "Username") }
        b.btnCopyPassword.setOnClickListener { copy(decryptedPassword, "Password") }

        b.tvFavorite.setOnClickListener {
            db.toggleFavorite(entry.id, !entry.isFavorite)
            loadEntry()
        }
    }

    private fun copy(text: String, label: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
        com.google.android.material.snackbar.Snackbar.make(b.root, "$label copied!", 2000)
            .setBackgroundTint(android.graphics.Color.parseColor("#00E5FF"))
            .setTextColor(android.graphics.Color.BLACK)
            .show()
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Password?")
            .setMessage("This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> db.delete(entryId); finish() }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
