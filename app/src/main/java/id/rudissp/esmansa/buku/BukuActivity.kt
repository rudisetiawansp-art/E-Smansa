package id.rudissp.esmansa.buku

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityBukuBinding
import org.json.JSONException
import org.json.JSONObject

class BukuActivity : AppCompatActivity() {
    lateinit var binding: ActivityBukuBinding

    private lateinit var adapter: BukuAdapter
    private var listBukuMaster = ArrayList<BukuModel>()
    private var listBukuTampil = ArrayList<BukuModel>()

    private val urlApiTampil = AppConstants.GET_BUKU


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBukuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        binding.rvBuku.layoutManager = LinearLayoutManager(this)
        adapter = BukuAdapter(this, listBukuTampil)
        binding.rvBuku.adapter = adapter

        // Panggil fungsi untuk mengambil data
        loadDataBuku()

        binding.toolbarnyaBuku.setNavigationOnClickListener {
            finish()
        }

        binding.fabBuku.setOnClickListener {
            startActivity(Intent(this, TambahBuku::class.java))
            finish()
        }

        binding.swipeRefreshBuku.setOnRefreshListener {
            loadDataBuku()
        }

        setupMenuPencarian()


    }

    private fun loadDataBuku() {
        binding.progressBarTampil.visibility = View.VISIBLE
        listBukuMaster.clear()
        listBukuTampil.clear()

        val stringRequest = StringRequest(
            Request.Method.GET, urlApiTampil,
            { response ->
                binding.progressBarTampil.visibility = View.GONE
                binding.swipeRefreshBuku.isRefreshing = false
                try {
                    val jsonObject = JSONObject(response)
                    val status = jsonObject.getBoolean("status")

                    if (status) {
                        val jsonArray = jsonObject.getJSONArray("data")

                        // Looping data dari JSON Array
                        for (i in 0 until jsonArray.length()) {
                            val dataObj = jsonArray.getJSONObject(i)

                            val buku = BukuModel(
                                nobuku = dataObj.getString("nobuku"),
                                judul = dataObj.getString("judul"),
                                penulis = dataObj.getString("penulis"),
                                jumlah = dataObj.getString("jumlah"),
                                fotoDepan = dataObj.getString("foto_depan"),
                                fotoSamping = dataObj.getString("foto_samping"),
                                tanggalbeli = dataObj.getString("tanggalbeli"),
                                penerbit = dataObj.getString("penerbit"),
                                harga = dataObj.getString("harga"),
                                sumberdana = dataObj.getString("sumberdana"),
                                jenis = dataObj.getString("jenis")
                            )
                            listBukuMaster.add(buku)
                            listBukuTampil.add(buku)
                        }

                        // Beritahu adapter bahwa ada data baru
                        adapter.notifyDataSetChanged()
                    } else {
                        val pesan = jsonObject.getString("pesan")
                        Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing data JSON", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                binding.progressBarTampil.visibility = View.GONE
                binding.swipeRefreshBuku.isRefreshing = false
                Toast.makeText(this, "Error koneksi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun setupMenuPencarian() {

        val searchItem = binding.toolbarnyaBuku.menu.findItem(R.id.menubuku_cari)
        val searchView = searchItem.actionView as SearchView

        searchView.queryHint = "Cari judul atau penulis..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterData(newText ?: "")
                return true
            }
        })
    }

    private fun filterData(query: String) {
        val filteredList = ArrayList<BukuModel>()

        // Pengecekan dilakukan pada listBukuMaster agar data asli tidak hilang
        for (buku in listBukuMaster) {
            // Cek apakah judul ATAU penulis mengandung kata kunci yang diketik
            if (buku.judul.lowercase().contains(query.lowercase()) ||
                buku.penulis.lowercase().contains(query.lowercase())) {
                filteredList.add(buku)
            }
        }

        // Perbarui data di RecyclerView melalui adapter
        adapter.updateData(filteredList)
    }

}