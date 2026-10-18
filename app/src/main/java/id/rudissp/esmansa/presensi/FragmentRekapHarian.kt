package id.rudissp.esmansa.presensi

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.databinding.FragmentRekapHarianBinding
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class FragmentRekapHarian : Fragment() {

    private lateinit var binding: FragmentRekapHarianBinding
    private lateinit var adapter: RekapHarianAdapter
    private val rekapList = mutableListOf<RekapHarianModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRekapHarianBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RekapHarianAdapter(rekapList)
        binding.rvRkpPresH.adapter = adapter
        binding.rvRkpPresH.layoutManager = LinearLayoutManager(requireContext())

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        binding.pilihtanggal.setText(currentDate)
        loadRekapPresensi(currentDate)

        binding.pilihtanggal.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                binding.pilihtanggal.setText(selectedDate)
                loadRekapPresensi(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun loadRekapPresensi(tanggal: String) {
        val url = "${AppConstants.REKAP_HARIAN}?tanggal=$tanggal"
        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonArray = JSONArray(response)
                    rekapList.clear()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val tgl = obj.optString("tanggal", "")
                        val nama = obj.getString("nama")
                        val masuk = obj.optString("masuk", "-")
                        val pulang = obj.optString("pulang", "-")
                        val jenis = obj.optString("jenis", "-")
                        val shift = obj.optString("shift")

                        // --- BAGIAN MENGHILANGKAN DETIK ---
                        // Jika data berisi jam (ada titik dua), ambil 5 karakter pertama (HH:mm)
                        val masuk1 = if (masuk.length > 5 && masuk.contains(":"))
                            masuk.substring(0, 5) else masuk
                        val pulang1 = if (pulang.length > 5 && pulang.contains(":"))
                            pulang.substring(0, 5) else pulang
                        // ----------------------------------

                        val hasilKWK = hitungKekurangan(tgl, masuk, pulang)

                        rekapList.add(
                            RekapHarianModel(
                                tanggal = nama, // nama ditampilkan di kolom tanggal
                                masuk = masuk1,
                                pulang = pulang1,
                                jenis = jenis,
                                shift = shift,
                                kurang = hasilKWK
                            )
                        )
                    }
                    adapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Gagal parsing data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
            })

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun hitungKekurangan(tgl: String, masuk: String, pulang: String): String {
        if (tgl.isEmpty()) return "0"

        val sdfTanggal = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        try {
            val dateObj = sdfTanggal.parse(tgl) ?: return "0"
            val cal = Calendar.getInstance().apply { time = dateObj }
            val hariKe = cal.get(Calendar.DAY_OF_WEEK)

            // 1. Tentukan Batas Waktu Berdasarkan Hari
            val batasMasuk = "07:00"
            val batasPulang = if (hariKe == Calendar.FRIDAY) "14:00" else "15:30"

            // Standar jam kerja harian untuk kasus absen kosong (seharian)
            val standarMenit = if (hariKe == Calendar.FRIDAY) 420 else 510 // 7j atau 8.5j

            // 2. Logika: Belum absen seharian
            if (masuk == "-" && pulang == "-") {
                val h = standarMenit / 60
                val m = standarMenit % 60
                return if (m > 0) "${h}j ${m}m" else "${h}j"
            }

            // 3. Logika: Hanya salah satu yang absen (Otomatis 4 jam sesuai permintaan sebelumnya)
            if (masuk == "-" || pulang == "-") return "4j"

            // 4. Logika: Jika status OFF
            if (masuk.contains("OFF", true)) return "0"

            // Helper untuk konversi jam ke menit
            fun keMenit(jamStr: String): Int {
                val parts = jamStr.split(":")
                val h = parts[0].trim().toIntOrNull() ?: 0
                val m = parts[1].trim().toIntOrNull() ?: 0
                return (h * 60) + m
            }

            val menitMasuk = keMenit(masuk)
            val menitPulang = keMenit(pulang)
            val menitBatasMasuk = keMenit(batasMasuk)
            val menitBatasPulang = keMenit(batasPulang)

            var totalKWK = 0

            // 5. Perhitungan Keterlambatan (Masuk > 07:00)
            if (menitMasuk > menitBatasMasuk) {
                totalKWK += (menitMasuk - menitBatasMasuk)
            }

            // 6. Perhitungan Pulang Cepat (Pulang < Batas Pulang)
            if (menitPulang < menitBatasPulang) {
                totalKWK += (menitBatasPulang - menitPulang)
            }

            // 7. Format Output
            return when {
                totalKWK <= 0 -> "-"
                totalKWK >= 60 -> {
                    val h = totalKWK / 60
                    val m = totalKWK % 60
                    if (m > 0) "${h}j ${m}m" else "${h}j"
                }
                else -> "${totalKWK}m"
            }

        } catch (e: Exception) {
            return "0"
        }
    }

}


