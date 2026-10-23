package id.rudissp.esmansa.pantau

import android.app.DatePickerDialog
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.rounded.Monitor
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.AppTheme
import id.rudissp.esmansa.LoadingIndicator
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class JadwalModel(
    val jamKe: Int,
    val status: String,
    val guru: String,
    val mapel: String,
    val keterangan: String,
)

data class KelasModel(
    val namaKelas: String,
    val jadwalList: List<JadwalModel>,
)

class Pantau : ComponentActivity() {

    private val listDataDashboard = mutableStateListOf<KelasModel>()
    private var maxJamState = mutableIntStateOf(8)
    private var isLoadingState = mutableStateOf(true)
    private var isRefreshingState = mutableStateOf(false)
    private var selectedDateState = mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        fetchDataPemantauan(selectedDateState.value)

        setContent {
            AppTheme {
                PantauScreen(
                    listKelas = listDataDashboard,
                    maxJam = maxJamState.intValue,
                    selectedDate = selectedDateState.value,
                    isLoading = isLoadingState.value,
                    isRefreshing = isRefreshingState.value,
                    onBackClick = { finish() },
                    onRefresh = {
                        isRefreshingState.value = true
                        fetchDataPemantauan(selectedDateState.value)
                    },
                    onDateChange = { newDate ->
                        selectedDateState.value = newDate
                        fetchDataPemantauan(newDate)
                    }
                )
            }
        }
    }

    private fun fetchDataPemantauan(tanggal: String) {
        isLoadingState.value = true
        val url = "${AppConstants.BASE_URL}pantau/api_pemantauan.php?tanggal=$tanggal"

        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                isLoadingState.value = false
                isRefreshingState.value = false
                try {
                    val status = response.getString("status")
                    if (status == "success") {
                        listDataDashboard.clear()
                        maxJamState.intValue = response.optInt("max_jam", 8)

                        val jsonArrayData = response.getJSONArray("data")
                        for (i in 0 until jsonArrayData.length()) {
                            val kelasObj = jsonArrayData.getJSONObject(i)
                            val namaKelas = kelasObj.getString("kelas")
                            val jadwalArray = kelasObj.getJSONArray("jadwal")

                            val listJadwalSementara = mutableListOf<JadwalModel>()
                            for (j in 0 until jadwalArray.length()) {
                                val jadwalObj = jadwalArray.getJSONObject(j)
                                listJadwalSementara.add(
                                    JadwalModel(
                                        jamKe = jadwalObj.getInt("jam_ke"),
                                        status = jadwalObj.getString("status"),
                                        guru = jadwalObj.getString("guru"),
                                        mapel = jadwalObj.getString("mapel"),
                                        keterangan = jadwalObj.getString("keterangan")
                                    )
                                )
                            }
                            listDataDashboard.add(KelasModel(namaKelas, listJadwalSementara))
                        }
                    } else {
                        Toast.makeText(this, "Gagal memuat data dari server", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Format data tidak sesuai", Toast.LENGTH_SHORT).show()
                }
            },
            {
                isLoadingState.value = false
                isRefreshingState.value = false
                Toast.makeText(this, "Koneksi ke server terputus", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantauScreen(
    listKelas: List<KelasModel>,
    maxJam: Int,
    selectedDate: String,
    isLoading: Boolean,
    isRefreshing: Boolean,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onDateChange: (String) -> Unit,
) {
    val context = LocalContext.current

    fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateChange(selected)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Pemantauan KBM",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker() }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Pilih Tanggal"
                        )
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
            if (isLoading && !isRefreshing) {
                ExpressiveLoadingView(message = "Memuat data pemantauan KBM...")
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            PantauHeroSummaryCard(
                                selectedDate = selectedDate,
                                totalKelas = listKelas.size,
                                onPickDate = { showDatePicker() }
                            )
                        }

                        if (listKelas.isEmpty()) {
                            item {
                                EmptyPantauView(
                                    title = "Belum Ada Data Pemantauan",
                                    description = "Tidak ada data KBM untuk tanggal $selectedDate."
                                )
                            }
                        } else {
                            item {
                                // Horizontal scrollable matrix table for classes & periods
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val horizontalScrollState = rememberScrollState()
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                            .horizontalScroll(horizontalScrollState),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Header row (Jam 1 .. maxJam)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(width = 80.dp, height = 40.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "Kelas",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }

                                            for (i in 1..maxJam) {
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                    modifier = Modifier.size(width = 110.dp, height = 40.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = "Jam $i",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                        // Rows per class
                                        listKelas.forEach { kelas ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Class Name Badge
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(width = 80.dp, height = 64.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = kelas.namaKelas,
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = MaterialTheme.colorScheme.onPrimary,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }

                                                // Schedule slots for each period
                                                for (i in 1..maxJam) {
                                                    val jadwal = kelas.jadwalList.find { it.jamKe == i }
                                                    JadwalSlotCell(jadwal = jadwal)
                                                }
                                            }
                                        }
                                    }
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
    }
}

@Composable
fun JadwalSlotCell(
    jadwal: JadwalModel?,
    modifier: Modifier = Modifier,
) {
    val status = jadwal?.status ?: "kosong"
    val (bgColor, textColor) = when (status) {
        "hadir" -> Pair(Color(0xFFE6F4EA), Color(0xFF137333))
        "kosong" -> Pair(Color(0xFFF1F3F4), Color(0xFF5F6368))
        "belum_isi" -> Pair(Color(0xFFFCE8E6), Color(0xFFC5221F))
        "tugas" -> Pair(Color(0xFFFEF7E0), Color(0xFFB06000))
        "izin" -> Pair(Color(0xFFE8F0FE), Color(0xFF1967D2))
        else -> Pair(Color(0xFFF1F3F4), Color(0xFF5F6368))
    }

    val guruName = jadwal?.guru
    val isGuruKosong = guruName.isNullOrBlank() || guruName == "-"

    Surface(
        modifier = modifier.size(width = 110.dp, height = 64.dp),
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isGuruKosong) "Kosong" else guruName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            if (!jadwal?.mapel.isNullOrBlank() && jadwal?.mapel != "-") {
                Text(
                    text = jadwal.mapel,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PantauHeroSummaryCard(
    selectedDate: String,
    totalKelas: Int,
    onPickDate: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                                imageVector = Icons.Rounded.Monitor,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Pemantauan KBM",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tanggal: $selectedDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onPickDate,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ganti")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Column {
                            Text("Kelas Dipantau", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$totalKelas Kelas", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPantauView(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
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
                        imageVector = Icons.Rounded.Monitor,
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

@Composable
fun ExpressiveLoadingView(
    message: String,
    modifier: Modifier = Modifier,
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

@Preview(showBackground = true)
@Composable
fun PantauScreenPreview() {
    AppTheme {
        val mockList = listOf(
            KelasModel("X 1", listOf(JadwalModel(1, "hadir", "Budi, S.Pd", "Matematika", ""))),
            KelasModel("X 2", listOf(JadwalModel(1, "kosong", "-", "-", "")))
        )
        PantauScreen(
            listKelas = mockList,
            maxJam = 4,
            selectedDate = "2026-06-10",
            isLoading = false,
            isRefreshing = false,
            onBackClick = {},
            onRefresh = {},
            onDateChange = {}
        )
    }
}
