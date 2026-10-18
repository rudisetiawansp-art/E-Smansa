package id.rudissp.esmansa.metu

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityTambahMetuBinding
import java.io.ByteArrayOutputStream

class TambahMetu : AppCompatActivity() {

    private var fotoBase64: String? = null
    lateinit var binding: ActivityTambahMetuBinding
    private lateinit var prefs: SharedPreferences
    private val CAMERA_REQUEST_CODE = 100

    private var isEditMode = false
    private var izinData: Metu? = null

    private val ambilFotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                binding.fotonya.setImageBitmap(imageBitmap)
                binding.fotonya.visibility = View.VISIBLE
                fotoBase64 = bitmapToBase64(imageBitmap)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {enableEdgeToEdge()
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        super.onCreate(savedInstanceState)
        binding = ActivityTambahMetuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarnya.menu.findItem(R.id.menunya_update).isVisible = false
        // Cek mode edit
        if (intent.getStringExtra("mode") == "edit") {
            isEditMode = true
            izinData = intent.getSerializableExtra("izin") as? Metu
            izinData?.let { isiDataEdit(it) }
            binding.toolbarnya.title = "Edit Izin"

        } else {
            val gurunya = prefs.getString("namalengkap", "")
            binding.ijinGuru.setText(gurunya)
        }

        // Ambil Foto
        binding.buttonFoto.setOnClickListener {
            checkCameraPermissionAndOpenCamera()

            //val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            //ambilFotoLauncher.launch(intent)
        }

        // Simpan Data (trigger dari menu atau tombol)
        binding.toolbarnya.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.menunya_simpan) {
                if (validasiInput()) {
                    simpanData()
                }
                true
            } else false
        }

        binding.toolbarnya.setNavigationOnClickListener { finish() }

    }

    private fun isiDataEdit(data: Metu) {
        binding.ijinNama.setText(data.nama)
        binding.ijinKelas.setText(data.kelas)
        binding.ijinJamke1.setText(data.jam1)
        binding.ijinJamke2.setText(data.jam2)
        binding.ijinGuru.setText(data.guru)
        binding.ijinKeperluan.setText(data.alasan)

        // Tampilkan gambar (asumsikan URL gambar bisa diakses)
        val urlFoto = AppConstants.BASE_URL + "fotometu/" +data.foto
        Glide.with(this)
            .load(urlFoto)
            .into(binding.fotonya)

        binding.fotonya.visibility = View.VISIBLE
    }

    private fun validasiInput(): Boolean {
        val nama = binding.ijinNama.text.toString().trim()
        val kelas = binding.ijinKelas.text.toString().trim()
        val jam1 = binding.ijinJamke1.text.toString().trim()
        val jam2 = binding.ijinJamke2.text.toString().trim()
        val guru = binding.ijinGuru.text.toString().trim()
        val keperluan = binding.ijinKeperluan.text.toString().trim()

        return when {
            fotoBase64 == null -> {
                toast("Silakan ambil foto siswa terlebih dahulu")
                false
            }
            nama.isEmpty() || kelas.isEmpty() || jam1.isEmpty() || jam2.isEmpty() ||
                    guru.isEmpty() || keperluan.isEmpty() -> {
                toast("Semua kolom harus diisi")
                false
            }
            else -> true
        }
    }

    private fun simpanData() {
        val nama = binding.ijinNama.text.toString().trim()
        val kelas = binding.ijinKelas.text.toString().trim()
        val jam1 = binding.ijinJamke1.text.toString().trim()
        val jam2 = binding.ijinJamke2.text.toString().trim()
        val guru = binding.ijinGuru.text.toString().trim()
        val keperluan = binding.ijinKeperluan.text.toString().trim()
        val foto = fotoBase64 ?: ""

        val url = if (isEditMode) {
            AppConstants.EDIT_METU
        } else {
            AppConstants.SAVE_METU
        }

        val queue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                if (response.contains("sukses", ignoreCase = true)) {
                    toast("Data berhasil disimpan")
                    finish()
                } else {
                    toast("Gagal simpan: $response")
                }
            },
            { error ->
                toast("Error: ${error.message ?: "Network error"}")
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf(
                    "nama" to nama,
                    "kelas" to kelas,
                    "jamke1" to jam1,
                    "jamke2" to jam2,
                    "guru" to guru,
                    "keperluan" to keperluan,
                    "foto" to foto
                )
            }
        }

        queue.add(stringRequest)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        ambilFotoLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                toast("Izin kamera ditolak")
            }
        }
    }
}