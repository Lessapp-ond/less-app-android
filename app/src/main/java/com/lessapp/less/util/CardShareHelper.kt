package com.lessapp.less.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.lessapp.less.data.model.Card
import java.io.File
import java.io.FileOutputStream

object CardShareHelper {

    private const val CARD_WIDTH = 1200f
    private const val CARD_HEIGHT = 1800f
    private const val PADDING = 60f
    private const val CORNER_RADIUS = 66f

    fun shareCard(context: Context, card: Card, l10n: L10n) {
        val bitmap = renderCardAsBitmap(card, l10n)
        val file = saveBitmapToCache(context, bitmap, "less_card_${card.id}.png")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, null))
    }

    private fun renderCardAsBitmap(card: Card, l10n: L10n): Bitmap {
        val bitmap = Bitmap.createBitmap(
            CARD_WIDTH.toInt(),
            CARD_HEIGHT.toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        // Background gradient
        val gradientPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, CARD_WIDTH, CARD_HEIGHT,
                intArrayOf(
                    Color.parseColor("#1A1A2E"),
                    Color.parseColor("#16213E"),
                    Color.parseColor("#0F3460")
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, CARD_WIDTH, CARD_HEIGHT, gradientPaint)

        // Card background
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        val cardRect = RectF(PADDING, PADDING * 2, CARD_WIDTH - PADDING, CARD_HEIGHT - PADDING * 3)
        canvas.drawRoundRect(cardRect, CORNER_RADIUS, CORNER_RADIUS, cardPaint)

        // Text paints
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A1A1A")
            textSize = 72f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val hookPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#444444")
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val bulletPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#333333")
            textSize = 46f
        }

        val whyLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#222222")
            textSize = 44f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val whyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#555555")
            textSize = 42f
        }

        val topicPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#888888")
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Draw content
        var y = PADDING * 2 + 100f
        val contentX = PADDING + 50f
        val maxWidth = CARD_WIDTH - PADDING * 2 - 100f

        // Title
        val titleLines = wrapText(card.title, titlePaint, maxWidth)
        titleLines.forEach { line ->
            canvas.drawText(line, contentX, y, titlePaint)
            y += 85f
        }
        y += 30f

        // Hook
        val hookLines = wrapText(card.hook, hookPaint, maxWidth)
        hookLines.forEach { line ->
            canvas.drawText(line, contentX, y, hookPaint)
            y += 65f
        }
        y += 40f

        // Bullets
        card.bullets.take(3).forEach { bullet ->
            val bulletLines = wrapText("• $bullet", bulletPaint, maxWidth)
            bulletLines.forEach { line ->
                canvas.drawText(line, contentX, y, bulletPaint)
                y += 58f
            }
            y += 15f
        }
        y += 30f

        // Why section
        canvas.drawText("${l10n.whyItMatters}", contentX, y, whyLabelPaint)
        y += 55f

        val whyLines = wrapText("→ ${card.why}", whyPaint, maxWidth)
        whyLines.forEach { line ->
            canvas.drawText(line, contentX, y, whyPaint)
            y += 55f
        }

        // Topic at bottom of card
        val topicY = cardRect.bottom - 50f
        canvas.drawText(card.topic.uppercase(), contentX, topicY, topicPaint)

        // Brand at bottom
        val brandText = "LESS"
        val brandWidth = brandPaint.measureText(brandText)
        canvas.drawText(brandText, (CARD_WIDTH - brandWidth) / 2, CARD_HEIGHT - PADDING, brandPaint)

        return bitmap
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = StringBuilder(testLine)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String): File {
        val cacheDir = File(context.cacheDir, "shared_images")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val file = File(cacheDir, fileName)
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        return file
    }
}
