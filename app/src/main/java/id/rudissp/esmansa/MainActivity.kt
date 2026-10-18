package id.rudissp.esmansa

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.AirplaneTicket
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.DoorBack
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.NoteAlt
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material.icons.rounded.ViewTimeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessaging
import id.rudissp.esmansa.admin.AdminActivity
import id.rudissp.esmansa.admin.EditUserActivity
import id.rudissp.esmansa.buku.BukuActivity
import id.rudissp.esmansa.gw.GWJurnal
import id.rudissp.esmansa.jadwal.JadwalV2
import id.rudissp.esmansa.jurnal.JurnalActivity
import id.rudissp.esmansa.jurnal.JurnalKelasActivity
import id.rudissp.esmansa.jurtu.Jurtu
import id.rudissp.esmansa.jurtu.JurtuRekap
import id.rudissp.esmansa.kaldik.Kaldik
import id.rudissp.esmansa.kampus.Jurusan
import id.rudissp.esmansa.lapor.Lapor
import id.rudissp.esmansa.link.LInkActivity
import id.rudissp.esmansa.lms.LMS
import id.rudissp.esmansa.metu.MetuActivity
import id.rudissp.esmansa.pantau.Pantau
import id.rudissp.esmansa.piket.Piket
import id.rudissp.esmansa.pray.Pray
import id.rudissp.esmansa.presensi.PresensiActivity
import id.rudissp.esmansa.presensi.siswa.PresensiSiswa
import id.rudissp.esmansa.siswa.Siswa
import id.rudissp.esmansa.sppd.SPPDActivity
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class MenuItemData(
    val title: String,
    val iconRes: ImageVector,
    val onClick: () -> Unit
)

