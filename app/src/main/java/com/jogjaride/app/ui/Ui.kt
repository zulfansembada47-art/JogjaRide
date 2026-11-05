package com.jogjaride.app.ui
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

val Brand = Color(0xFFA63A08)
val BrandLight = Color(0xFFFDEBE1)
val Bg = Color(0xFFFFFBF8)
val Ink = Color(0xFF1F1A17)
val Muted = Color(0xFF7A6F69)
val Soft = Color(0xFFF4F1EE)
val Green = Color(0xFF1E9E5A)
val GreenLight = Color(0xFFD5F5E3)
val BlueLight = Color(0xFFE8EDFB)

@Composable
fun JogjaTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = lightColorScheme(primary = Brand, background = Bg, surface = Color.White, onSurface = Ink),
    content = content
)

fun rp(n: Int) = "Rp " + "%,d".format(n).replace(',', '.')

data class Bike(val name: String, val sub: String, val tag: String, val specs: List<String>, val price: Int, val plate: String, val photo: String)

val Bikes = listOf(
    Bike("Honda Scoopy Prestige", "Tahun 2024 • Smart Key", "Favorit Wisatawan",
        listOf("Matic 110cc", "Keyless System", "Port Charger HP"), 85000, "AB 4812 XY", "scoopy"),
    Bike("Yamaha Fazzio Neo Hybrid", "Matic 125cc • Retro Modern", "Super Irit",
        listOf("Blue Core Hybrid", "Bagasi 17.8L", "Smart Lock Key"), 95000, "AB 3321 KA", "fazzio"),
    Bike("Honda Vario 125cc eSP", "Matic 125cc • Tangguh", "Tanjakan Kaliurang",
        listOf("Mesin 125cc eSP", "ISS (Idling Stop)", "Bagasi Muat Helm"), 80000, "AB 5540 ZZ", "vario")
)

class Booking {
    var bike by mutableStateOf(Bikes[0])
    var startMs by mutableLongStateOf(todayMs())
    var endMs by mutableLongStateOf(todayMs() + 2 * DAY)
    var hour by mutableStateOf("09.00")
    var endHour by mutableStateOf("18.00")
    val bookings = mutableStateListOf<BookingItem>()
    var lastCode by mutableStateOf("JOG-241026-088")
    var editing by mutableStateOf<String?>(null)
    fun confirm() {
        val code = editing ?: ("JOG-" + fmtDate(todayMs(), "yyMMdd") + "-" + "%03d".format(bookings.size + 1))
        val item = BookingItem(code, bike, startMs, endMs, hour, endHour, spots[spot], total)
        val i = bookings.indexOfFirst { it.code == code }
        if (i >= 0) bookings[i] = item else bookings.add(item)
        lastCode = code; editing = null
    }
    fun load(i: BookingItem) {
        bike = i.bike; startMs = i.startMs; endMs = i.endMs; hour = i.hour; endHour = i.endHour
        spot = spots.indexOf(i.spot).coerceAtLeast(0); lastCode = i.code
    }
    fun startEdit(i: BookingItem) { load(i); editing = i.code }
    var spot by mutableIntStateOf(0)
    var pay by mutableIntStateOf(0)
    val days get() = maxOf(1, ((endMs - startMs) / DAY).toInt())
    var name by mutableStateOf("Zulfan Cahya")
    var email by mutableStateOf("zulfan@email.com")
    var phone by mutableStateOf("+62 812-3456-7890")
    var role by mutableStateOf("user")
    fun fmt(ms: Long, p: String) = fmtDate(ms, p)
    fun setStart(ms: Long) { startMs = ms; if (endMs <= ms) endMs = ms + DAY }
    val startLabel get() = fmtDate(startMs, "EEEE, d MMM yyyy")
    val endLabel get() = fmtDate(endMs, "EEEE, d MMM yyyy")
    val total get() = bike.price * days
    val spots = listOf("Stasiun Tugu Yogyakarta", "Stasiun Lempuyangan", "Hotel / Homestay Kota")
}

@Composable
fun TopBar(title: String, onBack: (() -> Unit)?) {
    Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Kembali") }
        Box(Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(Brand), Alignment.Center) {
            Icon(Icons.Default.TwoWheeler, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(32.dp).clip(CircleShape).background(Brand), Alignment.Center) {
            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun Screen(
    title: String, onBack: (() -> Unit)?,
    bottom: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize().background(Bg)) {
        TopBar(title, onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), content = content)
        if (bottom != null) Surface(shadowElevation = 8.dp) {
            Column(Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding()) { bottom() }
        }
    }
}

@Composable
fun Card2(modifier: Modifier = Modifier, color: Color = Color.White, border: Color? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = color,
        border = border?.let { BorderStroke(1.5.dp, it) }, shadowElevation = if (border == null) 1.dp else 0.dp) {
        Column(Modifier.padding(14.dp), content = content)
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, arrow: Boolean = true, enabled: Boolean = true) {
    Button(onClick, Modifier.fillMaxWidth().height(52.dp), enabled = enabled, shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Brand, disabledContainerColor = Brand.copy(alpha = 0.5f), disabledContentColor = Color.White)) {
        Text(text, fontWeight = FontWeight.Bold)
        if (arrow) { Spacer(Modifier.width(8.dp)); Icon(Icons.Default.ArrowForward, null, Modifier.size(18.dp)) }
    }
}

