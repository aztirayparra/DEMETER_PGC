package com.example.demeter_pgc.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * Resultado de una clasificación: el animal detectado y su confianza (0f..1f).
 */
data class ClassificationResult(
    val label: String,
    val confidence: Float
)

/**
 * Envoltorio sobre el intérprete de TensorFlow Lite para clasificar entre los
 * animales definidos en labels.txt (caballo, gallina, gato, oveja, perro, vaca).
 *
 * No usa la librería tensorflow-lite-support (tiene un bug de namespace
 * duplicado con versiones nuevas de AGP); el preprocesamiento de la imagen
 * se hace a mano con Bitmap y ByteBuffer.
 */
class AnimalClassifier(
    context: Context,
    modelFileName: String = "best_animales.tflite",
    labelsFileName: String = "labels.txt"
) {
    private val interpreter: Interpreter
    private val labels: List<String>
    private val inputWidth: Int
    private val inputHeight: Int
    private val inputDataType: DataType
    private val outputSize: Int

    init {
        interpreter = Interpreter(loadModelFile(context, modelFileName))
        labels = context.assets.open(labelsFileName)
            .bufferedReader()
            .readLines()
            .filter { it.isNotBlank() }

        val inputTensor = interpreter.getInputTensor(0)
        val inputShape = inputTensor.shape() // esperado: [1, alto, ancho, 3]
        inputHeight = inputShape[1]
        inputWidth = inputShape[2]
        inputDataType = inputTensor.dataType()

        val outputShape = interpreter.getOutputTensor(0).shape() // esperado: [1, numClases]
        outputSize = outputShape[outputShape.size - 1]
    }

    private fun loadModelFile(context: Context, fileName: String): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd(fileName)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Clasifica una foto y devuelve el animal más probable con su confianza.
     */
    fun classify(bitmap: Bitmap): ClassificationResult {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)
        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

        val output = Array(1) { FloatArray(outputSize) }
        interpreter.run(inputBuffer, output)

        val probabilities = output[0]
        var bestIndex = 0
        for (i in probabilities.indices) {
            if (probabilities[i] > probabilities[bestIndex]) {
                bestIndex = i
            }
        }

        val label = labels.getOrElse(bestIndex) { "Desconocido" }
        return ClassificationResult(label = label, confidence = probabilities[bestIndex])
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val bytesPerChannel = if (inputDataType == DataType.FLOAT32) 4 else 1
        val byteBuffer = ByteBuffer.allocateDirect(inputWidth * inputHeight * 3 * bytesPerChannel)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputWidth * inputHeight)
        bitmap.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)

        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            if (inputDataType == DataType.FLOAT32) {
                byteBuffer.putFloat(r / 255f)
                byteBuffer.putFloat(g / 255f)
                byteBuffer.putFloat(b / 255f)
            } else {
                byteBuffer.put(r.toByte())
                byteBuffer.put(g.toByte())
                byteBuffer.put(b.toByte())
            }
        }
        return byteBuffer
    }

    fun close() {
        interpreter.close()
    }
}