@file:OptIn(ExperimentalMaterial3Api::class)

package id.rudissp.esmansa.metu

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class TambahMetu : ComponentActivity() {
    private lateinit var prefs: SharedPreferences
    private var idz = ""
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)

        val mode = intent.getStringExtra("mode") ?: ""
        isEditMode = mode == "edit"
        val izin = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("izin", Metu::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("izin") as? Metu
        }

        idz = izin?.id ?: ""
        val initNama = izin?.nama ?: ""
        val initKelas = izin?.kelas ?: ""
        val initJam1 = izin?.jam1 ?: ""
        val initJam2 = izin?.jam2 ?: ""
        val initGuru = izin?.guru ?: prefs.getString("namalengkap", "") ?: ""
        val initAlasan = izin?.alasan ?: ""
        val initTanggal = izin?.tanggal ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val initFoto = izin?.foto ?: ""

        setContent {
            AppTheme {
                TambahMetuScreen(
                    isEditMode = isEditMode,
                    initNama = initNama,
                    initKelas = initKelas,
                    initJam1 = initJam1,
                    initJam2 = initJam2,
                    initGuru = initGuru,
                    initAlasan = initAlasan,
                    initTanggal = initTanggal,
                    initFoto = initFoto,
                    onBackClick = { finish() },
                    onSave = { nama, kelas, jam1, jam2, guru, alasan, tanggal, uriFoto ->
                        simpanData(idz, isEditMode, nama, kelas, jam1, jam2, guru, alasan, tanggal, uriFoto)
                    }
                )
            }
        }
    }

    private fun simpanData(
        id: String,
        isEdit: Boolean,
        nama: String,
        kelas: String,
        jam1: String,
        jam2: String,
        guru: String,
        alasan: String,
        tanggal: String,
        uriFoto: Uri?
    ) {
        val url = if (isEdit) AppConstants.EDIT_METU else AppConstants.SAVE_METU
        val queue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { _ ->
                Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message ?: "Network error"}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                if (isEdit) {
                    params["id"] = id
                }
                params["nama"] = nama
                params["kelas"] = kelas
                params["jamke1"] = jam1
                params["jamke2"] = jam2
                params["guru"] = guru
                params["keperluan"] = alasan
                params["tanggal"] = tanggal
                uriFoto?.let {
                    params["foto"] = uriToCompressedBase64(it)
                }
                return params
            }
        }
        stringRequest.retryPolicy = DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream)
            Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
        } catch (_: Exception) {
            ""
        }
    }
}

@Composable
fun TambahMetuScreen(
    isEditMode: Boolean,
    initNama: String,
    initKelas: String,
    initJam1: String,
    initJam2: String,
    initGuru: String,
    initAlasan: String,
    initTanggal: String,
    initFoto: String,
    onBackClick: () -> Unit,
    onSave: (String, String, String, String, String, String, String, Uri?) -> Unit
) {
    val context = LocalContext.current
    var nama by remember { mutableStateOf(initNama) }
    var kelas by remember { mutableStateOf(initKelas) }
    var jam1 by remember { mutableStateOf(initJam1) }
    var jam2 by remember { mutableStateOf(initJam2) }
    var guru by remember { mutableStateOf(initGuru) }
    var alasan by remember { mutableStateOf(initAlasan) }
    var tanggal by remember { mutableStateOf(initTanggal) }

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) selectedUri = uri
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
                        text = if (isEditMode) "Edit Izin Keluar" else "Buat Izin Keluar",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
                        if (nama.isBlank() || kelas.isBlank() || jam1.isBlank() || jam2.isBlank() || guru.isBlank() || alasan.isBlank() || tanggal.isBlank()) {
                            Toast.makeText(context, "Lengkapi semua data", Toast.LENGTH_SHORT).show()
                        } else {
                            onSave(nama, kelas, jam1, jam2, guru, alasan, tanggal, selectedUri)
                        }
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .navigationBarsPadding()
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
                        text = "Simpan Izin Keluar",
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
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Banner
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
                        text = if (isEditMode) "Perbarui Izin Siswa" else "Formulir Izin Keluar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Lengkapi data siswa, jam pelajaran, keperluan, dan foto dokumentasi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Form Fields Card
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
                        text = "Detail Siswa & Keterangan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = nama,
                        onValueChange = { nama = it },
                        label = { Text("Nama Siswa") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = kelas,
                        onValueChange = { kelas = it },
                        label = { Text("Kelas") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = jam1,
                            onValueChange = { jam1 = it },
                            label = { Text("Jam Mulai") },
                            leadingIcon = {
                                Icon(Icons.Rounded.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = jam2,
                            onValueChange = { jam2 = it },
                            label = { Text("Jam Selesai") },
                            leadingIcon = {
                                Icon(Icons.Rounded.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = guru,
                        onValueChange = { guru = it },
                        label = { Text("Guru Pembuat / Piket") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tanggal,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tanggal") },
                        leadingIcon = {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        shape = RoundedCornerShape(20.dp),
                        trailingIcon = {
                            IconButton(onClick = { datePicker.show() }) {
                                Icon(Icons.Rounded.CalendarToday, contentDescription = "Pilih Tanggal")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePicker.show() }
                    )

                    OutlinedTextField(
                        value = alasan,
                        onValueChange = { alasan = it },
                        label = { Text("Keperluan / Alasan") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        shape = RoundedCornerShape(20.dp),
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Photo Upload Card
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
                        text = "Foto Dokumentasi Siswa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable { launcher.launch("image/*") }
                    ) {
                        if (selectedUri != null || initFoto.isNotBlank()) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { ctx ->
                                    ImageView(ctx).apply {
                                        scaleType = ImageView.ScaleType.CENTER_CROP
                                    }
                                },
                                update = { iv ->
                                    if (selectedUri != null) {
                                        iv.setImageURI(selectedUri)
                                    } else if (initFoto.isNotBlank()) {
                                        Glide.with(iv.context)
                                            .load(AppConstants.BASE_URL + "fotometu/" + initFoto)
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
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ketuk untuk Ambil / Unggah Foto",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TambahMetuScreenPreview() {
    AppTheme {
        TambahMetuScreen(
            isEditMode = false,
            initNama = "",
            initKelas = "",
            initJam1 = "",
            initJam2 = "",
            initGuru = "Rudi Susanto, S.Pd.",
            initAlasan = "",
            initTanggal = "2024-10-12",
            initFoto = "",
            onBackClick = {},
            onSave = { _, _, _, _, _, _, _, _ -> }
        )
    }
}
