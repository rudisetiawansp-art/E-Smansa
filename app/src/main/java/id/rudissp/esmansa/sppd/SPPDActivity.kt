package id.rudissp.esmansa.sppd

import android.content.Intent
import android.content.SharedPreferences
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
import id.rudissp.esmansa.databinding.ActivitySppdBinding
import org.json.JSONArray

class SPPDActivity : AppCompatActivity() {
    lateinit var binding: ActivitySppdBinding
    lateinit var prefs: SharedPreferences
    private lateinit var sppdAdapter: SppdAdapter
    private val sppdList = mutableListOf<SppdModel>()

    override fun onCreate(savedInstanceState: Bundle?) {enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        binding = ActivitySppdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabSppd.setOnClickListener {
            startActivity(Intent(this, TambahSppdActivity::class.java))
            finish()
        }

        binding.toolbarnyaSppd.setNavigationOnClickListener {
            finish()
        }

        sppdAdapter = SppdAdapter(this, sppdList)
        binding.rvSppd.layoutManager = LinearLayoutManager(this)
        binding.rvSppd.adapter = sppdAdapter

        binding.swipeRefreshSppd.setOnRefreshListener {
            getData()
        }

        getData()

    }

    private fun getData() {
        val username = prefs.getString("username", "")
        val url = AppConstants.GET_SPPD+ "?username=$username"

        binding.progressBarSppd.visibility = View.VISIBLE
        sppdList.clear()

        val request = StringRequest(Request.Method.GET, url, { response ->
            binding.progressBarSppd.visibility = View.GONE
            binding.swipeRefreshSppd.isRefreshing = false

            val dataArray = JSONArray(response)
            if (dataArray.length() == 0) {
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                binding.tvEmpty.visibility = View.GONE
                for (i in 0 until dataArray.length()) {
                    val item = dataArray.getJSONObject(i)
                    sppdList.add(
                        SppdModel(
                            item.getString("id"),
                            item.getString("username"),
                            item.getString("nama"),
                            item.getString("jabatan"),
                            item.getString("nip"),
                            item.getString("tanggal"),
                            item.getString("lokasi"),
                            item.getString("deskripsi"),
                            item.getString("foto"),
                            item.getString("foto_lokasi")
                        )
                    )
                }
                sppdAdapter.notifyDataSetChanged()
            }
        }, {
            binding.progressBarSppd.visibility = View.GONE
            binding.swipeRefreshSppd.isRefreshing = false
            Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show()
        })

        Volley.newRequestQueue(this).add(request)
    }


}