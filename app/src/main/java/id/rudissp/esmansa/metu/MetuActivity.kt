package id.rudissp.esmansa.metu

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.databinding.ActivityMetuBinding
import org.json.JSONArray
import org.json.JSONException

class MetuActivity : AppCompatActivity() {

    lateinit var binding: ActivityMetuBinding
    private lateinit var izinAdapter: MetuAdapter
    private val listIzin = mutableListOf<Metu>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMetuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        izinAdapter = MetuAdapter(
            this, listIzin,
            onEdit = { izin -> editData(izin) },
            onDelete = { izin -> hapusData(izin.id) }
        )

        binding.izinmetuRv.apply {
            layoutManager = LinearLayoutManager(this@MetuActivity)
            adapter = izinAdapter
        }

        // Load data dari server
        ambilDataDariServer()

        binding.tanbahIzinmetu.setOnClickListener {
            startActivity(Intent(this, TambahMetu::class.java))
        }

        binding.izinmetuToolbar.setNavigationOnClickListener {
            finish()
        }

    }

    private fun ambilDataDariServer() {
        val url = AppConstants.GET_METU
        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val jsonArray = JSONArray(response)
                    listIzin.clear()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val izin = Metu(
                            id = obj.getString("id"),
                            nama = obj.getString("nama"),
                            kelas = obj.getString("kelas"),
                            jam1 = obj.getString("jam1"),
                            jam2 = obj.getString("jam2"),
                            guru = obj.getString("guru"),
                            alasan = obj.getString("alasan"),
                            tanggal = obj.getString("tanggal"),
                            foto = obj.getString("foto")
                        )
                        listIzin.add(izin)
                    }
                    izinAdapter.notifyDataSetChanged()

                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Gagal parsing data", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Gagal ambil data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun editData(izin: Metu) {
        val intent = Intent(this, TambahMetu::class.java).apply {
            putExtra("mode", "edit")
            putExtra("izin", izin)
        }
        startActivity(intent)
    }

    private fun hapusData(id: String) {
        val url = AppConstants.HAPUS_METU
        val request = object : StringRequest(
            Method.POST, url,
            {
                Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                ambilDataDariServer()
            },
            {
                Toast.makeText(this, "Gagal hapus: ${it.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf("id" to id)
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

}