package com.example.obatin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.Calendar
import java.util.Locale
import android.content.Context
import android.app.AlarmManager
import android.app.PendingIntent
import kotlin.math.roundToInt
import com.google.firebase.auth.FirebaseAuth
import androidx.fragment.app.setFragmentResultListener
import java.text.SimpleDateFormat
import java.util.Date

// Menggunakan konstanta yang diimpor dari AppConstants.kt
import com.example.obatin.REQUEST_KEY_DAYS
import com.example.obatin.BUNDLE_KEY_DAYS
import com.example.obatin.REQUEST_KEY_CYCLE
import com.example.obatin.BUNDLE_KEY_CYCLE_VALUE
import com.example.obatin.BUNDLE_KEY_CYCLE_UNIT


class TambahObatActivity : AppCompatActivity(), ReminderListener
{

    // Deklarasi Views
    private lateinit var ivObat: ImageView
    private lateinit var etNamaObat: EditText
    private lateinit var etCatatan: EditText
    private lateinit var cbTidakIsiUlang: CheckBox
    private lateinit var cbYaIsiUlang: CheckBox
    private lateinit var btnTambahWaktuDosis: Button
    private lateinit var reminderContainer: LinearLayout
    private lateinit var tvMedicineType: TextView
    private lateinit var tvEndDate: TextView

    private lateinit var rgFrekuensi: RadioGroup
    private lateinit var btnSimpan: Button

    // Layout Detail frekuensi
    private lateinit var detailHariTertentu: LinearLayout
    private lateinit var rbHariTertentu: RadioButton
    private lateinit var rbSetiapX: RadioButton
    private lateinit var rbSetiapHari: RadioButton
    private lateinit var rbSesuaiKebutuhan: RadioButton

    private lateinit var tvSelectedDays: TextView
    private lateinit var tvCycleStatus: TextView


    // Firebase
    private lateinit var database: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth

