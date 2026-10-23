@file:OptIn(ExperimentalMaterial3Api::class)

package id.rudissp.esmansa.metu

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.AppTheme
import org.json.JSONArray
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MetuActivity : ComponentActivity() {
    private lateinit var prefs: SharedPreferences
    private val metuListState = mutableStateListOf<Metu>()
    private var isLoadingState = mutableStateOf(true)
    private var isRefreshingState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)

        getData()

        setContent {
            AppTheme {
                MetuScreen(
                    metuList = metuListState,
                    isLoading = isLoadingState.value,
                    isRefreshing = isRefreshingState.value,
                    onBackClick = { finish() },
                    onRefresh = {
                        isRefreshingState.value = true
                        getData()
                    },
                    onAddClick = {
                        startActivity(Intent(this, TambahMetu::class.java))
                    },
                    onItemClick = { metu ->
                        val intent = Intent(this, TambahMetu::class.java).apply {
                            putExtra("mode", "edit")
                            putExtra("izin", metu)
                        }
                        startActivity(intent)
                    },
                    onDeleteClick = { metu ->
                        hapusData(metu.id)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    private fun getData() {
        isLoadingState.value = true
        val url = AppConstants.GET_METU

        metuListState.clear()

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                isLoadingState.value = false
                isRefreshingState.value = false
                try {
                    val jsonArray = JSONArray(response)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        metuListState.add(
                            Metu(
                                id = obj.optString("id"),
                                nama = obj.optString("nama"),
                                kelas = obj.optString("kelas"),
                                jam1 = obj.optString("jam1"),
                                jam2 = obj.optString("jam2"),
                                guru = obj.optString("guru"),
                                alasan = obj.optString("alasan"),
                                tanggal = obj.optString("tanggal"),
                                foto = obj.optString("foto")
                            )
                        )
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Gagal parsing data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                isLoadingState.value = false
                isRefreshingState.value = false
                Toast.makeText(this, "Gagal ambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun hapusData(id: String) {
        val url = AppConstants.HAPUS_METU
        val request = object : StringRequest(
            Method.POST, url,
            { _ ->
                Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                getData()
            },
            { error ->
                Toast.makeText(this, "Gagal hapus: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf("id" to id)
            }
        }
        Volley.newRequestQueue(this).add(request)
    }
}

@Composable
fun MetuHeroSummaryCard(
    metuList: List<Metu>, // Sekarang menerima list yang sudah difilter
    modifier: Modifier = Modifier
) {
    val totalIzin = metuList.size

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.ExitToApp,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Izin Keluar Siswa",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Rekapitulasi Harian",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text("Total Izin", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(
                            "$totalIzin Siswa",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetuScreen(
    metuList: List<Metu>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onAddClick: () -> Unit,
    onItemClick: (Metu) -> Unit,
    onDeleteClick: (Metu) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<Metu?>(null) }

    // Simpan Calendar yang dipilih untuk memformat ke berbagai format string
    var filterCalendar by remember { mutableStateOf<Calendar?>(null) }
    val context = LocalContext.current

    val currentCal = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selected = Calendar.getInstance()
            selected.set(year, month, dayOfMonth)
            filterCalendar = selected
        },
        currentCal.get(Calendar.YEAR),
        currentCal.get(Calendar.MONTH),
        currentCal.get(Calendar.DAY_OF_MONTH)
    )

    // Logika Filter Multi-Format (Menyesuaikan dengan format API / Database)
    val filteredList = if (filterCalendar != null) {
        val formatIndo = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(filterCalendar!!.time)
        val formatIndoZero = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(filterCalendar!!.time)
        val formatDb = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(filterCalendar!!.time)
        val formatSlash = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(filterCalendar!!.time)

        metuList.filter {
            it.tanggal.contains(formatIndo, ignoreCase = true) ||
                    it.tanggal.contains(formatIndoZero, ignoreCase = true) ||
                    it.tanggal.contains(formatDb) ||
                    it.tanggal.contains(formatSlash)
        }
    } else {
        metuList
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Izin Keluar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(start = 12.dp)
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
                actions = {
                    IconButton(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(CircleShape)
                            .background(if (filterCalendar != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FilterList,
                            contentDescription = "Filter",
                            tint = if (filterCalendar != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = {
                    Text(
                        "Buat Izin",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading && !isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "Memuat data izin...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp) // Jarak antar card diperkecil
                    ) {
                        item {
                            // Mengirim filteredList agar Total Siswa menyesuaikan filter hari
                            MetuHeroSummaryCard(metuList = filteredList)
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (filterCalendar != null) {
                            item {
                                val displayDate = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(filterCalendar!!.time)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.CalendarToday,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Filter: $displayDate",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                        IconButton(
                                            onClick = { filterCalendar = null },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.Close,
                                                contentDescription = "Hapus Filter",
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredList.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (filterCalendar != null) "Tidak ada izin pada tanggal ini." else "Belum ada catatan izin keluar.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(filteredList, key = { it.id }) { item ->
                                var isExpanded by remember { mutableStateOf(false) }

                                Card(
                                    shape = RoundedCornerShape(20.dp), // Sudut diperkecil
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                    ),
                                    border = CardDefaults.outlinedCardBorder(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isExpanded = !isExpanded }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(IntrinsicSize.Min) // Agar tinggi Box foto mengikuti tinggi konten teks
                                    ) {
                                        // SISI KIRI: Foto Full Atas ke Bawah (Lebih Compact)
                                        Box(
                                            modifier = Modifier
                                                .width(100.dp) // Ukuran foto lebih ringkas
                                                .fillMaxHeight()
                                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                        ) {
                                            AndroidView(
                                                modifier = Modifier.fillMaxSize(),
                                                factory = { ctx ->
                                                    ImageView(ctx).apply {
                                                        scaleType = ImageView.ScaleType.CENTER_CROP
                                                    }
                                                },
                                                update = { iv ->
                                                    Glide.with(iv.context)
                                                        .load(AppConstants.BASE_URL + "fotometu/" + item.foto)
                                                        .into(iv)
                                                }
                                            )
                                        }

                                        // SISI KANAN: Detail Informasi
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(12.dp), // Padding lebih kecil agar ringkas
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Header Info: Nama & Tombol Hapus
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Column(
                                                    modifier = Modifier.weight(1f), // Mencegah nama menabrak ikon hapus
                                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Text(
                                                        text = item.nama,
                                                        style = MaterialTheme.typography.titleMedium, // Nama diperkecil
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = MaterialTheme.colorScheme.secondaryContainer
                                                    ) {
                                                        Text(
                                                            text = "Kelas: ${item.kelas}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }

                                                IconButton(
                                                    onClick = { showDeleteDialog = item },
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .padding(start = 4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Hapus",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            // Jam & Tanggal Expandable
                                            AnimatedVisibility(
                                                visible = isExpanded,
                                                enter = expandVertically(),
                                                exit = shrinkVertically()
                                            ) {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.AccessTime,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Text(
                                                                text = "Jam ke: ${item.jam1} - ${item.jam2}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                                            )
                                                        }
                                                    }

                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.CalendarToday,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.tertiary,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Text(
                                                                text = item.tanggal,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                                            )
                                                        }
                                                    }

                                                    if (item.alasan.isNotBlank()) {
                                                        Text(
                                                            text = "Keperluan: ${item.alasan}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "Oleh: ${item.guru}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        TextButton(
                                                            onClick = { onItemClick(item) },
                                                            contentPadding = PaddingValues(0.dp),
                                                            modifier = Modifier.height(24.dp)
                                                        ) {
                                                            Text("Edit", style = MaterialTheme.typography.labelMedium)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(88.dp))
                        }
                    }
                }
            }

            if (showDeleteDialog != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    icon = {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    },
                    title = { Text("Hapus Izin Keluar") },
                    text = { Text("Yakin ingin menghapus izin keluar siswa ini? Tindakan ini tidak dapat dibatalkan.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteDialog?.let { onDeleteClick(it) }
                                showDeleteDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Hapus")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = null }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MetuScreenPreview() {
    AppTheme {
        MetuScreen(
            metuList = listOf(
                Metu(
                    id = "1",
                    nama = "Ahmad Fauzi",
                    kelas = "XII IPA 1",
                    jam1 = "1",
                    jam2 = "2",
                    guru = "Budi Santoso, S.Pd.",
                    alasan = "Sakit perut",
                    tanggal = "12 Oktober 2024",
                    foto = ""
                ),
                Metu(
                    id = "2",
                    nama = "Siti Aminah",
                    kelas = "XI IPS 2",
                    jam1 = "3",
                    jam2 = "4",
                    guru = "Siti Rahma, M.Pd.",
                    alasan = "Mengikuti lomba OSN",
                    tanggal = "12 Oktober 2024",
                    foto = ""
                )
            ),
            isLoading = false,
            isRefreshing = false,
            onBackClick = {},
            onRefresh = {},
            onAddClick = {},
            onItemClick = {},
            onDeleteClick = {}
        )
    }
}