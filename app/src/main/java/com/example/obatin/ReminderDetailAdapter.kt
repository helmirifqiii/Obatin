// File: ReminderDetailAdapter.kt
package com.example.obatin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

// Asumsi data class Reminder ada di scope/package yang sama (MedicineModels.kt)

class ReminderDetailAdapter : ListAdapter<Reminder, ReminderDetailAdapter.ReminderViewHolder>(ReminderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        // Menggunakan R.layout.item_reminder_detail
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reminder_detail, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ✅ KONFIRMASI: Menggunakan ID yang benar dari item_reminder_detail.xml
        private val tvTime: TextView = itemView.findViewById(R.id.tv_reminder_time_detail)
        private val tvDosage: TextView = itemView.findViewById(R.id.tv_dosage_detail)

        fun bind(reminder: Reminder) {
            // Menggunakan operator Elvis untuk menangani Double?
            val safeDosage = reminder.dosageAmount ?: 0.0
            val safeUnit = reminder.dosageUnit ?: "mg"

            // Logika format dosis
            val formattedDosage = if (safeUnit == "Butir") {
                String.format(Locale.ROOT, "%.0f", safeDosage)
            } else {
                String.format(Locale.ROOT, "%.1f", safeDosage)
            }

            tvTime.text = reminder.time ?: "XX:XX"
            tvDosage.text = "$formattedDosage $safeUnit"
        }
    }
}

class ReminderDiffCallback : DiffUtil.ItemCallback<Reminder>() {
    override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
        return oldItem.time == newItem.time // Menggunakan waktu sebagai ID unik
    }

    override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
        // Menggunakan perbandingan data class
        return oldItem == newItem
    }
}