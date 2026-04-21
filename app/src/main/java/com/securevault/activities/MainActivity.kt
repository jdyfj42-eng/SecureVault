package com.securevault.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.securevault.adapters.PasswordAdapter
import com.securevault.databinding.ActivityMainBinding
import com.securevault.models.CATEGORIES
import com.securevault.models.CATEGORY_ICONS
import com.securevault.models.PasswordEntry
import com.securevault.services.DatabaseHelper
import com.securevault.services.EncryptionService
import com.google.android.material.chip.Chip

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: PasswordAdapter
    private var allEntries = listOf<PasswordEntry>()
    private var selectedCategory = "All"
    private var showFavoritesOnly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        db = DatabaseHelper(this)

        setupRecyclerView()
        setupSearch()
        setupChips()
        setupButtons()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = PasswordAdapter(
            onItemClick = { entry ->
                startActivity(Intent(this, DetailActivity::class.java)
                    .putExtra("entry_id", entry.id))
            },
            onFavoriteClick = { entry ->
                db.toggleFavorite(entry.id, !entry.isFavorite)
                loadData()
            },
            onDeleteClick = { entry ->
                db.delete(entry.id)
                loadData()
            }
        )
        b.recyclerView.layoutManager = LinearLayoutManager(this)
        b.recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        b.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { applyFilters() }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b2: Int, c: Int) {}
        })
    }

    private fun setupChips() {
        // Tab chips
        b.chipAll.setOnClickListener { showFavoritesOnly = false; applyFilters() }
        b.chipFavorites.setOnClickListener { showFavoritesOnly = true; applyFilters() }

        // Category chips
        val allChip = Chip(this).apply {
            text = "All"
            isCheckable = true
            isChecked = true
            setChipBackgroundColorResource(android.R.color.transparent)
            setTextColor(android.graphics.Color.WHITE)
        }
        b.chipGroupCategory.addView(allChip)
        allChip.setOnClickListener { selectedCategory = "All"; applyFilters() }

        CATEGORIES.forEach { cat ->
            val chip = Chip(this).apply {
                text = "${CATEGORY_ICONS[cat]} $cat"
                isCheckable = true
                setChipBackgroundColorResource(android.R.color.transparent)
                setTextColor(android.graphics.Color.WHITE)
            }
            b.chipGroupCategory.addView(chip)
            chip.setOnClickListener { selectedCategory = cat; applyFilters() }
        }
    }

    private fun setupButtons() {
        b.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditActivity::class.java))
        }
        b.btnGenerator.setOnClickListener {
            startActivity(Intent(this, GeneratorActivity::class.java))
        }
        b.btnLock.setOnClickListener {
            EncryptionService.clearKey()
            startActivity(Intent(this, LockActivity::class.java))
            finish()
        }
    }

    private fun loadData() {
        allEntries = db.getAll()
        applyFilters()
        updateStats()
    }

    private fun applyFilters() {
        var filtered = allEntries
        if (showFavoritesOnly) filtered = filtered.filter { it.isFavorite }
        if (selectedCategory != "All") filtered = filtered.filter { it.category == selectedCategory }
        val q = b.etSearch.text.toString()
        if (q.isNotEmpty()) {
            filtered = filtered.filter {
                it.title.contains(q, true) ||
                it.username.contains(q, true) ||
                it.website.contains(q, true)
            }
        }
        adapter.submitList(filtered)
        b.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateStats() {
        b.tvTotal.text = allEntries.size.toString()
        b.tvFavorites.text = allEntries.count { it.isFavorite }.toString()
        b.tvCategories.text = CATEGORIES.count { c -> allEntries.any { it.category == c } }.toString()
    }
}
