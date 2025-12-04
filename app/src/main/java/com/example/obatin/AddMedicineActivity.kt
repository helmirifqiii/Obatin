package com.example.obatin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// Ini adalah kerangka untuk AddMedicineActivity
class AddMedicineActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Ganti dengan layout Anda
        // setContentView(R.layout.activity_add_medicine)

        // Logika untuk mendeteksi mode EDIT dari Intent
        val medicineId = intent.getStringExtra("MEDICINE_ID")
        if (medicineId != null) {
            // Mode EDIT: Muat data obat dan isi formulir
            // Contoh: loadDataForEdit(medicineId)
        } else {
            // Mode TAMBAH: Kosongkan formulir
        }
    }
}