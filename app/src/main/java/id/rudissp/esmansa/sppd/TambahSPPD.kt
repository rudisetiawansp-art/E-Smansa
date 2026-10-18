package id.rudissp.esmansa.sppd

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityTambahSppdBinding
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class User(
    val nama: String,
    val username: String,
    val nip: String
)

class TambahSppdActivity : AppCompatActivity() {

    lateinit var idz: String
    private lateinit var binding: ActivityTambahSppdBinding
    private var selectedUsername: String? = null
    private var selectedPhotoUri: Uri? = null
    private var selectedPhotoUri2: Uri? = null
    private var selectedBitmap: Bitmap? = null
    private var selectedBitmap2: Bitmap? = null
    private val calendar = Calendar.getInstance()

    private val gukarMap = mutableMapOf<String, String>() // nama -> username
    private lateinit var listGukar: List<String>

    private val jabatanList = listOf("Kepala Sekolah", "Guru", "Karyawan", "Pembina Ekstra")

    private val pickImageLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
            binding.ivPreviewFoto.setImageURI(uri)
            binding.ivPreviewFoto.visibility = android.view.View.VISIBLE
            binding.txBerangkat.visibility = android.view.View.VISIBLE
        }
    }

    private val pickImageLauncher2 = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri2 = uri
            binding.ivPreviewFotoLokasi.setImageURI(uri)
            binding.ivPreviewFotoLokasi.visibility = android.view.View.VISIBLE
            binding.txLok.visibility = android.view.View.VISIBLE
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityTambahSppdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarTambahSppd.setNavigationOnClickListener { finish() }

        loadGukarList()
        setupJabatanDropdown()
        setupTanggalPicker()

        val mode = intent.getStringExtra("mode") ?: ""
        if (mode == "editmode") {
            binding.toolbarTambahSppd.title = "Edit Sppd"
            binding.toolbarTambahSppd.menu.findItem(R.id.menunya_update).isVisible = true
            binding.toolbarTambahSppd.menu.findItem(R.id.menunya_simpan).isVisible = false

            idz = intent.getStringExtra("id") ?: ""
            val namaz = intent.getStringExtra("nama") ?: ""
            val jabatanz = intent.getStringExtra("jabatan") ?: ""
            val nipz = intent.getStringExtra("nip") ?: ""
            val tanggalz = intent.getStringExtra("tanggal") ?: ""
            val lokasiz = intent.getStringExtra("lokasi") ?: ""
            val deskripsiz = intent.getStringExtra("deskripsi") ?: ""
            val fotoz = intent.getStringExtra("foto") ?: ""
            val foto2z = intent.getStringExtra("foto_lokasi") ?: ""
            binding.ivPreviewFoto.visibility = android.view.View.VISIBLE
            binding.ivPreviewFotoLokasi.visibility = android.view.View.VISIBLE
            binding.txBerangkat.visibility = android.view.View.VISIBLE
            binding.txLok.visibility = android.view.View.VISIBLE

            binding.tietNama.setText(namaz)
            binding.actvJabatan.setText(jabatanz)
            binding.tietNip.setText(nipz)
            binding.tietTanggal.setText(tanggalz)
            binding.tietLokasi.setText(lokasiz)
            binding.tietDeskripsi.setText(deskripsiz)
            Glide.with(this)
                .load("https://sman1sukorejo.sch.id/esmansa/$fotoz")
                .into(binding.ivPreviewFoto)

            Glide.with(this)
                .load("https://sman1sukorejo.sch.id/esmansa/$foto2z")
                .into(binding.ivPreviewFotoLokasi)


        } else {
            binding.toolbarTambahSppd.title = "Buat Sppd"
            binding.toolbarTambahSppd.menu.findItem(R.id.menunya_update).isVisible = false
            binding.toolbarTambahSppd.menu.findItem(R.id.menunya_simpan).isVisible = true

            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            binding.tietTanggal.setText(currentDate)
        }

        binding.btnPilihFoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.btnPilihFotoLokasi.setOnClickListener {
            pickImageLauncher2.launch("image/*")
        }

        binding.toolbarTambahSppd.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menunya_simpan -> {

                    // Semua valid → lanjut upload
                    submitData()
                    true
                }

                R.id.menunya_update -> {
                    editSppd()
                    true
                }

                else -> false
            }
        }
    }

    private fun loadGukarList() {
        val request = StringRequest(
            Request.Method.GET, AppConstants.GET_SEMUA_USER,
            { response ->
                val jsonArray = JSONArray(response)
                val listNama = mutableListOf<String>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val nama = obj.getString("nama")
                    val username = obj.getString("username")

                    listNama.add(nama)
                    gukarMap[nama] = username
                }

                listGukar = listNama
                val adapter =
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listNama)
                binding.tietNama.setAdapter(adapter)
            },
            { error ->
                Toast.makeText(this, "Gagal memuat data Gukar", Toast.LENGTH_SHORT).show()
                Log.e("Volley", "Error: ${error.message}")
            })

        Volley.newRequestQueue(this).add(request)
    }

    private fun setupJabatanDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, jabatanList)
        binding.actvJabatan.setAdapter(adapter)
    }

    private fun setupTanggalPicker() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        binding.tietTanggal.setOnClickListener {
            val dpd = DatePickerDialog(
                this,
                { _, y, m, d ->
                    calendar.set(y, m, d)
                    binding.tietTanggal.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dpd.show()
        }
    }

    private fun submitData() {
        val nama = binding.tietNama.text.toString().trim()
        val username = gukarMap[nama] ?: ""
        val jabatan = binding.actvJabatan.text.toString().trim()
        val nip = binding.tietNip.text.toString().trim()
        val tanggal = binding.tietTanggal.text.toString().trim()
        val lokasi = binding.tietLokasi.text.toString().trim()
        val deskripsi = binding.tietDeskripsi.text.toString().trim()

        if (nama.isEmpty() || jabatan.isEmpty() || nip.isEmpty() || tanggal.isEmpty() || lokasi.isEmpty() || deskripsi.isEmpty()) {
            Toast.makeText(this, "Lengkapi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert foto ke base64
        var fotoBase64 = ""
        selectedPhotoUri?.let { uri ->
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream) // 80 biar file lebih kecil
            val byteArray = stream.toByteArray()
            fotoBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
        }

        var fotoBase642 = ""
        selectedPhotoUri2?.let { uri ->
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream) // 80 biar file lebih kecil
            val byteArray = stream.toByteArray()
            fotoBase642 = Base64.encodeToString(byteArray, Base64.DEFAULT)
        }

        val progressDialog = showLoadingDialog("Mengunggah data SPPD...")

        val queue = Volley.newRequestQueue(this)
        val url = AppConstants.TAMBH_SPPD

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Laporan Perjalanan dinas berhasil ditambahkan",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(this, SPPDActivity::class.java)
                startActivity(intent)
                finish()
            },
            { error ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf(
                    "username" to username,
                    "nama" to nama,
                    "jabatan" to jabatan,
                    "nip" to nip,
                    "tanggal" to tanggal,
                    "lokasi" to lokasi,
                    "deskripsi" to deskripsi,
                    "foto" to fotoBase64,
                    "foto_lokasi" to fotoBase642
                )
            }
        }

        stringRequest.retryPolicy = DefaultRetryPolicy(
            0,
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        queue.add(stringRequest)
    }

    private fun editSppd() {
        val nama = binding.tietNama.text.toString().trim()
        val username = gukarMap[nama] ?: ""
        val jabatan = binding.actvJabatan.text.toString().trim()
        val nip = binding.tietNip.text.toString().trim()
        val tanggal = binding.tietTanggal.text.toString().trim()
        val lokasi = binding.tietLokasi.text.toString().trim()
        val deskripsi = binding.tietDeskripsi.text.toString().trim()

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Menyimpan perubahan...")
        progressDialog.show()

        val stringRequest = object : StringRequest(
            Method.POST, AppConstants.EDIT_SPPD,
            { response ->
                progressDialog.dismiss()
                Toast.makeText(this, "Berhasil mengedit SPPD", Toast.LENGTH_SHORT).show()
                finish()
            },
            { error ->
                progressDialog.dismiss()
                Toast.makeText(this, "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = idz
                params["username"] = username
                params["nama"] = nama
                params["jabatan"] = jabatan
                params["nip"] = nip
                params["tanggal"] = tanggal
                params["lokasi"] = lokasi
                params["deskripsi"] = deskripsi

                // Jika ada foto baru dipilih
                if (selectedBitmap != null) {
                    params["foto"] = bitmapToBase64(selectedBitmap!!)
                }

                if (selectedBitmap2 != null) {
                    params["foto_lokasi"] = bitmapToBase64(selectedBitmap2!!)
                }
                return params
            }
        }

        val queue = Volley.newRequestQueue(this)
        queue.add(stringRequest)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun showLoadingDialog(message: String): AlertDialog {
        val dialogView = layoutInflater.inflate(R.layout.dialog_loading, null)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessage)
        tvMessage.text = message

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        return dialog
    }

}
