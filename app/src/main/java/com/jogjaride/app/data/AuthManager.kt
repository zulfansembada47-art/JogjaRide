package com.jogjaride.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = ""
)

object AuthManager {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun registerUser(
        name: String,
        wa: String,
        email: String,
        pass: String,
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                val profile = UserProfile(uid = uid, name = name, email = email, phone = wa)

                val userMap = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "phone" to wa,
                    "email" to email,
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("users").document(uid)
                    .set(userMap)
                    .addOnSuccessListener {
                        onSuccess(profile)
                    }
                    .addOnFailureListener { e ->
                        // Jika simpan ke Firestore gagal, tetap panggil onSuccess dengan profile lokal
                        onSuccess(profile)
                    }
            }
            .addOnFailureListener { e ->
                val message = when (e) {
                    is FirebaseAuthUserCollisionException -> "Email sudah terdaftar. Silakan masuk."
                    else -> e.localizedMessage ?: "Pendaftaran gagal. Periksa koneksi internet Anda."
                }
                onError(message)
            }
    }

    fun loginUser(
        id: String,
        pass: String,
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        val targetEmail = if (id.contains("@")) id else "$id@jogjaride.com"

        auth.signInWithEmailAndPassword(targetEmail, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        val profile = if (doc.exists()) {
                            UserProfile(
                                uid = uid,
                                name = doc.getString("name") ?: result.user?.displayName ?: "Pengguna JogjaRide",
                                email = doc.getString("email") ?: (result.user?.email ?: targetEmail),
                                phone = doc.getString("phone") ?: ""
                            )
                        } else {
                            UserProfile(
                                uid = uid,
                                name = result.user?.displayName ?: "Pengguna JogjaRide",
                                email = result.user?.email ?: targetEmail,
                                phone = ""
                            )
                        }
                        onSuccess(profile)
                    }
                    .addOnFailureListener {
                        val fallbackProfile = UserProfile(
                            uid = uid,
                            name = result.user?.displayName ?: "Pengguna JogjaRide",
                            email = result.user?.email ?: targetEmail,
                            phone = ""
                        )
                        onSuccess(fallbackProfile)
                    }
            }
            .addOnFailureListener { e ->
                val message = when (e) {
                    is FirebaseAuthInvalidUserException, is FirebaseAuthInvalidCredentialsException -> "Email atau kata sandi salah."
                    else -> e.localizedMessage ?: "Gagal masuk. Periksa koneksi atau kredensial Anda."
                }
                onError(message)
            }
    }

    fun logoutUser() {
        try {
            auth.signOut()
        } catch (_: Exception) { }
    }

    fun getCurrentUser(onResult: (UserProfile?) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        onResult(
                            UserProfile(
                                uid = user.uid,
                                name = doc.getString("name") ?: "Pengguna JogjaRide",
                                email = doc.getString("email") ?: (user.email ?: ""),
                                phone = doc.getString("phone") ?: ""
                            )
                        )
                    } else {
                        onResult(UserProfile(uid = user.uid, email = user.email ?: ""))
                    }
                }
                .addOnFailureListener {
                    onResult(UserProfile(uid = user.uid, email = user.email ?: ""))
                }
        } else {
            onResult(null)
        }
    }
}
