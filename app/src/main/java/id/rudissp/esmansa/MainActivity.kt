@file:OptIn(
    ExperimentalMaterial3Api::class
)

package id.rudissp.esmansa

import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Class
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoorBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.NoteAlt
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.ViewTimeline
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
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
import kotlinx.coroutines.delay
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

    val scheme = MaterialTheme.colorScheme
    val isGuru = jenis.equals("Guru", ignoreCase = true)

    val categories: List<Pair<DashTab, List<MenuItemData>>> = buildList {
        if (isGuru) add(DashTab.Kurikulum to kurikulumItems)
        add(DashTab.Kesiswaan to kesiswaanItems)
        add(DashTab.Kepegawaian to kepegawaianItems)
        add(DashTab.Lainnya to lainItems)
    }
    val allEntries = categories.flatMap { (t, items) -> items.map { MenuEntry(it, t) } }
    val tabs = listOf(DashTab.Home) + categories.map { it.first }

    var tab by rememberSaveable { mutableStateOf(DashTab.fromKey(prefs?.getString("dash_tab", null))) }
    val activeTab = if (tab == DashTab.Kurikulum && !isGuru) DashTab.Home else tab
    var query by rememberSaveable { mutableStateOf("") }
    var showProfileSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    fun selectTab(target: DashTab) {
        tab = target
        query = ""
        prefs?.edit { putString("dash_tab", target.key) }
    }

    BackHandler(enabled = activeTab != DashTab.Home) { selectTab(DashTab.Home) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.surface)
    ) {
        AnimatedContent(
            targetState = activeTab,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                (fadeIn(ExpressiveMotion.effects()) +
                        slideInHorizontally(ExpressiveMotion.spatial()) { if (forward) it / 5 else -it / 5 }) togetherWith
                        (fadeOut(ExpressiveMotion.fastEffects()) +
                                slideOutHorizontally(ExpressiveMotion.spatial()) { if (forward) -it / 5 else it / 5 })
            },
            label = "dashTab"
        ) { current ->
            if (current == DashTab.Home) {
                HomePage(
                    namaLengkap = namaLengkap,
                    walikelas = walikelas,
                    eventHariIni = eventHariIni,
                    eventMendatang = eventMendatang,
                    isEventLoading = isEventLoading,
                    eventError = eventError,
                    categories = categories,
                    allEntries = allEntries,
                    query = query,
                    onQueryChange = { query = it },
                    onOpenKaldik = onOpenKaldik,
                    onOpenProfile = { showProfileSheet = true },
                    onGoTab = { selectTab(it) }
                )
            } else {
                CategoryPage(
                    tab = current,
                    items = categories.firstOrNull { it.first == current }?.second.orEmpty(),
                    onBack = { selectTab(DashTab.Home) }
                )
            }
        }

        DashboardToolbar(
            tabs = tabs,
            selected = activeTab,
            onSelect = { selectTab(it) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        )
    }

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

// =====================================================================
//  MODEL NAVIGASI
// =====================================================================

private enum class DashTab(
    val key: String,
    val label: String,
    val title: String,
    val icon: ImageVector
) {
    Home("home", "Beranda", "Beranda", Icons.Rounded.Home),
    Kurikulum("kurikulum", "Kurikulum", "Kurikulum", Icons.AutoMirrored.Rounded.MenuBook),
    Kesiswaan("kesiswaan", "Kesiswaan", "Kesiswaan", Icons.Rounded.Groups),
    Kepegawaian("kepegawaian", "Pegawai", "Tendik & Kepegawaian", Icons.Rounded.Fingerprint),
    Lainnya("lainnya", "Lainnya", "Lain-lain", Icons.Rounded.Category);

    companion object {
        fun fromKey(key: String?): DashTab = entries.firstOrNull { it.key == key } ?: Home
    }
}

private data class MenuEntry(val item: MenuItemData, val tab: DashTab)

