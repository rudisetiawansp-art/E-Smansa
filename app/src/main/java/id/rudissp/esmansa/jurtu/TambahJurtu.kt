package id.rudissp.esmansa.jurtu

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityTambahJurtuBinding
import id.rudissp.esmansa.sppd.SPPDActivity
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TambahJurtu : AppCompatActivity() {

    private lateinit var binding: ActivityTambahJurtuBinding
    lateinit var prefs: SharedPreferences
    lateinit var idz: String
    lateinit var nama: String

    private var selectedPhotoUri: Uri? = null
    private var selectedBitmap: Bitmap? = null
    private val calendar = Calendar.getInstance()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
            selectedBitmap = null
            binding.fotoJurtu.setImageURI(uri)
            binding.fotoJurtu.visibility = View.VISIBLE
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            selectedPhotoUri = null
            binding.fotoJurtu.setImageBitmap(bitmap)
            binding.fotoJurtu.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        binding = ActivityTambahJurtuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nama = prefs.getString("namalengkap", "").toString()


        binding.toolbarnyaJurtu.setNavigationOnClickListener { finish() }

        setupTanggalPicker()

        val mode = intent.getStringExtra("mode") ?: ""
        if (mode == "editmode") {
            binding.toolbarnyaJurtu.title = "Edit Jurnal"
            binding.toolbarnyaJurtu.menu.findItem(R.id.menunya_update).isVisible = true
            binding.toolbarnyaJurtu.menu.findItem(R.id.menunya_simpan).isVisible = false

            idz = intent.getStringExtra("id") ?: ""
            val namaz = intent.getStringExtra("nama") ?: ""
            val tanggalz = intent.getStringExtra("tanggal") ?: ""
            val jam1z = intent.getStringExtra("jam1") ?: ""
            val jam2z = intent.getStringExtra("jam2") ?: ""
            val rincianz = intent.getStringExtra("rincian") ?: ""
            val fotoz = intent.getStringExtra("foto") ?: ""

            binding.fotoJurtu.visibility = View.VISIBLE

            binding.pilihtanggalJurtu.setText(tanggalz)
            binding.jam1Jurtu.setText(jam1z)
            binding.jam2Jurtu.setText(jam2z)
            binding.rincianJurtu.setText(rincianz)
            Glide.with(this)
                .load("https://sman1sukorejo.sch.id/esmansa/$fotoz")
                .into(binding.fotoJurtu)

        } else {
            binding.toolbarnyaJurtu.title = "Buat Jurnal Baru"
            binding.toolbarnyaJurtu.menu.findItem(R.id.menunya_update).isVisible = false
            binding.toolbarnyaJurtu.menu.findItem(R.id.menunya_simpan).isVisible = true

            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            binding.pilihtanggalJurtu.setText(currentDate)

        }

        binding.pilihFotoJurtu.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.jam1Jurtu.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Pilih Waktu")
                .build()

            picker.show(supportFragmentManager, "jam1_jurtu_picker")

            picker.addOnPositiveButtonClickListener {
                // Format angka agar selalu 2 digit (contoh: 08:05)
                val formattedTime = String.format("%02d:%02d", picker.hour, picker.minute)
                binding.jam1Jurtu.setText(formattedTime)
            }
        }

        binding.jam2Jurtu.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Pilih Waktu")
                .build()

            picker.show(supportFragmentManager, "jam2_jurtu_picker")

            picker.addOnPositiveButtonClickListener {
                val formattedTime = String.format("%02d:%02d", picker.hour, picker.minute)
                binding.jam2Jurtu.setText(formattedTime)
            }
        }

        binding.kameraJurtu.setOnClickListener {
            takePictureLauncher.launch(null)
        }

        binding.toolbarnyaJurtu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menunya_simpan -> {

                    submitData()
                    true
                }

                R.id.menunya_update -> {
                    //edita()
                    true
                }

                else -> false
            }
        }


    }

    private fun setupTanggalPicker() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        binding.pilihtanggalJurtu.setOnClickListener {
            val dpd = DatePickerDialog(
                this,
                { _, y, m, d ->
                    calendar.set(y, m, d)
                    binding.pilihtanggalJurtu.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dpd.show()
        }
    }

    private fun submitData() {
        val tanggal = binding.pilihtanggalJurtu.text.toString().trim()
        val jam1 = binding.jam1Jurtu.text.toString().trim()
        val jam2 = binding.jam2Jurtu.text.toString().trim()
        val keterangan = binding.rincianJurtu.text.toString().trim()

        if (tanggal.isEmpty() || jam1.isEmpty() || jam2.isEmpty() || keterangan.isEmpty()) {
            Toast.makeText(this, "Lengkapi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        var fotoBase64 = ""

        try {
            var bitmapToUpload: Bitmap? = null

            if (selectedBitmap != null) {
                bitmapToUpload = selectedBitmap
            } else if (selectedPhotoUri != null) {
                bitmapToUpload = if (Build.VERSION.SDK_INT < 28) {
                    android.provider.MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
                } else {
                    val source = ImageDecoder.createSource(contentResolver, selectedPhotoUri!!)
                    ImageDecoder.decodeBitmap(source)
                }
            }


            bitmapToUpload?.let { bmp ->
                val stream = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream)
                val byteArray = stream.toByteArray()
                fotoBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
            return
        }
        val progressDialog = showLoadingDialog("Mengunggah data...")
        val queue = Volley.newRequestQueue(this)
        val url = AppConstants.TAMBAH_JURTU

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { _ ->
                progressDialog.dismiss()
                Toast.makeText(this, "Jurnal Kegiatan Berhasil Ditambahkan", Toast.LENGTH_LONG).show()
                val intent = Intent(this, Jurtu::class.java)
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
                    "nama" to nama,
                    "tanggal" to tanggal,
                    "jam1" to jam1,
                    "jam2" to jam2,
                    "kegiatan" to keterangan,
                    "foto" to fotoBase64
                )
            }
        }

        stringRequest.retryPolicy = DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        queue.add(stringRequest)
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