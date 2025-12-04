// File: ReminderDialogFragment.kt
package com.example.obatin

import android.app.Dialog
import android.os.Bundle
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.obatin.R
import java.io.Serializable
import java.util.Locale
import android.widget.LinearLayout
import kotlin.math.ceil
import android.os.Build

class ReminderDialogFragment : DialogFragment() {

    private val DIALOG_LAYOUT = R.layout.dialog_reminder_input

    private lateinit var timePicker: TimePicker
    private lateinit var tvDosageAmount: TextView
    private lateinit var btnMinusDosage: Button
    private lateinit var btnPlusDosage: Button
    private lateinit var rgDosageUnitDynamic: RadioGroup

    private var currentDosage: Double = 1.0
    private var initialReminder: Reminder? = null
    private var allowedUnits: Array<String>? = null
    private var selectedUnit: String = "mg"

    companion object {
        fun newInstance(reminder: Reminder?, allowedUnits: Array<String>?): ReminderDialogFragment {
            val fragment = ReminderDialogFragment()
            val args = Bundle()

            reminder?.let {
                args.putSerializable("REMINDER_DATA", it as Serializable)
            }
            allowedUnits?.let {
                args.putStringArray("ALLOWED_UNITS", it)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initialReminder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("REMINDER_DATA", Reminder::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("REMINDER_DATA") as? Reminder
        }

        allowedUnits = arguments?.getStringArray("ALLOWED_UNITS")

        selectedUnit = initialReminder?.dosageUnit
            ?: allowedUnits?.firstOrNull { it == "Butir" }
                    ?: allowedUnits?.firstOrNull()
                    ?: "mg"

        currentDosage = initialReminder?.dosageAmount ?: 1.0
        if (selectedUnit == "Butir") {
            currentDosage = currentDosage.coerceAtLeast(1.0).toInt().toDouble()
        }
    }

    private fun formatDosage(dosage: Double, unit: String): String {
        return if (unit == "Butir") {
            String.format(Locale.ROOT, "%.0f", dosage)
        } else {
            String.format(Locale.ROOT, "%.1f", dosage)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_reminder_input, null)

        timePicker = dialogView.findViewById(R.id.time_picker)
        tvDosageAmount = dialogView.findViewById(R.id.tv_dosage_amount)
        btnMinusDosage = dialogView.findViewById(R.id.btn_minus_dosage)
        btnPlusDosage = dialogView.findViewById(R.id.btn_plus_dosage)
        rgDosageUnitDynamic = dialogView.findViewById(R.id.rg_dosage_unit_dynamic)

        timePicker.setIs24HourView(true)
        setupDynamicUnitRadioButtons(dialogView)

        initialReminder?.let { reminder ->
            timePicker.hour = reminder.time?.substringBefore(':')?.toInt() ?: 12
            timePicker.minute = reminder.time?.substringAfter(':')?.toInt() ?: 0
        } ?: run {
            val calendar = java.util.Calendar.getInstance()
            timePicker.hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            timePicker.minute = calendar.get(java.util.Calendar.MINUTE)
        }

        tvDosageAmount.text = formatDosage(currentDosage, selectedUnit)

        btnMinusDosage.setOnClickListener {
            val step = if (selectedUnit == "Butir") 1.0 else 0.5
            val minLimit = if (selectedUnit == "Butir") 1.0 else 0.5

            if (currentDosage > minLimit) {
                currentDosage -= step
                if (selectedUnit == "Butir") {
                    currentDosage = currentDosage.toInt().toDouble()
                }
            }
            tvDosageAmount.text = formatDosage(currentDosage, selectedUnit)
        }

        btnPlusDosage.setOnClickListener {
            val step = if (selectedUnit == "Butir") 1.0 else 0.5
            currentDosage += step

            if (selectedUnit == "Butir") {
                currentDosage = currentDosage.toInt().toDouble()
            }

            tvDosageAmount.text = formatDosage(currentDosage, selectedUnit)
        }

        rgDosageUnitDynamic.setOnCheckedChangeListener { group, checkedId ->
            val checkedRadioButton = group.findViewById<RadioButton>(checkedId)
            if (checkedRadioButton != null) {
                selectedUnit = checkedRadioButton.text.toString()

                if (selectedUnit == "Butir") {
                    currentDosage = ceil(currentDosage).coerceAtLeast(1.0)
                }
                tvDosageAmount.text = formatDosage(currentDosage, selectedUnit)
            }
        }

        // ⭐️ Perbaikan/Penargetan Style: R.style.CustomAlertDialogTheme ⭐️
        return MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialogTheme).apply {
            setView(dialogView)

            setPositiveButton("SIMPAN") { _, _ ->
                val hour = timePicker.hour
                val minute = timePicker.minute
                val timeString = String.format("%02d:%02d", hour, minute)

                val finalDosage = if (selectedUnit == "Butir") currentDosage.toInt().toDouble() else currentDosage

                val newReminder = Reminder(
                    time = timeString,
                    dosageAmount = finalDosage,
                    dosageUnit = selectedUnit
                )

                if (initialReminder != null) {
                    (activity as? ReminderListener)?.onReminderUpdated(initialReminder!!, newReminder)
                } else {
                    (activity as? ReminderListener)?.onReminderAdded(newReminder)
                }
            }

            setNegativeButton("BATAL") { _, _ -> dismiss() }

        }.create()
    }

    private fun setupDynamicUnitRadioButtons(view: View) {
        val units = allowedUnits ?: arrayOf("mg", "mcg", "ml")

        val accentColor = try {
            requireContext().getColor(R.color.primary_accent)
        } catch (e: Exception) {
            android.graphics.Color.GREEN
        }

        val textColor = try {
            requireContext().getColor(R.color.text_dark)
        } catch (e: Exception) {
            android.graphics.Color.BLACK
        }

        val colorStateList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(accentColor, accentColor)
        )

        rgDosageUnitDynamic.orientation = LinearLayout.HORIZONTAL

        units.forEachIndexed { index, unit ->
            val radioButton = RadioButton(requireContext()).apply {
                id = View.generateViewId()
                text = unit
                tag = unit
                textSize = 16f
                setTextColor(textColor)

                setButtonTintList(colorStateList)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (index > 0) {
                        try {
                            marginStart = resources.getDimensionPixelSize(R.dimen.radio_button_margin)
                        } catch (e: Exception) {
                            marginStart = (8 * resources.displayMetrics.density).toInt()
                        }
                    }
                }

                val isCurrentUnit = initialReminder?.dosageUnit == unit
                val isDefaultUnit = unit == selectedUnit

                if (isCurrentUnit || isDefaultUnit) {
                    isChecked = true
                }
            }
            rgDosageUnitDynamic.addView(radioButton)
        }
    }
}