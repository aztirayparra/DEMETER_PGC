
//CameraX entrega la foto capturada como bytes en formato
// JPEG dentro de un ImageProxy. Esta función los convierte a Bitmap
// y corrige la rotación (los celulares suelen capturar "de lado"
// a nivel de sensor, y esto lo endereza).
package com.example.demeter_pgc.ml

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageProxy

/**
 * Convierte el JPEG capturado por CameraX (ImageCapture) a un Bitmap ya
 * rotado según la orientación real del dispositivo/cámara.
 *
 * Debe llamarse ANTES de cerrar el ImageProxy (image.close()).
 */
fun ImageProxy.toClassifierBitmap(): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    val rotationDegrees = imageInfo.rotationDegrees
    return if (rotationDegrees != 0) {
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
    } else {
        original
    }
}