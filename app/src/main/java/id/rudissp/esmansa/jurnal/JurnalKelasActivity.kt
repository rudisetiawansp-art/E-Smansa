package id.rudissp.esmansa.jurnal

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityJurnalKelasBinding
import org.json.JSONArray
import java.util.Calendar

class JurnalKelasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJurnalKelasBinding
    private val jurnalItems = mutableListOf<JurnalItemKelas>()

    //private val tglAwal = "2025-05-01"
    //private val tglAkhir = "2025-05-31"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityJurnalKelasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar back button
        binding.toolbarnyaKelas.setNavigationOnClickListener { finish() }

        // Inisialisasi RecyclerView
        binding.recJurnalkelas.layoutManager = LinearLayoutManager(this)
        binding.recJurnalkelas.adapter = JurnalAdapterGabungan(jurnalItems)

        binding.filKelas.setOnItemClickListener { parent, view, position, id ->
            //val kelasDipilih = parent.getItemAtPosition(position) as String
            //Toast.makeText(this, "Kelas dipilih: $kelasDipilih", Toast.LENGTH_SHORT).show()
            //Log.d("JurnalKelas", "Load jurnal untuk kelas: $kelasDipilih")

        }

        binding.darik.setOnClickListener {
            showDatePicker()
        }

        binding.sampaik.setOnClickListener {
            showDatePickersampai()
        }

        binding.cariJurnalkelas.setOnClickListener {
            val kelasDipilih = binding.filKelas.text.toString()
            val tglAwal1 = binding.darik.text.toString()
            val tglAkhir1 = binding.sampaik.text.toString()
            loadJurnalGrouped(kelasDipilih, tglAwal1, tglAkhir1)
        }
    }

    private fun loadJurnalGrouped(kelasDipilih: String, tglAwal1: String, tglAkhir1: String) {
        val url = AppConstants.GET_JURNAL_KELAS
        jurnalItems.clear()

        val request = object : StringRequest(Method.POST, url,
            { response ->
                Log.d("JurnalResponse", response)
                try {
                    val jsonArray = JSONArray(response)

                    val mapTanggal = linkedMapOf<String, MutableList<JurnalItemKelas.Detail>>()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)

                        val tanggal = obj.getString("tanggal")
                        val detail = JurnalItemKelas.Detail(
                            jamke1 = obj.getString("jamke1"),
                            jamke2 = obj.getString("jamke2"),
                            nama = obj.getString("nama"),
                            mapel = obj.getString("mapel"),
                            materi = obj.getString("materi")
                        )

                        if (mapTanggal.containsKey(tanggal)) {
                            mapTanggal[tanggal]?.add(detail)
                        } else {
                            mapTanggal[tanggal] = mutableListOf(detail)
                        }
                    }

                    // Isi ulang list dengan header & detail
                    for ((tanggal, details) in mapTanggal) {
                        jurnalItems.add(JurnalItemKelas.Header(tanggal))
                        jurnalItems.addAll(details)
                    }

                    binding.recJurnalkelas.adapter?.notifyDataSetChanged()

                } catch (e: Exception) {
                    Toast.makeText(this, "Parsing error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Request error: ${error.message}", Toast.LENGTH_LONG).show()
                Log.e("JurnalError", error.toString())
            }
        ) {

            override fun getParams(): Map<String, String> = mapOf(
                "kelas" to kelasDipilih,
                "tgl_awal" to tglAwal1,
                "tgl_akhir" to tglAkhir1
            )
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.darik.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showDatePickersampai() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.sampaik.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }
}
