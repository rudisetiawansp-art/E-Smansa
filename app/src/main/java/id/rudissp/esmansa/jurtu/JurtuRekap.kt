package id.rudissp.esmansa.jurtu

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.databinding.ActivityJurtuRekapBinding
import org.json.JSONArray
import java.util.Calendar
import java.util.Date
import java.util.Locale

class JurtuRekap : AppCompatActivity() {
    private lateinit var binding: ActivityJurtuRekapBinding
    private val rekapList = ArrayList<RekapPegawaiModel>()
    private lateinit var rekapAdapter: RekapAdapter

    // Variabel untuk menyimpan tanggal yang akan dikirim ke API
    private var tanggalUntukAPI: String = ""
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityJurtuRekapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar Action (Tombol Kembali)
        binding.toolbarnyaJurtuRekap.setNavigationOnClickListener { finish() }

        // Setup RecyclerView
        rekapAdapter = RekapAdapter(rekapList)
        binding.recyclerViewRekap.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRekap.adapter = rekapAdapter

        // 1. Inisialisasi Default (Tanggal Hari Ini)
        setTanggalKeLayar(calendar.time)

        // Load data pertama kali saat activity dibuka
        getRekapHarian(tanggalUntukAPI)

        // 2. Setup Aksi Klik Pilih Tanggal
        binding.cvPilihTanggal.setOnClickListener {
            tampilkanDatePicker()
        }
    }

    private fun tampilkanDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Saat user memilih tanggal baru, update calendar kita
                calendar.set(year, month, dayOfMonth)

                // Ubah tampilan teks di layar dan update variabel API
                setTanggalKeLayar(calendar.time)

                // Panggil ulang Volley dengan tanggal yang baru dipilih
                getRekapHarian(tanggalUntukAPI)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun setTanggalKeLayar(date: Date) {
        // Format untuk API (MySQL) -> 2026-03-29
        val formatAPI = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        tanggalUntukAPI = formatAPI.format(date)

        // Format untuk Tampilan di Layar UI -> 29 Maret 2026
        val formatLayar = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        binding.tvTanggalTampil.text = formatLayar.format(date)
    }

    private fun getRekapHarian(tanggalYangDipilih: String) {
        // Gabungkan URL dengan parameter tanggal
        val url = AppConstants.GET_JURTU_HARIAN + "?tanggal=$tanggalYangDipilih"

        binding.progressBarRekap.visibility = View.VISIBLE
        rekapList.clear()

        val request = StringRequest(Request.Method.GET, url, { response ->
            binding.progressBarRekap.visibility = View.GONE

            try {
                val dataArray = JSONArray(response)

                for (i in 0 until dataArray.length()) {
                    val itemPegawai = dataArray.getJSONObject(i)

                    // Parse Daftar Kegiatan Jurnal
                    val kegiatanArray = itemPegawai.getJSONArray("kegiatan")
                    val daftarKegiatanJurnal = ArrayList<JurnalItemModel>()

                    for (j in 0 until kegiatanArray.length()) {
                        val itemJurnal = kegiatanArray.getJSONObject(j)
                        daftarKegiatanJurnal.add(
                            JurnalItemModel(
                                itemJurnal.getString("id"),
                                itemJurnal.getString("jam"),
                                itemJurnal.getString("deskripsi"),
                                itemJurnal.getString("foto")
                            )
                        )
                    }

                    // Tambahkan Pegawai dengan daftar kegiatannya
                    rekapList.add(
                        RekapPegawaiModel(
                            itemPegawai.getString("nama"),
                            itemPegawai.getString("nip"),
                            daftarKegiatanJurnal
                        )
                    )
                }

                // Refresh Adapter agar layar diperbarui
                rekapAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error parsing data rekap", Toast.LENGTH_SHORT).show()
            }

        }, {
            binding.progressBarRekap.visibility = View.GONE
            Toast.makeText(this, "Gagal memuat rekap harian", Toast.LENGTH_SHORT).show()
        })

        Volley.newRequestQueue(this).add(request)
    }
}