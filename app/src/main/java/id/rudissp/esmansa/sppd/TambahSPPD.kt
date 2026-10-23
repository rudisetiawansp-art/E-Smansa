@file:OptIn(ExperimentalMaterial3Api::class)

package id.rudissp.esmansa.sppd

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.AppTheme
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TambahSppdActivity : ComponentActivity() {
    private lateinit var prefs: SharedPreferences
    private var idz = ""
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)

        val mode = intent.getStringExtra("mode") ?: ""
        isEditMode = mode == "editmode"
        idz = intent.getStringExtra("id") ?: ""

        val initNama = intent.getStringExtra("nama") ?: ""
        val initJabatan = intent.getStringExtra("jabatan") ?: ""
        val initNip = intent.getStringExtra("nip") ?: ""
        val initTanggal = intent.getStringExtra("tanggal") ?: SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(Date())
        val initLokasi = intent.getStringExtra("lokasi") ?: ""
        val initDeskripsi = intent.getStringExtra("deskripsi") ?: ""
        val initFoto = intent.getStringExtra("foto") ?: ""
        val initFotoLokasi = intent.getStringExtra("foto_lokasi") ?: ""

        setContent {
            AppTheme {
                TambahSppdScreen(
                    isEditMode = isEditMode,
                    initNama = initNama,
                    initJabatan = initJabatan,
                    initNip = initNip,
                    initTanggal = initTanggal,
                    initLokasi = initLokasi,
                    initDeskripsi = initDeskripsi,
                    initFoto = initFoto,
                    initFotoLokasi = initFotoLokasi,
                    onBackClick = { finish() },
                    onSave = { nama, username, jabatan, nip, tanggal, lokasi, deskripsi, uriFoto1, uriFoto2 ->
                        if (isEditMode) {
                            editSppd(
                                idz,
                                username,
                                nama,
                                jabatan,
                                nip,
                                tanggal,
                                lokasi,
                                deskripsi,
                                uriFoto1,
                                uriFoto2
                            )
                        } else {
                            submitData(
                                username,
                                nama,
                                jabatan,
                                nip,
                                tanggal,
                                lokasi,
                                deskripsi,
                                uriFoto1,
                                uriFoto2
                            )
                        }
                    }
                )
            }
        }
    }

    private fun submitData(
        username: String,
        nama: String,
        jabatan: String,
        nip: String,
        tanggal: String,
        lokasi: String,
        deskripsi: String,
        uriFoto1: Uri?,
        uriFoto2: Uri?
    ) {
        val fotoBase64 = uriFoto1?.let { uriToCompressedBase64(it) } ?: ""
        val fotoBase642 = uriFoto2?.let { uriToCompressedBase64(it) } ?: ""

        val queue = Volley.newRequestQueue(this)
        val url = AppConstants.TAMBH_SPPD

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { _ ->
                Toast.makeText(
                    this,
                    "Laporan Perjalanan Dinas berhasil ditambahkan",
                    Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(this, SPPDActivity::class.java))
                finish()
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf(
                    "username" to username,
                    "nama" to nama,
                    "jabatan" to jabatan,
                    "nip" to nip,
                    "tanggal" to tanggal,
                    "lokasi" to lokasi,
                    "deskripsi" to deskripsi,
                    "foto" to fotoBase64,
                    "foto_lokasi" to fotoBase642
                )
            }
        }
        stringRequest.retryPolicy =
            DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        queue.add(stringRequest)
    }

    private fun editSppd(
        id: String,
        username: String,
        nama: String,
        jabatan: String,
        nip: String,
        tanggal: String,
        lokasi: String,
        deskripsi: String,
        uriFoto1: Uri?,
        uriFoto2: Uri?
    ) {
        val queue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(
            Method.POST, AppConstants.EDIT_SPPD,
            { _ ->
                Toast.makeText(this, "Berhasil mengedit SPPD", Toast.LENGTH_SHORT).show()
                finish()
            },
            { error ->
                Toast.makeText(this, "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = id
                params["username"] = username
                params["nama"] = nama
                params["jabatan"] = jabatan
                params["nip"] = nip
                params["tanggal"] = tanggal
                params["lokasi"] = lokasi
                params["deskripsi"] = deskripsi

                uriFoto1?.let { params["foto"] = uriToCompressedBase64(it) }
                uriFoto2?.let { params["foto_lokasi"] = uriToCompressedBase64(it) }
                return params
            }
        }
        queue.add(stringRequest)
    }

    private fun uriToCompressedBase64(uri: Uri): String {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
            Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
        } catch (_: Exception) {
            ""
        }
    }
}

