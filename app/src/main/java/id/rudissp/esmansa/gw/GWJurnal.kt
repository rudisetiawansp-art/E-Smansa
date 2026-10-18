package id.rudissp.esmansa.gw

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityJurnalgwBinding
import id.rudissp.esmansa.jurnal.TambahJurnal
import org.json.JSONArray
import org.json.JSONObject


class GWJurnal : AppCompatActivity() {
    lateinit var binding: ActivityJurnalgwBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var jurnalgwadapter: JurnalGWAdapter
    private val jurnalgwlist = mutableListOf<JurnalGWModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        binding = ActivityJurnalgwBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabTambahjurnalgw.setOnClickListener {
            val intent = Intent(this, TambahJurnalGW::class.java)
            startActivity(intent)
            finish();
        }

        binding.tolbarJurnalgw.setNavigationOnClickListener {
            finish()
        }
        binding.tolbarJurnalgw.subtitle = prefs.getString("namalengkap", "")

        jurnalgwadapter = JurnalGWAdapter(this, jurnalgwlist)
        binding.rvJurnalgw.layoutManager = LinearLayoutManager(this)
        binding.rvJurnalgw.adapter = jurnalgwadapter

        binding.swipeRefreshJurnalgw.setOnRefreshListener {
            loaddata()
        }

        loaddata()

    }

    private fun loaddata() {
        val username = prefs.getString("username", "")
        val url = AppConstants.GET_JURNALGW + "?wali=" + username

        binding.progressBarJurnalgw.visibility = View.VISIBLE
        jurnalgwlist.clear()

        val request = StringRequest(Request.Method.GET, url, { response ->
            binding.progressBarJurnalgw.visibility = View.GONE
            binding.swipeRefreshJurnalgw.isRefreshing = false

            val dataArray = JSONArray(response)
            if (dataArray.length() == 0) {
                binding.emptyGw.visibility = View.VISIBLE
            } else {
                binding.emptyGw.visibility = View.GONE
                for (i in 0 until dataArray.length()) {
                    val obj = dataArray.getJSONObject(i)
                    jurnalgwlist.add(
                        JurnalGWModel(
                            obj.getString("id"),
                            obj.getString("tanggal"),
                            obj.getString("nama"),
                            obj.getString("topik"),
                            obj.getString("bentuk"),
                            obj.getString("tinjut"),
                            obj.getString("catatan"),
                            obj.getString("foto")
                        )
                    )
                }
                jurnalgwadapter.notifyDataSetChanged()

            }
        }, {
            binding.progressBarJurnalgw.visibility = View.GONE
            binding.swipeRefreshJurnalgw.isRefreshing = false
            Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_LONG).show()
        })

        Volley.newRequestQueue(this).add(request)
    }

}

data class JurnalGWModel(
    val id: String,
    val tanggal: String,
    val nama: String,
    val topik: String,
    val bentuk: String,
    val tinjut: String,
    val catatan: String,
    val foto: String
)

class JurnalGWAdapter(
    private val context: Context,
    private val list: MutableList<JurnalGWModel> // pakai MutableList biar bisa dihapus
) : RecyclerView.Adapter<JurnalGWAdapter.JurnalGWViewHolder>() {

    inner class JurnalGWViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tgl: TextView = view.findViewById(R.id.item_gwku_tanggal)
        val nama: TextView = view.findViewById(R.id.item_gwku_nama)
        val topik: TextView = view.findViewById(R.id.item_gwku_topik)
        val foto: ImageView = view.findViewById(R.id.item_gwku_gambar)
        val more: ImageView = view.findViewById(R.id.item_gwku_more)
        var recCard: MaterialCardView = itemView.findViewById(R.id.recCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JurnalGWViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gwjurnal, parent, false)
        return JurnalGWViewHolder(view)
    }

    override fun onBindViewHolder(holder: JurnalGWViewHolder, position: Int) {
        val item1 = list[position]
        holder.tgl.text = item1.tanggal
        holder.nama.text = item1.nama
        holder.topik.text = item1.topik

        holder.more.setOnClickListener {
            val popup = PopupMenu(context, holder.more)
            popup.inflate(R.menu.popup)
            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                if (menuItem.itemId == R.id.menunya_hapus) {
                    MaterialAlertDialogBuilder(context)
                        .setMessage("Yakin akan menghapus data ini?")
                        .setPositiveButton("Ya, Hapus...") { _, _ ->
                            deleteJurnal(item1.id, position)
                        }
                        .setNegativeButton("Batal", null)
                        .show()
                }
                true
            }
            popup.show()
        }

        holder.recCard.setOnClickListener {
            val intent = Intent(context, TambahJurnalGW::class.java)
            intent.putExtra("id", item1.id)
            intent.putExtra("tanggal", item1.tanggal)
            intent.putExtra("nama", item1.nama)
            intent.putExtra("topik", item1.topik)
            intent.putExtra("bentuk", item1.bentuk)
            intent.putExtra("tinjut", item1.tinjut)
            intent.putExtra("catatan", item1.catatan)
            intent.putExtra("foto", item1.foto)
            intent.putExtra("mode", "editmode")
            context.startActivity(intent)
        }

        Glide.with(holder.itemView.context)
            .load(item1.foto)
            .into(holder.foto)
    }

    private fun deleteJurnal(id: String, position: Int) {
        val prefs = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val user = prefs.getString("username", "") ?: ""

        val queue = Volley.newRequestQueue(context)
        val url = AppConstants.DELETE_JURNALGW

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                if (response.trim() == "success") {
                    list.removeAt(position)
                    notifyItemRemoved(position)
                    Toast.makeText(context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gagal hapus: $response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf(
                    "id" to id,
                    "wali" to user
                )
            }
        }

        queue.add(request)
    }

    override fun getItemCount(): Int = list.size
}
