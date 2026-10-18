package id.rudissp.esmansa.jurnal

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.databinding.ActivityJurnalAllBinding
import org.json.JSONArray
import java.util.Calendar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class JurnalActivity : AppCompatActivity() {
    lateinit var binding: ActivityJurnalAllBinding
    lateinit var prefs: SharedPreferences
    private lateinit var adapter: JurnalAdapter
    private val jurnalList = mutableListOf<JurnalModel>()

    private lateinit var listGuru: List<String>
    private val jurnalListAll = mutableListOf<JurnalModelAll>()
    private val gukarMap = mutableMapOf<String, String>()

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider
    private var sudahScan = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        binding = ActivityJurnalAllBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        val currentuser = prefs.getString("namalengkap", "")
        val nama = intent.getStringExtra("mode")!!
        if (nama == "individu") {
            binding.toolbarnyaJurnal.title = currentuser
            binding.layoutPilih.visibility = View.GONE
            loadJurnal()
        } else{
            binding.toolbarnyaJurnal.title = "Jurnal Semua Guru"
            binding.toolbarnyaJurnal.subtitle = "SMAN 1 Sukorejo"
            binding.layoutPilih.visibility = View.VISIBLE
            loadGuruList()
        }

        binding.swipeRefreshJurnal.setOnRefreshListener {
            loadJurnal()
        }


        binding.toolbarnyaJurnal.setNavigationOnClickListener {
            finish()
        }

        binding.fabz.setOnClickListener {
            scanCodebaru()
        }


        binding.dari.setOnClickListener {
            showDatePicker()
        }

        binding.sampai.setOnClickListener {
            showDatePickersampai()
        }

        binding.cariJurnal.setOnClickListener {
            val namaGuru = binding.gurunya.text.toString()
            val username = gukarMap[namaGuru] ?: ""
            val tglAwal = binding.dari.text.toString()
            val tglAkhir = binding.sampai.text.toString()

            if (username.isNotEmpty() && tglAwal.isNotEmpty() && tglAkhir.isNotEmpty()) {
                loadJurnalAll(username, tglAwal, tglAkhir)
            } else {
                Toast.makeText(this, "Lengkapi data pencarian", Toast.LENGTH_SHORT).show() }
        }

        binding.rvJurnal.layoutManager = LinearLayoutManager(this)
        adapter = JurnalAdapter(jurnalList, this)
        binding.rvJurnal.adapter = adapter

        binding.fabzTugas.setOnClickListener {
            val intent = Intent(this, TambahJurnal::class.java)
            intent.putExtra("tugaskah", "TUGAS")
            startActivity(intent)
        }



    }

    private fun loadJurnal() {
        val username = prefs.getString("username", "")
        val url = AppConstants.GET_JURNAL_USER+"?username=$username"


        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                jurnalList.clear()
                binding.swipeRefreshJurnal.isRefreshing = false
                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    val data = JurnalModel(
                        id = obj.getString("id"),
                        username = obj.getString("username"),
                        nama = obj.getString("nama"),
                        mapel = obj.getString("mapel"),
                        tanggal = obj.getString("tanggal"),
                        kelas = obj.getString("kelas"),
                        jamke1 = obj.getString("jamke1"),
                        jamke2 = obj.getString("jamke2"),
                        materi = obj.getString("materi"),
                        kehadiran = obj.getString("kehadiran"),
                        keterangan = obj.getString("keterangan")
                    )
                    jurnalList.add(data)
                }
                adapter.notifyDataSetChanged()
                binding.toolbarnyaJurnal.subtitle = "${jurnalList.size} Jurnal"
            },
            { error ->
                binding.swipeRefreshJurnal.isRefreshing = false
                Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                error.printStackTrace()
            })

        Volley.newRequestQueue(this).add(request)
    }

    private fun loadJurnalAll(username: String, tglAwal: String, tglAkhir: String) {
        val url = AppConstants.GET_JURNAL_CETAK

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                Log.d("JURNAL_RESPONSE", response.toString()) // Tambahan log

                val jsonArray = JSONArray(response)
                jurnalListAll.clear() // pastikan ini di awal, bukan di dalam loop

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val data = JurnalModelAll(
                        nama = obj.getString("nama"),
                        tanggal = obj.getString("tanggal"),
                        kelas = obj.getString("kelas"),
                        mapel = obj.getString("mapel"),
                        jamke1 = obj.getString("jamke1"),
                        jamke2 = obj.getString("jamke2"),
                        materi = obj.getString("materi"),
                        kehadiran = obj.getString("kehadiran"),
                        keterangan = obj.getString("keterangan")
                    )
                    jurnalListAll.add(data)
                }

                // Set adapter satu kali, bukan di dalam loop
                binding.rvJurnal.layoutManager = LinearLayoutManager(this)
                binding.rvJurnal.adapter = JurnalAdapterAll(jurnalListAll)
            },
            { error ->
                Toast.makeText(this, "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("JURNAL_ERROR", "Error: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "username" to username,
                    "tgl_awal" to tglAwal,
                    "tgl_akhir" to tglAkhir
                )
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun loadGuruList() {
        val request = StringRequest(
            Request.Method.GET, AppConstants.LIST_GURU,
            { response ->
                val jsonArray = JSONArray(response)
                val namaGuruList = mutableListOf<String>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val nama = obj.getString("nama")
                    val username = obj.getString("username")


                    namaGuruList.add(nama)
                    gukarMap[nama] = username
                }
                listGuru = namaGuruList

                val adapter =
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, namaGuruList)
                binding.gurunya.setAdapter(adapter)

            },
            { error ->
                Toast.makeText(this, "Gagal load guru: ${error.message}", Toast.LENGTH_SHORT).show()
            })

        Volley.newRequestQueue(this).add(request)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.dari.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showDatePickersampai() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.sampai.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }



    private fun scanCodebaru() {
        //prefs.edit { putString("tugaskah", "bukan") }
        binding.previewView.visibility = View.VISIBLE

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = binding.previewView.surfaceProvider
                }

                val barcodeScanner = BarcodeScanning.getClient()
                val imageAnalyzer = ImageAnalysis.Builder().build().also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !sudahScan) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            barcodeScanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        val rawValue = barcode.rawValue
                                        val daftarKelas = setOf(
                                            "X 1", "X 2", "X 3", "X 4", "X 5", "X 6", "X 7", "X 8", "X 9",
                                            "XI 1", "XI 2", "XI 3", "XI 4", "XI 5", "XI 6", "XI 7", "XI 8", "XI 9",
                                            "XII 1", "XII 2", "XII 3", "XII 4", "XII 5", "XII 6", "XII 7", "XII 8", "XII 9"
                                        )

                                        if (rawValue in daftarKelas && !sudahScan) {
                                            sudahScan = true

                                            // Tutup imageProxy dulu, baru lanjut proses
                                            imageProxy.close()

                                            Handler(Looper.getMainLooper()).post {
                                                try {
                                                    cameraProvider.unbindAll()
                                                } catch (e: Exception) {
                                                    Log.e("CameraX", "unbindAll error", e)
                                                }

                                                val intent = Intent(this, TambahJurnal::class.java)
                                                intent.putExtra("kelasnya", rawValue)
                                                intent.putExtra("tugaskah", "bukan")
                                                startActivity(intent)
                                                finish()
                                            }

                                            return@addOnSuccessListener // pastikan analyzer keluar
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    Log.e("Scan", "Gagal scan", it)
                                }
                                .addOnCompleteListener {
                                    if (!sudahScan) {
                                        imageProxy.close()
                                    }
                                }
                        } else {
                            imageProxy.close()
                        }
                    }
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)

            } catch (e: Exception) {
                Log.e("CameraX", "Gagal setup kamera", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!cameraExecutor.isShutdown) {
            cameraExecutor.shutdown()
        }
    }

    override fun onResume() {
        super.onResume()
        sudahScan = false
        binding.previewView.visibility = View.GONE

    }

    override fun onPause() {
        super.onPause()
        if (::cameraProvider.isInitialized) {
            cameraProvider.unbindAll()
        }
    }

}

data class JurnalModelAll(
    val nama: String,
    val tanggal: String,
    val kelas: String,
    val mapel: String,
    val jamke1: String,
    val jamke2: String,
    val materi: String,
    val kehadiran: String,
    val keterangan: String
)







