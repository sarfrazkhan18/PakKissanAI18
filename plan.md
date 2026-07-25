# Kisaan Dost (کسان دوست) — Build Plan

**Status:** Draft v1
**Last updated:** 2026-07-25
**Repo:** `sarfrazkhan18/PakKissanAI18`
**Scope of this document:** what to build, in what order, and why — grounded in the realities of the Pakistani smallholder farmer.

---

## 0. How to read this document

This is a working plan, not a pitch deck. It is organised so you can start at Section 6 (Phase 0) tomorrow morning and know exactly what to do.

Three things drive every decision in here:

1. **The user cannot read.** ~40% of rural adults in Pakistan are non-literate, and among the 50+ male farmers who make the planting decisions it is higher. Every feature is designed voice-first or icon-first, and text is a *secondary* channel, never the only one.
2. **The user is not on a good phone, on good internet, indoors.** He is on a PKR 25,000 Android with a dim LCD, standing in a field at 42°C, on 3G that drops, with 400 MB of data left for the month.
3. **Wrong advice costs him a year of income.** This is not a chatbot where a bad answer is a shrug. A wrong pesticide dose on 5 acres of cotton is a family's annual earnings. Safety architecture is not a phase-3 nice-to-have.

Guiding principle for the whole build:

> **Be the most trustworthy answer a farmer can get in 30 seconds, in his own language, on a bad connection.** Not the most feature-rich app.

---

## 1. Where we are today (honest baseline)

| Area | Built | Assessment |
|---|---|---|
| Onboarding (5 steps, voice-guided) | `KisaanOnboardingScreen.kt` (1,299 L) | Strong. Warm, voice-prompted, big type. Keep and refine. |
| Chat + voice UI | `MainFarmersScreen.kt` (2,774 L) | Functional but monolithic. Needs decomposition, not rewrite. |
| Local knowledge base (RAG) | `AgriKnowledgeSeeder.kt` — **15 entries** | The single most valuable asset here. Agronomically correct. Far too small. |
| Gemini text advisory + Search grounding | `FarmersViewModel.kt:340-460` | Works. Grounding is unconditional — expensive (see §9). |
| Gemini Live (realtime voice) | `GeminiLiveService.kt` | WebSocket endpoint at line 66 does not match the documented Live API path. Unverified. |
| Offline guide | `OfflineFarmingGuide.kt` (715 L) | Static tips only. Not connected to the RAG DB for offline Q&A. |
| Multi-profile local login | `FarmersViewModel.kt:82-134` | Right instinct (shared devices). Plaintext passwords, fake OTP. |
| Backend | **none** | Everything on-device. This is the root cause of the API-key, cost, and sync problems. |

### 1.1 Blocking defects (must be fixed before any public release)

| # | Defect | Location | Impact |
|---|---|---|---|
| D1 | Gemini API key ships inside the APK | `FarmersViewModel.kt:289,341` via `BuildConfig` | Anyone can extract it and bill your account. **Release blocker.** |
| D2 | Passwords stored in plaintext | `FarmersViewModel.kt:94,111` (`passwordHash = passwordRaw`) | Credential leak on any device compromise. |
| D3 | OTP is fake and pre-filled | `KisaanOnboardingScreen.kt:229-243` (`"1234"`/`"7860"`) | Phone number is the primary key for every profile; identity is unverified. |
| D4 | `fallbackToDestructiveMigration()` | `AppDatabase.kt:24` | Every schema bump wipes the farmer's profile and full history. Already at v4. |
| D5 | Duplicate user turn sent to model | `FarmersViewModel.kt:256` + `437-442` | Room Flow race: model sometimes sees the question twice. |
| D6 | Light theme hardwired to dark palette | `Theme.kt:34` (`LightColorScheme = DarkColorScheme`) | App is always near-black (`#0A0C0B`). Unreadable in direct sunlight on a cheap LCD. **See §4.1.** |
| D7 | 5 of 7 advertised languages have no real voice | `FarmersViewModel.kt:475-483` (all map to `audioLocale = "ur"`) | Pashto/Sindhi answers are read by an Urdu engine → unintelligible. |
| D8 | Grounding enabled on every query | `FarmersViewModel.kt:453` | ~$14 per 1,000 grounded queries, paid even for questions already in the local DB. |
| D9 | Knowledge search is `LIKE '%q%'` on a CSV column | `KisaanDao.kt:25` | Misses most natural Urdu phrasing and transliteration variants. |
| D10 | Region granularity is province-level | `KisaanOnboardingScreen.kt:1092-1200` | Sowing dates vary by 3+ weeks across Punjab districts. Too coarse to be correct. |

---

## 2. Who we are building for

Real personas, because the design decisions below only make sense against them.

