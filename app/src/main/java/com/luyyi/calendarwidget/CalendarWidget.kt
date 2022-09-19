package com.luyyi.calendarwidget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CalendarWidget : AppWidgetProvider() {

    private val tvDayIds by lazy {
        intArrayOf(
            R.id.tvDay1,
            R.id.tvDay2,
            R.id.tvDay3,
            R.id.tvDay4,
            R.id.tvDay5,
            R.id.tvDay6,
            R.id.tvDay7,
        )
    }

    private val tvWeekIds by lazy {
        intArrayOf(
            R.id.tvMon,
            R.id.tvTues,
            R.id.tvWed,
            R.id.tvThus,
            R.id.tvFri,
            R.id.tvSta,
            R.id.tvSun,
        )

    }
    private val weekViews = ArrayList<RemoteViews>()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val currentCalendar = Calendar.getInstance().apply {

            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val calendar = Calendar.getInstance().apply {

            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val array = Array(6) { LongArray(7) { 0L } }

        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar[Calendar.HOUR_OF_DAY] = 0
        val startDayWeek = calendar[Calendar.DAY_OF_WEEK]
        val startOffset = if (startDayWeek == 1) 6 else startDayWeek - 2
        calendar.add(Calendar.DAY_OF_YEAR, -startOffset)

        for (i in 0 until 6) {
            for (j in 0 until 7) {
                array[i][j] = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        updateWidget(
            context,
            appWidgetManager,
            appWidgetIds,
            array,
            currentCalendar
        )

    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        calendarArray: Array<LongArray>,
        currentCalendar: Calendar
    ) {
        appWidgetIds.forEach { appWidgetId ->
            weekViews.clear()
            val remoteViews = getRemoteViews(
                context,
                appWidgetManager,
                appWidgetId,
                calendarArray,
                currentCalendar
            )
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }

    }


    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getRemoteViews(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        array: Array<LongArray>,
        currentCalendar: Calendar,
    ): RemoteViews {
        val rootView = RemoteViews(
            context.packageName,
            R.layout.calendar_widget_white
        ).apply {
            setTextViewText(
                R.id.tvMonth,
                SimpleDateFormat("MMMM", Locale.ENGLISH).format(Date())
            )
        }

        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        resizeView(options, rootView)

        rootView.removeAllViews(R.id.llyDayContainer)
        array.forEachIndexed { _, week ->
            val dayView = RemoteViews(
                context.packageName,
                R.layout.calendar_widget_day_item_white
            )
            week.forEachIndexed { j, dayTime ->
                val day = getFormattedDateStr("d", dayTime / 1000)
                if (dayTime == currentCalendar.timeInMillis) {
                    dayView.setTextViewText(tvDayIds[j], day)
                    dayView.setTextColor(tvDayIds[j], Color.RED)
                } else {
                    dayView.setTextViewText(tvDayIds[j], day)
                }
            }
            resizeDayView(options, dayView)
            rootView.addView(R.id.llyDayContainer, dayView)
            weekViews.add(dayView)
        }

        val mainIntent = getAppTopIntent(context)
        val pIntent =
            PendingIntent.getActivity(
                context,
                0x11,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        rootView.setOnClickPendingIntent(R.id.flyRoot, pIntent)

        return rootView
    }

    private fun resizeView(
        options: Bundle?,
        rootView: RemoteViews
    ) {
        (options?.get(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 155).let {
            val width = it as Int
            val scale = width / 110f

            rootView.run {
                setTextViewTextSize(R.id.tvMonth, TypedValue.COMPLEX_UNIT_SP, 7f * scale)

                for (i in 0 until 7) {
                    setTextViewTextSize(tvWeekIds[i], TypedValue.COMPLEX_UNIT_SP, 6f * scale)

                }
            }

        }
    }

    private fun resizeDayView(
        options: Bundle?,
        rootView: RemoteViews
    ) {
        (options?.get(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 155).let {
            val width = it as Int
            val scale = width / 110f

            rootView.run {
                for (i in 0 until 7) {
                    setTextViewTextSize(tvDayIds[i], TypedValue.COMPLEX_UNIT_SP, 6f * scale)

                }
            }

        }
    }

    private fun getFormattedDateStr(format: String?, timestamp: Long): String? {
        val fm1 = SimpleDateFormat(format, Locale.ENGLISH)
        fm1.timeZone = TimeZone.getTimeZone("GMT+7")
        return fm1.format(timestamp * 1000L)
    }

    private fun getAppTopIntent(context: Context): Intent {
        return Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClass(context, MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
    }
}