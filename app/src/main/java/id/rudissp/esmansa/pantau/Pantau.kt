package id.rudissp.esmansa.pantau

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityPantauBinding
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Pantau : AppCompatActivity() {

    // Deklarasi ViewBinding
    private lateinit var binding: ActivityPantauBinding

    private val listDataDashboard = ArrayList<KelasModel>()
    private lateinit var adapter: KelasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Inisialisasi ViewBinding
        binding = ActivityPantauBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar (Opsional, agar tombol back berfungsi jika ada)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        // Setup RecyclerView Utama dengan ViewBinding
        adapter = KelasAdapter(listDataDashboard)
        binding.rvPemantauanUtama.layoutManager = LinearLayoutManager(this)
        binding.rvPemantauanUtama.adapter = adapter

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val tanggalHariIni = sdf.format(Date())

        fetchDataPemantauan(tanggalHariIni)
    }

    private fun fetchDataPemantauan(tanggal: String) {
        // Tampilkan ProgressBar melalui binding
        binding.progressBar.visibility = View.VISIBLE

        // Sesuaikan dengan URL server kamu
        val url = AppConstants.BASE_URL+"pantau/api_pemantauan.php?tanggal=$tanggal"

        val queue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                // Sembunyikan ProgressBar
                binding.progressBar.visibility = View.GONE

                try {
                    val status = response.getString("status")
                    if (status == "success") {
                        listDataDashboard.clear()

                        val maxJam = response.getInt("max_jam")
                        generateHeaderJam(maxJam)

                        val jsonArrayData = response.getJSONArray("data")

                        for (i in 0 until jsonArrayData.length()) {
                            val kelasObj = jsonArrayData.getJSONObject(i)
                            val namaKelas = kelasObj.getString("kelas")
                            val jadwalArray = kelasObj.getJSONArray("jadwal")

                            val listJadwalSementara = ArrayList<JadwalModel>()

                            for (j in 0 until jadwalArray.length()) {
                                val jadwalObj = jadwalArray.getJSONObject(j)

                                val modelJadwal = JadwalModel(
                                    jamKe = jadwalObj.getInt("jam_ke"),
                                    status = jadwalObj.getString("status"),
                                    guru = jadwalObj.getString("guru"),
                                    mapel = jadwalObj.getString("mapel"),
                                    keterangan = jadwalObj.getString("keterangan")
                                )
                                listJadwalSementara.add(modelJadwal)
                            }

                            listDataDashboard.add(KelasModel(namaKelas, listJadwalSementara))
                        }

                        // Beritahu adapter untuk memperbarui UI
                        adapter.notifyDataSetChanged()

                    } else {
                        Toast.makeText(this, "Gagal memuat data dari server", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Format data tidak sesuai", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                binding.progressBar.visibility = View.GONE
                error.printStackTrace()
                Toast.makeText(this, "Koneksi ke server terputus", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(request)
    }

    private fun generateHeaderJam(maxJam: Int) {
        binding.containerHeaderAngka.removeAllViews()

        for (i in 1..maxJam) {
            val tvHeader = TextView(this).apply {

                val widthPx = (94 * resources.displayMetrics.density).toInt()
                layoutParams = LinearLayout.LayoutParams(widthPx, LinearLayout.LayoutParams.WRAP_CONTENT)

                text = "Jam $i"
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            binding.containerHeaderAngka.addView(tvHeader)
        }
    }
}