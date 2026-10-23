package id.rudissp.esmansa.jadwal

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.AppTheme
import org.json.JSONArray
import org.json.JSONObject

class JadwalV2 : ComponentActivity() {

    private lateinit var prefs: SharedPreferences
    private val listDataJadwal = mutableStateListOf<HariModel>()
    private val listPencarian = mutableStateListOf<String>()
    private val gukarMap = HashMap<String, String>()

    private var isLoadingState = mutableStateOf(true)
    private var isRefreshingState = mutableStateOf(false)
    private var titleState = mutableStateOf("Jadwal Pelajaran")

    private var myName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        myName = prefs.getString("namalengkap", "") ?: ""
        titleState.value = myName.ifBlank { "Jadwal Pelajaran" }

        loadDataPencarian()
        fetchJadwalGuru(myName)

        setContent {
            AppTheme {
                JadwalScreen(
                    title = titleState.value,
                    listHari = listDataJadwal,
                    listPencarian = listPencarian,
                    isLoading = isLoadingState.value,
                    isRefreshing = isRefreshingState.value,
                    onBackClick = { finish() },
                    onRefresh = {
                        isRefreshingState.value = true
                        fetchJadwalGuru(myName)
                    },
                    onSelectTarget = { target ->
                        titleState.value = target
                        val kelasArray = resources.getStringArray(id.rudissp.esmansa.R.array.kelasnya)
                        if (kelasArray.contains(target)) {
                            fetchJadwalKelas(target)
                        } else {
                            val username = gukarMap[target] ?: target
                            fetchJadwalGuru(username)
                        }
                    },
                    onResetToMyJadwal = {
                        titleState.value = myName.ifBlank { "Jadwal Pelajaran" }
                        fetchJadwalGuru(myName)
                    }
                )
            }
        }
    }

    private fun loadDataPencarian() {
        val kelasArray = resources.getStringArray(id.rudissp.esmansa.R.array.kelasnya)
        listPencarian.clear()
        listPencarian.addAll(kelasArray)

        val request = StringRequest(
            Request.Method.GET, AppConstants.LIST_GURU,
            { response ->
                try {
                    val jsonArray = JSONArray(response)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val nama = obj.getString("nama")
                        val username = obj.getString("username")
                        gukarMap[nama] = username
                        if (!listPencarian.contains(nama)) {
                            listPencarian.add(nama)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun fetchJadwalGuru(namaGuru: String?) {
        val url = AppConstants.BASE_URL + "api_jadwal_guru.php?guru=$namaGuru"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                isLoadingState.value = false
                isRefreshingState.value = false
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        listDataJadwal.clear()
                        val jsonArrayData = jsonResponse.getJSONArray("data")

                        for (i in 0 until jsonArrayData.length()) {
                            val hariObj = jsonArrayData.getJSONObject(i)
                            val namaHari = hariObj.getString("hari")
                            val jadwalArray = hariObj.getJSONArray("jadwal")

                            val listJadwalSementara = ArrayList<JadwalItemModel>()

                            for (j in 0 until jadwalArray.length()) {
                                val jd = jadwalArray.getJSONObject(j)
                                listJadwalSementara.add(
                                    JadwalItemModel(
                                        jamKe = jd.getInt("jam_ke"),
                                        status = jd.getString("status"),
                                        mapel = jd.getString("mapel"),
                                        infoBawah = jd.optString("kelas", "")
                                    )
                                )
                            }
                            listDataJadwal.add(HariModel(namaHari, listJadwalSementara))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                isLoadingState.value = false
                isRefreshingState.value = false
                Toast.makeText(this, "Gagal memuat jadwal", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun fetchJadwalKelas(namaKelas: String) {
        isLoadingState.value = true
        val url = AppConstants.API_JADWAL_KELAS + "?kelas=$namaKelas"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                isLoadingState.value = false
                isRefreshingState.value = false
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        listDataJadwal.clear()
                        val jsonArrayData = jsonResponse.getJSONArray("data")

                        for (i in 0 until jsonArrayData.length()) {
                            val hariObj = jsonArrayData.getJSONObject(i)
                            val namaHari = hariObj.getString("hari")
                            val jadwalArray = hariObj.getJSONArray("jadwal")

                            val listJadwalSementara = ArrayList<JadwalItemModel>()

                            for (j in 0 until jadwalArray.length()) {
                                val jd = jadwalArray.getJSONObject(j)
                                listJadwalSementara.add(
                                    JadwalItemModel(
                                        jamKe = jd.getInt("jam_ke"),
                                        status = jd.getString("status"),
                                        mapel = jd.getString("mapel"),
                                        infoBawah = jd.optString("nama_guru", "")
                                    )
                                )
                            }
                            listDataJadwal.add(HariModel(namaHari, listJadwalSementara))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                isLoadingState.value = false
                isRefreshingState.value = false
                Toast.makeText(this, "Gagal memuat jadwal kelas", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }
}
