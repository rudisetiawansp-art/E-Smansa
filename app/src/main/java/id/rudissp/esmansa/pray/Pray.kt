package id.rudissp.esmansa.pray

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import id.rudissp.esmansa.databinding.ActivityPrayBinding

class Pray : AppCompatActivity(), TabLayout.OnTabSelectedListener {
    lateinit var binding: ActivityPrayBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("PREFS", MODE_PRIVATE)
        binding = ActivityPrayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.prayToolbar.subtitle = prefs.getString("namalengkap", "")
        binding.prayToolbar.setNavigationOnClickListener { finish() }

        val manager = supportFragmentManager
        val adapter = PrayTabs(manager, lifecycle)
        binding.prayPager.setAdapter(adapter)

        binding.prayTabs.addTab(binding.prayTabs.newTab().setText("Rekap Harian"))
        //binding.prayTabs.addTab(binding.prayTabs.newTab().setText("Rekap Bulanan"))
        binding.prayTabs.addTab(binding.prayTabs.newTab().setText("Halangan"))

        binding.prayTabs.addOnTabSelectedListener(this)
        binding.prayPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.prayTabs.selectTab(binding.prayTabs.getTabAt(position))
            }
        })


    }

    override fun onTabSelected(p0: TabLayout.Tab?) {
        binding.prayPager.currentItem = p0!!.position
    }

    override fun onTabUnselected(p0: TabLayout.Tab?) {
    }

    override fun onTabReselected(p0: TabLayout.Tab?) {
    }
}

class PrayTabs(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return PrayHarian()
            //1 -> return PrayBulanan()
            1 -> return PraySiswa()
        }
        return PrayHarian()
    }

    override fun getItemCount(): Int {
        return 2

    }
}