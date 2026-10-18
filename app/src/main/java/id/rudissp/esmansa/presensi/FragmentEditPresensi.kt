package id.rudissp.esmansa.presensi

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.databinding.FragmentEditPresensiBinding
import org.json.JSONArray
import java.util.Calendar


class FragmentEditPresensi : Fragment() {

    lateinit var binding: FragmentEditPresensiBinding

    private lateinit var listGukar: List<String>
    private lateinit var listJenisIzin: List<String>

    private val gukarMap = mutableMapOf<String, String>() // nama -> username

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditPresensiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadGukarList()
        listJenisIzin = listOf("DL", "CT")

        // Set adapter untuk dropdown Jenis Izin
        val adapterJenis = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            listJenisIzin
        )
        binding.autojenis.setAdapter(adapterJenis)

        // Tampilkan date picker saat klik tanggal
        binding.pilihtanggal.setOnClickListener {
            showDatePicker()
        }


        // Simpan data saat klik tombol
        binding.simpanCuti.setOnClickListener {
            val namaGukar = binding.autogukar.text.toString()
            val tanggal = binding.pilihtanggal.text.toString()
            val jenis = binding.autojenis.text.toString()
            val username = gukarMap[namaGukar] ?: ""

            if (username.isBlank() || tanggal.isBlank() || jenis.isBlank()) {
                Toast.makeText(requireContext(), "Lengkapi semua data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            kirimDataIzin(username, tanggal, jenis)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.pilihtanggal.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }


    private fun loadGukarList() {
        val request = StringRequest(
            Request.Method.GET, AppConstants.GET_GUKAR_GTTPTT,
            { response ->
                val jsonArray = JSONArray(response)
                val listNama = mutableListOf<String>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val nama = obj.getString("nama")
                    val username = obj.getString("username")

                    listNama.add(nama)
                    gukarMap[nama] = username
                }

                listGukar = listNama
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    listNama
                )
                binding.autogukar.setAdapter(adapter)
            },
            { error ->
                Toast.makeText(requireContext(), "Gagal memuat data Gukar", Toast.LENGTH_SHORT)
                    .show()
                Log.e("Volley", "Error: ${error.message}")
            })

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun kirimDataIzin(username: String, tanggal: String, jenis: String) {
        val request = object : StringRequest(
            Method.POST, AppConstants.SET_STATUS_PRESENSI,
            { response ->
                Toast.makeText(requireContext(), "Status berhasil disimpan", Toast.LENGTH_SHORT)
                    .show()
            },
            { error ->
                Toast.makeText(
                    requireContext(),
                    "Gagal menyimpan: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("VOLLEY", "Error: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["username"] = username
                params["tanggal"] = tanggal
                params["jenis"] = jenis
                return params
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }


}