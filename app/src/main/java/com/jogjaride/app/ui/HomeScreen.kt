package com.jogjaride.app.ui
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

@Composable
fun HomeScreen(b: Booking, go: (String) -> Unit, onLogout: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(containerColor = Bg, bottomBar = {
        NavigationBar(containerColor = Color.White) {
            listOf("Beranda" to Icons.Default.Home, "Armada" to Icons.Default.TwoWheeler,
                "Pesanan" to Icons.Default.Receipt, "Profil" to Icons.Default.Person).forEachIndexed { i, (l, ic) ->
                NavigationBarItem(tab == i, { tab = i }, { Icon(ic, l) }, label = { Text(l) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Brand, selectedTextColor = Brand, indicatorColor = BrandLight))
            }
        }
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(20.dp)) {
            when (tab) {
                0 -> HomeTab(b, go)
                1 -> ArmadaTab(b) { go("fleet") }
                2 -> PesananTab(b) { go("ticket") }
                else -> ProfilTab(b, go, onLogout)
            }
        }
    }
}

@Composable
private fun HomeTab(b: Booking, go: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(Brand), Alignment.Center) {
            Icon(Icons.Default.TwoWheeler, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column { Text("JogjaRide", fontWeight = FontWeight.Bold); Text("Yogyakarta", fontSize = 11.sp, color = Muted) }
        Spacer(Modifier.weight(1f))
        IconButton({ go("notif") }) { Icon(Icons.Default.Notifications, null) }
    }
    Spacer(Modifier.height(16.dp))
    Text("Halo, ${b.name}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
    Text("Mau jalan-jalan keliling Jogja naik apa hari ini?", color = Muted, fontSize = 13.sp)
    Spacer(Modifier.height(16.dp))
    b.bookings.toList().filter { it.isActive }.forEach { item ->
        Card2(color = BrandLight, modifier = Modifier.padding(bottom = 16.dp).clickable { b.load(item); go("ticket") }) {
            Text("SEWA AKTIF BERJALAN •", color = Brand, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("${item.bike.name} (${item.bike.plate})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Selesai ${fmtDate(item.endMs, "EEE, d MMM")}, ${item.endHour} WIB • ${item.spot}", color = Muted, fontSize = 12.sp)
        }
    }
    Card2 {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Pesan Motor Cepat", fontWeight = FontWeight.Bold); Pill("${b.days} Hari Sewa", BrandLight, Brand)
        }
        Spacer(Modifier.height(10.dp))
        Text("Titik Antar / Ambil Motor", fontSize = 12.sp, color = Muted)
        Surface(Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = RoundedCornerShape(12.dp), color = Soft) {
            Text("Stasiun Tugu (Pintu Barat / Pasar Kembang)", Modifier.padding(14.dp), fontSize = 14.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateField("Tanggal Mulai", b.startMs, Modifier.weight(1f), todayMs()) { b.setStart(it) }; TimeField("Jam Mulai", b.hour, Modifier.weight(1f)) { b.hour = it }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateField("Tanggal Selesai", b.endMs, Modifier.weight(1f), b.startMs + DAY) { b.endMs = it }; TimeField("Jam Selesai", b.endHour, Modifier.weight(1f)) { b.endHour = it }
        }
        Spacer(Modifier.height(12.dp))
        PrimaryButton("Cari Motor Tersedia", { b.editing = null; go("fleet") }, arrow = false)
    }
    val upcoming = b.bookings.toList().filter { it.isUpcoming }
    if (upcoming.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Jadwal Booking Mendatang", fontWeight = FontWeight.Bold)
            Text("${upcoming.size} Pesanan", color = Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
        upcoming.forEach { item ->
            Card2(modifier = Modifier.padding(bottom = 12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.code, color = Muted, fontSize = 11.sp); Pill("• Terkonfirmasi")
                }
                Text(item.bike.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 6.dp))
                Text("${fmtDate(item.startMs, "EEE, d MMM yyyy")} • ${item.hour} WIB", color = Muted, fontSize = 12.sp)
                Text("Kembali: ${fmtDate(item.endMs, "EEE, d MMM yyyy")} • ${item.endHour} WIB", color = Muted, fontSize = 12.sp)
                Text("Antar: ${item.spot}", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button({ b.startEdit(item); go("schedule") }, Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueLight, contentColor = Ink)) { Text("Ubah Jadwal") }
                    Button({ b.load(item); go("ticket") }, Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandLight, contentColor = Brand)) { Text("Lihat Tiket") }
                }
            }
        }
    }
}

@Composable
private fun MiniField(label: String, value: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(BlueLight).padding(10.dp)) {
        Text(label, fontSize = 10.sp, color = Muted); Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}