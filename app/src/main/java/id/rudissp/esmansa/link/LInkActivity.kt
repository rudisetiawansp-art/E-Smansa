package id.rudissp.esmansa.link

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityLinkBinding
import org.json.JSONException
import org.json.JSONObject
import kotlin.jvm.java

class LInkActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLinkBinding
    private lateinit var adapter: LinkAdapter

    lateinit var prefs: SharedPreferences

    private var listDataGabungan = ArrayList<Any>()

    private val urlApiLink = AppConstants.LINK + "?action=tampil"


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityLinkBinding.inflate(layoutInflater)
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        setContentView(binding.root)

        binding.toolbarnyaLink.setNavigationOnClickListener { finish() }

        binding.rvLink.layoutManager = LinearLayoutManager(this)
        adapter = LinkAdapter(listDataGabungan)
        binding.rvLink.adapter = adapter

        binding.swipeRefreshLink.setOnRefreshListener {
            loadDataLink()
        }

        binding.fabLink.setOnClickListener {
            val aaa = Intent(this, TambahLink::class.java)
            startActivity(aaa)
        }

        val namalengkap = prefs.getString("namalengkap", "")
        if (namalengkap == "dd" || namalengkap == "Rudi Setiawan S.P., S.Pd") {
            binding.fabLink.visibility = View.VISIBLE
        } else {
            binding.fabLink.visibility = View.GONE
        }


        loadDataLink()
    }

    private fun loadDataLink() {
        if (!binding.swipeRefreshLink.isRefreshing) {
            binding.progressBarTampil.visibility = View.VISIBLE
        }

        binding.tvEmpty.visibility = View.GONE

        val stringRequest = StringRequest(
            Request.Method.GET, urlApiLink,
            { response ->
                binding.progressBarTampil.visibility = View.GONE
                binding.swipeRefreshLink.isRefreshing = false // Matikan animasi loading

                try {
                    val jsonObject = JSONObject(response)
                    val status = jsonObject.getBoolean("status")

                    if (status) {
                        val jsonArray = jsonObject.getJSONArray("data")
                        val listReguler = ArrayList<LinkModel>()
                        val listSosmed = ArrayList<LinkModel>()

                        // Ekstrak data JSON
                        for (i in 0 until jsonArray.length()) {
                            val dataObj = jsonArray.getJSONObject(i)
                            val link = LinkModel(
                                id = dataObj.getString("id"),
                                judul = dataObj.getString("judul"),
                                link = dataObj.getString("link"),
                                kategori = dataObj.getString("kategori"),
                                platform = dataObj.optString("platform", "")
                            )

                            // Pisahkan berdasarkan kategori
                            if (link.kategori.equals("Reguler", ignoreCase = true)) {
                                listReguler.add(link)
                            } else {
                                listSosmed.add(link)
                            }
                        }

                        val dataFinal = ArrayList<Any>()

                        if (listReguler.isNotEmpty()) {
                            dataFinal.add("Link Penting")
                            dataFinal.addAll(listReguler)
                        }

                        if (listSosmed.isNotEmpty()) {
                            dataFinal.add("Sosial Media Resmi")
                            dataFinal.addAll(listSosmed)
                        }

                        // Perbarui Adapter
                        adapter.updateData(dataFinal)

                        // Tampilkan pesan kosong jika semua benar-benar kosong
                        if (dataFinal.isEmpty()) {
                            binding.tvEmpty.visibility = View.VISIBLE
                        }

                    } else {
                        // Jika status dari PHP false (Belum ada data)
                        adapter.updateData(ArrayList()) // Kosongkan
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.tvEmpty.text = jsonObject.getString("pesan")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing data JSON", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                binding.progressBarTampil.visibility = View.GONE
                binding.swipeRefreshLink.isRefreshing = false
                Toast.makeText(this, "Error koneksi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(stringRequest)
    }
}