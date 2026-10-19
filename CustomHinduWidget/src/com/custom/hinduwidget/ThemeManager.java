package com.custom.hinduwidget;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages Material Design 3 Dynamic Theming with authentic Japanese color palettes
 * inspired by Sanzo Wada's seminal 1930s work "A Dictionary of Color Combinations"
 * (和の配色辞典).
 */
public class ThemeManager {

    private static final String PREFS_NAME = "m3_theme_prefs";
    private static final String KEY_THEME_INDEX = "active_japanese_theme_index";

    public static class JapanesePalette {
        public final int id;
        public final String nameKanji;
        public final String nameRomaji;
        public final String nameEnglish;
        public final String description;
        public final int primary;
        public final int onPrimary;
        public final int primaryContainer;
        public final int onPrimaryContainer;
        public final int secondary;
        public final int surface;
        public final int surfaceContainer;
        public final int surfaceContainerHigh;
        public final int onSurface;
        public final int onSurfaceVariant;
        public final int swatch1;
        public final int swatch2;

        public JapanesePalette(int id, String kanji, String romaji, String eng, String desc,
                               String p, String onP, String pCont, String onPCont,
                               String sec, String surf, String surfCont, String surfContHigh,
                               String onSurf, String onSurfVar, String sw1, String sw2) {
            this.id = id;
            this.nameKanji = kanji;
            this.nameRomaji = romaji;
            this.nameEnglish = eng;
            this.description = desc;
            this.primary = Color.parseColor(p);
            this.onPrimary = Color.parseColor(onP);
            this.primaryContainer = Color.parseColor(pCont);
            this.onPrimaryContainer = Color.parseColor(onPCont);
            this.secondary = Color.parseColor(sec);
            this.surface = Color.parseColor(surf);
            this.surfaceContainer = Color.parseColor(surfCont);
            this.surfaceContainerHigh = Color.parseColor(surfContHigh);
            this.onSurface = Color.parseColor(onSurf);
            this.onSurfaceVariant = Color.parseColor(onSurfVar);
            this.swatch1 = Color.parseColor(sw1);
            this.swatch2 = Color.parseColor(sw2);
        }

        public String getDisplayName() {
            return nameKanji + " (" + nameRomaji + ") • " + nameEnglish;
        }
    }

    private static final List<JapanesePalette> PALETTES = new ArrayList<>();