private data class CategoryStyle(
    val fill: Color,
    val onFill: Color,
    val container: Color,
    val onContainer: Color
)

@Composable
private fun DashTab.style(): CategoryStyle {
    val s = MaterialTheme.colorScheme
    return when (this) {
        DashTab.Home, DashTab.Kurikulum ->
            CategoryStyle(s.primary, s.onPrimary, s.primaryContainer, s.onPrimaryContainer)
        DashTab.Kesiswaan ->
            CategoryStyle(s.secondary, s.onSecondary, s.secondaryContainer, s.onSecondaryContainer)
        DashTab.Kepegawaian ->
            CategoryStyle(s.tertiary, s.onTertiary, s.tertiaryContainer, s.onTertiaryContainer)
        DashTab.Lainnya ->
            CategoryStyle(s.inverseSurface, s.inverseOnSurface, s.surfaceContainerHighest, s.onSurface)
    }
}

/** Deskripsi singkat tiap menu (hanya untuk tampilan; ubah sesuai kebutuhan). */
private fun menuDescription(title: String): String = when (title) {
    "Jurnal" -> "Jurnal mengajar harian"
    "Pantau" -> "Pantau kegiatan belajar"
    "Jur. Kelas" -> "Jurnal per kelas"
    "LMS" -> "Pembelajaran daring"
    "Jadwal" -> "Jadwal mengajar"
    "Piket" -> "Guru piket hari ini"
    "Siswa" -> "Data dan profil siswa"
    "Presensi\nSiswa" -> "Catat kehadiran kelas"
    "G. Wali" -> "Jurnal guru wali"
    "Izin" -> "Izin keluar siswa"
    "Laporan" -> "Laporan kejadian"
    "Ibadah" -> "Pantau ibadah siswa"
    "Presensi" -> "Presensi pegawai"
    "Tendik" -> "Jurnal tenaga kependidikan"
    "Rekap" -> "Rekap kehadiran"
    "SPPD" -> "Perjalanan dinas"
    "Admin" -> "Pengelolaan pengguna"
    "Jurusan" -> "Info jurusan kampus"
    "Buku" -> "Perpustakaan"
    "Link" -> "Tautan penting"
    else -> ""
}

// =====================================================================
//  PRESS MORPH — sudut berubah bentuk saat ditekan
// =====================================================================

@Composable
private fun rememberPressMorph(interaction: MutableInteractionSource): Float {
    val pressed by interaction.collectIsPressedAsState()
    val t by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = ExpressiveMotion.fastSpatial(),
        label = "pressMorph"
    )
    return t
}

private fun morphShape(
    t: Float,
    topStart: Dp,
    topEnd: Dp,
    bottomEnd: Dp,
    bottomStart: Dp,
    pressed: Dp = 16.dp
) = RoundedCornerShape(
    topStart = lerp(topStart, pressed, t).coerceAtLeast(0.dp),
    topEnd = lerp(topEnd, pressed, t).coerceAtLeast(0.dp),
    bottomEnd = lerp(bottomEnd, pressed, t).coerceAtLeast(0.dp),
    bottomStart = lerp(bottomStart, pressed, t).coerceAtLeast(0.dp)
)

// =====================================================================
//  FLOATING TOOLBAR
// =====================================================================

@Composable
private fun DashboardToolbar(
    tabs: List<DashTab>,
    selected: DashTab,
    onSelect: (DashTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { t ->
                ToolbarTab(tab = t, selected = t == selected, onClick = { onSelect(t) })
            }
        }
    }
}

