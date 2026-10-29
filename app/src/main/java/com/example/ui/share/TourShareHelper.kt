package com.example.ui.share

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
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.HistoricalPlace
import java.io.File
import java.io.FileOutputStream

data class TourSummaryData(
    val tourTitle: String,
    val totalDistanceKm: Double,
    val visitedCount: Int,
    val totalStops: Int,
    val timeSpentMinutes: Int,
    val visitedLandmarks: List<HistoricalPlace> = emptyList()
)

object TourShareHelper {

    /**
     * Builds a formatted, emoji-rich summary text ready for messaging apps, Twitter/X, etc.
     */
    fun buildSummaryText(context: Context, data: TourSummaryData): String {
        val hours = data.timeSpentMinutes / 60
        val mins = data.timeSpentMinutes % 60
        val timeString = if (hours > 0) {
            context.getString(R.string.time_format_hours_mins, hours, mins)
        } else {
            context.getString(R.string.time_format_mins, mins)
        }

        val distanceString = context.getString(R.string.distance_format_km, data.totalDistanceKm)
        val progressPercent = if (data.totalStops > 0) {
            ((data.visitedCount.toFloat() / data.totalStops) * 100).toInt()
        } else {
            100
        }

        val sb = StringBuilder()
        sb.append(context.getString(R.string.share_text_header)).append("\n\n")
        sb.append(context.getString(R.string.share_text_tour)).append(": ").append(data.tourTitle).append("\n")
        sb.append(context.getString(R.string.share_text_distance)).append(": ").append(distanceString).append("\n")
        sb.append(context.getString(R.string.share_text_visited)).append(": ")
            .append(context.getString(R.string.share_stops_format, data.visitedCount, data.totalStops, progressPercent))
            .append("\n")
        sb.append(context.getString(R.string.share_text_time)).append(": ").append(timeString).append("\n\n")

        if (data.visitedLandmarks.isNotEmpty()) {
            sb.append(context.getString(R.string.share_text_highlights)).append("\n")
            data.visitedLandmarks.take(5).forEach { place ->
                sb.append("• ").append(place.category.emoji).append(" ").append(place.title).append("\n")
            }
            if (data.visitedLandmarks.size > 5) {
                sb.append("• +").append(data.visitedLandmarks.size - 5).append(" more...\n")
            }
            sb.append("\n")
        }

        sb.append(context.getString(R.string.share_text_footer)).append("\n")
        sb.append("#GeoChronicle #HistoryWalk #HistoricalTour #WalkingTour")
        return sb.toString()
    }

    /**
     * Generates a high-resolution, modern 1080x1350 summary card bitmap.
     */
    fun generateSummaryCardBitmap(context: Context, data: TourSummaryData): Bitmap {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background gradient (Deep Slate/Navy Theme)
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(Color.parseColor("#0F172A"), Color.parseColor("#1E293B"), Color.parseColor("#0F172A")),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Card Container with glowing subtle border
        val cardMargin = 50f
        val cardRect = RectF(cardMargin, cardMargin, width - cardMargin, height - cardMargin)
        val cardBgPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#172033")
            style = Paint.Style.FILL
        }
        val cardBorderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#38BDF8")
            alpha = 80
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(cardRect, 48f, 48f, cardBgPaint)
        canvas.drawRoundRect(cardRect, 48f, 48f, cardBorderPaint)

