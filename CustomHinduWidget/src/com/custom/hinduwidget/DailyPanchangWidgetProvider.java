package com.custom.hinduwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * AppWidgetProvider for 2x2 / 4x2 Daily Panchang home screen widget.
 * Displays Moon graphic, Illumination %, Tithi, Nakshatra, Sunrise & Sunset.
 */
public class DailyPanchangWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId);
        }
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        CityManager.City city = CityManager.getSelectedCity(context);
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);

        PanchangCalculator.DayPanchang dp = PanchangCalculator.calculatePanchang(
            year, month, day, city.latitude, city.longitude
        );

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_daily_panchang);

        // Date & Vaar
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy", Locale.US);
        String dateStr = sdf.format(now.getTime()) + " • " + dp.vaarName;
        views.setTextViewText(R.id.widget_panchang_date_vaar, dateStr);

        // Tithi
        String tithiFull = dp.hinduMonth + " " + dp.paksha + " " + dp.tithiName;
        if (dp.hasFestival()) {
            tithiFull += " (" + dp.specialOccasion + ")";
        }
        views.setTextViewText(R.id.widget_panchang_tithi, tithiFull);

        // Nakshatra & Moon Rashi
        views.setTextViewText(R.id.widget_panchang_nakshatra, dp.nakshatra + " • " + dp.chandraRashiFull);

        // Sun timings
        views.setTextViewText(R.id.widget_panchang_sun_timings, "🌅 " + dp.sunriseStr + " • 🌇 " + dp.sunsetStr);

        // City badge
        views.setTextViewText(R.id.widget_panchang_city_badge, city.name);

        // Moon Graphic & Illum
        views.setImageViewResource(R.id.widget_panchang_moon, dp.moonDrawableResId);
        views.setTextViewText(R.id.widget_panchang_moon_pct, (int) Math.round(dp.moonIlluminationPercent) + "% Illum.");

        // Click to launch MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_panchang_root, pi);

        appWidgetManager.updateAppWidget(widgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (Intent.ACTION_DATE_CHANGED.equals(action) ||
            Intent.ACTION_TIMEZONE_CHANGED.equals(action) ||
            Intent.ACTION_TIME_CHANGED.equals(action)) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            ComponentName cn = new ComponentName(context, DailyPanchangWidgetProvider.class);
            int[] ids = mgr.getAppWidgetIds(cn);
            if (ids != null && ids.length > 0) {
                onUpdate(context, mgr, ids);
            }
        }
    }
}
