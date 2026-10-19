package com.custom.hinduwidget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class VisualMuhurtaTimelineView extends View {

    public static class MuhurtaSegment {
        public String name;
        public String label;
        public boolean isAuspicious;
        public double startHour; // 0..24
        public double endHour;   // 0..24
        public int color;
        public String timeRangeStr;

        public MuhurtaSegment(String name, String label, boolean isAuspicious, double startHour, double endHour, int color, String timeRangeStr) {
            this.name = name;
            this.label = label;
            this.isAuspicious = isAuspicious;
            this.startHour = startHour;
            this.endHour = endHour;
            this.color = color;
            this.timeRangeStr = timeRangeStr;
        }
    }

    private final Paint paintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintDaySpan = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintSegment = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintHourLine = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintCurrentTime = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectTrack = new RectF();
    private final RectF rectSeg = new RectF();

    private double sunriseHour = 6.58;
    private double sunsetHour = 18.15;
    private final List<MuhurtaSegment> segments = new ArrayList<>();

    public VisualMuhurtaTimelineView(Context context) {
        super(context);
        init();
    }

    public VisualMuhurtaTimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VisualMuhurtaTimelineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintBackground.setColor(Color.parseColor("#EFE8DC")); // M3 surface container high
        paintDaySpan.setColor(Color.parseColor("#FFF8E7"));    // Daytime golden glow
        paintHourLine.setColor(Color.parseColor("#C4B8A5"));   // Hour tick
        paintHourLine.setStrokeWidth(2f);

        paintText.setColor(Color.parseColor("#5A4838"));
        paintText.setTextSize(22f);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setFakeBoldText(true);

        paintCurrentTime.setStrokeWidth(4f);
        paintCurrentTime.setStyle(Paint.Style.FILL_AND_STROKE);

        applyTheme(ThemeManager.getActivePalette(getContext()));
    }

    public void applyTheme(ThemeManager.JapanesePalette palette) {
        if (palette == null) return;
        if (paintCurrentTime != null) paintCurrentTime.setColor(palette.primary);
        if (paintText != null) paintText.setColor(palette.onSurfaceVariant);
        invalidate();
    }

    public void setMuhurtaData(PanchangCalculator.DayPanchang dp) {
        segments.clear();
        if (dp == null) {
            invalidate();
            return;
        }

        this.sunriseHour = dp.sunriseHour;
        this.sunsetHour = dp.sunsetHour;

        // Parse and populate standard Muhurtas
        // 1. Brahma Muhurta (Auspicious)
        double brahmaStart = Math.max(0.0, dp.sunriseHour - (96.0 / 60.0));
        double brahmaEnd = Math.max(0.0, dp.sunriseHour - (48.0 / 60.0));
        segments.add(new MuhurtaSegment("Brahma Muhurta (ब्रह्म मुहूर्त)", "Brahma", true,
                brahmaStart, brahmaEnd, Color.parseColor("#2E7D32"), dp.muhurta != null ? dp.muhurta.brahmaMuhurta : ""));

        // 2. Abhijit Muhurta (Auspicious)
        double dayMid = (dp.sunriseHour + dp.sunsetHour) / 2.0;
        double abhijitStart = dayMid - (24.0 / 60.0);
        double abhijitEnd = dayMid + (24.0 / 60.0);
        segments.add(new MuhurtaSegment("Abhijit Muhurta (अभिजीत मुहूर्त)", "Abhijit", true,
                abhijitStart, abhijitEnd, Color.parseColor("#388E3C"), dp.muhurta != null ? dp.muhurta.abhijit : ""));

        // 3. Rahu Kaal (Inauspicious)
        double dayLen = dp.sunsetHour - dp.sunriseHour;
        double oct = dayLen / 8.0;
        int dOfWeek = dp.dayOfWeek; // 1=Sun .. 7=Sat
        int[] rahuOct = { 7, 1, 6, 4, 5, 3, 2 }; // Sun=8th, Mon=2nd, Tue=7th, Wed=5th, Thu=6th, Fri=4th, Sat=3rd (0-indexed)
        int rIdx = rahuOct[(dOfWeek - 1) % 7];
        double rahuStart = dp.sunriseHour + (rIdx * oct);
        double rahuEnd = rahuStart + oct;
        segments.add(new MuhurtaSegment("Rahu Kaal (राहु काल)", "Rahu", false,
                rahuStart, rahuEnd, Color.parseColor("#C62828"), dp.muhurta != null ? dp.muhurta.rahuKaal : ""));

        // 4. Yamagandam (Inauspicious)
        int[] yamaOct = { 4, 3, 2, 1, 0, 6, 5 };
        int yIdx = yamaOct[(dOfWeek - 1) % 7];
        double yamaStart = dp.sunriseHour + (yIdx * oct);
        double yamaEnd = yamaStart + oct;
        segments.add(new MuhurtaSegment("Yamagandam (यमगंड)", "Yamaganda", false,
                yamaStart, yamaEnd, Color.parseColor("#E65100"), dp.muhurta != null ? dp.muhurta.yamagand : ""));

        // 5. Gulika Kaal (Auspicious / Shani child)
        int[] gulikaOct = { 6, 5, 4, 3, 2, 1, 0 };
        int gIdx = gulikaOct[(dOfWeek - 1) % 7];
        double gulikaStart = dp.sunriseHour + (gIdx * oct);
        double gulikaEnd = gulikaStart + oct;
        segments.add(new MuhurtaSegment("Gulika Kaal (गुलिक काल)", "Gulika", false,
                gulikaStart, gulikaEnd, Color.parseColor("#F57F17"), dp.muhurta != null ? dp.muhurta.gulika : ""));

        // 6. Amrit Kaal (Auspicious)
        if (dp.muhurta != null && dp.muhurta.amritKaal != null && !dp.muhurta.amritKaal.isEmpty()) {
            double amritStart = (dp.sunriseHour + 2.5) % 24.0;
            double amritEnd = (amritStart + 1.5) % 24.0;
            segments.add(new MuhurtaSegment("Amrit Kaal (अमृत काल)", "Amrit", true,
                    amritStart, amritEnd, Color.parseColor("#00796B"), dp.muhurta.amritKaal));
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float padding = 8f;
        float barTop = 32f;
        float barBottom = h - 24f;
        float barHeight = barBottom - barTop;

        rectTrack.set(padding, barTop, w - padding, barBottom);

        // 1. Draw outer 24h background track
        canvas.drawRoundRect(rectTrack, 16f, 16f, paintBackground);

        // 2. Draw Daytime Golden Span
        float usableW = (w - 2 * padding);
        float xSunrise = padding + (float) ((sunriseHour / 24.0) * usableW);
        float xSunset = padding + (float) ((sunsetHour / 24.0) * usableW);
        RectF rectDay = new RectF(xSunrise, barTop, xSunset, barBottom);
        canvas.drawRect(rectDay, paintDaySpan);

        // 3. Draw Hour tick lines & labels (every 4 hours: 00, 04, 08, 12, 16, 20, 24)
        for (int hr = 0; hr <= 24; hr += 4) {
            float xHr = padding + (float) ((hr / 24.0) * usableW);
            canvas.drawLine(xHr, barTop - 6f, xHr, barBottom + 6f, paintHourLine);

            String hrLabel = String.format(Locale.US, "%02d:00", hr == 24 ? 0 : hr);
            paintText.setColor(Color.parseColor("#8C7A6B"));
            paintText.setTextSize(18f);
            canvas.drawText(hrLabel, xHr, barTop - 10f, paintText);
        }

        // 4. Draw Sunrise & Sunset vertical dashed/prominent lines
        Paint sunMarker = new Paint(Paint.ANTI_ALIAS_FLAG);
        sunMarker.setColor(Color.parseColor("#D97706")); // Amber sun
        sunMarker.setStrokeWidth(3f);
        canvas.drawLine(xSunrise, barTop, xSunrise, barBottom, sunMarker);
        canvas.drawLine(xSunset, barTop, xSunset, barBottom, sunMarker);

        // 5. Draw Muhurta Segments
        for (MuhurtaSegment seg : segments) {
            float xStart = padding + (float) ((seg.startHour / 24.0) * usableW);
            float xEnd = padding + (float) ((seg.endHour / 24.0) * usableW);

            if (xEnd < xStart) {
                // Wraps around midnight
                xEnd = w - padding;
            }

            rectSeg.set(xStart, barTop + 4f, xEnd, barBottom - 4f);
            paintSegment.setColor(seg.color);
            paintSegment.setAlpha(220);
            canvas.drawRoundRect(rectSeg, 10f, 10f, paintSegment);

            // Label inside segment if wide enough
            float segW = xEnd - xStart;
            if (segW > 36f) {
                paintText.setColor(Color.WHITE);
                paintText.setTextSize(20f);
                float textY = barTop + (barHeight / 2f) + 7f;
                canvas.drawText(seg.label, (xStart + xEnd) / 2f, textY, paintText);
            }
        }

        // 6. Draw Current Time Needle & Glowing Dot
        Calendar now = Calendar.getInstance();
        double currentHour = now.get(Calendar.HOUR_OF_DAY) + (now.get(Calendar.MINUTE) / 60.0);
        float xNow = padding + (float) ((currentHour / 24.0) * usableW);

        // Needle line
        paintCurrentTime.setColor(Color.parseColor("#BF4A1D"));
        paintCurrentTime.setStrokeWidth(4f);
        canvas.drawLine(xNow, barTop - 12f, xNow, barBottom + 12f, paintCurrentTime);

        // Top pointer triangle & "NOW"
        paintCurrentTime.setStyle(Paint.Style.FILL);
        canvas.drawCircle(xNow, barTop - 12f, 7f, paintCurrentTime);
        canvas.drawCircle(xNow, barBottom + 12f, 7f, paintCurrentTime);

        paintText.setColor(Color.parseColor("#BF4A1D"));
        paintText.setTextSize(18f);
        canvas.drawText("NOW", xNow, h - 2f, paintText);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float padding = 8f;
            float usableW = getWidth() - 2 * padding;
            if (usableW > 0) {
                double touchHour = ((x - padding) / usableW) * 24.0;
                for (MuhurtaSegment seg : segments) {
                    if (touchHour >= seg.startHour && touchHour <= seg.endHour) {
                        String nature = seg.isAuspicious ? "✨ Auspicious (शुभ)" : "⚠️ Inauspicious (अशुभ)";
                        Toast.makeText(getContext(), seg.name + "\n" + nature + "\nTime: " + seg.timeRangeStr, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }
}
