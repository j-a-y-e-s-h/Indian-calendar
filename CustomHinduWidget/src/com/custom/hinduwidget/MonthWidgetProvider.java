package com.custom.hinduwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class MonthWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_PREV_MONTH = "com.custom.hinduwidget.ACTION_PREV_MONTH";
    public static final String ACTION_NEXT_MONTH = "com.custom.hinduwidget.ACTION_NEXT_MONTH";
    public static final String ACTION_TODAY = "com.custom.hinduwidget.ACTION_TODAY";

    private static final String PREFS_NAME = "widget_prefs";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        saveWidgetDimensions(context, appWidgetId, newOptions);
        updateAppWidget(context, appWidgetManager, appWidgetId);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    public static void saveWidgetDimensions(Context context, int appWidgetId, Bundle bundle) {
        if (bundle == null) return;
        int minW = bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0);
        int maxW = bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0);
        int minH = bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0);
        int maxH = bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0);

        int orientation = context.getResources().getConfiguration().orientation;
        int height = (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE)
                ? (minH > 0 ? minH : maxH)
                : (maxH > 0 ? maxH : minH);
        int width = (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE)
                ? (maxW > 0 ? maxW : minW)
                : (minW > 0 ? minW : maxW);

        if (Build.VERSION.SDK_INT >= 31) {
            try {
                java.util.List<android.util.SizeF> sizes = bundle.getParcelableArrayList(AppWidgetManager.OPTION_APPWIDGET_SIZES);
                if (sizes != null && !sizes.isEmpty()) {
                    android.util.SizeF best = sizes.get(0);
                    for (android.util.SizeF s : sizes) {
                        if (orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
                            if (s.getHeight() > 0 && Math.abs(s.getWidth() - width) < Math.abs(best.getWidth() - width)) {
                                best = s;
                            }
                        } else {
                            if (s.getWidth() > 0 && Math.abs(s.getHeight() - height) < Math.abs(best.getHeight() - height)) {
                                best = s;
                            }
                        }
                    }
                    if (best.getHeight() > 0 && best.getWidth() > 0) {
                        height = (int) best.getHeight();
                        width = (int) best.getWidth();
                    }
                }
            } catch (Exception ignored) {}
        }

        android.util.Log.i("CustomHinduWidget", "saveWidgetDimensions id=" + appWidgetId
                + " minW=" + minW + " maxW=" + maxW + " minH=" + minH + " maxH=" + maxH
                + " finalW=" + width + " finalH=" + height);

        if (height > 0 || width > 0) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                 .putInt("widget_height_" + appWidgetId, height)
                 .putInt("widget_width_" + appWidgetId, width)
                 .commit();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        if (ACTION_PREV_MONTH.equals(action) || ACTION_NEXT_MONTH.equals(action) || ACTION_TODAY.equals(action)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            ComponentName thisWidget = new ComponentName(context, MonthWidgetProvider.class);
            int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

            for (int widgetId : allWidgetIds) {
                if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID || widgetId == appWidgetId) {
                    int offset = prefs.getInt("offset_" + widgetId, 0);
                    if (ACTION_PREV_MONTH.equals(action)) {
                        offset--;
                    } else if (ACTION_NEXT_MONTH.equals(action)) {
                        offset++;
                    } else {
                        offset = 0; // Return to today
                    }
                    prefs.edit().putInt("offset_" + widgetId, offset).apply();
                }
                updateAppWidget(context, appWidgetManager, widgetId);
            }
        } else if (Intent.ACTION_DATE_CHANGED.equals(action) ||
                   "android.intent.action.TIME_SET".equals(action) ||
                   Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            // Midnight / date change: update all active widgets
            ComponentName thisWidget = new ComponentName(context, MonthWidgetProvider.class);
            int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            for (int widgetId : allWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId);
            }
        }

        super.onReceive(context, intent);
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int monthOffset = prefs.getInt("offset_" + appWidgetId, 0);

        // Fetch current dimensions if available
        Bundle opts = appWidgetManager.getAppWidgetOptions(appWidgetId);
        saveWidgetDimensions(context, appWidgetId, opts);

        Calendar targetCal = Calendar.getInstance();
        targetCal.set(Calendar.DAY_OF_MONTH, 1);
        if (monthOffset != 0) {
            targetCal.add(Calendar.MONTH, monthOffset);
        }

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
        String gregorianMonthTitle = monthFormat.format(targetCal.getTime()).toUpperCase(Locale.US);

        int y = targetCal.get(Calendar.YEAR);
        int m = targetCal.get(Calendar.MONTH) + 1;
        int daysInMonth = targetCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Hindu Month & Samvat calculation for middle of display month
        PanchangCalculator.DayPanchang midPanchang = PanchangCalculator.calculatePanchang(y, m, 15, 20.0, 78.0);
        String hinduSubtitle = midPanchang.hinduMonth + " (Amanta) • Vikram Samvat " + midPanchang.vikramSamvat + " • Surya: " + midPanchang.suryaRashi;

        // Dynamic Banner String with proper details
        String bannerText;
        if (monthOffset == 0) {
            Calendar now = Calendar.getInstance();
            PanchangCalculator.DayPanchang todayP = PanchangCalculator.calculatePanchang(
                    now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), 20.0, 78.0);
            StringBuilder sb = new StringBuilder();
            sb.append("TODAY: ").append(todayP.paksha).append(" ").append(todayP.tithiName);
            sb.append(" • ").append(todayP.nakshatra).append(" Nakshatra");
            sb.append(" • ").append(todayP.chandraRashi).append(" Rashi");
            if (todayP.hasFestival()) {
                sb.append(" • ").append(todayP.specialOccasion);
            }
            bannerText = sb.toString();
        } else {
            // Find key festivals of navigated month
            Set<String> monthFestivals = new LinkedHashSet<>();
            for (int d = 1; d <= daysInMonth; d++) {
                PanchangCalculator.DayPanchang dp = PanchangCalculator.calculatePanchang(y, m, d, 20.0, 78.0);
                if (dp.hasFestival()) {
                    monthFestivals.add(dp.specialOccasion + " (" + d + ")");
                }
            }
            if (!monthFestivals.isEmpty()) {
                bannerText = "Festivals: " + String.join(" • ", monthFestivals);
            } else {
                bannerText = "Vikram Samvat " + midPanchang.vikramSamvat + " • Saka " + midPanchang.sakaSamvat;
            }
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_month);
        views.setTextViewText(R.id.widget_month_title, gregorianMonthTitle);
        views.setTextViewText(R.id.widget_hindu_month, hinduSubtitle);
        views.setTextViewText(R.id.widget_today_banner, bannerText);

        int widgetHeight = prefs.getInt("widget_height_" + appWidgetId, 290);
        int widgetWidth = prefs.getInt("widget_width_" + appWidgetId, 330);

        // RemoteViewsService Intent for the GridView
        Intent serviceIntent = new Intent(context, MonthWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.putExtra("widget_height", widgetHeight);
        serviceIntent.putExtra("widget_width", widgetWidth);
        serviceIntent.putExtra("month_offset", monthOffset);
        serviceIntent.setData(Uri.parse("customhinduwidget://widget/grid/" + appWidgetId + "/" + widgetHeight + "/" + widgetWidth + "/" + monthOffset));
        views.setRemoteAdapter(R.id.calendar_grid, serviceIntent);

        int immutableFlag = (Build.VERSION.SDK_INT >= 23) ? PendingIntent.FLAG_IMMUTABLE : 0;
        int mutableFlag = (Build.VERSION.SDK_INT >= 31) ? PendingIntent.FLAG_MUTABLE : 0;

        // Previous Month Button with unique data URI
        Intent prevIntent = new Intent(context, MonthWidgetProvider.class);
        prevIntent.setAction(ACTION_PREV_MONTH);
        prevIntent.setData(Uri.parse("customhinduwidget://widget/prev/" + appWidgetId));
        prevIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId * 10 + 1, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag);
        views.setOnClickPendingIntent(R.id.btn_prev_month, prevPendingIntent);

        // Next Month Button with unique data URI
        Intent nextIntent = new Intent(context, MonthWidgetProvider.class);
        nextIntent.setAction(ACTION_NEXT_MONTH);
        nextIntent.setData(Uri.parse("customhinduwidget://widget/next/" + appWidgetId));
        nextIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId * 10 + 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag);
        views.setOnClickPendingIntent(R.id.btn_next_month, nextPendingIntent);

        // Header Title & Banner (Click to return to today's month)
        Intent todayIntent = new Intent(context, MonthWidgetProvider.class);
        todayIntent.setAction(ACTION_TODAY);
        todayIntent.setData(Uri.parse("customhinduwidget://widget/today/" + appWidgetId));
        todayIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent todayPendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId * 10 + 3, todayIntent, PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag);
        views.setOnClickPendingIntent(R.id.header_title_container, todayPendingIntent);
        views.setOnClickPendingIntent(R.id.widget_month_title, todayPendingIntent);
        views.setOnClickPendingIntent(R.id.widget_hindu_month, todayPendingIntent);
        views.setOnClickPendingIntent(R.id.widget_today_banner, todayPendingIntent);

        // Grid Cell Click Intent Template
        Intent cellClickIntent = new Intent(context, MainActivity.class);
        PendingIntent cellClickPendingIntent = PendingIntent.getActivity(
                context, appWidgetId, cellClickIntent, PendingIntent.FLAG_UPDATE_CURRENT | mutableFlag);
        views.setPendingIntentTemplate(R.id.calendar_grid, cellClickPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.calendar_grid);
    }
}
