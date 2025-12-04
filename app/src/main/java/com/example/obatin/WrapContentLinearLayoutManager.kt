package com.example.obatin

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet

/**
 * Custom LinearLayoutManager untuk mengatasi IllegalStateException/IndexOutOfBoundsException
 * yang sering terjadi ketika RecyclerView diatur ke 'wrap_content' di dalam ScrollView.
 */
class WrapContentLinearLayoutManager : LinearLayoutManager {

    // 1. Constructor untuk digunakan dari Kotlin (Hanya Context)
    constructor(context: Context) : super(context)

    // 2. Constructor untuk digunakan dari XML (Context, AttributeSet, Int, Int)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    // ⭐️ FIX KRITIS: 3. Constructor yang dipanggil dari HomeActivity.kt
    constructor(context: Context, orientation: Int, reverseLayout: Boolean)
            : super(context, orientation, reverseLayout)


    // Override onLayoutChildren untuk menangkap dan mengabaikan exception
    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            // Mengabaikan IndexOutOfBoundsException
        }
    }
}