### P1 — Allah Ditta, 52 — *the decision maker*
5 acres in Vehari, Punjab. Wheat (Rabi) → cotton (Kharif). Speaks Seraiki and Punjabi; understands Urdu; does not read. Owns a PKR 22,000 Android with a cracked screen. Data: PKR 250/month prepaid bundle. Charges the phone at the tubewell during load-shedding. **He decides what to plant, when to irrigate, what to spray.** He will never type. He will speak, and he needs to hear the answer.

### P2 — Bilal, 24 — *the operator*
Allah Ditta's son. Matric-pass, reads Urdu, fluent in WhatsApp and TikTok. **He is the one who will actually install the app and hold the phone.** Most of our "sessions" are Bilal operating the app on his father's behalf, with his father talking. Design for two people around one screen.

### P3 — Zubaida, 38 — *the invisible majority*
Works the family plot, manages two buffalo and a kitchen garden. Rarely owns the phone. BaKhabar Kissan has ~216,000 app users, of whom ~6,500 are women — **3%**. Livestock health and kitchen-garden content are what reach her, and she will use a shared device. Multi-profile matters.

### P4 — Rana Sahib, 34 — *the multiplier*
Agri-input dealer in the local market. Smartphone, literate, advises 60–80 farmers a season. **One of him is worth 50 of P1 in reach.** He wants speed, sources, and the ability to look credible in front of a customer.

### P5 — Extension officer / Kisan Card facilitation operator
Government-adjacent, has a smartphone and a target. Reaches dozens of farmers weekly. The B2G wedge.

---

## 3. Field scenarios the app must survive

These are the acceptance conditions. If a build fails these, it is not done.

| # | Scenario | Requirement it generates |
|---|---|---|
| S1 | Standing in a field, 1pm, direct sun, cheap LCD at max brightness | Light theme, high contrast, huge type, no thin grey text |
| S2 | Hands muddy / wet / gloved, phone held one-handed | Touch targets ≥ 64dp, bottom-of-screen primary actions, no small close buttons |
| S3 | Tractor / tubewell / buffalo noise during voice input | Push-to-hold mic, show transcript for confirmation, always offer retry + text fallback |
| S4 | Signal drops mid-answer | Offline-first: local DB answers without network; queue and retry; never a blank error screen |
| S5 | 400 MB left for the month | No autoplay video, no large images on the hot path, cache aggressively, show a data-light mode |
| S6 | Phone at 8% battery, load-shedding, no charge till evening | No background polling, no wake locks, minimal animation |
| S7 | Two people around one screen, one talking, one tapping | Read every answer aloud by default; large enough to be read from 60cm |
| S8 | Father borrows son's phone; three farmers share one device | Fast profile switch, no re-onboarding, per-profile history |
| S9 | Farmer asks "kal paani lagaun?" with no other context | App must know his crop, sowing date, district, and today's date to answer at all |
| S10 | Farmer follows advice and the crop is damaged | Source attribution, confidence signalling, a clear "verified vs AI" distinction, escalation to a human |
| S11 | Farmer opens the app 7 months later, at the next sowing season | Proactive seasonal push, not a passive chatbot he forgets |

---

## 4. UI/UX plan — designing around the difficulties

### 4.1 Fix the theme first (blocks everything visual)

The current app renders the dark palette in all conditions (`Theme.kt:34`). Dark UI at `#0A0C0B` in Punjab sunlight on a 400-nit screen is a usability failure, not a style choice.

**Work:**
- Build a real `LightColorScheme` from the already-defined but unused `AgriLight*` / `AgriGreen*` colours in `Color.kt`.
- **Default to light.** Dark becomes an opt-in "night mode" (genuinely useful — much usage is after Maghrib).
- Add a manual **☀️/🌙 toggle** in the top bar, not just system-following: the farmer's phone system theme is not a reliable proxy for whether he is standing in the sun.
- Enforce WCAG AAA (7:1) on all body text; ban `alpha < 0.87` on any text the farmer must read. The current `onSurfaceVariant = ElegantDarkText.copy(alpha = 0.7f)` fails this.
- Add a **"بڑا سائز" (extra large)** toggle that scales the type ramp by 1.3×, persisted per profile.

### 4.2 Typography — Urdu is currently rendered wrong

`Type.kt` uses `FontFamily.SansSerif` throughout. On Android that renders Urdu in a **Naskh** fallback. Pakistani readers read **Nastaliq**. It is the difference between comfortable and "foreign-looking".

**Work:**
- Bundle **Noto Nastaliq Urdu** (SIL OFL, redistributable) for Urdu/Punjabi-Shahmukhi/Seraiki text.
- Keep a Naskh/sans face for Sindhi, Pashto, Balochi and for all Latin/numeric runs.
- Nastaliq needs more vertical room: raise `lineHeight` to ~1.9× font size for Urdu styles (Nastaliq clips descenders at normal leading).
- The existing generous size ramp (58sp display, 24sp titleLarge) is **correct** — keep it. Never go below **18sp** for anything a farmer reads.
- Decide and enforce one numeral system. **Recommendation: Western digits (123)** — mandi boards, fertiliser bags, and SMS all use them; Eastern digits (۱۲۳) are recognised by fewer young users. Be consistent everywhere.

