package com.securevault.models

data class PasswordEntry(
    val id: Long = 0,
    val title: String,
    val username: String,
    val encryptedPassword: String,
    val website: String = "",
    val notes: String = "",
    val category: String = "General",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

val CATEGORIES = listOf("General","Social","Email","Banking","Work","Shopping","Gaming","Other")

val CATEGORY_ICONS = mapOf(
    "General" to "🔑",
    "Social"  to "💬",
    "Email"   to "📧",
    "Banking" to "🏦",
    "Work"    to "💼",
    "Shopping"to "🛒",
    "Gaming"  to "🎮",
    "Other"   to "📁"
)
