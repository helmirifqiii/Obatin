package com.example.obatin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import android.widget.ProgressBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.android.material.textfield.TextInputEditText
import com.example.obatin.R
import android.content.Context

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonLogin: Button
    private lateinit var checkBoxRemember: CheckBox
    private lateinit var textViewForgotPassword: TextView
    private lateinit var textViewRegisterLink: TextView
    private lateinit var loadingProgressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    // Konstanta untuk SharedPreferences
    private val PREFS_NAME = "SessionPrefs" // Ganti nama agar lebih umum
    private val KEY_REMEMBER = "rememberMe"
    private val KEY_EMAIL = "email"
    private val KEY_LAST_LOGIN = "lastLoginTimestamp" // ⭐️ Konstanta baru untuk waktu sesi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // INISIALISASI FIREBASE
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // INISIALISASI KOMPONEN UI DENGAN ID DARI XML
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        checkBoxRemember = findViewById(R.id.checkBoxRemember)
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword)
        textViewRegisterLink = findViewById(R.id.textViewRegisterLink)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)

        // ⭐️ FIX: MUAT DATA EMAIL SAJA DARI SHARED PREFERENCES ⭐️
        loadLoginPrefs()

        // 1. Tombol Login
        buttonLogin.setOnClickListener {
            performLogin()
        }

        // 2. Link Daftar
        textViewRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // 3. Link Lupa Password
        textViewForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadLoginPrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isRemembered = prefs.getBoolean(KEY_REMEMBER, false)

        if (isRemembered) {
            val savedEmail = prefs.getString(KEY_EMAIL, "")
            editTextEmail.setText(savedEmail)
            checkBoxRemember.isChecked = true
            // Password tidak dimuat untuk alasan keamanan
        }
    }

    private fun saveLoginPrefs(email: String, remember: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with (prefs.edit()) {
            if (remember) {
                putString(KEY_EMAIL, email)
                putBoolean(KEY_REMEMBER, true)
                // ⭐️ FIX: Simpan Timestamp Login Saat Ini ⭐️
                putLong(KEY_LAST_LOGIN, System.currentTimeMillis())
            } else {
                remove(KEY_EMAIL)
                remove(KEY_LAST_LOGIN) // Hapus timestamp jika tidak diingat
                putBoolean(KEY_REMEMBER, false)
            }
            apply()
        }
    }


    private fun performLogin() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val rememberMe = checkBoxRemember.isChecked

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password harus diisi.", Toast.LENGTH_SHORT).show()
            return
        }

        loadingProgressBar.visibility = View.VISIBLE
        buttonLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser!!.uid

                    // ⭐️ FIX: Panggil saveLoginPrefs di sini ⭐️
                    saveLoginPrefs(email, rememberMe)

                    // 2. Ambil nama user dari Realtime Database
                    database.getReference("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            val userName = snapshot.child("name").getValue(String::class.java) ?: "Pengguna"

                            // Login BERHASIL: Navigasi ke HomeActivity
                            loadingProgressBar.visibility = View.GONE
                            buttonLogin.isEnabled = true

                            val intent = Intent(this@LoginActivity, HomeActivity::class.java).apply {
                                putExtra("USER_ID", userId)
                                putExtra("USER_NAME", userName)
                            }
                            startActivity(intent)
                            finish()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            loadingProgressBar.visibility = View.GONE
                            buttonLogin.isEnabled = true

                            Toast.makeText(this@LoginActivity, "Login Berhasil, tapi Gagal ambil data user: ${error.message}", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                            finish()
                        }
                    })

                } else {
                    loadingProgressBar.visibility = View.GONE
                    buttonLogin.isEnabled = true

                    val errorMessage = task.exception?.message ?: "Gagal login. Periksa email dan password Anda."
                    Toast.makeText(this, "Gagal Login: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }
}