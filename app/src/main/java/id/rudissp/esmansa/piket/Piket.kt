package id.rudissp.esmansa.piket

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.databinding.ActivityPiketBinding
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Piket : AppCompatActivity() {

    private lateinit var binding: ActivityPiketBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var sudahScan = false

    override fun onCreate(savedInstanceState: Bundle?) {enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        binding = ActivityPiketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi thread untuk kamera
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.fabPiket.setOnClickListener {
            mulaiProsesScan()
        }

        loadDataPiket()
        binding.toolbarnyaPiket.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadDataPiket() {
        val namaUser = prefs.getString("namalengkap", "") ?: ""
        val url = AppConstants.GET_PIKET + "?nama=$namaUser"

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                val listPiket = mutableListOf<PiketModel>()
                val jsonArray = JSONArray(response)

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    listPiket.add(PiketModel(
                        obj.getString("id"),
                        obj.getString("nama"),
                        obj.getString("tanggal")
                    ))
                }

                // Set ke RecyclerView
                binding.rvPiket.layoutManager = LinearLayoutManager(this)
                binding.rvPiket.adapter = PiketAdapter(listPiket)
            },
            { error ->
                Toast.makeText(this, "Gagal ambil data", Toast.LENGTH_SHORT).show()
            })

        Volley.newRequestQueue(this).add(request)
    }

    private fun mulaiProsesScan() {
        sudahScan = false
        binding.previewPiket.visibility = View.VISIBLE

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = binding.previewPiket.surfaceProvider
                }

                val barcodeScanner = BarcodeScanning.getClient()

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null && !sudahScan) {
                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                barcodeScanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        for (barcode in barcodes) {
                                            val rawValue = barcode.rawValue
                                            if (rawValue != null && !sudahScan) {
                                                sudahScan = true
                                                // Jalankan di UI Thread
                                                runOnUiThread { prosesDataBarcode(rawValue) }
                                            }
                                        }
                                    }
                                    .addOnCompleteListener { imageProxy.close() }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)

            } catch (e: Exception) {
                Log.e("Piket", "Gagal inisialisasi kamera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun prosesDataBarcode(token: String) {
        try {
            val decodedBytes = Base64.decode(token, Base64.DEFAULT)
            val decodedString = String(decodedBytes).trim()

            if (decodedString.contains("SMAN1SUKOREJO", ignoreCase = true)) {
                // 1. Matikan kamera segera setelah barcode valid ditemukan
                hentikanKamera()

                // 2. Ambil nama dan kirim ke server
                val namaUser = prefs.getString("namalengkap", "") ?: ""
                if (namaUser.isNotEmpty()) {
                    uploadDataKeServer(namaUser)
                } else {
                    Toast.makeText(this, "Error: Nama tidak terdaftar di aplikasi", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Barcode tidak valid!", Toast.LENGTH_SHORT).show()
                sudahScan = false
            }
        } catch (e: Exception) {
            Log.e("Piket", "Dekode gagal: ${e.message}")
            sudahScan = false
        }
    }

    private fun hentikanKamera() {
        cameraProvider?.unbindAll()
        binding.previewPiket.visibility = View.GONE
    }

    private fun uploadDataKeServer(nama: String) {
        val request = object : StringRequest(Method.POST, AppConstants.TAMBAH_PIKET,
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.getString("status")
                    val message = jsonResponse.getString("message")

                    if (status == "success") {
                        Toast.makeText(this, "Berhasil: $nama", Toast.LENGTH_LONG).show()
                    } else {
                        // Jika status 'error' (misal: sudah absen ganda), tampilkan pesan dari PHP
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                        // Karena gagal (ganda), kita tutup kamera tapi jangan finish()
                        // agar user bisa melihat pesan errornya
                        hentikanKamera()
                        sudahScan = false // Reset agar tombol FAB bisa diklik lagi jika perlu
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Respon tidak dikenali", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "Masalah Jaringan", Toast.LENGTH_SHORT).show()
                sudahScan = false
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("nama" to nama)
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    override fun onResume() {
        super.onResume()
        sudahScan = false
        binding.previewPiket.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        hentikanKamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}