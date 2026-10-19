package com.custom.hinduwidget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class SouthIndianKundaliView extends View {

    private Paint borderPaint;
    private Paint gridPaint;
    private Paint rashiLabelPaint;
    private Paint planetPaint;
    private Paint lagnaPaint;
    private Paint centerPaint;
    private Paint centerSubPaint;

    private PanchangCalculator.DayPanchang panchangData;

    // South Indian Chart: Fixed Rashi Mapping to (row, col) in a 4x4 grid
    // Row 0: Meena(12), Mesha(1), Vrishabha(2), Mithuna(3)
    // Row 1: Kumbha(11), [Center], [Center], Karka(4)
    // Row 2: Makara(10), [Center], [Center], Simha(5)
    // Row 3: Dhanu(9), Vrischika(8), Tula(7), Kanya(6)
    private static final int[][] RASHI_GRID = {
        { 12, 1, 2, 3 },
        { 11, 0, 0, 4 },
        { 10, 0, 0, 5 },
        { 9, 8, 7, 6 }
    };

    private static final String[] RASHI_SHORT = {
        "Mesha", "Vrish", "Mithu", "Karka",
        "Simha", "Kanya", "Tula", "Vrisch",
        "Dhanu", "Makar", "Kumbh", "Meena"
    };

    public SouthIndianKundaliView(Context context) {
        super(context);
        init();
    }

    public SouthIndianKundaliView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SouthIndianKundaliView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(0xFFFFB77C); // Material 3 Saffron Primary
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4.5f);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(0xCCFFB77C);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2.5f);

        rashiLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rashiLabelPaint.setColor(0x99FFDCC2); // Subtle On-Surface-Variant
        rashiLabelPaint.setTextSize(22.0f);
        rashiLabelPaint.setTextAlign(Paint.Align.LEFT);

        planetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        planetPaint.setColor(0xFFFFE082); // Gold
        planetPaint.setTextSize(26.0f);
        planetPaint.setTextAlign(Paint.Align.CENTER);
        planetPaint.setFakeBoldText(true);

        lagnaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lagnaPaint.setColor(0xFFFF7043); // Coral Lagna
        lagnaPaint.setTextSize(28.0f);
        lagnaPaint.setTextAlign(Paint.Align.CENTER);
        lagnaPaint.setFakeBoldText(true);

        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(0xFFFFDCC2);
        centerPaint.setTextSize(34.0f);
        centerPaint.setTextAlign(Paint.Align.CENTER);
        centerPaint.setFakeBoldText(true);

        centerSubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerSubPaint.setColor(0xFFFFB77C);
        centerSubPaint.setTextSize(22.0f);
        centerSubPaint.setTextAlign(Paint.Align.CENTER);

        applyTheme(ThemeManager.getActivePalette(getContext()));
    }

    public void applyTheme(ThemeManager.JapanesePalette palette) {
        if (palette == null) return;
        if (borderPaint != null) borderPaint.setColor(palette.primary);
        if (gridPaint != null) gridPaint.setColor(palette.primary);
        if (rashiLabelPaint != null) rashiLabelPaint.setColor(palette.onSurfaceVariant);
        if (planetPaint != null) planetPaint.setColor(palette.swatch2);
        if (lagnaPaint != null) lagnaPaint.setColor(palette.primary);
        if (centerPaint != null) centerPaint.setColor(palette.onSurface);
        if (centerSubPaint != null) centerSubPaint.setColor(palette.primary);
        invalidate();
    }

    public void setPanchangData(PanchangCalculator.DayPanchang data) {
        this.panchangData = data;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;

        float pad = 16.0f;
        float chartW = w - 2 * pad;
        float chartH = h - 2 * pad;
        float cellSize = chartW / 4.0f;

        // Outer boundary box
        canvas.drawRect(pad, pad, pad + chartW, pad + chartH, borderPaint);

        // Draw outer 4x4 grid lines (excluding inner 2x2)
        // Horizontal lines
        for (int r = 1; r < 4; r++) {
            float y = pad + r * cellSize;
            if (r == 1 || r == 3) {
                canvas.drawLine(pad, y, pad + chartW, y, gridPaint);
            } else {
                // r == 2: line only in left and right col
                canvas.drawLine(pad, y, pad + cellSize, y, gridPaint);
                canvas.drawLine(pad + 3 * cellSize, y, pad + chartW, y, gridPaint);
            }
        }

        // Vertical lines
        for (int c = 1; c < 4; c++) {
            float x = pad + c * cellSize;
            if (c == 1 || c == 3) {
                canvas.drawLine(x, pad, x, pad + chartH, gridPaint);
            } else {
                // c == 2: line only in top and bottom row
                canvas.drawLine(x, pad, x, pad + cellSize, gridPaint);
                canvas.drawLine(x, pad + 3 * cellSize, x, pad + chartH, gridPaint);
            }
        }

        // Center box rectangle outline
        canvas.drawRect(pad + cellSize, pad + cellSize, pad + 3 * cellSize, pad + 3 * cellSize, borderPaint);

        // Center text
        float centerX = pad + 2 * cellSize;
        float centerY = pad + 2 * cellSize;
        canvas.drawText("ॐ", centerX, centerY - 28, centerPaint);
        canvas.drawText("South Indian Chart", centerX, centerY + 16, centerSubPaint);
        PanchangCalculator.PlanetPosition lagnaPos = null;
        if (panchangData != null && panchangData.planets != null && !panchangData.planets.isEmpty()) {
            for (PanchangCalculator.PlanetPosition p : panchangData.planets) {
                if ("Lagna".equalsIgnoreCase(p.name)) {
                    lagnaPos = p;
                    break;
                }
            }
        }
        if (lagnaPos != null) {
            canvas.drawText("Lagna: " + lagnaPos.rashiName, centerX, centerY + 48, rashiLabelPaint);
        }

        if (panchangData == null) return;

        int lagnaRashiIdx = (lagnaPos != null) ? lagnaPos.rashiIndex : -1;

        // Group planets by rashi (0..11)
        List<List<String>> planetsInRashi = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            planetsInRashi.add(new ArrayList<String>());
        }

        if (panchangData.planets != null) {
            for (PanchangCalculator.PlanetPosition p : panchangData.planets) {
                if (p.rashiIndex >= 0 && p.rashiIndex < 12) {
                    String label = getPlanetShortName(p.sanskritName);
                    if (p.isRetrograde && !label.equals("Ra") && !label.equals("Ke")) {
                        label += "(R)";
                    }
                    planetsInRashi.get(p.rashiIndex).add(label);
                }
            }
        }

        // Draw Rashi names and contents in each cell
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                int rashiNum = RASHI_GRID[r][c];
                if (rashiNum == 0) continue; // Center 2x2

                int rashiIdx = rashiNum - 1;
                float cellX = pad + c * cellSize;
                float cellY = pad + r * cellSize;

                // Rashi label top-left
                canvas.drawText(RASHI_SHORT[rashiIdx], cellX + 10, cellY + 28, rashiLabelPaint);

                // Check if Lagna is in this Rashi
                boolean isLagna = (rashiIdx == lagnaRashiIdx);
                if (isLagna) {
                    // Draw red corner slash
                    canvas.drawLine(cellX, cellY + cellSize, cellX + cellSize, cellY, lagnaPaint);
                }

                // Planets in this cell
                List<String> planets = planetsInRashi.get(rashiIdx);
                List<String> itemsToDraw = new ArrayList<>();
                if (isLagna) {
                    itemsToDraw.add("Lagna");
                }
                itemsToDraw.addAll(planets);

                float planetStartY = cellY + 54;
                float lineHeight = 28.0f;
                float cellCenterX = cellX + cellSize / 2.0f;

                for (int i = 0; i < itemsToDraw.size(); i++) {
                    String item = itemsToDraw.get(i);
                    Paint p = item.equals("Lagna") ? lagnaPaint : planetPaint;
                    canvas.drawText(item, cellCenterX, planetStartY + i * lineHeight, p);
                }
            }
        }
    }

    private String getPlanetShortName(String sanskritName) {
        if (sanskritName == null) return "";
        if (sanskritName.startsWith("Surya")) return "Su";
        if (sanskritName.startsWith("Chan")) return "Ch";
        if (sanskritName.startsWith("Mang")) return "Ma";
        if (sanskritName.startsWith("Budh")) return "Bu";
        if (sanskritName.startsWith("Guru")) return "Gu";
        if (sanskritName.startsWith("Shuk")) return "Sk";
        if (sanskritName.startsWith("Shan")) return "Sa";
        if (sanskritName.startsWith("Rahu")) return "Ra";
        if (sanskritName.startsWith("Ketu")) return "Ke";
        return sanskritName.substring(0, Math.min(2, sanskritName.length()));
    }
}
