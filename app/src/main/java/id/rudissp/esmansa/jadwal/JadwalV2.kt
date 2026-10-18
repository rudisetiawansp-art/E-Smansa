package id.rudissp.esmansa.jadwal

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityJadwalV2Binding
import org.json.JSONArray
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import id.rudissp.esmansa.AppConstants
import org.json.JSONException
import org.json.JSONObject


class JadwalV2 : AppCompatActivity() {

    private lateinit var binding: ActivityJadwalV2Binding

    private val gukarMap = HashMap<String, String>()
    private var listGuru = mutableListOf<String>()
    private lateinit var listKelas: Array<String>

    // UBAH: Sekarang menggunakan BarisJamModel
    private val listDataJadwal = ArrayList<HariModel>()
    private lateinit var adapterJadwal: JadwalRowAdapter
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)

        binding = ActivityJadwalV2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val myUsername = prefs.getString("namalengkap", "")
        fetchJadwalGuru(myUsername)

        binding.jadwalv2Toolbar.title = myUsername
        binding.jadwalv2Toolbar.setNavigationOnClickListener { finish() }

        binding.jadwalv2Toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.actioncari -> {
                    if (binding.cardPencarian.isVisible) {
                        binding.cardPencarian.visibility = View.GONE
                        setMenuAktif("saya")
                    } else {
                        binding.cardPencarian.visibility = View.VISIBLE
                        setMenuAktif("pencarian")
                        binding.dropdownPencarian.requestFocus()
                    }
                    true
                }

                R.id.actionsaya -> {
                    fetchJadwalGuru(myUsername)
                    true
                }

                else -> false
            }
        }

        loadDataPencarian()
        setupListeners()
        fetchDataJadwal()


    }

    private fun setupListeners() {
        binding.btnJadwalSaya.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup, AutoTransition())
            binding.cardPencarian.visibility = View.GONE
            setMenuAktif("saya")

            val myUsername = prefs.getString("namalengkap", "")
            fetchJadwalGuru(myUsername)
            binding.jadwalv2Toolbar.title = myUsername
        }

        binding.btnPencarian.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup, AutoTransition())

            if (binding.cardPencarian.isVisible) {
                binding.cardPencarian.visibility = View.GONE
                setMenuAktif("saya")
            } else {
                binding.cardPencarian.visibility = View.VISIBLE
                setMenuAktif("pencarian")
                binding.dropdownPencarian.requestFocus()
            }

            binding.dropdownPencarian.post {
                binding.dropdownPencarian.showDropDown()
            }
        }

        binding.dropdownPencarian.setOnItemClickListener { parent, _, position, _ ->
            val itemTerpilih = parent.getItemAtPosition(position).toString()

            TransitionManager.beginDelayedTransition(binding.root as ViewGroup, AutoTransition())
            binding.cardPencarian.visibility = View.GONE
            binding.dropdownPencarian.text.clear()

            binding.jadwalv2Toolbar.title = itemTerpilih

            if (listKelas.contains(itemTerpilih)) {
                fetchJadwalKelas(itemTerpilih)
            } else {
                fetchJadwalGuru(itemTerpilih)
            }
        }
    }

    private fun loadDataPencarian() {
        listKelas = resources.getStringArray(R.array.kelasnya)
        val request = StringRequest(
            Request.Method.GET, AppConstants.LIST_GURU,
            { response ->
                try {
                    val jsonArray = JSONArray(response)
                    val namaGuruList = mutableListOf<String>()
                    val listGabungan = mutableListOf<String>()

                    listGabungan.addAll(listKelas)

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val nama = obj.getString("nama")
                        val username = obj.getString("username")

                        namaGuruList.add(nama)
                        gukarMap[nama] = username
                        listGabungan.add(nama)
                    }
                    listGuru = namaGuruList

                    val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listGabungan)
                    binding.dropdownPencarian.setAdapter(adapter)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun setMenuAktif(menu: String) {
        val colorAktif = ContextCompat.getColor(this, R.color.md_theme_primary)
        val colorNonAktif = ContextCompat.getColor(this, R.color.md_theme_inversePrimary)

        if (menu == "saya") {
            binding.btnJadwalSaya.setColorFilter(colorAktif)
            binding.btnPencarian.setColorFilter(colorNonAktif)
        } else if (menu == "pencarian") {
            binding.btnJadwalSaya.setColorFilter(colorNonAktif)
            binding.btnPencarian.setColorFilter(colorAktif)
        }
    }


    private fun fetchJadwalGuru(namaGuru: String?) {
        val url = AppConstants.BASE_URL + "api_jadwal_guru.php?guru=$namaGuru"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        listDataJadwal.clear()
                        val jsonArrayData = jsonResponse.getJSONArray("data")

                        generateHeaderJam(10) // Panggil header Jam 1-10

                        for (i in 0 until jsonArrayData.length()) {
                            val hariObj = jsonArrayData.getJSONObject(i)
                            val namaHari = hariObj.getString("hari")
                            val jadwalArray = hariObj.getJSONArray("jadwal")

                            val listJadwalSementara = ArrayList<JadwalItemModel>()

                            for (j in 0 until jadwalArray.length()) {
                                val jd = jadwalArray.getJSONObject(j)
                                listJadwalSementara.add(
                                    JadwalItemModel(
                                        jamKe = jd.getInt("jam_ke"),
                                        status = jd.getString("status"),
                                        mapel = jd.getString("mapel"),
                                        infoBawah = jd.getString("kelas")
                                    )
                                )
                            }
                            listDataJadwal.add(HariModel(namaHari, listJadwalSementara))
                        }

                        adapterJadwal = JadwalRowAdapter(listDataJadwal, "guru")
                        binding.rvJadwalUtama.adapter = adapterJadwal
                        binding.rvJadwalUtama.layoutManager = LinearLayoutManager(this)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error -> Toast.makeText(this, "Koneksi gagal", Toast.LENGTH_SHORT).show() }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun fetchJadwalKelas(namaKelas: String) {
        val url = AppConstants.API_JADWAL_KELAS + "?kelas=$namaKelas"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        listDataJadwal.clear()
                        val jsonArrayData = jsonResponse.getJSONArray("data")

                        generateHeaderJam(10) // Panggil header Jam 1-10

                        for (i in 0 until jsonArrayData.length()) {
                            val hariObj = jsonArrayData.getJSONObject(i)
                            val namaHari = hariObj.getString("hari")
                            val jadwalArray = hariObj.getJSONArray("jadwal")

                            val listJadwalSementara = ArrayList<JadwalItemModel>()

                            for (j in 0 until jadwalArray.length()) {
                                val jd = jadwalArray.getJSONObject(j)
                                listJadwalSementara.add(
                                    JadwalItemModel(
                                        jamKe = jd.getInt("jam_ke"),
                                        status = jd.getString("status"),
                                        mapel = jd.getString("mapel"),
                                        // PERBAIKAN 1: Ambil nama_guru dari JSON, bukan kelas
                                        infoBawah = jd.optString("nama_guru", "")
                                    )
                                )
                            }
                            listDataJadwal.add(HariModel(namaHari, listJadwalSementara))
                        }

                        // PERBAIKAN 2: Ubah string identifier menjadi "kelas"
                        adapterJadwal = JadwalRowAdapter(listDataJadwal, "kelas")
                        binding.rvJadwalUtama.adapter = adapterJadwal
                        binding.rvJadwalUtama.layoutManager = LinearLayoutManager(this)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error -> Toast.makeText(this, "Koneksi gagal", Toast.LENGTH_SHORT).show() }
        )
        Volley.newRequestQueue(this).add(request)
    }


    // --- GENERATE HEADER HARI DI ATAS TABEL ---
    private fun generateHeaderJam(maxJam: Int) {
        binding.containerHeaderAngka.removeAllViews()
        for (i in 1..maxJam) {
            val tvHeader = TextView(this).apply {
                val widthPx = (94 * resources.displayMetrics.density).toInt()
                layoutParams = LinearLayout.LayoutParams(widthPx, LinearLayout.LayoutParams.WRAP_CONTENT)
                text = "Jam $i"
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setTypeface(null, Typeface.BOLD)
                textSize = 12f
            }
            binding.containerHeaderAngka.addView(tvHeader)
        }
    }
    private fun fetchDataJadwal() {
        // 1. Buat RequestQueue Volley
        val requestQueue = Volley.newRequestQueue(this)

        // 2. Buat JsonObjectRequest karena response terluar API adalah Object { "status": ..., "data": [...] }
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, "https://sman1sukorejo.sch.id/esmansa/jadwal/api_jadwal.php", null,
            { response ->
                try {
                    // 3. Cek status dari JSON Response
                    val status = response.getString("status")

                    if (status == "success") {
                        // 4. Ambil array "data"
                        val dataArray = response.getJSONArray("data")

                        if (dataArray.length() > 0) {
                            // Ambil data urutan pertama (index 0)
                            val jadwalAktif = dataArray.getJSONObject(0)

                            val namaJadwal = jadwalAktif.getString("nama_jadwal")
                            val linkJadwal = jadwalAktif.getString("link_jadwal")

                            // Jika di database path-nya relatif, gabungkan dengan base URL
                            // val finalImageUrl = baseImageUrl + linkJadwal
                            val finalImageUrl =
                                "https://sman1sukorejo.sch.id/esmansa/jadwal/$linkJadwal"

                            if (!isDestroyed && !isFinishing) {
                                Glide.with(this@JadwalV2)
                                    .load(finalImageUrl)
                                    .placeholder(R.drawable.cobajadwal) // Gambar sementara
                                    .error(R.drawable.icm_lapor)         // Gambar jika gagal/error
                                    .into(binding.imgJadwal)
                            }

                            Log.d("VOLLEY_SUCCESS", "Berhasil load: $namaJadwal")
                            binding.jadwalv2Toolbar.title = namaJadwal
                        } else {
                            Toast.makeText(this, "Data jadwal kosong", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val message = response.getString("message")
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing data JSON", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                // 6. Tangani jika terjadi error koneksi (server mati, no internet)
                Log.e("VOLLEY_ERROR", error.toString())
                Toast.makeText(this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
            }
        )

        // 7. Masukkan request ke dalam antrean (Queue) agar dieksekusi
        requestQueue.add(jsonObjectRequest)
    }
}