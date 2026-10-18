package id.rudissp.esmansa.presensi.siswa

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Filter
import android.widget.Filter.FilterResults
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginEnd
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityPresensiSiswaBinding
import id.rudissp.esmansa.siswa.EditSiswa
import id.rudissp.esmansa.siswa.SiswaGW
import id.rudissp.esmansa.siswa.SiswaGWAdapter
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.atomic.AtomicInteger


class PresensiSiswa : AppCompatActivity(), TabLayout.OnTabSelectedListener {

    lateinit var binding: ActivityPresensiSiswaBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var adapter: SiswaGWAdapter22
    private lateinit var siswaList: ArrayList<SiswaGW22>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        binding = ActivityPresensiSiswaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.searchResult.layoutManager = LinearLayoutManager(this)
        siswaList = ArrayList()
        adapter = SiswaGWAdapter22( siswaList)
        binding.searchResult.adapter = adapter
        loadData("siswa_semua")

        binding.searchView.editText.addTextChangedListener { editable ->
            val query = editable.toString()
            adapter.filter.filter(query)
        }

        binding.searchBar.setNavigationOnClickListener { finish() }

        val manager = supportFragmentManager
        val adapter = PresensiSiswaTabs(manager, lifecycle)
        binding.presensisiswaPager.setAdapter(adapter)
        binding.presensisiswaTabs.addTab(
            binding.presensisiswaTabs.newTab().setText("Perwalian Saya")
        )
        binding.presensisiswaTabs.addTab(binding.presensisiswaTabs.newTab().setText("Kelas"))


        binding.presensisiswaTabs.addOnTabSelectedListener(this)
        binding.presensisiswaPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.presensisiswaTabs.selectTab(binding.presensisiswaTabs.getTabAt(position))
            }
        })

    }

    override fun onTabSelected(p0: TabLayout.Tab?) {
        binding.presensisiswaPager.currentItem = p0!!.position
    }

    override fun onTabUnselected(p0: TabLayout.Tab?) {
    }

    override fun onTabReselected(p0: TabLayout.Tab?) {
    }

    private fun loadData(mode: String) {
        siswaList.clear()
        adapter.notifyDataSetChanged()

        val namalengkap = prefs.getString("namalengkap", "") ?: ""
        val waliEncoded = URLEncoder.encode(namalengkap, "UTF-8")
        val sayawakel = prefs.getString("kelas", "")
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
                    binding.loadingIndicator.visibility = View.GONE

                    val templist = ArrayList<SiswaGW22>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        templist.add(
                            SiswaGW22(
                                obj.getString("id"),
                                obj.getString("nama"),
                                obj.getString("nis"),
                                obj.getString("kelas"),
                                obj.getString("guruwali"),
                                obj.getString("foto"),
                                obj.getString("pass"),
                                obj.getString("nisn"),
                                obj.getString("device_id")
                            )

                        )
                    }

                    siswaList.clear()
                    siswaList.addAll(templist)
                    adapter.updateData(siswaList)

                    val uniqueClasses = siswaList.map { it.kelas }.distinct()
                    if (uniqueClasses.isEmpty()) {
                        // Tidak ada siswa, update adapter
                        adapter.updateData(siswaList)
                        return@StringRequest
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
        Volley.newRequestQueue(this).add(request)
    }


}

class PresensiSiswaTabs(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return PresensiSiswaWali()
            1 -> return PresensiSiswaKelas()

        }
        return PresensiSiswaWali()
    }

    override fun getItemCount(): Int {
        return 2

    }
}

data class SiswaGW22(
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

class SiswaGWAdapter22(
    private var siswaList: ArrayList<SiswaGW22>,
    private var walikelasMap: Map<String, String> = emptyMap()
) :
    RecyclerView.Adapter<SiswaGWAdapter22.SiswaViewHolder2>(), Filterable {

    private var siswaListFull: ArrayList<SiswaGW22> = ArrayList(siswaList)

    class SiswaViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaText: TextView = itemView.findViewById(R.id.item_gwlist_nama)
        val kelasText: TextView = itemView.findViewById(R.id.item_gwlist_kelas)
        val fotoText: ShapeableImageView = itemView.findViewById(R.id.item_gwlist_foto)
        val wakel = itemView.findViewById<TextView>(R.id.item_gwlist_wakel)
        val item_gwlist = itemView.findViewById<MaterialCardView>(R.id.item_gwlist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiswaViewHolder2 {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gwlist, parent, false)
        return SiswaViewHolder2(view)
    }

    override fun onBindViewHolder(holder: SiswaViewHolder2, position: Int) {
        val siswa = siswaList[position]
        val param = holder.item_gwlist.layoutParams as ViewGroup.MarginLayoutParams
        param.setMargins(8,4,8,4)
        holder.item_gwlist.layoutParams = param
        holder.namaText.text = siswa.nama
        holder.kelasText.text = siswa.kelas
        holder.wakel.text = "Wali Kelas: " + walikelasMap[siswa.kelas]
        holder.wakel.visibility = View.GONE

        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            val intent = Intent(holder.itemView.context, PresensiSiswaIndividu::class.java)
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

        }

        val urlFoto = "https://sman1sukorejo.sch.id/smania/fotoprofil/" + siswa.foto
        Glide.with(holder.itemView.context)
            .load(urlFoto)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_keluar)
            .into(holder.fotoText)

    }

    fun updateData(newList: ArrayList<SiswaGW22>) {
        siswaList = newList
        siswaListFull = ArrayList(newList)
        notifyDataSetChanged()
    }

    fun setWalikelasMap(map: Map<String, String>) {
        this.walikelasMap = map
        // tidak perlu notify, karena nanti di bind view akan ambil dari map
    }


    override fun getItemCount(): Int = siswaList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = ArrayList<SiswaGW22>()
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
                siswaList.addAll(results?.values as ArrayList<SiswaGW22>)
                notifyDataSetChanged()
            }
        }
    }
}