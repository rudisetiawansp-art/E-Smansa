package id.rudissp.esmansa.kaldik

import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppTheme
import id.rudissp.esmansa.LoadingIndicator
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

data class AgendaModel(
    val judul: String,
    val waktu: String
)

class Kaldik : ComponentActivity() {

    private val apiKey = "AIzaSyDMUXOGXDwtz5-yhcV2Oo8NpB9FqpGF3vw"
    private val calendarId = "2c13dee8396be174383342ba6abeff67b6aee566be14c1d99b4b2f9e2750a55a@group.calendar.google.com"

    private val semuaAgendaState = mutableStateMapOf<LocalDate, MutableList<AgendaModel>>()
    private var tanggalTerpilihState = mutableStateOf(LocalDate.now())
    private var currentMonthState = mutableStateOf(YearMonth.now())
    private var isLoadingState = mutableStateOf(true)
    private var isRefreshingState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        fetchCalendarData()

        setContent {
            AppTheme {
                KaldikScreen(
                    currentMonth = currentMonthState.value,
                    tanggalTerpilih = tanggalTerpilihState.value,
                    semuaAgenda = semuaAgendaState,
                    isLoading = isLoadingState.value,
                    isRefreshing = isRefreshingState.value,
                    onBackClick = { finish() },
                    onRefresh = {
                        isRefreshingState.value = true
                        fetchCalendarData()
                    },
                    onMonthChange = { newMonth -> currentMonthState.value = newMonth },
                    onDateSelected = { date -> tanggalTerpilihState.value = date }
                )
            }
        }
    }

    private fun fetchCalendarData() {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val cal = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.MONTH, -2)
        }
        val timeMinRaw = sdf.format(cal.time)
        val timeMinEncoded = URLEncoder.encode(timeMinRaw, "UTF-8")

        val url = "https://www.googleapis.com/calendar/v3/calendars/$calendarId/events?" +
                "key=$apiKey&singleEvents=true&orderBy=startTime&timeMin=$timeMinEncoded&maxResults=200"

        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                isLoadingState.value = false
                isRefreshingState.value = false
                try {
                    val items = response.getJSONArray("items")
                    semuaAgendaState.clear()

                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)
                        val judul = item.getString("summary")

                        val startObj = item.getJSONObject("start")
                        val endObj = item.getJSONObject("end")

                        val startStr = if (startObj.has("date")) {
                            startObj.getString("date")
                        } else {
                            startObj.getString("dateTime").substring(0, 10)
                        }

                        val endStr = if (endObj.has("date")) {
                            endObj.getString("date")
                        } else {
                            endObj.getString("dateTime").substring(0, 10)
                        }

                        val startDate = LocalDate.parse(startStr)
                        val endDate = LocalDate.parse(endStr)
                        val isAllDay = startObj.has("date")
                        val actualEndDate = if (isAllDay) endDate.minusDays(1) else endDate

                        var currentDate = startDate
                        while (!currentDate.isAfter(actualEndDate)) {
                            val waktuTampil = if (!isAllDay) {
                                startObj.getString("dateTime").substring(11, 16) + " WIB"
                            } else {
                                "Seharian"
                            }

                            if (!semuaAgendaState.containsKey(currentDate)) {
                                semuaAgendaState[currentDate] = mutableListOf()
                            }

                            if (semuaAgendaState[currentDate]?.none { it.judul == judul } == true) {
                                semuaAgendaState[currentDate]?.add(AgendaModel(judul, waktuTampil))
                            }

                            currentDate = currentDate.plusDays(1)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Kaldik", "Error parsing: ${e.message}")
                }
            },
            {
                isLoadingState.value = false
                isRefreshingState.value = false
                Toast.makeText(this, "Gagal memuat kalender akademik", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(jsonObjectRequest)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KaldikScreen(
    currentMonth: YearMonth,
    tanggalTerpilih: LocalDate,
    semuaAgenda: Map<LocalDate, List<AgendaModel>>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val formatterHeader = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("id-ID")) }
    val formatterDaftar = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID")) }

    val agendaHariIni = semuaAgenda[tanggalTerpilih] ?: emptyList()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Kalender Akademik",
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
                ExpressiveLoadingView(message = "Memuat kalender akademik...")
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
                        // Month Navigation Bar
                        item {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { onMonthChange(currentMonth.minusMonths(1)) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChevronLeft,
                                            contentDescription = "Bulan Sebelumnya",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    val locale = remember { Locale.getDefault() }
                                    Text(
                                        text = currentMonth.format(formatterHeader).uppercase(locale),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    IconButton(
                                        onClick = { onMonthChange(currentMonth.plusMonths(1)) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Bulan Berikutnya",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        // Calendar Grid Card
                        item {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Days of Week Header
                                    val daysOfWeek = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        daysOfWeek.forEach { dayName ->
                                            Text(
                                                text = dayName,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.outline,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                    // Month Days Grid
                                    MonthCalendarGrid(
                                        yearMonth = currentMonth,
                                        tanggalTerpilih = tanggalTerpilih,
                                        semuaAgenda = semuaAgenda,
                                        onDateSelected = onDateSelected
                                    )
                                }
                            }
                        }

                        // Agenda Section Header
                        item {
                            Text(
                                text = "Agenda: ${tanggalTerpilih.format(formatterDaftar)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Agenda List for Selected Date
                        if (agendaHariIni.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Tidak ada agenda pada tanggal ini.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(agendaHariIni) { agenda ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = agenda.judul,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = agenda.waktu,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
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
fun MonthCalendarGrid(
    yearMonth: YearMonth,
    tanggalTerpilih: LocalDate,
    semuaAgenda: Map<LocalDate, List<AgendaModel>>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val dayOfWeekValue = firstDayOfMonth.dayOfWeek.value // 1 (Mon) to 7 (Sun)
    val leadingSpaces = dayOfWeekValue - 1

    val totalCells = leadingSpaces + daysInMonth
    val totalRows = (totalCells + 6) / 7

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (row in 0 until totalRows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (col in 0 until 7) {
                    val dayIndex = row * 7 + col - leadingSpaces
                    if (dayIndex in 0 until daysInMonth) {
                        val date = yearMonth.atDay(dayIndex + 1)
                        val isSelected = date == tanggalTerpilih
                        val hasEvent = semuaAgenda.containsKey(date)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = (dayIndex + 1).toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )

                                if (hasEvent) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.primary
                                            )
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(7.dp))
                                }
                            }
                        }
                    } else {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
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
fun KaldikScreenPreview() {
    AppTheme {
        val mockAgenda = mapOf(
            LocalDate.now() to listOf(AgendaModel("Upacara Bendera", "07:00 WIB"), AgendaModel("Rapat Guru", "13:00 WIB"))
        )
        KaldikScreen(
            currentMonth = YearMonth.now(),
            tanggalTerpilih = LocalDate.now(),
            semuaAgenda = mockAgenda,
            isLoading = false,
            isRefreshing = false,
            onBackClick = {},
            onRefresh = {},
            onMonthChange = {},
            onDateSelected = {}
        )
    }
}
