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
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.net.Inet4Address

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
            Toast.makeText(this, "Presiona el boton para escanear redes", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "Permiso negado", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    private fun startScan() {
        Toast.makeText(this, "Escaneando redes", Toast.LENGTH_SHORT).show()
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
                dialog.setTitle("Ingresa contraseña de ${wifiScanResult.SSID}")
                dialog.setPositiveButton("Conectar") { _, _ ->
                    // Obtener la contraseña ingresada
                    val password = passwordInput.text.toString()

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
                            runOnUiThread {
                               // Toast.makeText(this@MainActivity, "Conexion exitosa", Toast.LENGTH_SHORT).show()
                                val databutton = findViewById<Button>(R.id.button2)
                                databutton.visibility = View.VISIBLE
                                val ipAddress = getIPAddress()
                                databutton.setOnClickListener {
                                    Toast.makeText(this@MainActivity, "Esta es tu ip: \n $ipAddress", Toast.LENGTH_SHORT).show()
                                }
                            }
                            if (connectivityManager.bindProcessToNetwork(network)) {
                                // Se ha vinculado el proceso a la red para recibir internet
                            } else {
                                // No se ha podido vincular el proceso a la red
                            }
                        }
                    }
                    connectivityManager.requestNetwork(request, networkCallback)
                }
                dialog.show()
            }
        }
        scrollView.removeAllViews() // Elimina cualquier vista anterior
        scrollView.addView(linearLayout)
    }

    private fun getIPAddress(): String {
        var ipAddress = ""
        try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddressInt = wifiInfo.ipAddress
            ipAddress = String.format(
                "%d.%d.%d.%d",
                ipAddressInt and 0xff,
                ipAddressInt shr 8 and 0xff,
                ipAddressInt shr 16 and 0xff,
                ipAddressInt shr 24 and 0xff
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        runOnUiThread {
            Toast.makeText(this, "$ipAddress", Toast.LENGTH_SHORT).show()
        }
        return ipAddress
    }
}