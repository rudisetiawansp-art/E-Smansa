package id.rudissp.esmansa.siswa

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivitySiswaBinding
import org.json.JSONArray
import java.net.URLEncoder

class Siswa : AppCompatActivity() {
    lateinit var binding: ActivitySiswaBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var adapter: SiswaGWAdapter
    private lateinit var siswaList: ArrayList<SiswaGW>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        binding = ActivitySiswaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tolbarSiswa.setNavigationOnClickListener {
            finish()
        }

        val sayawakel = prefs.getString("kelas", "")

        binding.rvGwlist.layoutManager = LinearLayoutManager(this)
        siswaList = ArrayList()
        adapter = SiswaGWAdapter(this, siswaList)
        binding.rvGwlist.adapter = adapter

        binding.loadingIndicator.visibility = View.VISIBLE
        loadData("siswa_saya")

        binding.siswaAsuh.setOnClickListener {
            loadData("siswa_saya")
            binding.loadingIndicator.visibility = View.VISIBLE
        }

        binding.siswaSemuanya.setOnClickListener {
            loadData("siswa_semua")
            binding.loadingIndicator.visibility = View.VISIBLE
        }

        binding.searchBox.addTextChangedListener { text ->
            adapter.filter.filter(text.toString())
            binding.clearButton.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        binding.clearButton.setOnClickListener {
            binding.searchBox.text.clear()
        }

        if (sayawakel == "XXXX" || sayawakel.isNullOrEmpty()) {
            binding.siswaPerwalian.visibility = View.GONE
        } else {
            binding.siswaPerwalian.visibility = View.VISIBLE
            binding.siswaPerwalian.setOnClickListener {
                loadData("siswa_perwalian")
                binding.loadingIndicator.visibility = View.VISIBLE
            }
        }
    }

    private fun loadData(mode: String) {
        siswaList.clear()
        adapter.notifyDataSetChanged()

        val namalengkap = prefs.getString("namalengkap", "") ?: ""
        val waliEncoded = URLEncoder.encode(namalengkap, "UTF-8")
        val sayawakel = prefs.getString("kelas", "") ?: ""
        val kelasEncoded = URLEncoder.encode(sayawakel, "UTF-8")

        val url = when (mode) {
            "siswa_saya" -> "${AppConstants.GETSISWABYWALI}?guruwali=$waliEncoded"
            "siswa_perwalian" -> "${AppConstants.GETSISWABYWAKEL}?kelas=$kelasEncoded"
            else -> AppConstants.GET_SISWA
        }

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val jsonArray = JSONArray(response)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        siswaList.add(
                            SiswaGW(
                                id = obj.optString("id", ""),
                                nama = obj.optString("nama", ""),
                                nis = obj.optString("nis", ""),
                                kelas = obj.optString("kelas", ""),
                                wali = obj.optString("guruwali", ""),
                                foto = obj.optString("foto", ""),
                                pass = obj.optString("pass", ""),
                                nisn = obj.optString("nisn", ""),
                                device_id = obj.optString("device_id", "")
                            )
                        )
                    }

