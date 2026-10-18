package id.rudissp.esmansa.presensi

import android.R
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.databinding.FragmentEditPresensiBinding
import id.rudissp.esmansa.databinding.FragmentHbBinding
import org.json.JSONArray
import java.util.Calendar


class FragmentHB : Fragment() {

    lateinit var binding: FragmentHbBinding
    private lateinit var adapter: HBAdapter
    private val liburList = mutableListOf<HariLibur>()


    private val gukarMap = mutableMapOf<String, String>() // nama -> username

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHbBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HBAdapter(liburList) { libur ->
            konfirmasiHapus(libur)
        }

        binding.rvHb.layoutManager = LinearLayoutManager(requireContext())

        binding.rvHb.adapter = adapter
        loadHariLibur()


        binding.pilihtanggallibur.setOnClickListener {
            showDatePickerlibur()
        }

        binding.simpanLibur.setOnClickListener {
            simpanHariLibur()
        }

    }

    private fun showDatePickerlibur() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.pilihtanggallibur.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }


    private fun simpanHariLibur() {
        val tanggal = binding.pilihtanggallibur.text.toString()
        val keterangan = binding.ketLibur.text.toString()

        if (tanggal.isEmpty() || keterangan.isEmpty()) {
            Toast.makeText(requireContext(), "Tanggal dan keterangan wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val url = AppConstants.TAMBAH_LIBUR
        val stringRequest = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                if (response == "success") {
                    loadHariLibur()
                    Toast.makeText(requireContext(), "Hari libur disimpan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal menyimpan", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(requireContext(), "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                return mapOf("tanggal" to tanggal, "keterangan" to keterangan)
            }
        }

        Volley.newRequestQueue(requireContext()).add(stringRequest)
    }

    private fun loadHariLibur() {
        val request = StringRequest(
            Request.Method.GET,
            AppConstants.GET_LIBUR,
            { response ->
                val jsonArray = JSONArray(response)
                liburList.clear()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    liburList.add(
                        HariLibur(
                            tanggal = obj.getString("tanggal"),
                            keterangan = obj.getString("keterangan")
                        )
                    )
                }
                adapter.notifyDataSetChanged()
            },
            { error -> Toast.makeText(context, "Gagal ambil data", Toast.LENGTH_SHORT).show() }
        )
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun konfirmasiHapus(libur: HariLibur) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Hari Libur")
            .setMessage("Yakin ingin menghapus hari libur ${libur.tanggal}?")
            .setPositiveButton("Hapus") { _, _ -> hapusHariLibur(libur) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusHariLibur(libur: HariLibur) {
        val request = object : StringRequest(
            Method.POST,
            AppConstants.HAPUS_LIBUR,
            {
                Toast.makeText(context, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                loadHariLibur()
            },
            { error ->
                Toast.makeText(context, "Gagal menghapus", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf("tanggal" to libur.tanggal)
            }
        }
        Volley.newRequestQueue(requireContext()).add(request)
    }


}
data class HariLibur(
    val tanggal: String,
    val keterangan: String
)