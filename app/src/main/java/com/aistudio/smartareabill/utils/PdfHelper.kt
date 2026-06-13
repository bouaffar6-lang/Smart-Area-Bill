package com.aistudio.smartareabill.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.aistudio.smartareabill.data.models.RoomEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.net.Uri
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Path

object PdfHelper {

    fun generateHomeBillPdf(context: Context, rooms: List<RoomEntity>): File? {
        // Run first pass to estimate total page count
        val totalPages = runPass(context, rooms, null)
        
        // Run second pass to output the actual PDF file containing full page numbering (e.g. page 1 of 2)
        return runPassWriteFile(context, rooms, totalPages)
    }

    private fun runPass(context: Context, rooms: List<RoomEntity>, totalPages: Int?): Int {
        val pdfDocument = PdfDocument()
        val result = drawDocument(context, rooms, pdfDocument, totalPages)
        pdfDocument.close()
        return result
    }

    private fun runPassWriteFile(context: Context, rooms: List<RoomEntity>, totalPages: Int): File? {
        val pdfDocument = PdfDocument()
        drawDocument(context, rooms, pdfDocument, totalPages)

        val outputDir = context.cacheDir
        val file = File(outputDir, "Estimates_Detailed_BOQ_${System.currentTimeMillis()}.pdf")
        return try {
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun drawDocument(
        context: Context,
        rooms: List<RoomEntity>,
        pdfDocument: PdfDocument,
        totalPages: Int?
    ): Int {
        val textPaint = TextPaint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }
        val paint = Paint()

        var pageIndex = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageIndex).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Load SharedPreferences for business identity
        val prefs = context.getSharedPreferences("work_item_templates_prefs", Context.MODE_PRIVATE)
        val companyName = prefs.getString("company_name", "") ?: ""
        val companyPhone = prefs.getString("company_phone", "") ?: ""
        val companyWebsite = prefs.getString("company_website", "") ?: ""
        val showCompany = prefs.getBoolean("show_company_invoice", true)
        val companyLogoUri = prefs.getString("company_logo_uri", "") ?: ""

        val clientName = prefs.getString("client_name", "") ?: ""
        val clientPhone = prefs.getString("client_phone", "") ?: ""
        val projectAddress = prefs.getString("project_address", "") ?: ""
        val boqReference = prefs.getString("boq_reference", "") ?: ""
        val boqDate = prefs.getString("boq_date", "") ?: ""
        val currencySymbol = prefs.getString("currency_symbol", "دج") ?: "دج"

        // Dynamic fallbacks to avoid unstyled gaps
        val finalCompanyName = if (showCompany && companyName.isNotBlank()) companyName else "فارس"
        val finalCompanyPhone = if (showCompany && companyPhone.isNotBlank()) companyPhone else "065896333"
        val finalClientName = if (clientName.isNotBlank()) clientName else "توفيق"
        val finalProjectAddress = if (projectAddress.isNotBlank()) projectAddress else "زفزاف"
        val finalBoqReference = if (boqReference.isNotBlank()) boqReference else "غير محدد"

        var startY = 40f

        // 1. Header Card (تروية المستند)
        val headerCardHeight = 115f
        paint.color = Color.parseColor("#F8FAFC") // Soft Slate-50 background tint
        canvas.drawRoundRect(30f, startY, 565f, startY + headerCardHeight, 8f, 8f, paint)

        // Navy-blue solid accent line on the right column edge
        paint.color = Color.parseColor("#0F2C59")
        canvas.drawRoundRect(557f, startY, 565f, startY + headerCardHeight, 4f, 4f, paint)

        // DRAW CORPORATE LOGO ON THE TOP LEFT
        val logoLeft = 45f
        val logoTop = startY + 11f
        val logoSize = 34f
        var logoDrawn = false

        if (showCompany && companyLogoUri.isNotBlank()) {
            try {
                val uri = Uri.parse(companyLogoUri)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val rawBitmap = BitmapFactory.decodeStream(inputStream)
                    if (rawBitmap != null) {
                        paint.color = Color.WHITE
                        canvas.drawRoundRect(logoLeft, logoTop, logoLeft + logoSize, logoTop + logoSize, 6f, 6f, paint)
                        val srcRect = Rect(0, 0, rawBitmap.width, rawBitmap.height)
                        val dstRect = RectF(logoLeft + 1.5f, logoTop + 1.5f, logoLeft + logoSize - 1.5f, logoTop + logoSize - 1.5f)
                        canvas.drawBitmap(rawBitmap, srcRect, dstRect, paint)
                        logoDrawn = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (!logoDrawn) {
            // Stylized, modern geometric builder fallback icon
            paint.color = Color.parseColor("#1E3E62")
            canvas.drawRoundRect(logoLeft, logoTop, logoLeft + logoSize, logoTop + logoSize, 6f, 6f, paint)
            
            paint.color = Color.parseColor("#F59E0B")
            val path = Path().apply {
                moveTo(logoLeft + 8f, logoTop + logoSize - 8f)
                lineTo(logoLeft + logoSize - 8f, logoTop + logoSize - 8f)
                lineTo(logoLeft + logoSize - 8f, logoTop + 8f)
                close()
            }
            canvas.drawPath(path, paint)
            
            paint.color = Color.WHITE
            paint.strokeWidth = 1.2f
            canvas.drawLine(logoLeft + 10f, logoTop + logoSize - 10f, logoLeft + logoSize - 10f, logoTop + 10f, paint)
        }

        // Restore paint style
        paint.style = Paint.Style.FILL

        // 1. Title / Publisher
        textPaint.color = Color.parseColor("#1E3E62")
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.textSize = 13.5f
        drawArabicText(canvas, "فاتورة القياسات الهندسية", 545f, startY + 12f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 500)

        // 2. Contractor details (بيانات المقاول)
        textPaint.color = Color.parseColor("#2D3748")
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.textSize = 9.5f
        drawArabicText(canvas, "المقاول المسؤول: $finalCompanyName   |   الهاتف: $finalCompanyPhone", 545f, startY + 36f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 500)

        // Elegant separator line
        paint.color = Color.parseColor("#CBD5E1")
        paint.strokeWidth = 0.5f
        canvas.drawLine(45f, startY + 54f, 550f, startY + 54f, paint)

        // 3. Client & Project details (بيانات العميل والمشروع)
        textPaint.color = Color.parseColor("#4A5568")
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.textSize = 9f
        drawArabicText(canvas, "العميل: $finalClientName    |    موقع المشروع: $finalProjectAddress    |    المرجع: $finalBoqReference", 545f, startY + 64f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 500)

        // 4. Documentation info (التوثيق)
        val sdf = SimpleDateFormat("yyyy/MM/dd (الساعة HH:mm)", Locale("en"))
        val dateStrRaw = if (boqDate.isNotBlank()) boqDate else sdf.format(Date())
        val dateStr = convertArabicNumeralsToEnglish(dateStrRaw)
        drawArabicText(canvas, "تاريخ الإصدار: $dateStr    |    الحالة: مستند معتمد هندسياً", 545f, startY + 84f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 500)

        startY += headerCardHeight + 20f

        // Page break helper inside drawing function
        fun checkPageOverflow(requiredHeight: Float): Canvas {
            if (startY + requiredHeight > 780f) {
                // Draw footer before turning page
                drawFooter(canvas, pageIndex, totalPages)

                pdfDocument.finishPage(page)
                pageIndex++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageIndex).create()
                page = pdfDocument.startPage(pageInfo)
                val newCanvas = page.canvas

                // Draw delicate page header accent line
                paint.color = Color.parseColor("#1E3E62")
                newCanvas.drawRect(30f, 30f, 565f, 32f, paint)

                textPaint.color = Color.parseColor("#718096")
                textPaint.textSize = 8f
                textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                drawArabicText(newCanvas, "فاتورة القياسات الهندسية", 565f, 15f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 300)

                startY = 45f
                return newCanvas
            }
            return canvas
        }

        // 2. Project Executive Summary Card (ملخص المشروع)
        val grandTotal = com.aistudio.smartareabill.domain.engine.AggregationEngine.calculateGrandTotal(rooms)
        val totalFloorArea = com.aistudio.smartareabill.domain.engine.AggregationEngine.calculateTotalFloorArea(rooms)

        val finalAdjustedTotal = grandTotal

        val summaryCardHeight = 65f
        canvas = checkPageOverflow(summaryCardHeight + 15f)

        // Background
        paint.color = Color.parseColor("#F1F5F9") // Light gray-slate background
        canvas.drawRoundRect(30f, startY, 565f, startY + summaryCardHeight, 6f, 6f, paint)

        // Border outline
        paint.color = Color.parseColor("#CBD5E1")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(30f, startY, 565f, startY + summaryCardHeight, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Extra thick line indicator inside left column
        paint.color = Color.parseColor("#0F2C59")
        canvas.drawRoundRect(559f, startY, 565f, startY + summaryCardHeight, 3f, 3f, paint)

        // Vertical dividers inside column divisions (from Right Column: 179px, 178px, 178px)
        paint.color = Color.parseColor("#E2E8F0")
        paint.strokeWidth = 1f
        canvas.drawLine(386f, startY + 10f, 386f, startY + summaryCardHeight - 10f, paint)
        canvas.drawLine(208f, startY + 10f, 208f, startY + summaryCardHeight - 10f, paint)

        // Card Content: Column 1 (Right): Rooms count
        textPaint.color = Color.parseColor("#4A5568")
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.textSize = 8.5f
        drawArabicText(canvas, "🏠 عدد الغرف الإجمالي", 550f, startY + 14f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 150)

        textPaint.color = Color.parseColor("#1E3E62")
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.textSize = 12.5f
        drawArabicText(canvas, "${rooms.size} غرف", 550f, startY + 34f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 150)

        // Card Content: Column 2 (Middle): Total floor area
        textPaint.color = Color.parseColor("#4A5568")
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.textSize = 8.5f
        drawArabicText(canvas, "📐 مسطح الأرضية الكلي", 376f, startY + 14f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 150)

        textPaint.color = Color.parseColor("#1E3E62")
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.textSize = 12.5f
        drawArabicText(canvas, "${String.format(Locale.ENGLISH, "%.1f", totalFloorArea)} م²", 376f, startY + 34f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 150)

        // Card Content: Column 3 (Left): Grand Price
        textPaint.color = Color.parseColor("#4A5568")
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.textSize = 8.5f
        drawArabicText(canvas, "💵 إجمالي المطلوب للدفع", 198f, startY + 14f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 150)

        textPaint.color = Color.parseColor("#C62828") // Highlight price
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.textSize = 13.5f
        drawArabicText(canvas, "${String.format(Locale.ENGLISH, "%,.0f", finalAdjustedTotal)} $currencySymbol", 198f, startY + 32f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 150)

        startY += summaryCardHeight + 20f

        // Analysis Categories cost distribution in a clean box
        val breakdown = com.aistudio.smartareabill.domain.engine.AggregationEngine.generateCategoryBreakdown(rooms)
        if (breakdown.isNotEmpty()) {
            val breakCardHeight = 25f + (breakdown.size * 18f)
            canvas = checkPageOverflow(breakCardHeight + 15f)

            // Background card
            paint.color = Color.parseColor("#F8FAFC")
            canvas.drawRoundRect(30f, startY, 565f, startY + breakCardHeight, 6f, 6f, paint)

            paint.color = Color.parseColor("#E2E8F0")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            canvas.drawRoundRect(30f, startY, 565f, startY + breakCardHeight, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            // Highlight line
            paint.color = Color.parseColor("#718096")
            canvas.drawRoundRect(559f, startY, 565f, startY + breakCardHeight, 3f, 3f, paint)

            // Label Title
            textPaint.color = Color.parseColor("#2D3748")
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textPaint.textSize = 9.5f
            drawArabicText(canvas, "تحليل وتوزيع التكلَف الإجمالي حسب نوع الأعمال ومجموع الحساب 📊", 545f, startY + 6f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 450)

            paint.color = Color.parseColor("#E2E8F0")
            paint.strokeWidth = 0.5f
            canvas.drawLine(40f, startY + 22f, 550f, startY + 22f, paint)

            var breakDeltaY = 28f
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textPaint.textSize = 8.5f

            breakdown.forEach { (category, amt) ->
                val percentOfTotal = if (grandTotal > 0.0) (amt / grandTotal) else 0.0
                textPaint.color = Color.parseColor("#2D3748")

                drawArabicText(canvas, "• $category", 540f, startY + breakDeltaY, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 300)

                textPaint.color = Color.parseColor("#1B2A4A")
                textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                val catValueStr = "${String.format(Locale.ENGLISH, "%,.0f", amt)} $currencySymbol   (${String.format(Locale.ENGLISH, "%.1f", percentOfTotal * 100)}%)"
                drawArabicText(canvas, catValueStr, 40f, startY + breakDeltaY, textPaint, Layout.Alignment.ALIGN_NORMAL, 200)

                textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                breakDeltaY += 16f
            }

            startY += breakCardHeight + 20f
        }

        // 3. Detailed BOQ tables loop (الجداول التفصيلية لبنود الأعمال)
        var roomIndex = 1
        for (room in rooms) {
            // Check room layout titles overflow
            canvas = checkPageOverflow(45f)

            // 1. Table title
            textPaint.color = Color.parseColor("#1E3E62")
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textPaint.textSize = 10f
            drawArabicText(canvas, "◀ [جدول $roomIndex: ${room.name}]", 565f, startY, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 535)

            // Dimensions details text
            val grossWallVal = room.perimeter * room.height
            val dimHeaderText = "الأبعاد والنتائج الهندسية: مسطح الأرضية: ${String.format(Locale.ENGLISH, "%.1f", room.floorArea)} م²   |   المحيط: ${String.format(Locale.ENGLISH, "%.1f", room.perimeter)} م   |   الجدران (بروت): ${String.format(Locale.ENGLISH, "%.1f", grossWallVal)}   |   الفتحات المستبعدة: ${String.format(Locale.ENGLISH, "%.1f", room.openingsArea)} م²   |   صافي الجدران: ${String.format(Locale.ENGLISH, "%.1f", room.netWallArea)} م²"

            textPaint.color = Color.parseColor("#4A5568")
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textPaint.textSize = 8f
            drawArabicText(canvas, dimHeaderText, 565f, startY + 15f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 535)

            startY += 30f

            // Start Drawing the table structure
            canvas = checkPageOverflow(26f)

            // Table header row background
            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawRect(30f, startY, 565f, startY + 18f, paint)

            paint.color = Color.parseColor("#CBD5E1")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(30f, startY, 565f, startY + 18f, paint)
            paint.style = Paint.Style.FILL

            // Column separators inside Header row
            canvas.drawLine(535f, startY, 535f, startY + 18f, paint)
            canvas.drawLine(285f, startY, 285f, startY + 18f, paint)
            canvas.drawLine(220f, startY, 220f, startY + 18f, paint)
            canvas.drawLine(160f, startY, 160f, startY + 18f, paint)
            canvas.drawLine(95f, startY, 95f, startY + 18f, paint)

            // Table Header columns text
            textPaint.color = Color.parseColor("#0F2C59")
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textPaint.textSize = 8.5f

            drawArabicText(canvas, "الرقم", 535f, startY + 3.5f, textPaint, Layout.Alignment.ALIGN_CENTER, 30)
            drawArabicText(canvas, "بيان الأعمال (البند)", 535f, startY + 3.5f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 245)
            drawArabicText(canvas, "وحدة القياس", 220f, startY + 3.5f, textPaint, Layout.Alignment.ALIGN_CENTER, 65)
            drawArabicText(canvas, "الأمتار", 160f, startY + 3.5f, textPaint, Layout.Alignment.ALIGN_CENTER, 60)
            drawArabicText(canvas, "سعر الوحدة", 95f, startY + 3.5f, textPaint, Layout.Alignment.ALIGN_CENTER, 65)
            drawArabicText(canvas, "الإجمالي الفرعي", 30f, startY + 3.5f, textPaint, Layout.Alignment.ALIGN_CENTER, 65)

            startY += 18f

            var itemNo = 1
            for (work in room.workItems) {
                val quantity = work.getQuantity(room)
                val cost = work.getCost(room)

                // Measure wrapped height of item name
                textPaint.textSize = 8f
                textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                val tempLayout = StaticLayout.Builder.obtain(work.name, 0, work.name.length, textPaint, 240).build()
                val textHeight = tempLayout.height.toFloat()
                val rowHeight = Math.max(20f, textHeight + 8f)

                canvas = checkPageOverflow(rowHeight)

                if (itemNo % 2 == 0) {
                    paint.color = Color.parseColor("#F8FAFC") // slate-50 alternating background
                    canvas.drawRect(30f, startY, 565f, startY + rowHeight, paint)
                }

                // Grid Border
                paint.color = Color.parseColor("#E2E8F0")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.5f
                canvas.drawRect(30f, startY, 565f, startY + rowHeight, paint)

                // Grid lines
                canvas.drawLine(535f, startY, 535f, startY + rowHeight, paint)
                canvas.drawLine(285f, startY, 285f, startY + rowHeight, paint)
                canvas.drawLine(220f, startY, 220f, startY + rowHeight, paint)
                canvas.drawLine(160f, startY, 160f, startY + rowHeight, paint)
                canvas.drawLine(95f, startY, 95f, startY + rowHeight, paint)
                paint.style = Paint.Style.FILL

                // Item Text details
                textPaint.color = Color.parseColor("#2D3748")

                // 1. الرقم
                drawArabicText(canvas, "$itemNo", 535f, startY + (rowHeight - textHeight)/2f, textPaint, Layout.Alignment.ALIGN_CENTER, 30)

                // 2. بيان الاعمال
                drawArabicText(canvas, work.name, 535f, startY + (rowHeight - textHeight)/2f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 245)

                // 3. وحدة القياس
                val qtyTypeStr = when (work.quantityType) {
                    "WALL_NET" -> "م² صافي"
                    "FLOOR" -> "م² أرضية"
                    "CEILING" -> "م² سقف"
                    "PERIMETER" -> "متر طولي"
                    "DOORS_COUNT" -> "حساب الأبواب"
                    "WINDOWS_COUNT" -> "حساب النوافذ"
                    else -> work.unitType
                }
                drawArabicText(canvas, qtyTypeStr, 220f, startY + (rowHeight - 8f)/2f, textPaint, Layout.Alignment.ALIGN_CENTER, 65)

                // 4. الكمية
                drawArabicText(canvas, String.format(Locale.ENGLISH, "%.1f", quantity), 160f, startY + (rowHeight - 8f)/2f, textPaint, Layout.Alignment.ALIGN_CENTER, 60)

                // 5. سعر وحدة البند
                drawArabicText(canvas, String.format(Locale.ENGLISH, "%,.0f", work.unitPrice), 95f, startY + (rowHeight - 8f)/2f, textPaint, Layout.Alignment.ALIGN_CENTER, 65)

                // 6. اجمالي فرعي للبند
                textPaint.color = Color.parseColor("#1B2A4A")
                textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                drawArabicText(canvas, String.format(Locale.ENGLISH, "%,.0f", cost), 30f, startY + (rowHeight - 8f)/2f, textPaint, Layout.Alignment.ALIGN_CENTER, 65)

                // Reset Typeface
                textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                startY += rowHeight
                itemNo++
            }

            // Room cumulative total and summary row at the bottom of the table
            canvas = checkPageOverflow(20f)

            paint.color = Color.parseColor("#F1F5F9")
            canvas.drawRect(30f, startY, 565f, startY + 18f, paint)

            paint.color = Color.parseColor("#CBD5E1")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(30f, startY, 565f, startY + 18f, paint)
            paint.style = Paint.Style.FILL

            // Border column subtotal line
            canvas.drawLine(95f, startY, 95f, startY + 18f, paint)

            textPaint.color = Color.parseColor("#0F2C59")
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textPaint.textSize = 8.5f

            val isCorridor = room.name.contains("ممر") || room.name.contains("corridor")
            val tableSummaryTitle = if (isCorridor) "إجمالي الممر" else "إجمالي الغرفة"
            drawArabicText(canvas, tableSummaryTitle, 535f, startY + 3.5f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 245)

            textPaint.color = Color.parseColor("#C62828")
            drawArabicText(canvas, "${String.format(Locale.ENGLISH, "%,.0f", room.totalCost)} $currencySymbol", 30f, startY + 3.5f, textPaint, Layout.Alignment.ALIGN_CENTER, 65)

            startY += 18f

            if (room.remarks.isNotBlank()) {
                canvas = checkPageOverflow(16f)
                textPaint.color = Color.parseColor("#718096")
                textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
                textPaint.textSize = 8f
                drawArabicText(canvas, "ملاحظات ومواصفات خاصة: ${room.remarks}", 555f, startY + 3f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 520)
                startY += 15f
            }

            startY += 20f
            roomIndex++
        }

        // 4. Final summary billing block (الخاتمة والتذييل)
        val advancePayment = prefs.getFloat("advance_payment", 0.0f).toDouble()
        val netTotal = (grandTotal - advancePayment).coerceAtLeast(0.0)

        val finalCalloutHeight = if (advancePayment > 0.0) 62f else 40f
        canvas = checkPageOverflow(finalCalloutHeight + 35f)

        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRoundRect(220f, startY, 565f, startY + finalCalloutHeight, 6f, 6f, paint)

        paint.color = Color.parseColor("#0F2C59")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawRoundRect(220f, startY, 565f, startY + finalCalloutHeight, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        if (advancePayment > 0.0) {
            textPaint.color = Color.parseColor("#4A5568")
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textPaint.textSize = 8.5f
            drawArabicText(canvas, "إجمالي قيمة القياسات:  ${String.format(Locale.ENGLISH, "%,.0f", grandTotal)} $currencySymbol", 550f, startY + 6f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 320)

            drawArabicText(canvas, "الدفعة المقدمة المدفوعة سلفاً (-):  ${String.format(Locale.ENGLISH, "%,.0f", advancePayment)} $currencySymbol", 550f, startY + 21f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 320)

            // Draw a thin separator line inside the card
            paint.color = Color.parseColor("#CBD5E1")
            paint.strokeWidth = 0.5f
            canvas.drawLine(230f, startY + 36f, 555f, startY + 36f, paint)
            paint.style = Paint.Style.FILL

            textPaint.color = Color.parseColor("#2E7D32") // Green for final remaining
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textPaint.textSize = 10f
            drawArabicText(canvas, "صافي المبلغ المتبقي نقداً:  ${String.format(Locale.ENGLISH, "%,.0f", netTotal)} $currencySymbol", 550f, startY + 41f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 320)
        } else {
            textPaint.color = Color.parseColor("#0F2C59")
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textPaint.textSize = 9.5f
            drawArabicText(canvas, "صافي التكلفة الإجمالية والنهائية للقياسات", 550f, startY + 8f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 320)

            textPaint.color = Color.parseColor("#C62828")
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textPaint.textSize = 12f
            drawArabicText(canvas, "${String.format(Locale.ENGLISH, "%,.0f", grandTotal)} $currencySymbol", 550f, startY + 22f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 320)
        }

        startY += finalCalloutHeight + 15f

        // Draw the footer on the last page before finishing
        drawFooter(canvas, pageIndex, totalPages)
        pdfDocument.finishPage(page)

        return pageIndex
    }

    private fun drawFooter(canvas: Canvas, pageIndex: Int, totalPages: Int?) {
        val paint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(30f, 800f, 565f, 800f, paint)

        val textPaint = TextPaint().apply {
            color = Color.parseColor("#718096")
            textSize = 8f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val pageText = if (totalPages != null) "صفحة $pageIndex من $totalPages" else "صفحة $pageIndex"
        drawArabicText(canvas, pageText, 30f, 808f, textPaint, Layout.Alignment.ALIGN_NORMAL, 150)

        val footerDesc = "فاتورة القياسات الهندسية التفصيلية"
        drawArabicText(canvas, footerDesc, 565f, 808f, textPaint, Layout.Alignment.ALIGN_OPPOSITE, 300)
    }

    private fun drawArabicText(
        canvas: Canvas,
        text: String,
        rightX: Float,
        topY: Float,
        textPaint: TextPaint,
        alignment: Layout.Alignment,
        width: Int
    ) {
        canvas.save()
        val xTranslate = if (alignment == Layout.Alignment.ALIGN_OPPOSITE) {
            rightX - width
        } else {
            rightX
        }
        canvas.translate(xTranslate, topY)

        val rtlAlignment = when (alignment) {
            Layout.Alignment.ALIGN_OPPOSITE -> Layout.Alignment.ALIGN_NORMAL
            Layout.Alignment.ALIGN_NORMAL -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_CENTER
        }

        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
            .setAlignment(rtlAlignment)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .setTextDirection(android.text.TextDirectionHeuristics.RTL)
            .build()

        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun convertArabicNumeralsToEnglish(input: String): String {
        val arabicChars = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val englishChars = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        var result = input
        for (i in 0..9) {
            result = result.replace(arabicChars[i], englishChars[i])
        }
        return result
    }

    fun sharePdfFile(context: Context, file: File) {
        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "تقرير حساب المساحات والتكلفة الهندسية")
            putExtra(Intent.EXTRA_TEXT, "مرفق إليكم تقرير المقايسة النهائي التفصيلي للمهندس.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "تصدير ومشاركة الملف عبر:"))
    }
}
