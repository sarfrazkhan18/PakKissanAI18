package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CropCalendar
import com.example.data.UserProfile
import com.example.ui.theme.LocalKisaanColors

private val cropNameUr = mapOf(
    "Wheat" to "گندم", "Rice" to "چاول", "Cotton" to "کپاس",
    "Sugarcane" to "گنا", "Maize" to "مکئی"
)

/**
 * Compact "this week" card for the home (P2.6). Shows the crop's current stage + due action,
 * computed from crop + sowing date. Renders nothing when there's no calendar/sowing date, so it
 * only appears once the farmer has filled in My Farm. Tapping opens the full calendar.
 */
@Composable
fun CropStageCard(profile: UserProfile?, onOpen: () -> Unit) {
    val status = profile?.let { CropCalendar.status(it.primaryCrop, it.sowingDateMillis) } ?: return
    val cropUr = cropNameUr[status.cropKey] ?: status.cropKey
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(LocalKisaanColors.current.accent.copy(alpha = 0.10f))
            .border(BorderStroke(1.dp, LocalKisaanColors.current.accent.copy(alpha = 0.4f)), RoundedCornerShape(14.dp))
            .clickable(onClick = onOpen)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(LocalKisaanColors.current.accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = LocalKisaanColors.current.accent, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "$cropUr — ${status.currentStage.nameUr} (${status.daysSinceSowing} دن)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = LocalKisaanColors.current.textHeading
            )
            Text(
                "👉 ${status.currentStage.actionUr}",
                fontSize = 12.sp,
                lineHeight = 17.sp,
                maxLines = 2,
                color = LocalKisaanColors.current.textPrimary.copy(alpha = 0.85f)
            )
        }
        Icon(Icons.Default.ChevronLeft, contentDescription = "کھولیں", tint = LocalKisaanColors.current.accent, modifier = Modifier.size(20.dp))
    }
}

/**
 * Full crop-stage timeline (P2.6). Shows where the farmer's crop is today (from crop + sowing
 * date), the action due now, and the whole season laid out stage by stage.
 */
@Composable
fun CropCalendarScreen(
    profile: UserProfile?,
    onOpenMyFarm: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = profile?.let { CropCalendar.status(it.primaryCrop, it.sowingDateMillis) }

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
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = LocalKisaanColors.current.accent)
                Text("فصل کیلنڈر • Crop Calendar", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = LocalKisaanColors.current.textHeading)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "بند کریں", tint = LocalKisaanColors.current.textPrimary)
            }
        }

        if (status == null) {
            // No crop calendar available — prompt to complete My Farm.
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Agriculture, contentDescription = null, tint = LocalKisaanColors.current.accent, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text(
                    "کیلنڈر دیکھنے کے لیے اپنی فصل اور کاشت کی تاریخ درج کریں۔",
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    color = LocalKisaanColors.current.textPrimary
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onOpenMyFarm,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalKisaanColors.current.accent)
                ) {
                    Text("میرا کھیت کھولیں", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                }
            }
        } else {

        val cropUr = cropNameUr[status.cropKey] ?: status.cropKey
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Current-stage highlight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(LocalKisaanColors.current.accent.copy(alpha = 0.12f))
                    .border(BorderStroke(1.dp, LocalKisaanColors.current.accent.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "$cropUr — کاشت کو ${status.daysSinceSowing} دن ہو گئے",
                    fontSize = 13.sp,
                    color = LocalKisaanColors.current.textPrimary.copy(alpha = 0.8f)
                )
                Text(
                    "موجودہ مرحلہ: ${status.currentStage.nameUr}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalKisaanColors.current.textHeading
                )
                Text(
                    "👉 ${status.currentStage.actionUr}",
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = LocalKisaanColors.current.textPrimary
                )
            }

            Text("پورا موسم • Full season", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LocalKisaanColors.current.textHeading)

            // Timeline of all stages
            status.stages.forEachIndexed { index, stage ->
                val isCurrent = index == status.stageIndex
                val isPast = index < status.stageIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isCurrent) LocalKisaanColors.current.accent.copy(alpha = 0.10f) else LocalKisaanColors.current.surface)
                        .border(
                            BorderStroke(1.dp, if (isCurrent) LocalKisaanColors.current.accent else LocalKisaanColors.current.border),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCurrent -> LocalKisaanColors.current.accent
                                    isPast -> LocalKisaanColors.current.accent.copy(alpha = 0.35f)
                                    else -> LocalKisaanColors.current.border
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPast) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(15.dp))
                        } else {
                            Text("${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${stage.nameUr}  (${stage.nameEn})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalKisaanColors.current.textPrimary
                        )
                        Text(
                            "دن ${stage.startDay}–${stage.endDay}",
                            fontSize = 11.sp,
                            color = LocalKisaanColors.current.textPrimary.copy(alpha = 0.5f)
                        )
                        Text(
                            stage.actionUr,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            color = LocalKisaanColors.current.textPrimary.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Text(
                "یہ مراحل عمومی رہنمائی ہیں؛ ضلع اور قسم کے مطابق آگے پیچھے ہو سکتے ہیں۔ اہم فیصلے مقامی ماہر سے تصدیق کر کے کریں۔",
                fontSize = 11.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                color = LocalKisaanColors.current.textPrimary.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp)
            )
        }
        }
    }
}
