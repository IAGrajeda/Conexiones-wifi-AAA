package com.example.conexiones_aaa

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val permissionsRequestCode = 1
    private lateinit var wifiManager: WifiManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiManager = getSystemService(WIFI_SERVICE) as WifiManager

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionsRequestCode)
        } else {
            startScan()
        }

        val scanButton = findViewById<Button>(R.id.button)
        scanButton.setOnClickListener { startScan() }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionsRequestCode && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScan()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {
        Toast.makeText(this, "Escaneando reddes", Toast.LENGTH_SHORT).show()
        val wifiScanResults = wifiManager.scanResults

        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL

        wifiScanResults.forEach { wifiScanResult ->
            val button = Button(this)
            button.text = wifiScanResult.SSID
            linearLayout.addView(button)

            button.setOnClickListener {
                val dialog = AlertDialog.Builder(this)
                val passwordInput = EditText(this)
                passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                dialog.setView(passwordInput)
                dialog.setTitle("Ingresa contrasena de ${wifiScanResult.SSID}")
                dialog.setPositiveButton("Conectar") { _, _ ->
                    // Obtener la contraseña ingresada
                    val password = passwordInput.text.toString()
                    Toast.makeText(this, "contraseña ingresada ${password}", Toast.LENGTH_SHORT).show()

                    val wifiConfiguration = WifiConfiguration()
                    wifiConfiguration.SSID = "\"${wifiScanResult.SSID}\""
                    wifiConfiguration.preSharedKey =  password

                    
                    if (password == wifiConfiguration.preSharedKey){
                        Toast.makeText(this, "conectando", Toast.LENGTH_SHORT).show()
                        val networkId = wifiManager.addNetwork(wifiConfiguration)
                        wifiManager.disconnect()
                        wifiManager.enableNetwork(networkId, true)
                        wifiManager.reconnect()
                    }else{
                        Toast.makeText(this, "contraseña incorrecta", Toast.LENGTH_SHORT).show()
                    }
                    
                }
                dialog.show()
            }
        }
        scrollView.removeAllViews() // Elimina cualquier vista anterior
        scrollView.addView(linearLayout)
        }
    }
