package id.rudissp.esmansa.presensi.siswa

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.databinding.FragmentPresensiSiswaHariBinding
import org.json.JSONArray
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PresensiSiswaKelas : Fragment() {

    private lateinit var binding: FragmentPresensiSiswaHariBinding
    private lateinit var adapter: PresensiSiswaHarianAdapter
    private val dataList = mutableListOf<PresensiSiswaHarian>()

    private val API_URL = AppConstants.SMANIA_URL + "android/presensi_rekap_harian.php"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPresensiSiswaHariBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        adapter = PresensiSiswaHarianAdapter(dataList)
        binding.rvPresiswahar.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPresiswahar.adapter = adapter

        // Set tanggal default = hari ini
        val today =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        binding.presiswaharPilihtanggal.setText(today)

        // Event klik untuk memilih tanggal
        binding.presiswaharPilihtanggal.setOnClickListener {
            showDatePicker()
        }

        binding.presiswaharKelas.setOnItemClickListener { _, _, _, _ ->
            loadData()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val tanggal =
                    String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.presiswaharPilihtanggal.setText(tanggal)
                loadData()
            },
            year, month, day
        ).show()
    }

    private fun loadData() {
        val kelas = binding.presiswaharKelas.text.toString().trim()
        val tanggal = binding.presiswaharPilihtanggal.text.toString().trim()

        if (kelas.isEmpty()) {
            Toast.makeText(requireContext(), "Kelas tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }
        if (tanggal.isEmpty()) {
            Toast.makeText(requireContext(), "Tanggal tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan loading (misal ProgressBar)
        // binding.progressBar.visibility = View.VISIBLE

        val requestQueue = Volley.newRequestQueue(requireContext())

        // Gunakan StringRequest agar kita bisa melihat raw response
        val stringRequest = object : StringRequest(
            Request.Method.POST,
            API_URL,
            { response ->
                // Sembunyikan loading
                // binding.progressBar.visibility = View.GONE

                // Log response mentah untuk debugging
                Log.d("PresensiHarian", "Response: $response")

                try {
                    // Coba parse response sebagai JSONArray
                    val jsonArray = JSONArray(response)
                    val list = mutableListOf<PresensiSiswaHarian>()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val item = PresensiSiswaHarian(
                            nis = obj.getString("nis"),
                            nama = obj.getString("nama"),
                            masuk = obj.getString("masuk"),
                            pulang = obj.getString("pulang"),
                            status = obj.getString("status")
                        )
                        list.add(item)
                    }

                    adapter.updateData(list)

                    if (list.isEmpty()) {
                        Toast.makeText(requireContext(), "Tidak ada data siswa", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: JSONException) {
                    // Jika gagal parse, tampilkan response mentah di log dan toast
                    Log.e("PresensiHarian", "Error parsing JSON: ${e.message}")
                    Log.e("PresensiHarian", "Raw response: $response")
                    Toast.makeText(
                        requireContext(),
                        "Error parsing data. Periksa log.",
                        Toast.LENGTH_LONG
                    ).show()
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e("PresensiHarian", "Volley error: ${error.message}")
                if (error.networkResponse != null) {
                    val statusCode = error.networkResponse.statusCode
                    val responseData = String(error.networkResponse.data)
                    Log.e("PresensiHarian", "Status code: $statusCode, Response: $responseData")
                    Toast.makeText(
                        requireContext(),
                        "Server error $statusCode: $responseData",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal memuat data: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                error.printStackTrace()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "tanggal" to tanggal,
                    "kelas" to kelas
                )
            }

        }

        stringRequest.retryPolicy = DefaultRetryPolicy(
            10000,
            0,
            1f
        )

        requestQueue.add(stringRequest)
    }

}

