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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

@Composable
fun FleetScreen(b: Booking, onBack: () -> Unit, onNext: () -> Unit) {
    var filter by remember { mutableIntStateOf(0) }
    Screen("Detail Motor", onBack, bottom = {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Total (1 Unit, ${b.days} Hari)", color = Muted, fontSize = 12.sp)
            Text(rp(b.total), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Brand)
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton("Lanjut Atur Jadwal & Titik Antar", onNext)
    }) {
        StepHeader(1, "Unit Armada")
        Card2(color = BrandLight) {
            Text("${b.fmt(b.startMs, "d MMM")} - ${b.fmt(b.endMs, "d MMM yyyy")} (${b.days} Hari)", fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text("Stasiun Tugu Jogja", color = Muted, fontSize = 12.sp)
        }
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Semua", "Scoopy & Fazzio", "Matic 125cc", "Maxi Scooter").forEachIndexed { i, t ->
                FilterChip(filter == i, { filter = i }, { Text(t) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Brand, selectedLabelColor = Color.White))
            }
        }
        Bikes.forEach { bike ->
            val sel = bike == b.bike
            Card2(border = if (sel) Brand else null, color = if (sel) BrandLight else Color.White,
                modifier = Modifier.padding(bottom = 12.dp).clickable { b.bike = bike }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(sel, { b.bike = bike }, colors = RadioButtonDefaults.colors(selectedColor = Brand))
                    Column {
                        Text(bike.name, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) { Pill(bike.tag); Spacer(Modifier.width(6.dp)); Text(bike.sub, fontSize = 11.sp, color = Muted) }
                    }
                }
                Row(Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    BikePhoto(bike, Modifier.size(100.dp, 90.dp))
                    Spacer(Modifier.width(12.dp))
                    Column { bike.specs.forEach { Text("• $it", fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp)) } }
                }
                Text("Gratis 2 Helm SNI + 2 Jas Hujan + Holder HP", fontSize = 11.sp, color = Green)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column { Text("Harga Harian", fontSize = 10.sp, color = Muted); Text("${rp(bike.price)}/hari", fontWeight = FontWeight.Bold, color = Brand) }
                    Column(horizontalAlignment = Alignment.End) { Text("Total ${b.days} Hari", fontSize = 10.sp, color = Muted); Text(rp(bike.price * b.days), fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun ScheduleScreen(b: Booking, onBack: () -> Unit, onNext: () -> Unit) {
    val hours = listOf("08.00" to "2 slot", "09.00" to "Terpilih", "10.00" to "3 slot", "11.00" to "1 slot",
        "13.00" to "4 slot", "14.00" to "2 slot", "15.00" to "Penuh", "16.00" to "3 slot")
    var note by remember { mutableStateOf("") }
    Screen("Detail Motor", onBack, bottom = {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Durasi Rental: ${b.days} Hari (${b.days * 24} Jam)", fontSize = 12.sp, color = Muted)
            Text("Estimasi ${rp(b.total)}", fontWeight = FontWeight.Bold, color = Brand)
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton("Lanjut ke Konfirmasi Sewa", onNext)
    }) {
        StepHeader(2, "Jadwal & Antar Unit")
        Card2 {
            Text(b.bike.name + " (2024)", fontWeight = FontWeight.Bold)
            Text("${b.bike.plate} • Keyless & Helm 2", fontSize = 12.sp, color = Muted)
        }
        Text("Tanggal Sewa", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateField("Tanggal Mulai", b.startMs, Modifier.weight(1f), todayMs()) { b.setStart(it) }
            DateField("Tanggal Selesai", b.endMs, Modifier.weight(1f), b.startMs + DAY) { b.endMs = it }
        }
        Text("Durasi sewa: ${b.days} hari", fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 6.dp))
        Text("Pilih Jam Antar", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        hours.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (h, s) ->
                    val full = s == "Penuh"; val sel = b.hour == h
                    Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (sel) Brand else BlueLight)
                        .clickable(enabled = !full) { b.hour = h }.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(h, fontWeight = FontWeight.Bold, color = if (sel) Color.White else if (full) Muted else Ink)
                        Text(if (sel) "Terpilih" else s, fontSize = 10.sp, color = if (sel) Color.White else Muted)
                    }
                }
            }
        }
        TimeField("Jam Selesai (Pengembalian)", b.endHour, Modifier.fillMaxWidth()) { b.endHour = it }
        Text("Titik Pengantaran Jogja", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
        b.spots.forEachIndexed { i, s ->
            Card2(border = if (b.spot == i) Brand else null, color = if (b.spot == i) BrandLight else Soft,
                modifier = Modifier.padding(bottom = 8.dp).clickable { b.spot = i }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(b.spot == i, { b.spot = i }, colors = RadioButtonDefaults.colors(selectedColor = Brand))
                    Text(s, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Pill(if (i == 2) "Gratis < 5 km" else "Gratis Ongkir")
                }
            }
        }
        LabeledField("Detail Patokan & Nomor Kontak Penjemputan", note, { note = it }, "Pintu Selatan Stasiun Tugu (Dekat Kopi Jos)")
        Spacer(Modifier.height(12.dp))
        Card2(color = BlueLight) { Text("Gratis biaya pengantaran unit untuk area Stasiun Tugu, Lempuyangan, dan pusat Malioboro! Motor tiba 15 menit lebih awal.", fontSize = 12.sp, color = Green) }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun ConfirmScreen(b: Booking, onBack: () -> Unit, onPay: () -> Unit) {
    var note by remember { mutableStateOf("") }
    Screen("Konfirmasi Sewa", onBack, bottom = {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Text("Total Bayar", fontSize = 11.sp, color = Muted); Text(rp(b.total), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Brand) }
            Text("Enkripsi Aman 256-bit", fontSize = 11.sp, color = Green)
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton("Konfirmasi & Bayar Sekarang", { b.confirm(); onPay() })
    }) {
        StepHeader(3, "Konfirmasi & Pembayaran")
        Card2(color = BrandLight) {
            Row(verticalAlignment = Alignment.CenterVertically) { Text(b.bike.name, fontWeight = FontWeight.Bold); Spacer(Modifier.width(8.dp)); Pill("2024") }
            Text("Plat Nomor: ${b.bike.plate}", fontSize = 12.sp, color = Muted)
        }
        Spacer(Modifier.height(8.dp))
        KV("Jadwal Sewa", "${b.startLabel} – ${b.endLabel}")
        KV("Total Durasi", "${b.days} Hari (${b.days * 24} Jam)")
        KV("Jam Pengantaran", "${b.hour} WIB")
        KV("Jam Pengembalian", "${b.endHour} WIB")
        KV("Titik Antar", b.spots[b.spot])
        Text("Rincian Biaya", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
        KV("Sewa ${b.bike.name.split(" ").take(2).joinToString(" ")} (${b.days} x ${rp(b.bike.price)})", rp(b.total))
        KV("2 Helm SNI + 2 Jas Hujan", "Rp 0 (Gratis)", Green)
        KV("Phone Holder Stang Waterproof", "Rp 0 (Gratis)", Green)
        KV("Antar-jemput Stasiun Kota", "Rp 0 (Gratis)", Green)
        KV("Garansi Sanitasi & Kunci Ganda", "Termasuk")
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        KV("Total Terbayar", rp(b.total), Brand)
        Text("Metode Pembayaran", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        listOf("QRIS Instant" to "Gopay, OVO, Dana, BCA Mobile, ShopeePay", "Transfer Virtual Account" to "BCA, Mandiri, BNI, BRI (Bebas Biaya Admin)").forEachIndexed { i, (t, s) ->
            Card2(border = if (b.pay == i) Brand else null, color = if (b.pay == i) BrandLight else BlueLight,
                modifier = Modifier.padding(bottom = 8.dp).clickable { b.pay = i }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(b.pay == i, { b.pay = i }, colors = RadioButtonDefaults.colors(selectedColor = Brand))
                    Column { Text(t, fontWeight = FontWeight.Bold); Text(s, fontSize = 12.sp, color = Muted) }
                }
            }
        }
        LabeledField("Catatan untuk Runner (opsional)", note, { note = it }, "Contoh: Saya naik KA Argo Dwipangga tiba jam 09.00")
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun SuccessScreen(b: Booking, onTicket: () -> Unit, onHome: () -> Unit) {
    val ctx = LocalContext.current
    Screen("Status Pengantaran", null) {
        Spacer(Modifier.height(24.dp))
        Box(Modifier.size(80.dp).clip(CircleShape).background(Green).align(Alignment.CenterHorizontally), Alignment.Center) {
            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(44.dp))
        }
        Text("Booking Berhasil!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
        Text("Konfirmasi pemesanan sewa motor telah dikirim ke WhatsApp dan email Anda.", color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        Card2(color = BlueLight) {
            Text("Kode Booking Sewa", fontSize = 12.sp, color = Muted, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(b.lastCode, fontSize = 22.sp)
            KV("Jadwal Pengantaran", "${b.fmt(b.startMs, "EEE, d MMM")} • ${b.hour} WIB")
            KV("Lokasi Serah Terima", b.spots[b.spot])
            KV("Unit Motor", "${b.bike.name} (${b.bike.plate})")
            KV("Total Terbayar", rp(b.total), Brand)
        }
        Spacer(Modifier.height(12.dp))
        Card2(color = Soft) {
            Text("Pengingat Serah Terima", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("Tim runner JogjaRide akan standby di lokasi 15 menit sebelum jam serah terima. Siapkan KTP/SIM C asli untuk verifikasi fisik cepat.", fontSize = 12.sp, color = Muted)
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton("Hubungi Runner via WhatsApp", { openWhatsApp(ctx, "Halo JogjaRide, saya ingin konfirmasi booking ${b.lastCode}") }, arrow = false)
        Spacer(Modifier.height(8.dp))
        Button(onTicket, Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandLight, contentColor = Brand)) { Text("Lihat Tiket & E-Kontrak", fontWeight = FontWeight.Bold) }
        TextButton(onHome, Modifier.align(Alignment.CenterHorizontally)) { Text("Kembali ke Beranda", color = Muted) }
    }
}