### 4.3 Interaction rules (non-negotiable)

- **Every screen has a 🔊 button** that reads the whole screen aloud. The existing `playVoiceGuidance` pattern in onboarding is exactly right — extend it to every screen.
- **No text entry anywhere on the critical path.** Text input stays as a fallback for P2/P4, never a requirement.
- **Max 4 primary actions per screen.** No hamburger menu, no nested navigation, no tabs-within-tabs. Digital-literacy testing consistently shows drawers and back-stacks are where non-literate users get lost.
- **Icon + Urdu label + colour, always all three.** Never an icon alone.
- **Every button ≥ 64dp**, primary actions in the bottom third (thumb zone, one-handed, gloves).
- **Confirm before anything destructive**, spoken aloud, with a big green ✅ and red ❌ — not an "OK/Cancel" text dialog.
- **Loading states must speak.** A silent spinner reads as "broken" to a first-time smartphone user. Say "میں سوچ رہا ہوں…" and show a progress animation.
- **Verify RTL end-to-end.** `supportsRtl="true"` is set in the manifest but every screen needs auditing in RTL — use `start`/`end` padding, never `left`/`right`.

### 4.4 The voice loop (the heart of the product)

Current implementation uses a one-shot `RecognizerIntent` activity plus a `SpeechRecognizer` dialog. That is the right base. What is missing:

1. **Push-and-hold to talk** (walkie-talkie model), not tap-to-start/tap-to-stop. Farmers understand a hold-to-speak button; a toggle mic produces 40-second recordings of tractor noise.
2. **Show what was heard, and speak it back**, before spending an API call: *"آپ نے پوچھا: گندم کو پانی کب لگائیں؟ — درست ہے؟"* with ✅ / 🔁 دوبارہ. This single change will cut wasted API spend and wrong answers more than any prompt tuning.
3. **Noise-aware retry.** On `ERROR_NO_MATCH` / `ERROR_SPEECH_TIMEOUT`, prompt *"شور زیادہ ہے، ذرا قریب ہو کر بولیں"* and re-arm automatically.
4. **Barge-in:** tapping anywhere stops TTS playback immediately.
5. **Replay button on every answer** + adjustable speech rate (0.8× / 1.0×), persisted.

### 4.5 Language: ship honest, not broad

Current: 7 languages advertised, 5 of them route to the Urdu TTS voice (D7).

**Decision: cut the claim to what works, then earn the rest back.**

| Tier | Languages | Input | Output |
|---|---|---|---|
| **Tier 1 — ship now** | Urdu | Google STT `ur-PK` | Urdu TTS ✅ |
| **Tier 1 — ship now** | English | `en-PK` | English TTS ✅ |
| **Tier 2 — Phase 2** | Punjabi (Shahmukhi), Seraiki | `ur-PK` STT (works acceptably; both are close to Urdu phonetically) | Urdu TTS with Punjabi/Seraiki vocabulary in the text — *label this honestly in the UI as "پنجابی متن، اردو آواز"* |
| **Tier 3 — Phase 4+, only with a real TTS partner** | Sindhi, Pashto, Balochi | Needs a non-Google STT | Needs a licensed or trained TTS voice |

Until Tier 3 has a real voice, **remove those options from the picker.** A Pashto speaker hearing garbled Urdu concludes the app is broken and that it is his fault — strictly worse than not offering it.

Budget line for Tier 3: evaluate a commercial Urdu/Pashto/Sindhi TTS vendor, or fine-tune an open model on recorded speech. Treat as a funded workstream, not a config change.

### 4.6 Screens to build or rework

| Screen | State | Plan |
|---|---|---|
| Onboarding | exists | Add district-level location (§5.1), crop sowing date, and a "voice-only" express path (3 taps to done) |
| Home / Ask | exists (in `MainFarmersScreen`) | Rework to a **big mic button + 6 icon shortcuts** (پانی، کھاد، کیڑے، بیماری، منڈی، موسم). No chat history on first screen. |
| Answer view | exists | Add source badge, confidence chip, 🔊 replay, 👍/👎, "پکا؟" (escalate to human) |
| My Farm (میرا کھیت) | **new** | Crop, area, sowing date, irrigation log — drives all personalised advice |
| Crop Calendar (فصل کیلنڈر) | **new** | Timeline of the current crop with what to do this week. The retention engine. |
| Mandi Rates (منڈی بھاؤ) | **new** | Per-maund rates by named mandi, with date + source, or an explicit "آج ریٹ اپ ڈیٹ نہیں ہوا" |
| Weather + alerts | **new** | 5-day, with rain-before-harvest and frost/heat alerts |
| Offline guide | exists | Wire it to the RAG DB so it answers questions offline, not just static tips |
| Profile switcher | exists | Surface it properly for shared devices (§5.5) |

