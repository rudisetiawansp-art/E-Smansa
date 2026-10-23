package id.rudissp.esmansa.siswa

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.AppTheme
import id.rudissp.esmansa.LoadingIndicator
import id.rudissp.esmansa.R
import org.json.JSONArray
import java.net.URLEncoder

data class SiswaGW(
    val id: String,
    val nama: String,
    val nis: String,
    val kelas: String,
    val wali: String,
    val foto: String,
    val pass: String,
    val nisn: String,
    val device_id: String
)

class Siswa : ComponentActivity() {

    private lateinit var prefs: SharedPreferences
    private val siswaListState = mutableStateListOf<SiswaGW>()
    private val walikelasMapState = mutableStateMapOf<String, String>()

    private var isLoadingState = mutableStateOf(true)
    private var isRefreshingState = mutableStateOf(false)
    private var currentModeState = mutableStateOf("siswa_saya")
    private var sayawakelState = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        sayawakelState.value = prefs.getString("kelas", "") ?: ""

        loadData(currentModeState.value)

        setContent {
            AppTheme {
                SiswaScreen(
                    siswaList = siswaListState,
                    walikelasMap = walikelasMapState,
                    currentMode = currentModeState.value,
                    sayawakel = sayawakelState.value,
                    isLoading = isLoadingState.value,
                    isRefreshing = isRefreshingState.value,
                    onBackClick = { finish() },
                    onRefresh = {
                        isRefreshingState.value = true
                        loadData(currentModeState.value)
                    },
                    onModeChange = { mode ->
                        currentModeState.value = mode
                        loadData(mode)
                    },
                    onStudentClick = { siswa ->
                        val intent = Intent(this, EditSiswa::class.java).apply {
                            putExtra("id", siswa.id)
                            putExtra("nis", siswa.nis)
                            putExtra("nama", siswa.nama)
                            putExtra("pass", siswa.pass)
                            putExtra("kelas", siswa.kelas)
                            putExtra("guruwali", siswa.wali)
                            putExtra("device_id", siswa.device_id)
                            putExtra("nisn", siswa.nisn)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadData(currentModeState.value)
    }

    private fun loadData(mode: String) {
        isLoadingState.value = true
        siswaListState.clear()

        val namalengkap = prefs.getString("namalengkap", "") ?: ""
        val waliEncoded = URLEncoder.encode(namalengkap, "UTF-8")
        val sayawakel = prefs.getString("kelas", "") ?: ""
        val kelasEncoded = URLEncoder.encode(sayawakel, "UTF-8")

        val url = when (mode) {
            "siswa_saya" -> "${AppConstants.GETSISWABYWALI}?guruwali=$waliEncoded"
            "siswa_perwalian" -> "${AppConstants.GETSISWABYWAKEL}?kelas=$kelasEncoded"
            else -> AppConstants.GET_SISWA
        }

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val jsonArray = JSONArray(response)
                    val tempList = mutableListOf<SiswaGW>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        tempList.add(
                            SiswaGW(
                                id = obj.optString("id", ""),
                                nama = obj.optString("nama", ""),
                                nis = obj.optString("nis", ""),
                                kelas = obj.optString("kelas", ""),
                                wali = obj.optString("guruwali", ""),
                                foto = obj.optString("foto", ""),
                                pass = obj.optString("pass", ""),
                                nisn = obj.optString("nisn", ""),
                                device_id = obj.optString("device_id", "")
                            )
                        )
                    }

                    fetchAllWalikelas { walikelasMap ->
                        isLoadingState.value = false
                        isRefreshingState.value = false
                        walikelasMapState.clear()
                        walikelasMapState.putAll(walikelasMap)
                        siswaListState.clear()
                        siswaListState.addAll(tempList)
                    }

                } catch (e: Exception) {
                    isLoadingState.value = false
                    isRefreshingState.value = false
                    e.printStackTrace()
                }
            },
            { error ->
                isLoadingState.value = false
                isRefreshingState.value = false
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        request.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        Volley.newRequestQueue(this).add(request)
    }

    private fun fetchAllWalikelas(callback: (Map<String, String>) -> Unit) {
        val url = "https://sman1sukorejo.sch.id/smania/android/api_wakel.php"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val walikelasMap = mutableMapOf<String, String>()
                try {
                    val status = response.optString("status")
                    if (status == "success") {
                        val dataArray = response.optJSONArray("data")
                        if (dataArray != null) {
                            for (i in 0 until dataArray.length()) {
                                val item = dataArray.getJSONObject(i)
                                val kelas = item.optString("kelas", "")
                                val wakel = item.optString("walikelas", "Tidak diketahui")
                                if (kelas.isNotEmpty()) {
                                    walikelasMap[kelas] = wakel
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                callback(walikelasMap)
            },
            {
                callback(emptyMap())
            }
        )
        request.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        Volley.newRequestQueue(this).add(request)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiswaScreen(
    siswaList: List<SiswaGW>,
    walikelasMap: Map<String, String>,
    currentMode: String,
    sayawakel: String,
    isLoading: Boolean,
    isRefreshing: Boolean,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onModeChange: (String) -> Unit,
    onStudentClick: (SiswaGW) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }

    val filteredList = remember(searchQuery, siswaList) {
        if (searchQuery.isBlank()) siswaList
        else siswaList.filter {
            it.nama.contains(searchQuery, ignoreCase = true) ||
                    it.nis.contains(searchQuery, ignoreCase = true) ||
                    it.kelas.contains(searchQuery, ignoreCase = true)
        }
    }

    val groupedStudents = remember(filteredList) {
        filteredList.groupBy { it.kelas.ifBlank { "Lainnya" } }.toSortedMap()
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Expressive Large-ish Center Aligned Top Bar
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Direktori Siswa",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )

                // M3 Expressive SearchBar wrapper
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    SearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onSearch = { searchActive = false },
                                expanded = searchActive,
                                onExpandedChange = { searchActive = it },
                                placeholder = { Text("Cari nama, NIS, atau kelas...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                trailingIcon = {
                                    if (searchActive) {
                                        IconButton(
                                            onClick = {
                                                if (searchQuery.isNotEmpty()) {
                                                    searchQuery = ""
                                                } else {
                                                    searchActive = false
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                                        }
                                    }
                                }
                            )
                        },
                        expanded = searchActive,
                        onExpandedChange = { searchActive = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SearchBarDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredList) { siswa ->
                                ElevatedCard(
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                    ),
                                    elevation = CardDefaults.elevatedCardElevation(2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    ListItem(
                                        headlineContent = { Text(siswa.nama, fontWeight = FontWeight.ExtraBold) },
                                        supportingContent = { Text("Kelas: ${siswa.kelas} • NIS: ${siswa.nis}") },
                                        leadingContent = {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                                }
                                            }
                                        },
                                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                        modifier = Modifier.clickable {
                                            searchActive = false
                                            searchQuery = ""
                                            onStudentClick(siswa)
                                        }
                                    )
                                }
                            }
                        }
                    }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Expressive Custom Tabs / Segmented Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExpressiveTab(
                        text = "Siswa Asuh",
                        isSelected = currentMode == "siswa_saya",
                        onClick = { onModeChange("siswa_saya") }
                    )

                    if (sayawakel != "XXXX" && sayawakel.isNotBlank()) {
                        ExpressiveTab(
                            text = "Perwalian",
                            isSelected = currentMode == "siswa_perwalian",
                            onClick = { onModeChange("siswa_perwalian") }
                        )
                    }

                    ExpressiveTab(
                        text = "Semua Siswa",
                        isSelected = currentMode == "siswa_semua",
                        onClick = { onModeChange("siswa_semua") }
                    )
                }

                if (isLoading && !isRefreshing) {
                    ExpressiveLoadingView(message = "Memuat daftar siswa...")
                } else {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            if (filteredList.isEmpty()) {
                                item {
                                    EmptySiswaView(
                                        title = "Tidak Ada Siswa",
                                        description = "Belum ada data siswa yang tersedia untuk kategori ini."
                                    )
                                }
                            } else {
                                groupedStudents.forEach { (kelasName, studentsInClass) ->
                                    item(key = "group_$kelasName") {
                                        SiswaGroup(
                                            kelasName = kelasName,
                                            students = studentsInClass,
                                            walikelasMap = walikelasMap,
                                            onStudentClick = onStudentClick
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressiveTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "tabColor"
    )
    val contentColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "tabContentColor"
    )

    Surface(
        shape = CircleShape,
        color = containerColor,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }
}

@Composable
fun SiswaGroup(
    kelasName: String,
    students: List<SiswaGW>,
    walikelasMap: Map<String, String>,
    onStudentClick: (SiswaGW) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Expressive Class Header Banner
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Kelas $kelasName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${students.size} Siswa",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Grouped List Items
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                students.forEachIndexed { index, siswa ->
                    SiswaRowItem(
                        siswa = siswa,
                        walikelasMap = walikelasMap,
                        onClick = { onStudentClick(siswa) }
                    )
                    if (index < students.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SiswaRowItem(
    siswa: SiswaGW,
    walikelasMap: Map<String, String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wakelName = walikelasMap[siswa.kelas] ?: "Tidak diketahui"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val fotoUrl = "https://sman1sukorejo.sch.id/smania/fotoprofil/" + siswa.foto

        // Image with border and expressive rounding
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.surfaceContainerHighest),
            modifier = Modifier.size(56.dp)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                factory = { ctx ->
                    ImageView(ctx).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                },
                update = { imageView ->
                    Glide.with(imageView.context)
                        .load(fotoUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_keluar)
                        .into(imageView)
                }
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = siswa.nama,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "NIS: ${siswa.nis} • NISN: ${siswa.nisn.ifBlank { "-" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Wali Kelas: $wakelName",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptySiswaView(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

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
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading infinite transition")
    val animatedProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
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
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 8.dp,
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
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SiswaScreenPreview() {
    AppTheme {
        val mockList = listOf(
            SiswaGW("1", "Ahmad Fauzi", "10291", "X 1", "Budi Santoso", "foto.jpg", "1234", "00812345", "dev1"),
            SiswaGW("2", "Siti Aminah", "10292", "X 2", "Siti Rahma", "foto2.jpg", "1234", "00812346", "dev2")
        )
        val mockWakel = mapOf("X 1" to "Budi, S.Pd", "X 2" to "Ani, M.Pd")
        SiswaScreen(
            siswaList = mockList,
            walikelasMap = mockWakel,
            currentMode = "siswa_saya",
            sayawakel = "X 1",
            isLoading = false,
            isRefreshing = false,
            onBackClick = {},
            onRefresh = {},
            onModeChange = {},
            onStudentClick = {}
        )
    }
}

// Legacy Adapter kept for compatibility with other classes (e.g. PresensiSiswa)
class SiswaGWAdapter(
    private val context: android.content.Context,
    private var siswaList: ArrayList<SiswaGW>,
    private var walikelasMap: Map<String, String> = emptyMap()
) : RecyclerView.Adapter<SiswaGWAdapter.SiswaViewHolder>(), Filterable {

    private var siswaListFull: ArrayList<SiswaGW> = ArrayList(siswaList)

    class SiswaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaText: TextView = itemView.findViewById(R.id.item_gwlist_nama)
        val kelasText: TextView = itemView.findViewById(R.id.item_gwlist_kelas)
        val fotoText: ShapeableImageView = itemView.findViewById(R.id.item_gwlist_foto)
        val wakel: TextView = itemView.findViewById(R.id.item_gwlist_wakel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiswaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gwlist, parent, false)
        return SiswaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SiswaViewHolder, position: Int) {
        val siswa = siswaList[position]
        holder.namaText.text = siswa.nama + " - " + siswa.kelas
        holder.kelasText.text = "Guru Wali: " + siswa.wali
        holder.wakel.text = "Wali Kelas: " + (walikelasMap[siswa.kelas] ?: "Tidak diketahui")
        holder.wakel.visibility = View.VISIBLE

        val urlFoto = "https://sman1sukorejo.sch.id/smania/fotoprofil/" + siswa.foto
        Glide.with(holder.itemView.context)
            .load(urlFoto)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_keluar)
            .into(holder.fotoText)
    }

    fun updateData(newList: ArrayList<SiswaGW>) {
        siswaList = newList
        siswaListFull = ArrayList(newList)
        notifyDataSetChanged()
    }

    fun setWalikelasMap(map: Map<String, String>) {
        this.walikelasMap = map
    }

    override fun getItemCount(): Int = siswaList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = ArrayList<SiswaGW>()
                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(siswaListFull)
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    for (item in siswaListFull) {
                        if (item.nama.lowercase().contains(filterPattern) ||
                            item.nis.lowercase().contains(filterPattern) ||
                            item.kelas.lowercase().contains(filterPattern)
                        ) {
                            filteredList.add(item)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                siswaList.clear()
                siswaList.addAll(results?.values as ArrayList<SiswaGW>)
                notifyDataSetChanged()
            }
        }
    }
}