    // State
    private var currentUserId: String? = null
    private val remindersList = mutableListOf<Reminder>()
    private var selectedDaysString: String? = null
    private var cycleValue: Int? = null
    private var cycleUnit: String? = null
    private var selectedMedicineType: MedicineType = MedicineType.PIL // Asumsi default PIL
    private var endDateCalendar: Calendar? = null
    private var medicineToEdit: Medicine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_obat)

        if (savedInstanceState != null) {
            selectedDaysString = savedInstanceState.getString("SELECTED_DAYS")
            cycleValue = savedInstanceState.getInt("CYCLE_VALUE").takeIf { it > 0 }
            cycleUnit = savedInstanceState.getString("CYCLE_UNIT")
        }


        firebaseAuth = FirebaseAuth.getInstance()
        currentUserId = firebaseAuth.currentUser?.uid
        database = FirebaseDatabase.getInstance()

        val incomingMedicineId = intent.getStringExtra("MEDICINE_ID")

        initializeViews()
        setupFragmentResultListeners()
        setupListeners()
        refreshReminderContainer()

        updateMedicineTypeDisplay()
        updateDayAndCycleDisplay()


        if (incomingMedicineId != null && currentUserId != null) {
            loadDataForEdit(incomingMedicineId, currentUserId!!)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SELECTED_DAYS", selectedDaysString)
        outState.putInt("CYCLE_VALUE", cycleValue ?: 0)
        outState.putString("CYCLE_UNIT", cycleUnit)
    }

    private fun setupFragmentResultListeners() {
        // Menggunakan konstanta yang diimpor
        supportFragmentManager.setFragmentResultListener(REQUEST_KEY_DAYS, this) { _, bundle ->
            val selectedDays = bundle.getString(BUNDLE_KEY_DAYS)
            if (selectedDays != null) {
                handleDaysSelected(selectedDays)
            }
        }

        // Menggunakan konstanta yang diimpor
        supportFragmentManager.setFragmentResultListener(REQUEST_KEY_CYCLE, this) { _, bundle ->
            val value = bundle.getInt(BUNDLE_KEY_CYCLE_VALUE, 0)
            val unit = bundle.getString(BUNDLE_KEY_CYCLE_UNIT)

            if (value > 0 && unit != null) {
                handleCycleSelected(value, unit)
            }
        }
    }


    private fun initializeViews() {
        ivObat = findViewById(R.id.iv_obat)
        etNamaObat = findViewById(R.id.et_nama_obat)
        rgFrekuensi = findViewById(R.id.rg_frekuensi)
        btnSimpan = findViewById(R.id.btn_simpan)
        etCatatan = findViewById(R.id.et_catatan)
        cbTidakIsiUlang = findViewById(R.id.cb_tidak_isi_ulang)
        cbYaIsiUlang = findViewById(R.id.cb_ya_isi_ulang)
        btnTambahWaktuDosis = findViewById(R.id.btn_tambah_waktu_dosis)
        reminderContainer = findViewById(R.id.reminder_container)

        detailHariTertentu = findViewById(R.id.layout_hari_tertentu_wrapper)

        rbHariTertentu = findViewById(R.id.rb_hari_tertentu)
        rbSetiapX = findViewById(R.id.rb_setiap_x)
        rbSetiapHari = findViewById(R.id.rb_setiap_hari)
        rbSesuaiKebutuhan = findViewById(R.id.rb_sesuai_kebutuhan)


        tvMedicineType = findViewById(R.id.tv_medicine_type)
        tvEndDate = findViewById(R.id.tv_end_date)

        tvSelectedDays = findViewById(R.id.tv_selected_days)
        tvCycleStatus = findViewById(R.id.tv_cycle_status)
    }

    private fun loadDataForEdit(medicineId: String, userId: String) {
        val medicineRef = database.getReference("medicines").child(userId).child(medicineId)

        medicineRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val medicine = snapshot.getValue(Medicine::class.java)

                if (medicine != null) {
                    medicineToEdit = medicine
                    bindDataToViews(medicine)
                    btnSimpan.text = "SIMPAN PERUBAHAN"
                    Toast.makeText(this@TambahObatActivity, "Mode Edit: Memuat ${medicine.name}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@TambahObatActivity, "Data obat tidak ditemukan.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TambahObatActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        })
    }

    private fun bindDataToViews(medicine: Medicine) {
        etNamaObat.setText(medicine.name)
        etCatatan.setText(medicine.note)

        cbYaIsiUlang.isChecked = medicine.refillNeeded
        cbTidakIsiUlang.isChecked = !medicine.refillNeeded

        val type = MedicineType.values().find { it.displayName == medicine.medicineType } ?: MedicineType.PIL
        selectedMedicineType = type
        updateMedicineTypeDisplay()

        medicine.endDate?.let {
            tvEndDate.text = it
        }

        remindersList.clear()
        remindersList.addAll(medicine.reminders)
        refreshReminderContainer()

        when (medicine.frequencyType) {
            "SETIAP_HARI" -> findViewById<RadioButton>(R.id.rb_setiap_hari).isChecked = true
            "HARI_TERTENTU" -> {
                findViewById<RadioButton>(R.id.rb_hari_tertentu).isChecked = true
                selectedDaysString = medicine.daysOfWeek
                detailHariTertentu.visibility = View.VISIBLE
            }
            "SETIAP_X" -> {
                findViewById<RadioButton>(R.id.rb_setiap_x).isChecked = true
                cycleValue = medicine.cycleValue
                cycleUnit = medicine.cycleUnit
                tvCycleStatus.visibility = View.VISIBLE
            }
            "SESUAI_KEBUTUHAN" -> findViewById<RadioButton>(R.id.rb_sesuai_kebutuhan).isChecked = true
        }

        updateDayAndCycleDisplay()
    }

    private fun updateDayAndCycleDisplay() {
        // Logika Hari Tertentu
        if (selectedDaysString.isNullOrEmpty()) {
            tvSelectedDays.text = "Pilih hari dengan menekan Hari Tertentu di atas"
        } else {
            tvSelectedDays.text = selectedDaysString?.replace(",", ", ")
        }

        // Logika Siklus
        if (cycleValue != null && cycleUnit != null) {
            tvCycleStatus.text = "Siklus: setiap $cycleValue $cycleUnit"
        } else {
            tvCycleStatus.text = "Siklus: belum diatur"
        }

        // Kontrol Visibilitas
        tvCycleStatus.visibility = if (findViewById<RadioButton>(R.id.rb_setiap_x).isChecked) View.VISIBLE else View.GONE
        detailHariTertentu.visibility = if (findViewById<RadioButton>(R.id.rb_hari_tertentu).isChecked) View.VISIBLE else View.GONE
    }


    private fun setupListeners() {
        btnSimpan.setOnClickListener { simpanDataObat() }

        ivObat.setOnClickListener { showMedicineTypeDialog() }
        tvMedicineType.setOnClickListener { showMedicineTypeDialog() }

        btnTambahWaktuDosis.setOnClickListener {
            val allowedUnits = selectedMedicineType.dosageUnitTypes
            val dialog = ReminderDialogFragment.newInstance(null, allowedUnits.toTypedArray())
            dialog.show(supportFragmentManager, "ReminderInput")
        }

        // Listener HARI TERTENTU - Buka dialog hanya jika tombolnya di-check (RadioGroup handles check)
        rbHariTertentu.setOnClickListener {
            if (rbHariTertentu.isChecked) {
                showDaySelectorDialog()
            }
        }

        // Listener SETIAP X - Buka dialog hanya jika tombolnya di-check
        rbSetiapX.setOnClickListener {
            if (rbSetiapX.isChecked) {
                showCycleSelectorDialog()
            }
        }

        // Listener Utama RadioGroup
        rgFrekuensi.setOnCheckedChangeListener { group: RadioGroup, checkedId: Int -> handleFrequencyChange(checkedId) }

        cbYaIsiUlang.setOnCheckedChangeListener { _, isChecked -> if (isChecked) cbTidakIsiUlang.isChecked = false }
        cbTidakIsiUlang.setOnCheckedChangeListener { _, isChecked -> if (isChecked) cbYaIsiUlang.isChecked = false }

        tvEndDate.setOnClickListener { showDatePickerDialog() }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dialog = android.app.DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                endDateCalendar = selectedDate
                tvEndDate.text = formatCalendarToDateString(selectedDate)
            },
            year,
            month,
            day
        )
        dialog.show()
    }

    private fun formatCalendarToDateString(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
        return dateFormat.format(calendar.time)
    }

    private fun showMedicineTypeDialog() {
        val types = MedicineType.values().map { it.displayName }.toTypedArray()

        android.app.AlertDialog.Builder(this)
            .setTitle("Pilih Jenis Obat")
            .setItems(types) { dialog, which ->
                val selectedName = types[which]

                selectedMedicineType = MedicineType.values().find { it.displayName == selectedName } ?: MedicineType.PIL

                updateMedicineTypeDisplay()
                remindersList.clear()
                refreshReminderContainer()
            }
            .show()
    }

    override fun updateMedicineTypeDisplay() {
        tvMedicineType.text = selectedMedicineType.displayName
        updateMedicineIcon(selectedMedicineType.displayName)
    }

    private fun updateMedicineIcon(medicineType: String) {
        // Asumsi resources icon_pil, icon_capsul, dll. ada.
        val iconResId = when (medicineType) {
            "Pil" -> R.drawable.icon_pil
            "Kapsul" -> R.drawable.icon_capsul
            "Sirup" -> R.drawable.icon_sirup
            "Suntik" -> R.drawable.icon_suntik
            else -> R.drawable.icon_pil
        }
        ivObat.setImageResource(iconResId)
    }

    private fun handleFrequencyChange(checkedId: Int) {
        // Logika Frekuensi yang diperbaiki
        when (checkedId) {
            R.id.rb_hari_tertentu -> {
                if (selectedDaysString.isNullOrEmpty()) {
                    showDaySelectorDialog()
                }
            }
            R.id.rb_setiap_x -> {
                if (cycleValue == null || cycleUnit == null) {
                    showCycleSelectorDialog()
                }
            }
            else -> {
                // Reset state untuk setiap hari/sesuai kebutuhan
                selectedDaysString = null
                cycleValue = null
                cycleUnit = null
            }
        }

        // Panggil display update untuk mengatur visibilitas yang benar
        updateDayAndCycleDisplay()
    }

    private fun showDaySelectorDialog() {
        val dialog = DaySelectorDialogFragment()
        dialog.show(supportFragmentManager, "DaySelector")
    }

    private fun showCycleSelectorDialog() {
        val dialog = CycleSelectorDialogFragment()
        dialog.show(supportFragmentManager, "CycleSelector")
    }

    private fun handleDaysSelected(selectedDays: String) {
        selectedDaysString = selectedDays
        tvSelectedDays.text = selectedDays.replace(",", ", ")
        Toast.makeText(this, "Hari terpilih: $selectedDays", Toast.LENGTH_LONG).show()
        updateDayAndCycleDisplay() // Refresh tampilan setelah data diterima
    }

    private fun handleCycleSelected(value: Int, unit: String) {
        cycleValue = value
        cycleUnit = unit
        tvCycleStatus.text = "Siklus: setiap $value $unit"
        Toast.makeText(this, "Siklus diatur: setiap $value $unit", Toast.LENGTH_LONG).show()
        updateDayAndCycleDisplay() // Refresh tampilan setelah data diterima
    }

    private fun formatDosageDisplay(dosage: Double, unit: String): String {
        if (unit == "Butir") {
            return String.format(Locale.ROOT, "%.0f", dosage)
        } else {
            return String.format(Locale.ROOT, "%.1f", dosage)
        }
    }

    override fun onReminderAdded(reminder: Reminder) {
        remindersList.add(reminder)
        remindersList.sortBy { it.time }
        refreshReminderContainer()
        Toast.makeText(this, "Pengingat ditambahkan: ${reminder.time}", Toast.LENGTH_SHORT).show()
    }

    override fun onReminderUpdated(oldItem: Reminder, newItem: Reminder) {
        val index = remindersList.indexOf(oldItem)
        if (index != -1) {
            remindersList[index] = newItem
            remindersList.sortBy { it.time }
            refreshReminderContainer()
            Toast.makeText(this, "Pengingat diperbarui: ${newItem.time}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error: Pengingat lama tidak ditemukan.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshReminderContainer() {
        reminderContainer.removeAllViews()
        remindersList.forEach { reminder ->
            addReminderCardToView(reminder)
        }
    }

    private fun addReminderCardToView(reminder: Reminder) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_reminder, reminderContainer, false)

        val tvTime = view.findViewById<TextView>(R.id.tv_reminder_time)
        val tvDosage = view.findViewById<TextView>(R.id.tv_reminder_dosage)

        val btnDelete = view.findViewById<ImageButton>(R.id.btn_delete_reminder)
        val btnEdit = view.findViewById<ImageButton>(R.id.btn_edit_reminder)

        val safeDosageAmount = reminder.dosageAmount ?: 0.0
        val dosageText = formatDosageDisplay(safeDosageAmount, reminder.dosageUnit ?: "mg")

        tvTime.text = reminder.time
        tvDosage.text = "💊 $dosageText ${reminder.dosageUnit}"

        btnDelete?.setOnClickListener {
            // BATALKAN ALARM sebelum menghapus pengingat dari daftar
            cancelReminder(medicineToEdit?.medicineId ?: "", reminder.time ?: "")
            remindersList.remove(reminder)
            refreshReminderContainer()
            Toast.makeText(this, "Pengingat ${reminder.time} dihapus.", Toast.LENGTH_SHORT).show()
        }

        btnEdit?.setOnClickListener {
            val allowedUnits = selectedMedicineType.dosageUnitTypes
            val dialog = ReminderDialogFragment.newInstance(reminder, allowedUnits.toTypedArray())
            dialog.show(supportFragmentManager, "EditReminder")
        }

        reminderContainer.addView(view)
    }


    private fun simpanDataObat() {
        val namaObat = etNamaObat.text.toString().trim()
        val refillNeeded = cbYaIsiUlang.isChecked
        val notes = etCatatan.text.toString().trim()
        val medicineType = selectedMedicineType.displayName

        if (currentUserId == null || namaObat.isEmpty() || remindersList.isEmpty()) {
            Toast.makeText(this, "Harap lengkapi Nama Obat dan Pengingat waktu.", Toast.LENGTH_LONG).show()
            return
        }

        val checkedId = rgFrekuensi.checkedRadioButtonId
        val frequencyType = when (checkedId) {
            R.id.rb_setiap_hari -> "SETIAP_HARI"
            R.id.rb_hari_tertentu -> "HARI_TERTENTU"
            R.id.rb_setiap_x -> "SETIAP_X"
            R.id.rb_sesuai_kebutuhan -> "SESUAI_KEBUTUHAN"
            else -> {
                Toast.makeText(this, "Harap pilih frekuensi konsumsi.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        var daysOfWeek: String? = null
        if (frequencyType == "HARI_TERTENTU") {
            if (selectedDaysString.isNullOrEmpty()) {
                // FIX: Jika Hari Tertentu dipilih tetapi belum ada hari yang diklik/disimpan
                Toast.makeText(this, "Pilih hari konsumsi obat.", Toast.LENGTH_SHORT).show()
                return
            }
            daysOfWeek = selectedDaysString
        }

        if (frequencyType == "SETIAP_X") {
            if (cycleValue == null || cycleUnit == null) {
                Toast.makeText(this, "Atur siklus konsumsi.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        performFinalSave(namaObat, refillNeeded, notes, frequencyType, daysOfWeek, medicineType)
    }


    private fun performFinalSave(namaObat: String, refillNeeded: Boolean, notes: String, frequencyType: String?, daysOfWeek: String?, medicineType: String) {

        val endDateString = endDateCalendar?.let { formatCalendarToDateString(it) }

        val finalMedicineId = medicineToEdit?.medicineId ?: database.getReference("medicines").push().key

        val oldReminders = medicineToEdit?.reminders?.toList() ?: emptyList()
        val safeReminders = remindersList.map { it }

        val medicine = Medicine(
            medicineId = finalMedicineId,
            userId = currentUserId,
            name = namaObat,
            note = notes,
            refillNeeded = refillNeeded,
            imageUrl = medicineToEdit?.imageUrl,
            frequencyType = frequencyType,
            daysOfWeek = daysOfWeek,
            reminders = safeReminders,
            medicineType = medicineType,
            endDate = endDateString,
            creationTimestamp = medicineToEdit?.creationTimestamp ?: System.currentTimeMillis(),
            cycleValue = cycleValue,
            cycleUnit = cycleUnit
        )

        if (finalMedicineId != null && currentUserId != null) {
            database.getReference("medicines")
                .child(currentUserId!!)
                .child(finalMedicineId)
                .setValue(medicine)
                .addOnSuccessListener {
                    Toast.makeText(this, "Obat '$namaObat' berhasil ${if (medicineToEdit != null) "diperbarui" else "disimpan"}!", Toast.LENGTH_LONG).show()

                    // 1. BATALKAN SEMUA ALARM LAMA TERKAIT ID INI (penting untuk mode EDIT)
                    oldReminders.forEach { oldReminder ->
                        cancelReminder(finalMedicineId, oldReminder.time ?: "")
                    }

                    // 2. SETEL ALARM BARU jika pengingat diaktifkan
                    if (cbYaIsiUlang.isChecked) {
                        for (reminder in remindersList) {
                            scheduleReminder(reminder, namaObat, finalMedicineId)
                        }
                    }

                    // FIX REFRESH: Memberi sinyal sukses kembali ke HomeActivity
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menyimpan data ke database: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun cancelReminder(medicineId: String, reminderTime: String) {
        if (medicineId.isEmpty() || reminderTime.isEmpty()) return

        val stableReminderId = (medicineId + reminderTime).hashCode()
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, ReminderBroadcastReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            stableReminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }


    private fun scheduleReminder(reminder: Reminder, medicineName: String, medicineId: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val parts = reminder.time?.split(":") ?: return
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val dosageText = formatDosageDisplay(reminder.dosageAmount ?: 0.0, reminder.dosageUnit ?: "mg")
        val dosageUnit = reminder.dosageUnit ?: "mg"

        val stableReminderId = (medicineId + reminder.time).hashCode()

        val intent = Intent(this, ReminderBroadcastReceiver::class.java).apply {
            putExtra("MEDICINE_NAME", medicineName)
            putExtra("REMINDER_TIME", reminder.time)
            putExtra("DOSAGE_TEXT", dosageText)
            putExtra("DOSAGE_UNIT", dosageUnit)
            putExtra("MEDICINE_ID", medicineId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            stableReminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // FIX KRITIS: Menggunakan setExactAndAllowWhileIdle untuk akurasi TEPAT WAKTU
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Toast.makeText(this, "Pengingat disetel untuk ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}", Toast.LENGTH_SHORT).show()

        // CATATAN: Logika Penjadwalan Ulang Harian (setelah 24 jam) HARUS ADA di ReminderBroadcastReceiver.kt
    }
}