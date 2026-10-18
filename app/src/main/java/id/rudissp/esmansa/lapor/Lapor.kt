package id.rudissp.esmansa.lapor

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.databinding.ActivityLaporBinding
import org.json.JSONException

class Lapor : AppCompatActivity() {

    lateinit var binding: ActivityLaporBinding
    lateinit var prefs: SharedPreferences

    private lateinit var laporAdapter: LaporAdapter
    private val listLaporan = ArrayList<LaporanModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        super.onCreate(savedInstanceState)
        binding = ActivityLaporBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvLapor.layoutManager = LinearLayoutManager(this)
        laporAdapter = LaporAdapter(this, listLaporan)
        binding.rvLapor.adapter = laporAdapter

        binding.toolbarnyaLapor.setNavigationOnClickListener {
            finish()
        }

        binding.swipeRefreshLapor.setOnRefreshListener {
            ambilDataLaporan()
        }

        ambilDataLaporan()

    }

    private fun ambilDataLaporan() {

        val nipGuru = prefs.getString("nip", "")
        val urlApi = AppConstants.GETLAPOR+"?nip=$nipGuru"

        binding.progressBarLapor.visibility = View.VISIBLE
        listLaporan.clear()

        val request = JsonObjectRequest(
            Request.Method.GET, urlApi, null,
            { response ->
                binding.swipeRefreshLapor.isRefreshing = false
                binding.progressBarLapor.visibility = View.GONE
                try {
                    val status = response.getString("status")
                    if (status == "success") {
                        val jsonArray = response.getJSONArray("data")
                        listLaporan.clear()

                        for (i in 0 until jsonArray.length()) {
                            val dataObj = jsonArray.getJSONObject(i)

                            val laporan = LaporanModel(
                                dataObj.getString("id_laporan"),
                                dataObj.getString("kategori"),
                                dataObj.getString("judul"),
                                dataObj.getString("isi_laporan"),
                                dataObj.getString("privasi"),
                                dataObj.getString("status_laporan"),
                                dataObj.getString("tgl_lapor"),
                                dataObj.getString("nama_pelapor"),
                                dataObj.getString("foto_url")
                            )
                            listLaporan.add(laporan)
                        }

                        // Beri tahu adapter kalau data sudah masuk
                        laporAdapter.notifyDataSetChanged()

                    } else {

                        val pesanError = response.getString("pesan")
                        Toast.makeText(this, "Gagal: $pesanError", Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing JSON", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                binding.swipeRefreshLapor.isRefreshing = false
                binding.progressBarLapor.visibility = View.GONE
                Log.e("API_ERROR", error.toString())
                Toast.makeText(this, "Tidak dapat terhubung ke server", Toast.LENGTH_SHORT).show()
            })

        Volley.newRequestQueue(this).add(request)
    }
}