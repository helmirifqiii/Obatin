package com.example.obatin

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

// ⭐️ FIX KRITIS: Import konstanta top-level langsung dari package file AppConstants.kt ⭐️
import com.example.obatin.USER_ID_KEY // Cukup import nama variabel
import com.example.obatin.USER_NAME_KEY // Cukup import nama variabel

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var imageViewProfile: CircleImageView
    private lateinit var buttonChangePhoto: Button
    private lateinit var buttonLogout: Button
    private lateinit var buttonSaveProfile: Button
    private lateinit var editTextUserName: EditText
    private lateinit var recyclerViewRiwayatObat: RecyclerView

    private lateinit var riwayatObatAdapter: HistoryAdapter
    private var currentUserId: String? = null
    private var currentUserName: String? = null
    private var selectedAvatarResId: Int? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val imageUri: Uri? = result.data?.data
            imageViewProfile.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Mendapatkan ID dan Nama dari Intent
        currentUserId = intent.getStringExtra(USER_ID_KEY)
        currentUserName = intent.getStringExtra(USER_NAME_KEY)

        // Inisialisasi Views
        imageViewProfile = findViewById(R.id.imageViewProfile)
        buttonChangePhoto = findViewById(R.id.btnGantiFoto)
        buttonLogout = findViewById(R.id.buttonLogout)
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile)
        editTextUserName = findViewById(R.id.editTextUserName)
        recyclerViewRiwayatObat = findViewById(R.id.recyclerViewRiwayatObat)

        editTextUserName.setText(currentUserName)

        riwayatObatAdapter = HistoryAdapter()

        recyclerViewRiwayatObat.layoutManager = LinearLayoutManager(this)
        recyclerViewRiwayatObat.adapter = riwayatObatAdapter

        // Listener untuk showChangePhotoDialog
        buttonChangePhoto.setOnClickListener { showChangePhotoDialog() }
        imageViewProfile.setOnClickListener { showChangePhotoDialog() }

        buttonSaveProfile.setOnClickListener { updateUserName() }
        buttonLogout.setOnClickListener { performLogout() }

        currentUserId?.let { loadRiwayatObat(it) }
            ?: Toast.makeText(this, "ID Pengguna tidak ditemukan.", Toast.LENGTH_SHORT).show()

        loadUserProfile()
    }

    // 🔹 Dialog pilih avatar (Diasumsikan R.layout.dialog_avatar_options, R.drawable.profilX ada)
    private fun showChangePhotoDialog() {
        // ⭐️ Asumsi R.layout.dialog_avatar_options ada ⭐️
        val dialogView = layoutInflater.inflate(R.layout.dialog_avatar_options, null)

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Asumsi ID Views di dialog_avatar_options ada (avatar1, avatar2, dst.)
        val avatars = listOf(
            dialogView.findViewById<android.widget.ImageView>(R.id.avatar1),
            dialogView.findViewById<android.widget.ImageView>(R.id.avatar2),
            dialogView.findViewById<android.widget.ImageView>(R.id.avatar3),
            dialogView.findViewById<android.widget.ImageView>(R.id.avatar4),
            dialogView.findViewById<android.widget.ImageView>(R.id.avatar5),
            dialogView.findViewById<android.widget.ImageView>(R.id.avatar6)
        )

        // Asumsi R.drawable.profilX ada
        val avatarDrawables = listOf(
            R.drawable.profil1,
            R.drawable.profil2,
            R.drawable.profil3,
            R.drawable.profil4,
            R.drawable.profil5,
            R.drawable.profil6
        )

        avatars.forEachIndexed { index, imageView ->
            imageView?.setOnClickListener {
                val selectedRes = avatarDrawables[index]
                imageViewProfile.setImageResource(selectedRes)
                selectedAvatarResId = selectedRes
                saveUserProfile(editTextUserName.text.toString(), selectedRes)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    // 🔹 Update nama pengguna dan simpan ke Firebase + lokal
    private fun updateUserName() {
        val newName = editTextUserName.text.toString().trim()
        if (newName.isEmpty()) {
            Toast.makeText(this, "Nama tidak boleh kosong.", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUserId == null) {
            Toast.makeText(this, "ID Pengguna tidak valid.", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = database.getReference("users").child(currentUserId!!)
        userRef.child("name").setValue(newName)
            .addOnSuccessListener {
                currentUserName = newName
                saveUserProfile(newName, selectedAvatarResId)
                Toast.makeText(this, "Nama berhasil diperbarui!", Toast.LENGTH_SHORT).show()

                val resultIntent = Intent().apply {
                    putExtra(USER_NAME_KEY, newName)
                }
                setResult(Activity.RESULT_OK, resultIntent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memperbarui nama: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    // 🔹 Memuat riwayat obat dari Firebase
    private fun loadRiwayatObat(userId: String) {
        val medicinesRef = database.getReference("medicines").child(userId)
        medicinesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val riwayatList = mutableListOf<Medicine>()
                if (snapshot.exists()) {
                    for (medicineSnapshot in snapshot.children) {
                        val medicine = medicineSnapshot.getValue(Medicine::class.java)
                        medicine?.let { riwayatList.add(it) }
                    }

                    riwayatList.sortByDescending { it.creationTimestamp }

                    riwayatObatAdapter.submitList(riwayatList)
                } else {
                    Toast.makeText(
                        this@AccountSettingsActivity,
                        "Tidak ada riwayat obat.",
                        Toast.LENGTH_LONG
                    ).show()
                    riwayatObatAdapter.submitList(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@AccountSettingsActivity,
                    "Error memuat: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    // 🔹 Logout
    private fun performLogout() {
        auth.signOut()
        Toast.makeText(this, "Berhasil Logout.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    // 🔹 Simpan profil ke SharedPreferences
    private fun saveUserProfile(name: String, avatarResId: Int?) {
        val prefs = getSharedPreferences("UserProfile", MODE_PRIVATE)
        with(prefs.edit()) {
            putString("user_name", name)
            if (avatarResId != null) {
                putInt("user_avatar", avatarResId)
            }
            apply()
        }
    }

    // 🔹 Muat profil dari SharedPreferences
    private fun loadUserProfile() {
        val prefs = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val savedName = prefs.getString("user_name", null)
        val savedAvatar = prefs.getInt("user_avatar", -1)

        if (!savedName.isNullOrEmpty()) {
            editTextUserName.setText(savedName)
        }

        if (savedAvatar != -1) {
            imageViewProfile.setImageResource(savedAvatar)
            selectedAvatarResId = savedAvatar
        }
    }
}