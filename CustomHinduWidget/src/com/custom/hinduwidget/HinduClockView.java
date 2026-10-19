package com.custom.hinduwidget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;
import java.util.Locale;

public class HinduClockView extends View {

    private Paint dialPaint;
    private Paint dayArcPaint;
    private Paint nightArcPaint;
    private Paint graduationPaint;
    private Paint textPaint;
    private Paint ghatiHandPaint;
    private Paint palaHandPaint;
    private Paint vipalHandPaint;
    private Paint centerPinPaint;

    private double sunriseHour = 6.44;
    private double sunsetHour = 17.95;

    private int ghati = 0;
    private int pala = 0;
    private int vipala = 0;

    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable ticker = new Runnable() {
        @Override
        public void run() {
            updateTime();
            invalidate();
            timerHandler.postDelayed(this, 1000);
        }
    };

    public HinduClockView(Context context) {
        super(context);
        init();
    }

    public HinduClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HinduClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        dialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dialPaint.setColor(0xFF221F1A);
        dialPaint.setStyle(Paint.Style.FILL);

        dayArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayArcPaint.setColor(0x33FFB77C); // Translucent Gold/Saffron
        dayArcPaint.setStyle(Paint.Style.FILL);

        nightArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nightArcPaint.setColor(0x22000000); // Translucent Dark
        nightArcPaint.setStyle(Paint.Style.FILL);

        graduationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        graduationPaint.setColor(0xAAFFB77C);
        graduationPaint.setStyle(Paint.Style.STROKE);
        graduationPaint.setStrokeWidth(2.0f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFFFDCC2);
        textPaint.setTextSize(22.0f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        ghatiHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ghatiHandPaint.setColor(0xFFFF8F00); // Saffron
        ghatiHandPaint.setStrokeWidth(8.0f);
        ghatiHandPaint.setStrokeCap(Paint.Cap.ROUND);

        palaHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        palaHandPaint.setColor(0xFFFFE082); // Light Gold
        palaHandPaint.setStrokeWidth(5.0f);
        palaHandPaint.setStrokeCap(Paint.Cap.ROUND);

        vipalHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        vipalHandPaint.setColor(0xFFEF5350); // Coral Red
        vipalHandPaint.setStrokeWidth(2.5f);
        vipalHandPaint.setStrokeCap(Paint.Cap.ROUND);

        centerPinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPinPaint.setColor(0xFFFFD54F);
        centerPinPaint.setStyle(Paint.Style.FILL);

        applyTheme(ThemeManager.getActivePalette(getContext()));
    }

    public void applyTheme(ThemeManager.JapanesePalette palette) {
        if (palette == null) return;
        if (dialPaint != null) dialPaint.setColor(palette.surfaceContainer);
        if (dayArcPaint != null) dayArcPaint.setColor((palette.primary & 0x00FFFFFF) | 0x33000000);
        if (graduationPaint != null) graduationPaint.setColor(palette.primary);
        if (textPaint != null) textPaint.setColor(palette.onSurface);
        if (ghatiHandPaint != null) ghatiHandPaint.setColor(palette.primary);
        if (palaHandPaint != null) palaHandPaint.setColor(palette.swatch2);
        if (centerPinPaint != null) centerPinPaint.setColor(palette.primary);
        invalidate();
    }

    public void setSunriseSunset(double sunrise, double sunset) {
        this.sunriseHour = sunrise;
        this.sunsetHour = sunset;
        updateTime();
        invalidate();
    }

