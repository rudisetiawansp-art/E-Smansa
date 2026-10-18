package id.rudissp.esmansa.admin

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.R
import id.rudissp.esmansa.databinding.ActivityEditUserBinding
import org.json.JSONObject

class EditUserActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditUserBinding
    lateinit var prefs: SharedPreferences

    // Variabel untuk menyimpan username awal (sebelum diedit) sebagai kunci pencarian di database
    private var oldUsername: String = ""
    private var modeForm: String = "tambah"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)

        // Ambil data mode dari Intent
        modeForm = intent.getStringExtra("mode") ?: "tambah"

        // ==========================================
        // CABANG LOGIKA BERDASARKAN MODE
        // ==========================================
        when (modeForm) {
            "user_edit" -> {
                oldUsername = prefs.getString("username", "") ?: ""

                binding.topAppBarUserinfo.title = "Edit Profil Saya"
                binding.resetID.visibility = View.GONE
                binding.hapususer.visibility = View.GONE
                binding.topAppBarUserinfo.menu.findItem(R.id.menunya_update)?.isVisible = true
                binding.topAppBarUserinfo.menu.findItem(R.id.menunya_simpan)?.isVisible = false

                loadUserDetail(oldUsername)
            }
            "edit" -> {
                oldUsername = intent.getStringExtra("username") ?: ""
                binding.detailNama.isEnabled = false
                binding.detailUsername.isEnabled = false
                binding.topAppBarUserinfo.title = "Edit Data Pegawai"
                binding.resetID.visibility = View.VISIBLE
                binding.hapususer.visibility = View.VISIBLE
                binding.topAppBarUserinfo.menu.findItem(R.id.menunya_update)?.isVisible = true
                binding.topAppBarUserinfo.menu.findItem(R.id.menunya_simpan)?.isVisible = false

                loadUserDetail(oldUsername)
            }
            else -> {
                // MODE: TAMBAH (Admin tambah user baru)
                binding.topAppBarUserinfo.title = "Tambah User Baru"
                binding.resetID.visibility = View.GONE
                binding.hapususer.visibility = View.GONE
                binding.topAppBarUserinfo.menu.findItem(R.id.menunya_update)?.isVisible = false
                binding.topAppBarUserinfo.menu.findItem(R.id.menunya_simpan)?.isVisible = true
            }
        }

        // ==========================================
        // AKSI TOMBOL
        // ==========================================
        binding.resetID.setOnClickListener {
            resetDeviceID(oldUsername)
        }

        binding.hapususer.setOnClickListener {
            hapusUser(oldUsername)
        }

        binding.topAppBarUserinfo.setNavigationOnClickListener {
            finish()
        }

        // Aksi Simpan / Update di Toolbar
        binding.topAppBarUserinfo.setOnMenuItemClickListener {
            // 1. AMBIL TEKS DI SINI, SAAT TOMBOL BENAR-BENAR DIKLIK
            val inputNama = binding.detailNama.text.toString().trim()
            val inputUser = binding.detailUsername.text.toString().trim()
            val inputPass = binding.detailPass.text.toString().trim()
            val inputNip = binding.detailNip.text.toString().trim()
            val inputStatus = binding.detailStatus.text.toString().trim()
            val inputJenis = binding.detailJenis.text.toString().trim()

            when (it.itemId) {
                R.id.menunya_update -> {
                    if (inputUser.isEmpty() || inputNama.isEmpty()) {
                        Toast.makeText(this, "Username dan Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    } else {
                        updateUser(oldUsername, inputUser, inputNama, inputPass, inputNip, inputStatus, inputJenis)
                    }
                    true
                }
                R.id.menunya_simpan -> {
                    if (inputUser.isEmpty() || inputNama.isEmpty()) {
                        Toast.makeText(this, "Username dan Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    } else {
                        tambahUser(inputUser, inputNama, inputPass, inputNip, inputStatus, inputJenis)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUserDetail(usernameTarget: String) {
        val url = AppConstants.GET_USER_DETAIL + "?username=$usernameTarget"
        val request = StringRequest(Request.Method.GET, url, { response ->
            try {
                val json = JSONObject(response)
                if (json.has("error")) {
                    Toast.makeText(this, json.getString("error"), Toast.LENGTH_SHORT).show()
                    return@StringRequest
                }

                binding.detailNama.setText(json.optString("nama", ""))
                binding.detailUsername.setText(json.optString("username", ""))
                binding.detailPass.setText(json.optString("password", ""))
                binding.detailNip.setText(json.optString("nip", ""))
                binding.detailStatus.setText(json.optString("status", ""), false)
                binding.detailJenis.setText(json.optString("jenis", ""), false)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Gagal parsing data", Toast.LENGTH_SHORT).show()
            }
        }, { error ->
            Toast.makeText(this, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            Log.e("EDIT_USER", "Volley error: ${error.message}")
        })
        Volley.newRequestQueue(this).add(request)
    }

    private fun updateUser(oldUserTarget: String, newUsername: String, nama: String, password: String, nip: String, status: String, jenis: String) {
        val url = AppConstants.UPDATE_USER
        val request = object : StringRequest(Method.POST, url, { response ->
            try {
                val json = JSONObject(response)
                Toast.makeText(this, json.optString("success", json.optString("error")), Toast.LENGTH_SHORT).show()

                if (json.has("success")) {
                    // CEK: Apakah yang diedit adalah akun yang sedang dipakai login di HP ini?
                    val currentLoggedInUser = prefs.getString("username", "")
                    if (oldUserTarget == currentLoggedInUser) {
                        // Jika iya, update SharedPreferences dengan data yang baru!
                        val editor = prefs.edit()
                        editor.putString("username", newUsername)
                        editor.putString("namalengkap", nama)
                        editor.putString("password", password)
                        editor.putString("nip", nip)
                        editor.putString("status", status)
                        editor.putString("jenis", jenis)
                        editor.apply()
                    }
                    finish() // Tutup halaman
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, { error ->
            Toast.makeText(this, "Gagal update: ${error.message}", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams() = mapOf(
                "old_username" to oldUserTarget,
                "username" to newUsername,
                "nama" to nama,
                "password" to password,
                "nip" to nip,
                "status" to status,
                "jenis" to jenis
            )
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun tambahUser(username: String, nama: String, password: String, nip: String, status: String, jenis: String) {
        val url = AppConstants.TAMBAH_USER
        val request = object : StringRequest(Method.POST, url, { response ->
            try {
                val json = JSONObject(response)
                Toast.makeText(this, json.optString("success", json.optString("error")), Toast.LENGTH_SHORT).show()
                if (json.has("success")) finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, { error ->
            Toast.makeText(this, "Gagal tambah user: ${error.message}", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams() = mapOf(
                "username" to username,
                "nama" to nama,
                "password" to password,
                "nip" to nip,
                "status" to status,
                "jenis" to jenis
            )
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun resetDeviceID(usernameTarget: String) {
        val url = AppConstants.RESET_DEVICE_ID
        val request = object : StringRequest(Method.POST, url, { response ->
            try {
                val json = JSONObject(response)
                Toast.makeText(this, json.optString("success", json.optString("error")), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, { error ->
            Toast.makeText(this, "Gagal reset ID: ${error.message}", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams() = mapOf("username" to usernameTarget)
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun hapusUser(usernameTarget: String) {
        val url = AppConstants.HAPUS_USER
        val request = object : StringRequest(Method.POST, url, { response ->
            try {
                val json = JSONObject(response)
                if (json.has("success")) {
                    Toast.makeText(this, json.getString("success"), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, json.getString("error"), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, { error ->
            Toast.makeText(this, "Gagal hapus user: ${error.message}", Toast.LENGTH_SHORT).show()
        }) {
            override fun getParams() = mapOf("username" to usernameTarget)
        }
        Volley.newRequestQueue(this).add(request)
    }
}