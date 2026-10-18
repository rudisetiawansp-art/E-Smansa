package id.rudissp.esmansa.jurnal

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityTambahJurnalBinding
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.content.edit

class TambahJurnal : AppCompatActivity() {

    lateinit var binding: ActivityTambahJurnalBinding
    lateinit var prefs: SharedPreferences
    lateinit var id: String
    lateinit var username: String
    lateinit var kelasa: String


    private val queue by lazy { Volley.newRequestQueue(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        binding = ActivityTambahJurnalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarnya.setNavigationOnClickListener {
            prefs.edit { putString("mode", "") }
            finish()
        }

        binding.pilihtanggal.setOnClickListener {
            showDatePicker()
        }
        val namauser = prefs.getString("namalengkap", "")
        val sssss = intent.getStringExtra("tugaskah")?: ""
        val mode = intent.getStringExtra("mode")?: ""
        kelasa = intent.getStringExtra("kelasnya")?: ""

        if (sssss == "TUGAS") {
            binding.etGurunya.isEnabled = false
            binding.gurunya.setText(namauser)
            binding.etKelasnya.isEnabled = true
            binding.etMateri.isEnabled = false
            binding.inputMateri.setText("TUGAS")
            binding.etKehadiran.visibility = View.GONE
            binding.etKet.visibility = View.VISIBLE
        } else {
            binding.gurunya.setText(namauser)
            binding.etGurunya.isEnabled = false
            binding.etKelasnya.isEnabled = false
            binding.etMateri.isEnabled = true
            binding.etKet.visibility = View.GONE
            binding.etKehadiran.visibility = View.VISIBLE
        }

        if (mode == "editmode") {
            id = intent.getStringExtra("id")!!
            username = intent.getStringExtra("username")!!
            val nama = intent.getStringExtra("nama")!!
            val mapel = intent.getStringExtra("mapel")!!
            val tanggal = intent.getStringExtra("tanggal")!!
            val kelas = intent.getStringExtra("kelas")!!
            val jamke1 = intent.getStringExtra("jamke1")!!
            val jamke2 = intent.getStringExtra("jamke2")!!
            val materi = intent.getStringExtra("materi")!!
            val kehadiran = intent.getStringExtra("kehadiran")!!
            val keterangan = intent.getStringExtra("keterangan")!!

            binding.gurunya.setText(nama)
            binding.mapelnya.setText(mapel)
            binding.pilihtanggal.setText(tanggal)
            binding.kelasnya.setText(kelas)
            binding.inputJamke1.setText(jamke1)
            binding.inputJamke2.setText(jamke2)
            binding.inputMateri.setText(materi)
            binding.inputKehadiran.setText(kehadiran)
            binding.inputKet.setText(keterangan)
            if (materi == "TUGAS") {
                binding.etMateri.isEnabled = false
                binding.etKet.visibility = View.VISIBLE
                binding.etKehadiran.visibility = View.GONE
            } else {
                binding.etMateri.isEnabled = true
                binding.etKet.visibility = View.GONE
                binding.etKehadiran.visibility = View.VISIBLE
            }

            binding.toolbarnya.title = "Edit Jurnal"
            binding.toolbarnya.menu.findItem(R.id.menunya_update).isVisible = true
            binding.toolbarnya.menu.findItem(R.id.menunya_simpan).isVisible = false
        } else {
            binding.toolbarnya.title = "Buat Jurnal Baru"
            binding.toolbarnya.menu.findItem(R.id.menunya_update).isVisible = false
            binding.toolbarnya.menu.findItem(R.id.menunya_simpan).isVisible = true

            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            binding.pilihtanggal.setText(currentDate)
            binding.kelasnya.setText(kelasa)
        }

        binding.toolbarnya.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menunya_simpan -> {
                    val username = prefs.getString("username", "") ?: ""
                    val nama = binding.gurunya.text.toString() // Nama guru
                    val mapel = binding.mapelnya.text.toString()
                    val tanggal = binding.pilihtanggal.text.toString()
                    val kelas = binding.kelasnya.text.toString()
                    val jamke1 = binding.inputJamke1.text.toString()
                    val jamke2 = binding.inputJamke2.text.toString()
                    val materi = binding.inputMateri.text.toString()
                    val kehadiran = binding.inputKehadiran.text.toString()
                    val keterangan = binding.inputKet.text.toString()

                    // Validasi input
                    if (username.isNotEmpty() && mapel.isNotEmpty() && tanggal.isNotEmpty()) {
                        // Kirim data ke server PHP untuk disimpan
                        simpanJurnal(
                            username, nama, mapel, tanggal, kelas, jamke1,
                            jamke2, materi, kehadiran, keterangan
                        )
                    } else {
                        Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
                    }
                    true
                }

                R.id.menunya_update -> {
                    editjurnal()
                    true
                }


                else -> false
            }
        }

    }

    private fun simpanJurnal(
        username: String, nama: String, mapel: String, tanggal: String,
        kelas: String, jamke1: String, jamke2: String, materi: String,
        kehadiran: String, keterangan: String
    ) {
        val stringRequest = object : StringRequest(
            Method.POST, AppConstants.SIMPAN_JURNAL,
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.getString("status")
                    if (status == "success") {
                        Toast.makeText(this, "Jurnal berhasil disimpan, swipe kebawah untuk refresh data", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, JurnalActivity::class.java)
                        intent.putExtra("mode", "individu")
                        startActivity(intent)
                        finish() // Kembali ke activity sebelumnya
                    } else {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_LONG)
                            .show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(
                    this,
                    "Gagal menghubungi server: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "username" to username,
                    "nama" to nama,
                    "mapel" to mapel,
                    "tanggal" to tanggal,
                    "kelas" to kelas,
                    "jamke1" to jamke1,
                    "jamke2" to jamke2,
                    "materi" to materi,
                    "kehadiran" to kehadiran,
                    "keterangan" to keterangan
                )
            }
        }
        queue.add(stringRequest)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.pilihtanggal.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun editjurnal() {
        val url = AppConstants.EDIT_JURNAL
        val params = HashMap<String, String>()
        params["id"] = id.toString()
        params["username"] = username
        params["nama"] = binding.gurunya.text.toString()
        params["mapel"] = binding.mapelnya.text.toString()
        params["tanggal"] = binding.pilihtanggal.text.toString()
        params["kelas"] = binding.kelasnya.text.toString()
        params["jamke1"] = binding.inputJamke1.text.toString()
        params["jamke2"] = binding.inputJamke2.text.toString()
        params["materi"] = binding.inputMateri.text.toString()
        params["kehadiran"] = binding.inputKehadiran.text.toString()
        params["keterangan"] = binding.inputKet.text.toString()

        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                // Update data di RecyclerView setelah berhasil mengedit
                Toast.makeText(this, "Jurnal berhasil diperbarui, swipe kebawah untuk refresh data", Toast.LENGTH_SHORT).show()
                prefs.edit { putString("mode", "") }
                finish() // Kembali ke activity sebelumnya
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Gagal mengedit jurnal", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

}
