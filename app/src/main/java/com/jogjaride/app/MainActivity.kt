package com.jogjaride.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jogjaride.app.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { JogjaTheme { App() } }
    }
}

@Composable
fun App() {
    val nav = rememberNavController()
    val b = remember { Booking() }
    val start = remember { if (AuthRepo.isLoggedIn) "home" else "register" }
    LaunchedEffect(Unit) { if (AuthRepo.isLoggedIn) AuthRepo.loadProfile(b) { } }
    val back: () -> Unit = { if (nav.previousBackStackEntry != null) nav.popBackStack() }
    val toHome: () -> Unit = { nav.navigate("home") { popUpTo(nav.graph.id) { inclusive = true } } }
    val logout: () -> Unit = {
        AuthRepo.logout(); b.bookings.clear()
        nav.navigate("login") { popUpTo(nav.graph.id) { inclusive = true } }
    }
    NavHost(nav, startDestination = start) {
        composable("register") { RegisterScreen(b, toHome) { nav.navigate("login") { launchSingleTop = true } } }
        composable("login") {
            LoginScreen(b, back, toHome, { nav.navigate("register") { launchSingleTop = true } }) { nav.navigate("forgot") }
        }
        composable("forgot") { ForgotScreen(back) }
        composable("home") { HomeScreen(b, { nav.navigate(it) }, logout) }
        composable("fleet") { FleetScreen(b, back) { nav.navigate("schedule") } }
        composable("schedule") { ScheduleScreen(b, back) { nav.navigate("confirm") } }
        composable("confirm") { ConfirmScreen(b, back) { nav.navigate("success") } }
        composable("success") {
            SuccessScreen(b, { nav.navigate("ticket") }) { nav.navigate("home") { popUpTo("home") { inclusive = true } } }
        }
        composable("ticket") { TicketScreen(b, back) }
        composable("notif") { NotifScreen(back) }
        composable("editprofile") { EditProfileScreen(b, back) }
        composable("security") { SecurityScreen(back) }
        composable("settings") { SettingsScreen(back) }
        composable("help") { HelpScreen(back) }
    }
}
