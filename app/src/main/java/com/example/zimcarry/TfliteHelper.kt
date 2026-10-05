package com.example.carryon    // ← 패키지명에 맞게 수정

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class TfliteHelper(context: Context,
                   model: String = "carryoncheck_fp16.tflite",
                   labelsFile: String = "labels.txt",
                   private val imgSize: Int = 224) {

    private val interpreter: Interpreter
    val labels: List<String>

    init {
        interpreter = Interpreter(loadModel(context, model))
        labels = context.assets.open(labelsFile).bufferedReader().readLines()
    }

    private fun loadModel(context: Context, path: String): ByteBuffer {
        val fd = context.assets.openFd(path)
        FileInputStream(fd.fileDescriptor).use { fis ->
            val fc = fis.channel
            return fc.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
        }
    }

    fun run(bm: Bitmap): Pair<String, Float> {
        val input = bitmapToFloatBuffer(bm, imgSize)
        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(input, output)
        val probs = output[0]
        var max = 0
        for (i in 1 until probs.size) if (probs[i] > probs[max]) max = i
        return labels[max] to probs[max]
    }

    private fun bitmapToFloatBuffer(bm: Bitmap, size: Int): ByteBuffer {
        val scaled = Bitmap.createScaledBitmap(bm, size, size, true)
        val buf = ByteBuffer.allocateDirect(4 * size * size * 3).order(ByteOrder.nativeOrder())
        val pixels = IntArray(size * size)
        scaled.getPixels(pixels, 0, size, 0, 0, size, size)
        var idx = 0
        for (y in 0 until size) for (x in 0 until size) {
            val p = pixels[idx++]
            buf.putFloat(((p shr 16) and 0xFF) / 255f)
            buf.putFloat(((p shr 8) and 0xFF) / 255f)
            buf.putFloat((p and 0xFF) / 255f)
        }
        buf.rewind()
        return buf
    }

    fun close() = interpreter.close()
}
