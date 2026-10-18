package id.rudissp.esmansa.gw

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Request.Method
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityGwBinding
import id.rudissp.esmansa.databinding.ActivityTambahJurnalGwBinding
import id.rudissp.esmansa.jurnal.JurnalActivity
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TambahJurnalGW : AppCompatActivity() {

    lateinit var binding: ActivityTambahJurnalGwBinding
    lateinit var prefs: SharedPreferences
    lateinit var id: String
    private val pickImageLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
            binding.gwjFoto.setImageURI(uri)
            binding.gwjFoto.visibility = android.view.View.VISIBLE
        }
    }
    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahJurnalGwBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)

        binding.gwjPilihFoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }



        binding.gwjPilihtanggal.setOnClickListener {
            showDatePicker()
        }

        binding.gwToolbarnya.setNavigationOnClickListener {
            finish()
        }

        binding.gwToolbarnya.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menunya_simpan -> {

                    uploadData()
                    true
                }

                R.id.menunya_update -> {
                    updateJurnalgw()
                    true
                }

                else -> false
            }
        }
        val mode = intent.getStringExtra("mode") ?: ""
        if (mode == "editmode") {
            //isi
            binding.gwToolbarnya.title = "Edit Jurnal"
            binding.gwToolbarnya.menu.findItem(R.id.menunya_update).isVisible = true
            binding.gwToolbarnya.menu.findItem(R.id.menunya_simpan).isVisible = false

            id = intent.getStringExtra("id") ?: ""
            val tanggal = intent.getStringExtra("tanggal") ?: ""
            val nama = intent.getStringExtra("nama") ?: ""
            val topik = intent.getStringExtra("topik") ?: ""
            val bentuk = intent.getStringExtra("bentuk") ?: ""
            val tinjut = intent.getStringExtra("tinjut") ?: ""
            val catatan = intent.getStringExtra("catatan") ?: ""
            val foto = intent.getStringExtra("foto") ?: ""
            val wali = intent.getStringExtra("wali") ?: ""
            binding.gwjFoto.visibility = android.view.View.VISIBLE

            binding.gwjPilihtanggal.setText(tanggal)
            binding.gwjSiswa.setText(nama)
            binding.gwjTopik.setText(topik)
            binding.gwjBentukj.setText(bentuk)
            binding.gwjTinjut.setText(tinjut)
            binding.gwjCatatan.setText(catatan)
            Glide.with(this)
                .load(foto)
                .into(binding.gwjFoto)

        } else {
            binding.gwToolbarnya.title = "Buat Jurnal"
            binding.gwToolbarnya.menu.findItem(R.id.menunya_update).isVisible = false
            binding.gwToolbarnya.menu.findItem(R.id.menunya_simpan).isVisible = true

            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            binding.gwjPilihtanggal.setText(currentDate)
        }


    }

    private fun uploadData() {
        val tanggal = binding.gwjPilihtanggal.text.toString().trim()
        val nama = binding.gwjSiswa.text.toString().trim()
        val topik = binding.gwjTopik.text.toString().trim()
        val bentuk = binding.gwjBentukj.text.toString().trim()
        val tinjut = binding.gwjTinjut.text.toString().trim()
        val catatan = binding.gwjCatatan.text.toString().trim()
        val user = prefs.getString("username", "") ?: ""

        // Validasi input wajib
        if (tanggal.isEmpty() || nama.isEmpty() || topik.isEmpty() ||
            bentuk.isEmpty() || tinjut.isEmpty() || catatan.isEmpty()
        ) {
            Toast.makeText(this, "Semua data harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi foto harus dipilih
        if (selectedPhotoUri == null) {
            Toast.makeText(this, "Pilih foto terlebih dahulu!", Toast.LENGTH_SHORT)
                .show()
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
            val byteArray = stream.toByteArray()
            fotoBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
        }

        val progressDialog = showLoadingDialog("Mengunggah data Jurnal Guru Wali...")

        val queue = Volley.newRequestQueue(this)
        val url = AppConstants.TAMBAH_JURNALGW

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Jurnal Perwalian berhasil ditambahkan, swipe kebawah untuk refresh",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(this, GWJurnal::class.java)
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
                    "tanggal" to tanggal,
                    "nama" to nama,
                    "topik" to topik,
                    "bentuk" to bentuk,
                    "tinjut" to tinjut,
                    "catatan" to catatan,
                    "foto" to fotoBase64,
                    "wali" to user
                )
            }
        }

        stringRequest.retryPolicy = DefaultRetryPolicy(
            0, // timeout 0 ms (biar cepat dianggap selesai)
            0, // tidak retry
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        queue.add(stringRequest)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.gwjPilihtanggal.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
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

    private fun updateJurnalgw() {
        val url = "https://sman1sukorejo.sch.id/esmansa/edit_jurnalgw.php"

        // Ambil nilai input
        val tanggal = binding.gwjPilihtanggal.text.toString()
        val nama = binding.gwjSiswa.text.toString()
        val topik = binding.gwjTopik.text.toString()
        val bentuk = binding.gwjBentukj.text.toString()
        val tinjut = binding.gwjTinjut.text.toString()
        val catatan = binding.gwjCatatan.text.toString()
        val wali = prefs.getString("username", "") ?: ""

        // Konversi foto ke Base64 jika ada
        var fotoBase64 = ""
        if (selectedPhotoUri != null) {
            val inputStream = contentResolver.openInputStream(selectedPhotoUri!!)
            val bytes = inputStream!!.readBytes()
            fotoBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
        }

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                try {
                    val obj = JSONObject(response)
                    if (obj.getBoolean("success")) {
                        Toast.makeText(this, "Jurnal berhasil diupdate", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = id
                params["tanggal"] = tanggal
                params["nama"] = nama
                params["topik"] = topik
                params["bentuk"] = bentuk
                params["tinjut"] = tinjut
                params["catatan"] = catatan
                params["wali"] = wali
                if (fotoBase64.isNotEmpty()) {
                    params["foto"] = fotoBase64
                }
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }


}