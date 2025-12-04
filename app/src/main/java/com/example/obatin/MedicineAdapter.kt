package com.example.obatin

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.obatin.R
import java.util.Locale
import java.util.Calendar
import java.text.SimpleDateFormat // Import untuk parsing waktu

class MedicineAdapter : ListAdapter<Medicine, MedicineAdapter.MedicineViewHolder>(MedicineDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medicine, parent, false)
        return MedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val medicine = getItem(position)
        holder.bind(medicine)
    }

    // ⭐️ FUNGSI BARU: Mencari pengingat dosis yang paling dekat dengan waktu sekarang ⭐️
    private fun findNextDueReminder(medicine: Medicine): Reminder? {
        if (medicine.reminders.isNullOrEmpty()) return null

        val now = Calendar.getInstance()
        var nextDueReminder: Reminder? = null
        var minTimeDiffMillis = Long.MAX_VALUE

        // Format untuk parsing waktu (asumsi HH:mm)
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.ROOT)

        for (reminder in medicine.reminders) {
            val reminderTimeStr = reminder.time ?: continue

            // Coba parsing waktu dari string
            val scheduledTime = timeFormatter.parse(reminderTimeStr) ?: continue

            val doseCal = Calendar.getInstance().apply {
                time = scheduledTime
                set(Calendar.YEAR, now.get(Calendar.YEAR))
                set(Calendar.MONTH, now.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
            }

            // Jika waktu dosis sudah lewat hari ini, setel untuk besok
            if (doseCal.timeInMillis < now.timeInMillis) {
                doseCal.add(Calendar.DAY_OF_YEAR, 1)
            }

            val timeDiffMillis = doseCal.timeInMillis - now.timeInMillis

            if (timeDiffMillis < minTimeDiffMillis) {
                minTimeDiffMillis = timeDiffMillis
                nextDueReminder = reminder
            }
        }

        return nextDueReminder
    }

    inner class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_medicine_name)
        private val scheduleTextView: TextView = itemView.findViewById(R.id.tv_medicine_schedule)
        private val imageView: ImageView = itemView.findViewById(R.id.img_icon_obat)

        fun bind(medicine: Medicine) {
            nameTextView.text = medicine.name

            // ⭐️ FIX: Gunakan dosis terdekat, bukan dosis pertama (firstOrNull) ⭐️
            val nextReminder = findNextDueReminder(medicine)

            if (nextReminder != null) {
                val safeDosage = nextReminder.dosageAmount ?: 0.0
                val safeUnit = nextReminder.dosageUnit ?: "mg"

                // Format dosis: %.0f jika Butir, %.1f jika lainnya
                val formattedDosage = if (safeUnit == "Butir") {
                    String.format(Locale.ROOT, "%.0f", safeDosage)
                } else {
                    String.format(Locale.ROOT, "%.1f", safeDosage)
                }

                scheduleTextView.text = itemView.context.getString(
                    R.string.medicine_schedule_format,
                    nextReminder.time ?: "XX:XX",
                    formattedDosage,
                    safeUnit
                )
            } else {
                scheduleTextView.text = itemView.context.getString(R.string.medicine_schedule_unset)
            }

            // Logika Gambar
            val type = MedicineType.values().find { it.displayName == medicine.medicineType } ?: MedicineType.PIL
            imageView.setImageResource(type.drawableResId)


            // Navigasi ke CheckListActivity
            itemView.setOnClickListener {
                if (medicine.medicineId != null) {
                    val context = itemView.context
                    val intent = Intent(context, CheckListActivity::class.java).apply {
                        putExtra("MEDICINE_ID", medicine.medicineId)
                    }
                    context.startActivity(intent)
                } else {
                    Toast.makeText(itemView.context, "ID Obat tidak valid.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

class MedicineDiffCallback : DiffUtil.ItemCallback<Medicine>() {
    override fun areItemsTheSame(oldItem: Medicine, newItem: Medicine): Boolean {
        return oldItem.medicineId == newItem.medicineId
    }

    override fun areContentsTheSame(oldItem: Medicine, newItem: Medicine): Boolean {
        // Harus membandingkan semua properti data untuk deteksi perubahan
        return oldItem == newItem
    }
}