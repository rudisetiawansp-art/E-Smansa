package id.rudissp.esmansa.presensi.siswa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityPresensiSiswaIndividuBinding
import id.rudissp.esmansa.presensi.RekapPresensiAdapter
import id.rudissp.esmansa.presensi.RekapPresensia
import org.json.JSONArray
import org.json.JSONException
import java.util.Calendar

class PresensiSiswaIndividu : AppCompatActivity() {
    lateinit var binding : ActivityPresensiSiswaIndividuBinding
    private val daftarRekapq = ArrayList<RekapPresensixx>()
    private lateinit var adapter: PresensiSiswaIndividuAdapter

    private val bulanMap = mapOf(
        "Januari" to 1, "Februari" to 2, "Maret" to 3, "April" to 4,
        "Mei" to 5, "Juni" to 6, "Juli" to 7, "Agustus" to 8,
        "September" to 9, "Oktober" to 10, "November" to 11, "Desember" to 12
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPresensiSiswaIndividuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PresensiSiswaIndividuAdapter(daftarRekapq)
        binding.recyclerViewRiwayat.adapter = adapter
        binding.recyclerViewRiwayat.layoutManager = LinearLayoutManager(this)

        val calendar = Calendar.getInstance()
        val bulanSekarang = calendar.get(Calendar.MONTH)
        val tahunSekarang = calendar.get(Calendar.YEAR)

        val bulanArray = resources.getStringArray(R.array.months)
        binding.bulannya.setText(bulanArray[bulanSekarang], false)
        binding.tahunnya.setText(tahunSekarang.toString())
        ambilRekap()

        binding.bulannya.setOnItemClickListener { _, _, _, _ -> ambilRekap() }
        binding.tahunnya.setOnEditorActionListener { _, _, _ ->
            ambilRekap()
            true
        }

        binding.toolbarPres.setNavigationOnClickListener {
            finish()
        }
        binding.toolbarPres.title = intent.getStringExtra("nama")


        val sssss = intent.getStringExtra("tugaskah")?: ""





    }
    private fun ambilRekap() {
        val bulan = binding.bulannya.text.toString()
        val bulanAngka = bulanMap[bulan]
        if (bulanAngka == null) {
            Toast.makeText(this, "Bulan tidak valid", Toast.LENGTH_SHORT).show()
            return
        }
        val tahun = binding.tahunnya.text.toString()

        if (bulan.isEmpty() || tahun.isEmpty()) {
            Toast.makeText(this, "Bulan dan Tahun wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        //val bulanAngka = resources.getStringArray(R.array.months).indexOf(bulan) + 1
        val queue = Volley.newRequestQueue(this)
        val request = object : StringRequest(Method.POST, AppConstants.SMANIA_URL + "android/presensi_rekap.php",
            { response ->
                try {
                    val jsonArray = JSONArray(response)
                    daftarRekapq.clear()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val data = RekapPresensixx(
                            obj.getString("tanggal"),
                            obj.getString("masuk"),
                            obj.getString("pulang"),
                            obj.getString("status"),
                            obj.getString("shift")

                        )
                        daftarRekapq.add(data)
                    }
                    adapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    Toast.makeText(this, "Data tidak valid", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Gagal koneksi: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val nis = intent.getStringExtra("nis")?: ""
                return mapOf(
                    "nis" to nis,
                    "bulan" to bulanAngka.toString(),
                    "tahun" to tahun
                )
            }
        }

        queue.add(request)
    }
}

class PresensiSiswaIndividuAdapter(private val items: ArrayList<RekapPresensixx>) :
    RecyclerView.Adapter<PresensiSiswaIndividuAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTanggal: TextView = view.findViewById(R.id.tvTanggal)
        val txtMasuk: TextView = view.findViewById(R.id.tvMasuk)
        val txtPulang: TextView = view.findViewById(R.id.tvPulang)
        val txtKeterangan: TextView = view.findViewById(R.id.tvjenis)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rekap_presensi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.txtTanggal.text = item.tanggal
        holder.txtMasuk.text = item.masuk
        holder.txtPulang.text = item.pulang

        if (item.shift == "malam") {
            holder.txtKeterangan.text = "Piket"

            val color = ContextCompat.getColor(holder.itemView.context, R.color.md_theme_tertiary_highContrast)
            holder.txtKeterangan.setTextColor(color)
            holder.txtMasuk.setTextColor(color)
            holder.txtPulang.setTextColor(color)

        } else{
            holder.txtKeterangan.text = item.jenis
        }
    }

    override fun getItemCount(): Int = items.size

}

data class RekapPresensixx(
    val tanggal: String,
    val masuk: String,
    val pulang: String,
    val jenis: String,
    val shift: String
)