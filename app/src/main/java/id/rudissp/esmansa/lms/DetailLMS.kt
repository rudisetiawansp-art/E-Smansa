package id.rudissp.esmansa.lms

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityDetailLmsBinding
import org.json.JSONException
import java.net.URLEncoder

class DetailLMS : AppCompatActivity() {

    private lateinit var binding: ActivityDetailLmsBinding
    private lateinit var prefs: SharedPreferences
    private var idTugas: String = ""
    private var isGambarTerlihat = true
    private var kelasAktif: String = ""
    private var isMateri: Boolean = false
    private var isOnline: Boolean = true
    private lateinit var adapterPengumpulan: AdapterPengumpulan

    private lateinit var adapterDiskusi: AdapterDiskusi

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLmsBinding.inflate(layoutInflater)
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        setContentView(binding.root)

        idTugas = intent.getStringExtra("ID_TUGAS") ?: ""
        val judulTugas = intent.getStringExtra("JUDUL") ?: "Detail Tugas"

        // Set judul awal di Card
        binding.tvDetailJudul.text = judulTugas


        setupToolbar()
        setupTabs()
        setupRecyclerView()
        setupListeners()
        fetchDetailTugas()
    }

    private fun setupToolbar() {
        // Tombol kembali di pojok kiri atas
        binding.toolbarDetail.setNavigationOnClickListener {
            finish()
        }

        binding.toolbarDetail.inflateMenu(R.menu.menu_detail_tugas)

        binding.toolbarDetail.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit_tugas -> {
                    // TODO: Buka Activity Edit Tugas
                    Toast.makeText(this, "Membuka halaman Edit...", Toast.LENGTH_SHORT).show()
                    // val intent = Intent(this, EditTugasActivity::class.java)
                    // intent.putExtra("ID_TUGAS", idTugas)
                    // startActivity(intent)
                    true
                }

                R.id.action_hapus_tugas -> {
                    // Tampilkan Dialog Konfirmasi sebelum menghapus
                    tampilkanDialogHapus()
                    true
                }

                else -> false
            }
        }
    }

    private fun tampilkanDialogHapus() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Tugas?")
            .setMessage("Apakah Anda yakin ingin menghapus tugas ini? Semua data pengumpulan siswa beserta file jawabannya juga akan terhapus secara permanen dari server.")
            .setPositiveButton("Hapus") { dialog, _ ->
                eksekusiHapusTugas()
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun eksekusiHapusTugas() {
        val url = AppConstants.HAPUS_TUGAS

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val jsonResponse = org.json.JSONObject(response)
                    val status = jsonResponse.getString("status")
                    val message = jsonResponse.getString("message")

                    if (status == "success") {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Gagal memproses respon server.", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Koneksi bermasalah saat menghapus tugas.", Toast.LENGTH_SHORT)
                    .show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id_tugas"] = idTugas
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun setupRecyclerView() {
        adapterPengumpulan = AdapterPengumpulan(this)
        binding.rvSiswaPengumpul.apply {
            layoutManager = LinearLayoutManager(this@DetailLMS)
            adapter = adapterPengumpulan
        }

        adapterDiskusi = AdapterDiskusi()
        binding.rvDiskusi.apply {
            layoutManager = LinearLayoutManager(this@DetailLMS)
            adapter = adapterDiskusi
        }

    }

    private fun setupTabs() {
        binding.tabLayoutLms.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Gunakan teks tab agar aman jika urutan/jumlah tab berubah (dihapus)
                val tabText = tab?.text.toString()

                // Sembunyikan semua layout terlebih dahulu
                binding.layoutDetail.visibility = View.GONE
                binding.layoutPengumpulan.visibility = View.GONE
                binding.layoutDiskusi.visibility = View.GONE
                binding.wadahpesannya.visibility = View.GONE

                // Munculkan layout yang sesuai
                when (tabText) {
                    "Detail" -> binding.layoutDetail.visibility = View.VISIBLE
                    "Pengumpulan" -> binding.layoutPengumpulan.visibility = View.VISIBLE
                    "Diskusi" -> {
                        binding.layoutDiskusi.visibility = View.VISIBLE
                        binding.wadahpesannya.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupListeners() {
        // Tombol Tampil/Sembunyi Gambar
        binding.btnToggleGambar.setOnClickListener {
            if (isGambarTerlihat) {
                binding.ivLampiran.visibility = View.GONE
                binding.btnToggleGambar.text = "Tampilkan Gambar"
                binding.btnToggleGambar.setIconResource(R.drawable.ic_visibility)
                isGambarTerlihat = false
            } else {
                binding.ivLampiran.visibility = View.VISIBLE
                binding.btnToggleGambar.text = "Sembunyikan Gambar"
                binding.btnToggleGambar.setIconResource(R.drawable.ic_visibility_off)
                isGambarTerlihat = true
            }
        }

        // Tombol Download Lampiran (PDF dll)
        binding.btnDownloadLampiran.setOnClickListener {
            Toast.makeText(this, "Mengunduh lampiran...", Toast.LENGTH_SHORT).show()
        }

        binding.btnKirimDiskusi.setOnClickListener {
            val komentar = binding.etDiskusi.text.toString().trim()
            if (komentar.isNotEmpty() && kelasAktif.isNotEmpty()) {
                // Panggil fungsi Volley untuk insert
                kirimKomentarDiskusi(komentar, kelasAktif)
            } else {
                Toast.makeText(this, "Pesan atau kelas belum dipilih.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchDetailTugas() {
        val url = AppConstants.BASE_URL + "get_lms_detail.php?id_tugas=" + idTugas
        val urlFolderUpload = AppConstants.BASE_URL + "lms/lampiran/"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val status = response.getString("status")
                    if (status == "success") {
                        val data = response.getJSONObject("data")

                        // 1. Parsing Deskripsi & Jenis
                        val deskripsiDariServer =
                            data.optString("deskripsi", "Tidak ada deskripsi.")
                        val namaFileDariServer = data.optString("file_lampiran", "")
                        val judultugas = data.optString("judul", "")

                        val jenisDariServer = data.optString("jenis", "")
                        isMateri = jenisDariServer.equals("Materi", ignoreCase = true)
                        isOnline = data.optInt("is_online", 1) == 1

                        // 2. Terapkan Teks ke UI Detail
                        binding.tvDetailDeskripsi.text = deskripsiDariServer
                        binding.toolbarDetail.title = judultugas

                        // 3. Setup Filter Kelas (Array kelas)
                        val daftarKelasArray = data.optJSONArray("daftar_kelas")
                        val listKelas = mutableListOf<String>()
                        if (daftarKelasArray != null) {
                            for (i in 0 until daftarKelasArray.length()) {
                                listKelas.add(daftarKelasArray.getString(i))
                            }
                        }
                        setupFilterKelas(listKelas)

                        // 4. Logika Penyembunyian Tab "Pengumpulan"
                        val tampilkanPengumpulan = !isMateri && isOnline
                        if (!tampilkanPengumpulan) {
                            // Hapus tab Pengumpulan (Index 1) jika tabnya masih lengkap ada 3
                            if (binding.tabLayoutLms.tabCount == 3) {
                                binding.tabLayoutLms.removeTabAt(1)
                            }
                        }

                        // Pastikan tampilan awal merender layout Detail
                        binding.layoutDetail.visibility = View.VISIBLE
                        binding.layoutPengumpulan.visibility = View.GONE
                        binding.layoutDiskusi.visibility = View.GONE

                        // 5. Logika Pengecekan Ekstensi File Lampiran
                        if (namaFileDariServer.isNotEmpty() && namaFileDariServer != "null") {
                            val fileUrl = urlFolderUpload + namaFileDariServer

                            if (namaFileDariServer.endsWith(".jpg", true) ||
                                namaFileDariServer.endsWith(".jpeg", true) ||
                                namaFileDariServer.endsWith(".png", true)
                            ) {
                                binding.layoutGambarLampiran.visibility = View.VISIBLE
                                binding.btnDownloadLampiran.visibility = View.GONE
                                Glide.with(this).load(fileUrl).into(binding.ivLampiran)
                                binding.ivLampiran.setOnClickListener {
                                    popupfull(fileUrl)
                                }
                            } else {
                                // PDF atau file lainnya
                                binding.layoutGambarLampiran.visibility = View.GONE
                                binding.btnDownloadLampiran.visibility = View.VISIBLE
                            }
                        } else {
                            binding.layoutGambarLampiran.visibility = View.GONE
                            binding.btnDownloadLampiran.visibility = View.GONE
                        }

                    } else {
                        val message =
                            response.optString("message", "Terjadi kesalahan pada server.")
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Gagal memproses data detail.", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(
                    this,
                    "Koneksi bermasalah saat mengambil detail.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }

    private fun setupFilterKelas(listKelas: List<String>) {
        binding.chipGroupKelas.removeAllViews()
        if (listKelas.isEmpty()) return

        for (kelas in listKelas) {
            val chip = Chip(this).apply {
                text = kelas
                isCheckable = true
                isClickable = true
            }
            binding.chipGroupKelas.addView(chip)
        }

        val firstChip = binding.chipGroupKelas.getChildAt(0) as Chip
        firstChip.isChecked = true
        kelasAktif = firstChip.text.toString()

        if (!isMateri && isOnline) {
            loadDataBerdasarkanKelas(kelasAktif)
        }
        loadDiskusiBerdasarkanKelas(kelasAktif)

        binding.chipGroupKelas.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChip = group.findViewById<Chip>(checkedIds[0])
                kelasAktif = selectedChip.text.toString()

                if (!isMateri && isOnline) {
                    loadDataBerdasarkanKelas(kelasAktif)
                }
                loadDiskusiBerdasarkanKelas(kelasAktif)
            }
        }


    }

    private fun loadDiskusiBerdasarkanKelas(kelas: String) {
        val kelasEncoded = URLEncoder.encode(kelas, "UTF-8")
        val url = AppConstants.BASE_URL + "get_diskusi.php?id_tugas=$idTugas&kelas=$kelasEncoded"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val status = response.getString("status")
                    if (status == "success") {
                        val jsonArray = response.getJSONArray("data")
                        val listDiskusi = mutableListOf<DiskusiLMS>()

                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            val diskusi = DiskusiLMS(
                                idDiskusi = obj.getInt("id_diskusi"),
                                pengirimTipe = obj.getString("pengirim_tipe"),
                                pengirimNama = obj.getString("pengirim_nama"),
                                pesan = obj.getString("pesan"),
                                waktu = obj.getString("created_at")
                            )
                            listDiskusi.add(diskusi)
                        }

                        // Tampilkan ke layar
                        adapterDiskusi.setData(listDiskusi)

                        // Otomatis scroll ke pesan paling bawah (pesan terbaru)
                        if (listDiskusi.isNotEmpty()) {
                            binding.rvDiskusi.scrollToPosition(listDiskusi.size - 1)
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            }
        )
        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }

    private fun kirimKomentarDiskusi(pesan: String, kelas: String) {
        val url = AppConstants.BASE_URL + "insert_diskusi.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val jsonResponse = org.json.JSONObject(response)
                    val status = jsonResponse.getString("status")

                    if (status == "success") {
                        binding.etDiskusi.text?.clear() // Kosongkan kolom input
                        loadDiskusiBerdasarkanKelas(kelas) // Refresh chat otomatis
                    } else {
                        val message = jsonResponse.getString("message")
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Gagal mengirim pesan.", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id_tugas"] = idTugas
                params["kelas"] = kelas
                params["id_guru"] = prefs.getString("username", "") as String
                params["nama_guru"] = prefs.getString("namalengkap", "") as String
                params["pesan"] = pesan
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun loadDataBerdasarkanKelas(kelas: String) {
        val kelasEncoded = URLEncoder.encode(kelas, "UTF-8")
        val url =
            AppConstants.BASE_URL + "get_pengumpulan.php?id_tugas=$idTugas&kelas=$kelasEncoded"
        Log.d("ssss", url)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val status = response.getString("status")
                    if (status == "success") {
                        val jsonArray = response.getJSONArray("data")
                        val listPengumpulan = mutableListOf<PengumpulanSiswa>()

                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            val siswa = PengumpulanSiswa(
                                idUser = obj.getInt("id_user"),
                                nis = obj.getString("nis"),
                                nama = obj.getString("nama"),
                                foto = obj.getString("foto"),
                                isMengumpulkan = obj.getBoolean("is_mengumpulkan"),
                                fileJawaban = obj.optString("file_jawaban", ""),
                                waktuKumpul = obj.optString("waktu_kumpul", ""),
                                nilai = obj.optInt("nilai", 0)
                            )
                            listPengumpulan.add(siswa)
                        }

                        // Lempar data ke Adapter RecyclerView
                        adapterPengumpulan.setData(listPengumpulan)

                    } else {
                        val message = response.optString("message", "Gagal memuat data siswa.")
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            }
        )
        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }

    private fun popupfull(fileUrl: String){
        binding.wadahFull.visibility = View.VISIBLE
        binding.lampiranFull.visibility = View.VISIBLE
        Glide.with(this).load(fileUrl).into(binding.lampiranFull)

        binding.fullKelua.setOnClickListener {
            binding.wadahFull.visibility = View.GONE
            binding.lampiranFull.visibility = View.GONE
        }
    }
}

data class PengumpulanSiswa(
    val idUser: Int,
    val nis: String,
    val nama: String,
    val foto: String,
    val isMengumpulkan: Boolean,
    val fileJawaban: String,
    val waktuKumpul: String,
    val nilai: Int
)