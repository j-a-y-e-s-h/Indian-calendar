package com.custom.hinduwidget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Material 3 Vedic 8-Directions Vastu Compass View.
 * Renders the classical Vedic compass dial with smooth spring damping physics.
 * Features:
 * 1. Dual-Sensor Fallback: TYPE_ROTATION_VECTOR + Fallback to ACCELEROMETER & MAGNETIC_FIELD.
 * 2. True North Declination compensation.
 * 3. Interactive Touch & Drag: manual inspection of all 8 sacred Vedic Dikpalas.
 * 4. Spring damping interpolation for silky smooth rotation without sensor jitter.
 */
public class VedicCompassView extends View implements SensorEventListener {

    public interface OnHeadingChangeListener {
        void onHeadingChanged(float degrees, VedicDirection direction, boolean isManual);
    }

    public static class VedicDirection {
        public final String nameEnglish;
        public final String nameHindi;
        public final String dikpala;
        public final String grahaLord;
        public final String element;
        public final String vastuTip;
        public final int badgeColor;

        public VedicDirection(String eng, String hi, String dikpala, String graha, String element, String vastu, int color) {
            this.nameEnglish = eng;
            this.nameHindi = hi;
            this.dikpala = dikpala;
            this.grahaLord = graha;
            this.element = element;
            this.vastuTip = vastu;
            this.badgeColor = color;
        }
    }

    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private Sensor accelerometerSensor;
    private Sensor magneticSensor;

    private float[] gravityValues = new float[3];
    private float[] magneticValues = new float[3];
    private boolean hasGravity = false;
    private boolean hasMagnetic = false;
    private boolean usingRotationVector = false;

    // Heading angles
    private float currentAzimuth = 0f;
    private float targetAzimuth = 0f;
    private float trueNorthDeclination = 0f; // in degrees

    // Manual Touch Interaction
    private boolean isManualMode = false;
    private float touchDownX = 0f;
    private float touchDownY = 0f;

    // Paints & Graphics
    private Bitmap dialBitmap;
    private Paint paint;
    private Paint needlePaint;
    private Paint needleAccentPaint;
    private Paint circlePaint;
    private Paint textHindiPaint;
    private Paint textEngPaint;
    private Paint petalMajorPaint;
    private Paint petalMinorPaint;
    private Paint petalShadePaint;
    private Paint tickPaint;
    private Paint innerCirclePaint;
    private Paint manualBadgePaint;
    private Paint manualBadgeTextPaint;

    private OnHeadingChangeListener headingListener;

    public VedicCompassView(Context context) {
        super(context);
        init(context);
    }

    public VedicCompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VedicCompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setColor(Color.parseColor("#BF4A1D")); // M3 Vermilion Terracotta
        needlePaint.setStyle(Paint.Style.FILL);

        needleAccentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needleAccentPaint.setColor(Color.parseColor("#FFD54F")); // Gold accent
        needleAccentPaint.setStyle(Paint.Style.FILL);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor("#442E18"));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(3f);

        textHindiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textHindiPaint.setColor(Color.parseColor("#FFDCC2"));
        textHindiPaint.setTextAlign(Paint.Align.CENTER);
        textHindiPaint.setTextSize(26f);
        textHindiPaint.setFakeBoldText(true);

        textEngPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textEngPaint.setColor(Color.parseColor("#BF4A1D"));
        textEngPaint.setTextAlign(Paint.Align.CENTER);
        textEngPaint.setTextSize(18f);
        textEngPaint.setFakeBoldText(true);

        petalMajorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        petalMajorPaint.setColor(Color.parseColor("#BF4A1D"));
        petalMajorPaint.setStyle(Paint.Style.FILL);

        petalMinorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        petalMinorPaint.setColor(Color.parseColor("#E68A00"));
        petalMinorPaint.setStyle(Paint.Style.FILL);

        petalShadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        petalShadePaint.setColor(Color.parseColor("#331E12"));
        petalShadePaint.setStyle(Paint.Style.FILL);

        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(Color.parseColor("#664E3A"));
        tickPaint.setStrokeWidth(2f);

        innerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerCirclePaint.setColor(Color.parseColor("#26170E"));
        innerCirclePaint.setStyle(Paint.Style.FILL);

        manualBadgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        manualBadgePaint.setColor(Color.parseColor("#991F2430"));
        manualBadgePaint.setStyle(Paint.Style.FILL);

        manualBadgeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        manualBadgeTextPaint.setColor(Color.parseColor("#FFD54F"));
        manualBadgeTextPaint.setTextAlign(Paint.Align.CENTER);
        manualBadgeTextPaint.setTextSize(20f);
        manualBadgeTextPaint.setFakeBoldText(true);

        try {
            dialBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dial);
        } catch (Exception e) {
            e.printStackTrace();
        }

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            if (rotationSensor != null) {
                usingRotationVector = true;
            } else {
                usingRotationVector = false;
                accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            }
        }

        applyTheme(ThemeManager.getActivePalette(context));
    }

    public void applyTheme(ThemeManager.JapanesePalette palette) {
        if (palette == null) return;
        if (needlePaint != null) needlePaint.setColor(palette.primary);
        if (needleAccentPaint != null) needleAccentPaint.setColor(palette.swatch2);
        if (textEngPaint != null) textEngPaint.setColor(palette.primary);
        if (petalMajorPaint != null) petalMajorPaint.setColor(palette.primary);
        if (petalMinorPaint != null) petalMinorPaint.setColor(palette.swatch2);
        if (circlePaint != null) circlePaint.setColor(palette.surfaceContainerHigh);
        if (textHindiPaint != null) textHindiPaint.setColor(palette.onPrimaryContainer);
        if (manualBadgeTextPaint != null) manualBadgeTextPaint.setColor(palette.swatch2);
        invalidate();
    }

    public void setOnHeadingChangeListener(OnHeadingChangeListener listener) {
        this.headingListener = listener;
        notifyListener();
    }

    public void setDeclinationFromLocation(double latitude, double longitude, double altitudeMeters) {
        try {
            GeomagneticField geoField = new GeomagneticField(
                (float) latitude,
                (float) longitude,
                (float) altitudeMeters,
                System.currentTimeMillis()
            );
            this.trueNorthDeclination = geoField.getDeclination();
        } catch (Exception e) {
            this.trueNorthDeclination = 0.5f; // Reasonable default for India
        }
    }

    public void startListening() {
        if (sensorManager != null) {
            if (usingRotationVector && rotationSensor != null) {
                sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI);
            } else {
                if (accelerometerSensor != null) {
                    sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
                }
                if (magneticSensor != null) {
                    sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_UI);
                }
            }
        }
    }

    public void stopListening() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    public void setHeading(float degrees) {
        targetAzimuth = (degrees % 360 + 360) % 360;
        currentAzimuth = targetAzimuth;
        notifyListener();
        invalidate();
    }

    public void resetToLiveSensor() {
        isManualMode = false;
        notifyListener();
        invalidate();
    }

    public boolean isManualMode() {
        return isManualMode;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return super.onTouchEvent(event);

        float cx = w / 2f;
        float cy = h / 2f;
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDownX = x;
                touchDownY = y;
                // If tapped near center while in manual mode, reset to live sensor
                float distFromCenter = (float) Math.hypot(x - cx, y - cy);
                if (isManualMode && distFromCenter < 50f) {
                    resetToLiveSensor();
                    return true;
                }
                isManualMode = true;
                updateManualAngle(cx, cy, x, y);
                return true;

            case MotionEvent.ACTION_MOVE:
                isManualMode = true;
                updateManualAngle(cx, cy, x, y);
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Remain at the inspected direction until tapped or reset
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void updateManualAngle(float cx, float cy, float touchX, float touchY) {
        double rad = Math.atan2(touchY - cy, touchX - cx);
        float deg = (float) Math.toDegrees(rad) + 90f; // 0 degrees at top (12 o'clock)
        deg = (deg % 360 + 360) % 360;

        // Smooth spring adjustment
        float diff = deg - currentAzimuth;
        if (diff > 180) diff -= 360;
        if (diff < -180) diff += 360;

        currentAzimuth += diff * 0.45f;
        currentAzimuth = (currentAzimuth % 360 + 360) % 360;
        targetAzimuth = currentAzimuth;

        notifyListener();
        invalidate();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isManualMode) {
            // User is manually inspecting directions; do not override with sensor
            return;
        }

        float azimuth = 0f;
        boolean hasNewAzimuth = false;

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientation);
            azimuth = (float) Math.toDegrees(orientation[0]);
            hasNewAzimuth = true;
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravityValues, 0, 3);
            hasGravity = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magneticValues, 0, 3);
            hasMagnetic = true;
        }

        if (!hasNewAzimuth && hasGravity && hasMagnetic) {
            float[] rMatrix = new float[9];
            float[] iMatrix = new float[9];
            boolean success = SensorManager.getRotationMatrix(rMatrix, iMatrix, gravityValues, magneticValues);
            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(rMatrix, orientation);
                azimuth = (float) Math.toDegrees(orientation[0]);
                hasNewAzimuth = true;
            }
        }

        if (hasNewAzimuth) {
            // Compensate for true north declination
            azimuth += trueNorthDeclination;
            azimuth = (azimuth % 360 + 360) % 360;

            // Spring Damping Filter (Exponential Smoothing with inertia)
            float diff = azimuth - targetAzimuth;
            if (diff > 180) diff -= 360;
            if (diff < -180) diff += 360;

            // 0.22f damping factor provides smooth, jitter-free needle physics
            targetAzimuth += diff * 0.22f;
            targetAzimuth = (targetAzimuth % 360 + 360) % 360;

            currentAzimuth = targetAzimuth;
            notifyListener();
            invalidate();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void notifyListener() {
        if (headingListener != null) {
            VedicDirection dir = getVedicDirection(currentAzimuth);
            headingListener.onHeadingChanged(currentAzimuth, dir, isManualMode);
        }
    }

    public static VedicDirection getVedicDirection(float deg) {
        float d = (deg % 360 + 360) % 360;
        if (d >= 337.5f || d < 22.5f) {
            return new VedicDirection("North", "उत्तर (Uttar)", "Kubera (कुबेर)", "Budha (Mercury)", "Water",
                "Prosperity & Wealth. Ideal for Entrance, Living room, Treasury, Balcony.", Color.parseColor("#2E7D32"));
        } else if (d < 67.5f) {
            return new VedicDirection("North-East", "ईशान (Ishanya)", "Ishana / Shiva (ईशान)", "Guru (Jupiter)", "Water/Akasha",
                "Divine Grace & Wisdom. Ideal for Mandir, Meditation, Water bodies. Keep light and pure.", Color.parseColor("#E68A00"));
        } else if (d < 112.5f) {
            return new VedicDirection("East", "पूर्व (Purva)", "Indra (इन्द्र)", "Surya (Sun)", "Fire/Ether",
                "Vitality & Clarity. Ideal for Main entrance, Puja, Children's study room.", Color.parseColor("#8A3816"));
        } else if (d < 157.5f) {
            return new VedicDirection("South-East", "आग्नेय (Agneya)", "Agni (अग्नि)", "Shukra (Venus)", "Fire",
                "Energy & Metabolism. Ideal for Kitchen, Fireplace, Inverter/Electrical appliances.", Color.parseColor("#BF4A1D"));
        } else if (d < 202.5f) {
            return new VedicDirection("South", "दक्षिण (Dakshin)", "Yama (यम)", "Mangal (Mars)", "Earth/Fire",
                "Strength & Discipline. Ideal for Heavy storage, Bedroom, High boundary walls.", Color.parseColor("#C62828"));
        } else if (d < 247.5f) {
            return new VedicDirection("South-West", "नैऋत्य (Nairitya)", "Nirriti (निर्ऋति)", "Rahu (राहु)", "Earth",
                "Stability & Leadership. Best for Master Bedroom, Wardrobe, Heavy structures.", Color.parseColor("#5D4037"));
        } else if (d < 292.5f) {
            return new VedicDirection("West", "पश्चिम (Paschim)", "Varuna (वरुण)", "Shani (Saturn)", "Air/Water",
                "Prosperity & Reflection. Ideal for Dining room, Study room, Overhead tank.", Color.parseColor("#1565C0"));
        } else {
            return new VedicDirection("North-West", "वायव्य (Vayavya)", "Vayu (वायु)", "Chandra (Moon)", "Air",
                "Movement & Relationships. Ideal for Guest room, Unmarried daughters' room, Finished goods.", Color.parseColor("#00838F"));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float cx = w / 2f;
        float cy = h / 2f;
        float radius = Math.min(cx, cy) - 16f;
        if (radius <= 10f) return;

        // Draw compass background circle
        canvas.drawCircle(cx, cy, radius, innerCirclePaint);
        canvas.drawCircle(cx, cy, radius, circlePaint);
        canvas.drawCircle(cx, cy, radius * 0.72f, circlePaint);

        // ROTATING VEDIC ROSE
        canvas.save();
        canvas.rotate(-currentAzimuth, cx, cy);

        // 1. Degree ticks around rim
        for (int deg = 0; deg < 360; deg += 5) {
            float angleRad = (float) Math.toRadians(deg - 90);
            float cos = (float) Math.cos(angleRad);
            float sin = (float) Math.sin(angleRad);

            float tickLen = (deg % 45 == 0) ? 18f : (deg % 15 == 0 ? 12f : 6f);
            tickPaint.setStrokeWidth(deg % 45 == 0 ? 3.5f : (deg % 15 == 0 ? 2f : 1.2f));
            tickPaint.setColor(deg % 45 == 0 ? Color.parseColor("#FFD54F") : Color.parseColor("#885A36"));

            float startX = cx + (radius - tickLen) * cos;
            float startY = cy + (radius - tickLen) * sin;
            float stopX = cx + radius * cos;
            float stopY = cy + radius * sin;

            canvas.drawLine(startX, startY, stopX, stopY, tickPaint);
        }

        // 2. Central 8-pointed Vedic Mandala / Star
        float majorR = radius * 0.48f;
        float minorR = radius * 0.36f;
        float innerR = radius * 0.12f;

        // Draw 4 Minor Petals (NE, SE, SW, NW)
        for (int i = 0; i < 4; i++) {
            float angle = 45f + i * 90f;
            canvas.save();
            canvas.rotate(angle, cx, cy);

            Path pathL = new Path();
            pathL.moveTo(cx, cy);
            pathL.lineTo(cx, cy - minorR);
            pathL.lineTo(cx - innerR, cy);
            pathL.close();
            canvas.drawPath(pathL, petalMinorPaint);

            Path pathR = new Path();
            pathR.moveTo(cx, cy);
            pathR.lineTo(cx, cy - minorR);
            pathR.lineTo(cx + innerR, cy);
            pathR.close();
            canvas.drawPath(pathR, petalShadePaint);

            canvas.restore();
        }

        // Draw 4 Major Petals (N, E, S, W)
        for (int i = 0; i < 4; i++) {
            float angle = i * 90f;
            canvas.save();
            canvas.rotate(angle, cx, cy);

            Path pathL = new Path();
            pathL.moveTo(cx, cy);
            pathL.lineTo(cx, cy - majorR);
            pathL.lineTo(cx - innerR, cy);
            pathL.close();
            if (i == 0) {
                // North petal is special Auspicious Gold / Green
                needleAccentPaint.setColor(Color.parseColor("#4CAF50"));
                canvas.drawPath(pathL, needleAccentPaint);
            } else {
                canvas.drawPath(pathL, petalMajorPaint);
            }

            Path pathR = new Path();
            pathR.moveTo(cx, cy);
            pathR.lineTo(cx, cy - majorR);
            pathR.lineTo(cx + innerR, cy);
            pathR.close();
            canvas.drawPath(pathR, petalShadePaint);

            canvas.restore();
        }

        // 3. 8 Sacred Vedic Direction Labels
        String[] hiLabels = {"उत्तर", "ईशान", "पूर्व", "आग्नेय", "दक्षिण", "नैऋत्य", "पश्चिम", "वायव्य"};
        String[] enLabels = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int[] labelColors = {
            Color.parseColor("#4CAF50"), // North Kubera (Green)
            Color.parseColor("#FFA000"), // NE Ishana (Saffron)
            Color.parseColor("#FF5722"), // East Indra (Vermilion)
            Color.parseColor("#E64A19"), // SE Agni (Deep Orange)
            Color.parseColor("#EF5350"), // South Yama (Crimson)
            Color.parseColor("#A1887F"), // SW Nirriti (Earthy Brown)
            Color.parseColor("#42A5F5"), // West Varuna (Sky Blue)
            Color.parseColor("#26A69A")  // NW Vayu (Teal)
        };

        float textRadius = radius * 0.82f;
        for (int i = 0; i < 8; i++) {
            float angle = i * 45f;
            canvas.save();
            canvas.rotate(angle, cx, cy);

            textHindiPaint.setColor(labelColors[i]);
            textEngPaint.setColor(labelColors[i]);

            canvas.drawText(hiLabels[i], cx, cy - textRadius + 10f, textHindiPaint);
            canvas.drawText(enLabels[i], cx, cy - textRadius + 28f, textEngPaint);

            canvas.restore();
        }

        canvas.restore(); // End rotating compass rose

        // FIXED TOP NEEDLE INDICATOR (Points to active reading)
        float needleLen = radius * 0.20f;
        Path needle = new Path();
        needle.moveTo(cx, cy - radius + 2f);
        needle.lineTo(cx - 10f, cy - radius + needleLen);
        needle.lineTo(cx + 10f, cy - radius + needleLen);
        needle.close();
        needlePaint.setColor(Color.parseColor("#BF4A1D"));
        canvas.drawPath(needle, needlePaint);

        Path innerNeedle = new Path();
        innerNeedle.moveTo(cx, cy - radius + 5f);
        innerNeedle.lineTo(cx - 4f, cy - radius + needleLen - 4f);
        innerNeedle.lineTo(cx + 4f, cy - radius + needleLen - 4f);
        innerNeedle.close();
        needleAccentPaint.setColor(Color.parseColor("#FFD54F"));
        canvas.drawPath(innerNeedle, needleAccentPaint);

        // Center hub circle
        canvas.drawCircle(cx, cy, 14f, petalMajorPaint);
        canvas.drawCircle(cx, cy, 7f, needleAccentPaint);

        // Manual Drag hint badge if active
        if (isManualMode) {
            float bw = 120f;
            float bh = 28f;
            RectF badgeRect = new RectF(cx - bw, cy + radius * 0.40f - bh, cx + bw, cy + radius * 0.40f + bh);
            canvas.drawRoundRect(badgeRect, 14f, 14f, manualBadgePaint);
            canvas.drawText("Manual Touch (Tap Center to Reset)", cx, cy + radius * 0.40f + 6f, manualBadgeTextPaint);
        }
    }
}
