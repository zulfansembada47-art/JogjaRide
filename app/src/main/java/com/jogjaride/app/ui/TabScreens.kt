package com.jogjaride.app.ui
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.jogjaride.app.data.AuthManager

@Composable
fun ArmadaTab(b: Booking, onSearch: () -> Unit) {
    Text("Armada Kami", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
    Text("Semua unit sudah termasuk 2 helm SNI, jas hujan, dan holder HP.", color = Muted, fontSize = 13.sp)
    Spacer(Modifier.height(16.dp))
    Bikes.forEach { bike ->
        Card2(modifier = Modifier.padding(bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BikePhoto(bike, Modifier.size(84.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(bike.name, fontWeight = FontWeight.Bold)
                    Text(bike.sub, fontSize = 12.sp, color = Muted)
                    Spacer(Modifier.height(4.dp))
                    Pill(bike.tag)
                    Spacer(Modifier.height(4.dp))
                    Text("${rp(bike.price)}/hari", fontWeight = FontWeight.Bold, color = Brand)
                }
            }
            Spacer(Modifier.height(10.dp))
            bike.specs.forEach { Text("• $it", fontSize = 12.sp, modifier = Modifier.padding(vertical = 1.dp)) }
            Spacer(Modifier.height(10.dp))
            PrimaryButton("Sewa Sekarang", { b.bike = bike; b.editing = null; onSearch() })
        }
    }
}

@Composable
fun PesananTab(b: Booking, onOpen: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    val all = b.bookings.toList()
    val list = if (tab == 0) all.filter { !it.isDone } else all.filter { it.isDone }
    Text("Pesanan Saya", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Aktif", "Riwayat").forEachIndexed { i, t ->
            FilterChip(tab == i, { tab = i }, { Text(t) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Brand, selectedLabelColor = Color.White))
        }
    }
    Spacer(Modifier.height(12.dp))
    if (list.isEmpty()) {
        Text(if (tab == 0) "Belum ada pesanan aktif." else "Belum ada riwayat pesanan.",
            color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp))
    }
    list.forEach { o ->
        Card2(modifier = Modifier.padding(bottom = 12.dp).clickable { b.load(o); onOpen() }) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(o.code, color = Muted, fontSize = 11.sp)
                when {
                    o.isDone -> Pill("Selesai", Soft, Muted)
                    o.isActive -> Pill("Sedang Berjalan")
                    else -> Pill("Terkonfirmasi")
                }
            }
            Text(o.bike.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 6.dp))
            Text("${fmtDate(o.startMs, "EEE, d MMM")} – ${fmtDate(o.endMs, "EEE, d MMM yyyy")}", color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", color = Muted, fontSize = 12.sp); Text(rp(o.total), fontWeight = FontWeight.Bold, color = Brand)
            }
        }
    }
}

@Composable
fun ProfilTab(b: Booking, go: (String) -> Unit, onLogout: () -> Unit) {
    Text("Profil", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
    Spacer(Modifier.height(16.dp))
    Card2 {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(Brand), Alignment.Center) {
                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(b.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(b.email, color = Muted, fontSize = 12.sp)
                Text(b.phone, color = Muted, fontSize = 12.sp)
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    val routes = mapOf("Edit Profil" to "editprofile", "Notifikasi" to "notif", "Keamanan Akun" to "security", "Pengaturan" to "settings", "Bantuan" to "help")
    listOf("Edit Profil" to Icons.Default.Person, "Notifikasi" to Icons.Default.Notifications,
        "Keamanan Akun" to Icons.Default.Lock, "Pengaturan" to Icons.Default.Settings,
        "Bantuan" to Icons.Default.Info).forEach { (t, ic) ->
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { go(routes[t] ?: "home") }.padding(vertical = 14.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(ic, null, tint = Brand); Spacer(Modifier.width(14.dp)); Text(t, modifier = Modifier.weight(1f))
        }
        HorizontalDivider(color = Soft)
    }
    Spacer(Modifier.height(20.dp))
    Button({ AuthManager.logoutUser(); onLogout() }, Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandLight, contentColor = Brand)) {
        Icon(Icons.AutoMirrored.Filled.Logout, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
        Text("Keluar", fontWeight = FontWeight.Bold)
    }
}