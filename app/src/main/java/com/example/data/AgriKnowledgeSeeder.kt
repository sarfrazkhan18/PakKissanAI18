package com.example.data

object AgriKnowledgeSeeder {
    fun getInitialKnowledge(): List<AgriKnowledge> {
        return listOf(
            // --- CROPS ---
            AgriKnowledge(
                id = "crop_wheat",
                category = "crops",
                titleEn = "Wheat (Gandum)",
                titleUr = "گندم (Wheat)",
                descriptionEn = "Wheat is Pakistan's primary staple Rabi crop, sown in November and harvested in April.",
                descriptionUr = "گندم پاکستان کی سب سے اہم ربیع کی فصل اور بنیادی غذا ہے، جو نومبر میں بوئی اور اپریل میں کاٹی جاتی ہے۔",
                detailsEn = """
                    1. Sowing Time: November 1st to November 20th is the golden period for Punjab and Sindh. Sowing after December 10th reduces yield by 1% daily.
                    2. Seed Rate: Use 45 to 50 kg of certified seeds per acre.
                    3. Seed Treatment: Always treat seed with a fungicide (e.g., thiophanate-methyl) to prevent loose smut and rust.
                    4. Irrigation Stages (Critical):
                       - 1st: 20-22 days after sowing (Crown Root Initiation stage).
                       - 2nd: 80-90 days after sowing (Heading/Booting stage).
                       - 3rd: 120 days after sowing (Milky/Grain-filling stage).
                    5. Fertilizer Requirement: 1 bag DAP and 1.5-2 bags of Urea per acre. Apply all DAP during soil preparation; split Urea across irrigations.
                """.trimIndent(),
                detailsUr = """
                    1. بوائی کا وقت: پنجاب اور سندھ کے لیے یکم نومبر سے 20 نومبر سنہری وقت ہے۔ 10 دسمبر کے بعد بوائی کی صورت میں پیداوار روزانہ 1 فیصد کم ہو جاتی ہے۔
                    2. بیج کی شرح: 45 سے 50 کلوگرام تصدیق شدہ بیج فی ایکڑ استعمال کریں۔
                    3. بیج کا علاج: اکھاڑا اور کنگی جیسی بیماریوں سے بچاؤ کے لیے بیج کو ہمیشہ پھپھوندی کش زہر (مثلاً تھائیوفینیٹ میتھائل) لگائیں۔
                    4. آبپاشی کے نازک مراحل:
                       - پہلا پانی: بوائی کے 20 سے 22 دن بعد (جب جڑیں بن رہی ہوں)۔
                       - دوسرا پانی: بوائی کے 80 سے 90 دن بعد (جب سٹا نکل رہا ہو)۔
                       - تیسرا پانی: بوائی کے 120 دن بعد (جب دانہ دودھیا مرحلے میں ہو)۔
                    5. کھاد کی ضرورت: 1 بوری ڈی اے پی (DAP) اور ڈیڑھ سے دو بوری یوریا (Urea) فی ایکڑ۔ ڈی اے پی بوائی کے وقت مٹی کی تیاری میں ڈالیں اور یوریا کو قسطوں میں پانی کے ساتھ دیں۔
                """.trimIndent(),
                keywords = "wheat, gandum, rabi, seed, irrigation, fertilizer, fani, gandum ki paidawar, dany, gndm, گندم, بیج, پانی, کھاد"
            ),
            AgriKnowledge(
                id = "crop_rice",
                category = "crops",
                titleEn = "Rice (Chawal)",
                titleUr = "چاول (Rice)",
                descriptionEn = "Rice is a major Kharif cash and export crop in Pakistan, dominated by premium Basmati varieties.",
                descriptionUr = "چاول پاکستان کی اہم خریف کی نقد آور اور برآمدی فصل ہے، جس میں خوشبودار باسمتی اقسام سرفہرست ہیں۔",
                detailsEn = """
                    1. Nursery Sowing: Mid-May to mid-June is ideal depending on Basmati or coarse varieties (IRRI).
                    2. Transplantation: Seedlings should be 25-30 days old. Transplant 2 healthy seedlings per hill.
                    3. Spacing: Maintain 9x9 inches distance to achieve a target of 80,000 hills per acre.
                    4. Water Management: Maintain 2-3 inches of standing water during the first 25-30 days to naturally suppress weed growth. Drain 15 days before harvest.
                    5. Weed Control: Apply pre-emergence herbicide within 3 to 5 days of transplanting while water is standing.
                """.trimIndent(),
                detailsUr = """
                    1. پنیری کی کاشت: باسمتی اور موٹی (IRRI) اقسام کے لیے مئی کے وسط سے جون کے وسط کا وقت بہترین ہے۔
                    2. پنیری کی منتقلی: منتقلی کے وقت پودوں کی عمر 25 سے 30 دن ہونی چاہیے۔ ہر جگہ پر 2 صحت مند پودے لگائیں۔
                    3. فاصلہ: فی ایکڑ 80,000 پودوں کا ہدف حاصل کرنے کے لیے پودوں کے درمیان 9x9 انچ کا فاصلہ رکھیں۔
                    4. پانی کا انتظام: جڑی بوٹیوں کو قدرتی طور پر روکنے کے لیے پہلے 25 سے 30 دنوں تک کھیت میں 2 سے 3 انچ پانی کھڑا رکھیں۔ کٹائی سے 15 دن پہلے پانی نکال دیں۔
                    5. جڑی بوٹیوں کا تدارک: منتقلی کے 3 سے 5 دنوں کے اندر کھڑے پانی میں جڑی بوٹیوں سے بچاؤ کی سپرے کریں۔
                """.trimIndent(),
                keywords = "rice, chawal, basmati, nursery, transplantation, irri, kharif, chawl, rice water, چاول, باسمتی, پنیری, پانی, جھونا"
            ),
            AgriKnowledge(
                id = "crop_cotton",
                category = "crops",
                titleEn = "Cotton (Kapas / Phutti)",
                titleUr = "کپاس (Cotton)",
                descriptionEn = "Known as 'white gold', Cotton is the backbone of Pakistan's textile economy, grown in Kharif.",
                descriptionUr = "کپاس پاکستان کی ٹیکسٹائل معیشت کی ریڑھ کی ہڈی ہے اور اسے 'سفید سونا' کہا جاتا ہے۔ یہ خریف کی فصل ہے۔",
                detailsEn = """
                    1. Sowing Window: Sowing starts from April in Sindh and May to mid-June in Punjab.
                    2. Variety Selection: Opt for BT certified seeds (e.g., CKC-3, IUB-2013) that resist chewing pests.
                    3. Pest Scouting: Conduct pest scouting twice a week to check for whitefly, jassid, and pink bollworm.
                    4. Water Management: Cotton is sensitive to standing water. Ensure ridge sowing for efficient drainage and apply water every 10-14 days.
                    5. Picking: Start picking (Phutti) when 60-70% of bolls are open. Avoid picking during morning dew to prevent moisture spoiling fiber quality.
                """.trimIndent(),
                detailsUr = """
                    1. بوائی کا وقت: سندھ میں بوائی اپریل سے اور پنجاب میں مئی سے جون کے وسط تک شروع ہوتی ہے۔
                    2. اقسام کا انتخاب: بی ٹی (BT) تصدیق شدہ بیجوں (جیسے CKC-3، IUB-2013) کو ترجیح دیں جو چبانے والے کیڑوں کے خلاف مدافعت رکھتے ہیں۔
                    3. پیسٹ سکاؤٹنگ: سفید مکھی، چست تیلا اور گلابی سنڈی کی مانیٹرنگ کے لیے ہفتے میں دو بار پیسٹ سکاؤٹنگ کریں۔
                    4. پانی کا انتظام: کپاس کھڑے پانی کے معاملے میں انتہائی حساس ہے۔ نکاسی آب اور پانی کی بچت کے لیے پٹریوں (کھیلیوں) پر کاشت کریں اور ہر 10 سے 14 دن بعد پانی دیں۔
                    5. چنائی: جب 60 سے 70 فیصد گولے کھل جائیں تو چنائی (پھٹی) شروع کریں۔ فائبر کی کوالٹی برقرار رکھنے کے لیے صبح کی شبنم میں چنائی سے گریز کریں۔
                """.trimIndent(),
                keywords = "cotton, kapas, phutti, bt cotton, whitefly, picking, kharif, kpas, کپاس, پھٹی, بی ٹی کپاس"
            ),
            AgriKnowledge(
                id = "crop_sugarcane",
                category = "crops",
                titleEn = "Sugarcane (Kamand / Ganna)",
                titleUr = "گنا / کماد (Sugarcane)",
                descriptionEn = "Sugarcane is a major annual cash crop in Pakistan requiring heavy water and deep clayey soils.",
                descriptionUr = "گنا پاکستان کی ایک اہم سالانہ نقد آور فصل ہے جس کے لیے بھاری مقدار میں پانی اور گہری مٹی کی ضرورت ہوتی ہے۔",
                detailsEn = """
                    1. Sowing Seasons: Autumn sowing (September-October) yields 25-30% higher than spring sowing (February-March).
                    2. Seed Selection: Use clean seed setts from healthy, disease-free crops. Each sett should have 2-3 healthy buds.
                    3. Row Spacing: Keep 3 to 4 feet distance between trenches to allow maximum tillering and sunlight.
                    4. Fertilizer: Requires high doses of Nitrogen and Potassium. Apply 2 bags of DAP at sowing and 3-4 bags of Urea in splits.
                    5. Weed Management: Critical for the first 90 days. Inter-culturing or mechanical weeding is highly recommended.
                """.trimIndent(),
                detailsUr = """
                    1. بوائی کے موسم: ستمبر اور اکتوبر کی کاشت (موسمِ خزاں) بہار کی کاشت (فروری-مارچ) کے مقابلے میں 25 سے 30 فیصد زیادہ پیداوار دیتی ہے۔
                    2. بیج کا انتخاب: صحت مند اور بیماریوں سے پاک فصل سے کٹے ہوئے گنے کے ٹکڑے (سمیں) استعمال کریں۔ ہر ٹکڑے پر 2 سے 3 صحت مند آنکھیں ہونی چاہئیں۔
                    3. لائنوں کا فاصلہ: ٹرینچوں کے درمیان 3 سے 4 فٹ کا فاصلہ رکھیں تاکہ سورج کی روشنی اور پودے کی شاخیں اچھی طرح بن سکیں۔
                    4. کھاد: نائٹروجن اور پوٹاشیم کی زیادہ ضرورت ہوتی ہے۔ بوائی کے وقت 2 بوری ڈی اے پی دیں اور 3 سے 4 بوری یوریا قسطوں میں ڈالیں۔
                    5. جڑی بوٹیوں کی تلفی: پہلے 90 دن انتہائی نازک ہوتے ہیں۔ گودی (گوڈی) یا مکینیکل جڑی بوٹی مار طریقوں کا استعمال کریں۔
                """.trimIndent(),
                keywords = "sugarcane, ganna, kamand, rabi, autumn sowing, trenches, gna, گنا, کماد, شوگر کین"
            ),

            // --- PESTS ---
            AgriKnowledge(
                id = "pest_whitefly",
                category = "pests",
                titleEn = "Whitefly (Sefaid Makhi)",
                titleUr = "سفید مکھی (Whitefly)",
                descriptionEn = "A notorious sap-sucking pest that devastates Cotton and other vegetable crops in Pakistan.",
                descriptionUr = "رس چوسنے والا ایک خطرناک کیڑا جو پاکستان میں کپاس اور سبزیوں کی فصلوں کو شدید نقصان پہنچاتا ہے۔",
                detailsEn = """
                    1. Damage: Sucks sap from underside of leaves, secretes honeydew leading to black sooty mold, and transmits Cotton Leaf Curl Virus (CLCV).
                    2. Economic Threshold Level (ETL): 5 whiteflies per leaf.
                    3. Desi / Natural Remedy:
                       - Neem Oil Spray: Mix 10ml Neem oil + 5g soap liquid in 1 liter of warm water. Spray thoroughly.
                       - Tobacco Decoction: Boil 1kg tobacco dust in 5 liters of water, strain, add soap, dilute with 50 liters of water and spray.
                    4. Chemical Control: Spray Diafenthiuron or Pyriproxyfen when threshold is breached.
                    5. Cultural Control: Install yellow sticky traps (10-12 per acre) to catch adult flies naturally.
                """.trimIndent(),
                detailsUr = """
                    1. نقصان: پتوں کے نیچے سے رس چوس کر پودے کو کمزور کرتی ہے، میٹھا مادہ خارج کرتی ہے جس سے کالی الی (پپھوندی) جم جاتی ہے، اور کپاس کا پتا مروڑ وائرس پھیلاتی ہے۔
                    2. معاشی نقصان کی حد (ETL): 5 سفید مکھیاں فی پتا۔
                    3. دیسی اور قدرتی علاج:
                       - نیم کا سپرے: 10 ملی لیٹر نیم کا تیل اور 5 گرام برتن دھونے کا مائع صابن 1 لیٹر نیم گرم پانی میں ملا کر سپرے کریں۔
                       - تمباکو کا پانی: 1 کلو تمباکو کا چورا 5 لیٹر پانی میں ابالیں، پن لیں، صابن ملائیں اور 50 لیٹر پانی میں ملا کر سپرے کریں۔
                    4. کیمیائی تدارک: معاشی حد پار ہونے پر ڈائیفینتھیوران یا پائری پروکسیفن کا سپرے کریں۔
                    5. دیگر طریقے: بالغ مکھیوں کو قدرتی طور پر پکڑنے کے لیے پیلے رنگ کے لیس دار کارڈز (پیلا سٹیکی ٹریپ - فی ایکڑ 10-12) لگائیں۔
                """.trimIndent(),
                keywords = "whitefly, sefaid makhi, cotton pest, neem oil, tobacco spray, clcv, white fly, سفید مکھی, کپاس کے کیڑے, نیم کا تیل"
            ),
            AgriKnowledge(
                id = "pest_pink_bollworm",
                category = "pests",
                titleEn = "Pink Bollworm (Gulabi Sundi)",
                titleUr = "گلابی سنڈی (Pink Bollworm)",
                descriptionEn = "A destructive boring pest of Cotton that hides inside the flowers and bolls.",
                descriptionUr = "کپاس کا ایک انتہائی تباہ کن کیڑا جو پھولوں اور ٹینڈوں کے اندر چھپ کر کپاس کی روئی اور بیج کو کھاتا ہے۔",
                detailsEn = """
                    1. Damage: Larvae enter flowers (causing 'rosette flowers') and feed on seeds inside bolls, turning fiber yellow and rotting the boll.
                    2. Economic Threshold Level (ETL): 5% infested bolls or 8 moths captured per pheromone trap per night.
                    3. Prevention: Grow BT varieties, maintain weed-free fields, and destroy cotton sticks debris in winter.
                    4. Natural Trap: Use Pink Bollworm Pheromone Traps (PB-Rope or plastic traps) to disrupt male moths and monitor density.
                    5. Chemical Control: Spray Gamma-Cyhalothrin or Spinetoram during peak infestation.
                """.trimIndent(),
                detailsUr = """
                    1. نقصان: سنڈی پھول کے اندر گھس جاتی ہے (جس سے گلاب نما پھول بنتا ہے) اور ٹینڈے کے اندر روئی اور بیج کو کھاتی ہے، جس سے روئی پیلی ہو جاتی ہے اور ٹینڈا سڑ جاتا ہے۔
                    2. معاشی نقصان کی حد (ETL): 5 فیصد متاثرہ ٹینڈے یا جنسی جال (Pheromone Trap) میں روزانہ 8 پروانے ملنا۔
                    3. بچاؤ: بی ٹی کپاس کی کاشت کریں، مینڈھوں کو صاف رکھیں، اور سردیوں میں کپاس کی چھڑیوں (لکڑیوں) کے مڈھوں کو زمین میں دبا کر یا جلا کر ختم کریں۔
                    4. قدرتی تدارک: نر پروانوں کو بھٹکانے اور نگرانی کے لیے جنسی جال (Pheromone Traps) کھیت میں لگائیں۔
                    5. کیمیائی تدارک: شدید حملے کی صورت میں گیما-سائہیلورتھرین یا اسپینیٹورام کا سپرے کریں۔
                """.trimIndent(),
                keywords = "pink bollworm, gulabi sundi, cotton bolls, rosette flowers, pheromone traps, bt cotton, گلابی سنڈی, کپاس کے ٹینڈے, جنسی جال"
            ),
            AgriKnowledge(
                id = "pest_aphids",
                category = "pests",
                titleEn = "Aphids (Sust Teela / Lahi)",
                titleUr = "سست تیلا (Aphids)",
                descriptionEn = "Small green/black insects that attack Wheat, Mustard, and vegetables in cooler winter months.",
                descriptionUr = "چھوٹے سبز یا کالے رنگ کے کیڑے جو سردیوں کے مہینوں میں گندم، سرسوں اور سبزیوں پر حملہ آور ہوتے ہیں۔",
                detailsEn = """
                    1. Damage: Sucks sap from tender wheat spikes, making grains shriveled and weak.
                    2. Economic Threshold: 15 aphids per plant/spike.
                    3. Natural Predators: Encourage Ladybird Beetles and Lacewings. One ladybug can eat 50-100 aphids per day!
                    4. Homemade Organic Spray: Mix soap water with garlic extract and red pepper, spray on affected plants.
                    5. Chemical Control: Imidacloprid or Acetamiprid spray if infestation exceeds limits.
                """.trimIndent(),
                detailsUr = """
                    1. نقصان: گندم کے نئے سٹوں اور پتوں سے رس چوستے ہیں، جس سے دانہ باریک اور کمزور رہ جاتا ہے۔
                    2. معاشی حد: 15 تیلا فی پودا یا سٹا۔
                    3. قدرتی دوست کیڑے: لیڈی برڈ بیٹل (ڈھیلا/چڑی) اور لیس ونگز کی حفاظت کریں۔ ایک لیڈی برڈ بیٹل روزانہ 50 سے 100 تیلے کھا جاتی ہے!
                    4. گھریلو دیسی سپرے: صابن والے پانی میں لہسن کا عرق اور لال مرچ کا عرق ملا کر متاثرہ حصوں پر سپرے کریں۔
                    5. کیمیائی تدارک: شدید حملے میں امیڈاکلوپرڈ یا ایسیٹامیپرڈ کا سپرے کریں۔
                """.trimIndent(),
                keywords = "aphids, sust teela, lahi, wheat pests, ladybird beetle, soap garlic spray, سست تیلا, گندم کا تیلا, سرسوں کا تیلا"
            ),

            // --- DISEASES ---
            AgriKnowledge(
                id = "disease_wheat_rust",
                category = "diseases",
                titleEn = "Wheat Rust (Kangi / Ratuja)",
                titleUr = "گندم کی کنگی / رتوجا (Wheat Rust)",
                descriptionEn = "A major air-borne fungal disease of wheat in Pakistan, categorized as Stripe/Yellow, Leaf/Brown, and Stem/Black rust.",
                descriptionUr = "ہوا کے ذریعے پھیلنے والی گندم کی ایک خطرناک پھپھوندی کی بیماری جو زرد، بھوری اور کالی کنگی کی شکلوں میں آتی ہے۔",
                detailsEn = """
                    1. Symptoms: Yellow, orange, or brown pustules/spots appear on leaves and stems resembling powder.
                    2. Favored Weather: High humidity, dew, and cool daytime temperatures (15-22C) followed by sunny afternoons.
                    3. Prevention: Sowing resistant varieties certified by seed corporations (e.g., Akbar-19, Dilkash, Faisalabad-08). Avoid outdated varieties like Galaxy.
                    4. Chemical Control: Spray Propiconazole or Tebuconazole fungicide at the first sign of rust patches in the field.
                """.trimIndent(),
                detailsUr = """
                    1. علامات: پتوں اور تنے پر زرد، نارنجی یا بھورے رنگ کے پاؤڈر جیسے دھبے (پسٹول) بن جاتے ہیں۔
                    2. موزوں موسم: ہوا میں زیادہ نمی، شبنم اور دن کا معتدل درجہ حرارت (15 سے 22 ڈگری سینٹی گریڈ) اس بیماری کو پھیلاتا ہے۔
                    3. بچاؤ: زرعی تحقیقاتی اداروں کی منظور شدہ نئی قوتِ مدافعت والی اقسام (مثلاً اکبر-19، دلکش، فیصل آباد-08) کاشت کریں۔ پرانی اقسام جیسے گلیکسی کا استعمال نہ کریں۔
                    4. کیمیائی تدارک: کھیت میں کنگی کا دھبہ نظر آتے ہی فوری طور پر پروپیکونازول یا ٹیبوکونازول جیسی پھپھوندی کش دوا کا سپرے کریں۔
                """.trimIndent(),
                keywords = "wheat rust, kangi, yellow rust, brown rust, leaf rust, Akbar-19, Dilkash, fungicide, tebuconazole, کنگی, گندم کی بیماری, زرد کنگی, رتوجا"
            ),
            AgriKnowledge(
                id = "disease_clcv",
                category = "diseases",
                titleEn = "Cotton Leaf Curl Virus (CLCV)",
                titleUr = "کپاس کا پتا مروڑ وائرس (CLCV)",
                descriptionEn = "A destructive viral disease of cotton transmitted by Whitefly, causing huge economic losses in Punjab.",
                descriptionUr = "کپاس کی ایک تباہ کن وائرل بیماری جو سفید مکھی کے ذریعے پھیلتی ہے اور پتوں کو الٹا مروڑ دیتی ہے۔",
                detailsEn = """
                    1. Symptoms: Upward curling of leaves, thickening of leaf veins, and development of a cup-like leaf growth (enation) on the underside.
                    2. Vector: Silverleaf Whitefly transmits the virus from weeds to cotton.
                    3. Prevention:
                       - Sow certified CLCV resistant BT hybrid varieties.
                       - Keep fields free of wild weeds (like Solanum nigrum) which act as winter hosts for the virus.
                    4. Cure: There is no chemical cure for viral diseases. Direct all efforts to eliminate whiteflies (the vector) using organic neem sprays or targeted pesticides.
                """.trimIndent(),
                detailsUr = """
                    1. علامات: پتوں کا اوپر کی طرف مڑ جانا، پتوں کی رگوں کا موٹا اور سبز ہونا، اور پتے کے نچلے حصے پر پیالہ نما فالتو پتا (اینیشن) بننا۔
                    2. پھیلاؤ: سفید مکھی جنگلی جڑی بوٹیوں سے وائرس لے کر کپاس کے پودوں میں منتقل کرتی ہے۔
                    3. بچاؤ:
                       - منظور شدہ وائرس کے خلاف قوتِ مدافعت رکھنے والی بی ٹی ہائبرڈ اقسام کاشت کریں۔
                       - کھیت کی مینڈھوں کو مکو اور دیگر جنگلی جڑی بوٹیوں سے صاف رکھیں جہاں یہ وائرس سردیوں میں پناہ لیتا ہے۔
                    4. علاج: وائرس کا کوئی براہ راست کیمیائی علاج نہیں ہے۔ وائرس پھیلانے والی سفید مکھی کو مارنے کے لیے نیم کے سپرے یا کیمیائی ادویات کا استعمال کریں۔
                """.trimIndent(),
                keywords = "clcv, leaf curl virus, cotton disease, sefaid makhi vector, weed management, پتا مروڑ, کپاس کا وائرس, سفید مکھی"
            ),

            // --- SOIL TYPES ---
            AgriKnowledge(
                id = "soil_alluvial",
                category = "soils",
                titleEn = "Alluvial Soil (Bhal Wali Mitti)",
                titleUr = "زرخیز میدانی مٹی / بھل والی مٹی (Alluvial Soil)",
                descriptionEn = "Rich, river-deposited soils forming the backbone of the fertile Indus Plains of Punjab and Sindh.",
                descriptionUr = "دریائی تلچھٹ سے بننے والی انتہائی زرخیز مٹی جو پنجاب اور سندھ کے میدانی علاقوں کی کاشتکاری کی بنیاد ہے۔",
                detailsEn = """
                    1. Characteristics: Fine, silt-clay-sand texture (loam), rich in minerals, excellent water holding capacity.
                    2. Ideal Crops: Wheat, Rice, Cotton, Sugarcane, Maize, and orchards of Citrus and Mango.
                    3. Nutrient Status: Moderately low in organic matter (nitrogen) and phosphorus. Requires application of Nitrogen (Urea) and Phosphorus (DAP).
                    4. Best Practices: Add well-rotted cow manure (Gobar) annually to keep the soil structure loose and biological activity active.
                """.trimIndent(),
                detailsUr = """
                    1. خصوصیات: ریت، مٹی اور بھل کا متناسب آمیزہ (میرا مٹی)، جو معدنیات سے مالا مال ہے اور پانی جذب کرنے کی بہترین صلاحیت رکھتی ہے۔
                    2. موزوں ترین فصلیں: گندم، چاول، کپاس، گنا، مکئی، اور کینو اور آم کے باغات۔
                    3. غذائی حالت: نامیاتی مادے (نائٹروجن) اور فاسفورس کی کچھ کمی ہوتی ہے۔ نائٹروجن (یوریا) اور فاسفورس (ڈی اے پی) کھادوں کی ضرورت ہوتی ہے۔
                    4. بہترین طریقے: مٹی کی زرخیزی اور جاندار مائکروبز کو متحرک رکھنے کے لیے ہر سال اچھی طرح گلی سڑی گائے کی گوبر والی کھاد ڈالیں۔
                """.trimIndent(),
                keywords = "alluvial soil, bhal wali mitti, indus plains, loam mitti, organic manure, punjab sindh soil, میرا مٹی, بھل, زرخیز مٹی"
            ),
            AgriKnowledge(
                id = "soil_sandy",
                category = "soils",
                titleEn = "Sandy Soil (Retli Mitti)",
                titleUr = "ریتلی مٹی / ریگستانی مٹی (Sandy Soil)",
                descriptionEn = "Coarse-textured soils found in arid desert regions of Pakistan (Thal, Cholistan, Thar).",
                descriptionUr = "موٹے ذرات پر مشتمل مٹی جو پاکستان کے صحرائی علاقوں (تھل، چولستان اور تھر) میں پائی جاتی ہے۔",
                detailsEn = """
                    1. Characteristics: High sand content, very low water holding capacity, high aeration, very poor nutrient retention.
                    2. Suitable Crops: Chickpea (Chana), Guar, Peanuts, Millets, and specialized drip-irrigated Citrus or Olive orchards.
                    3. Management:
                       - Use organic mulching to prevent moisture evaporation.
                       - Apply fertilizers in small, frequent doses rather than one large dose to avoid nutrient leaching.
                       - Drip Irrigation is highly recommended to save water.
                """.trimIndent(),
                detailsUr = """
                    1. خصوصیات: ریت کی زیادہ مقدار، پانی کو روکنے کی انتہائی کم صلاحیت، زیادہ ہوا دار، اور غذائی اجزاء کو محفوظ نہ رکھ پانے والی مٹی۔
                    2. موزوں فصلیں: چنا، گوار، مونگ پھلی، باجرہ، اور ڈرپ اریگیشن کے تحت کینو یا زیتون کے باغات۔
                    3. انتظام و اصلاح:
                       - نمی کو اڑنے سے روکنے کے لیے پتوں اور گھاس کی ملچنگ (گھاس پھوس سے مٹی ڈھانپنا) کریں۔
                       - غذائی اجزاء کو ضائع ہونے سے بچانے کے لیے کھاد کو ایک ہی بار دینے کے بجائے چھوٹے اور بار بار حصوں میں دیں۔
                       - پانی بچانے کے لیے ڈرپ اریگیشن (قطرہ قطرہ نظام) کا استعمال کریں۔
                """.trimIndent(),
                keywords = "sandy soil, retli mitti, thal, cholistan, thar, chickpea, drip irrigation, mulching, ریتلی مٹی, تھر, چولستان, چنا"
            ),

            // --- WEATHER PATTERNS ---
            AgriKnowledge(
                id = "weather_kharif",
                category = "weather",
                titleEn = "Kharif Season (Sawani)",
                titleUr = "موسمِ خریف / ساونی (Kharif Season)",
                descriptionEn = "Summer crop season in Pakistan starting from April/May and extending to October/November, driven by the Monsoon.",
                descriptionUr = "پاکستان میں گرمیوں کا کاشتکاری کا سیزن جو اپریل/مئی سے شروع ہو کر اکتوبر/نومبر تک چلتا ہے اور مون سون سے جڑا ہے۔",
                detailsEn = """
                    1. Key Crops: Rice, Cotton, Sugarcane, Maize, Mung bean.
                    2. Climatic Features: High heat, humidity, and intense precipitation during July-August Monsoon.
                    3. Challenges: Flash floods, standing water (damages cotton), high humidity causing fungal diseases and insect proliferation.
                    4. Best Practices:
                       - Ensure proper field drainage to clear excess rainwater from cotton and maize.
                       - Sowing crops on raised beds or ridges to protect roots from suffocation.
                """.trimIndent(),
                detailsUr = """
                    1. اہم فصلیں: چاول، کپاس، گنا، مکئی، مونگ۔
                    2. موسمی خصوصیات: جولائی اور اگست کے مون سون کے دوران شدید گرمی، حبس اور بھاری بارشیں۔
                    3. چیلنجز: اچانک سیلاب، کھیتوں میں پانی کھڑا ہونا (جو کپاس کو تباہ کرتا ہے)، اور زیادہ حبس کی وجہ سے فنگس اور کیڑوں کا بڑھتا حملہ۔
                    4. بہترین اقدامات:
                       - کپاس اور مکئی کے کھیتوں سے اضافی بارش کے پانی کی نکاسی کا فوری انتظام کریں۔
                       - جڑوں کو پانی میں گلنے سے بچانے کے لیے فصلوں کو اونچی پٹریوں (کھیلیوں) پر کاشت کریں۔
                """.trimIndent(),
                keywords = "kharif, sawani, summer crops, monsoon, drainage, raised beds, cotton water log, خریف, ساونی, گرمیوں کی فصلیں, مون سون"
            ),
            AgriKnowledge(
                id = "weather_rabi",
                category = "weather",
                titleEn = "Rabi Season (Haari)",
                titleUr = "موسمِ ربیع / ہاڑی (Rabi Season)",
                descriptionEn = "Winter crop season starting in October/November and ending in April/May, depending on winter rains and canal supplies.",
                descriptionUr = "سردیوں کی کاشت کا موسم جو اکتوبر/نومبر میں شروع اور اپریل/مئی میں ختم ہوتا ہے۔ یہ نہری پانی اور سردیوں کی بارشوں پر منحصر ہے۔",
                detailsEn = """
                    1. Key Crops: Wheat, Mustard (Sarso), Gram (Chana), Potato, Berseem fodder.
                    2. Temperature profile: Low temperatures, mild sunshine, frost warnings in late December and January.
                    3. Frost Protection: Frost causes freezing of cell sap in potatoes, tomatoes, and sugarcane. Apply a light irrigation to the field when extreme frost is forecast. Wet soil retains heat better than dry soil.
                    4. Western Depressions: Winter rains (December-March) are highly beneficial for barani (rainfed) wheat crops.
                """.trimIndent(),
                detailsUr = """
                    1. اہم فصلیں: گندم، سرسوں، چنا، آلو، برسیم (چارہ)۔
                    2. موسمی خصوصیات: کم درجہ حرارت، دھیمی دھوپ، اور دسمبر کے آخر اور جنوری میں کورے (Frost) کا خطرہ۔
                    3. کورے (کہر) سے بچاؤ: کورا آلو، ٹماٹر اور چارے کے پودوں کو جھلسا دیتا ہے۔ شدید سردی اور کورے کی پیش گوئی پر کھیت کو ہلکا پانی دیں۔ گیلی مٹی خشک مٹی سے زیادہ حرارت برقرار رکھتی ہے۔
                    4. مغربی ہوائیں (بارشیں): سردیوں کی بارانِ رحمت (دسمبر تا مارچ) بارانی علاقوں کی گندم کے لیے آبِ حیات کا کام کرتی ہے۔
                """.trimIndent(),
                keywords = "rabi, haari, winter crops, wheat season, frost protection, potato frost, rainfed wheat, ربیع, ہاڑی, سردیوں کی فصلیں, کورا"
            ),

            // --- BEST PRACTICES ---
            AgriKnowledge(
                id = "practice_laser_leveling",
                category = "practices",
                titleEn = "Laser Land Leveling",
                titleUr = "لیزر لینڈ لیولنگ (Laser Land Leveler)",
                descriptionEn = "A precision technique to flat-level farming land using laser beams, highly saving water in Pakistan.",
                descriptionUr = "لیزر شعاعوں کی مدد سے مٹی کو بالکل ایک سطح کرنے کا جدید طریقہ جس سے پاکستان میں پانی کی بڑی بچت ہوتی ہے۔",
                detailsEn = """
                    1. Working: A laser transmitter sweeps the field, and a receiver on a tractor scraper automatically adjusts soil cutting and filling.
                    2. Benefits:
                       - Saves 20 to 30% of irrigation water by eliminating high and low spots.
                       - Enhances crop yield by 15-20% due to uniform seed germination and even fertilizer distribution.
                       - Saves fuel and irrigation time.
                    3. Cost-effectiveness: Highly subsidized by provincial agriculture departments under water conservation schemes.
                """.trimIndent(),
                detailsUr = """
                    1. طریقہ کار: ایک لیزر ٹرانسمیٹر کھیت کی پیمائش کرتا ہے اور ٹریکٹر کے ساتھ لگا بلیڈ (کرا) خودکار طور پر اونچی جگہوں سے مٹی کاٹ کر نیچی جگہوں پر بھر دیتا ہے۔
                    2. فوائد:
                       - اونچے اور نیچے مقامات ختم ہونے سے آبپاشی کے پانی میں 20 سے 30 فیصد تک بچت ہوتی ہے۔
                       - یکساں بیج کے اگنے اور کھاد کی برابر تقسیم کی وجہ سے پیداوار میں 15 سے 20 فیصد اضافہ ہوتا ہے۔
                       - ٹریکٹر کا ڈیزل اور پانی دینے کا وقت بچتا ہے۔
                    3. حکومتی امداد: صوبائی محکمہ زراعت پانی بچاؤ سکیموں کے تحت کسانوں کو لیزر لیولر پر بھاری سبسڈی فراہم کرتا ہے۔
                """.trimIndent(),
                keywords = "laser land leveling, laser karah, water saving, uniform germination, subsidy, لیزر لینڈ لیولنگ, لیزر کرا, پانی کی بچت"
            ),
            AgriKnowledge(
                id = "practice_crop_rotation",
                category = "practices",
                titleEn = "Crop Rotation (Faslo ki Adla-Badli)",
                titleUr = "فصلوں کی ادلا بدلی (Crop Rotation)",
                descriptionEn = "The practice of growing different crops sequentially in the same field to build soil fertility and break pest cycles.",
                descriptionUr = "مٹی کی زرخیزی بڑھانے اور کیڑے مکوڑوں کے چکر کو توڑنے کے لیے ایک ہی کھیت میں ترتیب وار مختلف فصلیں اگانے کا عمل۔",
                detailsEn = """
                    1. Principle: Alternating deep-rooted and shallow-rooted crops, or nitrogen-demanding grains with nitrogen-fixing leguminous pulses.
                    2. Ideal Rotation in Pakistan:
                       - Cotton -> Wheat (breaks soil pests, wheat utilizes residual cotton fertilizer).
                       - Rice -> Berseem (clover restores organic nitrogen in clayey soils).
                    3. Major Advantages:
                       - Restores soil nitrogen naturally without expensive urea.
                       - Breaks insect pest and soil-borne fungus survival cycles.
                       - Controls aggressive weeds that adapt to a single crop style.
                """.trimIndent(),
                detailsUr = """
                    1. اصول: گہری اور کم گہری جڑوں والی فصلوں کی ادلا بدلی کریں، یا نائٹروجن کی زیادہ ضرورت والی فصلوں (گندم/مکئی) کے بعد مٹی کو نائٹروجن دینے والی پھلی دار فصلیں (دالیں/برسیم) کاشت کریں۔
                    2. پاکستان میں بہترین فصلوں کا چکر:
                       - کپاس -> گندم (مٹی کے کیڑے مارتا ہے، گندم کپاس کی بچی ہوئی کھاد استعمال کرتی ہے)۔
                       - چاو ل -> برسیم (برسیم مٹی میں قدرتی نائٹروجن بحال کرتا ہے)۔
                    3. اہم فوائد:
                       - مہنگی یوریا کھاد کے بغیر مٹی میں قدرتی نائٹروجن بحال ہوتی ہے۔
                       - نقصان دہ کیڑوں اور مٹی میں موجود فنگس کے زندہ رہنے کا لائف سائیکل ٹوٹ جاتا ہے۔
                       - مخصوص جڑی بوٹیوں کو اگنے سے روکتا ہے جو ایک ہی فصل اگانے سے عادی ہو جاتی ہیں۔
                """.trimIndent(),
                keywords = "crop rotation, adla badli, soil fertility, nitrogen fixation, pulses, berseem clover, فصلوں کا چکر, زمین کی زرخیزی, ادلا بدلی"
            )
        )
    }
}
