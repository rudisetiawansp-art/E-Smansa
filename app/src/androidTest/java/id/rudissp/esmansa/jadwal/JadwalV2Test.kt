package id.rudissp.esmansa.jadwal

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import id.rudissp.esmansa.R
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JadwalV2Test {

    @Before
    fun setup() {
        // Set SharedPreferences for testing
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        prefs.edit().putString("namalengkap", "Test User").apply()
    }

    @Test
    fun testActivityLaunchAndInitialState() {
        ActivityScenario.launch(JadwalV2::class.java).use {
            // Check toolbar title
            onView(withId(R.id.jadwalv2_toolbar))
                .check(matches(hasDescendant(withText("Test User"))))

            // Check if search card is initially hidden
            onView(withId(R.id.card_pencarian))
                .check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun testPencarianButtonToggle() {
        ActivityScenario.launch(JadwalV2::class.java).use {
            // Click search button
            onView(withId(R.id.btn_pencarian)).perform(click())

            // Check if search card is visible
            onView(withId(R.id.card_pencarian)).check(matches(isDisplayed()))

            // Click search button again to toggle
            onView(withId(R.id.btn_pencarian)).perform(click())

            // Check if search card is hidden
            onView(withId(R.id.card_pencarian)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun testJadwalSayaButtonResetsView() {
        ActivityScenario.launch(JadwalV2::class.java).use {
            // Show search card first
            onView(withId(R.id.btn_pencarian)).perform(click())
            onView(withId(R.id.card_pencarian)).check(matches(isDisplayed()))

            // Click "Jadwal Saya"
            onView(withId(R.id.btn_jadwal_saya)).perform(click())

            // Search card should be hidden
            onView(withId(R.id.card_pencarian)).check(matches(not(isDisplayed())))
            
            // Toolbar title should be back to my username
            onView(withId(R.id.jadwalv2_toolbar))
                .check(matches(hasDescendant(withText("Test User"))))
        }
    }
}
