package com.example.demeter_pgc.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

private const val TAG = "AnimalClassifier"

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
 * Detecta automáticamente si el modelo espera la imagen en formato
 * [1, alto, ancho, canales] (NHWC, típico de TensorFlow/Keras) o
 * [1, canales, alto, ancho] (NCHW, típico de modelos exportados desde
 * PyTorch/YOLO), para armar el buffer de entrada en el orden correcto.
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
    private val inputChannels: Int
    private val isChannelsFirst: Boolean
    private val inputDataType: DataType
    private val outputSize: Int

    init {
        interpreter = Interpreter(loadModelFile(context, modelFileName))
        labels = context.assets.open(labelsFileName)
            .bufferedReader()
            .readLines()
            .filter { it.isNotBlank() }

        val inputTensor = interpreter.getInputTensor(0)
        val inputShape = inputTensor.shape()
        inputDataType = inputTensor.dataType()

        // Si la segunda posición del shape es 3 (y no coincide con la última),
        // asumimos formato NCHW: [1, canales, alto, ancho].
        // Si no, asumimos el formato más común, NHWC: [1, alto, ancho, canales].
        isChannelsFirst = inputShape.size == 4 && inputShape[1] == 3 && inputShape[3] != 3
        if (isChannelsFirst) {
            inputChannels = inputShape[1]
            inputHeight = inputShape[2]
            inputWidth = inputShape[3]
        } else {
            inputHeight = inputShape[1]
            inputWidth = inputShape[2]
            inputChannels = inputShape[3]
        }

        Log.d(
            TAG,
            "Input shape=${inputShape.joinToString()} dataType=$inputDataType " +
                    "channelsFirst=$isChannelsFirst width=$inputWidth height=$inputHeight"
        )

        val outputShape = interpreter.getOutputTensor(0).shape()
        outputSize = outputShape[outputShape.size - 1]
        Log.d(TAG, "Output shape=${outputShape.joinToString()}")
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
        Log.d(TAG, "Probabilidades: " + labels.zip(probabilities.toList()).joinToString())
        val label = labels.getOrElse(bestIndex) { "Desconocido" }
        return ClassificationResult(label = label, confidence = probabilities[bestIndex])
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val bytesPerChannel = if (inputDataType == DataType.FLOAT32) 4 else 1
        val byteBuffer = ByteBuffer.allocateDirect(inputWidth * inputHeight * inputChannels * bytesPerChannel)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputWidth * inputHeight)
        bitmap.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)

        fun writeValue(value: Int) {
            if (inputDataType == DataType.FLOAT32) {
                byteBuffer.putFloat(value / 255f)
            } else {
                byteBuffer.put(value.toByte())
            }
        }

        if (isChannelsFirst) {
            // Formato [canales, alto, ancho]: primero todos los valores de
            // Rojo, luego todos los de Verde, luego todos los de Azul.
            for (pixel in pixels) writeValue((pixel shr 16) and 0xFF) // R
            for (pixel in pixels) writeValue((pixel shr 8) and 0xFF)  // G
            for (pixel in pixels) writeValue(pixel and 0xFF)          // B
        } else {
            // Formato [alto, ancho, canales]: R, G, B intercalados por píxel.
            for (pixel in pixels) {
                writeValue((pixel shr 16) and 0xFF) // R
                writeValue((pixel shr 8) and 0xFF)  // G
                writeValue(pixel and 0xFF)           // B
            }
        }
        return byteBuffer
    }

    fun close() {
        interpreter.close()
    }
}