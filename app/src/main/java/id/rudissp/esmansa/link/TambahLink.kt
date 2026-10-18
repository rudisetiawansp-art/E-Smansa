package id.rudissp.esmansa.link

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityTambahLinkBinding
import org.json.JSONException
import org.json.JSONObject

class TambahLink : AppCompatActivity() {

    private lateinit var binding: ActivityTambahLinkBinding

    private val urlApiTambah = AppConstants.LINK

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityTambahLinkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mode = intent.getStringExtra("mode") ?: ""
        if (mode == "editmode") {
            binding.toolbarTambahLink.title = "Edit Link"
            binding.toolbarTambahLink.menu.findItem(R.id.menunya_update).isVisible = true
            binding.toolbarTambahLink.menu.findItem(R.id.menunya_simpan).isVisible = false

            val judul = intent.getStringExtra("judul") ?: ""
            val link = intent.getStringExtra("link") ?: ""
            val kategori = intent.getStringExtra("kategori") ?: ""
            val platform = intent.getStringExtra("platform") ?: ""

            binding.etJudulLink.setText(judul)
            binding.etUrlLink.setText(link)

            // Tambahkan parameter 'false' di sini
            binding.actvKategori.setText(kategori, false)
            binding.actvPlatform.setText(platform, false)

            // Munculkan kolom platform jika data yang diedit adalah Sosial Media
            if (kategori == "Sosial Media") {
                binding.tilPlatform.visibility = View.VISIBLE
            }

        } else{
            binding.toolbarTambahLink.title = "Tambah Link"
            binding.toolbarTambahLink.menu.findItem(R.id.menunya_update).isVisible = false
            binding.toolbarTambahLink.menu.findItem(R.id.menunya_simpan).isVisible = true

        }

        setupToolbar()
        setupDropdowns()

    }

    private fun setupToolbar() {
        binding.toolbarTambahLink.setNavigationOnClickListener {
            finish()
        }

        binding.toolbarTambahLink.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menunya_simpan -> {

                    simpanDataLink()
                    true
                }

                R.id.menunya_update -> {
                    //updateDataBuku()
                    true
                }

                else -> false
            }
        }
    }

    private fun setupDropdowns() {
        val listKategori = arrayOf("Reguler", "Sosial Media")
        val adapterKategori = ArrayAdapter(this, android.R.layout.simple_list_item_1, listKategori)
        binding.actvKategori.setAdapter(adapterKategori)

        // 2. Setup Dropdown Platform
        val listPlatform = arrayOf("Instagram", "YouTube", "Facebook", "TikTok")
        val adapterPlatform = ArrayAdapter(this, android.R.layout.simple_list_item_1, listPlatform)
        binding.actvPlatform.setAdapter(adapterPlatform)

        // 3. Logika Menampilkan/Menyembunyikan Platform
        binding.actvKategori.setOnItemClickListener { parent, _, position, _ ->
            val pilihanKategori = parent.getItemAtPosition(position).toString()

            if (pilihanKategori == "Sosial Media") {
                binding.tilPlatform.visibility = View.VISIBLE
            } else {
                binding.tilPlatform.visibility = View.GONE
                binding.actvPlatform.text.clear()
            }
        }
    }

    private fun simpanDataLink() {
        val judul = binding.etJudulLink.text.toString().trim()
        val linkUrl = binding.etUrlLink.text.toString().trim()
        val kategori = binding.actvKategori.text.toString()
        val platform = binding.actvPlatform.text.toString()

        // Validasi Form Kosong
        if (judul.isEmpty() || linkUrl.isEmpty() || kategori.isEmpty()) {
            Toast.makeText(this, "Judul, Link, dan Kategori wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        if (kategori == "Sosial Media" && platform.isEmpty()) {
            Toast.makeText(this, "Pilih platform sosial media terlebih dahulu!", Toast.LENGTH_SHORT).show()
            return
        }

        // Mulai Loading
        binding.progressBarTambah.visibility = View.VISIBLE

        // Request POST dengan Volley
        val stringRequest = object : StringRequest(
            Method.POST, urlApiTambah,
            { response ->
                binding.progressBarTambah.visibility = View.GONE

                try {
                    val jsonObject = JSONObject(response)
                    val status = jsonObject.getBoolean("status")
                    val pesan = jsonObject.getString("pesan")

                    if (status) {
                        Toast.makeText(this@TambahLink, "Berhasil: $pesan", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@TambahLink, "Gagal: $pesan", Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this@TambahLink, "Error parsing respons server", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                binding.progressBarTambah.visibility = View.GONE
                Toast.makeText(this@TambahLink, "Error koneksi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["action"] = "tambah"
                params["judul"] = judul
                params["link"] = linkUrl
                params["kategori"] = kategori
                params["platform"] = platform
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }
}