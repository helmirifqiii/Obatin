package com.example.obatin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.obatin.R
import java.util.Calendar
import android.app.AlarmManager
import java.util.Locale
import android.net.Uri
import android.media.AudioAttributes // Import untuk AudioAttributes

class ReminderBroadcastReceiver : BroadcastReceiver() {

    private val CHANNEL_ID_REMINDER = "OBAT_REMINDER_CHANNEL"

    // ❌ HAPUS deklarasi CUSTOM_SOUND_URI di sini

    override fun onReceive(context: Context, intent: Intent) {
        // ⭐️ FIX: Deklarasikan CUSTOM_SOUND_URI di sini, menggunakan Context yang sudah tersedia
        val customSoundUri: Uri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.waktunya_minum_obat)


        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Pengingat Obat"
        val reminderTime = intent.getStringExtra("REMINDER_TIME") ?: "sekarang"
        val dosageText = intent.getStringExtra("DOSAGE_TEXT") ?: "1"
        val dosageUnit = intent.getStringExtra("DOSAGE_UNIT") ?: "butir"

        val medicineId = intent.getStringExtra("MEDICINE_ID")

        showNotification(context, medicineName, dosageText, dosageUnit, customSoundUri) // Kirim URI ke showNotification

        if (medicineId != null && reminderTime != null) {
            scheduleNextDayReminder(context, medicineId, reminderTime, medicineName, dosageText, dosageUnit)
        }
    }

    // ⭐️ FIX: Tambahkan parameter customSoundUri ⭐️
    private fun showNotification(context: Context, name: String, dosage: String, unit: String, customSoundUri: Uri) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // FIX API LEVEL: Kunci NotificationChannel di API 26 (Oreo) ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID_REMINDER,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
                // Setel suara notifikasi di Channel
                setSound(customSoundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val tapIntent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            tapIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
            .setSmallIcon(R.drawable.ic_pill)
            .setContentIntent(pendingIntent)
            .setContentTitle(name)
            .setContentText("Waktunya minum obat! Dosis: $dosage $unit")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Untuk API lama (di bawah Oreo), setel suara di Builder
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setSound(customSoundUri)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun scheduleNextDayReminder(context: Context, medicineId: String, reminderTime: String, medicineName: String, dosage: String, unit: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val parts = reminderTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Jadwalkan untuk hari berikutnya
            add(Calendar.DAY_OF_YEAR, 1)
        }

        val stableReminderId = (medicineId + reminderTime).hashCode()

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("MEDICINE_NAME", medicineName)
            putExtra("REMINDER_TIME", reminderTime)
            putExtra("DOSAGE_TEXT", dosage)
            putExtra("DOSAGE_UNIT", unit)
            putExtra("MEDICINE_ID", medicineId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            stableReminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}