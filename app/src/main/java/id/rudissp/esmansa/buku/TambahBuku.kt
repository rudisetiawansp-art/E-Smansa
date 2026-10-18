package id.rudissp.esmansa.buku

import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityTambahBukuBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TambahBuku : AppCompatActivity() {

    lateinit var binding: ActivityTambahBukuBinding
    lateinit var prefs: SharedPreferences


    private val urlApi = AppConstants.TAMBAH_BUKU

    // Variabel untuk menyimpan string base64
    private var base64Depan: String = ""
    private var base64Samping: String = ""

    private var progressAnimator: ValueAnimator? = null

    // Launcher untuk Foto Depan
    private val takePictureDepan = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            binding.llDepan.visibility = View.VISIBLE
            binding.ivPreviewDepan.setImageBitmap(bitmap)
            base64Depan = encodeImageToBase64(bitmap)
        }
    }

    // Launcher untuk Foto Samping
    private val takePictureSamping =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap != null) {
                binding.llSamping.visibility = View.VISIBLE
                binding.ivPreviewSamping.setImageBitmap(bitmap)
                base64Samping = encodeImageToBase64(bitmap)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTambahBukuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarnyaTbuku.setNavigationOnClickListener {
            finish()
        }

        setupTanggalPicker()

        val mode = intent.getStringExtra("mode") ?: ""
        if (mode == "editmode") {
            binding.toolbarnyaTbuku.title = "Edit Buku"
            binding.toolbarnyaTbuku.menu.findItem(R.id.menunya_update).isVisible = true
            binding.toolbarnyaTbuku.menu.findItem(R.id.menunya_simpan).isVisible = false

            val judul = intent.getStringExtra("judul") ?: ""
            val penulis = intent.getStringExtra("penulis") ?: ""
            val penerbit = intent.getStringExtra("penerbit") ?: ""
            val harga = intent.getStringExtra("harga") ?: ""
            val sumberDana = intent.getStringExtra("sumberdana") ?: ""
            val jumlah = intent.getStringExtra("jumlah") ?: ""
            val jenis = intent.getStringExtra("jenis") ?: ""
            val fotoDepan = intent.getStringExtra("foto_depan") ?: ""
            val fotoSamping = intent.getStringExtra("foto_samping") ?: ""
            val tanggalbeli = intent.getStringExtra("tanggalbeli") ?: ""
            val noBuku = intent.getStringExtra("nobuku") ?: ""

            binding.tbukuNomor.setText(noBuku)
            binding.tbukuJudul.setText(judul)
            binding.tbukuPenulis.setText(penulis)
            binding.tbukuPenerbit.setText(penerbit)
            binding.tbukuHarga.setText(harga)
            binding.tbukuJumlah.setText(jumlah)
            binding.actvDana.setText(sumberDana)
            binding.actvJenisbuku.setText(jenis)
            binding.pilihtanggalTbuku.setText(tanggalbeli)

            binding.ivPreviewDepan.visibility = View.VISIBLE
            binding.ivPreviewSamping.visibility = View.VISIBLE

            binding.tilNomor.isEnabled = false

            Glide.with(this)
                .load("https://sman1sukorejo.sch.id/elgrassio/uploads/$fotoDepan")
                .into(binding.ivPreviewDepan)

            Glide.with(this)
                .load("https://sman1sukorejo.sch.id/elgrassio/uploads/$fotoSamping")
                .into(binding.ivPreviewSamping)

        } else {
            binding.toolbarnyaTbuku.title = "Tambah Buku"
            binding.toolbarnyaTbuku.menu.findItem(R.id.menunya_update).isVisible = false
            binding.toolbarnyaTbuku.menu.findItem(R.id.menunya_simpan).isVisible = true
        }

        binding.kameraDepan.setOnClickListener {
            takePictureDepan.launch(null)
        }

        binding.kameraSamping.setOnClickListener {
            takePictureSamping.launch(null)
        }

        binding.toolbarnyaTbuku.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menunya_simpan -> {

                    simpanDataBuku()
                    true
                }

                R.id.menunya_update -> {
                    updateDataBuku()
                    true
                }

                else -> false
            }
        }


    }

    private fun encodeImageToBase64(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }


    private fun setupTanggalPicker() {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.pilihtanggalTbuku.setText(format.format(calendar.time))
        }

        binding.pilihtanggalTbuku.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun mulaiLoading() {
        binding.layoutProgress.visibility = View.VISIBLE
        binding.progressBarHorizontal.progress = 0
        binding.tvProgress.text = "Mengunggah... 0%"

        progressAnimator = ValueAnimator.ofInt(0, 90)
        progressAnimator?.duration = 2000
        progressAnimator?.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            binding.progressBarHorizontal.progress = progress
            binding.tvProgress.text = "Mengunggah... $progress%"
        }
        progressAnimator?.start()
    }

    private fun selesaiLoading(isSuccess: Boolean) {
        progressAnimator?.cancel()

        if (isSuccess) {
            binding.progressBarHorizontal.progress = 100
            binding.tvProgress.text = "Selesai! 100%"

            Handler(Looper.getMainLooper()).postDelayed({
                binding.layoutProgress.visibility = View.GONE
                val intentq = Intent(this, BukuActivity::class.java)
                startActivity(intentq)
                finish()
            }, 500)
        } else {
            binding.layoutProgress.visibility = View.GONE
        }
    }

    private fun simpanDataBuku() {
        val tanggal = binding.pilihtanggalTbuku.text.toString()
        val noBuku = binding.tbukuNomor.text.toString()
        val judul = binding.tbukuJudul.text.toString()
        val penulis = binding.tbukuPenulis.text.toString()
        val penerbit = binding.tbukuPenerbit.text.toString()
        val harga = binding.tbukuHarga.text.toString()
        val sumberDana = binding.actvDana.text.toString()
        val jumlah = binding.tbukuJumlah.text.toString()
        val jenis = binding.actvJenisbuku.text.toString()

        if (noBuku.isEmpty() || judul.isEmpty() || penulis.isEmpty() || penerbit.isEmpty()
            || harga.isEmpty() || sumberDana.isEmpty() || jumlah.isEmpty() || jenis.isEmpty()
            || base64Depan.isEmpty() || base64Samping.isEmpty()
        ) {
            Toast.makeText(this, "Data belum lengkap!!", Toast.LENGTH_SHORT).show()
            return
        }
        mulaiLoading()

        val stringRequest = object : StringRequest(
            Method.POST, urlApi,
            { response ->
                try {
                    val jsonObject = JSONObject(response)
                    val status = jsonObject.getBoolean("status")
                    val pesan = jsonObject.getString("pesan")

                    if (status) {
                        Toast.makeText(this@TambahBuku, "Berhasil: $pesan", Toast.LENGTH_SHORT)
                            .show()
                        selesaiLoading(true)
                    } else {
                        Toast.makeText(this@TambahBuku, "Gagal: $pesan", Toast.LENGTH_LONG).show()
                        selesaiLoading(false)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@TambahBuku,
                        "Error parsing data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    selesaiLoading(false)
                }
            },
            { error ->
                Toast.makeText(
                    this@TambahBuku,
                    "Error koneksi: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                selesaiLoading(false)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["tanggalbeli"] = tanggal
                params["nobuku"] = noBuku
                params["judul"] = judul
                params["penulis"] = penulis
                params["penerbit"] = penerbit
                params["harga"] = harga
                params["sumberdana"] = sumberDana
                params["jumlah"] = jumlah
                params["jenis"] = jenis
                params["foto_depan"] = base64Depan
                params["foto_samping"] = base64Samping

                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun updateDataBuku() {
        val tanggal = binding.pilihtanggalTbuku.text.toString()
        val noBuku = binding.tbukuNomor.text.toString()
        val judul = binding.tbukuJudul.text.toString()
        val penulis = binding.tbukuPenulis.text.toString()
        val penerbit = binding.tbukuPenerbit.text.toString()
        val harga = binding.tbukuHarga.text.toString()
        val sumberDana = binding.actvDana.text.toString()
        val jumlah = binding.tbukuJumlah.text.toString()
        val jenis = binding.actvJenisbuku.text.toString()

        mulaiLoading()

        val stringRequest = object : StringRequest(
            Method.POST, AppConstants.EDIT_BUKU,
            { response ->
                selesaiLoading(true)
                Toast.makeText(this, "Berhasil mengedit data Buku", Toast.LENGTH_SHORT).show()
            },
            { error ->
                selesaiLoading(false)
                Toast.makeText(this, "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }

        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["tanggalbeli"] = tanggal
                params["nobuku"] = noBuku
                params["judul"] = judul
                params["penulis"] = penulis
                params["penerbit"] = penerbit
                params["harga"] = harga
                params["sumberdana"] = sumberDana
                params["jumlah"] = jumlah
                params["jenis"] = jenis

                if (base64Depan.isNotEmpty()) {
                    params["foto_depan"] = base64Depan
                }
                if (base64Samping.isNotEmpty()) {
                    params["foto_samping"] = base64Samping
                }
                return params
            }
        }

        val queue = Volley.newRequestQueue(this)
        queue.add(stringRequest)
    }

}