package com.example.obatin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.database.FirebaseDatabase // Tidak diperlukan lagi
// import com.google.firebase.database.DataSnapshot // Tidak diperlukan lagi
// import com.google.firebase.database.DatabaseError // Tidak diperlukan lagi
// import com.google.firebase.database.ValueEventListener // Tidak diperlukan lagi

class ForgotPasswordActivity : AppCompatActivity() {

    // ⭐️ Menggunakan ID yang sudah diubah di layout (diasumsikan sudah direvisi ke Email)
    private lateinit var editTextEmailReset: TextInputEditText // ID diperbarui: Dari Whatsapp ke Email
    private lateinit var buttonSendResetLink: Button
    private lateinit var textViewBackToLogin: TextView

    private lateinit var auth: FirebaseAuth
    // private lateinit var database: FirebaseDatabase // Dihapus: Database tidak diperlukan lagi untuk mencari Email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        // database = FirebaseDatabase.getInstance() // Dihapus

        // Inisialisasi UI (sesuaikan dengan ID di layout yang direvisi)
        // Jika Anda menggunakan ID yang disarankan di revisi XML sebelumnya, gunakan R.id.editTextEmailReset
        // Namun, jika Anda menggunakan ID lama (R.id.editTextWhatsappReset), silakan ganti nama variabel di sini.
        // Saya asumsikan Anda sudah mengubah ID di XML ke 'editTextEmailReset'
        editTextEmailReset = findViewById(R.id.editTextEmailReset) // ⭐️ ID ASUMSI direvisi ke EmailReset
        buttonSendResetLink = findViewById(R.id.buttonSendResetLink)
        textViewBackToLogin = findViewById(R.id.textViewBackToLogin)

        // Listener untuk tombol kirim link
        buttonSendResetLink.setOnClickListener {
            // Memulai proses reset langsung menggunakan Email
            sendResetLink()
        }

        // Listener untuk kembali ke login
        textViewBackToLogin.setOnClickListener {
            finish()
        }
    }

    /**
     * FUNGSI UTAMA BARU: Mengambil input Email dan langsung mengirim tautan reset via Firebase.
     * Tidak ada lagi pencarian ke Realtime Database atau konfirmasi WhatsApp.
     */
    private fun sendResetLink() {
        // Ambil Email dari input field
        val email = editTextEmailReset.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Masukkan alamat email Anda.", Toast.LENGTH_SHORT).show()
            return
        }

        // Memeriksa format Email sederhana (opsional, bisa ditambah validasi yang lebih kuat)
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Format alamat email tidak valid.", Toast.LENGTH_SHORT).show()
            return
        }

        buttonSendResetLink.isEnabled = false // Nonaktifkan tombol saat loading

        // 1. LANGSUNG KIRIM RESET EMAIL MELALUI FIREBASE
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                buttonSendResetLink.isEnabled = true // Aktifkan kembali tombol

                if (task.isSuccessful) {
                    // Tautan reset sukses dikirim
                    Toast.makeText(
                        this,
                        "Tautan reset telah dikirim ke email Anda. Silakan cek kotak masuk email.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Kembali ke Login setelah proses selesai
                    finish()
                } else {
                    // Gagal mengirim tautan (misalnya, email tidak terdaftar di Firebase Auth)
                    val errorMessage = task.exception?.message
                    Toast.makeText(
                        this,
                        "Gagal mengirim tautan. Pastikan email terdaftar. Kesalahan: $errorMessage",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    // 🚫 FUNGSI LAMA DIHAPUS: findEmailAndSendReset()
    // 🚫 FUNGSI LAMA DIHAPUS: sendFirebaseResetEmail()
    // 🚫 FUNGSI LAMA DIHAPUS: openWhatsAppConfirmation()
}