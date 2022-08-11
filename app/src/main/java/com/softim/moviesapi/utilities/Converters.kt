package com.softim.moviesapi.utilities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap): ByteArray{
        val ouputstream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, ouputstream)
        return ouputstream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(array: ByteArray): Bitmap{
        return BitmapFactory.decodeByteArray(array, 0, array.size)
    }

}