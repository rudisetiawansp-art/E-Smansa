package id.rudissp.esmansa.lms

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.AppTheme
import id.rudissp.esmansa.LoadingIndicator
import id.rudissp.esmansa.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TambahTugas : ComponentActivity() {

    private lateinit var prefs: SharedPreferences
    private var selectedFileUri: Uri? = null
    private var cameraImageUri: Uri? = null
    private var cameraFileName: String = ""
    private var namaGuruLogin: String = ""

    private val listKelasState = mutableStateListOf<KelasModel>()
    private var isSavingState = mutableStateOf(false)

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                selectedFileUri = it
                try {
                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {}
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (!isSuccess) {
                cameraImageUri = null
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        namaGuruLogin = prefs.getString("namalengkap", "") ?: ""

        fetchKelasDariDatabase()

        setContent {
            AppTheme {
                TambahTugasScreen(
                    listKelas = listKelasState,
                    isSaving = isSavingState.value,
                    onBackClick = { finish() },
                    onSelectAllKelas = { select ->
                        for (i in listKelasState.indices) {
                            listKelasState[i] = listKelasState[i].copy(isSelected = select)
                        }
                    },
                    onToggleKelas = { index ->
                        val item = listKelasState[index]
                        listKelasState[index] = item.copy(isSelected = !item.isSelected)
                    },
                    onPickFile = {
                        val options = arrayOf("Ambil Foto (Kamera)", "Pilih File (Dokumen/Galeri)")
                        AlertDialog.Builder(this)
                            .setTitle("Pilih Lampiran")
                            .setItems(options) { _, which ->
                                when (which) {
                                    0 -> openCamera()
                                    1 -> filePickerLauncher.launch(arrayOf("application/pdf", "image/*"))
                                }
                            }
                            .show()
                    },
                    onSubmit = { mapel, jenis, judul, deskripsi, isOnline, deadlineCal ->
                        validateAndSubmit(mapel, jenis, judul, deskripsi, isOnline, deadlineCal)
                    }
                )
            }
        }
    }

    private fun openCamera() {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            cameraFileName = "FOTO_$timeStamp.jpg"
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val photoFile = File.createTempFile("FOTO_${timeStamp}_", ".jpg", storageDir)

            cameraImageUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                photoFile
            )
            cameraImageUri?.let {
                selectedFileUri = it
                takePictureLauncher.launch(it)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal membuka kamera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchKelasDariDatabase() {
        val urlApi = AppConstants.GETKELAS

        val stringRequest = object : StringRequest(
            Method.POST, urlApi,
            Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getString("status") == "success") {
                        val jsonArray = jsonObject.getJSONArray("data")
                        listKelasState.clear()

                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            listKelasState.add(KelasModel(item.getString("id"), item.getString("namaKelas")))
                        }
                    } else {
                        Toast.makeText(this, "Tidak ada kelas", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("nama_guru" to namaGuruLogin)
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun validateAndSubmit(
        mapel: String,
        jenis: String,
        judul: String,
        deskripsi: String,
        isOnline: Boolean,
        deadlineCal: Calendar?
    ) {
        if (mapel.isEmpty()) {
            Toast.makeText(this, "Pilih mapel", Toast.LENGTH_SHORT).show()
            return
        }
        if (judul.isEmpty()) {
            Toast.makeText(this, "Isi judul tugas/materi", Toast.LENGTH_SHORT).show()
            return
        }
        if (jenis.isEmpty()) {
            Toast.makeText(this, "Pilih jenis", Toast.LENGTH_SHORT).show()
            return
        }
        val kelasTerpilih = listKelasState.filter { it.isSelected }
        if (kelasTerpilih.isEmpty()) {
            Toast.makeText(this, "Pilih minimal satu kelas target", Toast.LENGTH_SHORT).show()
            return
        }
        if (deadlineCal == null) {
            Toast.makeText(this, "Tentukan tenggat waktu (deadline)", Toast.LENGTH_SHORT).show()
            return
        }

        val sdfDb = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val deadlineString = sdfDb.format(deadlineCal.time)

        val kelasArray = JSONArray()
        kelasTerpilih.forEach { kelasArray.put(it.id) }
        val kelasJsonString = kelasArray.toString()

        isSavingState.value = true
        val urlApi = AppConstants.SIMPAN_TUGAS

        val multipartRequest = object : VolleyMultipartRequest(
            Method.POST, urlApi,
            Response.Listener { response ->
                isSavingState.value = false
                val resultResponse = String(response.data)
                try {
                    val jsonObject = JSONObject(resultResponse)
                    if (jsonObject.getString("status") == "success") {
                        Toast.makeText(this, "Tugas Berhasil Disimpan!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Gagal: ${jsonObject.optString("message")}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Response error", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                isSavingState.value = false
                Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "jenis" to jenis,
                    "mapel" to mapel,
                    "judul" to judul,
                    "deskripsi" to deskripsi,
                    "is_online" to if (isOnline) "1" else "0",
                    "deadline" to deadlineString,
                    "nama_guru" to namaGuruLogin,
                    "kelas_array" to kelasJsonString
                )
            }

            override fun getByteData(): Map<String, DataPart>? {
                val params = HashMap<String, DataPart>()
                if (selectedFileUri != null) {
                    try {
                        val inputStream: InputStream? = contentResolver.openInputStream(selectedFileUri!!)
                        val fileBytes = inputStream?.readBytes()
                        if (fileBytes != null) {
                            val fileName = getFileName(selectedFileUri!!)
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

data class KelasModel(
    val id: String,
    val namaKelas: String,
    var isSelected: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahTugasScreen(
    listKelas: List<KelasModel>,
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onSelectAllKelas: (Boolean) -> Unit,
    onToggleKelas: (Int) -> Unit,
    onPickFile: () -> Unit,
    onSubmit: (
        mapel: String,
        jenis: String,
        judul: String,
        deskripsi: String,
        isOnline: Boolean,
        deadline: Calendar?
    ) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var mapelName by remember { mutableStateOf("") }
    var jenisName by remember { mutableStateOf("") }
    var judulStr by remember { mutableStateOf("") }
    var deskripsiStr by remember { mutableStateOf("") }
    var isOnline by remember { mutableStateOf(true) }
    var deadlineCalendar by remember { mutableStateOf<Calendar?>(null) }
    var deadlineDisplay by remember { mutableStateOf("") }

    var expandedMapelMenu by remember { mutableStateOf(false) }
    var expandedJenisMenu by remember { mutableStateOf(false) }

    val listMapel = stringArrayResource(id = R.array.mapelnya).toList()
    val listJenis = stringArrayResource(id = R.array.jenistugas).toList()

    val allSelected = listKelas.isNotEmpty() && listKelas.all { it.isSelected }

    fun showDateTimePicker() {
        val currentCalendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val cal = Calendar.getInstance().apply {
                            set(year, month, day, hour, minute)
                        }
                        deadlineCalendar = cal
                        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        deadlineDisplay = sdf.format(cal.time)
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Buat Tugas / Materi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            onSubmit(mapelName, jenisName, judulStr, deskripsiStr, isOnline, deadlineCalendar)
                        },
                        enabled = !isSaving && mapelName.isNotBlank() && judulStr.isNotBlank() && jenisName.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Simpan", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Mapel Selector
                ExposedDropdownMenuBox(
                    expanded = expandedMapelMenu,
                    onExpandedChange = { expandedMapelMenu = !expandedMapelMenu }
                ) {
                    OutlinedTextField(
                        value = mapelName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mata Pelajaran") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMapelMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(16.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedMapelMenu,
                        onDismissRequest = { expandedMapelMenu = false }
                    ) {
                        listMapel.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    mapelName = item
                                    expandedMapelMenu = false
                                }
                            )
                        }
                    }
                }

                // 2. Jenis Selector
                ExposedDropdownMenuBox(
                    expanded = expandedJenisMenu,
                    onExpandedChange = { expandedJenisMenu = !expandedJenisMenu }
                ) {
                    OutlinedTextField(
                        value = jenisName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jenis (Penugasan / Materi)") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Assignment, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJenisMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(16.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedJenisMenu,
                        onDismissRequest = { expandedJenisMenu = false }
                    ) {
                        listJenis.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    jenisName = item
                                    expandedJenisMenu = false
                                }
                            )
                        }
                    }
                }

                // 3. Judul Field
                OutlinedTextField(
                    value = judulStr,
                    onValueChange = { judulStr = it },
                    label = { Text("Judul Tugas / Materi") },
                    placeholder = { Text("Contoh: Tugas Bab 1 Aljabar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                // 4. Deskripsi Field
                OutlinedTextField(
                    value = deskripsiStr,
                    onValueChange = { deskripsiStr = it },
                    label = { Text("Deskripsi atau Isi Materi") },
                    placeholder = { Text("Tuliskan instruksi atau ringkasan materi...") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                // 5. Attachment Section
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Lampiran File / Foto",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Opsional (PDF atau Gambar)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onPickFile,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pilih File")
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // 6. Kelas Target Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pilih Kelas Target",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Checkbox(
                                checked = allSelected,
                                onCheckedChange = { onSelectAllKelas(it) }
                            )
                            Text(
                                text = "Pilih Semua",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Kelas Checkboxes Surface
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            listKelas.forEachIndexed { index, kelas ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleKelas(index) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = kelas.namaKelas,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Checkbox(
                                        checked = kelas.isSelected,
                                        onCheckedChange = { onToggleKelas(index) }
                                    )
                                }
                                if (index < listKelas.size - 1) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // 7. Switch Wajib Pengumpulan Online
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Wajib Pengumpulan Online",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Siswa wajib mengunggah file jawaban",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isOnline,
                            onCheckedChange = { isOnline = it }
                        )
                    }
                }

                // 8. Deadline Field
                OutlinedTextField(
                    value = deadlineDisplay,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tenggat Waktu (Deadline)") },
                    placeholder = { Text("Pilih tanggal dan waktu...") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showDateTimePicker() }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pilih Tanggal")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDateTimePicker() },
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Submit Button at bottom
                Button(
                    onClick = {
                        onSubmit(mapelName, jenisName, judulStr, deskripsiStr, isOnline, deadlineCalendar)
                    },
                    enabled = !isSaving && mapelName.isNotBlank() && judulStr.isNotBlank() && jenisName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simpan Tugas / Materi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Loading Overlay when saving
            if (isSaving) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "saving infinite transition")
                            val animatedProgress = infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(200, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "saving progress"
                            )

                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                tonalElevation = 4.dp,
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    LoadingIndicator(
                                        progress = { animatedProgress.value },
                                        modifier = Modifier.size(46.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Menyimpan tugas / materi...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TambahTugasScreenPreview() {
    AppTheme {
        val mockKelas = listOf(
            KelasModel("1", "X 1", true),
            KelasModel("2", "X 2", false),
            KelasModel("3", "XI 1", false)
        )
        TambahTugasScreen(
            listKelas = mockKelas,
            isSaving = false,
            onBackClick = {},
            onSelectAllKelas = {},
            onToggleKelas = {},
            onPickFile = {},
            onSubmit = { _, _, _, _, _, _ -> }
        )
    }
}
