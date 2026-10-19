package com.custom.hinduwidget;

import java.util.ArrayList;
import java.util.List;

public class NotableChartsManager {

    public static class NotablePerson {
        public String id;
        public String nameEn;
        public String nameHi;
        public String noteEn;
        public String noteHi;
        public String category;
        public int year;
        public int month;
        public int day;
        public int hour;
        public int minute;
        public String place;
        public double lat;
        public double lon;
        public double offsetHours;

        public NotablePerson(String id, String nameEn, String nameHi, String noteEn, String noteHi,
                             String category, int year, int month, int day, int hour, int minute,
                             String place, double lat, double lon, double offsetHours) {
            this.id = id;
            this.nameEn = nameEn;
            this.nameHi = nameHi;
            this.noteEn = noteEn;
            this.noteHi = noteHi;
            this.category = category;
            this.year = year;
            this.month = month;
            this.day = day;
            this.hour = hour;
            this.minute = minute;
            this.place = place;
            this.lat = lat;
            this.lon = lon;
            this.offsetHours = offsetHours;
        }

        public String getFormattedDate() {
            String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
            return String.format(java.util.Locale.US, "%d %s %d, %02d:%02d • %s",
                    day, months[month - 1], year, hour, minute, place);
        }

        @Override
        public String toString() {
            return nameEn + " (" + nameHi + ") - " + getFormattedDate();
        }
    }

    private static final List<NotablePerson> CHARTS = new ArrayList<>();

