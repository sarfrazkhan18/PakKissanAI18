package com.example.data

/**
 * Bundled district list for the My Farm district picker (P2.1).
 *
 * District-level location matters: sowing windows, pest cycles, soil and canal-vs-tubewell
 * water differ markedly across a single province (e.g. wheat sowing in Sialkot vs. Rahim Yar
 * Khan differs by ~3 weeks). The AI advisory is conditioned on the chosen district.
 *
 * This is a representative set of the main agricultural districts, not an exhaustive census
 * list. Each entry carries an Urdu name for display and an English name for AI context.
 */
data class District(val nameEn: String, val nameUr: String, val province: String)

object PakistanDistricts {

    val all: List<District> = buildList {
        // --- Punjab (پنجاب) ---
        val punjab = "Punjab"
        add(District("Lahore", "لاہور", punjab))
        add(District("Kasur", "قصور", punjab))
        add(District("Sheikhupura", "شیخوپورہ", punjab))
        add(District("Nankana Sahib", "ننکانہ صاحب", punjab))
        add(District("Faisalabad", "فیصل آباد", punjab))
        add(District("Jhang", "جھنگ", punjab))
        add(District("Toba Tek Singh", "ٹوبہ ٹیک سنگھ", punjab))
        add(District("Chiniot", "چنیوٹ", punjab))
        add(District("Gujranwala", "گوجرانوالہ", punjab))
        add(District("Gujrat", "گجرات", punjab))
        add(District("Sialkot", "سیالکوٹ", punjab))
        add(District("Narowal", "نارووال", punjab))
        add(District("Mandi Bahauddin", "منڈی بہاؤالدین", punjab))
        add(District("Hafizabad", "حافظ آباد", punjab))
        add(District("Sargodha", "سرگودھا", punjab))
        add(District("Khushab", "خوشاب", punjab))
        add(District("Mianwali", "میانوالی", punjab))
        add(District("Bhakkar", "بھکر", punjab))
        add(District("Multan", "ملتان", punjab))
        add(District("Khanewal", "خانیوال", punjab))
        add(District("Lodhran", "لودھراں", punjab))
        add(District("Vehari", "وہاڑی", punjab))
        add(District("Sahiwal", "ساہیوال", punjab))
        add(District("Okara", "اوکاڑہ", punjab))
        add(District("Pakpattan", "پاکپتن", punjab))
        add(District("Bahawalpur", "بہاولپور", punjab))
        add(District("Bahawalnagar", "بہاولنگر", punjab))
        add(District("Rahim Yar Khan", "رحیم یار خان", punjab))
        add(District("Dera Ghazi Khan", "ڈیرہ غازی خان", punjab))
        add(District("Muzaffargarh", "مظفرگڑھ", punjab))
        add(District("Rajanpur", "راجن پور", punjab))
        add(District("Layyah", "لیہ", punjab))
        add(District("Rawalpindi", "راولپنڈی", punjab))
        add(District("Attock", "اٹک", punjab))
        add(District("Jhelum", "جہلم", punjab))
        add(District("Chakwal", "چکوال", punjab))

        // --- Sindh (سندھ) ---
        val sindh = "Sindh"
        add(District("Karachi", "کراچی", sindh))
        add(District("Thatta", "ٹھٹھہ", sindh))
        add(District("Sujawal", "سجاول", sindh))
        add(District("Badin", "بدین", sindh))
        add(District("Hyderabad", "حیدرآباد", sindh))
        add(District("Tando Allahyar", "ٹنڈو الہ یار", sindh))
        add(District("Tando Muhammad Khan", "ٹنڈو محمد خان", sindh))
        add(District("Matiari", "مٹیاری", sindh))
        add(District("Jamshoro", "جامشورو", sindh))
        add(District("Dadu", "دادو", sindh))
        add(District("Thar (Mithi)", "تھرپارکر", sindh))
        add(District("Umerkot", "عمرکوٹ", sindh))
        add(District("Mirpur Khas", "میرپور خاص", sindh))
        add(District("Sanghar", "سانگھڑ", sindh))
        add(District("Nawabshah (SBA)", "نوابشاہ", sindh))
        add(District("Naushahro Feroze", "نوشہرو فیروز", sindh))
        add(District("Larkana", "لاڑکانہ", sindh))
        add(District("Shikarpur", "شکارپور", sindh))
        add(District("Jacobabad", "جیکب آباد", sindh))
        add(District("Kashmore", "کشمور", sindh))
        add(District("Sukkur", "سکھر", sindh))
        add(District("Khairpur", "خیرپور", sindh))
        add(District("Ghotki", "گھوٹکی", sindh))

        // --- Khyber Pakhtunkhwa (خیبر پختونخوا) ---
        val kpk = "Khyber Pakhtunkhwa"
        add(District("Peshawar", "پشاور", kpk))
        add(District("Nowshera", "نوشہرہ", kpk))
        add(District("Charsadda", "چارسدہ", kpk))
        add(District("Mardan", "مردان", kpk))
        add(District("Swabi", "صوابی", kpk))
        add(District("Mohmand", "مہمند", kpk))
        add(District("Khyber", "خیبر", kpk))
        add(District("Kohat", "کوہاٹ", kpk))
        add(District("Karak", "کرک", kpk))
        add(District("Bannu", "بنوں", kpk))
        add(District("Dera Ismail Khan", "ڈیرہ اسماعیل خان", kpk))
        add(District("Swat", "سوات", kpk))
        add(District("Dir (Lower)", "دیر", kpk))
        add(District("Chitral", "چترال", kpk))
        add(District("Shangla", "شانگلہ", kpk))
        add(District("Buner", "بونیر", kpk))
        add(District("Mansehra", "مانسہرہ", kpk))
        add(District("Abbottabad", "ایبٹ آباد", kpk))
        add(District("Haripur", "ہری پور", kpk))
        add(District("Bagh (Malakand)", "ملاکنڈ", kpk))

        // --- Balochistan (بلوچستان) ---
        val balochistan = "Balochistan"
        add(District("Quetta", "کوئٹہ", balochistan))
        add(District("Pishin", "پشین", balochistan))
        add(District("Killa Abdullah", "قلعہ عبداللہ", balochistan))
        add(District("Loralai", "لورالائی", balochistan))
        add(District("Zhob", "ژوب", balochistan))
        add(District("Sibi", "سبی", balochistan))
        add(District("Nasirabad", "نصیر آباد", balochistan))
        add(District("Jafarabad", "جعفر آباد", balochistan))
        add(District("Jhal Magsi", "جھل مگسی", balochistan))
        add(District("Kachhi (Bolan)", "کچھی", balochistan))
        add(District("Khuzdar", "خضدار", balochistan))
        add(District("Kalat", "قلات", balochistan))
        add(District("Mastung", "مستونگ", balochistan))
        add(District("Lasbela", "لسبیلہ", balochistan))
        add(District("Kharan", "خاران", balochistan))
        add(District("Panjgur", "پنجگور", balochistan))
        add(District("Kech (Turbat)", "کیچ (تربت)", balochistan))
        add(District("Gwadar", "گوادر", balochistan))
        add(District("Chagai", "چاغی", balochistan))

        // --- Islamabad Capital Territory ---
        add(District("Islamabad", "اسلام آباد", "Islamabad"))
    }

    /** Case-insensitive search over English and Urdu names. */
    fun search(query: String): List<District> {
        val q = query.trim()
        if (q.isEmpty()) return all
        return all.filter { it.nameEn.contains(q, ignoreCase = true) || it.nameUr.contains(q) }
    }
}
