package com.jogjaride.app.ui
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

fun toast(ctx: Context, s: String) = Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show()

fun openWhatsApp(ctx: Context, msg: String) {
    try {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/6281234567890?text=" + Uri.encode(msg))))
    } catch (e: Exception) { toast(ctx, "WhatsApp tidak tersedia di perangkat ini") }
}

@Composable
fun ForgotScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var email by remember { mutableStateOf("") }
    Screen("Lupa Kata Sandi", onBack) {
        Spacer(Modifier.height(16.dp))
        Text("Atur Ulang Kata Sandi", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text("Masukkan email atau nomor WhatsApp yang terdaftar. Kami akan mengirim tautan untuk membuat kata sandi baru.", color = Muted, fontSize = 13.sp)
        LabeledField("Email atau Nomor WhatsApp", email, { email = it }, "nama@email.com", Icons.Default.Email)
        Spacer(Modifier.height(20.dp))
        PrimaryButton("Kirim Tautan Reset", {
            if (!email.contains("@")) toast(ctx, "Isi email yang terdaftar")
            else AuthRepo.resetPassword(email.trim()) { e ->
                if (e == null) { toast(ctx, "Tautan reset dikirim ke email Anda"); onBack() } else toast(ctx, e)
            }
        }, arrow = false)
    }
}

