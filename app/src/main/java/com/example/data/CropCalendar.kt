package com.example.data

/**
 * Crop growth-stage calendar (P2.6).
 *
 * Given the farmer's crop and sowing date (captured in My Farm), we can compute which growth
 * stage the crop is in today and what action is due this week — turning a passive chatbot into
 * a proactive advisor. Stage windows follow general Punjab Agriculture Extension guidance for
 * each crop; they are approximate and district/variety can shift them by a week or two, which is
 * why every action still points the farmer at local verification.
 */
data class CropStage(
    val nameUr: String,
    val nameEn: String,
    val startDay: Int,   // days after sowing (inclusive)
    val endDay: Int,     // days after sowing (inclusive)
    val actionUr: String,
    val actionEn: String
)

data class CropStageStatus(
    val cropKey: String,
    val daysSinceSowing: Int,
    val currentStage: CropStage,
    val stages: List<CropStage>
) {
    val stageIndex: Int get() = stages.indexOf(currentStage).coerceAtLeast(0)
    val isPastHarvest: Boolean get() = daysSinceSowing > stages.last().endDay
}

object CropCalendar {

    private const val DAY_MS = 86_400_000L

    // Keys match UserProfile.primaryCrop values used across onboarding + My Farm.
    val stagesByCrop: Map<String, List<CropStage>> = mapOf(
        "Wheat" to listOf(
            CropStage("اُگاؤ", "Germination", 0, 21,
                "پہلا پانی بوائی کے 20-22 دن بعد لگائیں (جب جڑیں بن رہی ہوں)۔",
                "Apply the first irrigation 20-22 days after sowing (crown-root stage)."),
            CropStage("پھٹاؤ / کلے", "Tillering", 22, 45,
                "پہلی یوریا پہلے پانی کے ساتھ ڈالیں؛ جڑی بوٹیوں کی سپرے کریں۔",
                "First urea dose with the first irrigation; control weeds."),
            CropStage("تنا بننا", "Stem elongation", 46, 80,
                "دوسری یوریا دوسرے پانی کے ساتھ دیں؛ کھیت کو خشک نہ ہونے دیں۔",
                "Second urea dose with irrigation; do not let the field dry out."),
            CropStage("سٹا نکلنا", "Heading / Booting", 81, 100,
                "دوسرا نازک پانی (80-90 دن) ضرور لگائیں؛ کنگی/رسٹ کی نگرانی کریں۔",
                "Apply the critical second irrigation (80-90 days); scout for rust."),
            CropStage("دانہ بھرنا", "Grain filling", 101, 125,
                "تیسرا پانی (تقریباً 120 دن، دودھیا مرحلہ)؛ پانی کی کمی نہ ہو۔",
                "Third irrigation (~120 days, milky stage); avoid water stress."),
            CropStage("پکائی و کٹائی", "Maturity & Harvest", 126, 155,
                "کٹائی سے پہلے موسم دیکھیں؛ بارش سے پہلے فصل سنبھالیں۔",
                "Check the weather before harvest; secure the crop before rain.")
        ),
        "Rice" to listOf(
            CropStage("ابتدائی نشوونما", "Establishment", 0, 25,
                "2-3 انچ پانی کھڑا رکھیں تاکہ جڑی بوٹیاں دبی رہیں۔",
                "Keep 2-3 inches of standing water to suppress weeds."),
            CropStage("پھٹاؤ", "Tillering", 26, 55,
                "یوریا کی پہلی قسط دیں؛ کھڑا پانی برقرار رکھیں۔",
                "First urea split; maintain standing water."),
            CropStage("گابھ", "Panicle initiation", 56, 85,
                "یوریا کی دوسری قسط دیں؛ پانی کی کمی نہ کریں۔",
                "Second urea split; do not reduce water."),
            CropStage("بالیاں و پھول", "Flowering", 86, 110,
                "نازک مرحلہ: پانی مسلسل رکھیں۔",
                "Sensitive stage: keep water continuous."),
            CropStage("دانہ و پکائی", "Grain fill & Maturity", 111, 140,
                "کٹائی سے 12-15 دن پہلے پانی نکال دیں۔",
                "Drain the field 12-15 days before harvest.")
        ),
        "Cotton" to listOf(
            CropStage("اُگاؤ", "Emergence", 0, 30,
                "چھدرائی مکمل کریں؛ ابتدائی پانی ہلکا رکھیں۔",
                "Complete thinning; keep early irrigation light."),
            CropStage("شاخیں بننا", "Squaring", 31, 60,
                "سفید مکھی/تھرپس کی نگرانی کریں؛ متوازن کھاد دیں۔",
                "Scout for whitefly/thrips; apply balanced fertiliser."),
            CropStage("پھول و ٹینڈے", "Flowering & Boll", 61, 100,
                "سب سے نازک مرحلہ: باقاعدہ پانی؛ گلابی سنڈی کی نگرانی کریں۔",
                "Most critical stage: regular water; scout for pink bollworm."),
            CropStage("ٹینڈے بننا", "Boll development", 101, 140,
                "پانی کا وقفہ نہ بڑھائیں؛ کیڑوں کی سکاؤٹنگ جاری رکھیں۔",
                "Do not stretch irrigation intervals; keep scouting pests."),
            CropStage("چنائی", "Picking", 141, 175,
                "صاف چنائی کریں؛ روئی کو نمی سے بچائیں۔",
                "Pick clean; protect lint from moisture.")
        ),
        "Sugarcane" to listOf(
            CropStage("اُگاؤ", "Germination", 0, 45,
                "کھیت نم رکھیں؛ خالی جگہوں پر دوبارہ آنکھ لگائیں۔",
                "Keep the field moist; gap-fill missing setts."),
            CropStage("پھٹاؤ", "Tillering", 46, 120,
                "یوریا کی اقساط دیں؛ باقاعدہ پانی لگائیں۔",
                "Apply urea splits; irrigate regularly."),
            CropStage("بڑھوتری", "Grand growth", 121, 270,
                "سب سے زیادہ پانی و خوراک کی ضرورت؛ فصل کو باندھ لگائیں۔",
                "Peak water and nutrient demand; prop/tie the crop."),
            CropStage("پختگی", "Maturity", 271, 365,
                "پانی کم کریں تاکہ مٹھاس (recovery) بڑھے۔",
                "Reduce water to raise sugar recovery.")
        ),
        "Maize" to listOf(
            CropStage("اُگاؤ", "Emergence", 0, 20,
                "ابتدائی پانی لگائیں؛ چھدرائی کریں۔",
                "Apply early irrigation; thin the stand."),
            CropStage("نشوونما", "Vegetative", 21, 45,
                "یوریا کی پہلی قسط دیں؛ گوڈی کریں۔",
                "First urea split; hoe/earth-up."),
            CropStage("پھول و بھٹہ", "Tasseling / Silking", 46, 65,
                "سب سے نازک پانی کا مرحلہ: کمی نہ ہونے دیں۔",
                "Most critical water stage: avoid any stress."),
            CropStage("دانہ بھرنا", "Grain filling", 66, 95,
                "پانی برقرار رکھیں۔",
                "Maintain irrigation."),
            CropStage("پکائی", "Maturity", 96, 120,
                "چھلیاں سخت ہونے پر کٹائی کریں۔",
                "Harvest once cobs harden.")
        )
    )

    fun hasCalendar(cropKey: String): Boolean = stagesByCrop.containsKey(cropKey)

    fun daysSinceSowing(sowingMillis: Long, now: Long = System.currentTimeMillis()): Int {
        if (sowingMillis <= 0L) return -1
        return ((now - sowingMillis) / DAY_MS).toInt().coerceAtLeast(0)
    }

    /** Current stage status, or null if the crop has no calendar or no valid sowing date. */
    fun status(cropKey: String, sowingMillis: Long, now: Long = System.currentTimeMillis()): CropStageStatus? {
        val stages = stagesByCrop[cropKey] ?: return null
        val days = daysSinceSowing(sowingMillis, now)
        if (days < 0) return null
        val stage = stages.firstOrNull { days in it.startDay..it.endDay } ?: stages.last()
        return CropStageStatus(cropKey, days, stage, stages)
    }
}
