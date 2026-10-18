package id.rudissp.esmansa.admin

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.rudissp.esmansa.AppConstants
import id.rudissp.esmansa.databinding.ActivityAdminBinding
import id.rudissp.esmansa.presensi.RekapHarianModel
import org.json.JSONArray

class AdminActivity : AppCompatActivity() {
    lateinit var binding: ActivityAdminBinding
    lateinit var prefs: SharedPreferences
    private lateinit var adapterq: UserAdapter

    lateinit var dataList: ArrayList<UserModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        binding.topAppBarAdmin.setNavigationOnClickListener {
            finish()
        }

        dataList = ArrayList()
        binding.recyclerUser.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        adapterq = UserAdapter(this, dataList)
        binding.recyclerUser.adapter = adapterq

        loadUser()

        binding.btnTambahUser.setOnClickListener {
            startActivity(Intent(this, EditUserActivity::class.java))
        }

    }

    private fun loadUser() {
        val url = AppConstants.GET_SEMUA_USER
        val request = StringRequest(Request.Method.GET, url, { response ->
            val jsonArray = JSONArray(response)
            dataList.clear()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val nama = obj.getString("nama")
                val username = obj.getString("username")

                dataList.add(
                    UserModel(
                        nama = nama,
                        username = username
                    )
                )
            }
            adapterq.notifyDataSetChanged()
        }, { error ->
            Toast.makeText(this, "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            Log.e("JADWAL_ERROR", "Error: ${error.message}")
        })

        Volley.newRequestQueue(this).add(request)
    }

    override fun onResume() {
        super.onResume()
        loadUser()
    }

}
