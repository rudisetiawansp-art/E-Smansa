package id.rudissp.esmansa.jurnal

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Class
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.AppTheme
import id.rudissp.esmansa.LoadingIndicator
import id.rudissp.esmansa.R
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TambahJurnal : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    private var idJurnal = ""
    private var usernameJurnal = ""
    private var kelasaIntent = ""

    private var isSavingState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)

        val namauser = prefs.getString("namalengkap", "") ?: ""
        val currentUsername = prefs.getString("username", "") ?: ""
        val tugaskah = intent.getStringExtra("tugaskah") ?: ""
        val mode = intent.getStringExtra("mode") ?: ""
        kelasaIntent = intent.getStringExtra("kelasnya") ?: ""

        val isEditMode = mode == "editmode"
        val isTugas = tugaskah == "TUGAS" || (isEditMode && intent.getStringExtra("materi") == "TUGAS")

        if (isEditMode) {
            idJurnal = intent.getStringExtra("id") ?: ""
            usernameJurnal = intent.getStringExtra("username") ?: ""
        } else {
            usernameJurnal = currentUsername
        }

        val initNamaGuru = if (isEditMode) intent.getStringExtra("nama") ?: namauser else namauser
        val initMapel = if (isEditMode) intent.getStringExtra("mapel") ?: "" else ""
        val initTanggal = if (isEditMode) {
            intent.getStringExtra("tanggal") ?: currentDateString()
        } else {
            currentDateString()
        }
        val initKelas = if (isEditMode) intent.getStringExtra("kelas") ?: kelasaIntent else kelasaIntent
        val initJam1 = if (isEditMode) intent.getStringExtra("jamke1") ?: "" else ""
        val initJam2 = if (isEditMode) intent.getStringExtra("jamke2") ?: "" else ""
        val initMateri = if (isEditMode) {
            intent.getStringExtra("materi") ?: ""
        } else if (isTugas) {
            "TUGAS"
        } else {
            ""
        }
        val initKehadiran = if (isEditMode) intent.getStringExtra("kehadiran") ?: "" else ""
        val initKeterangan = if (isEditMode) intent.getStringExtra("keterangan") ?: "" else ""

        setContent {
            AppTheme {
                TambahJurnalScreen(
                    isEditMode = isEditMode,
                    isTugas = isTugas,
                    initNamaGuru = initNamaGuru,
                    initMapel = initMapel,
                    initTanggal = initTanggal,
                    initKelas = initKelas,
                    initJam1 = initJam1,
                    initJam2 = initJam2,
                    initMateri = initMateri,
                    initKehadiran = initKehadiran,
                    initKeterangan = initKeterangan,
                    isSaving = isSavingState.value,
                    onBackClick = {
                        prefs.edit { putString("mode", "") }
                        finish()
                    },
                    onSubmit = { guru, mapel, tgl, kls, j1, j2, mat, hadir, ket ->
                        if (usernameJurnal.isNotBlank() && mapel.isNotBlank() && tgl.isNotBlank() && kls.isNotBlank()) {
                            if (isEditMode) {
                                editJurnalApi(guru, mapel, tgl, kls, j1, j2, mat, hadir, ket)
                            } else {
                                simpanJurnalApi(guru, mapel, tgl, kls, j1, j2, mat, hadir, ket)
                            }
                        } else {
                            Toast.makeText(this, "Lengkapi kolom wajib (Mapel, Kelas, Tanggal)", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    private fun currentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun simpanJurnalApi(
        nama: String, mapel: String, tanggal: String,
        kelas: String, jamke1: String, jamke2: String,
        materi: String, kehadiran: String, keterangan: String
    ) {
        isSavingState.value = true
        val url = AppConstants.SIMPAN_JURNAL

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                isSavingState.value = false
                try {
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.optString("status")
                    if (status == "success") {
                        Toast.makeText(this, "Jurnal berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, JurnalActivity::class.java).apply {
                            putExtra("mode", "individu")
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        val msg = jsonResponse.optString("message", "Gagal menyimpan jurnal")
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Terjadi kesalahan respon server", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                isSavingState.value = false
                Toast.makeText(this, "Gagal terhubung ke server: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "username" to usernameJurnal,
                    "nama" to nama,
                    "mapel" to mapel,
                    "tanggal" to tanggal,
                    "kelas" to kelas,
                    "jamke1" to jamke1,
                    "jamke2" to jamke2,
                    "materi" to materi,
                    "kehadiran" to kehadiran,
                    "keterangan" to keterangan
                )
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun editJurnalApi(
        nama: String, mapel: String, tanggal: String,
        kelas: String, jamke1: String, jamke2: String,
        materi: String, kehadiran: String, keterangan: String
    ) {
        isSavingState.value = true
        val url = AppConstants.EDIT_JURNAL

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { _ ->
                isSavingState.value = false
                Toast.makeText(this, "Jurnal berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                prefs.edit { putString("mode", "") }
                finish()
            },
            { error ->
                isSavingState.value = false
                Toast.makeText(this, "Gagal mengedit jurnal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "id" to idJurnal,
                    "username" to usernameJurnal,
                    "nama" to nama,
                    "mapel" to mapel,
                    "tanggal" to tanggal,
                    "kelas" to kelas,
                    "jamke1" to jamke1,
                    "jamke2" to jamke2,
                    "materi" to materi,
                    "kehadiran" to kehadiran,
                    "keterangan" to keterangan
                )
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahJurnalScreen(
    isEditMode: Boolean,
    isTugas: Boolean,
    initNamaGuru: String,
    initMapel: String,
    initTanggal: String,
    initKelas: String,
    initJam1: String,
    initJam2: String,
    initMateri: String,
    initKehadiran: String,
    initKeterangan: String,
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onSubmit: (
        namaGuru: String, mapel: String, tanggal: String, kelas: String,
        jamke1: String, jamke2: String, materi: String, kehadiran: String, keterangan: String
    ) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var guruName by remember { mutableStateOf(initNamaGuru) }
    var mapelName by remember { mutableStateOf(initMapel) }
    var tanggalStr by remember { mutableStateOf(initTanggal) }
    var kelasName by remember { mutableStateOf(initKelas) }
    var jamke1Str by remember { mutableStateOf(initJam1) }
    var jamke2Str by remember { mutableStateOf(initJam2) }
    var materiStr by remember { mutableStateOf(initMateri) }
    var kehadiranStr by remember { mutableStateOf(initKehadiran) }
    var keteranganStr by remember { mutableStateOf(initKeterangan) }

    var expandedKelasMenu by remember { mutableStateOf(false) }
    var expandedMapelMenu by remember { mutableStateOf(false) }

    val listKelas = stringArrayResource(id = R.array.kelasnya).toList()
    val listMapel = stringArrayResource(id = R.array.mapelnya).toList()

    val isKelasEditable = isEditMode || isTugas

    // Expressive Shape constants
    val fieldShape = RoundedCornerShape(24.dp)
    val cardShape = RoundedCornerShape(32.dp)

    fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                tanggalStr = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            // Using CenterAlignedTopAppBar for expressive modern look
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Jurnal" else if (isTugas) "Jurnal Tugas" else "Jurnal Baru",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                // Bagian actions dihapus agar judul App Bar lebih lega
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
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
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // M3 Expressive Banner Card - fluid rounding and tertiary/secondary palettes
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = cardShape,
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isTugas) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isTugas) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isTugas) Icons.AutoMirrored.Rounded.Assignment else Icons.Rounded.School,
                                    contentDescription = null,
                                    tint = if (isTugas) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (isTugas) "Penugasan Siswa" else "Mengajar Reguler",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isTugas) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = if (isTugas) "Pemberian tugas saat guru berhalangan hadir" else "Rincian materi mengajar dan presensi",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isTugas) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // 1. Guru Mapel Field (Read Only)
                OutlinedTextField(
                    value = guruName,
                    onValueChange = { guruName = it },
                    enabled = false,
                    label = { Text("Guru Mapel") },
                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                )

                // 2. Kelas Selector (Exposed Dropdown)
                ExposedDropdownMenuBox(
                    expanded = expandedKelasMenu,
                    onExpandedChange = {
                        if (isKelasEditable) expandedKelasMenu = !expandedKelasMenu
                    }
                ) {
                    OutlinedTextField(
                        value = kelasName,
                        onValueChange = { kelasName = it },
                        readOnly = !isKelasEditable,
                        enabled = isKelasEditable,
                        label = { Text("Kelas") },
                        leadingIcon = { Icon(Icons.Rounded.Class, contentDescription = null) },
                        trailingIcon = {
                            if (isKelasEditable) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKelasMenu)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = fieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )

                    if (isKelasEditable) {
                        ExposedDropdownMenu(
                            expanded = expandedKelasMenu,
                            onDismissRequest = { expandedKelasMenu = false }
                        ) {
                            listKelas.forEach { itemKelas ->
                                DropdownMenuItem(
                                    text = { Text(itemKelas) },
                                    onClick = {
                                        kelasName = itemKelas
                                        expandedKelasMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. Mapel Selector (Exposed Dropdown)
                ExposedDropdownMenuBox(
                    expanded = expandedMapelMenu,
                    onExpandedChange = { expandedMapelMenu = !expandedMapelMenu }
                ) {
                    OutlinedTextField(
                        value = mapelName,
                        onValueChange = { mapelName = it },
                        readOnly = true,
                        label = { Text("Mata Pelajaran (Mapel)") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMapelMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = fieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandedMapelMenu,
                        onDismissRequest = { expandedMapelMenu = false }
                    ) {
                        listMapel.forEach { itemMapel ->
                            DropdownMenuItem(
                                text = { Text(itemMapel) },
                                onClick = {
                                    mapelName = itemMapel
                                    expandedMapelMenu = false
                                }
                            )
                        }
                    }
                }

                // 4. Tanggal Selector Field
                OutlinedTextField(
                    value = tanggalStr,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tanggal Mengajar") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker() }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pilih Tanggal", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker() },
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                )

                // 5. Jam Ke Range Input
                Surface(
                    shape = cardShape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Durasi Jam Pelajaran",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = jamke1Str,
                                onValueChange = { jamke1Str = it },
                                label = { Text("Mulai") },
                                placeholder = { Text("1") },
                                leadingIcon = { Icon(Icons.Rounded.Schedule, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            Text(
                                text = "s.d.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = jamke2Str,
                                onValueChange = { jamke2Str = it },
                                label = { Text("Selesai") },
                                placeholder = { Text("2") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                }

                // 6. Rincian Kegiatan / Materi Field
                OutlinedTextField(
                    value = materiStr,
                    onValueChange = { if (!isTugas) materiStr = it },
                    enabled = !isTugas,
                    label = { Text("Rincian Kegiatan / Materi") },
                    placeholder = { Text("Tuliskan ringkasan materi yang diajarkan...") },
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                )

                // 7. Presensi Siswa Field (Only for Regular)
                if (!isTugas) {
                    OutlinedTextField(
                        value = kehadiranStr,
                        onValueChange = { kehadiranStr = it },
                        label = { Text("Presensi Siswa / Catatan Kehadiran") },
                        placeholder = { Text("Contoh: Hadir 32, Nihil / S : 1, I : 0, A : 0") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )
                }

                // 8. Keterangan Guru Tidak Hadir (Only for Tugas)
                if (isTugas) {
                    OutlinedTextField(
                        value = keteranganStr,
                        onValueChange = { keteranganStr = it },
                        label = { Text("Keterangan Guru Tidak Hadir") },
                        placeholder = { Text("Sebab ketidakhadiran / Catatan instruksi tugas...") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Expressive Bottom Action Button (Satu-satunya tombol simpan sekarang)
                Button(
                    onClick = {
                        onSubmit(
                            guruName, mapelName, tanggalStr, kelasName,
                            jamke1Str, jamke2Str, materiStr, kehadiranStr, keteranganStr
                        )
                    },
                    enabled = !isSaving && mapelName.isNotBlank() && kelasName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp), // Thicker, more tactile expressive button
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isEditMode) "Perbarui Jurnal" else "Simpan Jurnal Baru",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }

            // Loading Overlay when saving
            if (isSaving) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)
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
                                    animation = tween(400, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "saving progress"
                            )

                            Surface(
                                shape = cardShape, // Adaptive expressive container for loader
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shadowElevation = 12.dp,
                                modifier = Modifier.size(96.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    LoadingIndicator(
                                        progress = { animatedProgress.value },
                                        modifier = Modifier.size(48.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Menyimpan data...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(name = "Tambah Jurnal Screen - Tugas", showBackground = true, showSystemUi = true)
@Composable
fun TambahJurnalScreenTugasPreview() {
    AppTheme {
        TambahJurnalScreen(
            isEditMode = false,
            isTugas = true,
            initNamaGuru = "Drs. Budi Santoso, M.Pd.",
            initMapel = "Matematika (Umum)",
            initTanggal = "2025-05-10",
            initKelas = "X-1",
            initJam1 = "3",
            initJam2 = "4",
            initMateri = "TUGAS",
            initKehadiran = "",
            initKeterangan = "Kerjakan LKS Halaman 45 Nomor 1-10",
            isSaving = false,
            onBackClick = {},
            onSubmit = { _, _, _, _, _, _, _, _, _ -> }
        )
    }
}
