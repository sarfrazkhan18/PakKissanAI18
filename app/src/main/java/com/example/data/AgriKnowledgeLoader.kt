package com.example.data

import android.content.Context
import android.util.Log
import org.json.JSONArray

/**
 * Loads verified agricultural knowledge from a bundled JSON asset (P2.3 / plan §7).
 *
 * The knowledge base is the product's real moat, and it needs to grow from a handful of entries
 * to 200+. Keeping the expandable content in `assets/agri_knowledge.json` (rather than compiled
 * Kotlin) means a content/agronomy contributor can add entries without touching code, and it is
 * the natural precursor to over-the-air content updates from the backend. Each entry carries a
 * `source` and `reviewedBy` so nothing ships as "verified" without provenance.
 */
object AgriKnowledgeLoader {
    private const val ASSET = "agri_knowledge.json"
    private const val TAG = "AgriKnowledgeLoader"

    fun loadFromAssets(context: Context): List<AgriKnowledge> {
        return try {
            val json = context.assets.open(ASSET).bufferedReader().use { it.readText() }
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                AgriKnowledge(
                    id = o.optString("id"),
                    category = o.optString("category"),
                    titleEn = o.optString("titleEn"),
                    titleUr = o.optString("titleUr"),
                    descriptionEn = o.optString("descriptionEn"),
                    descriptionUr = o.optString("descriptionUr"),
                    detailsEn = o.optString("detailsEn"),
                    detailsUr = o.optString("detailsUr"),
                    keywords = o.optString("keywords"),
                    source = o.optString("source"),
                    reviewedBy = o.optString("reviewedBy"),
                    reviewedOn = o.optString("reviewedOn")
                )
            }.filter { it.id.isNotBlank() }
        } catch (e: Exception) {
            Log.w(TAG, "Could not load agri_knowledge.json: ${e.message}")
            emptyList()
        }
    }
}
