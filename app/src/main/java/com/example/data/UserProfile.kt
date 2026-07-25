package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val id: String, // Farmer's unique phone number as the ID (supports 1 million+ profiles)
    val fullName: String,
    val phoneNumber: String,
    val region: String,
    val primaryCrop: String,
    val onboardingCompleted: Boolean = false,
    val selectedDialect: String = "Urdu",
    val passwordHash: String = "", // Secure local password storage
    val isActive: Boolean = false, // Live login session tracker

    // --- My Farm profile (P2.1/P2.2) ---
    // District matters because sowing dates and pest cycles vary by 3+ weeks across a
    // province; the AI advisory is conditioned on these fields (see FarmersViewModel).
    val district: String = "",
    val cropVariety: String = "",
    val landArea: String = "",              // free text, e.g. "5" or "2.5"
    val areaUnit: String = "Acre",          // Acre / Kanal / Murabba
    val sowingDateMillis: Long = 0L,        // 0 = not set
    val irrigationSource: String = ""       // نہری / ٹیوب ویل / بارانی
)
