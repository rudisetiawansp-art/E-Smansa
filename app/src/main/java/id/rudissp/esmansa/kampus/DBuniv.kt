package id.rudissp.esmansa.kampus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.loadingindicator.LoadingIndicator
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityDbunivBinding
import id.rudissp.esmansa.databinding.ItemKampusBinding

class DBuniv : AppCompatActivity() {
    lateinit var binding: ActivityDbunivBinding

    private lateinit var adapter: KampusAdapter
    private lateinit var progressBar: LoadingIndicator

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDbunivBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = "Daftar Kampus"

        adapter = KampusAdapter(emptyList())
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        fetchKampusData()

    }

    private fun fetchKampusData() {
        progressBar = findViewById(R.id.progressBar)
        progressBar.show()

        val url = AppConstants.BASE_URL + "api_universitas.php"
        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                progressBar.hide()

                val type = object : TypeToken<List<Kampus>>() {}.type
                val kampusList: List<Kampus> = gson.fromJson(response.toString(), type)

                val sorted = kampusList.sortedBy { it.nama }
                adapter.updateData(sorted)

            },
            { error ->
                progressBar.hide()

                error.printStackTrace()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView


        searchView.queryHint = "Cari kampus, kota, provinsi..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText)
                return true
            }
        })


        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean = true
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {

                adapter.filter("")
                return true
            }
        })

        return true
    }
}

data class Kampus(
    val no: String,
    val nama: String,
    val npsn: String,
    val nama_singkat: String,
    val kode_pos: String,
    val telepon: String,
    val fax: String,
    val email: String,
    val alamat: String,
    val kota: String,
    val provinsi: String,
    val negara: String,
    val lintang: String,
    val bujur: String,
    val akreditasi: String,
    val status: String,
    val rank_qs: String,
    val rank_country: String
)

class KampusAdapter(private var items: List<Kampus>) :
    RecyclerView.Adapter<KampusAdapter.ViewHolder>() {

    private var filteredItems = items

    inner class ViewHolder(private val binding: ItemKampusBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(kampus: Kampus) {
            binding.tvNama.text = kampus.nama
            binding.tvKotaProvinsi.text = "${kampus.kota}, ${kampus.provinsi}"
            binding.tvAkreditasi.text = kampus.akreditasi.ifEmpty { "Tidak terakreditasi" }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKampusBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredItems[position])
    }

    override fun getItemCount(): Int = filteredItems.size

    // Filter berdasarkan query (nama, kota, provinsi)
    fun filter(query: String?) {
        val q = query?.trim() ?: ""
        filteredItems = if (q.isEmpty()) {
            items
        } else {
            items.filter {
                it.nama.contains(q, ignoreCase = true) ||
                        it.kota.contains(q, ignoreCase = true) ||
                        it.provinsi.contains(q, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    // Untuk update data dari API
    fun updateData(newItems: List<Kampus>) {
        this.items = newItems
        this.filteredItems = newItems
        notifyDataSetChanged()
    }
}

