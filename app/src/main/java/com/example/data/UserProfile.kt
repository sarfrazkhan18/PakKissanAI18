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
    val isActive: Boolean = false  // Live login session tracker
)
