// File: MedicineModels.kt
package com.example.obatin

import java.io.Serializable

// === DATA CLASSES ===

/**
 * Merepresentasikan satu jadwal pengingat (waktu dan dosis) untuk obat.
 */
data class Reminder(
    val time: String? = null,
    val dosageAmount: Double = 0.0,
    val dosageUnit: String = ""
) : Serializable

/**
 * Merepresentasikan data utama obat yang disimpan di database.
 */
data class Medicine(
    val medicineId: String? = null,
    val userId: String? = null,
    val name: String? = null,
    val note: String? = null,
    val refillNeeded: Boolean = false,
    val imageUrl: String? = null,
    val frequencyType: String? = null,
    val daysOfWeek: String? = null,
    val reminders: List<Reminder> = emptyList(),
    val medicineType: String? = null,
    val endDate: String? = null,
    val creationTimestamp: Long = System.currentTimeMillis(),
    val cycleValue: Int? = null,
    val cycleUnit: String? = null
) : Serializable

/**
 * Merepresentasikan satu catatan (log) kepatuhan minum obat.
 */
data class AdherenceLog(
    val userId: String? = null,
    val medicineId: String? = null,
    val reminderTime: String? = null,           // Waktu Dosis Terjadwal (String: "HH:mm")
    val actualTakenTimestamp: Long? = null,    // Waktu Obat Diminum (Epoch time)
    val actualTakenTime_str: String? = null,   // Waktu Obat Diminum (String: "HH:mm")
    val calculatedAdherenceStatus: String? = null // Status: "TEPAT_WAKTU", "TERLAMBAT", dll.
) : Serializable

// === ENUM dan INTERFACE ===

/**
 * Enum yang mendefinisikan tipe-tipe obat, ikon visual, dan unit dosis yang diperbolehkan.
 */
enum class MedicineType(val displayName: String, val drawableResId: Int, val dosageUnitTypes: List<String>) {
    PIL("Pil", R.drawable.icon_pil, listOf("Butir", "mg", "mcg")),
    CAPSUL("Kapsul", R.drawable.icon_capsul, listOf("Butir", "mg", "mcg")),
    SIRUP("Sirup", R.drawable.icon_sirup, listOf("ml")),
    SUNTIK("Suntik", R.drawable.icon_suntik, listOf("ml"));

    companion object {
        fun fromDisplayName(displayName: String) = values().find { it.displayName == displayName } ?: PIL
    }
}

/**
 * Interface untuk mendengarkan peristiwa penambahan/pembaruan pengingat dosis.
 */
interface ReminderListener {
    fun onReminderAdded(reminder: Reminder)
    fun onReminderUpdated(oldReminder: Reminder, newReminder: Reminder)
    fun updateMedicineTypeDisplay()
}