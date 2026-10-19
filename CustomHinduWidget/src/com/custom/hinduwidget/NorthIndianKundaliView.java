package com.custom.hinduwidget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class NorthIndianKundaliView extends View {

    private Paint borderPaint;
    private Paint gridPaint;
    private Paint houseNumberPaint;
    private Paint planetPaint;
    private Paint lagnaPaint;

    private PanchangCalculator.DayPanchang panchangData;

    public NorthIndianKundaliView(Context context) {
        super(context);
        init();
    }

    public NorthIndianKundaliView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NorthIndianKundaliView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(0xFFFFB77C); // Material 3 Saffron Primary
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4.0f);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(0xCCFFB77C);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2.5f);

        houseNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        houseNumberPaint.setColor(0x99FFDCC2); // Subtle On-Surface-Variant
        houseNumberPaint.setTextSize(26.0f);
        houseNumberPaint.setTextAlign(Paint.Align.CENTER);

        planetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        planetPaint.setColor(0xFFFFE082); // Gold
        planetPaint.setTextSize(30.0f);
        planetPaint.setTextAlign(Paint.Align.CENTER);
        planetPaint.setFakeBoldText(true);

        lagnaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lagnaPaint.setColor(0xFFFF7043); // Coral Lagna
        lagnaPaint.setTextSize(30.0f);
        lagnaPaint.setTextAlign(Paint.Align.CENTER);
        lagnaPaint.setFakeBoldText(true);

        applyTheme(ThemeManager.getActivePalette(getContext()));
    }

    public void applyTheme(ThemeManager.JapanesePalette palette) {
        if (palette == null) return;
        if (borderPaint != null) borderPaint.setColor(palette.primary);
        if (gridPaint != null) gridPaint.setColor(palette.primary);
        if (houseNumberPaint != null) houseNumberPaint.setColor(palette.onSurfaceVariant);
        if (planetPaint != null) planetPaint.setColor(palette.swatch2);
        if (lagnaPaint != null) lagnaPaint.setColor(palette.primary);
        invalidate();
    }

    public void setPanchangData(PanchangCalculator.DayPanchang data) {
        this.panchangData = data;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        // Keep it square
        int size = width > 0 ? width : 600;
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float pad = 24.0f;

        float left = pad;
        float top = pad;
        float right = w - pad;
        float bottom = h - pad;
        float midX = (left + right) / 2.0f;
        float midY = (top + bottom) / 2.0f;

        // Background card rounded rect
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(0xFF221F1A); // M3 Surface Container
        bgPaint.setStyle(Paint.Style.FILL);
        RectF bgRect = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(bgRect, 20.0f, 20.0f, bgPaint);

        // Outer border
        canvas.drawRoundRect(bgRect, 20.0f, 20.0f, borderPaint);

        // Diagonals (Corner to Corner)
        canvas.drawLine(left, top, right, bottom, gridPaint);
        canvas.drawLine(left, bottom, right, top, gridPaint);

        // Inner Diamond connecting midpoints
        Path diamondPath = new Path();
        diamondPath.moveTo(midX, top);
        diamondPath.lineTo(right, midY);
        diamondPath.lineTo(midX, bottom);
        diamondPath.lineTo(left, midY);
        diamondPath.close();
        canvas.drawPath(diamondPath, gridPaint);

        if (panchangData == null || panchangData.planets == null || panchangData.planets.isEmpty()) {
            return;
        }

        // Ascendant (Lagna) Rashi index: 0..11 (0=Mesha/Aries .. 9=Makar/Capricorn)
        PanchangCalculator.PlanetPosition lagnaPos = panchangData.planets.get(0);
        int lagnaRashi = lagnaPos.rashiIndex; // 0..11

        // Map planets to Houses 1..12
        // House 1 has lagnaRashi, House 2 has (lagnaRashi+1)%12, etc.
        List<List<String>> housePlanets = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            housePlanets.add(new ArrayList<>());
        }

        // Add Lagna to House 1
        housePlanets.get(0).add("Lagna");

        for (int i = 1; i < panchangData.planets.size(); i++) {
            PanchangCalculator.PlanetPosition p = panchangData.planets.get(i);
            int pRashi = p.rashiIndex;
            // House number: (pRashi - lagnaRashi + 12) % 12
            int houseIdx = (pRashi - lagnaRashi + 12) % 12;
            String label = p.sanskritName;
            if (p.isRetrograde && !p.sanskritName.equals("Rahu") && !p.sanskritName.equals("Ketu")) {
                label += "*";
            }
            housePlanets.get(houseIdx).add(label);
        }

        // 12 House Centers (approximate coordinates in North Indian Vedic layout)
        // House 1: Top Diamond (center = midX, (top + midY) / 2)
        // House 2: Top-left triangle (center = (left + midX)/2, (top + top + (midY-top)/2)/2)
        // House 3: Upper-left corner triangle
        // House 4: Left diamond
        // House 5: Lower-left corner triangle
        // House 6: Bottom-left triangle
        // House 7: Bottom diamond
        // House 8: Bottom-right triangle
        // House 9: Lower-right corner triangle
        // House 10: Right diamond
        // House 11: Upper-right corner triangle
        // House 12: Top-right triangle

        float[][] houseCenters = {
            { midX, (top + midY) / 2.0f + 10.0f }, // H1
            { left + (midX - left) * 0.45f, top + (midY - top) * 0.25f }, // H2
            { left + (midX - left) * 0.25f, top + (midY - top) * 0.45f }, // H3
            { (left + midX) / 2.0f, midY }, // H4
            { left + (midX - left) * 0.25f, midY + (bottom - midY) * 0.55f }, // H5
            { left + (midX - left) * 0.45f, midY + (bottom - midY) * 0.75f }, // H6
            { midX, (midY + bottom) / 2.0f }, // H7
            { right - (right - midX) * 0.45f, midY + (bottom - midY) * 0.75f }, // H8
            { right - (right - midX) * 0.25f, midY + (bottom - midY) * 0.55f }, // H9
            { (midX + right) / 2.0f, midY }, // H10
            { right - (right - midX) * 0.25f, top + (midY - top) * 0.45f }, // H11
            { right - (right - midX) * 0.45f, top + (midY - top) * 0.25f }  // H12
        };

        for (int hIdx = 0; hIdx < 12; hIdx++) {
            float cx = houseCenters[hIdx][0];
            float cy = houseCenters[hIdx][1];

            // Rashi number (1 to 12, 1=Aries, 10=Capricorn)
            int rashiNum = ((lagnaRashi + hIdx) % 12) + 1;
            canvas.drawText(String.valueOf(rashiNum), cx, cy - 24.0f, houseNumberPaint);

            // Draw planets
            List<String> pls = housePlanets.get(hIdx);
            if (!pls.isEmpty()) {
                float startY = cy + 10.0f;
                for (int pIdx = 0; pIdx < pls.size(); pIdx++) {
                    String pName = pls.get(pIdx);
                    Paint p = pName.equals("Lagna") ? lagnaPaint : planetPaint;
                    canvas.drawText(pName, cx, startY + (pIdx * 32.0f), p);
                }
            }
        }
    }
}
