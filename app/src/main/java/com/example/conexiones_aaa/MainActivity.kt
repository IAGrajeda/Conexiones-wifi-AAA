package com.example.conexiones_aaa

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val permissionsRequestCode = 1
    private lateinit var wifiManager: WifiManager

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionsRequestCode)
        } else {
            startScan()
        }

        val scanButton = findViewById<Button>(R.id.button)
        scanButton.setOnClickListener { startScan() }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionsRequestCode && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScan()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
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

                    val specifier = WifiNetworkSpecifier.Builder()
                        .setSsid(wifiScanResult.SSID)
                        .setWpa2Passphrase(password)
                        .build()

                    val request = NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .setNetworkSpecifier(specifier)
                        .build()

                    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val networkCallback = object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            // Conexión disponible, puedes realizar operaciones en la red
                        }
                    }
                    val activeNetworkInfo = connectivityManager.activeNetworkInfo
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                        Toast.makeText(this, "Estas conectado a internet", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "No estas conectado", Toast.LENGTH_SHORT).show()
                    }
                    connectivityManager.requestNetwork(request, networkCallback)
                }
                dialog.show()
            }
        }
        scrollView.removeAllViews() // Elimina cualquier vista anterior
        scrollView.addView(linearLayout)
    }
}