package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalKisaanColors
import com.example.utils.UrduDictionary
import com.example.viewmodel.LanguageOption

// Farm advice empty state pre-filled helper cards
@Composable
fun EmptyStateGuide(
    selectedLanguage: LanguageOption,
    onTopicSelected: (String, String) -> Unit,
    onMicClick: () -> Unit,
    isHandsFreeActive: Boolean,
    onHandsFreeToggle: (Boolean) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Aesthetic concentric decorative rings with centralized mic and bilingual speak prompt
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            // Elegant Concentric Web Mock Circles
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .border(BorderStroke(1.dp, LocalKisaanColors.current.accent.copy(alpha = 0.15f)), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .border(BorderStroke(1.dp, LocalKisaanColors.current.accent.copy(alpha = 0.10f)), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .border(BorderStroke(1.dp, LocalKisaanColors.current.accent.copy(alpha = 0.05f)), CircleShape)
            )

            // Inner focus content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "موبائل سے بات کریں",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = LocalKisaanColors.current.textPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "زراعت اور فصلوں کا کوئی بھی سوال پوچھیے",
                    fontSize = 12.sp,
                    color = LocalKisaanColors.current.textHeading.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Elegant physical dark circle mic button with customized drop shadowglow
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(
                            elevation = 24.dp,
                            shape = CircleShape,
                            spotColor = LocalKisaanColors.current.textHeading.copy(alpha = 0.35f)
                        )
                        .background(LocalKisaanColors.current.textHeading, CircleShape)
                        .clickable(onClick = onMicClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Speak voice query",
                        tint = LocalKisaanColors.current.background,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Decorative wave equalizer bars at the bottom
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(width = 3.dp, height = 12.dp).background(LocalKisaanColors.current.accent.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.size(width = 3.dp, height = 20.dp).background(LocalKisaanColors.current.accent.copy(alpha = 0.6f), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.size(width = 3.dp, height = 14.dp).background(LocalKisaanColors.current.accent.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                }
            }
        }

        // Six big one-tap shortcuts to the questions farmers ask most (P1.5).
        HomeShortcutGrid(onTopicSelected = onTopicSelected)

        Spacer(modifier = Modifier.height(12.dp))

        // State-driven hands-free toggler on the empty state guide
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 12.dp)
                .background(LocalKisaanColors.current.surface, RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, if (isHandsFreeActive) LocalKisaanColors.current.accent else LocalKisaanColors.current.border), RoundedCornerShape(16.dp))
                .clickable { onHandsFreeToggle(!isHandsFreeActive) }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = if (isHandsFreeActive) LocalKisaanColors.current.accent else LocalKisaanColors.current.textHeading.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = UrduDictionary.VOICE_AUTOMATIC_MODE,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalKisaanColors.current.textPrimary
                    )
                    Text(
                        text = "معاون خود بخود جواب سنائے گا اور آپ کا سوال سنے گا",
                        fontSize = 10.sp,
                        color = LocalKisaanColors.current.textPrimary.copy(alpha = 0.5f)
                    )
                }
            }
            Switch(
                checked = isHandsFreeActive,
                onCheckedChange = onHandsFreeToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = LocalKisaanColors.current.background,
                    checkedTrackColor = LocalKisaanColors.current.accent,
                    uncheckedThumbColor = LocalKisaanColors.current.textHeading.copy(alpha = 0.5f),
                    uncheckedTrackColor = LocalKisaanColors.current.surface
                ),
                modifier = Modifier.scale(0.85f).testTag("handsfree_welcome_switch")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Honest disclaimer (P2.5): the app is an assistant, not a final authority.
        Text(
            text = "کِسان دوست ایک معاون ہے۔ اہم فیصلوں اور سپرے سے پہلے مقامی زرعی ماہر سے تصدیق ضرور کریں۔",
            fontSize = 11.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            color = LocalKisaanColors.current.textPrimary.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
        )
    }
}

data class GuideItem(val title: String, val prompt: String, val category: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

data class HomeShortcut(
    val label: String,
    val prompt: String,
    val category: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// The six most-asked topics, one tap each. Big icon + Urdu label so a non-literate farmer
// can recognise them by shape/colour (P1.5). Tapping sends the prompt like a spoken question.
@Composable
fun HomeShortcutGrid(onTopicSelected: (String, String) -> Unit) {
    val shortcuts = listOf(
        HomeShortcut("پانی", "میری فصل کو پانی کب اور کتنی مقدار میں لگانا چاہیے؟", "Weather", Icons.Outlined.WaterDrop),
        HomeShortcut("کھاد", "میری فصل کے لیے کھاد کا صحیح شیڈول اور مقدار کیا ہے؟", "Crops", Icons.Outlined.Grass),
        HomeShortcut("کیڑے", "میری فصل پر کیڑوں کے حملے کا سستا اور دیسی علاج بتائیں۔", "Pest", Icons.Outlined.BugReport),
        HomeShortcut("بیماری", "میری فصل میں بیماری کی علامات اور اس کا تدارک کیا ہے؟", "Pest", Icons.Outlined.Healing),
        HomeShortcut("منڈی بھاؤ", "آج میری فصل کا منڈی بھاؤ کیا ہے؟", "Weather", Icons.Outlined.TrendingUp),
        HomeShortcut("موسم", "اگلے چند دن موسم کیسا رہے گا اور کیا سپرے کرنا چاہیے؟", "Weather", Icons.Outlined.WbSunny)
    )
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        shortcuts.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { sc ->
                    ShortcutChip(
                        shortcut = sc,
                        modifier = Modifier.weight(1f),
                        onClick = { onTopicSelected(sc.prompt, sc.category) }
                    )
                }
            }
        }
    }
}

@Composable
fun ShortcutChip(shortcut: HomeShortcut, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .heightIn(min = 88.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(LocalKisaanColors.current.surface)
            .border(BorderStroke(1.dp, LocalKisaanColors.current.border), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(LocalKisaanColors.current.accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = shortcut.icon,
                contentDescription = shortcut.label,
                tint = LocalKisaanColors.current.accent,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = shortcut.label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = LocalKisaanColors.current.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