    private void updateTime() {
        Calendar now = Calendar.getInstance();
        double currentHour = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0 + now.get(Calendar.SECOND) / 3600.0;
        double elapsedHours = currentHour - sunriseHour;
        if (elapsedHours < 0) elapsedHours += 24.0;

        double totalGhati = elapsedHours * 2.5; // 24hr = 60 ghati
        ghati = (int) totalGhati;
        double remPala = (totalGhati - ghati) * 60.0;
        pala = (int) remPala;
        vipala = (int) Math.round((remPala - pala) * 60.0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        timerHandler.post(ticker);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        timerHandler.removeCallbacks(ticker);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int size = w > 0 ? w : 600;
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float cx = w / 2.0f;
        float cy = h / 2.0f;
        float r = Math.min(w, h) / 2.0f - 24.0f;

        // Background Circle
        canvas.drawCircle(cx, cy, r, dialPaint);

        // Calculate Day Arc in 60 Ghati dial (1 Ghati = 6 degrees, 60 Ghati = 360 degrees)
        double dayLengthHours = sunsetHour - sunriseHour;
        if (dayLengthHours < 0) dayLengthHours += 24.0;
        float dayGhati = (float)(dayLengthHours * 2.5);
        float dayDegrees = dayGhati * 6.0f;

        RectF dialRect = new RectF(cx - r, cy - r, cx + r, cy + r);
        // Start at -90 deg (Top = Sunrise = 0 Ghati)
        canvas.drawArc(dialRect, -90.0f, dayDegrees, true, dayArcPaint);
        canvas.drawArc(dialRect, -90.0f + dayDegrees, 360.0f - dayDegrees, true, nightArcPaint);

        // Outer Ring
        Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setColor(0xFFFFB77C);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(3.0f);
        canvas.drawCircle(cx, cy, r, ringPaint);

        // 60 Graduations
        for (int i = 0; i < 60; i++) {
            double angleRad = Math.toRadians((i * 6.0) - 90.0);
            float xOuter = (float)(cx + r * Math.cos(angleRad));
            float yOuter = (float)(cy + r * Math.sin(angleRad));

            float tickLen = (i % 5 == 0) ? 18.0f : 8.0f;
            float xInner = (float)(cx + (r - tickLen) * Math.cos(angleRad));
            float yInner = (float)(cy + (r - tickLen) * Math.sin(angleRad));

            graduationPaint.setStrokeWidth((i % 5 == 0) ? 3.0f : 1.5f);
            canvas.drawLine(xInner, yInner, xOuter, yOuter, graduationPaint);

            // Numbers at multiples of 5 (00, 05, 10, ... 55)
            if (i % 5 == 0) {
                float textR = r - 36.0f;
                float tx = (float)(cx + textR * Math.cos(angleRad));
                float ty = (float)(cy + textR * Math.sin(angleRad)) + 8.0f;
                String numStr = String.format(Locale.US, "%02d", i);
                canvas.drawText(numStr, tx, ty, textPaint);
            }
        }

        // Draw Hands
        // Ghati Hand: 60 Ghati = 360 deg -> (ghati + pala/60.0) * 6.0
        double ghatiAngle = (ghati + pala / 60.0) * 6.0 - 90.0;
        float gx = (float)(cx + (r * 0.52f) * Math.cos(Math.toRadians(ghatiAngle)));
        float gy = (float)(cy + (r * 0.52f) * Math.sin(Math.toRadians(ghatiAngle)));
        canvas.drawLine(cx, cy, gx, gy, ghatiHandPaint);

        // Pala Hand: 60 Pala = 360 deg -> (pala + vipala/60.0) * 6.0
        double palaAngle = (pala + vipala / 60.0) * 6.0 - 90.0;
        float px = (float)(cx + (r * 0.72f) * Math.cos(Math.toRadians(palaAngle)));
        float py = (float)(cy + (r * 0.72f) * Math.sin(Math.toRadians(palaAngle)));
        canvas.drawLine(cx, cy, px, py, palaHandPaint);

        // Vipala Hand: 60 Vipala = 360 deg
        double vipalAngle = (vipala * 6.0) - 90.0;
        float vx = (float)(cx + (r * 0.82f) * Math.cos(Math.toRadians(vipalAngle)));
        float vy = (float)(cy + (r * 0.82f) * Math.sin(Math.toRadians(vipalAngle)));
        canvas.drawLine(cx, cy, vx, vy, vipalHandPaint);

        // Center Pin
        canvas.drawCircle(cx, cy, 10.0f, centerPinPaint);
    }

    public int getGhati() { return ghati; }
    public int getPala() { return pala; }
    public int getVipala() { return vipala; }
}
