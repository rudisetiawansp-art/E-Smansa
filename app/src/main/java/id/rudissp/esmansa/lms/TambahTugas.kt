package id.rudissp.esmansa.lms

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityTambahTugasBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TambahTugas : AppCompatActivity() {

    private lateinit var binding: ActivityTambahTugasBinding

    private lateinit var prefs: SharedPreferences

    // Variabel penyimpan data
    private var selectedDeadline: Calendar? = null
    private var selectedFileUri: Uri? = null
    private var cameraImageUri: Uri? = null
    private var cameraFileName: String = ""

    private var namaGuruLogin: String = ""

    // Setup Adapter Kelas
    private val listKelas = mutableListOf<KelasModel>()
    private lateinit var kelasAdapter: KelasCheckboxAdapter

    // Launcher untuk melampirkan file PDF/Gambar
    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                selectedFileUri = it
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                val fileName = getFileName(it)
                binding.tvNamaFile.text = fileName
            }
        }

    // Launcher BARU untuk Kamera
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                selectedFileUri = cameraImageUri
                binding.tvNamaFile.text = cameraFileName
            } else {
                cameraImageUri = null
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        binding = ActivityTambahTugasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.toolbar.menu.findItem(R.id.menunya_update).isVisible = false

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menunya_simpan -> {
                    validateAndSubmit()
                    true
                }

                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        // Inisialisasi adapter dengan list kosong terlebih dahulu
        kelasAdapter = KelasCheckboxAdapter(listKelas)
        binding.rvKelas.layoutManager = LinearLayoutManager(this)
        binding.rvKelas.adapter = kelasAdapter

        // Ambil data kelas dari database
        fetchKelasDariDatabase()
    }

    private fun fetchKelasDariDatabase() {
        namaGuruLogin = prefs.getString("namalengkap", "").toString()


        val urlApi = AppConstants.GETKELAS

        val stringRequest = object : StringRequest(
            Method.POST, urlApi,
            Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getString("status") == "success") {
                        val jsonArray = jsonObject.getJSONArray("data")

                        listKelas.clear()

                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            val idKelas = item.getString("id")
                            val namaKelas = item.getString("namaKelas")
                            listKelas.add(KelasModel(idKelas, namaKelas))
                        }

                        kelasAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this, "Tidak ada kelas", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { _ ->
                Toast.makeText(this, "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["nama_guru"] = namaGuruLogin
                return params
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)

    }

    private fun setupListeners() {
        // PERUBAHAN: Menampilkan Dialog Pilihan saat tombol diklik
        binding.btnLampirkan.setOnClickListener {
            val options = arrayOf("Ambil Foto (Kamera)", "Pilih File (Dokumen/Galeri)")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pilih Lampiran")
            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera() // Pilihan Kamera
                    1 -> {
                        // Pilihan File Manager
                        val mimeTypes = arrayOf("application/pdf", "image/*")
                        filePickerLauncher.launch(mimeTypes)
                    }
                }
            }
            builder.show()
        }

        binding.cbPilihSemua.setOnCheckedChangeListener { _, isChecked ->
            kelasAdapter.selectAll(isChecked)
        }

        binding.pilihtanggalLms.setOnClickListener {
            showDateTimePicker()
        }
    }

    private fun openCamera() {
        try {
            // Membuat nama file yang unik berdasarkan waktu
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
                Date()
            )
            cameraFileName = "FOTO_$timeStamp.jpg"

            // Membuat file sementara di direktori aplikasi
            val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val photoFile = File.createTempFile("FOTO_${timeStamp}_", ".jpg", storageDir)

            // Mendapatkan URI yang aman melalui FileProvider
            cameraImageUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                photoFile
            )


            // Membuka aplikasi kamera bawaan HP
            cameraImageUri?.let { takePictureLauncher.launch(it) }

        } catch (e: Exception) {
            Toast.makeText(this, "Gagal membuka kamera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDateTimePicker() {
        val currentCalendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                TimePickerDialog(
                    this,
                    { _, hour, minute ->

                        selectedDeadline = Calendar.getInstance()
                        selectedDeadline?.set(year, month, day, hour, minute)

                        // Menampilkan format: 25 Okt 2026, 23:59
                        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        binding.pilihtanggalLms.setText(sdf.format(selectedDeadline!!.time))

                    },
                    currentCalendar.get(Calendar.HOUR_OF_DAY),
                    currentCalendar.get(Calendar.MINUTE),
                    true
                ).show()

            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun validateAndSubmit() {
        val mapel = binding.mapelnya.text.toString().trim()
        val judul = binding.tlmsJudul.text.toString().trim()
        val deskripsi = binding.tlmsDeskripsi.text.toString().trim()
        val isOnline = binding.switchOnline.isChecked
        val kelasTerpilih = listKelas.filter { it.isSelected }

        val jenisTugas = binding.jenisnya.text.toString().trim()

        // Validasi
        if (mapel.isEmpty()) {
            binding.mapelnya.error = "Pilih mapel"; return
        }
        if (judul.isEmpty()) {
            binding.tlmsJudul.error = "Isi judul"; return
        }
        if (jenisTugas.isEmpty()) {
            binding.jenisnya.error = "Pilih jenis"; return
        }
        if (kelasTerpilih.isEmpty()) {
            Toast.makeText(this, "Pilih kelas", Toast.LENGTH_SHORT).show(); return
        }
        if (selectedDeadline == null) {
            Toast.makeText(this, "Tentukan deadline", Toast.LENGTH_SHORT).show(); return
        }

        val sdfDb = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val deadlineString = sdfDb.format(selectedDeadline!!.time)

        // Ubah list id kelas menjadi JSON Array String (agar mudah ditangkap PHP)
        val kelasArray = JSONArray()
        kelasTerpilih.forEach { kelasArray.put(it.id) }
        val kelasJsonString = kelasArray.toString()

        val urlApi = AppConstants.SIMPAN_TUGAS

        // Tampilkan loading (opsional)
        Toast.makeText(this, "Menyimpan data...", Toast.LENGTH_SHORT).show()

        val multipartRequest = object : VolleyMultipartRequest(
            Method.POST, urlApi,
            Response.Listener { response ->
                val resultResponse = String(response.data)
                try {
                    val jsonObject = JSONObject(resultResponse)
                    if (jsonObject.getString("status") == "success") {
                        Toast.makeText(
                            this@TambahTugas,
                            "Tugas Berhasil Disimpan!",
                            Toast.LENGTH_LONG
                        ).show()
                        finish() // Tutup activity setelah berhasil
                    } else {
                        Toast.makeText(
                            this@TambahTugas,
                            "Gagal: ${jsonObject.getString("message")}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@TambahTugas, "Response error", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this@TambahTugas, "Network Error", Toast.LENGTH_SHORT).show()
            }) {

            // 1. Mengirim Data Teks
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["jenis"] = jenisTugas
                params["mapel"] = mapel
                params["judul"] = judul
                params["deskripsi"] = deskripsi
                params["is_online"] = if (isOnline) "1" else "0"
                params["deadline"] = deadlineString
                params["nama_guru"] = namaGuruLogin
                params["kelas_array"] = kelasJsonString
                return params
            }

            // 2. Mengirim File (Jika ada)
            override fun getByteData(): Map<String, DataPart>? {
                val params = HashMap<String, DataPart>()
                if (selectedFileUri != null) {
                    try {
                        val inputStream: InputStream? =
                            contentResolver.openInputStream(selectedFileUri!!)
                        val fileBytes = inputStream?.readBytes()
                        if (fileBytes != null) {
                            val fileName = binding.tvNamaFile.text.toString()
                            params["file_lampiran"] = DataPart(
                                fileName,
                                fileBytes,
                                contentResolver.getType(selectedFileUri!!)
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return params
            }
        }

        Volley.newRequestQueue(this).add(multipartRequest)
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = it.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "File_Terlampir"
    }


}

// ==========================================
// DATA CLASS & ADAPTER
// ==========================================

data class KelasModel(
    val id: String,
    val namaKelas: String,
    var isSelected: Boolean = false
)

class KelasCheckboxAdapter(private val list: List<KelasModel>) :
    RecyclerView.Adapter<KelasCheckboxAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        // PERBAIKAN: Ubah CheckBox menjadi CheckedTextView
        val cb: CheckedTextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_multiple_choice, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.cb.text = item.namaKelas
        holder.cb.isChecked = item.isSelected

        // Memastikan state tersimpan saat baris diklik dan UI langsung diperbarui
        holder.cb.setOnClickListener {
            item.isSelected = !item.isSelected
            holder.cb.isChecked = item.isSelected // Update centang di layar
        }
    }

    override fun getItemCount(): Int = list.size

    fun selectAll(select: Boolean) {
        for (item in list) {
            item.isSelected = select
        }
        notifyDataSetChanged()
    }
}