        // 3. Header badge & Brand
        val badgePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#0284C7")
            style = Paint.Style.FILL
        }
        val badgeRect = RectF(90f, 90f, 520f, 150f)
        canvas.drawRoundRect(badgeRect, 30f, 30f, badgePaint)

        val badgeTextPaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(context.getString(R.string.share_badge_tour), 116f, 132f, badgeTextPaint)

        // 4. Tour Title
        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val titleLayout = StaticLayout.Builder.obtain(
            data.tourTitle, 0, data.tourTitle.length,
            titlePaint, width - 180
        ).setMaxLines(2)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setEllipsize(android.text.TextUtils.TruncateAt.END)
            .build()

        canvas.save()
        canvas.translate(90f, 180f)
        titleLayout.draw(canvas)
        canvas.restore()

        val subtitlePaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.parseColor("#94A3B8")
            textSize = 30f
        }
        canvas.drawText(context.getString(R.string.share_card_subtitle), 90f, 300f, subtitlePaint)

        // 5. Statistics Triple Cards
        val statTop = 350f
        val statCardHeight = 220f
        val statGap = 24f
        val availableWidth = (width - 180f) - (statGap * 2)
        val statCardWidth = availableWidth / 3f

        val statBgPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }
        val statBorderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#334155")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val hours = data.timeSpentMinutes / 60
        val mins = data.timeSpentMinutes % 60
        val timeStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

        val stats = listOf(
            Triple("🗺️", String.format("%.1f km", data.totalDistanceKm), context.getString(R.string.total_distance_covered)),
            Triple("🏛️", "${data.visitedCount} / ${data.totalStops}", context.getString(R.string.landmarks_visited_count)),
            Triple("⏱️", timeStr, context.getString(R.string.exploration_time))
        )

        for (i in stats.indices) {
            val left = 90f + i * (statCardWidth + statGap)
            val rect = RectF(left, statTop, left + statCardWidth, statTop + statCardHeight)
            canvas.drawRoundRect(rect, 28f, 28f, statBgPaint)
            canvas.drawRoundRect(rect, 28f, 28f, statBorderPaint)

            // Stat Icon
            val iconPaint = TextPaint().apply {
                isAntiAlias = true
                textSize = 44f
            }
            canvas.drawText(stats[i].first, left + 24f, statTop + 60f, iconPaint)

            // Stat Value
            val valPaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.parseColor("#38BDF8")
                textSize = 36f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(stats[i].second, left + 24f, statTop + 125f, valPaint)

            // Stat Label
            val lblPaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.parseColor("#94A3B8")
                textSize = 24f
            }
            canvas.drawText(stats[i].third, left + 24f, statTop + 175f, lblPaint)
        }

        // 6. Tour Progress Bar
        val progressPercent = if (data.totalStops > 0) {
            ((data.visitedCount.toFloat() / data.totalStops) * 100).toInt().coerceIn(0, 100)
        } else {
            100
        }

        val progressTop = 620f
        val progressTextPaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(
            context.getString(R.string.share_completed_stops_format, data.visitedCount, data.totalStops, progressPercent),
            90f,
            progressTop,
            progressTextPaint
        )

        val barBgRect = RectF(90f, progressTop + 16f, width - 90f, progressTop + 40f)
        val barBgPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#334155")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(barBgRect, 12f, 12f, barBgPaint)

        val fillWidth = (barBgRect.width() * (progressPercent / 100f)).coerceAtLeast(16f)
        val barFillRect = RectF(90f, progressTop + 16f, 90f + fillWidth, progressTop + 40f)
        val barFillPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#10B981")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(barFillRect, 12f, 12f, barFillPaint)

        // 7. Visited Highlights Section
        val highlightsTop = 720f
        val sectionHeaderPaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.parseColor("#F1F5F9")
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(context.getString(R.string.share_highlights_title), 90f, highlightsTop, sectionHeaderPaint)

        var currentY = highlightsTop + 45f
        val itemBgPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }
        val itemTitlePaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val itemCatPaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.parseColor("#38BDF8")
            textSize = 22f
        }

        if (data.visitedLandmarks.isNotEmpty()) {
            data.visitedLandmarks.take(4).forEach { place ->
                val itemRect = RectF(90f, currentY, width - 90f, currentY + 76f)
                canvas.drawRoundRect(itemRect, 20f, 20f, itemBgPaint)

                canvas.drawText(place.category.emoji, 114f, currentY + 50f, TextPaint().apply {
                    isAntiAlias = true
                    textSize = 36f
                })

                val safeTitle = if (place.title.length > 36) place.title.substring(0, 33) + "..." else place.title
                canvas.drawText(safeTitle, 175f, currentY + 40f, itemTitlePaint)
                canvas.drawText(place.category.name.lowercase().replaceFirstChar { it.uppercase() }, 175f, currentY + 66f, itemCatPaint)

                currentY += 92f
            }
        } else {
            val emptyPaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.parseColor("#94A3B8")
                textSize = 26f
            }
            canvas.drawText(context.getString(R.string.share_no_highlights), 90f, currentY + 40f, emptyPaint)
            currentY += 80f
        }

        // 8. Footer Watermark & Quote
        val footerPaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.parseColor("#64748B")
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }
        canvas.drawText("\"${context.getString(R.string.share_quote)}\"", 90f, height - 120f, footerPaint)

        val brandPaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.parseColor("#38BDF8")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(context.getString(R.string.share_footer_brand), 90f, height - 80f, brandPaint)

        return bitmap
    }

    /**
     * Saves bitmap to the shared cache directory and returns its content Uri.
     */
    fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val sharedDir = File(context.cacheDir, "shared")
            if (!sharedDir.exists()) {
                sharedDir.mkdirs()
            }
            val imageFile = File(sharedDir, "geochronicle_tour_summary_${System.currentTimeMillis()}.png")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Launches the Android Share Intent with Summary Card Image and/or Formatted Text.
     */
    fun shareTourProgress(
        context: Context,
        data: TourSummaryData,
        includeImage: Boolean = true
    ) {
        val summaryText = buildSummaryText(context, data)
        val imageUri = if (includeImage) {
            val bitmap = generateSummaryCardBitmap(context, data)
            saveBitmapToCache(context, bitmap)
        } else {
            null
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            if (imageUri != null) {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
            }
            putExtra(Intent.EXTRA_TEXT, summaryText)
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_tour_summary_title))
        }

        val chooser = Intent.createChooser(sendIntent, context.getString(R.string.share_chooser_title)).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
