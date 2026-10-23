package id.rudissp.esmansa.lms

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.AppTheme
import org.json.JSONException
import org.json.JSONObject
import java.net.URLEncoder

data class DiskusiLMS(
    val idDiskusi: Int,
    val pengirimTipe: String,
    val pengirimNama: String,
    val pesan: String,
    val waktu: String
)

data class PengumpulanSiswa(
    val idUser: Int,
    val nis: String,
    val nama: String,
    val foto: String,
    val isMengumpulkan: Boolean,
    val fileJawaban: String,
    val waktuKumpul: String,
    val nilai: Int
)

class DetailLMS : ComponentActivity() {

    private lateinit var prefs: SharedPreferences
    private var idTugas: String = ""
    private var judulTugasIntent: String = ""

    private var isLoadingState = mutableStateOf(true)
    private var deskripsiState = mutableStateOf("")
    private var fileLampiranState = mutableStateOf("")
    private var jenisState = mutableStateOf("")
    private var isOnlineState = mutableStateOf(true)
    private var daftarKelasState = mutableStateListOf<String>()
    private var kelasAktifState = mutableStateOf("")

    private val listPengumpulanState = mutableStateListOf<PengumpulanSiswa>()
    private val listDiskusiState = mutableStateListOf<DiskusiLMS>()

