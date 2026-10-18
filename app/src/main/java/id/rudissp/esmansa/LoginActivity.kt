package id.rudissp.esmansa

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.databinding.ActivityLoginBinding
import org.json.JSONObject
import androidx.core.content.edit
import com.android.volley.Request
import id.rudissp.esmansa.presensi.PresensiActivity

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private val queue by lazy { Volley.newRequestQueue(this) }

    override fun onCreate(savedInstanceState: Bundle?) {enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btSignIn.setOnClickListener {
            val username = binding.etUser.text.toString()
            val password = binding.etPass.text.toString()

            // Validasi input
            if (username.isNotEmpty() && password.isNotEmpty()) {
                login(username, password)
            } else {
                Toast.makeText(
                    this,
                    "Username atau password tidak boleh kosong",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }


    }

    @SuppressLint("HardwareIds")
    private fun login(username: String, password: String) {
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        val stringRequest = object : StringRequest(Method.POST, AppConstants.LOGIN_URL,
            Response.Listener { response ->
                try {
                    val json = JSONObject(response)
                    val status = json.getString("status")

                    if (status == "success") {
                        getSharedPreferences("PREFS", MODE_PRIVATE).edit {
                            putBoolean("is_logged_in", true)
                            putString("user_id", json.getString("user_id"))
                            putString("username", json.getString("username"))
                            putString("namalengkap", json.getString("nama"))
                            putString("role", json.getString("role"))
                            putString("password", password)
                            putString("status", json.optString("status_user", ""))
                            putString("jenis", json.optString("jenis", ""))
                            putString("nip", json.optString("nip", ""))
                            putString("device_id", json.optString("device_id", ""))
                        }

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Login gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {

            override fun getParams(): Map<String, String> {
                return mapOf(
                    "username" to username,
                    "password" to password,
                    "device_id" to deviceId
                )
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }

}