@Composable
private fun ToolbarTab(tab: DashTab, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val bg by animateColorAsState(
        targetValue = if (selected) scheme.primary else Color.Transparent,
        animationSpec = ExpressiveMotion.effects(),
        label = "tabBg"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) scheme.onPrimary else scheme.onSurface,
        animationSpec = ExpressiveMotion.effects(),
        label = "tabFg"
    )
    Row(
        modifier = Modifier
            .height(48.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = fg,
            modifier = Modifier.size(24.dp)
        )
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(ExpressiveMotion.size()) + fadeIn(ExpressiveMotion.effects()),
            exit = shrinkHorizontally(ExpressiveMotion.size()) + fadeOut(ExpressiveMotion.fastEffects())
        ) {
            Text(
                text = tab.label,
                color = fg,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

// =====================================================================
//  BERANDA
// =====================================================================

@Composable
private fun HomePage(
    namaLengkap: String,
    walikelas: String,
    eventHariIni: String?,
    eventMendatang: String?,
    isEventLoading: Boolean,
    eventError: String?,
    categories: List<Pair<DashTab, List<MenuItemData>>>,
    allEntries: List<MenuEntry>,
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenKaldik: () -> Unit,
    onOpenProfile: () -> Unit,
    onGoTab: (DashTab) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 4..10 -> "Selamat pagi"
            in 11..14 -> "Selamat siang"
            in 15..17 -> "Selamat sore"
            else -> "Selamat malam"
        }
    }

    val shortcutTitles = listOf("Jurnal", "Presensi", "Jadwal", "LMS", "Piket")
    val shortcuts = shortcutTitles
        .mapNotNull { t -> allEntries.firstOrNull { it.item.title == t } }
        .ifEmpty { allEntries.take(5) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Baris atas: avatar + sapaan
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                .staggeredEntrance(0),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileAvatarButton(namaLengkap = namaLengkap, onClick = onOpenProfile)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
                Text(
                    text = namaLengkap.ifBlank { "Pengguna E-Smansa" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (walikelas.isNotBlank() && walikelas != "XXXX") {
                Surface(shape = CircleShape, color = scheme.secondaryContainer) {
                    Text(
                        text = "Wali $walikelas",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Kolom cari menu
        TextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            placeholder = { Text("Cari menu: jurnal, presensi, SPPD") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Hapus")
                    }
                }
            },
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = scheme.surfaceContainerHigh,
                unfocusedContainerColor = scheme.surfaceContainer,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .staggeredEntrance(1)
        )

        AnimatedContent(
            targetState = query.isNotBlank(),
            transitionSpec = {
                (fadeIn(ExpressiveMotion.effects()) +
                        scaleIn(initialScale = 0.96f, animationSpec = ExpressiveMotion.spatial())) togetherWith
                        fadeOut(ExpressiveMotion.fastEffects())
            },
            label = "searchSwitch"
        ) { searching ->
            if (searching) {
                SearchResults(query = query.trim(), entries = allEntries)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    TodayHeroCard(
                        eventHariIni = eventHariIni,
                        eventMendatang = eventMendatang,
                        isLoading = isEventLoading,
                        errorMessage = eventError,
                        onClick = onOpenKaldik,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .staggeredEntrance(2)
                    )

                    if (shortcuts.isNotEmpty()) {
                        SectionLabel("Pintasan", Modifier.staggeredEntrance(3))
                        ShortcutCarousel(
                            entries = shortcuts,
                            modifier = Modifier.staggeredEntrance(4)
                        )
                    }

                    SectionLabel("Kategori", Modifier.staggeredEntrance(5))
                    CategoryBento(
                        categories = categories,
                        onGoTab = onGoTab,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    ExpressiveFooter(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .staggeredEntrance(9)
                    )
                }
            }
        }

        BottomToolbarSpace()
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(horizontal = 20.dp)
    )
}

/** Ruang kosong di bawah supaya konten tidak tertutup floating toolbar. */
@Composable
private fun BottomToolbarSpace() {
    Box(modifier = Modifier.navigationBarsPadding()) {
        Spacer(modifier = Modifier.height(96.dp))
    }
}

// ---------- Hero tanggal + agenda ----------

private sealed interface AgendaState {
    data object Loading : AgendaState
    data class Error(val message: String) : AgendaState
    data object Empty : AgendaState
    data class Data(val today: String?, val upcoming: String?) : AgendaState
}

@Composable
private fun TodayHeroCard(
    eventHariIni: String?,
    eventMendatang: String?,
    isLoading: Boolean,
    errorMessage: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val state: AgendaState = when {
        isLoading -> AgendaState.Loading
        errorMessage != null -> AgendaState.Error(errorMessage)
        eventHariIni == null && eventMendatang == null -> AgendaState.Empty
        else -> AgendaState.Data(eventHariIni, eventMendatang)
    }

    val today = remember { LocalDate.now() }
    val idLocale = remember { Locale.forLanguageTag("id-ID") }
    val dayName = remember { today.format(DateTimeFormatter.ofPattern("EEEE", idLocale)) }
    val monthYear = remember { today.format(DateTimeFormatter.ofPattern("MMMM yyyy", idLocale)) }

    val interaction = remember { MutableInteractionSource() }
    val t = rememberPressMorph(interaction)

    val infinite = rememberInfiniteTransition(label = "hero")
    val spin by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(60_000, easing = LinearEasing)),
        label = "heroSpin"
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "heroPulse"
    )

    Card(
        onClick = onClick,
        interactionSource = interaction,
        shape = morphShape(t, 40.dp, 40.dp, 40.dp, 14.dp, pressed = 20.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.primary, contentColor = scheme.onPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        modifier = modifier.graphicsLayer {
            val s = 1f - 0.02f * t
            scaleX = s
            scaleY = s
        }
    ) {
        Box {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 60.dp, y = (-60).dp)
                    .size(200.dp)
                    .graphicsLayer { rotationZ = spin }
                    .background(
                        color = scheme.onPrimary.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(24.dp)
                    )
            )

            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${today.dayOfMonth}",
                        fontSize = 68.sp,
                        lineHeight = 64.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.padding(bottom = 6.dp)) {
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onPrimary
                        )
                        Text(
                            text = monthYear,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(shape = CircleShape, color = scheme.onPrimary, contentColor = scheme.primary) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .graphicsLayer { alpha = pulse }
                                    .background(scheme.primary, CircleShape)
                            )
                            Text(
                                text = "Hari ini",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = state,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(ExpressiveMotion.size()),
                    transitionSpec = {
                        (fadeIn(ExpressiveMotion.effects()) +
                                scaleIn(initialScale = 0.94f, animationSpec = ExpressiveMotion.spatial())) togetherWith
                                fadeOut(ExpressiveMotion.fastEffects())
                    },
                    label = "heroContent"
                ) { s ->
                    when (s) {
                        AgendaState.Loading -> Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = scheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                            Text("Memuat agenda kalender…", style = MaterialTheme.typography.bodyMedium)
                        }

                        is AgendaState.Error -> Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Error, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text(s.message, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }

                        AgendaState.Empty -> Text(
                            text = "Belum ada agenda terdekat",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        is AgendaState.Data -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = s.today ?: "Tidak ada agenda hari ini",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            s.upcoming?.let {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Rounded.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Mendatang: $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(Icons.Rounded.ChevronRight, contentDescription = "Buka Kaldik", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------- Carousel pintasan ----------

@Composable
private fun ShortcutCarousel(entries: List<MenuEntry>, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(132.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(entries) { entry -> // Menggunakan items biasa, index tidak lagi diperlukan untuk shape
            Card(
                onClick = entry.item.onClick,
                shape = RoundedCornerShape(24.dp), // Sudut membulat kartu diseragamkan
                colors = CardDefaults.cardColors(
                    containerColor = scheme.surfaceContainerHigh, // Warna background kartu diseragamkan
                    contentColor = scheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp), // Bentuk wadah ikon diseragamkan
                        color = scheme.primary, // Warna wadah ikon diseragamkan
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = entry.item.iconRes,
                                contentDescription = null,
                                tint = scheme.onPrimary, // Warna ikon diseragamkan
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Text(
                        text = entry.item.title.replace("\n", " "),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

// ---------- Bento kategori ----------

@Composable
private fun CategoryBento(
    categories: List<Pair<DashTab, List<MenuItemData>>>,
    onGoTab: (DashTab) -> Unit,
    modifier: Modifier = Modifier
) {
    // bentuk sudut asimetris tiap tile: topStart, topEnd, bottomEnd, bottomStart
    val corners = listOf(
        listOf(40, 40, 40, 40),
        listOf(40, 40, 40, 40),
        listOf(40, 40, 40, 40),
        listOf(40, 40, 40, 40)
    )
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.chunked(2).forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEachIndexed { col, (tab, items) ->
                    val flat = rowIndex * 2 + col
                    BentoTile(
                        tab = tab,
                        count = items.size,
                        shapeIndex = flat + 2,
                        corner = corners[flat % corners.size],
                        onClick = { onGoTab(tab) },
                        modifier = Modifier
                            .weight(1f)
                            .staggeredEntrance(6 + flat)
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BentoTile(
    tab: DashTab,
    count: Int,
    shapeIndex: Int,
    corner: List<Int>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = tab.style()
    val interaction = remember { MutableInteractionSource() }
    val t = rememberPressMorph(interaction)

    Card(
        onClick = onClick,
        interactionSource = interaction,
        shape = morphShape(t, corner[0].dp, corner[1].dp, corner[2].dp, corner[3].dp),
        colors = CardDefaults.cardColors(containerColor = style.container, contentColor = style.onContainer),
        modifier = modifier.height(120.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = expressiveShape(shapeIndex),
                color = style.fill,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = style.onFill,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column {
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$count menu",
                    style = MaterialTheme.typography.labelMedium,
                    color = style.onContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ---------- Hasil pencarian ----------

@Composable
private fun SearchResults(query: String, entries: List<MenuEntry>) {
    val results = entries.filter {
        it.item.title.contains(query, ignoreCase = true) ||
                menuDescription(it.item.title).contains(query, ignoreCase = true)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (results.isEmpty()) {
            Text(
                text = "Menu tidak ditemukan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            )
        } else {
            results.forEachIndexed { i, entry ->
                val style = entry.tab.style()
                MenuListRow(
                    item = entry.item,
                    index = i,
                    count = results.size,
                    style = style,
                    modifier = Modifier.staggeredEntrance(i, delayPerItemMs = 30L),
                    trailing = {
                        Surface(shape = CircleShape, color = style.container) {
                            Text(
                                text = entry.tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = style.onContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}

// =====================================================================
//  HALAMAN KATEGORI
// =====================================================================

@Composable
private fun CategoryPage(
    tab: DashTab,
    items: List<MenuItemData>,
    onBack: () -> Unit
) {
    val style = tab.style()
    val spin by rememberInfiniteTransition(label = "cat").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(70_000, easing = LinearEasing)),
        label = "catSpin"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Surface(
            shape = RoundedCornerShape(bottomStart = 44.dp, bottomEnd = 44.dp),
            color = style.fill,
            contentColor = style.onFill,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 70.dp, y = (-40).dp)
                        .size(220.dp)
                        .graphicsLayer { rotationZ = spin }
                        .background(
                            color = style.onFill.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(32.dp)
                        )
                )
                Column(
                    // PERUBAHAN: Padding diperbesar agar header tidak terasa kosong
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onBack)
                            .padding(end = 16.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Kembali",
                            modifier = Modifier.size(24.dp) // PERUBAHAN: Ukuran ikon disamakan
                        )
                        Text(
                            "Beranda",
                            // PERUBAHAN: Diperbesar dari labelLarge menjadi titleMedium
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        text = tab.title,
                        // PERUBAHAN: Menggunakan displaySmall agar judul kategori lebih tegas
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(shape = CircleShape, color = style.onFill, contentColor = style.fill) {
                        Text(
                            text = "${items.size} Menu Tersedia", // PERUBAHAN: Teks diperpanjang agar lebih proporsional
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            items.forEachIndexed { i, item ->
                MenuListRow(
                    item = item,
                    index = i,
                    count = items.size,
                    style = style,
                    modifier = Modifier.staggeredEntrance(i + 1)
                )
            }
        }

        BottomToolbarSpace()
    }
}

/** Baris menu bergaya daftar "grouped": ujung bersudut besar, tengah bersudut kecil. */
@Composable
private fun MenuListRow(
    item: MenuItemData,
    index: Int,
    count: Int,
    style: CategoryStyle,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val t = rememberPressMorph(interaction)
    val top = if (index == 0) 30.dp else 8.dp
    val bottom = if (index == count - 1) 30.dp else 8.dp

    Card(
        onClick = item.onClick,
        interactionSource = interaction,
        shape = morphShape(t, top, top, bottom, bottom),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                val s = 1f - 0.03f * t
                scaleX = s
                scaleY = s
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = expressiveShape(index),
                color = style.fill,
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.iconRes,
                        contentDescription = null,
                        tint = style.onFill,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title.replace("\n", " "),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val desc = menuDescription(item.title)
                if (desc.isNotEmpty()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (trailing != null) {
                trailing()
            } else {
                Surface(
                    shape = CircleShape,
                    color = scheme.surfaceContainerHighest,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowOutward,
                            contentDescription = null,
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// =====================================================================
//  MOTION — token gerak Material 3 Expressive (spring-based)
// =====================================================================
private object ExpressiveMotion {
    fun <T> fastSpatial() = spring<T>(dampingRatio = 0.6f, stiffness = 800f)
    fun <T> spatial() = spring<T>(dampingRatio = 0.8f, stiffness = 380f)
    fun <T> effects() = spring<T>(dampingRatio = 1f, stiffness = 1600f)
    fun <T> fastEffects() = spring<T>(dampingRatio = 1f, stiffness = 3800f)

    /** Untuk animasi ukuran layout: critically damped agar tidak pernah negatif. */
    fun <T> size() = spring<T>(dampingRatio = 1f, stiffness = 380f)
}

/** Muncul bertahap: fade + naik + membesar dengan spring, tertunda per indeks. */
@Composable
private fun Modifier.staggeredEntrance(index: Int, delayPerItemMs: Long = 45L): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * delayPerItemMs)
        progress.animateTo(1f, ExpressiveMotion.spatial())
    }
    return this.graphicsLayer {
        val p = progress.value
        alpha = p.coerceIn(0f, 1f)
        translationY = (1f - p) * 40.dp.toPx()
        val s = 0.94f + 0.06f * p
        scaleX = s
        scaleY = s
    }
}

/**
 * Bentuk ekspresif (menggantikan M3 Shapes Experimental)
 * Memanfaatkan sudut asimetris untuk desain organik
 */
@Composable
private fun expressiveShape(index: Int): Shape = when (index % 6) {
    0 -> RoundedCornerShape(topStart = 24.dp, bottomEnd = 24.dp)
    1 -> RoundedCornerShape(16.dp)
    2 -> RoundedCornerShape(topEnd = 24.dp, bottomStart = 24.dp)
    3 -> CutCornerShape(16.dp)
    4 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
    else -> CircleShape
}

private fun initialsOf(name: String): String =
    name.trim().split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { word -> word.firstOrNull { it.isLetter() }?.uppercaseChar() }
        .joinToString("")

// =====================================================================
//  APP BAR AVATAR + CHIP IDENTITAS
// =====================================================================

@Composable
private fun ProfileAvatarButton(
    namaLengkap: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = ExpressiveMotion.fastSpatial(),
        label = "avatarScale"
    )
    val initials = remember(namaLengkap) { initialsOf(namaLengkap) }

    Surface(
        onClick = onClick,
        interactionSource = interaction,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (initials.isNotEmpty()) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            } else {
                Icon(Icons.Rounded.Person, contentDescription = "Profil", modifier = Modifier.size(24.dp))
            }
        }
    }
}

// =====================================================================
//  BOTTOM SHEET PROFIL
// =====================================================================

private data class InfoRowSpec(val icon: ImageVector, val label: String, val value: String)

/** Daftar "grouped": item pertama/terakhir bersudut besar, di tengah bersudut kecil. */
private fun groupedShape(index: Int, count: Int): Shape {
    val big = 24.dp
    val small = 6.dp
    return when {
        count == 1 -> RoundedCornerShape(big)
        index == 0 -> RoundedCornerShape(big, big, small, small)
        index == count - 1 -> RoundedCornerShape(small, small, big, big)
        else -> RoundedCornerShape(small)
    }
}

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
    val scheme = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val initials = remember(namaLengkap) { initialsOf(namaLengkap) }

    val infinite = rememberInfiniteTransition(label = "avatar")
    val spin by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(30_000, easing = LinearEasing)),
        label = "avatarSpin"
    )

    val rows = buildList {
        val jabatan = listOf(status, jenis).filter { it.isNotBlank() }.joinToString(" - ").ifBlank { "-" }
        add(InfoRowSpec(Icons.Rounded.Work, "Jabatan / Profesi", jabatan))
        if (username.isNotBlank()) add(InfoRowSpec(Icons.Rounded.AlternateEmail, "Username", "@$username"))
        if (walikelas.isNotBlank() && walikelas != "XXXX") add(InfoRowSpec(Icons.Rounded.Class, "Wali Kelas", walikelas))
        if (role.isNotBlank()) {
            add(InfoRowSpec(Icons.Rounded.VerifiedUser, "Hak Akses", role.replaceFirstChar { it.uppercase() }))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        containerColor = scheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(108.dp).staggeredEntrance(0)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = spin }
                        .background(scheme.primaryContainer, RoundedCornerShape(32.dp))
                )
                if (initials.isNotEmpty()) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onPrimaryContainer
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.icm_person),
                        contentDescription = "Avatar",
                        tint = scheme.onPrimaryContainer,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = namaLengkap.ifBlank { "Pegawai E-Smansa" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = scheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.staggeredEntrance(1)
            )

            if (nip.isNotBlank() && nip != "-") {
                Text(
                    text = "NIP. $nip",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.staggeredEntrance(2)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                rows.forEachIndexed { index, row ->
                    Surface(
                        shape = groupedShape(index, rows.size),
                        color = scheme.surfaceContainerHighest.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(3 + index)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = scheme.secondaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = row.icon,
                                        contentDescription = null,
                                        tint = scheme.onSecondaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = row.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = scheme.onSurfaceVariant
                                )
                                Text(
                                    text = row.value,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(3 + rows.size),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilledTonalButton(
                    onClick = onEditProfile,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profil", style = MaterialTheme.typography.labelLarge)
                }

                Button(
                    onClick = onPromptLogout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// =====================================================================
//  DIALOG LOGOUT + FOOTER
// =====================================================================

@Composable
fun LogoutConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(32.dp),
        icon = {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = scheme.errorContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = null,
                        tint = scheme.onErrorContainer,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Keluar dari Akun?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Sesi Anda akan diakhiri dan data sesi login pada perangkat ini akan dihapus. Anda perlu memasukkan kredensial login kembali.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = scheme.error, contentColor = scheme.onError)
            ) { Text(text = "Ya, Logout") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = RoundedCornerShape(16.dp)) { Text(text = "Batal") }
        }
    )
}

@Composable
fun ExpressiveFooter(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Text(
                text = stringResource(id = R.string.versiapp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
            )
        }
    }
}

// ==================== COMPOSE PREVIEWS ====================

@Preview(name = "Dashboard Guru (Light)", showBackground = true, showSystemUi = true)
@Preview(
    name = "Dashboard Guru (Dark)",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
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