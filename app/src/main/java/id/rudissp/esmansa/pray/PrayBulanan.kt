package id.rudissp.esmansa.pray

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.databinding.FragmentPrayHarianBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PrayBulanan : Fragment(){

    private var _binding: FragmentPrayHarianBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: SharedPreferences

    private var siswaList = mutableListOf<SiswaModel>()
    private lateinit var requestQueue: RequestQueue


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrayHarianBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = requireActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        requestQueue = Volley.newRequestQueue(requireContext())
        binding.rvPrayHarian.layoutManager = LinearLayoutManager(requireContext())

        // Klik Tanggal
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.pilihtanggal.setOnClickListener { tampilkanKalender() }
        binding.pilihtanggal.setText(sdf.format(Date()))

        // Pilih Kelas
        binding.kelasnya.setOnItemClickListener { _, _, _, _ -> muatData() }


    }

    private fun tampilkanKalender() {
        val c = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            val tgl = "$year-${String.format("%02d", month + 1)}-${String.format("%02d", day)}"
            binding.pilihtanggal.setText(tgl)
            muatData()
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun muatData() {
        val tgl = binding.pilihtanggal.text.toString()
        val kls = binding.kelasnya.text.toString()

        if (tgl.isEmpty() || kls.isEmpty()) return

        val url = "https://sman1sukorejo.sch.id/ibadah/api_rekap_harian.php?tanggal=$tgl&kelas=$kls"

        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                siswaList.clear()
                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    siswaList.add(
                        SiswaModel(
                            obj.getString("nama"),
                            obj.getString("waktu"),
                            obj.getString("keterangan")
                        )
                    )
                }
                binding.rvPrayHarian.adapter = RekapAdapter(siswaList)
            },
            { Toast.makeText(context, "Koneksi Gagal", Toast.LENGTH_SHORT).show() }
        )
        requestQueue.add(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Mencegah memory leak
    }
}


