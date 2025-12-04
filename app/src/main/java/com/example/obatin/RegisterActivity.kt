package com.example.obatin

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.obatin.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextName: TextInputEditText
    // ⭐️ Deklarasi baru untuk Nomor WhatsApp
    private lateinit var editTextWhatsappNumber: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextConfirmPassword: TextInputEditText
    private lateinit var buttonRegister: Button
    private lateinit var textViewLoginLink: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // INISIALISASI FIREBASE
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // INISIALISASI KOMPONEN UI DENGAN ID DARI XML
        editTextName = findViewById(R.id.editTextName)
        // ⭐️ Inisialisasi komponen baru
        editTextWhatsappNumber = findViewById(R.id.editTextWhatsappNumber)

        editTextEmail = findViewById(R.id.editTextEmailRegister)
        editTextPassword = findViewById(R.id.editTextPasswordRegister)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        buttonRegister = findViewById(R.id.buttonRegister)
        textViewLoginLink = findViewById(R.id.textViewLoginLink)

        // 1. Tombol Daftar
        buttonRegister.setOnClickListener {
            performRegistration()
        }

        // 2. Link Masuk
        textViewLoginLink.setOnClickListener {
            finish()
        }
    }

    private fun performRegistration() {
        val name = editTextName.text.toString().trim()
        // ⭐️ Ambil nilai Nomor WhatsApp
        val whatsappNumber = editTextWhatsappNumber.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val confirmPassword = editTextConfirmPassword.text.toString().trim()

        if (name.isEmpty() || whatsappNumber.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi.", Toast.LENGTH_SHORT).show()
            return
        }

        // ⭐️ Validasi Nomor WA sederhana (opsional, bisa lebih ketat)
        if (whatsappNumber.length < 9) {
            Toast.makeText(this, "Nomor WhatsApp terlalu pendek.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Konfirmasi Password tidak cocok.", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. DAFTARKAN USER MENGGUNAKAN FIREBASE AUTH
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid

                    if (userId != null) {
                        // 2. SIMPAN DATA TAMBAHAN (NAMA & WA) KE REALTIME DATABASE
                        // ⭐️ Masukkan Nomor WA ke model User
                        val user = User(name, email, whatsappNumber)

                        database.getReference("users").child(userId).setValue(user)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(this, "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_LONG).show()
                                    // Pindah ke LoginActivity (atau HomeActivity jika sudah auto-login)
                                    val loginIntent = Intent(this, LoginActivity::class.java)
                                    loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(loginIntent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Gagal simpan data user. ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
                                    firebaseUser.delete() // Hapus user dari Auth jika gagal simpan data
                                }
                            }
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Terjadi kesalahan tidak dikenal."
                    Toast.makeText(this, "Gagal Registrasi: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }
}