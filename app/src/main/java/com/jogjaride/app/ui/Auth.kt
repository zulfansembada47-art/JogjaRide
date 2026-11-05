package com.jogjaride.app.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Semua akses login, daftar, dan profil pengguna lewat Firebase. onDone(null) = sukses, selain itu = pesan error. */
object AuthRepo {
    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()

    val isLoggedIn get() = auth.currentUser != null

    fun register(name: String, phone: String, email: String, pass: String, onDone: (String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { res ->
                val uid = res.user?.uid ?: return@addOnSuccessListener onDone("Gagal membuat akun")
                val data = hashMapOf(
                    "name" to name, "phone" to phone, "email" to email,
                    "role" to "user", "createdAt" to System.currentTimeMillis()
                )
                db.collection("users").document(uid).set(data)
                    .addOnSuccessListener { onDone(null) }
                    .addOnFailureListener { onDone(msg(it)) }
            }
            .addOnFailureListener { onDone(msg(it)) }
    }

    fun login(email: String, pass: String, b: Booking, onDone: (String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { loadProfile(b, onDone) }
            .addOnFailureListener { onDone(msg(it)) }
    }

    fun googleSignIn(context: Context, b: Booking, onDone: (String?) -> Unit) {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        val webClientId = if (resId != 0) context.getString(resId) else ""

        if (webClientId.isBlank()) {
            onDone("Web Client ID tidak ditemukan. Silakan unduh google-services.json terbaru dari Firebase setelah mengaktifkan Google Sign-In.")
            return
        }

        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val scope = (context as? ComponentActivity)?.lifecycleScope ?: CoroutineScope(Dispatchers.Main)

        scope.launch {
            try {
                val result = credentialManager.getCredential(request = request, context = context)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)

                    auth.signInWithCredential(authCredential)
                        .addOnSuccessListener { res ->
                            val uid = res.user?.uid ?: return@addOnSuccessListener onDone("Gagal mendapatkan User ID")
                            val userDoc = db.collection("users").document(uid)
                            userDoc.get().addOnSuccessListener { d ->
                                if (!d.exists()) {
                                    val data = hashMapOf(
                                        "name" to (res.user?.displayName ?: "Pengguna Google"),
                                        "phone" to (res.user?.phoneNumber ?: ""),
                                        "email" to (res.user?.email ?: ""),
                                        "role" to "user",
                                        "createdAt" to System.currentTimeMillis()
                                    )
                                    userDoc.set(data)
                                        .addOnSuccessListener { loadProfile(b, onDone) }
                                        .addOnFailureListener { onDone(msg(it)) }
                                } else {
                                    loadProfile(b, onDone)
                                }
                            }.addOnFailureListener { onDone(msg(it)) }
                        }
                        .addOnFailureListener { onDone(msg(it)) }
                } else {
                    onDone("Tipe kredensial tidak valid")
                }
            } catch (e: Exception) {
                if (e is GetCredentialCancellationException || e.message?.contains("canceled", ignoreCase = true) == true) {
                    onDone("Login Google dibatalkan")
                } else {
                    onDone(msg(e))
                }
            }
        }
    }

    fun loadProfile(b: Booking, onDone: (String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onDone("Belum login")
        db.collection("users").document(uid).get()
            .addOnSuccessListener { d ->
                b.name = d.getString("name") ?: b.name
                b.email = d.getString("email") ?: (auth.currentUser?.email ?: b.email)
                b.phone = d.getString("phone") ?: b.phone
                b.role = d.getString("role") ?: "user"
                onDone(null)
            }
            .addOnFailureListener { onDone(msg(it)) }
    }

    fun saveProfile(b: Booking) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).update(mapOf("name" to b.name, "phone" to b.phone))
    }

    fun resetPassword(email: String, onDone: (String?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onDone(null) }
            .addOnFailureListener { onDone(msg(it)) }
    }

    fun logout() = auth.signOut()

    private fun msg(e: Exception): String = when (e) {
        is FirebaseAuthUserCollisionException -> "Email sudah terdaftar"
        is FirebaseAuthWeakPasswordException -> "Kata sandi terlalu lemah"
        is FirebaseAuthInvalidCredentialsException -> "Email atau kata sandi salah"
        is FirebaseAuthInvalidUserException -> "Akun tidak ditemukan"
        is FirebaseNetworkException -> "Tidak ada koneksi internet"
        else -> e.localizedMessage ?: "Terjadi kesalahan"
    }
}