---

## 5. Feature plan

### 5.1 Must-have (no product without these)

**F1 — District-level personalisation.**
Province is not enough (D10). Wheat sowing in Sialkot vs. Rahim Yar Khan differs by ~3 weeks. Ship a bundled district list (Punjab, Sindh, KP, Balochistan — ~130 districts) with a searchable + voice-selectable picker, plus optional GPS auto-detect (`ACCESS_COARSE_LOCATION`, always skippable — never block onboarding on a permission).

**F2 — My Farm profile.**
Crop, variety if known, area (in **acres/kanal/murabba**, never hectares), sowing date, irrigation source (نہری/ٹیوب ویل/بارانی). This is what turns a generic chatbot into an advisor. Every answer should be conditioned on it.

**F3 — Expanded, verified knowledge base.**
15 → **200+** entries. See §7.

**F4 — Offline-first answering.**
On no/poor network, answer from the local DB and say so, clearly: *"انٹرنیٹ نہیں ہے — یہ محفوظ شدہ معلومات ہیں"*. Never show a bare error. Add a `ConnectivityManager` check before every network call.

**F5 — Backend proxy.**
Fixes D1, D8, and unlocks caching, analytics, rate limiting, and content updates without an app release. Minimal: one endpoint, key held server-side, response cache keyed on (normalised question + district + crop + week).

**F6 — Safety and trust layer.**
See §8. Source attribution, verified-vs-AI distinction, pesticide guardrails, escalation path.

**F7 — Crop calendar + proactive nudges.**
Given crop + sowing date + district, compute the current growth stage and push 1–2 notifications a week: *"آپ کی گندم کو 20 دن ہو گئے — پہلا پانی لگانے کا وقت ہے"*. **This is the retention mechanism.** A passive chatbot is opened twice and forgotten; a calendar that knows his field earns a weekly open.

### 5.2 Should-have (Phase 3)

- **F8 — Mandi rates**, per-maund, by named mandi, sourced from AMIS/provincial portals, with date and explicit "not updated today" handling. Highest-pull feature; also the least reliable — do it properly or not at all.
- **F9 — Weather + actionable alerts.** Not a forecast widget — *"پرسوں بارش ہے، آج سپرے نہ کریں"*.
- **F10 — Photo pest/disease diagnosis.** High wow-factor, high risk. Only with a confidence threshold and a mandatory "دکاندار/زرعی افسر سے تصدیق کریں" caveat.
- **F11 — Livestock module.** Reaches P3 (women), doubles addressable questions, and is well-served by static verified content (vaccination schedules, mastitis, deworming).
- **F12 — Share-to-WhatsApp answer card.** Farmers already forward everything on WhatsApp. Free distribution, and it meets them in the channel they use.

### 5.3 Later / conditional

- **F13 — WhatsApp bot channel.** Strategically probably more important than the app (§11), but it is a separate build on the same backend.
- **F14 — IVR channel.** The channel that actually reaches P1. Requires a telco partner.
- **F15 — Dealer/extension mode** for P4/P5 — multi-farmer management, printable advisories.
- **F16 — Kisan Card / subsidy info integration** (8070, CM Punjab Kisan Card) — high-trust government hook, low build cost.

### 5.4 Explicitly out of scope for v1

Marketplace/e-commerce, credit/lending, land records, drone/satellite imagery, social feed, in-app payments. Each is a separate company. Saying no now protects the roadmap.

### 5.5 Shared-device support (already half-built — finish it)

Multi-profile exists (`user_profiles`, `isActive` flag) and is the right call. Finish it: avatar+name profile chips on launch, one-tap switch with voice confirmation, per-profile history and language, and a PIN (not a password) that a non-literate user can actually enter — 4 digits on a big numeric keypad.

---

## 6. Phased delivery plan

### Phase 0 — Stop the bleeding (1 week)
*Goal: the app is safe to put on a real device in front of a real farmer.*

- [~] **P0.1** Backend proxy built (`backend/`, zero-dep Node, injects key server-side) and the app base URL is now configurable via `GEMINI_API_BASE_URL` (`build.gradle.kts`). **Remaining:** deploy the proxy behind HTTPS and remove `GEMINI_API_KEY` from the app `.env` — that deploy step is what finally closes D1 (D1 partially closed)
- [x] **P0.2** Passcodes hashed with salted PBKDF2 (`utils/PasswordHasher.kt`); legacy plaintext rows verified and upgraded on next login (D2 ✓)
- [x] **P0.3** Fake pre-filled OTP removed; registration now creates a local, on-device profile honestly, with a duplicate-phone guard that routes existing numbers to login (D3 ✓)
- [x] **P0.4** `fallbackToDestructiveMigration()` removed; schema export enabled (`room.schemaLocation`); destructive fallback limited to pre-release versions 1–3 so v4+ upgrades require a real migration instead of wiping data (D4 ✓)
- [x] **P0.5** Duplicate-turn race fixed — history is snapshotted before the insert so the model sees the prompt once (D5 ✓)
- [x] **P0.6** Live WebSocket repointed to the documented `BidiGenerateContent` endpoint + Live-capable model; whole feature gated behind `LIVE_API_ENABLED` (default off) and the entry button hidden until verified (D6-live ✓)
- [x] **P0.7** Grounding now gated on intent via `needsLiveSearch()` — only mandi/weather/subsidy queries search (D8 ✓)

