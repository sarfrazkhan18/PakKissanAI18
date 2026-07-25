package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PakistanDistricts
import com.example.data.UserProfile
import com.example.ui.theme.LocalKisaanColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * My Farm (میرا کھیت) — where the farmer records the details that make the advice specific to
 * him: district (P2.1), crop, variety, land area, sowing date and water source (P2.2). Every AI
 * answer is then conditioned on these (see FarmersViewModel.executeGeminiQuery).
 */
@Composable
fun MyFarmScreen(
    profile: UserProfile?,
    onSave: (district: String, crop: String, variety: String, area: String, areaUnit: String, sowingMillis: Long, irrigation: String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val key = profile?.id ?: "none"

    var district by remember(key) { mutableStateOf(profile?.district ?: "") }
    var districtQuery by remember(key) { mutableStateOf("") }
    var crop by remember(key) { mutableStateOf(profile?.primaryCrop ?: "") }
    var variety by remember(key) { mutableStateOf(profile?.cropVariety ?: "") }
    var area by remember(key) { mutableStateOf(profile?.landArea ?: "") }
    var areaUnit by remember(key) { mutableStateOf(profile?.areaUnit ?: "Acre") }
    var sowingMillis by remember(key) { mutableStateOf(profile?.sowingDateMillis ?: 0L) }
    var irrigation by remember(key) { mutableStateOf(profile?.irrigationSource ?: "") }

    val cropOptions = listOf(
        "Wheat" to "گندم", "Cotton" to "کپاس", "Rice" to "چاول", "Sugarcane" to "گنا",
        "Maize" to "مکئی", "Vegetables" to "سبزیاں", "Livestock" to "مال مویشی"
    )
    val areaUnits = listOf("Acre" to "ایکڑ", "Kanal" to "کنال", "Murabba" to "مربع")
    val irrigationOptions = listOf("نہری", "ٹیوب ویل", "بارانی")

    fun openDatePicker() {
        val cal = Calendar.getInstance()
        if (sowingMillis > 0L) cal.timeInMillis = sowingMillis
        android.app.DatePickerDialog(
            context,
            { _, y, m, d ->
                val c = Calendar.getInstance()
                c.set(y, m, d, 0, 0, 0)
                c.set(Calendar.MILLISECOND, 0)
                sowingMillis = c.timeInMillis
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LocalKisaanColors.current.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalKisaanColors.current.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Agriculture, contentDescription = null, tint = LocalKisaanColors.current.accent)
                Text("میرا کھیت • My Farm", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = LocalKisaanColors.current.textHeading)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "بند کریں", tint = LocalKisaanColors.current.textPrimary)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                "یہ معلومات دینے سے کِسان دوست آپ کے ضلع، فصل اور موسم کے مطابق درست مشورہ دے گا۔",
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = LocalKisaanColors.current.textPrimary.copy(alpha = 0.8f)
            )

            // --- District ---
            FieldLabel("ضلع (District)")
            if (district.isNotBlank()) {
                SelectedChip(text = district, onClear = { district = "" })
            } else {
                OutlinedTextField(
                    value = districtQuery,
                    onValueChange = { districtQuery = it },
                    placeholder = { Text("ضلع تلاش کریں... (search)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("district_search")
                )
                val results = remember(districtQuery) { PakistanDistricts.search(districtQuery).take(8) }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    results.forEach { d ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(LocalKisaanColors.current.surface)
                                .border(BorderStroke(1.dp, LocalKisaanColors.current.border), RoundedCornerShape(10.dp))
                                .clickable {
                                    district = "${d.nameUr} (${d.nameEn})"
                                    districtQuery = ""
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(d.nameUr, fontSize = 15.sp, color = LocalKisaanColors.current.textPrimary)
                            Text(d.province, fontSize = 11.sp, color = LocalKisaanColors.current.textPrimary.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // --- Crop ---
            FieldLabel("بنیادی فصل (Main crop)")
            ChipFlow(
                options = cropOptions.map { it.first to it.second },
                selectedValue = crop,
                onSelect = { crop = it }
            )

            // --- Variety ---
            FieldLabel("قسم / ورائٹی (Variety) — اختیاری")
            OutlinedTextField(
                value = variety,
                onValueChange = { variety = it },
                placeholder = { Text("مثال: FSD-2008، سپر باسمتی") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // --- Area + unit ---
            FieldLabel("رقبہ (Land area)")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it.filter { c -> c.isDigit() || c == '.' } },
                    placeholder = { Text("مثال: 5") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            ChipFlow(
                options = areaUnits.map { it.first to it.second },
                selectedValue = areaUnit,
                onSelect = { areaUnit = it }
            )

            // --- Sowing date ---
            FieldLabel("کاشت کی تاریخ (Sowing date)")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LocalKisaanColors.current.surface)
                    .border(BorderStroke(1.dp, LocalKisaanColors.current.border), RoundedCornerShape(12.dp))
                    .clickable { openDatePicker() }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (sowingMillis > 0L)
                        SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH).format(Date(sowingMillis))
                    else "تاریخ منتخب کریں",
                    fontSize = 15.sp,
                    color = if (sowingMillis > 0L) LocalKisaanColors.current.textPrimary else LocalKisaanColors.current.textPrimary.copy(alpha = 0.5f)
                )
                Icon(Icons.Default.DateRange, contentDescription = null, tint = LocalKisaanColors.current.accent)
            }

            // --- Irrigation source ---
            FieldLabel("پانی کا ذریعہ (Water source)")
            ChipFlow(
                options = irrigationOptions.map { it to it },
                selectedValue = irrigation,
                onSelect = { irrigation = it }
            )

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    onSave(district, crop, variety, area, areaUnit, sowingMillis, irrigation)
                    onClose()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("save_farm"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocalKisaanColors.current.accent)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                Spacer(Modifier.width(8.dp))
                Text("محفوظ کریں (Save)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LocalKisaanColors.current.textHeading)
}

@Composable
private fun SelectedChip(text: String, onClear: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(LocalKisaanColors.current.accent.copy(alpha = 0.15f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LocalKisaanColors.current.textPrimary)
        Icon(
            Icons.Default.Close,
            contentDescription = "تبدیل کریں",
            tint = LocalKisaanColors.current.textPrimary,
            modifier = Modifier.size(18.dp).clip(CircleShape).clickable(onClick = onClear)
        )
    }
}

// Wrapping row of selectable chips. `options` is (value, displayLabel); the stored value is
// `value`, the farmer sees `displayLabel`.
@Composable
private fun ChipFlow(options: List<Pair<String, String>>, selectedValue: String, onSelect: (String) -> Unit) {
    FlowRowSimple {
        options.forEach { (value, label) ->
            val selected = value == selectedValue
            Row(
                modifier = Modifier
                    .padding(end = 8.dp, bottom = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selected) LocalKisaanColors.current.accent else LocalKisaanColors.current.surface)
                    .border(
                        BorderStroke(1.dp, if (selected) LocalKisaanColors.current.accent else LocalKisaanColors.current.border),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelect(value) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) androidx.compose.ui.graphics.Color.White else LocalKisaanColors.current.textPrimary
                )
            }
        }
    }
}

// Minimal wrap layout so we don't depend on the experimental FlowRow API.
@Composable
private fun FlowRowSimple(content: @Composable () -> Unit) {
    Layout(content = content) { measurables, constraints ->
        val maxWidth = constraints.maxWidth
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }
        var x = 0
        var y = 0
        var rowHeight = 0
        val positions = ArrayList<Pair<Int, Int>>(placeables.size)
        placeables.forEach { p ->
            if (x + p.width > maxWidth) {
                x = 0
                y += rowHeight
                rowHeight = 0
            }
            positions.add(x to y)
            x += p.width
            rowHeight = maxOf(rowHeight, p.height)
        }
        val totalHeight = y + rowHeight
        layout(maxWidth, totalHeight) {
            placeables.forEachIndexed { i, p ->
                p.placeRelative(positions[i].first, positions[i].second)
            }
        }
    }
}
