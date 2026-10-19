package com.custom.hinduwidget;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Manages user's personal Tithis (My Tithi / मेरी तिथि) - Birthdays,
 * Anniversaries, Shraddhas, and custom religious observances.
 * Persists to SharedPreferences as JSON.
 */
public class MyTithiManager {

    private static final String PREF_NAME = "my_tithi_prefs";
    private static final String KEY_TITHIS = "saved_tithis_json";

    public static class TithiEvent {
        public String id;
        public String title;
        public String category; // "Birthday", "Anniversary", "Shraddha", "Vrat"
        public int monthIndex;   // 0 = Chaitra .. 11 = Phalguna
        public String paksha;    // "Shukla" or "Krishna"
        public int tithiNum;     // 1 to 15 (15 = Purnima/Amavasya)
        public String note;

        public TithiEvent(String id, String title, String category, int monthIndex, String paksha, int tithiNum, String note) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.monthIndex = monthIndex;
            this.paksha = paksha;
            this.tithiNum = tithiNum;
            this.note = note;
        }

        public String getTithiDescription() {
            String mName = PanchangCalculator.HINDU_MONTHS_AMANTA[monthIndex % 12];
            String tName = PanchangCalculator.TITHI_NAMES[(tithiNum - 1) % 15];
            if (tithiNum == 15) {
                tName = "Shukla".equalsIgnoreCase(paksha) ? "Purnima" : "Amavasya";
            }
            return mName + " " + paksha + " " + tName;
        }

        public JSONObject toJson() {
            try {
                JSONObject obj = new JSONObject();
                obj.put("id", id);
                obj.put("title", title);
                obj.put("category", category);
                obj.put("monthIndex", monthIndex);
                obj.put("paksha", paksha);
                obj.put("tithiNum", tithiNum);
                obj.put("note", note);
                return obj;
            } catch (Exception e) {
                return new JSONObject();
            }
        }

        public static TithiEvent fromJson(JSONObject obj) {
            try {
                return new TithiEvent(
                    obj.getString("id"),
                    obj.getString("title"),
                    obj.optString("category", "Birthday"),
                    obj.optInt("monthIndex", 0),
                    obj.optString("paksha", "Shukla"),
                    obj.optInt("tithiNum", 1),
                    obj.optString("note", "")
                );
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static List<TithiEvent> loadEvents(Context context) {
        List<TithiEvent> list = new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY_TITHIS, null);

        if (raw == null || raw.trim().isEmpty()) {
            // Seed default curated events
            list.add(new TithiEvent("1", "Shri Rama Navami", "Birthday", 0, "Shukla", 9, "Chaitra Shukla Navami"));
            list.add(new TithiEvent("2", "Ganesh Chaturthi", "Vrat", 5, "Shukla", 4, "Bhadrapada Shukla Chaturthi"));
            list.add(new TithiEvent("3", "Krishna Janmashtami", "Birthday", 4, "Krishna", 8, "Shravana Krishna Ashtami"));
            list.add(new TithiEvent("4", "Mahalaya Pitru Shraddha", "Shraddha", 6, "Krishna", 15, "Ashwina Krishna Amavasya"));
            list.add(new TithiEvent("5", "Hanuman Jayanti", "Birthday", 0, "Shukla", 15, "Chaitra Purnima"));
            saveEvents(context, list);
            return list;
        }

        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                TithiEvent ev = TithiEvent.fromJson(arr.getJSONObject(i));
                if (ev != null) list.add(ev);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void saveEvents(Context context, List<TithiEvent> list) {
        try {
            JSONArray arr = new JSONArray();
            for (TithiEvent ev : list) {
                arr.put(ev.toJson());
            }
            SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            sp.edit().putString(KEY_TITHIS, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addEvent(Context context, TithiEvent event) {
        List<TithiEvent> list = loadEvents(context);
        list.add(event);
        saveEvents(context, list);
    }

    public static void deleteEvent(Context context, String id) {
        List<TithiEvent> list = loadEvents(context);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id.equals(id)) {
                list.remove(i);
                break;
            }
        }
        saveEvents(context, list);
    }
}