    static {
        // Spiritual Masters
        CHARTS.add(new NotablePerson("vivekananda", "Swami Vivekananda", "स्वामी विवेकानन्द",
                "Monk and teacher of Vedanta", "वेदान्त के संन्यासी और आचार्य", "Spiritual",
                1863, 1, 12, 6, 33, "Calcutta", 22.5333, 88.3667, 5.891));

        CHARTS.add(new NotablePerson("ramakrishna", "Sri Ramakrishna", "श्री रामकृष्ण परमहंस",
                "Mystic of Dakshineswar", "दक्षिणेश्वर के सन्त", "Spiritual",
                1836, 2, 18, 5, 15, "Kamarpukur", 22.9100, 87.6500, 5.843));

        CHARTS.add(new NotablePerson("ramana", "Ramana Maharshi", "रमण महर्षि",
                "Sage of Arunachala", "अरुणाचल के महर्षि", "Spiritual",
                1879, 12, 30, 1, 0, "Tiruchuzhi", 9.5167, 78.1000, 5.207));

        CHARTS.add(new NotablePerson("aurobindo", "Sri Aurobindo", "श्री अरविन्द",
                "Revolutionary, yogi and philosopher", "क्रान्तिकारी, योगी और दार्शनिक", "Spiritual",
                1872, 8, 15, 5, 16, "Calcutta", 22.5333, 88.3667, 5.891));

        CHARTS.add(new NotablePerson("krishnamurti", "J. Krishnamurti", "जिद्दु कृष्णमूर्ति",
                "Philosopher and teacher", "दार्शनिक और शिक्षक", "Spiritual",
                1895, 5, 12, 0, 30, "Madanapalle", 13.5500, 78.5000, 5.353));

        CHARTS.add(new NotablePerson("anandamayi", "Anandamayi Ma", "आनन्दमयी माँ",
                "Saint of Bengal", "बंगाल की सन्त", "Spiritual",
                1896, 4, 30, 3, 45, "Kheora", 23.7833, 91.0833, 5.889));

        CHARTS.add(new NotablePerson("rajneesh", "Osho (Rajneesh)", "ओशो (रजनीश)",
                "Spiritual teacher", "आध्यात्मिक गुरु", "Spiritual",
                1931, 12, 11, 17, 13, "Kuchwada", 23.1447, 78.3492, 5.500));

        // National Leaders & Architects
        CHARTS.add(new NotablePerson("gandhi", "Mahatma Gandhi", "महात्मा गांधी",
                "Father of the Nation", "राष्ट्रपिता", "Leaders",
                1869, 10, 2, 7, 8, "Porbandar", 21.6333, 69.6000, 4.640));

        CHARTS.add(new NotablePerson("nehru", "Jawaharlal Nehru", "जवाहरलाल नेहरू",
                "First Prime Minister of India", "भारत के पहले प्रधानमंत्री", "Leaders",
                1889, 11, 14, 23, 30, "Allahabad", 25.4500, 81.8500, 5.353));

        CHARTS.add(new NotablePerson("ambedkar", "B. R. Ambedkar", "भीमराव आम्बेडकर",
                "Architect of Constitution of India", "भारतीय संविधान के शिल्पी", "Leaders",
                1891, 4, 14, 12, 0, "Mhow", 22.5500, 75.7667, 5.353));

        CHARTS.add(new NotablePerson("bose", "Subhas Chandra Bose", "सुभाष चन्द्र बोस",
                "Leader of Indian National Army", "आज़ाद हिन्द फ़ौज के नेता", "Leaders",
                1897, 1, 23, 12, 0, "Cuttack", 20.5000, 85.8333, 5.353));

        CHARTS.add(new NotablePerson("tilak", "Bal Gangadhar Tilak", "बाल गंगाधर तिलक",
                "Lokmanya, freedom movement leader", "लोकमान्य, स्वतन्त्रता सेनानी", "Leaders",
                1856, 7, 23, 6, 24, "Ratnagiri", 16.9833, 73.3000, 4.887));

        CHARTS.add(new NotablePerson("indira", "Indira Gandhi", "इन्दिरा गांधी",
                "Prime Minister of India", "भारत की प्रधानमंत्री", "Leaders",
                1917, 11, 19, 23, 11, "Allahabad", 25.4500, 81.8500, 5.500));

        CHARTS.add(new NotablePerson("rajiv", "Rajiv Gandhi", "राजीव गांधी",
                "Prime Minister of India", "भारत के प्रधानमंत्री", "Leaders",
                1944, 8, 20, 6, 34, "Bombay", 18.9667, 72.8333, 4.856));

        // Science & Mathematics
        CHARTS.add(new NotablePerson("ramanujan", "Srinivasa Ramanujan", "श्रीनिवास रामानुजन",
                "Genius Mathematician", "महान गणितज्ञ", "Science",
                1887, 12, 22, 18, 0, "Erode", 11.3500, 77.7333, 5.350));

        CHARTS.add(new NotablePerson("cvraman", "C. V. Raman", "सी. वी. रमन",
                "Physicist, Nobel Laureate", "भौतिकविद्, नोबेल विजेता", "Science",
                1888, 11, 8, 4, 25, "Trichinopoly", 10.8167, 78.6833, 5.350));

        CHARTS.add(new NotablePerson("einstein", "Albert Einstein", "अल्बर्ट आइंस्टाइन",
                "Theoretical Physicist, Nobel Laureate", "महान वैज्ञानिक, नोबेल विजेता", "Science",
                1879, 3, 14, 11, 30, "Ulm, Germany", 48.4000, 10.0000, 0.667));

        // Arts & Culture
        CHARTS.add(new NotablePerson("lata", "Lata Mangeshkar", "लता मंगेशकर",
                "Nightingale of India", "भारत कोकिला, स्वर कोकिला", "Arts",
                1929, 9, 28, 22, 30, "Indore", 22.7167, 75.8333, 5.500));

        CHARTS.add(new NotablePerson("gurudutt", "Guru Dutt", "गुरु दत्त",
                "Legendary Filmmaker & Actor", "महान फ़िल्मकार और अभिनेता", "Arts",
                1925, 7, 9, 12, 0, "Bangalore", 12.9833, 77.5833, 5.500));

        // Vedic Jyotish Master
        CHARTS.add(new NotablePerson("bvraman", "B. V. Raman", "बी. वी. रमन",
                "Grandmaster of Modern Vedic Astrology", "आधुनिक वैदिक ज्योतिष के पितामह", "Jyotisha",
                1912, 8, 8, 19, 43, "Bangalore", 12.9833, 77.5833, 5.500));
    }

    public static List<NotablePerson> getAllCharts() {
        return new ArrayList<>(CHARTS);
    }

    public static NotablePerson findById(String id) {
        for (NotablePerson p : CHARTS) {
            if (p.id.equalsIgnoreCase(id)) return p;
        }
        return null;
    }
}
