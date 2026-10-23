package id.rudissp.esmansa.jadwal

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.bumptech.glide.Glide
import com.google.android.material.transition.platform.MaterialArcMotion
import com.google.android.material.transition.platform.MaterialContainerTransform
import id.rudissp.esmansa.R
import id.rudissp.esmansa.ZoomClass
import androidx.core.graphics.toColorInt

class ull : AppCompatActivity() {

    lateinit var prefs: SharedPreferences

    lateinit var gambar: ZoomClass


    override fun onCreate(savedInstanceState: Bundle?) {enableEdgeToEdge()
        val transform = MaterialContainerTransform()

        transform.fitMode = MaterialContainerTransform.FIT_MODE_AUTO
        transform.setDuration(550L)
        transform.setPathMotion(MaterialArcMotion())
        transform.setInterpolator(FastOutSlowInInterpolator())
        transform.scrimColor = "#4AED93".toColorInt()

        prefs = this.getSharedPreferences("PREFS", MODE_PRIVATE)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full)

        gambar = findViewById(R.id.zoom)

        val filenya = prefs.getString("fullnya_gambar", "")

        Glide.with(this).load(filenya).into(gambar)


    }
}