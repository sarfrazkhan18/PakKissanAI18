package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agri_knowledge")
data class AgriKnowledge(
    @PrimaryKey val id: String,
    val category: String, // "crops", "pests", "diseases", "soils", "weather", "practices"
    val titleEn: String,
    val titleUr: String,
    val descriptionEn: String,
    val descriptionUr: String,
    val detailsEn: String,
    val detailsUr: String,
    val keywords: String // Comma-separated search words for matching
)
