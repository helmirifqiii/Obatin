package com.example.obatin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.app.Activity
import com.example.obatin.TambahObatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageButton
import android.widget.EditText
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.Locale
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit


class HomeActivity : AppCompatActivity() {

    private val USER_ID_KEY = "USER_ID"
    private val USER_NAME_KEY = "USER_NAME"

    private lateinit var tvGreeting: TextView
    private lateinit var btnTambah: FloatingActionButton
    private lateinit var btnPengaturan: ImageButton
    private lateinit var recyclerViewObat: RecyclerView
    private lateinit var medicineAdapter: MedicineAdapter
    private lateinit var tvEmptyStatus: TextView
    private lateinit var etSearch: EditText
    private lateinit var tvTitleActiveObat: TextView

    // FIX: Hapus deklarasi btnPrevObat dan btnNextObat


    private lateinit var database: FirebaseDatabase
    private var currentUserId: String? = null
    private var currentUserName: String? = null

    private val medicineList = mutableListOf<Medicine>()
    private val filteredList = mutableListOf<Medicine>()
    private val firebaseAuth = FirebaseAuth.getInstance()


    // Variabel Result Launcher untuk Pengaturan Akun
    private val settingsResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val newName = data?.getStringExtra(USER_NAME_KEY)

            if (newName != null) {
                currentUserName = newName
                setGreetingMessage()
                Toast.makeText(this, "Nama di layar utama diperbarui!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // LAUNCHER UNTUK TAMBAH/EDIT OBAT
    private val addMedicineResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (currentUserId != null) {
                loadMedicines(currentUserId!!)
                Toast.makeText(this, "Daftar obat dimuat ulang via Launcher.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        database = FirebaseDatabase.getInstance()

        // 🔹 Inisialisasi Views
        tvGreeting = findViewById(R.id.tvGreeting)
        btnTambah = findViewById(R.id.btnTambah)
        btnPengaturan = findViewById(R.id.btnPengaturan)
        tvEmptyStatus = findViewById(R.id.tvEmptyMessage)
        recyclerViewObat = findViewById(R.id.recyclerViewObat)
        etSearch = findViewById(R.id.etSearch)
        tvTitleActiveObat = findViewById(R.id.tvTitleActiveObat)

        // FIX: Hapus inisialisasi btnPrevObat dan btnNextObat


        // 🔹 Atur RecyclerView
        medicineAdapter = MedicineAdapter()

        // ⭐️ FIX KRITIS: MEMAKSA LAYOUT MANAGER KUSTOM VERTIKAL ⭐️
        // Mengembalikan ke WrapContentLinearLayoutManager Vertikal untuk mengatasi clipping
        val customLayoutManager = WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerViewObat.layoutManager = customLayoutManager
        recyclerViewObat.adapter = medicineAdapter


        // 🔹 Mendapatkan data user dari Firebase Auth
        currentUserId = firebaseAuth.currentUser?.uid

        // Panggil pemuatan data di onCreate untuk tampilan awal
        if (currentUserId != null) {
            loadUserNameFromDB(currentUserId!!)
        } else {
            setGreetingMessage()
        }


        // 🔹 Tombol Tambah
        btnTambah.setOnClickListener {
            if (currentUserId != null) {
                val intent = Intent(this, TambahObatActivity::class.java).apply {
                    putExtra(USER_ID_KEY, currentUserId)
                }
                addMedicineResultLauncher.launch(intent)
            } else {
                Toast.makeText(this, "Aplikasi sedang memuat data pengguna. Coba lagi.", Toast.LENGTH_SHORT).show()
            }
        }

        // FIX: Hapus Listener Tombol Navigasi Horizontal


        // 🔹 PENGATURAN
        btnPengaturan.setOnClickListener {
            if (currentUserId != null && currentUserName != null) {
                val intent = Intent(this, AccountSettingsActivity::class.java).apply {
                    putExtra(USER_ID_KEY, currentUserId)
                    putExtra(USER_NAME_KEY, currentUserName)
                }
                settingsResultLauncher.launch(intent)
            } else {
                Toast.makeText(this, "Aplikasi sedang memuat data pengguna. Coba lagi.", Toast.LENGTH_SHORT).show()
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterMedicines(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // FIX: Hapus updateNavigationButtons()


    private fun loadUserNameFromDB(userId: String) {
        database.getReference("users").child(userId).child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentUserName = snapshot.getValue(String::class.java)
                    setGreetingMessage()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@HomeActivity, "Gagal memuat nama pengguna.", Toast.LENGTH_SHORT).show()
                    currentUserName = "Pengguna"
                    setGreetingMessage()
                }
            })
    }

    private fun setGreetingMessage() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 4..10 -> "Selamat Pagi,"
            in 11..14 -> "Selamat Siang,"
            in 15..17 -> "Selamat Sore,"
            else -> "Selamat Malam,"
        }
        val userName = currentUserName ?: firebaseAuth.currentUser?.displayName ?: "Pengguna"

        tvGreeting.text = "$greeting $userName"
    }


    override fun onResume() {
        super.onResume()
        if (currentUserId != null) {
            loadUserNameFromDB(currentUserId!!)
            loadMedicines(currentUserId!!)
        }
    }

    private fun filterMedicines(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())
        filteredList.clear()

        if (query.isEmpty()) {
            filteredList.addAll(medicineList)
        } else {
            for (medicine in medicineList) {
                if (medicine.name?.lowercase(Locale.getDefault())?.contains(lowerCaseQuery) == true) {
                    filteredList.add(medicine)
                }
            }
        }

        medicineAdapter.submitList(filteredList.toList())
        updateVisibility(filteredList.toList())
    }

