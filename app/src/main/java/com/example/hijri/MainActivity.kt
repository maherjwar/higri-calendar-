package com.example.hijri

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

private val GREEN = Color(0xFF1B6B4A)
private val GOLD = Color(0xFFC8962E)

private val monthNames = listOf(
    "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
    "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
)
private val weekDays = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

// المفتاح: (الشهر الهجري, اليوم)
private val occasions = mapOf(
    (1 to 1) to "رأس السنة الهجرية",
    (1 to 10) to "يوم عاشوراء",
    (3 to 12) to "المولد النبوي الشريف",
    (7 to 27) to "الإسراء والمعراج",
    (8 to 15) to "ليلة النصف من شعبان",
    (9 to 1) to "أول يوم من رمضان",
    (9 to 27) to "ليلة القدر (الأرجح)",
    (10 to 1) to "عيد الفطر",
    (12 to 8) to "يوم التروية",
    (12 to 9) to "يوم عرفة",
    (12 to 10) to "عيد الأضحى",
    (12 to 11) to "أول أيام التشريق",
    (12 to 12) to "ثاني أيام التشريق",
    (12 to 13) to "ثالث أيام التشريق",
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme(colorScheme = lightColorScheme(primary = GREEN)) {
                    Surface(Modifier.fillMaxSize()) { HijriApp() }
                }
            }
        }
    }
}

@Composable
fun HijriApp() {
    val today = remember { HijrahDate.from(LocalDate.now()) }
    var year by remember { mutableIntStateOf(today.get(ChronoField.YEAR)) }
    var month by remember { mutableIntStateOf(today.get(ChronoField.MONTH_OF_YEAR)) }

    val first = HijrahDate.of(year, month, 1)
    val length = first.lengthOfMonth()
    val offset = (first.get(ChronoField.DAY_OF_WEEK) + 1) % 7 // الأسبوع يبدأ بالسبت
    val cells: List<Int?> = List(offset) { null } + (1..length).toList()
    val isCurrentMonth = year == today.get(ChronoField.YEAR) && month == today.get(ChronoField.MONTH_OF_YEAR)
    val todayDay = today.get(ChronoField.DAY_OF_MONTH)

    fun shift(delta: Int) {
        var m = month + delta
        var y = year
        if (m > 12) { m = 1; y++ } else if (m < 1) { m = 12; y-- }
        if (y in 1300..1600) { month = m; year = y }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
            .statusBarsPadding()
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { shift(-1) }) { Text("›", fontSize = 28.sp) }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${monthNames[month - 1]} $year هـ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GREEN)
                val g1 = LocalDate.from(first)
                val g2 = g1.plusDays(length - 1L)
                Text("${g1.month.value}/${g1.year} – ${g2.month.value}/${g2.year} م", fontSize = 12.sp, color = Color.Gray)
            }
            TextButton(onClick = { shift(1) }) { Text("‹", fontSize = 28.sp) }
        }
        TextButton(
            onClick = { year = today.get(ChronoField.YEAR); month = today.get(ChronoField.MONTH_OF_YEAR) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) { Text("اليوم") }

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            weekDays.forEach {
                Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GREEN)
            }
        }
        Spacer(Modifier.height(4.dp))

        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                for (i in 0 until 7) {
                    val d = week.getOrNull(i)
                    Box(Modifier.weight(1f).height(52.dp), contentAlignment = Alignment.Center) {
                        if (d != null) {
                            val isToday = isCurrentMonth && d == todayDay
                            val special = occasions.containsKey(month to d)
                            val greg = LocalDate.from(HijrahDate.of(year, month, d)).dayOfMonth
                            Column(
                                Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
                                    .background(if (isToday) GREEN else if (special) GOLD.copy(alpha = 0.18f) else Color.Transparent),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("$d", fontSize = 17.sp, fontWeight = FontWeight.Bold,
                                    color = if (isToday) Color.White else Color.Black)
                                Text("$greg", fontSize = 10.sp,
                                    color = if (isToday) Color.White.copy(alpha = 0.8f) else Color.Gray)
                                if (special) Box(Modifier.size(5.dp).clip(CircleShape).background(if (isToday) Color.White else GOLD))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("مناسبات الشهر", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GREEN)
        Spacer(Modifier.height(6.dp))
        val monthEvents = (1..length).mapNotNull { d -> occasions[month to d]?.let { d to it } }
        if (monthEvents.isEmpty()) {
            Text("لا توجد مناسبات مسجّلة لهذا الشهر", color = Color.Gray, fontSize = 13.sp)
        } else {
            monthEvents.forEach { (d, name) ->
                val g = LocalDate.from(HijrahDate.of(year, month, d))
                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(GOLD))
                    Spacer(Modifier.width(10.dp))
                    Text(name, Modifier.weight(1f), fontSize = 15.sp)
                    Text("$d ${monthNames[month - 1]} • ${g.dayOfMonth}/${g.monthValue}", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}
