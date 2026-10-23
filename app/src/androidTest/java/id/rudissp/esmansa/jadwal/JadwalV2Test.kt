package id.rudissp.esmansa.jadwal

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JadwalV2Test {

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        prefs.edit().putString("namalengkap", "Test User").apply()
    }

    @Test
    fun testActivityLaunch() {
        ActivityScenario.launch(JadwalV2::class.java).use { scenario ->
            assertNotNull(scenario)
        }
    }
}
