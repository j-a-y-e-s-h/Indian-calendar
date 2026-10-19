package com.custom.hinduwidget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

/**
 * East Indian (Bangla / Maithili) Kundali Chart View.
 * Displays classical 12-bhavas in counter-clockwise East Indian style with fixed zodiac positions.
 */
public class EastIndianKundaliView extends View {

    private Paint borderPaint;
    private Paint gridPaint;
    private Paint rashiNumberPaint;
    private Paint planetPaint;
    private Paint lagnaPaint;

    private PanchangCalculator.DayPanchang panchangData;

    public EastIndianKundaliView(Context context) {
        super(context);
        init();
    }

    public EastIndianKundaliView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EastIndianKundaliView(Context context, AttributeSet attrs, int defStyleAttr) {
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

        rashiNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rashiNumberPaint.setColor(0x99FFDCC2);
        rashiNumberPaint.setTextSize(24.0f);
        rashiNumberPaint.setTextAlign(Paint.Align.CENTER);

        planetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        planetPaint.setColor(0xFFFFE082); // Gold
        planetPaint.setTextSize(26.0f);
        planetPaint.setTextAlign(Paint.Align.CENTER);
        planetPaint.setFakeBoldText(true);

        lagnaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lagnaPaint.setColor(0xFFFF7043);
        lagnaPaint.setStyle(Paint.Style.STROKE);
        lagnaPaint.setStrokeWidth(3.0f);

        applyTheme(ThemeManager.getActivePalette(getContext()));
    }

