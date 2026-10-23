package id.rudissp.esmansa.jadwal

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rudissp.esmansa.LoadingIndicator

data class SubjectColorScheme(
    val containerColor: Color,
    val contentColor: Color,
    val accentColor: Color,
)

fun getSubjectColorScheme(mapel: String, status: String): SubjectColorScheme {
    val isKosong = status.equals("kosong", ignoreCase = true) || mapel.isBlank() || mapel == "-"
    if (isKosong) {
        return SubjectColorScheme(
            containerColor = Color(0xFFF1F3F4),
            contentColor = Color(0xFF5F6368),
            accentColor = Color(0xFF80868B)
        )
    }
    val palettes = listOf(
        SubjectColorScheme(Color(0xFFE8F0FE), Color(0xFF1967D2), Color(0xFF1A73E8)), // Blue
        SubjectColorScheme(Color(0xFFE6F4EA), Color(0xFF137333), Color(0xFF1E8E3E)), // Green
        SubjectColorScheme(Color(0xFFE0F7FA), Color(0xFF00796B), Color(0xFF00897B)), // Teal
        SubjectColorScheme(Color(0xFFF3E8FF), Color(0xFF6B21A8), Color(0xFF8B5CF6)), // Purple
        SubjectColorScheme(Color(0xFFFCE8E6), Color(0xFFC5221F), Color(0xFFD9381E)), // Red
        SubjectColorScheme(Color(0xFFFEF7E0), Color(0xFFB06000), Color(0xFFE37400)), // Amber
        SubjectColorScheme(Color(0xFFFCE4EC), Color(0xFFC2185B), Color(0xFFE91E63)), // Pink
        SubjectColorScheme(Color(0xFFFBE9E7), Color(0xFFD84315), Color(0xFFF4511E)), // Deep Orange
        SubjectColorScheme(Color(0xFFF0F4C3), Color(0xFF827717), Color(0xFF9E9D24)), // Lime
        SubjectColorScheme(Color(0xFFEFEBE9), Color(0xFF4E342E), Color(0xFF6D4C41))  // Brown
    )
    val hash = kotlin.math.abs(mapel.trim().lowercase().hashCode())
    return palettes[hash % palettes.size]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JadwalScreen(
    title: String,
    listHari: List<HariModel>,
    listPencarian: List<String>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onSelectTarget: (String) -> Unit,
    onResetToMyJadwal: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }

    val filteredList = remember(searchQuery, listPencarian) {
        if (searchQuery.isBlank()) listPencarian
        else listPencarian.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Material 3 SearchBar component
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onSearch = {
                                searchActive = false
                                if (searchQuery.isNotBlank()) {
                                    onSelectTarget(searchQuery)
                                }
                            },
                            expanded = searchActive,
                            onExpandedChange = { searchActive = it },
                            placeholder = { Text("Cari guru atau kelas...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredList) { item ->
                            ListItem(
                                headlineContent = { Text(item, fontWeight = FontWeight.Bold) },
                                leadingContent = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchActive = false
                                        searchQuery = ""
                                        onSelectTarget(item)
                                    }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading && !isRefreshing) {
                    ExpressiveLoadingView(message = "Memuat jadwal pelajaran...")
                } else {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (listHari.isEmpty()) {
                            EmptyJadwalView(
                                title = "Jadwal Kosong",
                                description = "Belum ada data jadwal pelajaran yang tersedia untuk saat ini."
                            )
                        } else {
                            JadwalGroupedList(
                                title = title,
                                listHari = listHari,
                                onOpenSearch = { searchActive = true },
                                onResetToMyJadwal = onResetToMyJadwal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JadwalHeroSummaryCard(
    title: String,
    listHari: List<HariModel>,
    onOpenSearch: () -> Unit,
    onResetToMyJadwal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalHari = listHari.size
    val totalSesi = listHari.sumOf { it.listJadwal.size }
    val totalKBM = listHari.sumOf { hari -> hari.listJadwal.count { !it.status.equals("kosong", ignoreCase = true) && it.mapel != "-" } }

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
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Jadwal Pelajaran",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "$totalHari Hari",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
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
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Column {
                            Text("Total Sesi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$totalSesi Sesi", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                        Column {
                            Text("Mapel KBM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("$totalKBM Mapel", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenSearch,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cari Guru / Kelas", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = onResetToMyJadwal,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Jadwal Saya", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

fun getCurrentIndonesianDayName(): String {
    val calendar = java.util.Calendar.getInstance()
    return when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
        java.util.Calendar.MONDAY -> "Senin"
        java.util.Calendar.TUESDAY -> "Selasa"
        java.util.Calendar.WEDNESDAY -> "Rabu"
        java.util.Calendar.THURSDAY -> "Kamis"
        java.util.Calendar.FRIDAY -> "Jumat"
        java.util.Calendar.SATURDAY -> "Sabtu"
        java.util.Calendar.SUNDAY -> "Minggu"
        else -> "Senin"
    }
}

@Composable
fun JadwalGroupedList(
    title: String,
    listHari: List<HariModel>,
    onOpenSearch: () -> Unit,
    onResetToMyJadwal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val todayName = remember { getCurrentIndonesianDayName() }
    var selectedDay by remember(listHari) {
        mutableStateOf(
            listHari.firstOrNull { it.namaHari.equals(todayName, ignoreCase = true) }?.namaHari
                ?: listHari.firstOrNull()?.namaHari ?: ""
        )
    }

    val currentHariModel = listHari.find { it.namaHari.equals(selectedDay, ignoreCase = true) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            JadwalHeroSummaryCard(
                title = title,
                listHari = listHari,
                onOpenSearch = onOpenSearch,
                onResetToMyJadwal = onResetToMyJadwal
            )
        }

        // Day Selector Tabs Row
        if (listHari.isNotEmpty()) {
            item {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listHari.forEach { hari ->
                        val isSelected = hari.namaHari.equals(selectedDay, ignoreCase = true)
                        Surface(
                            onClick = { selectedDay = hari.namaHari },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = hari.namaHari,
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

        // Selected Day Schedule Items
        if (currentHariModel != null) {
            item {
                JadwalDayGroup(hariModel = currentHariModel)
            }
        } else {
            item {
                EmptyJadwalView(
                    title = "Tidak Ada Jadwal",
                    description = "Tidak ada jadwal pelajaran untuk hari $selectedDay."
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun JadwalDayGroup(
    hariModel: HariModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Day Header Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = hariModel.namaHari,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${hariModel.listJadwal.size} Sesi",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Schedule Items (Grouped List without Cards)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                hariModel.listJadwal.forEachIndexed { index, jadwal ->
                    JadwalRowItem(jadwal = jadwal)
                    if (index < hariModel.listJadwal.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JadwalRowItem(
    jadwal: JadwalItemModel,
    modifier: Modifier = Modifier,
) {
    val isKosong = jadwal.status.equals("kosong", ignoreCase = true) || jadwal.mapel == "-"
    val colorScheme = getSubjectColorScheme(jadwal.mapel, jadwal.status)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Jam Ke Indicator Pill
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isKosong) MaterialTheme.colorScheme.surfaceVariant else colorScheme.accentColor,
            modifier = Modifier.size(width = 46.dp, height = 46.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Jam",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = if (isKosong) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = jadwal.jamKe.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isKosong) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                    )
                }
            }
        }

        // Subject & Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isKosong) "Jam Kosong" else jadwal.mapel,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isKosong) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!isKosong) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colorScheme.containerColor
                    ) {
                        Text(
                            text = "KBM",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.contentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (jadwal.infoBawah.isNotBlank()) {
                Text(
                    text = jadwal.infoBawah,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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

@Composable
fun EmptyJadwalView(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp),
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
                        imageVector = Icons.Rounded.Schedule,
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun JadwalHeroSummaryCardPreview() {
    id.rudissp.esmansa.AppTheme {
        val mockHari = listOf(
            HariModel("Senin", listOf(JadwalItemModel(1, "hadir", "Matematika", "X 1"), JadwalItemModel(2, "hadir", "Fisika", "X 1"))),
            HariModel("Selasa", listOf(JadwalItemModel(1, "kosong", "-", "-")))
        )
        Box(modifier = Modifier.padding(16.dp)) {
            JadwalHeroSummaryCard(
                title = "Jadwal Pelajaran X 1",
                listHari = mockHari,
                onOpenSearch = {},
                onResetToMyJadwal = {}
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun JadwalRowItemPreview() {
    id.rudissp.esmansa.AppTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            JadwalRowItem(
                jadwal = JadwalItemModel(jamKe = 3, status = "hadir", mapel = "Matematika Lanjutan", infoBawah = "XII IPA 1")
            )
            JadwalRowItem(
                jadwal = JadwalItemModel(jamKe = 4, status = "kosong", mapel = "-", infoBawah = "")
            )
        }
    }
}