@Composable
fun TambahSppdScreen(
    isEditMode: Boolean,
    initNama: String,
    initJabatan: String,
    initNip: String,
    initTanggal: String,
    initLokasi: String,
    initDeskripsi: String,
    initFoto: String,
    initFotoLokasi: String,
    onBackClick: () -> Unit,
    onSave: (String, String, String, String, String, String, String, Uri?, Uri?) -> Unit
) {
    val context = LocalContext.current
    var nama by remember { mutableStateOf(initNama) }
    var username by remember { mutableStateOf("") }
    var jabatan by remember { mutableStateOf(initJabatan) }
    var nip by remember { mutableStateOf(initNip) }
    var tanggal by remember { mutableStateOf(initTanggal) }
    var lokasi by remember { mutableStateOf(initLokasi) }
    var deskripsi by remember { mutableStateOf(initDeskripsi) }

    var selectedUri1 by remember { mutableStateOf<Uri?>(null) }
    var selectedUri2 by remember { mutableStateOf<Uri?>(null) }

    val launcher1 = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) selectedUri1 = uri
    }
    val launcher2 = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) selectedUri2 = uri
    }

    var gukarMap by remember { mutableStateOf(mapOf<String, String>()) }
    var listGukar by remember { mutableStateOf(listOf<String>()) }
    val jabatanList = listOf("Kepala Sekolah", "Guru", "Karyawan", "Pembina Ekstra")

    var expandedNama by remember { mutableStateOf(false) }
    var expandedJabatan by remember { mutableStateOf(false) }

    // Load users (Gukar)
    LaunchedEffect(Unit) {
        val request = StringRequest(
            Request.Method.GET, AppConstants.GET_SEMUA_USER,
            { response ->
                val jsonArray = JSONArray(response)
                val map = mutableMapOf<String, String>()
                val list = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val n = obj.optString("nama")
                    val u = obj.optString("username")
                    map[n] = u
                    list.add(n)
                }
                gukarMap = map
                listGukar = list
            },
            { }
        )
        Volley.newRequestQueue(context).add(request)
    }

    val calendar = Calendar.getInstance()
    val datePicker = DatePickerDialog(
        context,
        { _, y, m, d ->
            calendar.set(y, m, d)
            tanggal = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit SPPD" else "Buat SPPD",
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

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                )
            )
        },

        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        username = gukarMap[nama] ?: ""
                        if (nama.isBlank() || jabatan.isBlank() || nip.isBlank() || tanggal.isBlank() || lokasi.isBlank() || deskripsi.isBlank()) {
                            Toast.makeText(context, "Lengkapi semua data", Toast.LENGTH_SHORT).show()
                        } else {
                            onSave(nama, username, jabatan, nip, tanggal, lokasi, deskripsi, selectedUri1, selectedUri2)
                        }
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 12.dp) // Paling atas biar jadi margin luar tombol
                        .navigationBarsPadding() // Biar aman dari tombol/gesture bar HP
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simpan SPPD",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Banner Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isEditMode) "Perbarui Form SPPD" else "Formulir Tugas SPPD",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Lengkapi detail identitas pegawai, tempat tujuan, serta dokumentasi tugas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Main Form Container Card
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Detail Pegawai & Agenda",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Nama Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedNama && listGukar.isNotEmpty(),
                        onExpandedChange = { expandedNama = it }
                    ) {
                        OutlinedTextField(
                            value = nama,
                            onValueChange = {
                                nama = it
                                expandedNama = true
                            },
                            label = { Text("Nama Pegawai / Guru") },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNama) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(
                                    ExposedDropdownMenuAnchorType.PrimaryEditable,
                                    enabled = true
                                )
                        )
                        if (listGukar.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = expandedNama,
                                onDismissRequest = { expandedNama = false }
                            ) {
                                listGukar.filter { it.contains(nama, ignoreCase = true) }
                                    .forEach { suggestion ->
                                        DropdownMenuItem(
                                            text = { Text(suggestion) },
                                            onClick = {
                                                nama = suggestion
                                                expandedNama = false
                                            }
                                        )
                                    }
                            }
                        }
                    }

                    // Jabatan Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedJabatan,
                        onExpandedChange = { expandedJabatan = it }
                    ) {
                        OutlinedTextField(
                            value = jabatan,
                            onValueChange = { jabatan = it },
                            label = { Text("Jabatan") },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Work,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJabatan) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(
                                    ExposedDropdownMenuAnchorType.PrimaryEditable,
                                    enabled = true
                                )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedJabatan,
                            onDismissRequest = { expandedJabatan = false }
                        ) {
                            jabatanList.forEach { j ->
                                DropdownMenuItem(
                                    text = { Text(j) },
                                    onClick = {
                                        jabatan = j
                                        expandedJabatan = false
                                    }
                                )
                            }
                        }
                    }

                    // NIP
                    OutlinedTextField(
                        value = nip,
                        onValueChange = { nip = it },
                        label = { Text("NIP") },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Badge,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Tanggal Perjalanan
                    OutlinedTextField(
                        value = tanggal,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tanggal Perjalanan") },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        trailingIcon = {
                            IconButton(onClick = { datePicker.show() }) {
                                Icon(
                                    Icons.Rounded.CalendarToday,
                                    contentDescription = "Pilih Tanggal"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePicker.show() }
                    )

                    // Lokasi Tujuan
                    OutlinedTextField(
                        value = lokasi,
                        onValueChange = { lokasi = it },
                        label = { Text("Lokasi Tujuan") },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Deskripsi / Keperluan
                    OutlinedTextField(
                        value = deskripsi,
                        onValueChange = { deskripsi = it },
                        label = { Text("Deskripsi / Keperluan") },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Photo Upload Section Card
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Dokumentasi & Lampiran",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Foto 1 - Surat
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Foto Surat SPPD",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clickable { launcher1.launch("image/*") }
                            ) {
                                if (selectedUri1 != null || initFoto.isNotBlank()) {
                                    AndroidView(
                                        modifier = Modifier.fillMaxSize(),
                                        factory = { ctx ->
                                            ImageView(ctx).apply {
                                                scaleType = ImageView.ScaleType.CENTER_CROP
                                            }
                                        },
                                        update = { iv ->
                                            if (selectedUri1 != null) {
                                                iv.setImageURI(selectedUri1)
                                            } else if (initFoto.isNotBlank()) {
                                                Glide.with(iv.context)
                                                    .load(AppConstants.BASE_URL + initFoto).into(iv)
                                            }
                                        }
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddAPhoto,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Upload Foto",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Foto 2 - Lokasi
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Foto Lokasi / Kegiatan",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clickable { launcher2.launch("image/*") }
                            ) {
                                if (selectedUri2 != null || initFotoLokasi.isNotBlank()) {
                                    AndroidView(
                                        modifier = Modifier.fillMaxSize(),
                                        factory = { ctx ->
                                            ImageView(ctx).apply {
                                                scaleType = ImageView.ScaleType.CENTER_CROP
                                            }
                                        },
                                        update = { iv ->
                                            if (selectedUri2 != null) {
                                                iv.setImageURI(selectedUri2)
                                            } else if (initFotoLokasi.isNotBlank()) {
                                                Glide.with(iv.context)
                                                    .load(AppConstants.BASE_URL + initFotoLokasi)
                                                    .into(iv)
                                            }
                                        }
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Image,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Upload Lokasi",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    username = gukarMap[nama] ?: ""
                    if (nama.isBlank() || jabatan.isBlank() || nip.isBlank() || tanggal.isBlank() || lokasi.isBlank() || deskripsi.isBlank()) {
                        Toast.makeText(context, "Lengkapi semua data", Toast.LENGTH_SHORT).show()
                    } else {
                        onSave(
                            nama,
                            username,
                            jabatan,
                            nip,
                            tanggal,
                            lokasi,
                            deskripsi,
                            selectedUri1,
                            selectedUri2
                        )
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier
                    .padding(end = 12.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Simpan",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TambahSppdScreenPreview() {
    AppTheme {
        TambahSppdScreen(
            isEditMode = false,
            initNama = "",
            initJabatan = "",
            initNip = "",
            initTanggal = "2026-01-03",
            initLokasi = "",
            initDeskripsi = "",
            initFoto = "",
            initFotoLokasi = "",
            onBackClick = {},
            onSave = { _, _, _, _, _, _, _, _, _ -> }
        )
    }
}