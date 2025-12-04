package com.example.obatin

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.obatin.R
import java.util.Locale
import kotlin.math.roundToInt

// Menggunakan konstanta yang diimpor dari AppConstants.kt
import com.example.obatin.REQUEST_KEY_CYCLE
import com.example.obatin.BUNDLE_KEY_CYCLE_VALUE
import com.example.obatin.BUNDLE_KEY_CYCLE_UNIT


class CycleSelectorDialogFragment : DialogFragment() {

    private lateinit var btnHari: Button
    private lateinit var btnMinggu: Button
    private lateinit var btnBulan: Button
    private lateinit var tvStatus: TextView
    private lateinit var numberPicker: NumberPicker

    private var cycleUnit: String = "hari"
    private var cycleValue: Int = 4

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_cycle_selector, null)

        // Inisialisasi Views
        btnHari = dialogView.findViewById(R.id.btn_cycle_hari)
        btnMinggu = dialogView.findViewById(R.id.btn_cycle_minggu)
        btnBulan = dialogView.findViewById(R.id.btn_cycle_bulan)
        tvStatus = dialogView.findViewById(R.id.tv_cycle_status)
        numberPicker = dialogView.findViewById(R.id.number_picker)

        setupNumberPicker()
        setupUnitButtons()

        // Pastikan Tombol Hari dipilih default
        if (cycleUnit == "hari") btnHari.performClick()
        else if (cycleUnit == "minggu") btnMinggu.performClick()
        else btnBulan.performClick()


        return MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("Atur Siklus Konsumsi")
            setView(dialogView)

            setPositiveButton("OK") { dialog, which ->
                val selectedValue = numberPicker.value // Ambil nilai terakhir dari picker

                // 🚨 FIX PENTING: Kirim state yang sudah final
                setFragmentResult(REQUEST_KEY_CYCLE, Bundle().apply {
                    putInt(BUNDLE_KEY_CYCLE_VALUE, selectedValue)
                    putString(BUNDLE_KEY_CYCLE_UNIT, cycleUnit)
                })
            }
            setNegativeButton("BATAL", null)
        }.create()
    }

    private fun setupNumberPicker() {
        numberPicker.minValue = 1
        numberPicker.maxValue = 30
        numberPicker.value = cycleValue

        numberPicker.setOnValueChangedListener { _, _, newVal ->
            cycleValue = newVal
            updateStatusText(newVal)
        }
        updateStatusText(numberPicker.value)
    }

    private fun updateStatusText(value: Int) {
        tvStatus.text = "setiap $value $cycleUnit"
    }

    private fun setupUnitButtons() {
        val buttons = listOf(btnHari, btnMinggu, btnBulan)
        val unitMap = mapOf(
            btnHari to "hari",
            btnMinggu to "minggu",
            btnBulan to "bulan"
        )

        buttons.forEach { button ->
            button.setOnClickListener { clickedButton ->
                cycleUnit = unitMap[button] ?: "hari"

                // Atur batas NumberPicker berdasarkan Unit
                numberPicker.maxValue = when (cycleUnit) {
                    "minggu" -> 4
                    "bulan" -> 12
                    else -> 30 // Hari
                }

                if (numberPicker.value > numberPicker.maxValue) {
                    numberPicker.value = numberPicker.maxValue
                }

                // Setel warna tombol
                buttons.forEach { b ->
                    val isClicked = (b == clickedButton)
                    b.setBackgroundColor(if (isClicked) Color.WHITE else Color.parseColor("#616161"))
                    b.setTextColor(if (isClicked) Color.BLACK else Color.WHITE)
                }
                updateStatusText(numberPicker.value)
            }
        }
    }
}