package com.example.obatin

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pindahkan user langsung ke LoginActivity, karena ini hanyalah Activity bawaan
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}