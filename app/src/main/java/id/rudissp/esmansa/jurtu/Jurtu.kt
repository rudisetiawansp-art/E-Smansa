package id.rudissp.esmansa.jurtu

import android.content.Intent
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Collections
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.databinding.ActivityJurtuBinding
import org.json.JSONArray
import org.json.JSONObject

class Jurtu : AppCompatActivity() {

    lateinit var binding: ActivityJurtuBinding
    lateinit var prefs: SharedPreferences

    private lateinit var jurtuAdapter: JurtuAdapter
    private val jurtuList = mutableListOf<JurtuModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        binding = ActivityJurtuBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val currentuser = prefs.getString("namalengkap", "")
        binding.toolbarnyaJurtu.subtitle = currentuser

        binding.toolbarnyaJurtu.setNavigationOnClickListener { finish() }

        binding.fabJurtu.setOnClickListener {
            startActivity(Intent(this, TambahJurtu::class.java))
            finish()

        }

        jurtuAdapter = JurtuAdapter(this, jurtuList)
        binding.rvJurtu.layoutManager = LinearLayoutManager(this)
        binding.rvJurtu.adapter = jurtuAdapter

        binding.swipeRefreshJurtu.setOnRefreshListener {
            getData()
        }

        getData()

    }

    private fun getData() {
        val currentuser = prefs.getString("namalengkap", "")
        val url = AppConstants.GET_JURTU + "?nama=$currentuser"

        binding.progressBarJurtu.visibility = View.VISIBLE
        jurtuList.clear()

        val request = StringRequest(Request.Method.GET, url, { response ->
            binding.progressBarJurtu.visibility = View.GONE
            binding.swipeRefreshJurtu.isRefreshing = false

            val dataArray = JSONArray(response)
            if (dataArray.length() == 0) {
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                binding.tvEmpty.visibility = View.GONE

                // 1. Pindahkan data dari JSONArray ke ArrayList biasa agar mudah diurutkan
                val jsonList = ArrayList<JSONObject>()
                for (i in 0 until dataArray.length()) {
                    jsonList.add(dataArray.getJSONObject(i))
                }

                // 2. Proses Mengurutkan (Sorting) berdasarkan Tanggal, lalu Jam 1
                jsonList.sortWith(Comparator { obj1, obj2 ->
                    val tgl1 = obj1.getString("tanggal")
                    val tgl2 = obj2.getString("tanggal")

                    // Bandingkan tanggalnya terlebih dahulu
                    val compareTanggal = tgl1.compareTo(tgl2) // Gunakan tgl2.compareTo(tgl1) jika ingin urutan dari yang Terbaru (Descending)

                    if (compareTanggal != 0) {
                        compareTanggal
                    } else {
                        // Jika tanggalnya sama, bandingkan berdasarkan jam1
                        val jamAwal1 = obj1.getString("jam1")
                        val jamAwal2 = obj2.getString("jam1")
                        jamAwal1.compareTo(jamAwal2) // Gunakan jamAwal2.compareTo(jamAwal1) jika ingin Descending
                    }
                })

                // 3. Persiapkan Format Tanggal (Dari YYYY-MM-DD ke dd MMMM yyyy bahasa Indonesia)
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

                // 4. Masukkan data yang sudah urut ke dalam JurtuModel
                for (item in jsonList) {
                    // --- Format Tanggal ---
                    val rawTanggal = item.getString("tanggal")
                    var tanggalIndonesia = rawTanggal // Default jika terjadi error parse
                    try {
                        val parsedDate = inputFormat.parse(rawTanggal)
                        if (parsedDate != null) {
                            tanggalIndonesia = outputFormat.format(parsedDate)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // --- Format Jam (Hilangkan Detik) ---
                    val rawJam1 = item.getString("jam1")
                    val rawJam2 = item.getString("jam2")
                    val jam1TanpaDetik = if (rawJam1.length >= 5) rawJam1.substring(0, 5) else rawJam1
                    val jam2TanpaDetik = if (rawJam2.length >= 5) rawJam2.substring(0, 5) else rawJam2

                    // --- Tambahkan ke List Model ---
                    jurtuList.add(
                        JurtuModel(
                            item.getString("id"),
                            item.getString("nama"),
                            tanggalIndonesia, // <-- Masukkan tanggal yang sudah diubah formatnya
                            jam1TanpaDetik,
                            jam2TanpaDetik,
                            item.getString("kegiatan"),
                            item.getString("foto")
                        )
                    )
                }
                jurtuAdapter.notifyDataSetChanged()
            }

        }, {
            binding.progressBarJurtu.visibility = View.GONE
            binding.swipeRefreshJurtu.isRefreshing = false
            Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show()
        })

        Volley.newRequestQueue(this).add(request)
    }
}