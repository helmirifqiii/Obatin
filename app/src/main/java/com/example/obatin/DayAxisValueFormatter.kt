// File: DayAxisValueFormatter.kt
package com.example.obatin

import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Locale

/**
 * Custom ValueFormatter untuk mengubah nilai float sumbu X (indeks hari)
 * menjadi label Hari/Tanggal yang dapat dibaca.
 */
class DayAxisValueFormatter(private val days: Array<String>) : ValueFormatter() {

    // Gunakan Locale Indonesia untuk label hari (opsional)
    private val locale = Locale("id", "ID")

    override fun getFormattedValue(value: Float): String {
        val index = value.toInt()

        // Pastikan indeks berada dalam batas array days
        return if (index >= 0 && index < days.size) {
            days[index]
        } else {
            ""
        }
    }
}