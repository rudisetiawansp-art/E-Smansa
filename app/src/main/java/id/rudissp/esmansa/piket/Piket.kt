@file:OptIn(ExperimentalMaterial3Api::class)

package id.rudissp.esmansa.piket

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.AppTheme
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

data class PiketModel(
    val id: String,
    val nama: String,
    val tanggal: String
)

fun extractMonthYear(tanggal: String): String {
    val months = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    for (m in months) {
        if (tanggal.contains(m, ignoreCase = true)) {
            val yearMatch = Regex("\\d{4}").find(tanggal)?.value ?: ""
            return if (yearMatch.isNotEmpty()) "$m $yearMatch" else m
        }
    }
    return if (tanggal.length >= 7) tanggal.take(7) else "Lainnya"
}

@ExperimentalMaterial3Api
class Piket : ComponentActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var cameraExecutor: ExecutorService

    private val piketListState = mutableStateListOf<PiketModel>()
    private var isLoadingState = mutableStateOf(true)
    private var isRefreshingState = mutableStateOf(false)
    private var isScanningState = mutableStateOf(false)
    private var sudahScan = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        cameraExecutor = Executors.newSingleThreadExecutor()

        loadDataPiket()

        setContent {
            AppTheme {
                PiketScreen(
                    piketList = piketListState,
                    isLoading = isLoadingState.value,
                    isRefreshing = isRefreshingState.value,
                    isScanning = isScanningState.value,
                    onBackClick = { finish() },
                    onRefresh = {
                        isRefreshingState.value = true
                        loadDataPiket()
                    },
                    onStartScan = { mulaiProsesScan() },
                    onStopScan = {
                        hentikanKamera()
                        isScanningState.value = false
                    },
                    cameraProviderFuture = { ProcessCameraProvider.getInstance(this) },
                    lifecycleOwner = this
                )
            }
        }
    }

    private fun loadDataPiket() {
        isLoadingState.value = true
        val namaUser = prefs.getString("namalengkap", "") ?: ""
        val url = AppConstants.GET_PIKET + "?nama=$namaUser"

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                isLoadingState.value = false
                isRefreshingState.value = false
                piketListState.clear()
                try {
                    val jsonArray = JSONArray(response)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        piketListState.add(
                            PiketModel(
                                obj.getString("id"),
                                obj.getString("nama"),
                                obj.getString("tanggal")
                            )
                        )
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal parsing data", Toast.LENGTH_SHORT).show()
                }
            },
            { _ ->
                isLoadingState.value = false
                isRefreshingState.value = false
                Toast.makeText(this, "Gagal ambil data", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun mulaiProsesScan() {
        sudahScan = false
        isScanningState.value = true

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    fun processBarcodeResult(token: String) {
        if (sudahScan) return
        try {
            val decodedBytes = Base64.decode(token, Base64.DEFAULT)
            val decodedString = String(decodedBytes).trim()

            if (decodedString.contains("SMAN1SUKOREJO", ignoreCase = true)) {
                sudahScan = true
                hentikanKamera()
                isScanningState.value = false

                val namaUser = prefs.getString("namalengkap", "") ?: ""
                if (namaUser.isNotEmpty()) {
                    uploadDataKeServer(namaUser)
                } else {
                    Toast.makeText(this, "Error: Nama tidak terdaftar", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Barcode tidak valid!", Toast.LENGTH_SHORT).show()
                sudahScan = false
            }
        } catch (e: Exception) {
            Log.e("Piket", "Dekode gagal: ${e.message}")
            sudahScan = false
        }
    }

    private fun hentikanKamera() {
        try {
            ProcessCameraProvider.getInstance(this).get().unbindAll()
        } catch (_: Exception) {}
    }

    private fun uploadDataKeServer(nama: String) {
        val request = object : StringRequest(Method.POST, AppConstants.TAMBAH_PIKET,
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.getString("status")
                    val message = jsonResponse.getString("message")

                    if (status == "success") {
                        Toast.makeText(this, "Berhasil absen piket: $nama", Toast.LENGTH_LONG).show()
                        loadDataPiket()
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                } catch (_: Exception) {
                    Toast.makeText(this, "Respon tidak dikenali", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "Masalah Jaringan", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("nama" to nama)
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    override fun onResume() {
        super.onResume()
        sudahScan = false
        isScanningState.value = false
    }

    override fun onPause() {
        super.onPause()
        hentikanKamera()
        isScanningState.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@Composable
fun PiketScreen(
    piketList: List<PiketModel>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    isScanning: Boolean,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    cameraProviderFuture: () -> com.google.common.util.concurrent.ListenableFuture<ProcessCameraProvider>,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner
) {
    val groupedByMonth = remember(piketList) {
        piketList.groupBy { extractMonthYear(it.tanggal) }.toSortedMap(reverseOrder())
    }
    val months = remember(groupedByMonth) { groupedByMonth.keys.toList() }
    var selectedMonth by remember(months) { mutableStateOf(months.firstOrNull() ?: "") }

    val currentRecords = groupedByMonth[selectedMonth] ?: emptyList()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Jurnal Piket Petugas",
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
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onStartScan,
                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                text = { Text("Scan QR Piket") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(20.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (isLoading && !isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (piketList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Tidak ada data piket",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Month Selector Tabs Row
                            if (months.isNotEmpty()) {
                                item {
                                    val scrollState = rememberScrollState()
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(scrollState),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        months.forEach { month ->
                                            val isSelected = month.equals(selectedMonth, ignoreCase = true)
                                            Surface(
                                                onClick = { selectedMonth = month },
                                                shape = RoundedCornerShape(16.dp),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
                                            ) {
                                                Text(
                                                    text = month,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Selected Month Group Container (Surface without individual item cards)
                            if (currentRecords.isNotEmpty()) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        ) {
                                            currentRecords.forEachIndexed { index, piket ->
                                                ListItem(
                                                    headlineContent = {
                                                        Text(
                                                            text = piket.nama,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    },
                                                    supportingContent = {
                                                        Text(
                                                            text = "Tanggal: ${piket.tanggal} • ID: ${piket.id}",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    },
                                                    leadingContent = {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                                            modifier = Modifier.size(40.dp)
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Icon(
                                                                    imageVector = Icons.Rounded.Person,
                                                                    contentDescription = null,
                                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                                                )
                                                            }
                                                        }
                                                    },
                                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                                )
                                                if (index < currentRecords.size - 1) {
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(horizontal = 16.dp),
                                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Tidak ada data untuk bulan $selectedMonth",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }
            }

            // Camera Scanner Overlay when scanning
            if (isScanning) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).also { previewView ->
                                val cameraProviderFutureInst = cameraProviderFuture()
                                cameraProviderFutureInst.addListener({
                                    try {
                                        val provider = cameraProviderFutureInst.get()
                                        val preview = androidx.camera.core.Preview.Builder().build().also {
                                            it.surfaceProvider = previewView.surfaceProvider
                                        }
                                        val barcodeScanner = BarcodeScanning.getClient()
                                        val imageAnalyzer = ImageAnalysis.Builder()
                                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                            .build()
                                            .also { analysis ->
                                                analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                                    readImageProxy(imageProxy, barcodeScanner, lifecycleOwner)
                                                }
                                            }

                                        provider.unbindAll()
                                        provider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageAnalyzer
                                        )
                                    } catch (e: Exception) {
                                        Log.e("Piket", "Camera bind error", e)
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    IconButton(
                        onClick = onStopScan,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(16.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup Scanner",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Arahkan kamera ke QR Code Piket",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(32.dp)
                    )
                }
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun readImageProxy(
    imageProxy: ImageProxy,
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (rawValue != null) {
                        if (lifecycleOwner is Piket) {
                            lifecycleOwner.processBarcodeResult(rawValue)
                        }
                    }
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}

@Preview(showBackground = true)
@Composable
fun PiketScreenPreview() {
    AppTheme {
        PiketScreen(
            piketList = listOf(
                PiketModel("1", "Budi Santoso", "Senin, 3 Januari 2026"),
                PiketModel("2", "Siti Aminah", "Senin, 3 Januari 2026"),
                PiketModel("3", "Ahmad Fajar", "Minggu, 2 Januari 2026"),
                PiketModel("4", "Dewi Lestari", "Rabu, 12 Februari 2026")
            ),
            isLoading = false,
            isRefreshing = false,
            isScanning = false,
            onBackClick = {},
            onRefresh = {},
            onStartScan = {},
            onStopScan = {},
            cameraProviderFuture = { throw UnsupportedOperationException() },
            lifecycleOwner = ComponentActivity()
        )
    }
}
