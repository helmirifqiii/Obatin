package com.example.obatin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 1000 // Kurangi timeout untuk pengecekan cepat (1 detik)

    // Konstanta Sesi
    private val PREFS_NAME = "SessionPrefs"
    private val KEY_LAST_LOGIN = "lastLoginTimestamp"
    private val SESSION_EXPIRY_DAYS = 7
    private val EXPIRY_MILLIS = TimeUnit.DAYS.toMillis(SESSION_EXPIRY_DAYS.toLong())

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        // Menggunakan Handler untuk menunda perpindahan
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSession()
        }, SPLASH_TIME_OUT)
    }

    private fun checkUserSession() {
        val currentUser = auth.currentUser
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastLogin = prefs.getLong(KEY_LAST_LOGIN, 0L)
        val currentTime = System.currentTimeMillis()

        val targetActivity: Class<*>

        if (currentUser != null) {
            // 1. User terautentikasi (Auth Token masih ada)
            if (lastLogin == 0L) {
                // Sesi validasi awal belum tersimpan, anggap valid
                targetActivity = HomeActivity::class.java
            } else if (currentTime - lastLogin > EXPIRY_MILLIS) {
                // 2. Sesi Sudah Kadaluarsa (Lebih dari 7 hari)
                auth.signOut() // Hapus sesi Firebase
                // Hapus timestamp lokal (optional)
                prefs.edit().remove(KEY_LAST_LOGIN).apply()
                targetActivity = LoginActivity::class.java
            } else {
                // 3. Sesi Masih Valid (Di bawah 7 hari)
                targetActivity = HomeActivity::class.java
            }
        } else {
            // 4. Tidak ada sesi Firebase aktif
            targetActivity = LoginActivity::class.java
        }

        // Navigasi ke Activity target
        val intent = Intent(this, targetActivity)
        startActivity(intent)
        finish()
    }
}