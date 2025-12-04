package com.example.obatin

// Digunakan untuk menyimpan data user ke Firebase Realtime Database
data class User(
    val name: String? = null,
    val email: String? = null,
    // ⭐️ Tambahkan properti Nomor WhatsApp
    val whatsappNumber: String? = null
) {
    // Konstruktor kosong untuk Firebase Realtime Database
    constructor() : this(null, null, null)
}