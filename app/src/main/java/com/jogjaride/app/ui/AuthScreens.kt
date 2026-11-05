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
import androidx.compose.ui.text.input.KeyboardType

private val ErrRed = Color(0xFFC62828)

@Composable
fun RegisterScreen(b: Booking, onDone: () -> Unit, onLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var wa by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }
    var agree by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    Screen("Daftar Akun", null) {
        Spacer(Modifier.height(8.dp))
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(Brand), Alignment.Center) {
            Icon(Icons.Default.VpnKey, null, tint = Color.White)
        }
        Text("Daftar Akun Baru", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 12.dp))
        Text("Nikmati kemudahan sewa motor tanpa ribet di Jogja", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        InfoBanner("Unit Siap Antar ke Stasiun Tugu & Lempuyangan", "Termasuk 2 helm SNI & jas hujan bersih")
        LabeledField("Nama Lengkap (Sesuai KTP / SIM C)", name, { name = it }, "Masukkan nama lengkap", Icons.Default.Badge)
        LabeledField("Nomor WhatsApp", wa, { wa = it }, "+62 812-3456-7890", Icons.Default.Phone, keyboard = KeyboardType.Phone)
        LabeledField("Email", email, { email = it }, "nama@email.com", Icons.Default.Email, keyboard = KeyboardType.Email)
        LabeledField("Kata Sandi", pass, { pass = it }, "Minimal 8 karakter", password = true)
        LabeledField("Konfirmasi Kata Sandi", pass2, { pass2 = it }, "Masukkan ulang kata sandi", password = true)
        Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(agree, { agree = it }, colors = CheckboxDefaults.colors(checkedColor = Brand))
            Text("Saya menyetujui Syarat & Ketentuan serta Kebijakan Privasi sewa unit JogjaRide", fontSize = 12.sp, color = Muted)
        }
        if (err.isNotEmpty()) Text(err, color = ErrRed, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        PrimaryButton("Daftar Sekarang", {
            err = when {
                name.isBlank() -> "Nama lengkap wajib diisi"
                wa.length < 9 -> "Nomor WhatsApp tidak valid"
                !email.contains("@") -> "Email tidak valid"
                pass.length < 8 -> "Kata sandi minimal 8 karakter"
                pass != pass2 -> "Konfirmasi kata sandi tidak sama"
                !agree -> "Setujui syarat & ketentuan dulu"
                else -> ""
            }
            if (err.isEmpty()) {
                loading = true
                AuthRepo.register(name.trim(), wa.trim(), email.trim(), pass) { e ->
                    loading = false
                    if (e == null) { b.name = name.trim(); b.email = email.trim(); b.phone = wa.trim(); b.role = "user"; onDone() } else err = e
                }
            }
        }, enabled = !loading)
        TextButton(onLogin, Modifier.align(Alignment.CenterHorizontally)) {
            Text("Sudah punya akun? ", color = Muted); Text("Masuk Sekarang", color = Brand, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun LoginScreen(b: Booking, onBack: () -> Unit, onDone: () -> Unit, onRegister: () -> Unit, onForgot: () -> Unit) {
    var id by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var err by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    Screen("Masuk", onBack) {
        Spacer(Modifier.height(8.dp))
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(Brand), Alignment.Center) {
            Icon(Icons.Default.TwoWheeler, null, tint = Color.White)
        }
        Text("Selamat datang di JogjaRide", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 12.dp))
        Text("Sewa motor mudah & cepat khusus Yogyakarta", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        InfoBanner("Garansi Motor Prima & Unit Resmi", "Antar-jemput gratis di Stasiun Tugu & Lempuyangan")
        LabeledField("Email atau Nomor WhatsApp / HP", id, { id = it }, "nama@email.com atau 08xxxxxxxxxx", Icons.Default.AlternateEmail)
        LabeledField("Kata Sandi", pass, { pass = it }, "••••••••", password = true)
        TextButton(onForgot, Modifier.align(Alignment.End)) { Text("Lupa kata sandi?", color = Brand, fontSize = 12.sp) }
        if (err.isNotEmpty()) Text(err, color = ErrRed, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        PrimaryButton("Masuk", {
            err = when {
                id.isBlank() -> "Email atau nomor HP wajib diisi"
                !id.contains("@") -> "Masukkan email yang terdaftar"
                pass.isBlank() -> "Kata sandi wajib diisi"
                else -> ""
            }
            if (err.isEmpty()) {
                loading = true
                AuthRepo.login(id.trim(), pass, b) { e -> loading = false; if (e == null) onDone() else err = e }
            }
        }, enabled = !loading)
        Spacer(Modifier.height(16.dp))
        Text("atau masuk dengan", color = Muted, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(12.dp))
        OutlinedButton({
            err = ""
            loading = true
            AuthRepo.googleSignIn(ctx, b) { e ->
                loading = false
                if (e == null) onDone() else err = e
            }
        }, Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp), enabled = !loading) {
            Text("Lanjutkan dengan Google", color = Ink, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(10.dp))
        Button(onRegister, Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandLight, contentColor = Brand)) {
            Text("Daftar Akun Baru", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
    }
}