    // FUNGSI Mendapatkan waktu dosis terdekat (dalam milidetik)
    private fun getNextDoseTimeMillis(medicine: Medicine): Long {
        if (medicine.reminders.isNullOrEmpty()) return Long.MAX_VALUE

        val now = Calendar.getInstance()
        var nextDoseTimeMillis = Long.MAX_VALUE

        val timeFormatter = SimpleDateFormat("HH:mm", Locale.ROOT)

        for (reminder in medicine.reminders) {
            val reminderTimeStr = reminder.time ?: continue

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

            if (doseCal.timeInMillis < nextDoseTimeMillis) {
                nextDoseTimeMillis = doseCal.timeInMillis
            }
        }
        return nextDoseTimeMillis
    }

    private fun loadMedicines(userId: String) {
        val medicinesRef = database.getReference("medicines").child(userId)

        tvEmptyStatus.text = "Memuat data..."
        tvEmptyStatus.visibility = View.VISIBLE
        tvTitleActiveObat.visibility = View.GONE
        recyclerViewObat.visibility = View.GONE

        // Memaksa sinkronisasi dengan keepSynced(true)
        medicinesRef.keepSynced(true)

        medicinesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                medicineList.clear()

                if (snapshot.exists()) {
                    for (medicineSnapshot in snapshot.children) {
                        val medicine = medicineSnapshot.getValue(Medicine::class.java)

                        if (medicine != null && !medicine.reminders.isNullOrEmpty()) {
                            medicineList.add(medicine)
                        }
                    }
                }

                // FIX URUTAN: Mengubah pengurutan menjadi Dosis Terdekat
                medicineList.sortBy { getNextDoseTimeMillis(it) }

                filterMedicines(etSearch.text.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Error memuat data: " + error.message, Toast.LENGTH_LONG).show()
                tvEmptyStatus.text = "Gagal memuat data."
                tvEmptyStatus.visibility = View.VISIBLE
                tvTitleActiveObat.visibility = View.GONE
                recyclerViewObat.visibility = View.GONE
            }
        })
    }

    private fun updateVisibility(list: List<Medicine>) {
        if (list.isEmpty() && etSearch.text.isEmpty()) {
            tvEmptyStatus.text = "Belum ada obat aktif yang terdaftar."
            tvEmptyStatus.visibility = View.VISIBLE
            tvTitleActiveObat.visibility = View.GONE
            recyclerViewObat.visibility = View.GONE
        } else if (list.isEmpty() && etSearch.text.isNotEmpty()) {
            tvEmptyStatus.text = "Tidak ditemukan obat dengan nama '${etSearch.text}'"
            tvEmptyStatus.visibility = View.VISIBLE
            tvTitleActiveObat.visibility = View.GONE
            recyclerViewObat.visibility = View.GONE
        } else {
            tvEmptyStatus.visibility = View.GONE
            tvTitleActiveObat.visibility = View.VISIBLE
            recyclerViewObat.visibility = View.VISIBLE
        }
    }
}