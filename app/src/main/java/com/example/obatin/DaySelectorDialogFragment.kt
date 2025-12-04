package com.example.obatin

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.ToggleButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.core.view.children
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.obatin.R
import java.util.Locale

// Menggunakan konstanta yang diimpor dari AppConstants.kt (Asumsi)
import com.example.obatin.REQUEST_KEY_DAYS
import com.example.obatin.BUNDLE_KEY_DAYS


class DaySelectorDialogFragment : DialogFragment() {

    private val days = arrayOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
    private lateinit var dayToggleContainer: LinearLayout

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_day_selector, null)
        // Pastikan R.id.day_toggle_container didefinisikan di dialog_day_selector.xml
        dayToggleContainer = view.findViewById(R.id.day_toggle_container)

        // Logika inisialisasi ToggleButton/CheckBox jika diperlukan

        return MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("Pilih Hari Konsumsi")
            setView(view)

            setPositiveButton("SIMPAN") { dialog, which ->
                onDaysSelected()
            }
            setNegativeButton("BATAL") { dialog, which ->
                dismiss()
            }
        }.create()
    }

    private fun onDaysSelected() {
        val selectedDayNames = mutableListOf<String>()

        // Iterasi melalui anak-anak di container untuk menemukan ToggleButton
        dayToggleContainer.children.forEach { view ->
            if (view is ToggleButton) {
                if (view.isChecked) {
                    // Menggunakan textOn (yang berisi nama hari, cth: SEN)
                    selectedDayNames.add(view.textOn.toString())
                }
            }
        }

        if (selectedDayNames.isEmpty()) {
            Toast.makeText(requireContext(), "Pilih minimal satu hari.", Toast.LENGTH_SHORT).show()
            return
        }

        val resultString = selectedDayNames.joinToString(separator = ",")

        // Mengirim hasil kembali menggunakan konstanta yang diimpor
        setFragmentResult(REQUEST_KEY_DAYS, Bundle().apply {
            putString(BUNDLE_KEY_DAYS, resultString)
        })

        dismiss()
    }
}