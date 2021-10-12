package com.example.jsmusicapp.Utils

import android.graphics.Color
import java.util.*

class CommonUtils {

    private fun randomColorGenerator(): Int {
        val random = Random()
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }

}