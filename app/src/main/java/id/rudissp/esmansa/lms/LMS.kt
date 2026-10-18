package id.rudissp.esmansa.lms

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityLmsBinding
import org.json.JSONObject
import kotlin.jvm.java

class LMS : AppCompatActivity() {
    private lateinit var binding: ActivityLmsBinding
    private lateinit var prefs: SharedPreferences
    private val listLms = mutableListOf<LmsModel>()
    private lateinit var lmsAdapter: LmsAdapter

    private var namaGuruLogin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        binding = ActivityLmsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        fetchDataLMS()
    }

    override fun onResume() {
        super.onResume()
        fetchDataLMS()
    }

    private fun setupUI() {
        // Setup Toolbar
        binding.toolbarnyaLms.setNavigationOnClickListener { finish() }

        // Setup RecyclerView
        lmsAdapter = LmsAdapter(this, listLms)
        binding.rvLink.layoutManager = LinearLayoutManager(this)
        binding.rvLink.adapter = lmsAdapter

        // Setup SwipeRefresh
        binding.swipeRefreshLink.setOnRefreshListener {
            fetchDataLMS()
        }

        // Tampilkan FAB & Beri Aksi Pindah ke TambahTugasActivity
        binding.fabLink.visibility = View.VISIBLE
        binding.fabLink.text = "Buat Tugas / Materi"
        binding.fabLink.setOnClickListener {
            startActivity(Intent(this, TambahTugas::class.java))
        }
    }

    private fun fetchDataLMS() {
        namaGuruLogin = prefs.getString("namalengkap", "").toString()
        Log.d("coba",namaGuruLogin)
        binding.swipeRefreshLink.isRefreshing = true
        binding.tvEmpty.visibility = View.GONE

        val urlApi = AppConstants.GET_TUGAS

        val stringRequest = object : StringRequest(Method.POST, urlApi,
            Response.Listener { response ->
                binding.swipeRefreshLink.isRefreshing = false
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getString("status") == "success") {
                        val jsonArray = jsonObject.getJSONArray("data")
                        listLms.clear()

                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            listLms.add(
                                LmsModel(
                                    idTugas = item.getString("id_tugas"),
                                    jenis = item.getString("jenis"),
                                    mapel = item.getString("mapel"),
                                    judul = item.getString("judul"),
                                    daftarKelas = item.getString("daftar_kelas"),
                                    deadline = item.getString("deadline")
                                )
                            )
                        }
                        lmsAdapter.notifyDataSetChanged()
                    } else {
                        // Jika status error / tidak ada data
                        listLms.clear()
                        lmsAdapter.notifyDataSetChanged()
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.tvEmpty.text = "Belum ada tugas atau materi"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Format respon salah", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                binding.swipeRefreshLink.isRefreshing = false
                Toast.makeText(this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["nama_guru"] = namaGuruLogin
                return params
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }
}