                    // Panggil fungsi untuk mengambil semua data walikelas sekaligus (Hanya 1x Request)
                    fetchAllWalikelas { walikelasMap ->
                        binding.loadingIndicator.visibility = View.GONE
                        adapter.setWalikelasMap(walikelasMap)
                        adapter.updateData(siswaList)
                    }

                } catch (e: Exception) {
                    binding.loadingIndicator.visibility = View.GONE
                    e.printStackTrace()
                }
            },
            { error ->
                binding.loadingIndicator.visibility = View.GONE
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        // Mencegah Volley mengirim ulang request berkali-kali saat koneksi lemah
        request.retryPolicy = com.android.volley.DefaultRetryPolicy(
            0,
            com.android.volley.DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun fetchAllWalikelas(callback: (Map<String, String>) -> Unit) {
        val url = "https://sman1sukorejo.sch.id/smania/android/api_wakel.php" // Memanggil API tanpa parameter kelas

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val walikelasMap = mutableMapOf<String, String>()
                try {
                    val status = response.optString("status")
                    if (status == "success") {
                        val dataArray = response.optJSONArray("data")
                        if (dataArray != null) {
                            for (i in 0 until dataArray.length()) {
                                val item = dataArray.getJSONObject(i)
                                val kelas = item.optString("kelas", "")
                                val wakel = item.optString("walikelas", "Tidak diketahui")
                                if (kelas.isNotEmpty()) {
                                    walikelasMap[kelas] = wakel
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                callback(walikelasMap)
            },
            { error ->
                error.printStackTrace()
                callback(emptyMap())
            }
        )
        // Mencegah Volley mengirim ulang request berkali-kali saat koneksi lemah
        request.retryPolicy = com.android.volley.DefaultRetryPolicy(
            0,
            com.android.volley.DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(this).add(request)
    }
}

data class SiswaGW(
    val id: String,
    val nama: String,
    val nis: String,
    val kelas: String,
    val wali: String,
    val foto: String,
    val pass: String,
    val nisn: String,
    val device_id: String
)

class SiswaGWAdapter(
    private val context: AppCompatActivity,
    private var siswaList: ArrayList<SiswaGW>,
    private var walikelasMap: Map<String, String> = emptyMap()
) : RecyclerView.Adapter<SiswaGWAdapter.SiswaViewHolder>(), Filterable {

    private var siswaListFull: ArrayList<SiswaGW> = ArrayList(siswaList)

    class SiswaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaText: TextView = itemView.findViewById(R.id.item_gwlist_nama)
        val kelasText: TextView = itemView.findViewById(R.id.item_gwlist_kelas)
        val fotoText: ShapeableImageView = itemView.findViewById(R.id.item_gwlist_foto)
        val wakel: TextView = itemView.findViewById(R.id.item_gwlist_wakel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiswaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gwlist, parent, false)
        return SiswaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SiswaViewHolder, position: Int) {
        val siswa = siswaList[position]
        holder.namaText.text = siswa.nama + " - " + siswa.kelas
        holder.kelasText.text = "Guru Wali: " + siswa.wali

        // Ambil data walikelas dari Map, jika tidak ada tampilkan 'Tidak diketahui'
        holder.wakel.text = "Wali Kelas: " + (walikelasMap[siswa.kelas] ?: "Tidak diketahui")
        holder.wakel.visibility = View.VISIBLE

        holder.itemView.setOnClickListener {
            if (siswaList.isNotEmpty() && position < siswaList.size) {
                val bundle = Bundle()
                val intent = Intent(holder.itemView.context, EditSiswa::class.java)
                intent.putExtra("id", siswa.id)
                intent.putExtra("nis", siswa.nis)
                intent.putExtra("nama", siswa.nama)
                intent.putExtra("pass", siswa.pass)
                intent.putExtra("kelas", siswa.kelas)
                intent.putExtra("guruwali", siswa.wali)
                intent.putExtra("device_id", siswa.device_id)
                intent.putExtra("nisn", siswa.nisn)
                intent.putExtras(bundle)
                holder.itemView.context.startActivity(intent)
            } else {
                Toast.makeText(context, "Data belum siap", Toast.LENGTH_SHORT).show()
            }
        }

        val urlFoto = "https://sman1sukorejo.sch.id/smania/fotoprofil/" + siswa.foto
        Glide.with(holder.itemView.context)
            .load(urlFoto)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_keluar)
            .into(holder.fotoText)
    }

    fun updateData(newList: ArrayList<SiswaGW>) {
        siswaList = newList
        siswaListFull = ArrayList(newList)
        notifyDataSetChanged()
    }

    fun setWalikelasMap(map: Map<String, String>) {
        this.walikelasMap = map
    }

    override fun getItemCount(): Int = siswaList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = ArrayList<SiswaGW>()
                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(siswaListFull)
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    for (item in siswaListFull) {
                        if (item.nama.lowercase().contains(filterPattern) ||
                            item.nis.lowercase().contains(filterPattern) ||
                            item.kelas.lowercase().contains(filterPattern)
                        ) {
                            filteredList.add(item)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                siswaList.clear()
                siswaList.addAll(results?.values as ArrayList<SiswaGW>)
                notifyDataSetChanged()
            }
        }
    }
}