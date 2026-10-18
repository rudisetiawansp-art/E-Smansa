package id.rudissp.esmansa.pray

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.databinding.FragmentPraysiswaBinding
import id.rudissp.esmansa.databinding.ItemPraySiswaBinding
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt

class PraySiswa : Fragment() {

    private var _binding: FragmentPraysiswaBinding? = null
    private val binding get() = _binding!!
    private lateinit var requestQueue: RequestQueue
    private val baseUrl = "https://sman1sukorejo.sch.id/smania/ibadah/"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPraysiswaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestQueue = Volley.newRequestQueue(requireContext())
        binding.rvAdminSiswa.layoutManager = LinearLayoutManager(requireContext())

        // Set Tanggal Default Hari Ini
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.etTanggalAdmin.setText(sdf.format(Date()))

        // Event Klik Tanggal
        binding.etTanggalAdmin.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                val tgl = "$y-${String.format("%02d", m + 1)}-${String.format("%02d", d)}"
                binding.etTanggalAdmin.setText(tgl)
                val kls = binding.acKelasAdmin.text.toString()
                if (kls.isNotEmpty()) muatDaftarSiswa(kls)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Event Pilih Kelas
        binding.acKelasAdmin.setOnItemClickListener { parent, _, position, _ ->
            val kelasTerpilih = parent.getItemAtPosition(position).toString()
            muatDaftarSiswa(kelasTerpilih)
        }
    }

    private fun muatDaftarSiswa(kelas: String) {
        val tgl = binding.etTanggalAdmin.text.toString()
        val url = "${baseUrl}api_siswa_per_kelas.php?kelas=$kelas&tanggal=$tgl"

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                val listSiswa = mutableListOf<SiswaAdminModel>()
                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    listSiswa.add(SiswaAdminModel(
                        obj.getString("nis"),
                        obj.getString("nama"),
                        obj.optString("device_id", null),
                        obj.getBoolean("is_halangan")
                    ))
                }
                binding.rvAdminSiswa.adapter = AdminSiswaAdapter(listSiswa) { action, nis, checked ->
                    eksekusiAksi(action, nis, checked)
                }
            },
            { Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show() }
        )
        requestQueue.add(request)
    }

    private fun eksekusiAksi(action: String, nis: String, checked: Boolean?) {
        val url = "${baseUrl}api_admin_action.php"
        val request = object : StringRequest(Method.POST, url,
            { response ->
                val json = JSONObject(response)
                Toast.makeText(context, json.getString("message"), Toast.LENGTH_SHORT).show()
            },
            { Toast.makeText(context, "Koneksi Gagal", Toast.LENGTH_SHORT).show() }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["action"] = action
                params["nis"] = nis
                params["tanggal"] = binding.etTanggalAdmin.text.toString()
                if (checked != null) params["status"] = checked.toString()
                return params
            }
        }
        requestQueue.add(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class SiswaAdminModel(
    val nis: String,
    val nama: String,
    val device_id: String?,
    val is_halangan: Boolean
)

class AdminSiswaAdapter(
    private val listSiswa: List<SiswaAdminModel>,
    private val onAction: (action: String, nis: String, isChecked: Boolean?) -> Unit
) : RecyclerView.Adapter<AdminSiswaAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPraySiswaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPraySiswaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val siswa = listSiswa[position]

        holder.binding.apply {
            tvNamaSiswa.text = siswa.nama
            tvNisSiswa.text = "NIS: ${siswa.nis}"

            cbHalangan.setOnCheckedChangeListener(null)
            cbHalangan.isChecked = siswa.is_halangan


            cbHalangan.setOnCheckedChangeListener { _, isChecked ->
                onAction("tambah_halangan", siswa.nis, isChecked)
            }

            btnResetDevice.setOnClickListener {
                onAction("reset_device", siswa.nis, null)
            }

//            if (siswa.device_id.isNullOrEmpty()) {
//                tvNisSiswa.append(" • Akun Terbuka")
//                tvNisSiswa.setTextColor("#2E7D32".toColorInt())
//            } else {
//                tvNisSiswa.append(" • Terkunci HP")
//                tvNisSiswa.setTextColor(Color.GRAY)
//            }
        }
    }

    override fun getItemCount() = listSiswa.size
}