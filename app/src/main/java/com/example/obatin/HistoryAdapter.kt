package com.example.obatin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.obatin.R
import java.util.Locale

class HistoryAdapter : ListAdapter<Medicine, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat_obat, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_history_name)
        // ⭐️ PERBAIKAN: Deklarasi tv_history_status ⭐️
        private val statusTextView: TextView = itemView.findViewById(R.id.tv_history_status)
        private val iconImageView: ImageView = itemView.findViewById(R.id.icon_pill_small)

        fun bind(medicine: Medicine) {
            nameTextView.text = medicine.name ?: "Obat Tidak Dikenal"

            // ⭐️ LOGIKA STATUS RIWAYAT ⭐️
            // Obat diarsipkan jika reminders-nya kosong atau frequencyType="DIARSIPKAN"
            val isArchived = medicine.reminders.isNullOrEmpty() || medicine.frequencyType == "DIARSIPKAN"

            if (isArchived) {
                statusTextView.text = "Selesai (${medicine.endDate ?: "Tidak Tercatat"})"
                statusTextView.setTextColor(android.graphics.Color.GRAY)
            } else {
                statusTextView.text = "Aktif"
                statusTextView.setTextColor(android.graphics.Color.parseColor("#2ECC71"))
            }

            statusTextView.visibility = View.VISIBLE

            // LOGIKA IKON DINAMIS
            val medicineType = MedicineType.fromDisplayName(medicine.medicineType ?: "Pil")
            iconImageView.setImageResource(medicineType.drawableResId)

            // Tambahkan listener untuk membuka detail obat
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, "Riwayat: ${medicine.name} - Status: ${statusTextView.text}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class HistoryDiffCallback : DiffUtil.ItemCallback<Medicine>() {
    override fun areItemsTheSame(oldItem: Medicine, newItem: Medicine): Boolean {
        return oldItem.medicineId == newItem.medicineId
    }

    override fun areContentsTheSame(oldItem: Medicine, newItem: Medicine): Boolean {
        return oldItem == newItem
    }
}