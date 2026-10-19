package com.custom.hinduwidget;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CityManager {

    public static class City {
        public String name;
        public double latitude;
        public double longitude;
        public String countryCode;
        public String timezone;
        public double timezoneOffsetHours;
        public String country;

        public City(String name, double latitude, double longitude, String timezone, String country) {
            this(name, latitude, longitude, "IN", timezone, 5.5, country);
        }

        public City(String name, double latitude, double longitude, String countryCode, String timezone, double timezoneOffsetHours, String country) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.countryCode = countryCode;
            this.timezone = timezone;
            this.timezoneOffsetHours = timezoneOffsetHours;
            this.country = country;
        }

        @Override
        public String toString() {
            if (country != null && !country.isEmpty() && !country.equalsIgnoreCase("India")) {
                return name + ", " + country;
            }
            return name + ", India";
        }
    }

    private static final List<City> CITIES = new ArrayList<>();
    private static boolean isLoaded = false;

    static {
        // Core curated Vedic & reference cities (guaranteed immediately available)
        CITIES.add(new City("Ujjain (Avanti Prime Meridian)", 23.18, 75.77, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Varanasi (Kashi)", 25.32, 82.97, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Pune", 18.5204, 73.8567, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Mumbai", 19.0760, 72.8777, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("New Delhi", 28.6139, 77.2090, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Bengaluru", 12.9716, 77.5946, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Kolkata", 22.5726, 88.3639, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Chennai", 13.0827, 80.2707, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Hyderabad", 17.3850, 78.4867, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Ahmedabad", 23.0225, 72.5714, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Jaipur", 26.9124, 75.7873, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Lucknow", 26.8467, 80.9462, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Patna", 25.5941, 85.1376, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Haridwar", 29.9457, 78.1642, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Ayodhya", 26.7922, 82.1998, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Mathura", 27.4924, 77.6737, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Puri", 19.8135, 85.8312, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Tirupati", 13.6288, 79.4192, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Madurai", 9.9252, 78.1198, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Rameshwaram", 9.2876, 79.3129, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Dwarka", 22.2442, 68.9685, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Nagpur", 21.1458, 79.0882, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Indore", 22.7196, 75.8577, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Bhopal", 23.2599, 77.4126, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Kanpur", 26.4499, 80.3319, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Chandigarh", 30.7333, 76.7794, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Amritsar", 31.6340, 74.8723, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Guwahati", 26.1445, 91.7362, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Nashik (Trimbak)", 19.9975, 73.7898, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Surat", 21.1702, 72.8311, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Vadodara", 22.3072, 73.1812, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Coimbatore", 11.0168, 76.9558, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Kochi", 9.9312, 76.2673, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Thiruvananthapuram", 8.5241, 76.9366, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Visakhapatnam", 17.6868, 83.2185, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Bhubaneswar", 20.2961, 85.8245, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Srinagar", 34.0837, 74.7973, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Shimla", 31.1048, 77.1734, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Dehradun", 30.3165, 78.0322, "IN", "Asia/Kolkata", 5.5, "India"));
        CITIES.add(new City("Kathmandu", 27.7172, 85.3240, "NP", "Asia/Kathmandu", 5.75, "Nepal"));
        CITIES.add(new City("London", 51.5074, -0.1278, "GB", "Europe/London", 0.0, "United Kingdom"));
        CITIES.add(new City("New York", 40.7128, -74.0060, "US", "America/New_York", -5.0, "USA"));
        CITIES.add(new City("San Francisco", 37.7749, -122.4194, "US", "America/Los_Angeles", -8.0, "USA"));
        CITIES.add(new City("Dubai", 25.2048, 55.2708, "AE", "Asia/Dubai", 4.0, "UAE"));
        CITIES.add(new City("Singapore", 1.3521, 103.8198, "SG", "Asia/Singapore", 8.0, "Singapore"));
        CITIES.add(new City("Sydney", -33.8688, 151.2093, "AU", "Australia/Sydney", 10.0, "Australia"));
        CITIES.add(new City("Toronto", 43.6532, -79.3832, "CA", "America/Toronto", -5.0, "Canada"));
    }

    public static synchronized void loadCitiesFromAssets(Context context) {
        if (isLoaded || context == null) return;
        try {
            InputStream is = context.getAssets().open("cities.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            List<City> fullList = new ArrayList<>(4600);
            // Retain top reference cities first
            fullList.addAll(CITIES);

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 8) {
                    try {
                        String name = parts[0].trim();
                        double lat = Double.parseDouble(parts[1].trim());
                        double lon = Double.parseDouble(parts[2].trim());
                        String code = parts[3].trim();
                        String tz = parts[4].trim();
                        double offset = Double.parseDouble(parts[6].trim());
                        String country = parts[7].trim();
                        fullList.add(new City(name, lat, lon, code, tz, offset, country));
                    } catch (Exception ignored) {
                    }
                }
            }
            reader.close();

            CITIES.clear();
            CITIES.addAll(fullList);
            isLoaded = true;
        } catch (Exception e) {
            // Fallback to static list if asset read fails
            isLoaded = true;
        }
    }

    public static List<City> getAllCities() {
        return CITIES;
    }

    public static City getDefaultCity() {
        return CITIES.get(2); // Pune
    }

    public static List<City> searchCities(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            if (limit <= 0 || limit >= CITIES.size()) return CITIES;
            return CITIES.subList(0, Math.min(limit, CITIES.size()));
        }

        String q = query.toLowerCase(Locale.ROOT).trim();
        List<City> prefixMatches = new ArrayList<>();
        List<City> containsMatches = new ArrayList<>();

        for (City c : CITIES) {
            String nameLower = c.name.toLowerCase(Locale.ROOT);
            if (nameLower.startsWith(q)) {
                prefixMatches.add(c);
            } else if (nameLower.contains(q) || c.country.toLowerCase(Locale.ROOT).contains(q)) {
                containsMatches.add(c);
            }

            if (prefixMatches.size() + containsMatches.size() >= limit * 2) {
                break;
            }
        }

        List<City> results = new ArrayList<>(prefixMatches);
        results.addAll(containsMatches);
        if (limit > 0 && results.size() > limit) {
            return results.subList(0, limit);
        }
        return results;
    }


    public static City getSelectedCity(Context context) {
        if (context == null) return getDefaultCity();
        android.content.SharedPreferences sp = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        String name = sp.getString("selected_city_name", "Pune");
        double lat = Double.longBitsToDouble(sp.getLong("selected_city_lat", Double.doubleToLongBits(18.5204)));
        double lon = Double.longBitsToDouble(sp.getLong("selected_city_lon", Double.doubleToLongBits(73.8567)));
        String tz = sp.getString("selected_city_tz", "Asia/Kolkata");
        String country = sp.getString("selected_city_country", "India");
        return new City(name, lat, lon, tz, country);
    }

    public static void saveSelectedCity(Context context, City city) {
        if (context == null || city == null) return;
        android.content.SharedPreferences sp = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        sp.edit()
            .putString("selected_city_name", city.name)
            .putLong("selected_city_lat", Double.doubleToRawLongBits(city.latitude))
            .putLong("selected_city_lon", Double.doubleToRawLongBits(city.longitude))
            .putString("selected_city_tz", city.timezone)
            .putString("selected_city_country", city.country)
            .apply();
    }
}