    static {
        // 0. 朱色・山吹 (Shu-iro & Yamabuki) - Traditional Sacred Lacquer & Golden Kerria
        PALETTES.add(new JapanesePalette(
            0, "朱色・山吹", "Shu-iro & Yamabuki", "Vermilion & Gold",
            "Classical Vedic temple lacquer, sacred kumkum, and autumn golden kerria.",
            "#BF4A1D", "#FFFFFF", "#6D2400", "#FFDCC2",
            "#FFB74D", "#1C1B18", "#26231E", "#322D26",
            "#EDE0D4", "#C8BEB3", "#BF4A1D", "#FFB74D"
        ));

        // 1. 鶯色・桜 (Uguisu & Sakura) - Bush Warbler Matcha & Spring Cherry Blossom
        PALETTES.add(new JapanesePalette(
            1, "鶯色・桜", "Uguisu & Sakura", "Matcha Olive & Blossom",
            "Subtle spring foliage, contemplative matcha green, and delicate petal rose.",
            "#7CB342", "#1B3700", "#2F5200", "#DCF5B5",
            "#F48FB1", "#171C17", "#212821", "#2A332A",
            "#E4EFE4", "#BECABE", "#7CB342", "#F48FB1"
        ));

        // 2. 瑠璃・月 (Ruri & Tsuki) - Deep Lapis Lazuli Ultramarine & Moonlit Pearl
        PALETTES.add(new JapanesePalette(
            2, "瑠璃・月", "Ruri & Tsuki", "Ultramarine & Moon Pearl",
            "Meditative midnight sky, lapis lazuli jewel, and glowing celestial gold.",
            "#5C6BC0", "#FFFFFF", "#283593", "#E8EAF6",
            "#FFD54F", "#161822", "#1F2231", "#292D3E",
            "#E6E8F2", "#C2C5D6", "#5C6BC0", "#FFD54F"
        ));

        // 3. 黒檀・琥珀 (Kokutan & Kohaku) - Ebony Wood Charcoal & Resinous Amber
        PALETTES.add(new JapanesePalette(
            3, "黒檀・琥珀", "Kokutan & Kohaku", "Ebony & Rich Amber",
            "High-contrast Zen minimalism: polished charcoal wood and warm sunstone amber.",
            "#FFA726", "#4D2C00", "#663C00", "#FFE0B2",
            "#FFCC80", "#141414", "#212121", "#2D2D2D",
            "#F5F0EB", "#D1CCC6", "#FFA726", "#8D6E63"
        ));

        // 4. 浅葱・白菊 (Asagi & Shiragiku) - Morning Mist Teal & White Chrysanthemum
        PALETTES.add(new JapanesePalette(
            4, "浅葱・白菊", "Asagi & Shiragiku", "Morning Teal & Chrysanthemum",
            "Refreshing mineral water, Kyoto riverbank turquoise, and pure chrysanthemum.",
            "#26A69A", "#003731", "#004D40", "#B2DFDB",
            "#80CBC4", "#141C1B", "#1C2725", "#253431",
            "#E0F2F1", "#B2DFDB", "#26A69A", "#80CBC4"
        ));

        // 5. 紫・柿 (Murasaki & Kaki) - Imperial Edo Violet & Autumn Persimmon
        PALETTES.add(new JapanesePalette(
            5, "紫・柿", "Murasaki & Kaki", "Royal Violet & Persimmon",
            "Heian court royal purple harmonized with rich ripe autumn persimmon orange.",
            "#AB47BC", "#FFFFFF", "#6A1B9A", "#F3E5F5",
            "#FF7043", "#1A161E", "#26202C", "#322A3B",
            "#EFE8F2", "#CEC4D3", "#AB47BC", "#FF7043"
        ));

        // 6. 藍・茜 (Ai & Akane) - Japanese Indigo & Madder Red
        PALETTES.add(new JapanesePalette(
            6, "藍・茜", "Ai & Akane", "Indigo & Madder Red",
            "Traditional samurai indigo cloth paired with rich crimson-madder herb dye.",
            "#3F51B5", "#FFFFFF", "#1A237E", "#C5CAE9",
            "#E53935", "#131620", "#1C2130", "#272E42",
            "#E8EAF6", "#C5CAE9", "#3F51B5", "#E53935"
        ));

        // 7. 若竹・山葡萄 (Wakatake & Yamabudou) - Young Bamboo & Wild Mountain Grape
        PALETTES.add(new JapanesePalette(
            7, "若竹・山葡萄", "Wakatake & Yamabudou", "Young Bamboo & Wild Grape",
            "Fresh green bamboo grove contrasted against deep mountain wild grapes.",
            "#00897B", "#FFFFFF", "#004D40", "#B2DFDB",
            "#8E24AA", "#121A18", "#1A2624", "#233330",
            "#E0F2F1", "#B2DFDB", "#00897B", "#8E24AA"
        ));
    }

    public static List<JapanesePalette> getAllPalettes() {
        return PALETTES;
    }

    public static JapanesePalette getPalette(int index) {
        if (index < 0 || index >= PALETTES.size()) {
            return PALETTES.get(0);
        }
        return PALETTES.get(index);
    }

    public static int getSelectedThemeIndex(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sp.getInt(KEY_THEME_INDEX, 0);
    }

    public static void setSelectedThemeIndex(Context context, int index) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().putInt(KEY_THEME_INDEX, index).apply();
    }

    public static JapanesePalette getActivePalette(Context context) {
        return getPalette(getSelectedThemeIndex(context));
    }

    public static int getActivePrimary(Context context) {
        return getActivePalette(context).primary;
    }

    public static int getActiveSecondary(Context context) {
        return getActivePalette(context).secondary;
    }

    public static int getActivePrimaryContainer(Context context) {
        return getActivePalette(context).primaryContainer;
    }

    public static android.graphics.drawable.GradientDrawable createCardHighlight(int strokeColor, int fillColor, float density) {
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        gd.setCornerRadius(16f * density);
        gd.setColor(fillColor);
        gd.setStroke((int)(2f * density), strokeColor);
        return gd;
    }

    public static android.graphics.drawable.GradientDrawable createPillIndicator(int fillColor, float density) {
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        gd.setCornerRadius(9999f * density);
        gd.setColor(fillColor);
        return gd;
    }

    public static android.graphics.drawable.GradientDrawable createChipDrawable(int strokeColor, int fillColor, float density) {
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        gd.setCornerRadius(12f * density);
        gd.setColor(fillColor);
        gd.setStroke((int)(1.5f * density), strokeColor);
        return gd;
    }

    public static android.graphics.drawable.GradientDrawable createBadge(int fillColor, float density) {
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        gd.setCornerRadius(6f * density);
        gd.setColor(fillColor);
        return gd;
    }

    public static void applyThemeToActivity(Activity activity) {
        JapanesePalette p = getActivePalette(activity);
        if (activity.getWindow() != null) {
            activity.getWindow().setStatusBarColor(p.surface);
            activity.getWindow().setNavigationBarColor(p.surfaceContainer);
        }
    }
}