**Exit criteria:** no secrets in the APK *(pending proxy deploy — P0.1)*; no data loss on upgrade *(done)*; API cost per query measured and logged *(cost reduced via P0.7; measurement lands with the proxy in P0.1)*.

**Build note:** changes were written and reviewed without an Android SDK/Gradle toolchain in this environment; a `./gradlew assembleDebug` on a dev machine is the outstanding verification step before merge.

### Phase 1 — Make it usable in a field (2–3 weeks)
*Goal: an illiterate farmer in direct sunlight can get one correct answer unaided.*

- [x] **P1.1** Light theme, default-on, manual ☀️/🌙 toggle (D6 ✓). Added a semantic `KisaanColors` token set (light + dark) exposed via `LocalKisaanColors`; built a real `LightColorScheme`; migrated ~294 of the 332 hardcoded `Color(0x…)` literals to tokens (only bright semantic colors — red/blue/white — remain literal). Default is light for sunlight readability; `darkMode` preference persisted, toggled from the top bar
- [~] **P1.2** Line-heights raised for Urdu; `NumeralUtils` added **and applied** to displayed message text (Eastern→Western digits); single font-swap point (`KisaanFontFamily`). **Remaining only:** bundle the Nastaliq font binary — every source (GitHub raw, jsDelivr, gstatic) is blocked by this environment's proxy, so it must be dropped in on a dev machine and `KisaanFontFamily` pointed at it (a one-line change)
- [~] **P1.3** Voice loop — **transcript confirmation** added (manual mode shows what STT heard with ✅ بھیجیں / 🔁 دوبارہ, plus سنیں replay and درست کریں edit, before spending an API call); answer **replay + stop-on-tap barge-in already existed** via the bubble speaker toggle; **noise-retry** handled in the listening dialog. **Remaining:** true hold-to-talk (walkie-talkie) mic, a larger rework of the SpeechRecognizer dialog
- [x] **P1.4** Picker cut to selectable tiers (Urdu, English, Punjabi, Seraiki) with honest voice notes ("پنجابی متن، اردو آواز"); Sindhi/Pashto/Balochi hidden from the voice picker but kept for text translation (D7 ✓)
- [x] **P1.5** Home shortcuts: six big one-tap icon shortcuts (پانی/کھاد/کیڑے/بیماری/منڈی بھاؤ/موسم) under the big mic on the empty-state home; each sends its question like a spoken query (✓)
- [x] **P1.6** Offline-first answering: `ConnectivityManager` check; offline (or failed-call) answers come from the local verified KB with a clear "انٹرنیٹ نہیں ہے" banner instead of a dead error; `ACCESS_NETWORK_STATE` added (F4 ✓)
- [~] **P1.7** Decompose `MainFarmersScreen.kt`. Started: extracted the home-components block (~380 lines: `EmptyStateGuide`, the shortcut grid, `GuideItem`/`HomeShortcut`) into `HomeComponents.kt` (same package, brace/paren-balanced, verified). **Remaining:** the higher-dependency pieces (the SpeechRecognizer + Live dialogs, and the main screen's private voice helpers) are best split with a compiler in the loop
- [x] **P1.8** "بڑا سائز" text-scale toggle: global `LocalDensity` fontScale (works across hardcoded sp), persisted per device, top-bar toggle (✓)

**Exit criteria:** 5 farmers, no coaching, each completes one voice question → heard answer, outdoors, on a sub-PKR-30k phone. *(Blocked on P1.1 sunlight-readable theme and P1.3/P1.5 voice+home rework.)*

**Build note:** written and reviewed without an Android SDK/Gradle toolchain in this environment; `./gradlew assembleDebug` on a dev machine remains the outstanding verification step.

### Phase 2 — Make it correct (3–4 weeks)
*Goal: the advice is right for this farmer, this district, this week.*

- [~] **P2.1** District picker (F1) — bundled ~100-district searchable list (`PakistanDistricts.kt`) grouped by province, wired into the My Farm screen; the AI advisory is now conditioned on the district. **Remaining:** optional GPS auto-detect (skippable; deferred)
- [x] **P2.2** My Farm profile (F2 ✓) — new `MyFarmScreen` (میرا کھیت, opened from the bottom nav) captures district, crop, variety, land area + unit (ایکڑ/کنال/مربع), sowing date (native date picker) and water source (نہری/ٹیوب ویل/بارانی). Persisted via a non-destructive Room v4→v5 migration; every AI answer is now conditioned on these fields plus today's date (so timing questions reason from the sowing date)
- [ ] **P2.3** Knowledge base to 200+ entries, expert-reviewed (§7)
- [ ] **P2.4** Replace `LIKE` search with **Room FTS4/FTS5** + Urdu/Roman-Urdu synonym expansion, using the existing `UrduDictionary.kt` as a seed (D9)
- [x] **P2.5** Safety layer (F6, §8 ✓) — answers now carry an honest badge (🟢 تصدیق شدہ when grounded in the verified KB, 🔵 AI مشورہ otherwise; the old always-on "verified" badge was misleading); a pesticide/chemical guardrail box ("سپرے سے پہلے تصدیق کریں") on any answer mentioning sprays or doses; a "ماہر سے پوچھیں" escalation that dials the Punjab Agriculture Helpline (0800-15000); 👍/👎 feedback persisted per message (v5→v6 migration); and a standing home-screen disclaimer

### Launch simplification (reduce first-time overwhelm)
- [x] Hidden the **Mandi-rate tab** — its prices were hardcoded placeholders, and showing fabricated rates as real is a trust/liability risk (re-enable with real sourcing, P3.1). Nav is now 4 items: Ask / My Farm / Guide / History.
- [x] Removed the fabricated **"Latest Advice"** card ("2 mins ago") and the redundant topic cards from the home; the home is now the big mic + six shortcuts + hands-free toggle + disclaimer.
- [ ] **P2.6** Crop calendar + weekly nudge notifications (F7)

**Exit criteria:** an agronomist reviews 100 real answers and rates ≥90% "correct and actionable"; zero unsafe pesticide recommendations.

### Phase 3 — Make it sticky (4 weeks)
- [ ] **P3.1** Mandi rates with real sourcing (F8)
- [ ] **P3.2** Weather + actionable alerts (F9)
- [ ] **P3.3** Livestock module (F11)
- [ ] **P3.4** Share-to-WhatsApp answer cards (F12)
- [ ] **P3.5** Analytics: question taxonomy, retention cohorts, cost per active farmer

**Exit criteria:** week-4 retention ≥ 25% among onboarded farmers.

### Phase 4 — Reach (ongoing, run in parallel from Phase 1)
- [ ] **P4.1** WhatsApp bot on the same backend (F13)
- [ ] **P4.2** Partner conversations: telco, bank, fertiliser company, provincial ag department (§11)
- [ ] **P4.3** Dealer/extension mode (F15)
- [ ] **P4.4** Tier 3 language voices, if funded

---

## 7. Content plan (the real moat)

The LLM is a commodity. **Verified, district-specific Pakistani agronomy is not.** This is where the defensibility is, and it is currently 15 rows.

**Target for Phase 2: 200+ entries.**

| Category | Target | Notes |
|---|---|---|
| Major crops | 12 | Wheat, cotton, rice, sugarcane, maize, potato, onion, chilli, gram, mustard, fodder, citrus |
| Crop stages | ~60 | Each crop × 5 stages, with per-stage irrigation/fertiliser/watch-outs |
| Pests | 30 | With **PAD-registered** pesticides, doses per acre, PHI (pre-harvest interval), and a دیسی/IPM alternative first |
| Diseases | 25 | Symptoms in farmer language, not pathology terms |
| Soil & water | 15 | Including salinity/sodicity — critical in lower Sindh and south Punjab |
| Livestock | 25 | Vaccination calendar, mastitis, FMD, deworming, feed |
| Government schemes | 10 | Kisan Card, subsidies, 8070/8171, support prices |
| Post-harvest / storage | 10 | Where a lot of avoidable loss happens |
| Kitchen garden | 15 | Reaches P3 |

**Rules for every entry:**
- Sourced from **PARC, provincial Agriculture Extension, AARI Faisalabad, NARC, or a named university** — with the source recorded in the row.
- Reviewed and signed off by a named agronomist. Add `reviewedBy` and `reviewedOn` fields to `AgriKnowledge`.
- Units in **maund (40kg), acre, kanal, bori (50kg)** — never hectares or kg/ha.
- Written at a listening level, not a reading level — short sentences, one instruction each, because TTS will read it.
- Pesticides: active ingredient + a common local trade name + dose per acre + PHI + safety warning. **No pesticide advice ships without a named human reviewer.**

**Schema additions needed on `AgriKnowledge`:** `source`, `reviewedBy`, `reviewedOn`, `provinces`, `districts`, `cropStage`, `validFromWeek`, `validToWeek`, `severity`.

**Content pipeline:** move content out of the compiled `AgriKnowledgeSeeder.kt` and into a versioned JSON asset that the backend can update over-the-air. Content should not need an app release.

---

## 8. Safety, trust and liability

This is the section that decides whether one bad story ends the product.

1. **Two-tier answers, visibly distinguished.**
   - 🟢 **تصدیق شدہ (Verified)** — served from the reviewed knowledge base, with source and review date shown.
   - 🔵 **AI مشورہ (AI advice)** — model-generated, shown with an explicit "تصدیق کر لیں" caveat.
   The farmer must never have to guess which he is looking at.
2. **Pesticide guardrails.** Any answer containing a pesticide/dose is intercepted: served only from verified content, dose shown per acre in a highlighted box, PHI stated, safety-equipment line appended, and a mandatory *"چھڑکاؤ سے پہلے مقامی زرعی افسر یا ڈیلر سے تصدیق کریں"*.
3. **No invented numbers.** The existing system-prompt rule (`FarmersViewModel.kt:417-420`) is good but is a request, not a guarantee. Enforce it in code: post-process responses; if a mandi rate appears without a grounding citation, strip it and substitute the "not updated today" message.
4. **Escalation to a human.** A "پکا نہیں؟ ماہر سے پوچھیں" button on every answer → agriculture helpline number, provincial extension contact, or (later) a callback queue. Costs almost nothing, buys enormous trust.
5. **Feedback loop.** 👍/👎 on every answer, stored with the question, district, crop, and served-tier. This is both a quality signal and the training set for what to add to the knowledge base next.
6. **Disclaimer, spoken once at onboarding**, not buried in a ToS nobody can read.
7. **Data protection.** Farmer phone numbers, land details and location are personal data under Pakistan's draft PDPB. Minimise collection, keep it on-device where possible, encrypt at rest, and never sell it. Publish a plain-Urdu privacy statement.

---

## 9. Cost model and guardrails

Current design cost, per query with grounding on `gemini-3.5-flash`:

| Component | Estimate |
|---|---|
| Tokens (~3k in / ~600 out) | ~$0.009 |
| Google Search grounding | **$14 / 1,000 queries = $0.014** (after 5,000 free/month, and one request can trigger multiple searches) |
| **Total** | **~$0.023 / query** |

At 10 questions/farmer/month: **~$0.23/farmer/month ≈ PKR 65.**
At 100,000 active farmers: **~$23,000/month ≈ PKR 6.5M/month**, against approximately zero willingness to pay.

**Guardrails to build (all in Phase 0–2):**

- **G1** Intent-gate grounding — only mandi/weather/subsidy queries search (P0.7). *Biggest single lever.*
- **G2** Server-side response cache on (normalised question + district + crop + ISO week). Farmers in the same district ask the same question in the same week. Expect a high hit rate.
- **G3** Answer from the local DB without any API call when confidence is high — the RAG DB should *serve* answers, not just decorate prompts.
- **G4** Shrink the prompt. The current system directive is ~700 tokens and is resent every turn; trim it and cap history at 6 turns rather than 10.
- **G5** Consider a Flash-Lite tier for simple classification/routing, reserving the larger model for real advisory.
- **G6** Per-user daily quota with a friendly Urdu message on exhaustion.
- **G7** Log cost per query and per active farmer from day one, on a dashboard.

**Target after guardrails: < $0.05/farmer/month.** That is the number that makes a B2B/B2G deal signable.

---

## 10. Technical work breakdown

### 10.1 Backend (new — the biggest missing piece)
Minimal, boring, cheap. Node/Fastify or Go, one small VM or Cloud Run.

- `POST /ask` — auth, rate limit, cache lookup, intent classify, RAG retrieve, Gemini call, safety post-process, log
- `GET /content/:version` — over-the-air knowledge base updates
- `GET /mandi?crop=&district=` — scraped/ingested AMIS data, cached, with an explicit staleness field
- `GET /weather?district=` — weather provider passthrough, cached per district per hour
- `POST /feedback` — 👍/👎 collection
- Auth: device token issued at onboarding; phone-OTP via an SMS provider

### 10.2 Android
- Decompose `MainFarmersScreen.kt` (2,774 L) → `HomeScreen`, `AnswerScreen`, `MandiScreen`, `WeatherScreen`, `CalendarScreen`, `components/`
- Introduce a repository interface so the ViewModel talks to `KisaanRepository` only, never to Retrofit directly (currently `FarmersViewModel` calls `RetrofitClient.service` at lines 324 and 456)
- Add `ConnectivityManager` observation → an app-wide `isOnline` flow driving offline UI
- Room FTS for knowledge search; proper migrations
- WorkManager for the crop-calendar notification scheduler
- Keep `minSdk 24` — correct for the device base. Watch APK size; target **< 20 MB** (Nastaliq font adds ~2 MB; consider on-demand download for Tier 2/3 fonts)

### 10.3 Testing
- Unit tests for the intent classifier, safety post-processor, and crop-stage calculator (pure logic, high value)
- Roborazzi screenshot tests already scaffolded — extend to cover light theme, RTL, and the XL text scale
- A **manual field-test checklist** derived from §3, run on a real budget device before every release

---

## 11. Distribution (the thing that actually decides success)

The uncomfortable data point: **BaKhabar Kissan reaches ~12 million farmers via IVR/SMS/call-centre, and ~216,000 via its app** — a 55:1 ratio, achieved by the best-funded agritech in the country with a bank and a telco behind it.

Building only a Play Store app means competing for the small side of that ratio, with less distribution muscle.

**Therefore: build the app, but architect the backend so the app is one client of several.** Every feature goes behind `/ask`, so the same brain can serve:

1. **The Android app** — P2, P4, P5. Real, but not the volume channel.
2. **WhatsApp** — zero install friction, already habitual, supports voice notes (which is exactly our input modality). **Highest ROI channel; start in Phase 4, arguably earlier.**
3. **IVR/SMS** — the only channel that reaches P1 directly. Requires a telco partner.
4. **B2B/B2G licensing** — sell the advisory engine to whoever already owns the farmer relationship: a bank running Kisan Card disbursement, a fertiliser company with a dealer network, a provincial agriculture department. **This is where the revenue is**, and it also solves §9 because the payer is an institution, not a smallholder.

**Go-to-market for v1 (app):** pick **one district**, one crop cycle, 200–500 farmers, recruited through 5–10 dealers (P4). Depth over breadth. Learn what they actually ask, then expand.

---

## 12. Risk register

| # | Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|---|
| R1 | Nobody installs the app (channel mismatch) | **High** | Fatal | Backend-first architecture; WhatsApp channel; dealer-led distribution |
| R2 | API cost outruns any revenue | **High** | Fatal | §9 guardrails; B2B payer |
| R3 | Bad advice damages a crop → reputational collapse | Medium | Fatal | §8 safety layer; verified-tier; human escalation |
| R4 | Incumbent (BKK) with bank + telco backing | High | Severe | Do not compete head-on; differentiate on true conversational voice + district specificity, or partner |
| R5 | STT accuracy in noisy field conditions | High | Severe | Confirmation loop; icon shortcuts as a no-voice path; text fallback |
| R6 | Mandi rate sources unreliable/unscrapeable | Medium | Moderate | Explicit staleness UX; never fabricate; partner for a data feed |
| R7 | Gemini pricing or availability changes | Medium | Moderate | Backend abstraction over the model provider; cache aggressively |
| R8 | Content review capacity (need a real agronomist) | Medium | Severe | Budget for a part-time agronomist from Phase 2; partner with a university |
| R9 | Tier 3 languages never get a real voice | Medium | Moderate | Ship honestly (§4.5); do not promise what does not work |

---

## 13. Success metrics

**Phase 1 (usability):** 5/5 farmers complete an unaided voice question outdoors. Time-to-first-answer < 45s.

**Phase 2 (correctness):** ≥90% of 100 sampled answers rated correct+actionable by an agronomist. Zero unsafe pesticide recommendations. ≥60% of answers served from the verified tier.

**Phase 3 (retention):** Week-4 retention ≥25%. ≥3 sessions/farmer/month. 👍 rate ≥80%.

**Phase 4 (viability):** Cost < $0.05/active farmer/month. At least one signed institutional pilot (bank, telco, input company, or provincial department).

---

## 14. Immediate next actions

1. Decide the Phase-0 backend stack and stand up `/ask` with the key server-side (unblocks everything).
2. Ship the light theme and Nastaliq font — one week, transforms first impressions.
3. Recruit an agronomist reviewer before writing more content.
4. Pick the pilot district and the 5 dealers.
5. Decide, explicitly: is the Android app the product, or the first client of an advisory engine? **This plan assumes the latter, and every architecture choice above follows from it.**

---

## Appendix A — Defect backlog (tracked in Phase 0/1)

D1 API key in APK · D2 plaintext passwords · D3 fake OTP · D4 destructive migration · D5 duplicate turn race · D6 dark-only theme · D7 fake multilingual voice · D8 unconditional grounding · D9 weak knowledge search · D10 province-level granularity

## Appendix B — Sources

- [GSMA — BaKhabar Kissan portfolio (12M reach vs 216k app users)](https://www.gsma.com/solutions-and-impact/connectivity-for-good/mobile-for-development/digital-grantees-portfolio/bakhabar-kissan-bkk/)
- [BaKhabar Kissan platform](https://bkk.ag/)
- [CM Punjab Kisan Card 2025 — scheme, 8070/8171 helplines](https://ncf.org.pk/cm-punjab-kissan-card-scheme-2025/)
- [Gemini Developer API pricing](https://ai.google.dev/gemini-api/docs/pricing)
- [Gemini API models](https://ai.google.dev/gemini-api/docs/models)
