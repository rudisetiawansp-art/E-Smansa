package id.rudissp.esmansa.siswa

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityEditSiswaBinding
import org.json.JSONObject
import java.net.URLEncoder

class EditSiswa : AppCompatActivity() {
    lateinit var binding: ActivityEditSiswaBinding
    lateinit var prefs: SharedPreferences

    private val BASE_URL = AppConstants.BASE_URL
    private val SMANIA_URL = AppConstants.SMANIA_URL
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        binding = ActivityEditSiswaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBar.visibility = View.GONE

        binding.tolbarEditsiswa.setNavigationOnClickListener {
            finish()
        }
        binding.tolbarEditsiswa.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menunya_simpan -> {

                    saveData()
                    true
                }
                else -> false
            }
        }

        //ambildatakampus()
        ambildatajurusan()
        ambilmantab()

        // Sembunyikan menu update (tidak digunakan)
        binding.tolbarEditsiswa.menu.findItem(R.id.menunya_update).isVisible = false

        // Load profil siswa (data login)
        loadProfile()
        // Load data rencana studi berdasarkan NIS
        loadSiswaByNis(intent.getStringExtra("nis")!!)

        // Tombol simpan (floating / bottom)
        binding.btnReset.setOnClickListener {
            resetid()
        }
    }

    // ============================================
    // Fungsi utama untuk menyimpan (insert/update)
    // ============================================
    private fun saveData() {
        val siswa = collectDataFromViews()
        if (siswa.nis.isEmpty()) {
            Toast.makeText(this, "NIS tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEditMode) {
            updateSiswa(siswa)
        } else {
            insertSiswa(siswa)
        }
    }

    // ============================================
    // Load data profil siswa (dari Intent)
    // ============================================
    private fun loadProfile() {
        val id = intent.getStringExtra("id") ?: ""
        val nama = intent.getStringExtra("nama") ?: ""
        val nis = intent.getStringExtra("nis") ?: ""
        val pass = intent.getStringExtra("pass") ?: ""
        val kelas = intent.getStringExtra("kelas") ?: ""
        val guruwali = intent.getStringExtra("guruwali") ?: ""
        val nisn = intent.getStringExtra("nisn") ?: ""
        val device_id = intent.getStringExtra("device_id") ?: ""

        if (nis.isEmpty()) {
            Toast.makeText(this, "NIS tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.etDeviceId.setText(if (device_id == "null" || device_id.isEmpty()) "" else device_id)
        binding.tolbarEditsiswa.title = nama
        binding.etId.setText(id)
        binding.etNisn.setText(nisn)
        binding.etNis.setText(nis)
        binding.etNama.setText(nama)
        binding.etPass.setText(pass)
        binding.etKelas.setText(kelas)
        binding.etGuruWali.setText(guruwali)
        fetchWalikelas(kelas)

    }

    private fun fetchWalikelas(kelas: String) {
        val encodedKelas = URLEncoder.encode(kelas, "UTF-8")
        val url = "https://sman1sukorejo.sch.id/smania/android/api_wakel.php?kelas=$encodedKelas"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val status = response.getString("status")
                    if (status == "success") {
                        val data = response.getJSONObject("data")
                        val walikelas = data.getString("walikelas")
                        binding.etwalikelas.setText(walikelas)

                    } else {

                    }
                } catch (e: Exception) {
                    e.printStackTrace()

                }
            },
            { error ->
                error.printStackTrace()

            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    // ============================================
    // GET: Load data rencana studi berdasarkan NIS
    // ============================================
    private fun loadSiswaByNis(nis: String) {
        val url = SMANIA_URL + "android/api_siswa.php?nis=$nis"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val status = response.optString("status")
                if (status == "success") {
                    val dataObj = response.optJSONObject("data")
                    if (dataObj != null) {
                        val siswa = parseSiswaFromJson(dataObj)
                        fillFormWithSiswa(siswa)
                        isEditMode = true
                        // Toast tidak perlu ditampilkan setiap load
                    } else {
                        // Data tidak ditemukan -> mode tambah baru
                        isEditMode = false
                        Toast.makeText(this, "Data baru, silakan isi", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Jika status error, kemungkinan data belum ada
                    isEditMode = false
                    val msg = response.optString("message", "Data tidak ditemukan")
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                // Jika gagal koneksi, asumsikan data baru
                isEditMode = false
                Toast.makeText(this, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    // ============================================
    // Fungsi collect data dari form
    // ============================================
    private fun collectDataFromViews(): datasiswa {
        return datasiswa(
            nis = binding.etNis.text.toString().trim(),
            rencanaPascaStudi = binding.etRencana.text.toString().trim(),
            jurusanImpian1 = binding.etJurusanImpian1.text.toString().trim(),
            jurusanImpian2 = binding.etJurusanImpian2.text.toString().trim(),
            kampusImpian1 = binding.etKampusImpian1.text.toString().trim(),
            kampusImpian2 = binding.etKampusImpian2.text.toString().trim(),
            bidangKarier = binding.etBidangKarier.text.toString().trim(),
            mapelFavorit = binding.etMapelFavorit.text.toString().trim(),
            skillDimiliki = binding.etSkillMilik.text.toString().trim(),
            skillDipelajari = binding.etSkillPelajari.text.toString().trim(),
            pengalaman = binding.etPengalaman.text.toString().trim(),
            programSekolah = binding.etProgramSekolah.text.toString().trim(),
            editedBy = prefs.getString("nama", "")
        )
    }

    // ============================================
    // POST: Insert data baru
    // ============================================
    private fun insertSiswa(siswa: datasiswa) {
        binding.progressBar.visibility = View.VISIBLE
        

        val url = "$SMANIA_URL/android/api_siswa.php"
        val jsonObject = siswaToJson(siswa)

        val request = object : JsonObjectRequest(
            Method.POST, url, jsonObject,
            { response ->
                binding.progressBar.visibility = View.GONE
                val status = response.optString("status")
                if (status == "success") {
                    Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val msg = response.optString("message", "Gagal menyimpan")
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    // ============================================
    // PUT: Update data berdasarkan NIS
    // ============================================
    private fun updateSiswa(siswa: datasiswa) {
        binding.progressBar.visibility = View.VISIBLE
        

        val url = "$SMANIA_URL/android/api_siswa.php?nis=${siswa.nis}"
        val jsonObject = siswaToJson(siswa)

        val request = object : JsonObjectRequest(
            Method.PUT, url, jsonObject,
            { response ->
                binding.progressBar.visibility = View.GONE
                
                val status = response.optString("status")
                if (status == "success") {
                    Toast.makeText(this, "Data berhasil diupdate", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val msg = response.optString("message", "Gagal update")
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                binding.progressBar.visibility = View.GONE
                
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    // ============================================
    // Helper: konversi datasiswa ke JSONObject
    // ============================================
    private fun siswaToJson(siswa: datasiswa): JSONObject {
        return JSONObject().apply {
            put("nis", siswa.nis)
            siswa.rencanaPascaStudi?.let { put("rencana_pasca_studi", it) }
            siswa.jurusanImpian1?.let { put("jurusan_impian_1", it) }
            siswa.jurusanImpian2?.let { put("jurusan_impian_2", it) }
            siswa.kampusImpian1?.let { put("kampus_impian_1", it) }
            siswa.kampusImpian2?.let { put("kampus_impian_2", it) }
            siswa.bidangKarier?.let { put("bidang_karier", it) }
            siswa.mapelFavorit?.let { put("mapel_favorit", it) }
            siswa.skillDimiliki?.let { put("skill_dimiliki", it) }
            siswa.skillDipelajari?.let { put("skill_dipelajari", it) }
            siswa.pengalaman?.let { put("pengalaman", it) }
            siswa.programSekolah?.let { put("program_sekolah", it) }
            siswa.editedBy?.let { put("edited_by", prefs.getString("namalengkap", "")) }
        }
    }

    // ============================================
    // Parse JSONObject -> datasiswa
    // ============================================
    private fun parseSiswaFromJson(obj: JSONObject): datasiswa {
        return datasiswa(
            nis = obj.optString("nis", ""),
            rencanaPascaStudi = obj.optString("rencana_pasca_studi", ""),
            jurusanImpian1 = obj.optString("jurusan_impian_1", ""),
            jurusanImpian2 = obj.optString("jurusan_impian_2", ""),
            kampusImpian1 = obj.optString("kampus_impian_1", ""),
            kampusImpian2 = obj.optString("kampus_impian_2", ""),
            bidangKarier = obj.optString("bidang_karier", ""),
            mapelFavorit = obj.optString("mapel_favorit", ""),
            skillDimiliki = obj.optString("skill_dimiliki", ""),
            skillDipelajari = obj.optString("skill_dipelajari", ""),
            pengalaman = obj.optString("pengalaman", ""),
            programSekolah = obj.optString("program_sekolah", ""),
            editedBy = obj.optString("edited_by", ""),
            editTime = obj.optString("edit_time", "")
        )
    }

    // ============================================
    // Mengisi form dengan data siswa (untuk edit)
    // ============================================
    private fun fillFormWithSiswa(siswa: datasiswa) {
        binding.etNis.setText(siswa.nis)
        binding.etRencana.setText(siswa.rencanaPascaStudi)
        binding.etJurusanImpian1.setText(siswa.jurusanImpian1)
        binding.etJurusanImpian2.setText(siswa.jurusanImpian2)
        binding.etKampusImpian1.setText(siswa.kampusImpian1)
        binding.etKampusImpian2.setText(siswa.kampusImpian2)
        binding.etBidangKarier.setText(siswa.bidangKarier)
        binding.etMapelFavorit.setText(siswa.mapelFavorit)
        binding.etSkillMilik.setText(siswa.skillDimiliki)
        binding.etSkillPelajari.setText(siswa.skillDipelajari)
        binding.etPengalaman.setText(siswa.pengalaman)
        binding.etProgramSekolah.setText(siswa.programSekolah)
        binding.updatesiapa.text = "Diubah oleh: " + siswa.editedBy + " pada " + siswa.editTime
    }

    // ============================================
    // Fungsi reset device_id (jika diperlukan)
    // ============================================
    private fun resetid() {
        binding.progressBar.visibility = View.VISIBLE
        

        val id = binding.etId.text.toString().trim()
        val nis = binding.etNis.text.toString().trim()
        val nama = binding.etNama.text.toString().trim()
        val pass = binding.etPass.text.toString().trim()
        val kelas = binding.etKelas.text.toString().trim()
        val guruwali = binding.etGuruWali.text.toString().trim()

        val params = HashMap<String, String>()
        params["id"] = id
        params["nis"] = nis
        params["nama"] = nama
        params["pass"] = pass
        params["kelas"] = kelas
        params["guruwali"] = guruwali
        params["device_id"] = ""

        val url = "$BASE_URL/update_siswa.php"
        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                binding.progressBar.visibility = View.GONE
                
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        Toast.makeText(this, "Login siswa $nama berhasil direset", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    } else {
                        Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: $response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                binding.progressBar.visibility = View.GONE
                
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    // ============================================
    // Ambil data kampus (AutoComplete)
    // ============================================
    private fun ambildatakampus() {
        val urlApi = AppConstants.BASE_URL + "api_universitas.php"
        val queue = Volley.newRequestQueue(this)
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, urlApi, null,
            { response ->
                val listRencanaDariDB = ArrayList<String>()
                for (i in 0 until response.length()) {
                    listRencanaDariDB.add(response.getString(i))
                }
                val adapterRencana = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    listRencanaDariDB
                )
                //binding.etKampusImpian1.setAdapter(adapterRencana)
                //binding.etKampusImpian2.setAdapter(adapterRencana)
            },
            { error ->
                Toast.makeText(
                    this,
                    "Gagal mengambil data kampus: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        queue.add(jsonArrayRequest)
    }

    // ============================================
    // Ambil data jurusan (AutoComplete)
    // ============================================
    private fun ambildatajurusan() {
        val urlApi = AppConstants.BASE_URL + "api_jurusan.php"
        val queue = Volley.newRequestQueue(this)
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, urlApi, null,
            { response ->
                val listRencanaDariDB = ArrayList<String>()
                for (i in 0 until response.length()) {
                    listRencanaDariDB.add(response.getString(i))
                }
                val adapterRencana = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    listRencanaDariDB
                )
                binding.etJurusanImpian1.setAdapter(adapterRencana)
                binding.etJurusanImpian2.setAdapter(adapterRencana)
            },
            { error ->
                Toast.makeText(
                    this,
                    "Gagal mengambil data jurusan: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        queue.add(jsonArrayRequest)
    }

    private fun ambilmantab(){
            val urlApi = AppConstants.BASE_URL + "api_mantab.php"
            val queue = Volley.newRequestQueue(this)
            val jsonArrayRequest = JsonArrayRequest(
                Request.Method.GET, urlApi, null,
                { response ->
                    val listRencanaDariDB = ArrayList<String>()
                    for (i in 0 until response.length()) {
                        listRencanaDariDB.add(response.getString(i))
                    }
                    val adapterRencana = ArrayAdapter(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        listRencanaDariDB
                    )
                    binding.etProgramSekolah.setAdapter(adapterRencana)
                },
                { error ->
                    Toast.makeText(
                        this,
                        "Gagal mengambil data: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
            queue.add(jsonArrayRequest)
    }
}

data class datasiswa(
    var nis: String,
    var rencanaPascaStudi: String? = null,
    var jurusanImpian1: String? = null,
    var jurusanImpian2: String? = null,
    var kampusImpian1: String? = null,
    var kampusImpian2: String? = null,
    var bidangKarier: String? = null,
    var mapelFavorit: String? = null,
    var skillDimiliki: String? = null,
    var skillDipelajari: String? = null,
    var pengalaman: String? = null,
    var programSekolah: String? = null,
    var editedBy: String? = null,
    var editTime: String? = null
)