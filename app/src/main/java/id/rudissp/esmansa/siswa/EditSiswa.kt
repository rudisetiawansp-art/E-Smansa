package id.rudissp.esmansa.siswa

import android.content.SharedPreferences
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.AppTheme
import id.rudissp.esmansa.LoadingIndicator
import org.json.JSONObject
import java.net.URLEncoder

class EditSiswa : ComponentActivity() {
    private lateinit var prefs: SharedPreferences

    private val BASE_URL = AppConstants.BASE_URL
    private val SMANIA_URL = AppConstants.SMANIA_URL

    // States
    private var isLoadingState = mutableStateOf(false)
    private var isEditModeState = mutableStateOf(false)

    private var idState = mutableStateOf("")
    private var nisState = mutableStateOf("")
    private var nisnState = mutableStateOf("")
    private var namaState = mutableStateOf("")
    private var passState = mutableStateOf("")
    private var kelasState = mutableStateOf("")
    private var guruWaliState = mutableStateOf("")
    private var deviceIdState = mutableStateOf("")
    private var editInfoState = mutableStateOf("")

    private var rencanaPascaStudiState = mutableStateOf("")
    private var jurusanImpian1State = mutableStateOf("")
    private var jurusanImpian2State = mutableStateOf("")
    private var kampusImpian1State = mutableStateOf("")
    private var kampusImpian2State = mutableStateOf("")
    private var bidangKarierState = mutableStateOf("")
    private var mapelFavoritState = mutableStateOf("")
    private var skillDimilikiState = mutableStateOf("")
    private var skillDipelajariState = mutableStateOf("")
    private var pengalamanState = mutableStateOf("")
    private var programSekolahState = mutableStateOf("")