@Composable
fun Pill(text: String, bg: Color = GreenLight, fg: Color = Green) {
    Text(text, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp))
}

@Composable
fun KV(k: String, v: String, vColor: Color = Ink) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(k, color = Muted, fontSize = 13.sp)
        Text(v, color = vColor, fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.End, modifier = Modifier.weight(1f).padding(start = 16.dp))
    }
}

@Composable
fun InfoBanner(title: String, sub: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(BrandLight).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Shield, null, tint = Brand)
        Spacer(Modifier.width(10.dp))
        Column { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp); Text(sub, fontSize = 12.sp, color = Muted) }
    }
}

@Composable
fun LabeledField(label: String, value: String, onChange: (String) -> Unit, hint: String,
                 icon: ImageVector? = null, password: Boolean = false, keyboard: KeyboardType = KeyboardType.Text) {
    var hidden by remember { mutableStateOf(password) }
    Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 14.dp, bottom = 6.dp))
    OutlinedTextField(
        value, onChange, Modifier.fillMaxWidth(),
        placeholder = { Text(hint, color = Muted) }, singleLine = true, shape = RoundedCornerShape(14.dp),
        visualTransformation = if (hidden) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        trailingIcon = {
            if (password) IconButton({ hidden = !hidden }) {
                Icon(if (hidden) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
            } else if (icon != null) Icon(icon, null, tint = Muted)
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Soft, focusedContainerColor = Color.White,
            unfocusedBorderColor = Color.Transparent, focusedBorderColor = Brand)
    )
}

@Composable
fun StepHeader(step: Int, label: String) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("LANGKAH $step DARI 3", color = Brand, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Muted, fontSize = 11.sp)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(progress = { step / 3f }, Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = Brand, trackColor = Soft)
    }
}

@Composable
fun BikePhoto(bike: Bike, modifier: Modifier) {
    val ctx = LocalContext.current
    val id = remember(bike.photo) { ctx.resources.getIdentifier(bike.photo, "drawable", ctx.packageName) }
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(Soft), Alignment.Center) {
        if (id != 0) Image(painterResource(id), bike.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Icon(Icons.Default.TwoWheeler, null, Modifier.size(44.dp), tint = Brand)
    }
}

const val DAY = 86_400_000L

fun fmtDate(ms: Long, p: String): String =
    SimpleDateFormat(p, Locale("id", "ID")).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(ms))

fun todayMs(): Long {
    val c = Calendar.getInstance()
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear(); set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(label: String, millis: Long, modifier: Modifier = Modifier, min: Long = 0L, onPick: (Long) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(BlueLight).clickable { open = true }.padding(12.dp)) {
        Text(label, fontSize = 10.sp, color = Muted)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(fmtDate(millis, "d MMM yyyy"), fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.CalendarMonth, null, Modifier.size(16.dp), tint = Brand)
        }
    }
    if (open) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = millis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= min
            }
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = { TextButton({ state.selectedDateMillis?.let(onPick); open = false }) { Text("Pilih", color = Brand) } },
            dismissButton = { TextButton({ open = false }) { Text("Batal", color = Muted) } }
        ) { DatePicker(state = state) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(label: String, value: String, modifier: Modifier = Modifier, onPick: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(BlueLight).clickable { open = true }.padding(12.dp)) {
        Text(label, fontSize = 10.sp, color = Muted)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$value WIB", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.Schedule, null, Modifier.size(16.dp), tint = Brand)
        }
    }
    if (open) {
        val parts = value.split(".")
        val state = rememberTimePickerState(parts.getOrNull(0)?.toIntOrNull() ?: 9, parts.getOrNull(1)?.toIntOrNull() ?: 0, true)
        Dialog({ open = false }, DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pilih Jam", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                    TimePicker(state = state)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton({ open = false }) { Text("Batal", color = Muted) }
                        TextButton({ onPick("%02d.%02d".format(state.hour, state.minute)); open = false }) { Text("Pilih", color = Brand) }
                    }
                }
            }
        }
    }
}

data class BookingItem(val code: String, val bike: Bike, val startMs: Long, val endMs: Long,
                       val hour: String, val endHour: String, val spot: String, val total: Int) {
    val startAt get() = startMs + minutesOf(hour)
    val endAt get() = endMs + minutesOf(endHour)
    val isActive get() = nowMs() in startAt until endAt
    val isUpcoming get() = startAt > nowMs()
    val isDone get() = endAt <= nowMs()
}

fun minutesOf(h: String): Long {
    val p = h.split(".")
    return ((p.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (p.getOrNull(1)?.toIntOrNull() ?: 0)) * 60000L
}

fun nowMs(): Long {
    val c = Calendar.getInstance()
    return todayMs() + (c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)) * 60000L
}
