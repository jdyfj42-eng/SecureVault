package com.securevault.services

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.securevault.models.PasswordEntry

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "securevault.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE passwords (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                username TEXT NOT NULL,
                encrypted_password TEXT NOT NULL,
                website TEXT DEFAULT '',
                notes TEXT DEFAULT '',
                category TEXT DEFAULT 'General',
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_favorite INTEGER DEFAULT 0
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {}

    fun getAll(): List<PasswordEntry> {
        val list = mutableListOf<PasswordEntry>()
        val cursor = readableDatabase.query(
            "passwords", null, null, null, null, null, "updated_at DESC"
        )
        cursor.use {
            while (it.moveToNext()) list.add(it.toEntry())
        }
        return list
    }

    fun search(query: String): List<PasswordEntry> {
        val q = "%$query%"
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM passwords WHERE title LIKE ? OR username LIKE ? OR website LIKE ? ORDER BY updated_at DESC",
            arrayOf(q, q, q)
        )
        val list = mutableListOf<PasswordEntry>()
        cursor.use { while (it.moveToNext()) list.add(it.toEntry()) }
        return list
    }

    fun getByCategory(cat: String): List<PasswordEntry> {
        val cursor = readableDatabase.query(
            "passwords", null, "category=?", arrayOf(cat), null, null, "updated_at DESC"
        )
        val list = mutableListOf<PasswordEntry>()
        cursor.use { while (it.moveToNext()) list.add(it.toEntry()) }
        return list
    }

    fun getFavorites(): List<PasswordEntry> {
        val cursor = readableDatabase.query(
            "passwords", null, "is_favorite=1", null, null, null, "updated_at DESC"
        )
        val list = mutableListOf<PasswordEntry>()
        cursor.use { while (it.moveToNext()) list.add(it.toEntry()) }
        return list
    }

    fun insert(entry: PasswordEntry): Long {
        return writableDatabase.insert("passwords", null, entry.toValues())
    }

    fun update(entry: PasswordEntry) {
        val values = entry.toValues()
        values.put("updated_at", System.currentTimeMillis())
        writableDatabase.update("passwords", values, "id=?", arrayOf(entry.id.toString()))
    }

    fun delete(id: Long) {
        writableDatabase.delete("passwords", "id=?", arrayOf(id.toString()))
    }

    fun toggleFavorite(id: Long, fav: Boolean) {
        val values = ContentValues().apply { put("is_favorite", if (fav) 1 else 0) }
        writableDatabase.update("passwords", values, "id=?", arrayOf(id.toString()))
    }

    fun getById(id: Long): PasswordEntry? {
        val cursor = readableDatabase.query(
            "passwords", null, "id=?", arrayOf(id.toString()), null, null, null
        )
        return cursor.use { if (it.moveToFirst()) it.toEntry() else null }
    }

    fun getCount(): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM passwords", null)
        return cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    private fun android.database.Cursor.toEntry() = PasswordEntry(
        id = getLong(getColumnIndexOrThrow("id")),
        title = getString(getColumnIndexOrThrow("title")),
        username = getString(getColumnIndexOrThrow("username")),
        encryptedPassword = getString(getColumnIndexOrThrow("encrypted_password")),
        website = getString(getColumnIndexOrThrow("website")) ?: "",
        notes = getString(getColumnIndexOrThrow("notes")) ?: "",
        category = getString(getColumnIndexOrThrow("category")) ?: "General",
        createdAt = getLong(getColumnIndexOrThrow("created_at")),
        updatedAt = getLong(getColumnIndexOrThrow("updated_at")),
        isFavorite = getInt(getColumnIndexOrThrow("is_favorite")) == 1
    )

    private fun PasswordEntry.toValues() = ContentValues().apply {
        put("title", title)
        put("username", username)
        put("encrypted_password", encryptedPassword)
        put("website", website)
        put("notes", notes)
        put("category", category)
        put("created_at", createdAt)
        put("updated_at", updatedAt)
        put("is_favorite", if (isFavorite) 1 else 0)
    }
}
