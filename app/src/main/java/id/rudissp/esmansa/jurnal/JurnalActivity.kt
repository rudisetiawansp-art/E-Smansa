package id.rudissp.esmansa.jurnal

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.AppTheme
import id.rudissp.esmansa.LoadingIndicator
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class JurnalActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var cameraExecutor: ExecutorService

    // Reactive State Variables
    private val jurnalListState = mutableStateListOf<JurnalModel>()
    private val jurnalListAllState = mutableStateListOf<JurnalModelAll>()
    private val guruListState = mutableStateListOf<String>()
    private val gukarMapState = mutableStateMapOf<String, String>()

    private var isLoadingState = mutableStateOf(true)
    private var isRefreshingState = mutableStateOf(false)
    private var isScanningState = mutableStateOf(false)

    private var modeState = mutableStateOf("individu")
    private var currentUserState = mutableStateOf("")
    private var currentUsernameState = mutableStateOf("")

    // All Teachers Search Inputs
    private var tglDariState = mutableStateOf("")
    private var tglSampaiState = mutableStateOf("")
    private var selectedGuruState = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        cameraExecutor = Executors.newSingleThreadExecutor()

        currentUserState.value = prefs.getString("namalengkap", "") ?: ""
        currentUsernameState.value = prefs.getString("username", "") ?: ""
        modeState.value = intent.getStringExtra("mode") ?: "individu"

        if (modeState.value == "individu") {
            loadJurnalUser()
        } else {
            loadGuruList()
        }

        setContent {
            AppTheme {
                JurnalScreen(
                    mode = modeState.value,
                    currentUser = currentUserState.value,
                    jurnalList = jurnalListState,
                    jurnalListAll = jurnalListAllState,
                    guruList = guruListState,
                    isLoading = isLoadingState.value,
                    isRefreshing = isRefreshingState.value,
                    isScanning = isScanningState.value,
                    tglDari = tglDariState.value,
                    tglSampai = tglSampaiState.value,
                    selectedGuru = selectedGuruState.value,
                    onBackClick = { finish() },
                    onRefresh = {
                        if (modeState.value == "individu") {
                            isRefreshingState.value = true
                            loadJurnalUser()
                        }
                    },
                    onSearchJurnalAll = { guru, dari, sampai ->
                        val usernameGuru = gukarMapState[guru] ?: ""
                        if (usernameGuru.isNotEmpty() && dari.isNotEmpty() && sampai.isNotEmpty()) {
                            loadJurnalAll(usernameGuru, dari, sampai)
                        } else {
                            Toast.makeText(this, "Lengkapi data pencarian", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onTglDariChange = { tglDariState.value = it },
                    onTglSampaiChange = { tglSampaiState.value = it },
                    onSelectedGuruChange = { selectedGuruState.value = it },
                    onStartScan = { checkCameraAndScan() },
                    onCloseScan = { isScanningState.value = false },
                    onAddTugas = {
                        val intent = Intent(this, TambahJurnal::class.java).apply {
                            putExtra("tugaskah", "TUGAS")
                        }
                        startActivity(intent)
                    },
                    onEditJurnal = { item ->
                        val intent = Intent(this, TambahJurnal::class.java).apply {
                            putExtra("id", item.id)
                            putExtra("username", item.username)
                            putExtra("nama", item.nama)
                            putExtra("mapel", item.mapel)
                            putExtra("tanggal", item.tanggal)
                            putExtra("kelas", item.kelas)
                            putExtra("jamke1", item.jamke1)
                            putExtra("jamke2", item.jamke2)
                            putExtra("materi", item.materi)
                            putExtra("kehadiran", item.kehadiran)
                            putExtra("keterangan", item.keterangan)
                            putExtra("mode", "editmode")
                        }
                        startActivity(intent)
                    },
                    onDeleteJurnal = { item ->
                        deleteJurnal(item.id)
                    },
                    onClassScanned = { kelasScanned ->
                        isScanningState.value = false
                        val intent = Intent(this, TambahJurnal::class.java).apply {
                            putExtra("kelasnya", kelasScanned)
                            putExtra("tugaskah", "bukan")
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

    private fun loadJurnalUser() {
        val username = currentUsernameState.value
        val url = "${AppConstants.GET_JURNAL_USER}?username=$username"

        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                jurnalListState.clear()
                isLoadingState.value = false
                isRefreshingState.value = false
                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    val data = JurnalModel(
                        id = obj.getString("id"),
                        username = obj.getString("username"),
                        nama = obj.getString("nama"),
                        mapel = obj.getString("mapel"),
                        tanggal = obj.getString("tanggal"),
                        kelas = obj.getString("kelas"),
                        jamke1 = obj.getString("jamke1"),
                        jamke2 = obj.getString("jamke2"),
                        materi = obj.getString("materi"),
                        kehadiran = obj.getString("kehadiran"),
                        keterangan = obj.getString("keterangan")
                    )
                    jurnalListState.add(data)
                }
            },
            { error ->
                isLoadingState.value = false
                isRefreshingState.value = false
                Toast.makeText(this, "Gagal memuat data jurnal", Toast.LENGTH_SHORT).show()
                error.printStackTrace()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun loadJurnalAll(username: String, tglAwal: String, tglAkhir: String) {
        isLoadingState.value = true
        val url = AppConstants.GET_JURNAL_CETAK

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                isLoadingState.value = false
                try {
                    val jsonArray = JSONArray(response)
                    jurnalListAllState.clear()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val data = JurnalModelAll(
                            nama = obj.getString("nama"),
                            tanggal = obj.getString("tanggal"),
                            kelas = obj.getString("kelas"),
                            mapel = obj.getString("mapel"),
                            jamke1 = obj.getString("jamke1"),
                            jamke2 = obj.getString("jamke2"),
                            materi = obj.getString("materi"),
                            kehadiran = obj.getString("kehadiran"),
                            keterangan = obj.getString("keterangan")
                        )
                        jurnalListAllState.add(data)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Gagal mengolah data jurnal", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                isLoadingState.value = false
                Toast.makeText(this, "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "username" to username,
                    "tgl_awal" to tglAwal,
                    "tgl_akhir" to tglAkhir
                )
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun loadGuruList() {
        isLoadingState.value = true
        val request = StringRequest(
            Request.Method.GET, AppConstants.LIST_GURU,
            { response ->
                isLoadingState.value = false
                try {
                    val jsonArray = JSONArray(response)
                    guruListState.clear()
                    gukarMapState.clear()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val nama = obj.getString("nama")
                        val username = obj.getString("username")

                        guruListState.add(nama)
                        gukarMapState[nama] = username
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                isLoadingState.value = false
                Toast.makeText(this, "Gagal memuat daftar guru: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun deleteJurnal(id: String) {
        val url = "${AppConstants.HAPUS_JURNAL}?id=$id"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val status = response.optString("status")
                if (status == "success") {
                    jurnalListState.removeAll { it.id == id }
                    Toast.makeText(this, "Data jurnal berhasil dihapus!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal menghapus jurnal", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Gagal menghapus jurnal", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun checkCameraAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            isScanningState.value = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isScanningState.value = true
        } else {
            Toast.makeText(this, "Izin kamera diperlukan untuk scan QR", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!cameraExecutor.isShutdown) {
            cameraExecutor.shutdown()
        }
    }

    override fun onResume() {
        super.onResume()
        if (modeState.value == "individu") {
            loadJurnalUser()
        }
    }
}

fun formatTanggalIndo(tanggal: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(tanggal)
        val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("id-ID"))
        outputFormat.format(date!!)
    } catch (_: Exception) {
        tanggal
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JurnalScreen(
    mode: String,
    currentUser: String,
    jurnalList: List<JurnalModel>,
    jurnalListAll: List<JurnalModelAll>,
    guruList: List<String>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    isScanning: Boolean,
    tglDari: String,
    tglSampai: String,
    selectedGuru: String,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onSearchJurnalAll: (guru: String, dari: String, sampai: String) -> Unit,
    onTglDariChange: (String) -> Unit,
    onTglSampaiChange: (String) -> Unit,
    onSelectedGuruChange: (String) -> Unit,
    onStartScan: () -> Unit,
    onCloseScan: () -> Unit,
    onAddTugas: () -> Unit,
    onEditJurnal: (JurnalModel) -> Unit,
    onDeleteJurnal: (JurnalModel) -> Unit,
    onClassScanned: (String) -> Unit
) {
    var itemToDelete by remember { mutableStateOf<JurnalModel?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (mode == "individu") currentUser.ifBlank { "Jurnal Saya" } else "Jurnal Semua Guru",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            )
        },
        floatingActionButton = {
            if (mode == "individu" && !isScanning) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // FAB Tambah Tugas
                    FloatingActionButton(
                        onClick = onAddTugas,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Assignment,
                                contentDescription = "Tambah Tugas"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tugas",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Extended FAB Scan / Tambah Jurnal
                    ExtendedFloatingActionButton(
                        onClick = onStartScan,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan QR Jurnal"
                            )
                        },
                        text = { Text("Scan QR", fontWeight = FontWeight.Bold) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (isLoading && !isRefreshing) {
                ExpressiveLoadingView(
                    message = if (mode == "individu") "Memuat data jurnal anda..." else "Memproses data jurnal..."
                )
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Section 1: Hero Card / Search Filter
                        if (mode == "individu") {
                            item {
                                JurnalHeroSummaryCard(
                                    jurnalList = jurnalList,
                                    onStartScan = onStartScan,
                                    onAddTugas = onAddTugas
                                )
                            }
                        } else {
                            item {
                                JurnalFilterCard(
                                    guruList = guruList,
                                    tglDari = tglDari,
                                    tglSampai = tglSampai,
                                    selectedGuru = selectedGuru,
                                    onTglDariChange = onTglDariChange,
                                    onTglSampaiChange = onTglSampaiChange,
                                    onSelectedGuruChange = onSelectedGuruChange,
                                    onSearch = { onSearchJurnalAll(selectedGuru, tglDari, tglSampai) }
                                )
                            }
                        }

                        // Section 2: Journal List Data
                        if (mode == "individu") {
                            if (jurnalList.isEmpty()) {
                                item {
                                    EmptyJurnalView(
                                        title = "Belum Ada Jurnal",
                                        description = "Belum ada catatan jurnal yang tersimpan. Tekan 'Scan QR Kelas' untuk menambah jurnal baru."
                                    )
                                }
                            } else {
                                items(jurnalList, key = { it.id }) { item ->
                                    JurnalCardItem(
                                        jurnal = item,
                                        onEdit = { onEditJurnal(item) },
                                        onDelete = { itemToDelete = item }
                                    )
                                }
                            }
                        } else {
                            if (jurnalListAll.isEmpty()) {
                                item {
                                    EmptyJurnalView(
                                        title = "Cari Jurnal Guru",
                                        description = "Pilih rentang tanggal dan nama guru di atas lalu tekan tombol 'Cari' untuk menampilkan jurnal."
                                    )
                                }
                            } else {
                                items(jurnalListAll) { item ->
                                    JurnalCardAllItem(jurnal = item)
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }

            // Camera Scan Overlay
            AnimatedVisibility(
                visible = isScanning,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CameraQrScannerOverlay(
                    onClose = onCloseScan,
                    onClassScanned = onClassScanned
                )
            }

            // Delete Confirmation Dialog
            itemToDelete?.let { item ->
                AlertDialog(
                    onDismissRequest = { itemToDelete = null },
                    title = { Text("Hapus Jurnal", fontWeight = FontWeight.Bold) },
                    text = {
                        Text("Apakah Anda yakin ingin menghapus jurnal kelas ${item.kelas} (${item.mapel})?")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onDeleteJurnal(item)
                                itemToDelete = null
                            }
                        ) {
                            Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { itemToDelete = null }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }
}

/**
 * Hero Card Expressive untuk Ringkasan Jurnal User
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JurnalHeroSummaryCard(
    jurnalList: List<JurnalModel>,
    onStartScan: () -> Unit,
    onAddTugas: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalJurnal = jurnalList.size
    val totalTugas = jurnalList.count { it.materi.equals("TUGAS", ignoreCase = true) }
    val totalReguler = totalJurnal - totalTugas
    val latestJurnal = jurnalList.firstOrNull()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.40f),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Notes,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Ringkasan Jurnal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Statistik Aktivitas Mengajar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "$totalJurnal Total",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Metrics Cards FlowRow
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricChip(
                    title = "Reguler",
                    value = "$totalReguler Jurnal",
                    icon = Icons.Rounded.School,
                    containerColor = MaterialTheme.colorScheme.surface
                )
                MetricChip(
                    title = "Tugas",
                    value = "$totalTugas Tugas",
                    icon = Icons.AutoMirrored.Rounded.Assignment,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                )
            }

            if (latestJurnal != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Terakhir: ${latestJurnal.kelas} • ${latestJurnal.mapel}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${formatTanggalIndo(latestJurnal.tanggal)} (Jam ke ${latestJurnal.jamke1}-${latestJurnal.jamke2})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun MetricChip(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Filter Form Card untuk Mode "semua" (Search Jurnal Guru)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JurnalFilterCard(
    guruList: List<String>,
    tglDari: String,
    tglSampai: String,
    selectedGuru: String,
    onTglDariChange: (String) -> Unit,
    onTglSampaiChange: (String) -> Unit,
    onSelectedGuruChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expandedGuruMenu by remember { mutableStateOf(false) }

    fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Cari Jurnal Guru",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = tglDari,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Dari") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker(onTglDariChange) }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pilih Tanggal Dari")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePicker(onTglDariChange) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                OutlinedTextField(
                    value = tglSampai,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sampai") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker(onTglSampaiChange) }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pilih Tanggal Sampai")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePicker(onTglSampaiChange) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            // Dropdown Selector Guru
            ExposedDropdownMenuBox(
                expanded = expandedGuruMenu,
                onExpandedChange = { expandedGuruMenu = !expandedGuruMenu }
            ) {
                OutlinedTextField(
                    value = selectedGuru,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nama Guru") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGuruMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(14.dp)
                )

                ExposedDropdownMenu(
                    expanded = expandedGuruMenu,
                    onDismissRequest = { expandedGuruMenu = false }
                ) {
                    guruList.forEach { guru ->
                        DropdownMenuItem(
                            text = { Text(guru) },
                            onClick = {
                                onSelectedGuruChange(guru)
                                expandedGuruMenu = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = onSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cari Jurnal", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Item Card untuk Jurnal Individu User
 */
@Composable
fun JurnalCardItem(
    jurnal: JurnalModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val isTugas = jurnal.materi.equals("TUGAS", ignoreCase = true)

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isTugas) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Kelas Badge, Jam Ke, Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isTugas) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = jurnal.kelas,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isTugas) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (isTugas) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "TUGAS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Jam ${jurnal.jamke1}-${jurnal.jamke2}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opsi",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Jurnal") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Edit, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Hapus Jurnal", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Body
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = jurnal.mapel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = jurnal.materi,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Footer info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTanggalIndo(jurnal.tanggal),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                if (jurnal.kehadiran.isNotBlank()) {
                    Text(
                        text = "Hadir: ${jurnal.kehadiran}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Item Card untuk Jurnal Semua Guru
 */
@Composable
fun JurnalCardAllItem(
    jurnal: JurnalModelAll,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = jurnal.kelas,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "Jam ${jurnal.jamke1}-${jurnal.jamke2}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = jurnal.tanggal,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = jurnal.nama,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = jurnal.mapel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = jurnal.materi,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Loading View dengan Expressive LoadingIndicator
 */
@Composable
fun ExpressiveLoadingView(
    message: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading infinite transition")
    val animatedProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading progress"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
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
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Tampilan saat data kosong
 */
@Composable
fun EmptyJurnalView(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Book,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Camera QR Scanner Overlay untuk Scan QR Kelas
 */
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun CameraQrScannerOverlay(
    onClose: () -> Unit,
    onClassScanned: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isScanned by remember { mutableStateOf(false) }

    val daftarKelas = remember {
        setOf(
            "X 1", "X 2", "X 3", "X 4", "X 5", "X 6", "X 7", "X 8", "X 9",
            "XI 1", "XI 2", "XI 3", "XI 4", "XI 5", "XI 6", "XI 7", "XI 8", "XI 9",
            "XII 1", "XII 2", "XII 3", "XII 4", "XII 5", "XII 6", "XII 7", "XII 8", "XII 9"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                val cameraExecutor = Executors.newSingleThreadExecutor()

                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val barcodeScanner = BarcodeScanning.getClient()
                        val imageAnalyzer = ImageAnalysis.Builder().build().also { analyzer ->
                            analyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null && !isScanned) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    barcodeScanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            for (barcode in barcodes) {
                                                val rawValue = barcode.rawValue
                                                if (rawValue in daftarKelas && !isScanned) {
                                                    isScanned = true
                                                    imageProxy.close()
                                                    cameraProvider.unbindAll()
                                                    onClassScanned(rawValue!!)
                                                    return@addOnSuccessListener
                                                }
                                            }
                                        }
                                        .addOnCompleteListener {
                                            if (!isScanned) {
                                                imageProxy.close()
                                            }
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }
                        }

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalyzer
                        )
                    } catch (e: Exception) {
                        Log.e("CameraX", "Error setup camera", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Header & Controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "Arahkan Kamera ke QR Kode Kelas",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup Kamera",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

data class JurnalModel(
    val id: String,
    val username: String,
    val nama: String,
    val mapel: String,
    val tanggal: String,
    val kelas: String,
    val jamke1: String,
    val jamke2: String,
    val materi: String,
    val kehadiran: String,
    val keterangan: String
)

data class JurnalModelAll(
    val nama: String,
    val tanggal: String,
    val kelas: String,
    val mapel: String,
    val jamke1: String,
    val jamke2: String,
    val materi: String,
    val kehadiran: String,
    val keterangan: String
)

private val sampleJurnalList = listOf(
    JurnalModel(
        id = "1",
        username = "budi",
        nama = "Drs. Budi Santoso",
        mapel = "Matematika",
        tanggal = "2025-02-20",
        kelas = "XII 1",
        jamke1 = "1",
        jamke2 = "2",
        materi = "Persamaan Diferensial dan Penerapannya",
        kehadiran = "32",
        keterangan = "Selesai"
    ),
    JurnalModel(
        id = "2",
        username = "budi",
        nama = "Drs. Budi Santoso",
        mapel = "Matematika",
        tanggal = "2025-02-19",
        kelas = "XII 2",
        jamke1 = "3",
        jamke2 = "4",
        materi = "TUGAS",
        kehadiran = "30",
        keterangan = "Mengerjakan LKS Halaman 45"
    )
)

private val sampleJurnalAllList = listOf(
    JurnalModelAll(
        nama = "Drs. Budi Santoso",
        tanggal = "2025-02-20",
        kelas = "XII 1",
        mapel = "Matematika",
        jamke1 = "1",
        jamke2 = "2",
        materi = "Persamaan Diferensial",
        kehadiran = "32",
        keterangan = "Hadir"
    ),
    JurnalModelAll(
        nama = "Siti Aminah, S.Pd.",
        tanggal = "2025-02-20",
        kelas = "XI 3",
        mapel = "Bahasa Indonesia",
        jamke1 = "3",
        jamke2 = "4",
        materi = "Teks Tinjauan Kritis",
        kehadiran = "34",
        keterangan = "Hadir"
    )
)

private val sampleGuruList = listOf("Drs. Budi Santoso", "Siti Aminah, S.Pd.", "Ahmad Yani, M.Pd.")

@ComposePreview(name = "Jurnal Screen - Mode Individu", showBackground = true, showSystemUi = true)
@Composable
fun JurnalScreenIndividuPreview() {
    AppTheme {
        JurnalScreen(
            mode = "individu",
            currentUser = "Drs. Budi Santoso",
            jurnalList = sampleJurnalList,
            jurnalListAll = emptyList(),
            guruList = sampleGuruList,
            isLoading = false,
            isRefreshing = false,
            isScanning = false,
            tglDari = "2025-02-01",
            tglSampai = "2025-02-28",
            selectedGuru = "Drs. Budi Santoso",
            onBackClick = {},
            onRefresh = {},
            onSearchJurnalAll = { _, _, _ -> },
            onTglDariChange = {},
            onTglSampaiChange = {},
            onSelectedGuruChange = {},
            onStartScan = {},
            onCloseScan = {},
            onAddTugas = {},
            onEditJurnal = {},
            onDeleteJurnal = {},
            onClassScanned = {}
        )
    }
}

@ComposePreview(name = "Jurnal Screen - Mode Semua Guru", showBackground = true, showSystemUi = true)
@Composable
fun JurnalScreenSemuaPreview() {
    AppTheme {
        JurnalScreen(
            mode = "semua",
            currentUser = "Drs. Budi Santoso",
            jurnalList = emptyList(),
            jurnalListAll = sampleJurnalAllList,
            guruList = sampleGuruList,
            isLoading = false,
            isRefreshing = false,
            isScanning = false,
            tglDari = "2025-02-01",
            tglSampai = "2025-02-28",
            selectedGuru = "Drs. Budi Santoso",
            onBackClick = {},
            onRefresh = {},
            onSearchJurnalAll = { _, _, _ -> },
            onTglDariChange = {},
            onTglSampaiChange = {},
            onSelectedGuruChange = {},
            onStartScan = {},
            onCloseScan = {},
            onAddTugas = {},
            onEditJurnal = {},
            onDeleteJurnal = {},
            onClassScanned = {}
        )
    }
}
