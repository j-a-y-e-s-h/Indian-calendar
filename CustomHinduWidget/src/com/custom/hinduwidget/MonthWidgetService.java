package com.custom.hinduwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MonthWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MonthGridRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class MonthGridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context mContext;
    private final int mAppWidgetId;
    private int mWidgetHeight;
    private int mWidgetWidth;
    private final List<PanchangCalculator.DayPanchang> mItems = new ArrayList<>();

    // User design colors
    private static final int COLOR_DATE_NORMAL = 0xFFF5F5F5;
    private static final int COLOR_DATE_TODAY = 0xFFFFFFFF;
    private static final int COLOR_TITHI_NORMAL = 0xFFF0DFC6;
    private static final int COLOR_NAKSHATRA = 0xFFBDB6AB;
    private static final int COLOR_OCCASION = 0xFFFFD778;
    private static final int COLOR_TITHI_MUTED = 0xFFBDB6AB;

    public MonthGridRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID,
                android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID);
        mWidgetHeight = intent.getIntExtra("widget_height", 0);
        mWidgetWidth = intent.getIntExtra("widget_width", 0);
    }

    @Override
    public void onCreate() {
        loadCalendarData();
    }

    @Override
    public void onDataSetChanged() {
        SharedPreferences prefs = mContext.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE);
        int h = prefs.getInt("widget_height_" + mAppWidgetId, 0);
        if (h > 0) mWidgetHeight = h;
        int w = prefs.getInt("widget_width_" + mAppWidgetId, 0);
        if (w > 0) mWidgetWidth = w;
        loadCalendarData();
    }

    @Override
    public void onDestroy() {
        mItems.clear();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position < 0 || position >= mItems.size()) {
            return null;
        }

        PanchangCalculator.DayPanchang item = mItems.get(position);
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_month_cell);

        // Read dynamically stored or queried widget dimensions
        SharedPreferences prefs = mContext.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE);
        int widgetHeight = prefs.getInt("widget_height_" + mAppWidgetId, mWidgetHeight);
        int widgetWidth = prefs.getInt("widget_width_" + mAppWidgetId, mWidgetWidth);

        if (widgetHeight <= 0) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(mContext);
            android.os.Bundle opts = mgr.getAppWidgetOptions(mAppWidgetId);
            if (opts != null) {
                widgetHeight = Math.max(opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0),
                                        opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0));
            }
        }
        if (widgetHeight <= 0) {
            widgetHeight = 290; // Safe baseline if unset
        }

        int numRows = Math.max(5, (mItems.size() + 6) / 7);

        // Account for launcher density scaling on Xiaomi / HyperOS / POCO
        // On HyperOS, LauncherWidgetLayoutFactory scales widget RemoteViews down by ~0.78 (2.535 / 3.25 density).
        // Using 1.24f with safe bounds ensures the grid fills the height without any bottom row clipping.
        boolean isXiaomi = "Xiaomi".equalsIgnoreCase(android.os.Build.MANUFACTURER) ||
                           "Redmi".equalsIgnoreCase(android.os.Build.MANUFACTURER) ||
                           "POCO".equalsIgnoreCase(android.os.Build.MANUFACTURER);
        float launcherScaleFactor = isXiaomi ? 1.24f : 1.0f;
        float effectiveHeightDp = (float) widgetHeight * launcherScaleFactor;

        // Header height in Launcher coordinate space:
        // Nav header (~32dp) + Today banner (~18dp) + Weekdays (~18dp) + padding/margins (~4dp) = ~72dp
        float headerAreaHeightDp = (effectiveHeightDp < 280f) ? 58f : ((effectiveHeightDp < 360f) ? 66f : 72f);
        // Safety margin so bottom row border and text never clip against the GridView viewport edge
        float bottomSafetyMarginDp = 8.0f;
        float gridAvailableHeightDp = Math.max(160f, effectiveHeightDp - headerAreaHeightDp - bottomSafetyMarginDp);

        int verticalSpacingDp = 1;
        float totalSpacingDp = (numRows - 1) * verticalSpacingDp;
        float cellHeightDp = (gridAvailableHeightDp - totalSpacingDp) / (float) numRows;

        // Optimal bounds to guarantee 100% visible, unclipped borders & text:
        // In 5-row month: 53.0dp - 54.0dp (134-137px per row in launcher, perfectly unclipped)
        // In 6-row month: 44.0dp - 45.5dp
        float maxAllowedHeightDp = (numRows > 5) ? 46.0f : 54.0f;
        float minCellHeightDp = (numRows > 5) ? 42.0f : 51.0f;
        if (cellHeightDp > maxAllowedHeightDp) {
            cellHeightDp = maxAllowedHeightDp;
        }
        if (cellHeightDp < minCellHeightDp) {
            cellHeightDp = minCellHeightDp;
        }

        float density = mContext.getResources().getDisplayMetrics().density;
        int cellHeightPx = Math.round(cellHeightDp * density);

        // Auto-scaling element dimensions based on available cell height
        int moonSizeDp;
        float dayTextSizeSp;
        float tithiTextSizeSp;
        float monthTextSizeSp;
        float rashiNakTextSizeSp = 7.5f;
        float occasionTextSizeSp = 7.5f;
        boolean isSpacious = (cellHeightDp >= 68.0f);

        if (cellHeightDp >= 85.0f) {
            // Extra Tall / Full Screen
            moonSizeDp = 42;
            dayTextSizeSp = 20.0f;
            tithiTextSizeSp = 14.0f;
            monthTextSizeSp = 10.5f;
            rashiNakTextSizeSp = 9.2f;
            occasionTextSizeSp = 9.2f;
        } else if (cellHeightDp >= 68.0f) {
            // Very Spacious (when resized taller: 5 or 6 rows)
            moonSizeDp = 38;
            dayTextSizeSp = 18.5f;
            tithiTextSizeSp = 13.0f;
            monthTextSizeSp = 9.8f;
            rashiNakTextSizeSp = 8.2f;
            occasionTextSizeSp = 8.2f;
        } else if (cellHeightDp >= 56.0f) {
            // Standard 4-cell Mode (~57-62dp)
            moonSizeDp = 35;
            dayTextSizeSp = 17.5f;
            tithiTextSizeSp = 12.5f;
            monthTextSizeSp = 8.5f;
        } else {
            // Compact Mode (~46-55dp)
            moonSizeDp = 33;
            dayTextSizeSp = 16.5f;
            tithiTextSizeSp = 11.5f;
            monthTextSizeSp = 7.8f;
        }

        if (position == 0) {
            android.util.Log.i("CustomHinduWidget", "getViewAt pos 0: widgetId=" + mAppWidgetId + " widgetH=" + widgetHeight + " cellHDp=" + cellHeightDp + " cellHPx=" + cellHeightPx + " isSpacious=" + isSpacious);
        }

        // 1. Force exact height on internal child anchor (guarantees RelativeLayout measures to cellHeightDp)
        views.setViewLayoutHeight(R.id.cell_height_guide, cellHeightDp, TypedValue.COMPLEX_UNIT_DIP);
        // 2. Set minimum height on cell_container (enforced in GridView UNBOUNDED measure)
        views.setInt(R.id.cell_container, "setMinimumHeight", cellHeightPx);
        // 3. Set layout height on cell_container
        views.setViewLayoutHeight(R.id.cell_container, cellHeightDp, TypedValue.COMPLEX_UNIT_DIP);

        if (!item.isCurrentMonth) {
            views.setViewVisibility(R.id.cell_moon, View.INVISIBLE);
            views.setTextViewText(R.id.cell_tithi, "");
            views.setTextViewText(R.id.cell_month, "");
            views.setTextViewText(R.id.cell_day, "");
            views.setTextViewText(R.id.cell_extra_tag, "");
            views.setViewVisibility(R.id.cell_spacious_info, View.GONE);
            views.setInt(R.id.cell_container, "setBackgroundResource", R.drawable.cell_transparent);
            views.setOnClickFillInIntent(R.id.cell_container, new Intent());
            return views;
        }

        // Top-left Realistic Moon
        views.setViewVisibility(R.id.cell_moon, View.VISIBLE);
        views.setImageViewResource(R.id.cell_moon, item.moonDrawableResId);
        views.setViewLayoutWidth(R.id.cell_moon, moonSizeDp, TypedValue.COMPLEX_UNIT_DIP);
        views.setViewLayoutHeight(R.id.cell_moon, moonSizeDp, TypedValue.COMPLEX_UNIT_DIP);

        // Hindu Tithi number on top half of Moon
        views.setTextViewText(R.id.cell_tithi, String.valueOf(item.tithiIndex));
        views.setTextViewTextSize(R.id.cell_tithi, TypedValue.COMPLEX_UNIT_SP, tithiTextSizeSp);
        views.setTextColor(R.id.cell_tithi, 0xFFFFE18C);

        // Hindu Month Name (e.g. Ashwin / Bhadra) on Moon lower half
        views.setTextViewText(R.id.cell_month, item.getShortMonthName());
        views.setTextViewTextSize(R.id.cell_month, TypedValue.COMPLEX_UNIT_SP, monthTextSizeSp);
        views.setTextColor(R.id.cell_month, COLOR_TITHI_NORMAL);

        // Gregorian day number in bottom-right corner (never clipped)
        views.setTextViewText(R.id.cell_day, String.valueOf(item.day));
        views.setTextViewTextSize(R.id.cell_day, TypedValue.COMPLEX_UNIT_SP, dayTextSizeSp);
        views.setTextColor(R.id.cell_day, item.isToday ? COLOR_DATE_TODAY : COLOR_DATE_NORMAL);

        // Astrological / Festival information based on mode
        if (isSpacious) {
            views.setViewVisibility(R.id.cell_spacious_info, View.VISIBLE);
            views.setViewVisibility(R.id.cell_extra_tag, View.GONE);

            String rashiNak = item.chandraRashi + " • " + item.nakshatra;
            if (rashiNak.length() > 16) {
                String shortNak = item.nakshatra.replace("Phalguni", "Phal.").replace("Bhadrapada", "Bhad.");
                rashiNak = item.chandraRashi + " • " + shortNak;
            }
            views.setTextViewText(R.id.cell_rashi_nakshatra, rashiNak);
            views.setTextViewTextSize(R.id.cell_rashi_nakshatra, TypedValue.COMPLEX_UNIT_SP, rashiNakTextSizeSp);
            views.setTextColor(R.id.cell_rashi_nakshatra, COLOR_NAKSHATRA);

            views.setTextViewText(R.id.cell_occasion, item.getDisplayOccasion());
            views.setTextViewTextSize(R.id.cell_occasion, TypedValue.COMPLEX_UNIT_SP, occasionTextSizeSp);
            if (item.hasFestival()) {
                views.setTextColor(R.id.cell_occasion, COLOR_OCCASION);
            } else {
                views.setTextColor(R.id.cell_occasion, COLOR_TITHI_MUTED);
            }
        } else {
            // Compact Mode: show bottom-left tag
            views.setViewVisibility(R.id.cell_spacious_info, View.GONE);
            views.setViewVisibility(R.id.cell_extra_tag, View.VISIBLE);

            if (item.hasFestival()) {
                views.setTextViewText(R.id.cell_extra_tag, item.getShortOccasion());
                views.setTextColor(R.id.cell_extra_tag, COLOR_OCCASION);
            } else {
                // Show Chandra Rashi (e.g. Dhanu, Makar, etc.)
                views.setTextViewText(R.id.cell_extra_tag, item.chandraRashi);
                views.setTextColor(R.id.cell_extra_tag, COLOR_NAKSHATRA);
            }
        }

        // Lunar Phase Highlights (Dark Phase, Light Phase, Amavasya, Purnima/Date 26, and Today)
        if (item.isToday) {
            views.setInt(R.id.cell_container, "setBackgroundResource", R.drawable.today_highlight);
        } else if (item.tithiIndex == 15) {
            // Purnima (Full Moon - Highlighted on Date 26 with olive-gold wash)
            views.setInt(R.id.cell_container, "setBackgroundResource", R.drawable.cell_purnima);
        } else if (item.tithiIndex == 30) {
            // Amavasya (New Moon - Deep dark wash)
            views.setInt(R.id.cell_container, "setBackgroundResource", R.drawable.cell_amavasya);
        } else if (item.tithiIndex < 15) {
            // Shukla Paksha (Light Phase - Soft luminous wash)
            views.setInt(R.id.cell_container, "setBackgroundResource", R.drawable.cell_shukla);
        } else {
            // Krishna Paksha (Dark Phase - Subtle dark wash)
            views.setInt(R.id.cell_container, "setBackgroundResource", R.drawable.cell_krishna);
        }

        // Fill-in intent for clicking a date cell
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("year", item.year);
        fillInIntent.putExtra("month", item.month);
        fillInIntent.putExtra("day", item.day);
        views.setOnClickFillInIntent(R.id.cell_container, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private void loadCalendarData() {
        mItems.clear();

        SharedPreferences prefs = mContext.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE);
        int monthOffset = prefs.getInt("offset_" + mAppWidgetId, 0);

        Calendar todayCal = Calendar.getInstance();
        int todayYear = todayCal.get(Calendar.YEAR);
        int todayMonth = todayCal.get(Calendar.MONTH) + 1; // 1-12
        int todayDay = todayCal.get(Calendar.DAY_OF_MONTH);

        Calendar targetCal = Calendar.getInstance();
        targetCal.set(Calendar.DAY_OF_MONTH, 1);
        if (monthOffset != 0) {
            targetCal.add(Calendar.MONTH, monthOffset);
        }

        int year = targetCal.get(Calendar.YEAR);
        int month = targetCal.get(Calendar.MONTH) + 1;
        int daysInMonth = targetCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Day of week for 1st of month: 1=Sunday..7=Saturday
        int firstDayOfWeek = targetCal.get(Calendar.DAY_OF_WEEK);
        int leadingEmptyDays = firstDayOfWeek - Calendar.SUNDAY;

        for (int i = 0; i < leadingEmptyDays; i++) {
            PanchangCalculator.DayPanchang emptyItem = new PanchangCalculator.DayPanchang();
            emptyItem.isCurrentMonth = false;
            mItems.add(emptyItem);
        }

        for (int d = 1; d <= daysInMonth; d++) {
            PanchangCalculator.DayPanchang dayPanchang = PanchangCalculator.calculatePanchang(
                    year, month, d, 20.0, 78.0);
            dayPanchang.isCurrentMonth = true;
            dayPanchang.isToday = (year == todayYear && month == todayMonth && d == todayDay);
            dayPanchang.moonDrawableResId = getMoonDrawable(dayPanchang.tithiIndex);
            mItems.add(dayPanchang);
        }

        int remainder = mItems.size() % 7;
        if (remainder != 0) {
            int trailingEmpty = 7 - remainder;
            for (int i = 0; i < trailingEmpty; i++) {
                PanchangCalculator.DayPanchang emptyItem = new PanchangCalculator.DayPanchang();
                emptyItem.isCurrentMonth = false;
                mItems.add(emptyItem);
            }
        }
    }

    private int getMoonDrawable(int tithiIndex) {
        String resName = String.format(java.util.Locale.US, "moon_real_%02d", Math.max(1, Math.min(30, tithiIndex)));
        int resId = mContext.getResources().getIdentifier(resName, "drawable", mContext.getPackageName());
        if (resId != 0) {
            return resId;
        }
        return R.drawable.moon_real_01;
    }
}