    public void applyTheme(ThemeManager.JapanesePalette palette) {
        if (palette == null) return;
        if (borderPaint != null) borderPaint.setColor(palette.primary);
        if (gridPaint != null) gridPaint.setColor(palette.primary);
        if (rashiNumberPaint != null) rashiNumberPaint.setColor(palette.onSurfaceVariant);
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
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(w, h > 0 ? h : w);
        if (size <= 0) size = 800;
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float pad = 12f;
        float w = getWidth() - pad * 2f;
        float h = getHeight() - pad * 2f;
        float left = pad;
        float top = pad;
        float right = left + w;
        float bottom = top + h;

        // Outer square
        canvas.drawRect(left, top, right, bottom, borderPaint);

        // Grid lines (3x3 grid)
        float col1 = left + w / 3f;
        float col2 = left + 2f * w / 3f;
        float row1 = top + h / 3f;
        float row2 = top + 2f * h / 3f;

        canvas.drawLine(col1, top, col1, bottom, gridPaint);
        canvas.drawLine(col2, top, col2, bottom, gridPaint);
        canvas.drawLine(left, row1, right, row1, gridPaint);
        canvas.drawLine(left, row2, right, row2, gridPaint);

        // Corner diagonals
        canvas.drawLine(left, top, col1, row1, gridPaint);
        canvas.drawLine(right, top, col2, row1, gridPaint);
        canvas.drawLine(left, bottom, col1, row2, gridPaint);
        canvas.drawLine(right, bottom, col2, row2, gridPaint);

        // Center cross diagonals
        canvas.drawLine(col1, row1, col2, row2, gridPaint);
        canvas.drawLine(col2, row1, col1, row2, gridPaint);

        if (panchangData == null) return;

        // East Indian fixed sign positions (centers for text placement)
        // 0: Mesha (Top center)
        // 1: Vrishabha (Top right)
        // 2: Mithuna (Center right)
        // 3: Karka (Bottom right)
        // 4: Simha (Bottom center)
        // 5: Kanya (Bottom left)
        // 6: Tula (Center left)
        // 7: Vrishchika (Top left)
        // 8: Dhanu (Inner top)
        // 9: Makara (Inner right)
        // 10: Kumbha (Inner bottom)
        // 11: Meena (Inner left)
        float[][] signCenters = {
            { (col1 + col2) / 2f, (top + row1) / 2f },             // 1: Mesha
            { (col2 + right) / 2f + 15f, (top + row1) / 2f - 15f },// 2: Vrishabha
            { (col2 + right) / 2f, (row1 + row2) / 2f },          // 3: Mithuna
            { (col2 + right) / 2f + 15f, (row2 + bottom) / 2f + 15f }, // 4: Karka
            { (col1 + col2) / 2f, (row2 + bottom) / 2f },          // 5: Simha
            { (left + col1) / 2f - 15f, (row2 + bottom) / 2f + 15f },  // 6: Kanya
            { (left + col1) / 2f, (row1 + row2) / 2f },           // 7: Tula
            { (left + col1) / 2f - 15f, (top + row1) / 2f - 15f },    // 8: Vrishchika
            { (col1 + col2) / 2f, row1 + 35f },                     // 9: Dhanu
            { col2 - 35f, (row1 + row2) / 2f },                    // 10: Makara
            { (col1 + col2) / 2f, row2 - 35f },                     // 11: Kumbha
            { col1 + 35f, (row1 + row2) / 2f }                     // 12: Meena
        };

        PanchangCalculator.PlanetPosition lagnaPos = null;
        if (panchangData.planets != null) {
            for (PanchangCalculator.PlanetPosition p : panchangData.planets) {
                if ("Lagna".equalsIgnoreCase(p.name)) {
                    lagnaPos = p;
                    break;
                }
            }
        }
        int lagnaSign = (lagnaPos != null && lagnaPos.rashiIndex >= 0) ? lagnaPos.rashiIndex % 12 : 0;

        // Group planets by Rashi
        List<List<String>> planetsInSign = new ArrayList<>();
        for (int i = 0; i < 12; i++) planetsInSign.add(new ArrayList<String>());

        planetsInSign.get(lagnaSign).add("Lg");

        if (panchangData.planets != null) {
            for (PanchangCalculator.PlanetPosition p : panchangData.planets) {
                if ("Lagna".equalsIgnoreCase(p.name)) continue;
                if (p.rashiIndex >= 0 && p.rashiIndex < 12) {
                    String sym = getShortSymbol(p.name);
                    if (p.isRetrograde && !sym.equals("Ra") && !sym.equals("Ke")) {
                        sym += "(R)";
                    }
                    planetsInSign.get(p.rashiIndex).add(sym);
                }
            }
        }

        for (int i = 0; i < 12; i++) {
            float cx = signCenters[i][0];
            float cy = signCenters[i][1];

            // Rashi Roman/Devanagari number
            canvas.drawText(String.valueOf(i + 1), cx, cy - 14f, rashiNumberPaint);

            // Highlight Lagna with red diagonal slash if in this sign
            if (i == lagnaSign) {
                canvas.drawLine(cx - 24f, cy - 24f, cx + 24f, cy + 24f, lagnaPaint);
            }

            // Draw Planets
            List<String> list = planetsInSign.get(i);
            if (!list.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int p = 0; p < list.size(); p++) {
                    if (p > 0) sb.append(" ");
                    sb.append(list.get(p));
                }
                canvas.drawText(sb.toString(), cx, cy + 20f, planetPaint);
            }
        }
    }

    private String getShortSymbol(String name) {
        if ("Sun".equalsIgnoreCase(name)) return "Su";
        if ("Moon".equalsIgnoreCase(name)) return "Mo";
        if ("Mars".equalsIgnoreCase(name)) return "Ma";
        if ("Mercury".equalsIgnoreCase(name)) return "Me";
        if ("Jupiter".equalsIgnoreCase(name)) return "Ju";
        if ("Venus".equalsIgnoreCase(name)) return "Ve";
        if ("Saturn".equalsIgnoreCase(name)) return "Sa";
        if ("Rahu".equalsIgnoreCase(name)) return "Ra";
        if ("Ketu".equalsIgnoreCase(name)) return "Ke";
        if ("Uranus".equalsIgnoreCase(name)) return "Ur";
        if ("Neptune".equalsIgnoreCase(name)) return "Ne";
        if ("Pluto".equalsIgnoreCase(name)) return "Pl";
        return name.length() > 2 ? name.substring(0, 2) : name;
    }
}