    private var selectedImageUrlState = mutableStateOf<String?>(null)
    private var showDeleteDialogState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)

        idTugas = intent.getStringExtra("ID_TUGAS") ?: ""
        judulTugasIntent = intent.getStringExtra("JUDUL") ?: "Detail Tugas"

        fetchDetailTugas()

        setContent {
            AppTheme {
                DetailLMSScreen(
                    judulTugas = judulTugasIntent,
                    deskripsi = deskripsiState.value,
                    fileLampiran = fileLampiranState.value,
                    jenis = jenisState.value,
                    isOnline = isOnlineState.value,
                    daftarKelas = daftarKelasState,
                    kelasAktif = kelasAktifState.value,
                    listPengumpulan = listPengumpulanState,
                    listDiskusi = listDiskusiState,
                    isLoading = isLoadingState.value,
                    selectedImageUrl = selectedImageUrlState.value,
                    showDeleteDialog = showDeleteDialogState.value,
                    onBackClick = { finish() },
                    onSelectKelas = { kelas ->
                        kelasAktifState.value = kelas
                        val isMat = jenisState.value.equals("materi", ignoreCase = true)
                        if (!isMat && isOnlineState.value) {
                            loadDataBerdasarkanKelas(kelas)
                        }
                        loadDiskusiBerdasarkanKelas(kelas)
                    },
                    onSendDiskusi = { pesan ->
                        if (pesan.isNotBlank() && kelasAktifState.value.isNotBlank()) {
                            kirimKomentarDiskusi(pesan, kelasAktifState.value)
                        }
                    },
                    onImageViewClick = { url -> selectedImageUrlState.value = url },
                    onDismissImageDialog = { selectedImageUrlState.value = null },
                    onDeleteClick = { showDeleteDialogState.value = true },
                    onDismissDeleteDialog = { showDeleteDialogState.value = false },
                    onConfirmDelete = {
                        showDeleteDialogState.value = false
                        eksekusiHapusTugas()
                    }
                )
            }
        }
    }

    private fun fetchDetailTugas() {
        isLoadingState.value = true
        val url = AppConstants.BASE_URL + "get_lms_detail.php?id_tugas=" + idTugas

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                isLoadingState.value = false
                try {
                    val status = response.getString("status")
                    if (status == "success") {
                        val data = response.getJSONObject("data")
                        deskripsiState.value = data.optString("deskripsi", "Tidak ada deskripsi.")
                        fileLampiranState.value = data.optString("file_lampiran", "")
                        val serverJudul = data.optString("judul", "")
                        if (serverJudul.isNotBlank()) {
                            judulTugasIntent = serverJudul
                        }
                        jenisState.value = data.optString("jenis", "")
                        isOnlineState.value = data.optInt("is_online", 1) == 1

                        val daftarKelasArray = data.optJSONArray("daftar_kelas")
                        daftarKelasState.clear()
                        if (daftarKelasArray != null) {
                            for (i in 0 until daftarKelasArray.length()) {
                                daftarKelasState.add(daftarKelasArray.getString(i))
                            }
                        }

                        if (daftarKelasState.isNotEmpty()) {
                            kelasAktifState.value = daftarKelasState[0]
                            val isMat = jenisState.value.equals("materi", ignoreCase = true)
                            if (!isMat && isOnlineState.value) {
                                loadDataBerdasarkanKelas(kelasAktifState.value)
                            }
                            loadDiskusiBerdasarkanKelas(kelasAktifState.value)
                        }
                    } else {
                        Toast.makeText(this, response.optString("message", "Gagal memuat detail"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            {
                isLoadingState.value = false
                Toast.makeText(this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }

    private fun loadDataBerdasarkanKelas(kelas: String) {
        val kelasEncoded = URLEncoder.encode(kelas, "UTF-8")
        val url = AppConstants.BASE_URL + "get_pengumpulan.php?id_tugas=$idTugas&kelas=$kelasEncoded"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    if (response.getString("status") == "success") {
                        val jsonArray = response.getJSONArray("data")
                        listPengumpulanState.clear()
                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            listPengumpulanState.add(
                                PengumpulanSiswa(
                                    idUser = obj.getInt("id_user"),
                                    nis = obj.getString("nis"),
                                    nama = obj.getString("nama"),
                                    foto = obj.getString("foto"),
                                    isMengumpulkan = obj.getBoolean("is_mengumpulkan"),
                                    fileJawaban = obj.optString("file_jawaban", ""),
                                    waktuKumpul = obj.optString("waktu_kumpul", ""),
                                    nilai = obj.optInt("nilai", 0)
                                )
                            )
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { }
        )
        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }

    private fun loadDiskusiBerdasarkanKelas(kelas: String) {
        val kelasEncoded = URLEncoder.encode(kelas, "UTF-8")
        val url = AppConstants.BASE_URL + "get_diskusi.php?id_tugas=$idTugas&kelas=$kelasEncoded"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    if (response.getString("status") == "success") {
                        val jsonArray = response.getJSONArray("data")
                        listDiskusiState.clear()
                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            listDiskusiState.add(
                                DiskusiLMS(
                                    idDiskusi = obj.getInt("id_diskusi"),
                                    pengirimTipe = obj.getString("pengirim_tipe"),
                                    pengirimNama = obj.getString("pengirim_nama"),
                                    pesan = obj.getString("pesan"),
                                    waktu = obj.getString("created_at")
                                )
                            )
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { }
        )
        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }

    private fun kirimKomentarDiskusi(pesan: String, kelas: String) {
        val url = AppConstants.BASE_URL + "insert_diskusi.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        loadDiskusiBerdasarkanKelas(kelas)
                    } else {
                        Toast.makeText(this, jsonResponse.optString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            {
                Toast.makeText(this, "Gagal mengirim pesan.", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "id_tugas" to idTugas,
                    "kelas" to kelas,
                    "id_guru" to (prefs.getString("username", "") ?: ""),
                    "nama_guru" to (prefs.getString("namalengkap", "") ?: ""),
                    "pesan" to pesan
                )
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun eksekusiHapusTugas() {
        val url = AppConstants.HAPUS_TUGAS
        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this, jsonResponse.optString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            {
                Toast.makeText(this, "Koneksi bermasalah saat menghapus tugas.", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("id_tugas" to idTugas)
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }
}

@Composable
fun GlideImageView(
    url: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            ImageView(ctx).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        },
        update = { imageView ->
            Glide.with(context)
                .load(url)
                .into(imageView)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailLMSScreen(
    judulTugas: String,
    deskripsi: String,
    fileLampiran: String,
    jenis: String,
    isOnline: Boolean,
    daftarKelas: List<String>,
    kelasAktif: String,
    listPengumpulan: List<PengumpulanSiswa>,
    listDiskusi: List<DiskusiLMS>,
    isLoading: Boolean,
    selectedImageUrl: String?,
    showDeleteDialog: Boolean,
    onBackClick: () -> Unit,
    onSelectKelas: (String) -> Unit,
    onSendDiskusi: (String) -> Unit,
    onImageViewClick: (String) -> Unit,
    onDismissImageDialog: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val isMateri = jenis.equals("materi", ignoreCase = true)
    val showPengumpulanTab = !isMateri && isOnline

    var selectedTabIndex by remember { mutableStateOf(0) }
    var diskusiInput by remember { mutableStateOf("") }
    var isImageHidden by remember { mutableStateOf(false) }

    val urlFolderUpload = AppConstants.BASE_URL + "lms/lampiran/"
    val hasImageAttachment = fileLampiran.isNotBlank() && fileLampiran != "null" &&
            (fileLampiran.endsWith(".jpg", true) || fileLampiran.endsWith(".jpeg", true) || fileLampiran.endsWith(".png", true))

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = judulTugas,
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
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Hapus Tugas",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                )

                // Kelas Chips Row
                if (daftarKelas.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kelas:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            daftarKelas.forEach { kelas ->
                                val isSelected = kelas == kelasAktif
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectKelas(kelas) },
                                    label = { Text(kelas, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                    }
                }

                // Tabs (Detail, Pengumpulan, Diskusi)
                val tabs = buildList {
                    add("Detail")
                    if (showPengumpulanTab) add("Pengumpulan")
                    add("Diskusi")
                }

                PrimaryTabRow(selectedTabIndex = selectedTabIndex.coerceIn(0, tabs.size - 1)) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, fontWeight = FontWeight.Bold) }
                        )
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
            if (isLoading) {
                ExpressiveLoadingView(message = "Memuat detail tugas...")
            } else {
                val activeTabTitle = when {
                    selectedTabIndex == 0 -> "Detail"
                    selectedTabIndex == 1 && showPengumpulanTab -> "Pengumpulan"
                    else -> "Diskusi"
                }

                when (activeTabTitle) {
                    "Detail" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = judulTugas,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                        Text(
                                            text = deskripsi,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        if (hasImageAttachment) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            TextButton(
                                                onClick = { isImageHidden = !isImageHidden }
                                            ) {
                                                Icon(
                                                    imageVector = if (isImageHidden) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(if (isImageHidden) "Tampilkan Gambar" else "Sembunyikan Gambar")
                                            }

                                            if (!isImageHidden) {
                                                val imageUrl = urlFolderUpload + fileLampiran
                                                GlideImageView(
                                                    url = imageUrl,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(220.dp)
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .clickable { onImageViewClick(imageUrl) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Pengumpulan" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (listPengumpulan.isEmpty()) {
                                item {
                                    EmptyLmsView(
                                        title = "Belum Ada Pengumpulan",
                                        description = "Belum ada siswa di kelas $kelasAktif yang mengumpulkan."
                                    )
                                }
                            } else {
                                items(listPengumpulan, key = { it.idUser }) { siswa ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (siswa.isMengumpulkan) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceContainerLow,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = siswa.nama,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "NIS: ${siswa.nis}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                if (siswa.isMengumpulkan) {
                                                    Text(
                                                        text = "Kumpul: ${siswa.waktuKumpul}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (siswa.isMengumpulkan) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.errorContainer
                                            ) {
                                                Text(
                                                    text = if (siswa.isMengumpulkan) "Sudah" else "Belum",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (siswa.isMengumpulkan) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Diskusi" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 70.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (listDiskusi.isEmpty()) {
                                    item {
                                        EmptyLmsView(
                                            title = "Belum Ada Diskusi",
                                            description = "Mulai percakapan dengan siswa di kelas $kelasAktif."
                                        )
                                    }
                                } else {
                                    items(listDiskusi, key = { it.idDiskusi }) { disk ->
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(14.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = disk.pengirimNama,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = disk.waktu,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                                Text(
                                                    text = disk.pesan,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Chat Input Bar at bottom
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                tonalElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = diskusiInput,
                                        onValueChange = { diskusiInput = it },
                                        placeholder = { Text("Tulis pesan diskusi...") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(24.dp),
                                        maxLines = 3
                                    )

                                    IconButton(
                                        onClick = {
                                            if (diskusiInput.isNotBlank()) {
                                                onSendDiskusi(diskusiInput)
                                                diskusiInput = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Send,
                                            contentDescription = "Kirim",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Fullscreen Image Dialog Viewer
            if (selectedImageUrl != null) {
                Dialog(onDismissRequest = onDismissImageDialog) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.9f))
                            .clickable { onDismissImageDialog() },
                        contentAlignment = Alignment.Center
                    ) {
                        GlideImageView(
                            url = selectedImageUrl,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                    }
                }
            }

            // Delete Confirmation Dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = onDismissDeleteDialog,
                    title = { Text("Hapus Tugas?", fontWeight = FontWeight.Bold) },
                    text = {
                        Text("Apakah Anda yakin ingin menghapus tugas ini? Semua data pengumpulan siswa beserta file jawabannya akan terhapus permanen.")
                    },
                    confirmButton = {
                        TextButton(onClick = onConfirmDelete) {
                            Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onDismissDeleteDialog) {
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
fun DetailLMSScreenPreview() {
    AppTheme {
        DetailLMSScreen(
            judulTugas = "Tugas Pecahan Aljabar",
            deskripsi = "Selesaikan soal-soal halaman 45 buku paket matematika.",
            fileLampiran = "",
            jenis = "penugasan",
            isOnline = true,
            daftarKelas = listOf("X 1", "X 2", "X 3"),
            kelasAktif = "X 1",
            listPengumpulan = listOf(
                PengumpulanSiswa(1, "10291", "Ahmad Fauzi", "", true, "file.pdf", "2026-06-10 12:00:00", 85)
            ),
            listDiskusi = listOf(
                DiskusiLMS(1, "guru", "Bapak Guru", "Kumpulkan tepat waktu ya.", "2026-06-09 10:00:00")
            ),
            isLoading = false,
            selectedImageUrl = null,
            showDeleteDialog = false,
            onBackClick = {},
            onSelectKelas = {},
            onSendDiskusi = {},
            onImageViewClick = {},
            onDismissImageDialog = {},
            onDeleteClick = {},
            onDismissDeleteDialog = {},
            onConfirmDelete = {}
        )
    }
}
