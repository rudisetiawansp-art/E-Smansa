package id.rudissp.esmansa

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

private val AppLightColorScheme = lightColorScheme(
    primary = Color(0xFFAC153A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCF3350),
    onPrimaryContainer = Color(0xFFFFF2F2),
    secondary = Color(0xFF99434D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFC929B),
    onSecondaryContainer = Color(0xFF772833),
    tertiary = Color(0xFF8A4400),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFAF5800),
    onTertiaryContainer = Color(0xFFFFF2EC),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFFFF8F7),
    onBackground = Color(0xFF261819),
    surface = Color(0xFFFFF8F7),
    onSurface = Color(0xFF261819),
    surfaceVariant = Color(0xFFFFDADB),
    onSurfaceVariant = Color(0xFF594042),
    outline = Color(0xFF8D7071),
    outlineVariant = Color(0xFFE1BEBF),
    inverseSurface = Color(0xFF3D2C2D),
    inverseOnSurface = Color(0xFFFFECED),
    inversePrimary = Color(0xFFFFB2B8),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFF0F0),
    surfaceContainer = Color(0xFFFFE9E9),
    surfaceContainerHigh = Color(0xFFFDE2E3),
    surfaceContainerHighest = Color(0xFFF7DCDD)
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppLightColorScheme,
        content = content
    )
}

object LoadingIndicatorDefaults {
    val indicatorColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.primary

    // Poligon ekspresif dengan variasi morphing dari 4 sudut ke 8 sudut yang lebih membulat
    val DeterminateIndicatorPolygons: List<RoundedPolygon> = listOf(
        RoundedPolygon(numVertices = 4, rounding = CornerRounding(0.35f)),
        RoundedPolygon(numVertices = 8, rounding = CornerRounding(0.25f))
    )
}

@Composable
fun LoadingIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = LoadingIndicatorDefaults.indicatorColor,
    polygons: List<RoundedPolygon> = LoadingIndicatorDefaults.DeterminateIndicatorPolygons
) {
    val currentProgress = progress()
    val morph = remember(polygons) {
        if (polygons.size >= 2) Morph(polygons[0], polygons[1]) else null
    }

    Box(
        modifier = modifier
            .drawWithCache {
                val androidPath = android.graphics.Path()
                if (morph != null) {
                    morph.toPath(progress = currentProgress.coerceIn(0f, 1f), path = androidPath)
                } else if (polygons.isNotEmpty()) {
                    polygons[0].toPath(androidPath)
                }
                val path = androidPath.asComposePath()

                val bounds = path.getBounds()
                val matrix = Matrix()
                if (bounds.width > 0 && bounds.height > 0) {
                    val scale = minOf(size.width / bounds.width, size.height / bounds.height)
                    matrix.scale(scale, scale)
                    matrix.translate(-bounds.left, -bounds.top)
                    path.transform(matrix)
                }

                onDrawBehind {
                    drawPath(path = path, color = color)
                }
            }
    )
}

class Splash : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    private val _statusMessage = mutableStateOf("Memeriksa pembaruan sistem...")
    private val _isLoading = mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)

        setContent {
            AppTheme {
                SplashContent(
                    statusMessage = _statusMessage.value,
                    isLoading = _isLoading.value
                )
            }
        }

        if (!isInternetAvailable()) {
            Toast.makeText(this, "Tidak ada koneksi internet", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        checkDeviceAndVersion()
    }

    private fun checkDeviceAndVersion() {
        val localVersionCode = BuildConfig.VERSION_CODE
        val url = AppConstants.CEK_APP_VERSION

        val username = prefs.getString("username", "") ?: ""
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        if (!isLoggedIn || username.isEmpty()) {
            continueToNextScreen()
            return
        }

        _statusMessage.value = "Menghubungkan ke server..."

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)

                    if (json.has("status") && json.getString("status") == "error") {
                        val errorMessage = json.getString("message")
                        _isLoading.value = false
                        showErrorDialogAndExit(errorMessage)
                    } else {
                        val serverVersionCode = json.getInt("version_code")
                        val serverMessage = json.getString("message")
                        val downloadUrl = json.getString("url")

                        if (serverVersionCode > localVersionCode) {
                            _isLoading.value = false
                            showUpdateDialog(serverMessage, downloadUrl)
                        } else {
                            _statusMessage.value = "Memuat aplikasi..."
                            continueToNextScreen()
                        }
                    }
                } catch (_: Exception) {
                    _isLoading.value = false
                    showErrorDialogAndExit("Terjadi kesalahan saat memproses data.")
                }
            },
            {
                _isLoading.value = false
                Toast.makeText(this, "Gagal terhubung ke server, periksa koneksi anda.", Toast.LENGTH_SHORT).show()
                finish()
            }
        ) {
            @SuppressLint("HardwareIds")
            override fun getParams(): MutableMap<String, String> {
                val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                val params = HashMap<String, String>()
                params["username"] = username
                params["device_id"] = deviceId
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun showUpdateDialog(message: String, downloadUrl: String) {
        AlertDialog.Builder(this)
            .setTitle("Update Tersedia")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Update Sekarang") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, downloadUrl.toUri())
                startActivity(intent)
            }
            .setNegativeButton("Keluar") { _, _ ->
                finish()
            }
            .show()
    }

    private fun showErrorDialogAndExit(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Keluar") { _, _ ->
                finish()
            }
            .show()
    }

    private fun continueToNextScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            val isLoggedIn = prefs.getBoolean("is_logged_in", false)
            Log.d("SplashActivity", "Status login: $isLoggedIn")
            if (isLoggedIn) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }, 1000)
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

@Composable
fun SplashContent(statusMessage: String, isLoading: Boolean) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceContainerLowest,
                            MaterialTheme.colorScheme.surfaceContainerLow,
                            MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                )
                .systemBarsPadding()
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-16.dp))
            ) {
                // Ikon Aplikasi Ekspresif dengan radius 40.dp
                Surface(
                    shape = RoundedCornerShape(40.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 8.dp,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(140.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                            contentDescription = "Logo E-Smansa",
                            modifier = Modifier.size(104.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Selamat datang di",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "E-Smansa",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                if (isLoading) {
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

                    // Wadah Loading Indicator bergaya Material 3 Expressive
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 2.dp,
                        modifier = Modifier.size(76.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LoadingIndicator(
                                progress = { animatedProgress.value },
                                modifier = Modifier.size(44.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            Text(
                text = "Developed by\nKurikulum SMAN 1 Sukorejo",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun SplashContentPreview() {
    AppTheme {
        SplashContent(
            statusMessage = "Memeriksa pembaruan sistem...",
            isLoading = true
        )
    }
}