    private val jurusanListState = mutableStateListOf<String>()
    private val kampusListState = mutableStateListOf<String>()
    private val programListState = mutableStateListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)

        loadProfile()
        intent.getStringExtra("nis")?.let { loadSiswaByNis(it) }

        ambildatajurusan()
        ambildatakampus()
        ambilmantab()

        setContent {
            AppTheme {
                EditSiswaScreen(
                    isLoading = isLoadingState.value,
                    isEditMode = isEditModeState.value,
                    nama = namaState.value,
                    nis = nisState.value,
                    nisn = nisnState.value,
                    kelas = kelasState.value,
                    guruWali = guruWaliState.value,
                    deviceId = deviceIdState.value,
                    editInfo = editInfoState.value,
                    rencanaPascaStudi = rencanaPascaStudiState.value,
                    onRencanaChange = { rencanaPascaStudiState.value = it },
                    jurusanImpian1 = jurusanImpian1State.value,
                    onJurusan1Change = { jurusanImpian1State.value = it },
                    jurusanImpian2 = jurusanImpian2State.value,
                    onJurusan2Change = { jurusanImpian2State.value = it },
                    kampusImpian1 = kampusImpian1State.value,
                    onKampus1Change = { kampusImpian1State.value = it },
                    kampusImpian2 = kampusImpian2State.value,
                    onKampus2Change = { kampusImpian2State.value = it },
                    bidangKarier = bidangKarierState.value,
                    onBidangKarierChange = { bidangKarierState.value = it },
                    mapelFavorit = mapelFavoritState.value,
                    onMapelFavoritChange = { mapelFavoritState.value = it },
                    skillDimiliki = skillDimilikiState.value,
                    onSkillDimilikiChange = { skillDimilikiState.value = it },
                    skillDipelajari = skillDipelajariState.value,
                    onSkillDipelajariChange = { skillDipelajariState.value = it },
                    pengalaman = pengalamanState.value,
                    onPengalamanChange = { pengalamanState.value = it },
                    programSekolah = programSekolahState.value,
                    onProgramSekolahChange = { programSekolahState.value = it },
                    jurusanList = jurusanListState,
                    kampusList = kampusListState,
                    programList = programListState,
                    onBackClick = { finish() },
                    onSaveClick = { saveData() },
                    onResetDeviceId = { resetid() }
                )
            }
        }
    }

    private fun loadProfile() {
        val id = intent.getStringExtra("id") ?: ""
        val nama = intent.getStringExtra("nama") ?: ""
        val nis = intent.getStringExtra("nis") ?: ""
        val pass = intent.getStringExtra("pass") ?: ""
        val kelas = intent.getStringExtra("kelas") ?: ""
        val guruwali = intent.getStringExtra("guruwali") ?: ""
        val nisn = intent.getStringExtra("nisn") ?: ""
        val device_id = intent.getStringExtra("device_id") ?: ""

        if (nis.isEmpty()) {
            Toast.makeText(this, "NIS tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        idState.value = id
        namaState.value = nama
        nisState.value = nis
        nisnState.value = nisn
        passState.value = pass
        kelasState.value = kelas
        guruWaliState.value = guruwali
        deviceIdState.value = if (device_id == "null" || device_id.isEmpty()) "" else device_id

        fetchWalikelas(kelas)
    }

    private fun fetchWalikelas(kelas: String) {
        val encodedKelas = URLEncoder.encode(kelas, "UTF-8")
        val url = "https://sman1sukorejo.sch.id/smania/android/api_wakel.php?kelas=$encodedKelas"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val status = response.getString("status")
                    if (status == "success") {
                        val data = response.getJSONObject("data")
                        val walikelas = data.getString("walikelas")
                        guruWaliState.value = walikelas
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

    private fun loadSiswaByNis(nis: String) {
        val url = SMANIA_URL + "android/api_siswa.php?nis=$nis"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val status = response.optString("status")
                if (status == "success") {
                    val dataObj = response.optJSONObject("data")
                    if (dataObj != null) {
                        parseSiswaFromJson(dataObj)
                        isEditModeState.value = true
                    } else {
                        isEditModeState.value = false
                    }
                } else {
                    isEditModeState.value = false
                }
            },
            { _ ->
                isEditModeState.value = false
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun parseSiswaFromJson(obj: JSONObject) {
        rencanaPascaStudiState.value = obj.optString("rencana_pasca_studi", "")
        jurusanImpian1State.value = obj.optString("jurusan_impian_1", "")
        jurusanImpian2State.value = obj.optString("jurusan_impian_2", "")
        kampusImpian1State.value = obj.optString("kampus_impian_1", "")
        kampusImpian2State.value = obj.optString("kampus_impian_2", "")
        bidangKarierState.value = obj.optString("bidang_karier", "")
        mapelFavoritState.value = obj.optString("mapel_favorit", "")
        skillDimilikiState.value = obj.optString("skill_dimiliki", "")
        skillDipelajariState.value = obj.optString("skill_dipelajari", "")
        pengalamanState.value = obj.optString("pengalaman", "")
        programSekolahState.value = obj.optString("program_sekolah", "")
        val editedBy = obj.optString("edited_by", "")
        val editTime = obj.optString("edit_time", "")
        if (editedBy.isNotEmpty() && editTime.isNotEmpty()) {
            editInfoState.value = "Diperbarui oleh: $editedBy pada $editTime"
        }
    }

    private fun saveData() {
        if (nisState.value.isEmpty()) {
            Toast.makeText(this, "NIS tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }
        val siswa = datasiswa(
            nis = nisState.value.trim(),
            rencanaPascaStudi = rencanaPascaStudiState.value.trim(),
            jurusanImpian1 = jurusanImpian1State.value.trim(),
            jurusanImpian2 = jurusanImpian2State.value.trim(),
            kampusImpian1 = kampusImpian1State.value.trim(),
            kampusImpian2 = kampusImpian2State.value.trim(),
            bidangKarier = bidangKarierState.value.trim(),
            mapelFavorit = mapelFavoritState.value.trim(),
            skillDimiliki = skillDimilikiState.value.trim(),
            skillDipelajari = skillDipelajariState.value.trim(),
            pengalaman = pengalamanState.value.trim(),
            programSekolah = programSekolahState.value.trim(),
            editedBy = prefs.getString("namalengkap", "")
        )

        if (isEditModeState.value) {
            updateSiswa(siswa)
        } else {
            insertSiswa(siswa)
        }
    }

    private fun insertSiswa(siswa: datasiswa) {
        isLoadingState.value = true
        val url = "$SMANIA_URL/android/api_siswa.php"
        val jsonObject = siswaToJson(siswa)

        val request = object : JsonObjectRequest(
            Method.POST, url, jsonObject,
            { response ->
                isLoadingState.value = false
                val status = response.optString("status")
                if (status == "success") {
                    Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val msg = response.optString("message", "Gagal menyimpan")
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                isLoadingState.value = false
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getBodyContentType(): String = "application/json; charset=utf-8"
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun updateSiswa(siswa: datasiswa) {
        isLoadingState.value = true
        val url = "$SMANIA_URL/android/api_siswa.php?nis=${siswa.nis}"
        val jsonObject = siswaToJson(siswa)

        val request = object : JsonObjectRequest(
            Method.PUT, url, jsonObject,
            { response ->
                isLoadingState.value = false
                val status = response.optString("status")
                if (status == "success") {
                    Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val msg = response.optString("message", "Gagal update")
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                isLoadingState.value = false
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getBodyContentType(): String = "application/json; charset=utf-8"
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun siswaToJson(siswa: datasiswa): JSONObject {
        return JSONObject().apply {
            put("nis", siswa.nis)
            siswa.rencanaPascaStudi?.let { put("rencana_pasca_studi", it) }
            siswa.jurusanImpian1?.let { put("jurusan_impian_1", it) }
            siswa.jurusanImpian2?.let { put("jurusan_impian_2", it) }
            siswa.kampusImpian1?.let { put("kampus_impian_1", it) }
            siswa.kampusImpian2?.let { put("kampus_impian_2", it) }
            siswa.bidangKarier?.let { put("bidang_karier", it) }
            siswa.mapelFavorit?.let { put("mapel_favorit", it) }
            siswa.skillDimiliki?.let { put("skill_dimiliki", it) }
            siswa.skillDipelajari?.let { put("skill_dipelajari", it) }
            siswa.pengalaman?.let { put("pengalaman", it) }
            siswa.programSekolah?.let { put("program_sekolah", it) }
            siswa.editedBy?.let { put("edited_by", it) }
        }
    }

    private fun resetid() {
        isLoadingState.value = true
        val params = HashMap<String, String>().apply {
            put("id", idState.value)
            put("nis", nisState.value)
            put("nama", namaState.value)
            put("pass", passState.value)
            put("kelas", kelasState.value)
            put("guruwali", guruWaliState.value)
            put("device_id", "")
        }

        val url = "$BASE_URL/update_siswa.php"
        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                isLoadingState.value = false
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        deviceIdState.value = ""
                        Toast.makeText(this, "Login siswa ${namaState.value} berhasil direset", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: $response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                isLoadingState.value = false
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> = params
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun ambildatajurusan() {
        val urlApi = AppConstants.BASE_URL + "api_jurusan.php"
        val queue = Volley.newRequestQueue(this)
        val request = JsonArrayRequest(
            Request.Method.GET, urlApi, null,
            { response ->
                jurusanListState.clear()
                for (i in 0 until response.length()) {
                    jurusanListState.add(response.getString(i))
                }
            },
            { _ -> }
        )
        queue.add(request)
    }

    private fun ambildatakampus() {
        val urlApi = AppConstants.BASE_URL + "api_universitas.php"
        val queue = Volley.newRequestQueue(this)
        val request = JsonArrayRequest(
            Request.Method.GET, urlApi, null,
            { response ->
                kampusListState.clear()
                for (i in 0 until response.length()) {
                    kampusListState.add(response.getString(i))
                }
            },
            { _ -> }
        )
        queue.add(request)
    }

    private fun ambilmantab() {
        val urlApi = AppConstants.BASE_URL + "api_mantab.php"
        val queue = Volley.newRequestQueue(this)
        val request = JsonArrayRequest(
            Request.Method.GET, urlApi, null,
            { response ->
                programListState.clear()
                for (i in 0 until response.length()) {
                    programListState.add(response.getString(i))
                }
            },
            { _ -> }
        )
        queue.add(request)
    }
}

data class datasiswa(
    var nis: String,
    var rencanaPascaStudi: String? = null,
    var jurusanImpian1: String? = null,
    var jurusanImpian2: String? = null,
    var kampusImpian1: String? = null,
    var kampusImpian2: String? = null,
    var bidangKarier: String? = null,
    var mapelFavorit: String? = null,
    var skillDimiliki: String? = null,
    var skillDipelajari: String? = null,
    var pengalaman: String? = null,
    var programSekolah: String? = null,
    var editedBy: String? = null,
    var editTime: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSiswaScreen(
    isLoading: Boolean,
    isEditMode: Boolean,
    nama: String,
    nis: String,
    nisn: String,
    kelas: String,
    guruWali: String,
    deviceId: String,
    editInfo: String,
    rencanaPascaStudi: String,
    onRencanaChange: (String) -> Unit,
    jurusanImpian1: String,
    onJurusan1Change: (String) -> Unit,
    jurusanImpian2: String,
    onJurusan2Change: (String) -> Unit,
    kampusImpian1: String,
    onKampus1Change: (String) -> Unit,
    kampusImpian2: String,
    onKampus2Change: (String) -> Unit,
    bidangKarier: String,
    onBidangKarierChange: (String) -> Unit,
    mapelFavorit: String,
    onMapelFavoritChange: (String) -> Unit,
    skillDimiliki: String,
    onSkillDimilikiChange: (String) -> Unit,
    skillDipelajari: String,
    onSkillDipelajariChange: (String) -> Unit,
    pengalaman: String,
    onPengalamanChange: (String) -> Unit,
    programSekolah: String,
    onProgramSekolahChange: (String) -> Unit,
    jurusanList: List<String>,
    kampusList: List<String>,
    programList: List<String>,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onResetDeviceId: () -> Unit
) {
    val fieldShape = RoundedCornerShape(24.dp)
    val cardShape = RoundedCornerShape(32.dp)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (nama.isNotBlank()) nama else "Profil Siswa",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isEditMode) {
                            Text(
                                text = "Mode Edit (Data Tersedia)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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
        },
        bottomBar = {
            Surface(
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onResetDeviceId,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset ID", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSaveClick,
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Data", fontWeight = FontWeight.ExtraBold)
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // Section 1: Informasi Umum Banner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Text(
                        text = "Data Akademik & Sistem",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                ElevatedCard(
                    shape = cardShape,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.elevatedCardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = nis,
                            onValueChange = {},
                            label = { Text("NIS") },
                            readOnly = true,
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        )

                        OutlinedTextField(
                            value = nisn,
                            onValueChange = {},
                            label = { Text("NISN") },
                            readOnly = true,
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = kelas,
                                onValueChange = {},
                                label = { Text("Kelas") },
                                readOnly = true,
                                shape = fieldShape,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            )

                            OutlinedTextField(
                                value = guruWali,
                                onValueChange = {},
                                label = { Text("Wali Kelas") },
                                readOnly = true,
                                shape = fieldShape,
                                modifier = Modifier.weight(1.5f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            )
                        }

                        OutlinedTextField(
                            value = deviceId.ifEmpty { "Belum terikat perangkat" },
                            onValueChange = {},
                            label = { Text("Device ID Terdaftar") },
                            readOnly = true,
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        )
                    }
                }

                // Section 2: Rencana Studi & Karir Banner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.School, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                    Text(
                        text = "Rencana Studi & Karier",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                ElevatedCard(
                    shape = cardShape,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.elevatedCardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = rencanaPascaStudi,
                            onValueChange = onRencanaChange,
                            label = { Text("Rencana Pasca Studi") },
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        )

                        AutocompleteTextField(
                            value = jurusanImpian1,
                            onValueChange = onJurusan1Change,
                            label = "Jurusan Impian 1",
                            suggestions = jurusanList,
                            shape = fieldShape
                        )

                        AutocompleteTextField(
                            value = jurusanImpian2,
                            onValueChange = onJurusan2Change,
                            label = "Jurusan Impian 2",
                            suggestions = jurusanList,
                            shape = fieldShape
                        )

                        AutocompleteTextField(
                            value = kampusImpian1,
                            onValueChange = onKampus1Change,
                            label = "Kampus Impian 1",
                            suggestions = kampusList,
                            shape = fieldShape
                        )

                        AutocompleteTextField(
                            value = kampusImpian2,
                            onValueChange = onKampus2Change,
                            label = "Kampus Impian 2",
                            suggestions = kampusList,
                            shape = fieldShape
                        )

                        OutlinedTextField(
                            value = bidangKarier,
                            onValueChange = onBidangKarierChange,
                            label = { Text("Bidang Karier") },
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        )

                        OutlinedTextField(
                            value = mapelFavorit,
                            onValueChange = onMapelFavoritChange,
                            label = { Text("Mapel Favorit") },
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        )

                        OutlinedTextField(
                            value = skillDimiliki,
                            onValueChange = onSkillDimilikiChange,
                            label = { Text("Skill yang Dimiliki") },
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        )

                        OutlinedTextField(
                            value = skillDipelajari,
                            onValueChange = onSkillDipelajariChange,
                            label = { Text("Skill yang Ingin Dipelajari") },
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        )

                        OutlinedTextField(
                            value = pengalaman,
                            onValueChange = onPengalamanChange,
                            label = { Text("Pengalaman Organisasi / Prestasi") },
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        )

                        AutocompleteTextField(
                            value = programSekolah,
                            onValueChange = onProgramSekolahChange,
                            label = "Program Sekolah / Mantab",
                            suggestions = programList,
                            shape = fieldShape
                        )
                    }
                }

                if (editInfo.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = editInfo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp)) // Extra padding for bottom bar
            }

            // Expressive Loading Overlay
            if (isLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "saving infinite transition")
                            val animatedProgress = infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(400, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "saving progress"
                            )

                            Surface(
                                shape = RoundedCornerShape(32.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shadowElevation = 12.dp,
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
                                text = "Menyimpan data...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)
) {
    var expanded by remember { mutableStateOf(false) }
    val filteredSuggestions = remember(value, suggestions) {
        if (value.isBlank()) suggestions
        else suggestions.filter { it.contains(value, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filteredSuggestions.isNotEmpty(),
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true)
        )
        if (filteredSuggestions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            onValueChange(suggestion)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditSiswaScreenPreview() {
    AppTheme {
        EditSiswaScreen(
            isLoading = false,
            isEditMode = true,
            nama = "Ahmad Fajar",
            nis = "12345",
            nisn = "0081234567",
            kelas = "XII IPA 1",
            guruWali = "Dra. Siti Aminah",
            deviceId = "device-xyz-123",
            editInfo = "Diperbarui oleh: Admin pada 2025-02-20 10:00:00",
            rencanaPascaStudi = "Kuliah di Universitas Indonesia",
            onRencanaChange = {},
            jurusanImpian1 = "Teknik Informatika",
            onJurusan1Change = {},
            jurusanImpian2 = "Sistem Informasi",
            onJurusan2Change = {},
            kampusImpian1 = "Universitas Indonesia",
            onKampus1Change = {},
            kampusImpian2 = "Universitas Gadjah Mada",
            onKampus2Change = {},
            bidangKarier = "Software Engineer",
            onMapelFavoritChange = {},
            mapelFavorit = "Matematika & Informatika",
            onBidangKarierChange = {},
            skillDimiliki = "Kotlin, Java",
            onSkillDimilikiChange = {},
            skillDipelajari = "Jetpack Compose, AI",
            onSkillDipelajariChange = {},
            pengalaman = "Ketua OSIS",
            onPengalamanChange = {},
            programSekolah = "Olimpiade Komputer",
            onProgramSekolahChange = {},
            jurusanList = listOf("Teknik Informatika", "Sistem Informasi", "Kedokteran"),
            kampusList = listOf("Universitas Indonesia", "Universitas Gadjah Mada", "ITB"),
            programList = listOf("Olimpiade Komputer", "English Club"),
            onBackClick = {},
            onSaveClick = {},
            onResetDeviceId = {}
        )
    }
}