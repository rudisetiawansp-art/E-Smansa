@file:OptIn(ExperimentalMaterial3Api::class)

package id.rudissp.esmansa.sppd

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.AppTheme
import org.json.JSONArray

data class SppdModel(
    val id: String,
    val username: String,
    val nama: String,
    val jabatan: String,
    val nip: String,
    val tanggal: String,
    val lokasi: String,
    val deskripsi: String,
    val foto: String,
    val foto2: String
)

class SPPDActivity : ComponentActivity() {
    private lateinit var prefs: SharedPreferences
    private val sppdListState = mutableStateListOf<SppdModel>()
    private var isLoadingState = mutableStateOf(true)
    private var isRefreshingState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)

        getData()

        setContent {
            AppTheme {
                SPPDScreen(
                    sppdList = sppdListState,
                    isLoading = isLoadingState.value,
                    isRefreshing = isRefreshingState.value,
                    onBackClick = { finish() },
                    onRefresh = {
                        isRefreshingState.value = true
                        getData()
                    },
                    onAddClick = {
                        startActivity(Intent(this, TambahSppdActivity::class.java))
                    },
                    onItemClick = { sppd ->
                        val intent = Intent(this, TambahSppdActivity::class.java).apply {
                            putExtra("id", sppd.id)
                            putExtra("username", sppd.username)
                            putExtra("nama", sppd.nama)
                            putExtra("jabatan", sppd.jabatan)
                            putExtra("nip", sppd.nip)
                            putExtra("tanggal", sppd.tanggal)
                            putExtra("lokasi", sppd.lokasi)
                            putExtra("deskripsi", sppd.deskripsi)
                            putExtra("foto", sppd.foto)
                            putExtra("foto_lokasi", sppd.foto2)
                            putExtra("mode", "editmode")
                        }
                        startActivity(intent)
                    },
                    onDeleteClick = { sppd ->
                        hapusSppd(sppd.id)
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
        val username = prefs.getString("username", "")
        val url = AppConstants.GET_SPPD + "?username=$username"

        sppdListState.clear()

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                isLoadingState.value = false
                isRefreshingState.value = false
                try {
                    val dataArray = JSONArray(response)
                    for (i in 0 until dataArray.length()) {
                        val item = dataArray.getJSONObject(i)
                        sppdListState.add(
                            SppdModel(
                                item.optString("id"),
                                item.optString("username"),
                                item.optString("nama"),
                                item.optString("jabatan"),
                                item.optString("nip"),
                                item.optString("tanggal"),
                                item.optString("lokasi"),
                                item.optString("deskripsi"),
                                item.optString("foto"),
                                item.optString("foto_lokasi")
                            )
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            {
                isLoadingState.value = false
                isRefreshingState.value = false
                Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun hapusSppd(id: String) {
        val url = AppConstants.HAPUS_SPPD
        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                if (response.trim() == "success") {
                    Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                    getData()
                } else {
                    Toast.makeText(this, "Gagal hapus: $response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf("id" to id)
            }
        }
        Volley.newRequestQueue(this).add(request)
    }
}

@Composable
fun SPPDHeroSummaryCard(
    sppdList: List<SppdModel>,
    modifier: Modifier = Modifier
) {
    val totalSppd = sppdList.size
    val uniqueLokasi = sppdList.map { it.lokasi }.distinct().size

    // M3 Expressive uses layered container colors for heroic headers
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Rekap SPPD",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Perjalanan Dinas",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Adaptive container shape transitioning
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Laporan", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary)
                        Text(
                            "$totalSppd Total",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Destinasi", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "$uniqueLokasi Kota",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SPPDScreen(
    sppdList: List<SppdModel>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onAddClick: () -> Unit,
    onItemClick: (SppdModel) -> Unit,
    onDeleteClick: (SppdModel) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<SppdModel?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Perjalanan Dinas",
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
                        "Buat SPPD",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(24.dp) // Expressive pill shape
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
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (sppdList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Belum ada laporan dinas.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                SPPDHeroSummaryCard(sppdList = sppdList)
                            }

                            items(sppdList, key = { it.id }) { sppd ->
                                // Expressive card elevation & shape
                                Card(
                                    shape = RoundedCornerShape(28.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                    ),
                                    border = CardDefaults.outlinedCardBorder(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onItemClick(sppd) }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                                    modifier = Modifier.size(48.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Person,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                    }
                                                }
                                                Column(modifier = Modifier.padding(top = 2.dp)) {
                                                    Text(
                                                        text = sppd.nama,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "${sppd.jabatan} • NIP: ${sppd.nip}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                                onClick = { showDeleteDialog = sppd }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Hapus",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.padding(10.dp).size(20.dp)
                                                )
                                            }
                                        }

                                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CalendarToday,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = sppd.tanggal,
                                                    style = MaterialTheme.typography.labelLarge
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.LocationOn,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = sppd.lokasi,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = MaterialTheme.colorScheme.tertiary
                                                )
                                            }
                                        }

                                        if (sppd.deskripsi.isNotBlank()) {
                                            Text(
                                                text = sppd.deskripsi,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        if (sppd.foto.isNotBlank() || sppd.foto2.isNotBlank()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                if (sppd.foto.isNotBlank()) {
                                                    Surface(
                                                        shape = RoundedCornerShape(16.dp),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(140.dp),
                                                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                                                    ) {
                                                        AndroidView(
                                                            modifier = Modifier.fillMaxSize(),
                                                            factory = { ctx ->
                                                                ImageView(ctx).apply {
                                                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                                                }
                                                            },
                                                            update = { iv ->
                                                                Glide.with(iv.context).load(AppConstants.BASE_URL + sppd.foto).into(iv)
                                                            }
                                                        )
                                                    }
                                                }
                                                if (sppd.foto2.isNotBlank()) {
                                                    Surface(
                                                        shape = RoundedCornerShape(16.dp),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(140.dp),
                                                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                                                    ) {
                                                        AndroidView(
                                                            modifier = Modifier.fillMaxSize(),
                                                            factory = { ctx ->
                                                                ImageView(ctx).apply {
                                                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                                                }
                                                            },
                                                            update = { iv ->
                                                                Glide.with(iv.context).load(AppConstants.BASE_URL + sppd.foto2).into(iv)
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(72.dp)) // Extra padding for FAB
                            }
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
                    title = { Text("Hapus SPPD") },
                    text = { Text("Yakin ingin menghapus laporan dinas ini? Tindakan ini tidak dapat dibatalkan.") },
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

@Preview(showBackground = true)
@Composable
fun SPPDScreenPreview() {
    AppTheme {
        SPPDScreen(
            sppdList = listOf(
                SppdModel(
                    id = "1",
                    username = "rudissp",
                    nama = "Rudi Susanto, S.Pd.",
                    jabatan = "Guru Madya",
                    nip = "198501012010011001",
                    tanggal = "12 Oktober 2024",
                    lokasi = "Bandung",
                    deskripsi = "Menghadiri seminar Kurikulum Merdeka dan Workshop Pembelajaran Digital di Aula Dinas Pendidikan Provinsi.",
                    foto = "",
                    foto2 = ""
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