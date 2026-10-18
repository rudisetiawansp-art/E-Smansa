package id.rudissp.esmansa.presensi

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityPresensiBinding
import org.json.JSONException
import org.json.JSONObject
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class PresensiActivity : AppCompatActivity() {

    lateinit var binding: ActivityPresensiBinding
    lateinit var prefs: SharedPreferences
    lateinit var prefs2: SharedPreferences

    //ini lokasi skj
    private val targetLatitude = -7.090535290590152
    private val targetLongitude = 110.04503560697529

    //lokasi rumahku
    //private val targetLatitude = -6.93003
    //private val targetLongitude = 110.2017

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapController: IMapController
    private var userLocation: Location? = null
    private var userMarker: Marker? = null
    private lateinit var mapView: MapView

    //private var radius: Double = 30.0
    private var radius: Double = 40.0
    private var presensiRadiusPolygon: Polygon? = null
    private var isShiftMalam: Boolean = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPresensiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        prefs2 = this.getSharedPreferences("osmdroid", MODE_PRIVATE)
        Configuration.getInstance().load(this, prefs2)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapController = mapView.controller
        mapController.setZoom(20.0)

        val namalengkap = prefs.getString("namalengkap", "")
        binding.topAppBar.title = "Presensi $namalengkap"
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        binding.fragmentContainer.visibility = View.GONE

        var isInitialSetup = true

        binding.shiftToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isInitialSetup) return@addOnButtonCheckedListener

            if (isChecked) {
                when (checkedId) {
                    R.id.btnShiftPagi -> {
                        isShiftMalam = false
                        Toast.makeText(this, "Shift Pagi Aktif", Toast.LENGTH_SHORT).show()
                        checkDistanceAndUpdateUI()
                    }
                    R.id.btnShiftMalam -> {
                        isShiftMalam = true
                        Toast.makeText(this, "Shift Malam Aktif", Toast.LENGTH_SHORT).show()
                        checkDistanceAndUpdateUI()
                    }
                }
            }
        }
        binding.shiftToggleGroup.post {
            binding.shiftToggleGroup.check(R.id.btnShiftPagi) // <- Set tombol Shift Pagi sebagai default
            isInitialSetup = false
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.presensi -> {
                    binding.fragmentContainer.removeAllViews()
                    binding.fragmentContainer.visibility = View.GONE
                }

                R.id.rekap -> {
                    binding.fragmentContainer.visibility = View.VISIBLE
                    loadFragment(RekapPresensiFragment())
                }

                R.id.rekap_harian -> {
                    if (prefs.getString("role", "") == "admin") {
                        binding.fragmentContainer.visibility = View.VISIBLE
                        loadFragment(FragmentRekapHarian())
                    } else {
                        Toast.makeText(
                            this,
                            "Anda tidak bisa mengakses fitur ini",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                R.id.tambahizin -> {
                    if (prefs.getString("role", "") == "admin") {
                        binding.fragmentContainer.visibility = View.VISIBLE
                        loadFragment(FragmentEditPresensi())
                    } else {
                        Toast.makeText(
                            this,
                            "Anda tidak bisa mengakses fitur ini",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                R.id.settinghb -> {
                    if (prefs.getString("role", "") == "admin") {
                        binding.fragmentContainer.visibility = View.VISIBLE
                        loadFragment(FragmentHB())
                    } else {
                        Toast.makeText(
                            this,
                            "Anda tidak bisa mengakses fitur ini",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }
            true
        }

        setupTargetMarker()
        getUserLocation()
        val username = prefs.getString("username", "") ?: ""
        cekStatusPresensiDariServer(username)

        binding.btnPresensi.setOnClickListener { presensi("masuk") }
        binding.btnPresensiPulang.setOnClickListener { presensi("pulang") }
        binding.btnRefresh.setOnClickListener { getUserLocation() }

    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun setupTargetMarker() {
        val targetPoint = GeoPoint(targetLatitude, targetLongitude)
        //val targetMarker = Marker(mapView).apply {
        //   position = targetPoint
        //   title = "Titik Presensi"
        //icon = ContextCompat.getDrawable(this@PresensiActivity, R.drawable.ic_presensi)
        //   setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        //}

        val geoPoint = GeoPoint(targetLatitude, targetLongitude)
        drawCircle(mapView, geoPoint, radius)

        //mapView.overlays.add(targetMarker)
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateDistanceMeters(5f)
            .setWaitForAccurateLocation(true)
            .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return

                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && location.isMock) || location.isFromMockProvider) {
                    Toast.makeText(this@PresensiActivity, "Terdeteksi fake GPS!", Toast.LENGTH_LONG)
                        .show()
                    return
                }

                userLocation = location
                updateMap(location)
                checkDistanceAndUpdateUI()
                fusedLocationClient.removeLocationUpdates(this)
                val username = prefs.getString("username", "") ?: ""
                cekStatusPresensiDariServer(username)
            }
        }, this.mainLooper)
    }

    private fun updateMap(location: Location) {
        val userLat = location.latitude
        val userLng = location.longitude

        userMarker?.let { mapView.overlays.remove(it) }
        userMarker = Marker(mapView).apply {
            position = GeoPoint(userLat, userLng)
            title = "Lokasi Anda"
            //icon = ContextCompat.getDrawable(this@PresensiActivity, R.drawable.ic_rekap)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }



        mapView.overlays.add(userMarker)
        mapController.setCenter(GeoPoint(userLat, userLng))
        mapView.invalidate()
    }

    private fun checkDistanceAndUpdateUI() {
        val location = userLocation
        if (location == null) {
            Toast.makeText(this, "Lokasi pengguna belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val distance = getDistance(location.latitude, location.longitude, targetLatitude, targetLongitude)
        binding.tvDistance.text = "Jarak dari lokasi presensi: ${"%.2f".format(distance)} meter"

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        if (distance < radius) {
            if (isShiftMalam) {
                // Shift malam: masuk 18–21, pulang 01–08
                binding.btnPresensi.visibility = if (hour in 18..22) View.VISIBLE else View.GONE
                binding.btnPresensiPulang.visibility = if (hour in 1..8) View.VISIBLE else View.GONE
            } else {
                // Shift pagi: masuk 06–10, pulang 11–22
                binding.btnPresensi.visibility = if (hour in 5..11) View.VISIBLE else View.GONE
                binding.btnPresensiPulang.visibility = if (hour in 12..22) View.VISIBLE else View.GONE
            }
        } else {
            binding.btnPresensi.visibility = View.GONE
            binding.btnPresensiPulang.visibility = View.GONE
            Toast.makeText(this, "Anda terlalu jauh dari lokasi presensi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val loc1 = Location("").apply { latitude = lat1; longitude = lon1 }
        val loc2 = Location("").apply { latitude = lat2; longitude = lon2 }
        return loc1.distanceTo(loc2).toDouble()
    }

    private fun cekStatusPresensiDariServer(username: String) {
        val tanggal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val stringRequest = object : StringRequest(
            Method.POST, AppConstants.CEK_PRESENSI,
            { response ->
                try {
                    val json = JSONObject(response)
                    val sudahMasuk = json.getBoolean("masuk")
                    val sudahPulang = json.getBoolean("pulang")

                    // Update teks status tanpa mematikan (disable) tombol
                    if (sudahMasuk && sudahPulang) {
                        binding.tvstatuspresensi.text = "Anda sudah presensi masuk dan pulang hari ini."
                    } else if (sudahMasuk) {
                        binding.tvstatuspresensi.text = "Anda sudah presensi masuk hari ini."
                    } else if (sudahPulang) {
                        binding.tvstatuspresensi.text = "Anda sudah presensi pulang hari ini."
                    }

                    if (sudahMasuk || sudahPulang) {
                        binding.tvstatuspresensi.visibility = View.VISIBLE
                    }

                    // Pastikan tombol tetap aktif agar user bisa absen berkali-kali
                    binding.btnPresensi.isEnabled = true
                    binding.btnPresensiPulang.isEnabled = true

                } catch (_: JSONException) {
                    // Jangan tampilkan toast error di sini agar tidak mengganggu UI
                }
            },
            { error ->
                Log.e("CekStatus", "Gagal koneksi: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf("username" to username, "tanggal" to tanggal)
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }


    private fun presensi(jenis: String) {
        val tanggal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val latitude = userLocation?.latitude ?: 0.0
        val longitude = userLocation?.longitude ?: 0.0
        val username = prefs.getString("username", "") ?: ""
        val shift = if (isShiftMalam) "malam" else "pagi"

        prefs.edit {
            putBoolean(
                if (jenis == "masuk") "sudah_presensi_masuk" else "sudah_presensi_pulang",
                true
            )
        }

        kirimPresensiKeServer(username, jenis, latitude, longitude, shift)
    }

    private fun kirimPresensiKeServer(
        username: String,
        jenis: String,
        latitude: Double,
        longitude: Double,
        shift: String
    ) {
        val queue = Volley.newRequestQueue(this)

        // Mengambil waktu saat ini untuk ditampilkan di dialog
        val sdfTanggal = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val sdfJam = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val currentTanggal = sdfTanggal.format(Date())
        val currentJam = sdfJam.format(Date())

        val tanggalParam = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val jamParam = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

        val stringRequest = object : StringRequest(
            Method.POST, AppConstants.PRESENSI_URL,
            { response ->
                try {
                    val json = JSONObject(response)
                    val status = json.getString("status")

                    if (status == "sukses") {
                        // Membuat Dialog Sukses
                        androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Presensi Berhasil")
                            .setMessage("Anda berhasil melakukan presensi pada $currentTanggal pukul $currentJam")
                            .setPositiveButton("OK") { _, _ ->
                                finish() // Menutup PresensiActivity
                            }
                            .setCancelable(false) // User harus menekan OK
                            .show()
                    } else {
                        Toast.makeText(this, json.getString("pesan"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    // Jika index unik di DB belum dihapus, biasanya lari ke sini karena respon bukan JSON
                    Toast.makeText(this, "Berhasil simpan, tapi respon error. Cek Database.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            },
            { error ->
                Log.e("Presensi", "Error: ${error.message}", error)
                Toast.makeText(this, "Gagal koneksi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "username" to username,
                    "jenis" to jenis,
                    "latitude" to latitude.toString(),
                    "longitude" to longitude.toString(),
                    "tanggal" to tanggalParam,
                    "jam" to jamParam,
                    "shift" to shift
                )
            }
        }

        stringRequest.retryPolicy = DefaultRetryPolicy(
            10_000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        queue.add(stringRequest)
    }

    private fun drawCircle(mapView: MapView, center: GeoPoint, radius: Double) {
        val circlePoints = mutableListOf<GeoPoint>()

        val numPoints = 360
        for (i in 0 until numPoints) {
            val angle = i * (PI / 180) // Convert degree to radians
            val latOffset = radius * sin(angle) / 100000 // Adjust the scaling
            val lonOffset = radius * cos(angle) / 100000 // Adjust the scaling

            val newPoint = GeoPoint(center.latitude + latOffset, center.longitude + lonOffset)
            circlePoints.add(newPoint)
        }

        // Menghapus radius lama jika ada
        presensiRadiusPolygon?.let {
            mapView.overlays.remove(it)
        }

        // Membuat polygon baru untuk radius
        presensiRadiusPolygon = Polygon()
        presensiRadiusPolygon?.points = circlePoints
        presensiRadiusPolygon?.fillColor = 0x220000FF // Semi-transparent blue
        presensiRadiusPolygon?.strokeColor = 0xFF000FF // Solid blue border

        // Menambahkan polygon baru ke peta
        mapView.overlays.add(presensiRadiusPolygon)
    }


}