@Composable
fun TicketScreen(b: Booking, onBack: () -> Unit) {
    val ctx = LocalContext.current
    Screen("E-Tiket & E-Kontrak", onBack, bottom = {
        PrimaryButton("Hubungi Runner via WhatsApp", { openWhatsApp(ctx, "Halo JogjaRide, saya ingin konfirmasi booking ${b.lastCode}") }, arrow = false)
    }) {
        Spacer(Modifier.height(8.dp))
        Card2(color = BlueLight) {
            Text("Kode Booking Sewa", fontSize = 12.sp, color = Muted, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(b.lastCode, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Brand,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 6.dp))
            Box(Modifier.size(150.dp).align(Alignment.CenterHorizontally).clip(RoundedCornerShape(12.dp)).background(Color.White), Alignment.Center) {
                Icon(Icons.Default.QrCode2, null, Modifier.size(130.dp))
            }
            Text("Tunjukkan kode QR ini kepada runner saat serah terima", fontSize = 11.sp, color = Muted, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        }
        Spacer(Modifier.height(12.dp))
        KV("Unit Motor", b.bike.name)
        KV("Plat Nomor", b.bike.plate)
        KV("Jadwal Sewa", "${b.startLabel} – ${b.endLabel}")
        KV("Jam Pengantaran", "${b.hour} WIB")
        KV("Jam Pengembalian", "${b.endHour} WIB")
        KV("Titik Antar", b.spots[b.spot])
        KV("Durasi", "${b.days} Hari")
        KV("Total Terbayar", rp(b.total), Brand)
        Text("E-Kontrak Sewa", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        Card2(color = Soft) {
            listOf("Penyewa wajib menunjukkan KTP/SIM C asli saat serah terima.",
                "Motor digunakan hanya oleh penyewa dan tidak boleh disewakan kembali.",
                "Keterlambatan pengembalian dikenakan biaya per jam.",
                "Kerusakan atau kehilangan akibat kelalaian penyewa menjadi tanggung jawab penyewa.",
                "Bahan bakar dikembalikan seperti kondisi awal.").forEachIndexed { i, t ->
                Text("${i + 1}. $t", fontSize = 12.sp, modifier = Modifier.padding(vertical = 3.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun NotifScreen(onBack: () -> Unit) {
    val items = listOf(
        Triple("Booking Terkonfirmasi", "Pesanan JOG-241026-088 sudah dikonfirmasi. Runner siap mengantar.", "Baru saja"),
        Triple("Pembayaran Berhasil", "Pembayaran sebesar Rp 170.000 sudah kami terima.", "10 menit lalu"),
        Triple("Pengingat Serah Terima", "Siapkan KTP/SIM C asli untuk verifikasi saat motor tiba.", "1 jam lalu"),
        Triple("Promo Akhir Pekan", "Diskon 10% untuk sewa 3 hari. Berlaku sampai Minggu.", "Kemarin"))
    Screen("Notifikasi", onBack) {
        Spacer(Modifier.height(8.dp))
        items.forEach { (t, d, w) ->
            Card2(modifier = Modifier.padding(bottom = 10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(t, fontWeight = FontWeight.Bold); Text(w, fontSize = 11.sp, color = Muted)
                }
                Text(d, fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun EditProfileScreen(b: Booking, onBack: () -> Unit) {
    val ctx = LocalContext.current
    var name by remember { mutableStateOf(b.name) }
    var email by remember { mutableStateOf(b.email) }
    var phone by remember { mutableStateOf(b.phone) }
    Screen("Edit Profil", onBack, bottom = {
        PrimaryButton("Simpan Perubahan", {
            if (name.isBlank()) toast(ctx, "Nama tidak boleh kosong")
            else { b.name = name.trim(); b.phone = phone.trim(); AuthRepo.saveProfile(b); toast(ctx, "Profil disimpan"); onBack() }
        }, arrow = false)
    }) {
        Spacer(Modifier.height(16.dp))
        Box(Modifier.size(72.dp).clip(CircleShape).background(Brand).align(Alignment.CenterHorizontally), Alignment.Center) {
            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(40.dp))
        }
        LabeledField("Nama Lengkap", name, { name = it }, "Nama lengkap", Icons.Default.Badge)
        LabeledField("Email", email, { email = it }, "nama@email.com", Icons.Default.Email)
        LabeledField("Nomor WhatsApp", phone, { phone = it }, "+62 812-3456-7890", Icons.Default.Phone)
    }
}

@Composable
fun SecurityScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var old by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var conf by remember { mutableStateOf("") }
    var twoFa by remember { mutableStateOf(false) }
    Screen("Keamanan Akun", onBack, bottom = {
        PrimaryButton("Simpan Kata Sandi", {
            when {
                old.isBlank() -> toast(ctx, "Isi kata sandi lama")
                new.length < 8 -> toast(ctx, "Kata sandi baru minimal 8 karakter")
                new != conf -> toast(ctx, "Konfirmasi kata sandi tidak sama")
                else -> { toast(ctx, "Kata sandi diperbarui"); onBack() }
            }
        }, arrow = false)
    }) {
        LabeledField("Kata Sandi Lama", old, { old = it }, "Masukkan kata sandi lama", password = true)
        LabeledField("Kata Sandi Baru", new, { new = it }, "Minimal 8 karakter", password = true)
        LabeledField("Konfirmasi Kata Sandi Baru", conf, { conf = it }, "Ulangi kata sandi baru", password = true)
        Spacer(Modifier.height(12.dp))
        SwitchRow("Verifikasi 2 Langkah", "Kode OTP dikirim via WhatsApp saat masuk", twoFa) { twoFa = it }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var push by remember { mutableStateOf(true) }
    var remind by remember { mutableStateOf(true) }
    var data by remember { mutableStateOf(false) }
    Screen("Pengaturan", onBack) {
        Spacer(Modifier.height(8.dp))
        SwitchRow("Notifikasi Push", "Info booking dan promo", push) { push = it }
        SwitchRow("Pengingat Serah Terima", "Ingatkan 1 jam sebelum jadwal", remind) { remind = it }
        SwitchRow("Mode Hemat Data", "Kurangi pemuatan gambar", data) { data = it }
        Spacer(Modifier.height(12.dp))
        KV("Bahasa", "Indonesia")
        KV("Versi Aplikasi", "1.0.0")
    }
}

@Composable
fun HelpScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var open by remember { mutableIntStateOf(-1) }
    val faq = listOf(
        "Dokumen apa yang harus dibawa?" to "Siapkan KTP/SIM C asli untuk verifikasi fisik saat serah terima motor.",
        "Bagaimana jika terlambat mengembalikan?" to "Keterlambatan dikenakan biaya per jam. Hubungi runner bila perlu perpanjangan.",
        "Apakah bisa ubah jadwal?" to "Bisa, lewat tombol Ubah Jadwal di Beranda, selama unit masih tersedia.",
        "Metode pembayaran apa saja?" to "QRIS (Gopay, OVO, Dana, BCA Mobile, ShopeePay) dan Virtual Account bank.")
    Screen("Bantuan", onBack, bottom = {
        PrimaryButton("Hubungi Customer Service", { openWhatsApp(ctx, "Halo JogjaRide, saya butuh bantuan") }, arrow = false)
    }) {
        Spacer(Modifier.height(8.dp))
        faq.forEachIndexed { i, (q, a) ->
            Card2(modifier = Modifier.padding(bottom = 10.dp).clickable { open = if (open == i) -1 else i }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(q, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Icon(if (open == i) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
                }
                if (open == i) Text(a, fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Composable
private fun SwitchRow(title: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Medium); Text(sub, fontSize = 12.sp, color = Muted) }
        Switch(checked, onChange, colors = SwitchDefaults.colors(checkedTrackColor = Brand))
    }
    HorizontalDivider(color = Soft)
}
