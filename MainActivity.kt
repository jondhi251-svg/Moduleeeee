package com.example.simstatediagnostic

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var output: TextView
    private var simulated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 28)
        }
        val title = TextView(this).apply {
            text = "SIM State Diagnostic"
            textSize = 24f
        }
        output = TextView(this).apply {
            textSize = 16f
            setPadding(0, 28, 0, 24)
        }
        val toggle = Button(this).apply {
            text = "Toggle local simulation"
            setOnClickListener {
                simulated = !simulated
                refresh()
            }
        }
        val note = TextView(this).apply {
            text = "\nSimulation changes only this screen. It does NOT change Android's SIM state, create a subscription, or affect other apps."
            textSize = 14f
        }
        root.addView(title)
        root.addView(output)
        root.addView(toggle)
        root.addView(note)
        setContentView(root)
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), 7)
        } else refresh()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refresh()
    }

    private fun refresh() {
        val realState = if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            try {
                val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
                if (Build.VERSION.SDK_INT >= 26) tm.simState else tm.simState
            } catch (e: Exception) { -1 }
        } else -2

        val realLabel = when (realState) {
            TelephonyManager.SIM_STATE_UNKNOWN -> "UNKNOWN (0)"
            TelephonyManager.SIM_STATE_ABSENT -> "ABSENT (1)"
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN_REQUIRED (2)"
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK_REQUIRED (3)"
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "NETWORK_LOCKED (4)"
            TelephonyManager.SIM_STATE_READY -> "READY (5)"
            else -> if (realState == -2) "Permission not granted" else "Unavailable ($realState)"
        }
        output.text = "Actual Android SIM state:\n$realLabel\n\nLocal display simulation:\n" +
            if (simulated) "READY (5) — display only" else "OFF"
    }
}
