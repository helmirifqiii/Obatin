package com.example.obatin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import java.util.Calendar
import java.util.concurrent.TimeUnit
import android.graphics.Color
import android.view.View

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import androidx.appcompat.app.AlertDialog

// Import MPAndroidChart Classes
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.components.YAxis


class CheckListActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var medicineId: String? = null
    private var currentUserId: String? = null
    private var currentMedicine: Medicine? = null

    // Views
    private lateinit var iconPill: ImageView
    private lateinit var textDate: TextView
    private lateinit var textMedicineName: TextView
    private lateinit var textCardStatus: TextView
    private lateinit var textFrequencyContent: TextView
    private lateinit var textNotesContent: TextView
    private lateinit var textEndDateContent: TextView
    private lateinit var buttonEditMain: Button
    private lateinit var buttonHapus: Button
    private lateinit var recyclerViewReminders: RecyclerView
    private lateinit var reminderAdapter: ReminderDetailAdapter
    private lateinit var cardStatus: MaterialCardView
    private lateinit var layoutStatusIndicator: LinearLayout
    private lateinit var adherenceChart: BarChart
    private lateinit var labelKepatuhanJudul: TextView

    private var dosesTakenToday: MutableMap<String, AdherenceLog> = mutableMapOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_list)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        currentUserId = firebaseAuth.currentUser?.uid

        medicineId = intent.getStringExtra("MEDICINE_ID")

        initializeViews()
        setupRecyclerView()
        setupComplianceChart()

        setupListeners()
    }

    // FIX 1: MENAMBAHKAN ONRESUME UNTUK AUTO-REFRESH
    override fun onResume() {
        super.onResume()
        if (medicineId != null && currentUserId != null) {
            // Memuat ulang data setiap kali Activity kembali ke latar depan (setelah Simpan Perubahan)
            loadMedicineDetails(currentUserId!!, medicineId!!)
        }
    }

    private fun initializeViews() {
        iconPill = findViewById(R.id.icon_pill)
        textDate = findViewById(R.id.text_date)
        textMedicineName = findViewById(R.id.text_medicine_name)
        textFrequencyContent = findViewById(R.id.text_refill_date)
        textNotesContent = findViewById(R.id.text_notes_content)
        textEndDateContent = findViewById(R.id.text_end_date_content)

        buttonEditMain = findViewById(R.id.button_edit_main)
        buttonHapus = findViewById(R.id.button_hapus)
        recyclerViewReminders = findViewById(R.id.recycler_view_reminders)
        cardStatus = findViewById(R.id.card_sudah_diminum)
        layoutStatusIndicator = findViewById(R.id.layout_status_indicator)
        adherenceChart = findViewById(R.id.adherence_chart)
        labelKepatuhanJudul = findViewById(R.id.label_kepatuhan_judul)
        textCardStatus = findViewById(R.id.text_card_status)

        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        textDate.text = dateFormat.format(Date())

        labelKepatuhanJudul.text = "Kepatuhan Minum Obat"
    }

    private fun setupRecyclerView() {
        reminderAdapter = ReminderDetailAdapter()
        recyclerViewReminders.layoutManager = LinearLayoutManager(this)
        recyclerViewReminders.adapter = reminderAdapter
    }

    private fun setupComplianceChart() {
        adherenceChart.description.isEnabled = false
        adherenceChart.legend.isEnabled = false
        adherenceChart.setScaleEnabled(false)
        adherenceChart.setTouchEnabled(false)

        val xAxis = adherenceChart.xAxis

        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.setCenterAxisLabels(false)

        xAxis.setLabelCount(7, false)
        xAxis.labelRotationAngle = -45f
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.yOffset = 10f

        val leftAxis = adherenceChart.axisLeft
        leftAxis.setDrawGridLines(true)
        // FIX 2: MENGHILANGKAN AXIS MINUS (0-100%)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 100f
        leftAxis.zeroLineWidth = 1f
        leftAxis.zeroLineColor = Color.GRAY
        leftAxis.setDrawZeroLine(true)

        adherenceChart.axisRight.isEnabled = false
        adherenceChart.setNoDataText("Memuat data kepatuhan...")
    }


    private fun setupListeners() {
        buttonEditMain.setOnClickListener {
            if (medicineId != null) {
                // FIX 3: Memastikan Intent memanggil TambahObatActivity dengan benar
                val intent = Intent(this, TambahObatActivity::class.java).apply {
                    putExtra("MEDICINE_ID", medicineId)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Tidak dapat mengedit. ID Obat tidak valid.", Toast.LENGTH_SHORT).show()
            }
        }

        buttonHapus.setOnClickListener {
            if (medicineId != null && currentUserId != null) {
                AlertDialog.Builder(this)
                    .setTitle("Akhiri Jadwal Obat")
                    .setMessage("Apakah Anda yakin ingin MENGAKHIRKAN jadwal pengingat ${currentMedicine?.name ?: "obat"} ini? Data ini akan dipindahkan ke Riwayat Obat dan pengingat akan dihentikan.")
                    .setPositiveButton("Akhiri Jadwal") { dialog, which ->
                        archiveMedicine(currentUserId!!, medicineId!!, currentMedicine?.reminders ?: emptyList())
                    }
                    .setNegativeButton("Batal", null)
                    .show()

            } else {
                Toast.makeText(this, "Tidak dapat menghapus. ID Obat atau Pengguna tidak valid.", Toast.LENGTH_SHORT).show()
            }
        }

        layoutStatusIndicator.setOnClickListener {
            if (currentMedicine != null && currentUserId != null) {
                attemptLogDoseToday(currentMedicine!!.reminders)
            }
        }
    }

    private fun cancelReminder(medicineId: String, reminderTime: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val stableReminderId = (medicineId + reminderTime).hashCode()

        val intent = Intent(this, ReminderBroadcastReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            stableReminderId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }


    private fun archiveMedicine(userId: String, medicineId: String, reminders: List<Reminder>) {
        reminders.forEach { reminder ->
            cancelReminder(medicineId, reminder.time.orEmpty())
        }

        val medicineRef = database.getReference("medicines").child(userId).child(medicineId)

        val updates = hashMapOf<String, Any?>(
            "reminders" to emptyList<Reminder>(),
            "endDate" to SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(Calendar.getInstance().time),
            "frequencyType" to "DIARSIPKAN"
        )

        medicineRef.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "${currentMedicine?.name} berhasil diarsipkan dan masuk Riwayat Obat.", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengarsipkan obat: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun attemptLogDoseToday(reminders: List<Reminder>) {
        val now = Calendar.getInstance()
        val currentHourMinute = SimpleDateFormat("HH:mm", Locale.ROOT).format(now.time)

        val scheduledRemindersToday = currentMedicine?.reminders?.sortedBy { it.time } ?: emptyList()

        // Cari dosis yang seharusnya sudah diambil (waktu terjadwal <= waktu sekarang)
        val pendingReminders = scheduledRemindersToday.filter { reminder ->
            val reminderCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, reminder.time?.substringBefore(':')?.toInt() ?: 0)
                set(Calendar.MINUTE, reminder.time?.substringAfter(':')?.toInt() ?: 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Dosis yang harusnya sudah diambil (waktu terjadwal <= waktu sekarang)
            // DAN belum dicatat
            (reminderCal.timeInMillis <= now.timeInMillis) && !dosesTakenToday.containsKey(reminder.time)
        }

        val nextDueReminder = pendingReminders.firstOrNull()


        if (nextDueReminder != null) {
            logAdherenceStatus(
                reminderTime = nextDueReminder.time!!,
                medicineId = medicineId!!,
                currentUserId = currentUserId!!
            )
        } else {
            // Jika tidak ada dosis pending (semua sudah dicatat atau belum waktunya)
            Toast.makeText(this, "Semua dosis hari ini sudah dicatat atau belum waktunya!", Toast.LENGTH_LONG).show()
        }
    }


    private fun loadMedicineDetails(userId: String, medicineId: String) {
        val medicineRef = database.getReference("medicines").child(userId).child(medicineId)

        medicineRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val medicine = snapshot.getValue(Medicine::class.java)

                if (medicine != null) {
                    currentMedicine = medicine
                    bindMedicineData(medicine)
                    loadAdherenceHistory(userId, medicineId)
                } else {
                    Toast.makeText(this@CheckListActivity, "Data obat tidak ditemukan.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CheckListActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun bindMedicineData(medicine: Medicine) {
        textMedicineName.text = medicine.name

        val medicineType = MedicineType.entries.find { it.displayName == medicine.medicineType } ?: MedicineType.PIL
        iconPill.setImageResource(medicineType.drawableResId)

        if (medicine.reminders.isNotEmpty()) {
            reminderAdapter.submitList(medicine.reminders)
        }

        val frequencyText = when (medicine.frequencyType) {
            "SETIAP_HARI" -> getString(R.string.frekuensi_setiap_hari)
            "HARI_TERTENTU" -> "${getString(R.string.frekuensi_hari_tertentu)}: ${medicine.daysOfWeek ?: "Tidak diatur"}"
            "SETIAP_X" -> "Setiap ${medicine.cycleValue ?: "X"} ${medicine.cycleUnit ?: "hari"}"
            "SESUAI_KEBUTUHAN" -> getString(R.string.frekuensi_sesuai_kebutuhan)
            "DIARSIPKAN" -> "Diarsipkan"
            else -> "Tidak diketahui"
        }
        textFrequencyContent.text = frequencyText
        textNotesContent.text = if (medicine.note.isNullOrEmpty()) "Tidak ada" else medicine.note
        textEndDateContent.text = medicine.endDate ?: "Tidak ada"
    }

    private fun calculateAdherenceStatus(reminderTimeStr: String, actualTime: Calendar): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.ROOT)
        val scheduledTime = Calendar.getInstance().apply {
            time = formatter.parse(reminderTimeStr) ?: Date()

            set(
                actualTime.get(Calendar.YEAR),
                actualTime.get(Calendar.MONTH),
                actualTime.get(Calendar.DAY_OF_MONTH)
            )
        }

        val diffMillis = actualTime.timeInMillis - scheduledTime.timeInMillis
        val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis).toInt()

        val TOLERANCE_MINUTES_ON_TIME = 30 // Tepat waktu jika dalam 30 menit
        val LATE_THRESHOLD_MINUTES = 60 // Batas Telat yang masih bisa diterima (misalnya)

        return when {
            // TEPAT WAKTU (On-Time): Dalam 30 menit sebelum atau sesudah
            diffMinutes >= -TOLERANCE_MINUTES_ON_TIME && diffMinutes <= TOLERANCE_MINUTES_ON_TIME -> {
                "TEPAT_WAKTU"
            }
            // TELAT DALAM TOLERANSI (Late - Dalam Toleransi): Lebih dari 30 menit, tapi kurang dari 60 menit
            diffMinutes > TOLERANCE_MINUTES_ON_TIME && diffMinutes <= LATE_THRESHOLD_MINUTES -> {
                "TELAT_DALAM_TOLERANSI"
            }
            // GAGAL/TELAT (Failed/Late): Lebih dari 60 menit setelah jadwal
            diffMinutes > LATE_THRESHOLD_MINUTES -> {
                "GAGAL_TELAT"
            }
            // TERLALU CEPAT (Early): Lebih dari 30 menit sebelum jadwal
            diffMinutes < -TOLERANCE_MINUTES_ON_TIME -> {
                "TERLALU_CEPAT"
            }
            else -> "ERROR_UNKNOWN"
        }
    }

    private fun logAdherenceStatus(reminderTime: String, medicineId: String, currentUserId: String) {
        val actualTakenTime = Calendar.getInstance()
        val actualTakenTimestamp = actualTakenTime.timeInMillis
        val actualTakenTimeStr = SimpleDateFormat("HH:mm", Locale.ROOT).format(actualTakenTime.time)

        if (dosesTakenToday.containsKey(reminderTime)) {
            Toast.makeText(this, "Dosis pukul $reminderTime sudah dicatat hari ini. Tidak ada log duplikat.", Toast.LENGTH_SHORT).show()
            return
        }

        val status = calculateAdherenceStatus(reminderTime, actualTakenTime)

        if (status == "TERLALU_CEPAT") {
            Toast.makeText(this, "Belum waktu minum obat pukul $reminderTime. Mohon tunggu.", Toast.LENGTH_LONG).show()
            return
        }

        val log = AdherenceLog(
            userId = currentUserId,
            medicineId = medicineId,
            reminderTime = reminderTime,
            actualTakenTimestamp = actualTakenTimestamp,
            actualTakenTime_str = actualTakenTimeStr,
            calculatedAdherenceStatus = status
        )

        val logRef = database.getReference("adherence_logs")
            .child(currentUserId)
            .push()

        logRef.setValue(log)
            .addOnSuccessListener {
                Toast.makeText(this, "Log Obat dicatat: $status", Toast.LENGTH_LONG).show()
                dosesTakenToday[reminderTime] = log
                // Refresh data setelah log berhasil disimpan
                loadAdherenceHistory(currentUserId!!, medicineId)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan log: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadAdherenceHistory(userId: String, medicineId: String) {
        val logsRef = database.getReference("adherence_logs").child(userId)
            .orderByChild("actualTakenTimestamp")
            .limitToLast(100)

        logsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adherenceLogs = mutableListOf<AdherenceLog>()
                snapshot.children.forEach { logSnapshot ->
                    val log = logSnapshot.getValue(AdherenceLog::class.java)
                    if (log != null && log.medicineId == medicineId) {
                        adherenceLogs.add(log)
                    }
                }

                dosesTakenToday.clear()
                val totalDosesScheduled = currentMedicine?.reminders?.size ?: 1
                val medicineCreationDateMillis = currentMedicine?.creationTimestamp ?: 0L

                // Gunakan SATU list untuk skor kepatuhan harian (0-100%)
                val adherenceEntries = mutableListOf<BarEntry>()
                val daysOfWeekLabels = mutableListOf<String>()

                val logsByDay = adherenceLogs.groupBy {
                    SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date(it.actualTakenTimestamp ?: 0L))
                }

                val percentagePerDose = if (totalDosesScheduled > 0) 100f / totalDosesScheduled.toFloat() else 0f


                for (i in 6 downTo 0) {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, -i)
                    val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(calendar.time)
                    val dayName = SimpleDateFormat("d MMM", Locale("id", "ID")).format(calendar.time)

                    val logsForDay = logsByDay[dateKey] ?: emptyList()
                    val dosesTakenTimes = logsForDay.map { it.reminderTime }.toSet()

                    var totalScoreForDay = 0f // Inisialisasi Skor Harian (0%)

                    // 1. Cek apakah obat sudah ada pada hari ini atau hari sebelumnya
                    if (calendar.timeInMillis >= medicineCreationDateMillis) {

                        // Hitung skor dari dosis yang DICATAT (TEPAT WAKTU / TELAT DALAM TOLERANSI)
                        logsForDay.forEach { log ->
                            when (log.calculatedAdherenceStatus) {
                                "TEPAT_WAKTU", "TELAT_DALAM_TOLERANSI" -> {
                                    totalScoreForDay += percentagePerDose // Dosis Patuh: 100% per dosis
                                }
                                "GAGAL_TELAT" -> {
                                    // Dosis Gagal Telat: 0% per dosis
                                }
                                else -> {}
                            }
                        }

                        // 2. Cek Dosis yang Terlewat Total (Missed Dose)
                        val isToday = i == 0
                        val scheduledTimesForDay = currentMedicine?.reminders?.map { it.time } ?: emptyList()

                        val missedDosesCount = scheduledTimesForDay.count { scheduledTime ->
                            val isRecorded = logsForDay.any { it.reminderTime == scheduledTime }
                            if (isRecorded) return@count false

                            val reminderCal = Calendar.getInstance().apply {
                                time = calendar.time
                                set(Calendar.HOUR_OF_DAY, scheduledTime?.substringBefore(':')?.toInt() ?: 0)
                                set(Calendar.MINUTE, scheduledTime?.substringAfter(':')?.toInt() ?: 0)
                                set(Calendar.SECOND, 0)
                            }

                            val now = Calendar.getInstance()

                            // Kriteria Terlewat: Jika sudah melewati ambang batas 4 jam
                            val thresholdMissed = reminderCal.clone() as Calendar
                            thresholdMissed.add(Calendar.HOUR_OF_DAY, 4)

                            // Hari Ini: Terlewat jika sudah lewat ambang batas 4 jam
                            if (isToday) {
                                return@count now.timeInMillis > thresholdMissed.timeInMillis
                            } else {
                                // Hari Sebelumnya: Semua yang tidak tercatat dianggap terlewat
                                return@count true
                            }
                        }

                        // Dosis yang terlewat tidak menambah skor (tetap 0)
                        // totalScoreForDay += 0f

                    } else {
                        // Jika obat BELUM ADA pada hari itu
                        totalScoreForDay = 0f
                    }


                    // PLOT HANYA SKOR KEPATUHAN POSITIF (0-100)
                    adherenceEntries.add(BarEntry((6 - i).toFloat(), totalScoreForDay.coerceIn(0f, 100f)))

                    daysOfWeekLabels.add(dayName)

                    if (i == 0) { // Hanya hari ini
                        logsForDay.forEach { log ->
                            dosesTakenToday[log.reminderTime.orEmpty()] = log
                        }
                    }
                }

                // 4. Tampilkan Grafik
                if (adherenceEntries.isNotEmpty()) {

                    val dataSets = mutableListOf<IBarDataSet>()

                    val adherenceSet = BarDataSet(adherenceEntries, "Skor Kepatuhan").apply {
                        color = Color.parseColor("#55AD9B")
                        valueTextColor = Color.BLACK
                        valueTextSize = 10f
                    }
                    dataSets.add(adherenceSet)

                    val barData = BarData(dataSets)
                    barData.barWidth = 0.8f
                    barData.setDrawValues(false)

                    adherenceChart.xAxis.valueFormatter = IndexAxisValueFormatter(daysOfWeekLabels)
                    adherenceChart.data = barData
                    adherenceChart.invalidate()

                    adherenceChart.visibility = View.VISIBLE
                    labelKepatuhanJudul.visibility = View.VISIBLE
                } else {
                    adherenceChart.visibility = View.GONE
                    labelKepatuhanJudul.visibility = View.GONE
                    adherenceChart.setNoDataText("Belum ada data kepatuhan yang dicatat.")
                }

                // Update Status Card
                val totalDosesTakenToday = dosesTakenToday.size
                val isDoseRemaining = totalDosesTakenToday < totalDosesScheduled

                layoutStatusIndicator.isEnabled = isDoseRemaining

                if (isDoseRemaining) {
                    textCardStatus.text = "KLIK UNTUK CATAT DOSIS"
                    cardStatus.setCardBackgroundColor(Color.parseColor("#FF9800"))
                } else {
                    textCardStatus.text = "SEMUA DOSIS DICATAT"
                    cardStatus.setCardBackgroundColor(Color.parseColor("#4CAF50"))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CheckListActivity, "Gagal memuat log grafik: ${error.message}", Toast.LENGTH_LONG).show()
                adherenceChart.visibility = View.GONE
                labelKepatuhanJudul.visibility = View.GONE
            }
        })
    }
}