class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    // Reactive State Variables
    private val namaLengkapState = mutableStateOf("")
    private val roleState = mutableStateOf("")
    private val jenisState = mutableStateOf("")
    private val nipState = mutableStateOf("")
    private val usernameState = mutableStateOf("")
    private val statusState = mutableStateOf("")
    private val walikelasState = mutableStateOf("")

    private val eventHariIniState = mutableStateOf<String?>(null)
    private val eventMendatangState = mutableStateOf<String?>(null)
    private val isEventLoadingState = mutableStateOf(true)
    private val eventErrorState = mutableStateOf<String?>(null)

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { entry ->
            val permissionName = entry.key
            val isGranted = entry.value
            Log.d("PermissionCheck", "$permissionName diizinkan: $isGranted")
            if (permissionName == android.Manifest.permission.CAMERA && isGranted) {
                Log.d("CameraPerm", "Kamera siap digunakan!")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)

        // Read stored user details
        namaLengkapState.value = prefs.getString("namalengkap", "") ?: ""
        roleState.value = prefs.getString("role", "") ?: ""
        jenisState.value = prefs.getString("jenis", "") ?: ""
        nipState.value = prefs.getString("nip", "") ?: ""
        usernameState.value = prefs.getString("username", "") ?: ""
        statusState.value = prefs.getString("status", "") ?: ""
        walikelasState.value = prefs.getString("kelas", "") ?: ""

        askPermission()
        loadEventTerdekatDashboard()
        ceksayawalas(namaLengkapState.value)

        FirebaseMessaging.getInstance().subscribeToTopic("allUsers")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Subscribed to allUsers")
                } else {
                    Log.e("FCM", "Subscribe failed")
                }
            }

        setContent {
            AppTheme {
                MainDashboardScreen(
                    namaLengkap = namaLengkapState.value,
                    role = roleState.value,
                    jenis = jenisState.value,
                    nip = nipState.value,
                    username = usernameState.value,
                    status = statusState.value,
                    walikelas = walikelasState.value,
                    eventHariIni = eventHariIniState.value,
                    eventMendatang = eventMendatangState.value,
                    isEventLoading = isEventLoadingState.value,
                    eventError = eventErrorState.value,
                    prefs = prefs,
                    onOpenKaldik = { startActivity(Intent(this, Kaldik::class.java)) },
                    onEditProfile = {
                        val intent = Intent(this, EditUserActivity::class.java).apply {
                            putExtra("username", usernameState.value)
                            putExtra("mode", "edit")
                        }
                        startActivity(intent)
                    },
                    onLogout = { logout() }
                )
            }
        }
    }

    private fun ceksayawalas(walikelas: String?) {
        if (walikelas.isNullOrBlank()) return
        val encodedKelas = URLEncoder.encode(walikelas, "UTF-8")
        val url = "https://sman1sukorejo.sch.id/smania/android/api_sayawakel.php?walikelas=$encodedKelas"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val status = response.getString("status")
                    if (status == "success") {
                        val data = response.getJSONObject("data")
                        val kelas = data.getString("kelas")
                        walikelasState.value = kelas
                        prefs.edit { putString("kelas", kelas) }
                    } else {
                        walikelasState.value = ""
                        prefs.edit { putString("kelas", "XXXX") }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun loadEventTerdekatDashboard() {
        val queue = Volley.newRequestQueue(this)
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val timeMinRaw = sdf.format(Date())
        val timeMinEncoded = URLEncoder.encode(timeMinRaw, "UTF-8")

        val url = "https://www.googleapis.com/calendar/v3/calendars/2c13dee8396be174383342ba6abeff67b6aee566be14c1d99b4b2f9e2750a55a@group.calendar.google.com/events?key=AIzaSyDMUXOGXDwtz5-yhcV2Oo8NpB9FqpGF3vw&timeMin=$timeMinEncoded&orderBy=startTime&singleEvents=true"

        isEventLoadingState.value = true
        eventErrorState.value = null

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                isEventLoadingState.value = false
                try {
                    val items = response.getJSONArray("items")
                    val hariIni = LocalDate.now()

                    var foundHariIni: String? = null
                    var foundMendatang: String? = null

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

                        if (foundHariIni == null && !hariIni.isBefore(startDate) && !hariIni.isAfter(actualEndDate)) {
                            foundHariIni = judul
                        }

                        if (foundMendatang == null && startDate.isAfter(hariIni)) {
                            val formatTanggal = startDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID")))
                            foundMendatang = "$judul ($formatTanggal)"
                        }

                        if (foundHariIni != null && foundMendatang != null) {
                            break
                        }
                    }

                    eventHariIniState.value = foundHariIni
                    eventMendatangState.value = foundMendatang
                } catch (e: Exception) {
                    eventErrorState.value = "Gagal memuat agenda"
                    Log.e("DASHBOARD_KALENDER", "Error parsing: ${e.message}")
                }
            },
            { error ->
                isEventLoadingState.value = false
                eventErrorState.value = "Gagal terhubung"
                val statusCode = error.networkResponse?.statusCode
                Log.e("DASHBOARD_KALENDER", "Error Volley: ${error.message}, Status Code: $statusCode")
            }
        )

        queue.add(jsonObjectRequest)
    }

    private fun logout() {
        prefs.edit { clear() }
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun askPermission() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (!notificationManager.areNotificationsEnabled()) {
                permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.CAMERA)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    namaLengkap: String,
    role: String,
    jenis: String,
    nip: String,
    username: String,
    status: String,
    walikelas: String,
    eventHariIni: String?,
    eventMendatang: String?,
    isEventLoading: Boolean,
    eventError: String?,
    prefs: SharedPreferences? = null,
    onOpenKaldik: () -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    // Accordion Expansion State backed by SharedPreferences
    var isKurikulumExpanded by remember {
        mutableStateOf(prefs?.getBoolean("is_kurikulum_expanded", true) ?: true)
    }
    var isKesiswaanExpanded by remember {
        mutableStateOf(prefs?.getBoolean("is_kesiswaan_expanded", false) ?: false)
    }
    var isKepegawaianExpanded by remember {
        mutableStateOf(prefs?.getBoolean("is_kepegawaian_expanded", false) ?: false)
    }
    var isLainExpanded by remember {
        mutableStateOf(prefs?.getBoolean("is_lain_expanded", false) ?: false)
    }

    var showProfileSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Menu Item Definitions
    val kurikulumItems = remember {
        listOf(
            MenuItemData("Jurnal", Icons.Rounded.NoteAlt) {
                val intent = Intent(context, JurnalActivity::class.java).apply {
                    putExtra("mode", "individu")
                }
                context.startActivity(intent)
            },
            MenuItemData("Pantau", Icons.Rounded.RemoveRedEye) {
                context.startActivity(Intent(context, Pantau::class.java))
            },
            MenuItemData("Jur. Kelas", Icons.Rounded.Groups) {
                context.startActivity(Intent(context, JurnalKelasActivity::class.java))
            },
            MenuItemData("LMS", Icons.Rounded.School) {
                context.startActivity(Intent(context, LMS::class.java))
            },
            MenuItemData("Jadwal", Icons.Rounded.ViewTimeline) {
                context.startActivity(Intent(context, JadwalV2::class.java))
            },
            MenuItemData("Piket", Icons.Rounded.Group) {
                context.startActivity(Intent(context, Piket::class.java))
            }
        )
    }

    val kesiswaanItems = remember {
        listOf(
            MenuItemData("Siswa", Icons.Rounded.Person) {
                context.startActivity(Intent(context, Siswa::class.java))
            },
            MenuItemData("Presensi\nSiswa", Icons.Rounded.Fingerprint) {
                context.startActivity(Intent(context, PresensiSiswa::class.java))
            },
            MenuItemData("G. Wali", Icons.Rounded.GroupAdd) {
                context.startActivity(Intent(context, GWJurnal::class.java))
            },
            MenuItemData("Izin", Icons.Rounded.DoorBack) {
                context.startActivity(Intent(context, MetuActivity::class.java))
            },
            MenuItemData("Laporan", Icons.Rounded.Error) {
                context.startActivity(Intent(context, Lapor::class.java))
            },
            MenuItemData("Ibadah", Icons.Rounded.Mosque) {
                context.startActivity(Intent(context, Pray::class.java))
            }
        )
    }

    val kepegawaianItems = remember(role) {
        val list = mutableListOf(
            MenuItemData("Presensi", Icons.Rounded.Fingerprint) {
                context.startActivity(Intent(context, PresensiActivity::class.java))
            },
            MenuItemData("Tendik", Icons.Rounded.NoteAlt) {
                context.startActivity(Intent(context, Jurtu::class.java))
            },
            MenuItemData("Rekap", Icons.AutoMirrored.Rounded.ListAlt) {
                context.startActivity(Intent(context, JurtuRekap::class.java))
            },
            MenuItemData("SPPD", Icons.Rounded.TravelExplore) {
                context.startActivity(Intent(context, SPPDActivity::class.java))
            }
        )
        if (role.equals("admin", ignoreCase = true)) {
            list.add(
                MenuItemData("Admin", Icons.Rounded.AdminPanelSettings) {
                    context.startActivity(Intent(context, AdminActivity::class.java))
                }
            )
        }
        list
    }

    val lainItems = remember {
        listOf(
            MenuItemData("Jurusan", Icons.Rounded.School) {
                context.startActivity(Intent(context, Jurusan::class.java))
            },
            MenuItemData("Buku", Icons.Rounded.Book) {
                context.startActivity(Intent(context, BukuActivity::class.java))
            },
            MenuItemData("Link", Icons.Rounded.Link) {
                context.startActivity(Intent(context, LInkActivity::class.java))
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Material 3 Expressive Hero Header
            ExpressiveHeroHeader(
                namaLengkap = namaLengkap,
                nip = nip,
                status = status,
                jenis = jenis,
                walikelas = walikelas,
                onProfileClick = { showProfileSheet = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Kaldik & Event Showcase Card
            KaldikAgendaCard(
                eventHariIni = eventHariIni,
                eventMendatang = eventMendatang,
                isLoading = isEventLoading,
                errorMessage = eventError,
                onClick = onOpenKaldik,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Kurikulum (Visible if jenis == "Guru")
            if (jenis.equals("Guru", ignoreCase = true)) {
                ExpandableSectionCard(
                    title = "Kurikulum",
                    iconRes = R.drawable.icm_jurnal,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                    iconTint = MaterialTheme.colorScheme.primary,
                    badgeCount = kurikulumItems.size,
                    isExpanded = isKurikulumExpanded,
                    onToggleExpand = {
                        isKurikulumExpanded = !isKurikulumExpanded
                        prefs?.edit { putBoolean("is_kurikulum_expanded", isKurikulumExpanded) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    MenuGrid(items = kurikulumItems)
                }
            }

            // Section 2: Kesiswaan
            ExpandableSectionCard(
                title = "Kesiswaan",
                iconRes = R.drawable.icm_person,
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                iconTint = MaterialTheme.colorScheme.secondary,
                badgeCount = kesiswaanItems.size,
                isExpanded = isKesiswaanExpanded,
                onToggleExpand = {
                    isKesiswaanExpanded = !isKesiswaanExpanded
                    prefs?.edit { putBoolean("is_kesiswaan_expanded", isKesiswaanExpanded) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                MenuGrid(items = kesiswaanItems)
            }

            // Section 3: Tendik & Kepegawaian
            ExpandableSectionCard(
                title = "Tendik & Kepegawaian",
                iconRes = R.drawable.icm_fingger,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f),
                iconTint = MaterialTheme.colorScheme.tertiary,
                badgeCount = kepegawaianItems.size,
                isExpanded = isKepegawaianExpanded,
                onToggleExpand = {
                    isKepegawaianExpanded = !isKepegawaianExpanded
                    prefs?.edit { putBoolean("is_kepegawaian_expanded", isKepegawaianExpanded) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                MenuGrid(items = kepegawaianItems)
            }

            // Section 4: Lain - Lain
            ExpandableSectionCard(
                title = "Lain - Lain",
                iconRes = R.drawable.icm_kampus,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                iconTint = MaterialTheme.colorScheme.primary,
                badgeCount = lainItems.size,
                isExpanded = isLainExpanded,
                onToggleExpand = {
                    isLainExpanded = !isLainExpanded
                    prefs?.edit { putBoolean("is_lain_expanded", isLainExpanded) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                MenuGrid(items = lainItems)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Section
            ExpressiveFooter(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp)
            )
        }

        // Material 3 Expressive Profile Bottom Sheet
        if (showProfileSheet) {
            ProfileBottomSheet(
                namaLengkap = namaLengkap,
                nip = nip,
                role = role,
                jenis = jenis,
                username = username,
                status = status,
                walikelas = walikelas,
                onDismiss = { showProfileSheet = false },
                onEditProfile = {
                    showProfileSheet = false
                    onEditProfile()
                },
                onPromptLogout = {
                    showProfileSheet = false
                    showLogoutDialog = true
                }
            )
        }

        // Confirmation Dialog for Logout
        if (showLogoutDialog) {
            LogoutConfirmDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    showLogoutDialog = false
                    onLogout()
                }
            )
        }
    }
}

/**
 * Material 3 Expressive Hero Header:
 * Dynamic Greeting, Bold Display Name, NIP / Role / Walikelas Badges, Expressive Avatar Button
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpressiveHeroHeader(
    namaLengkap: String,
    nip: String,
    status: String,
    jenis: String,
    walikelas: String,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = remember(currentHour) {
        when (currentHour) {
            in 4..10 -> "Selamat Pagi 🌅"
            in 11..14 -> "Selamat Siang ☀️"
            in 15..17 -> "Selamat Sore 🌤️"
            else -> "Selamat Malam 🌙"
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = namaLengkap.ifBlank { "Pengguna E-Smansa" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Pill Badges Row
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (nip.isNotBlank() && nip != "-") {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.20f)
                            ) {
                                Text(
                                    text = "NIP. $nip",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (walikelas.isNotBlank() && walikelas != "XXXX") {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "Wali Kelas: $walikelas",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (status.isNotBlank() || jenis.isNotBlank()) {
                            val roleText = listOf(status, jenis).filter { it.isNotBlank() }.joinToString(" • ")
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = roleText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Profile Avatar Button (Expressive circular ring)
                Surface(
                    onClick = onProfileClick,
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.22f),
                    border = BorderStroke(2.5.dp, MaterialTheme.colorScheme.onPrimary),
                    shadowElevation = 3.dp,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profil",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Kaldik Agenda Widget:
 * Displays active and upcoming events with real-time status pills and an interactive shortcut to Kaldik.
 */
@Composable
fun KaldikAgendaCard(
    eventHariIni: String?,
    eventMendatang: String?,
    isLoading: Boolean,
    errorMessage: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = "Kalender",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Kaldik SMAN 1 Sukorejo",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ketuk untuk melihat agenda lengkap",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Buka",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Memuat agenda kalender...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
                eventHariIni == null && eventMendatang == null -> {
                    Text(
                        text = "Tidak ada agenda terdekat dalam waktu dekat",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        eventHariIni?.let { event ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .graphicsLayer { alpha = pulseAlpha }
                                            .background(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            )
                                    )
                                    Text(
                                        text = "Hari Ini:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = event,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        eventMendatang?.let { event ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Mendatang:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = event,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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

/**
 * Expandable Section Accordion Card:
 * Smooth animated expand/collapse, category badge, rotated chevron, and responsive menu grid.
 */
@Composable
fun ExpandableSectionCard(
    title: String,
    iconRes: Int,
    containerColor: Color,
    iconTint: Color,
    badgeCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 0f else -90f,
        animationSpec = spring(stiffness = 400f),
        label = "rotation"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = containerColor,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = title,
                                tint = iconTint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = "$badgeCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Filled.ArrowCircleDown,
                    contentDescription = if (isExpanded) "Tutup" else "Buka",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer { rotationZ = rotationAngle }
                )
            }

            // Expandable Grid Body
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(200)) + expandVertically(
                    animationSpec = spring(stiffness = 400f)
                ),
                exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(
                    animationSpec = spring(stiffness = 400f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    content()
                }
            }
        }
    }
}

/**
 * 3-Column Menu Grid for Category Items
 */
@Composable
fun MenuGrid(
    items: List<MenuItemData>,
    modifier: Modifier = Modifier
) {
    val rows = items.chunked(3)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (item in row) {
                    Box(modifier = Modifier.weight(1f)) {
                        ExpressiveMenuCard(item = item)
                    }
                }
                // Fill blank spaces if the row has fewer than 3 items
                if (row.size < 3) {
                    for (i in 0 until (3 - row.size)) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Expressive Menu Card:
 * Tactile spring scale animation, rounded container, distinct icon badge, clear multi-line label.
 */
@Composable
fun ExpressiveMenuCard(
    item: MenuItemData,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1.0f)

    Card(
        onClick = item.onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        // Hilangkan shadow elevation
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(108.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.iconRes,
                        contentDescription = item.title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}

/**
 * Material 3 Expressive Profile Bottom Sheet:
 * User Details, NIP, Roles, Edit Profile button, and Logout trigger.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileBottomSheet(
    namaLengkap: String,
    nip: String,
    role: String,
    jenis: String,
    username: String,
    status: String,
    walikelas: String,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit,
    onPromptLogout: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large Avatar Ring
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                shadowElevation = 4.dp,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.icm_person),
                        contentDescription = "Avatar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = namaLengkap.ifBlank { "Pegawai E-Smansa" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            if (nip.isNotBlank() && nip != "-") {
                Text(
                    text = "NIP. $nip",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // User Info Detail Card
            OutlinedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileInfoRow(label = "Jabatan / Profesi", value = "$status - $jenis")
                    if (username.isNotBlank()) {
                        ProfileInfoRow(label = "Username", value = "@$username")
                    }
                    if (walikelas.isNotBlank() && walikelas != "XXXX") {
                        ProfileInfoRow(label = "Wali Kelas", value = walikelas)
                    }
                    if (role.isNotBlank()) {
                        ProfileInfoRow(label = "Hak Akses", value = role.replaceFirstChar { it.uppercase() })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = onEditProfile,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text(text = "Edit Profil", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onPromptLogout,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text(text = "Logout", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
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

/**
 * Material 3 Expressive Confirmation Dialog for Logout
 */
@Composable
fun LogoutConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "Keluar dari Akun?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Sesi Anda akan diakhiri dan data sesi login pada perangkat ini akan dihapus. Anda perlu memasukkan kredensial login kembali.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(text = "Ya, Logout", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(text = "Batal", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

/**
 * Expressive Footer displaying App Version and Copyright information
 */
@Composable
fun ExpressiveFooter(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
        ) {
            Text(
                text = stringResource(id = R.string.versiapp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

// ==================== COMPOSE PREVIEWS ====================

@Preview(name = "Dashboard Guru (Light)", showBackground = true, showSystemUi = true)
@Composable
fun MainDashboardGuruPreview() {
    AppTheme {
        MainDashboardScreen(
            namaLengkap = "Rudi Setiawan S.P., S.Pd",
            role = "admin",
            jenis = "Guru",
            nip = "199208052019021004",
            username = "rudissp",
            status = "ASN",
            walikelas = "XII 1",
            eventHariIni = "Penilaian Akhir Semester (PAS)",
            eventMendatang = "Classmeeting (20 Oktober 2026)",
            isEventLoading = false,
            eventError = null,
            prefs = null,
            onOpenKaldik = {},
            onEditProfile = {},
            onLogout = {}
        )
    }
}