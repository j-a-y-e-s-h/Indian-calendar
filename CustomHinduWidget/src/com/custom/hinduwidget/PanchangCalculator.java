package com.custom.hinduwidget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PanchangCalculator {

    public static final String[] TITHI_NAMES = {
        "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
        "Shashthi", "Saptami", "Ashtami", "Navami", "Dashami",
        "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Purnima",
        "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
        "Shashthi", "Saptami", "Ashtami", "Navami", "Dashami",
        "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Amavasya"
    };

    public static final String[] HINDU_MONTHS_AMANTA = {
        "Chaitra", "Vaishakha", "Jyeshtha", "Ashadha", "Shravana", "Bhadrapada",
        "Ashwina", "Kartika", "Margashirsha", "Pausha", "Magha", "Phalguna"
    };

    public static final String[] RASHIS = {
        "Mesha", "Vrishabh", "Mithun", "Kark", "Simha", "Kanya",
        "Tula", "Vrischik", "Dhanu", "Makar", "Kumbha", "Meen"
    };

    public static final String[] RASHIS_ENGLISH = {
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    };

    public static final String[] RASHI_SYMBOLS = {
        "♈", "♉", "♊", "♋", "♌", "♍",
        "♎", "♏", "♐", "♑", "♒", "♓"
    };

    public static final String[] RASHI_LORDS = {
        "Mangal (Mars)", "Shukra (Venus)", "Budh (Mercury)", "Chandra (Moon)",
        "Surya (Sun)", "Budh (Mercury)", "Shukra (Venus)", "Mangal (Mars)",
        "Guru (Jupiter)", "Shani (Saturn)", "Shani (Saturn)", "Guru (Jupiter)"
    };

    public static final String[] NAKSHATRAS = {
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra",
        "Punarvasu", "Pushya", "Ashlesha", "Magha", "P. Phalguni", "U. Phalguni",
        "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha",
        "Mula", "P. Ashadha", "U. Ashadha", "Shravana", "Dhanishta", "Shatabhisha",
        "P. Bhadrapada", "U. Bhadrapada", "Revati"
    };

    public static final String[] NAKSHATRA_LORDS = {
        "Ketu", "Shukra", "Surya", "Chandra", "Mangal", "Rahu",
        "Guru", "Shani", "Budh", "Ketu", "Shukra", "Surya",
        "Chandra", "Mangal", "Rahu", "Guru", "Shani", "Budh",
        "Ketu", "Shukra", "Surya", "Chandra", "Mangal", "Rahu",
        "Guru", "Shani", "Budh"
    };

    public static final String[] NAKSHATRA_SYLLABLES = {
        "Chu, Che, Cho, La", "Li, Lu, Le, Lo", "A, I, U, E", "O, Va, Vi, Vu", "Ve, Vo, Ka, Ki", "Ku, Gha, Nga, Chha",
        "Ke, Ko, Ha, Hi", "Hu, He, Ho, Da", "Di, Du, De, Do", "Ma, Mi, Mu, Me", "Mo, Ta, Ti, Tu", "Te, To, Pa, Pi",
        "Pu, Sha, Na, Tha", "Pe, Po, Ra, Ri", "Ru, Re, Ro, Taa", "Ti, Tu, Te, To", "Na, Ni, Nu, Ne", "No, Ya, Yi, Yu",
        "Ye, Yo, Bha, Bhi", "Bhu, Dha, Bha, Dha", "Bhe, Bho, Ja, Ji", "Khi, Khu, Khe, Kho", "Ga, Gi, Gu, Ge", "Go, Sa, Si, Su",
        "Se, So, Da, Di", "Du, Tha, Jna, Da", "De, Do, Cha, Chi"
    };

    public static final String[] YOGAS = {
        "Vishkambha", "Priti", "Ayushman", "Saubhagya", "Shobhana", "Atiganda",
        "Sukarma", "Dhriti", "Shula", "Ganda", "Vriddhi", "Dhruva",
        "Vyaghata", "Harshana", "Vajra", "Siddhi", "Vyatipata", "Variyan",
        "Parigha", "Shiva", "Siddha", "Sadhya", "Shubha", "Shukla",
        "Brahma", "Indra", "Vaidhriti"
    };

    public static final String[] KARANAS = {
        "Bava", "Balava", "Kaulava", "Taitila", "Garija", "Vanija", "Vishti (Bhadra)",
        "Shakuni", "Chatushpada", "Naga", "Kintughna"
    };

    public static final String[] SAMVATSARAS = {
        "Prabhava", "Vibhava", "Shukla", "Pramoda", "Prajapati", "Angirasa", "Shrimukha", "Bhava", "Yuva", "Dhatri",
        "Ishwara", "Bahudhanya", "Pramathi", "Vikrama", "Vrisha", "Chitrabhanu", "Subhanu", "Tarana", "Parthiva", "Vyaya",
        "Sarvajit", "Sarvadhari", "Virodhi", "Vikruti", "Khara", "Nandana", "Vijaya", "Jaya", "Manmatha", "Durmukha",
        "Hemalamba", "Vilamba", "Vikari", "Sharvari", "Plava", "Shubhakrit", "Shobhakrit", "Krodhi", "Vishvavasu", "Parabhava",
        "Plavanga", "Kilaka", "Saumya", "Sadharana", "Virodhikrit", "Paridhavi", "Pramadicha", "Ananda", "Rakshasa", "Nala",
        "Pingala", "Kalayukta", "Siddharthi", "Raudra", "Durmati", "Dundubhi", "Rudhirodgari", "Raktaksha", "Krodhana", "Kshaya"
    };

    public static final String[] CHOGHADIYA_TYPES = {
        "Udveg", "Char", "Labh", "Amrit", "Kaal", "Shubh", "Rog"
    };

    public static final String[] CHOGHADIYA_NATURE = {
        "Inauspicious (Ashubh)", "Neutral / Auspicious (Chanchal)", "Auspicious (Labhadayak)",
        "Highly Auspicious (Amrit)", "Inauspicious (Haani)", "Auspicious (Kalyankari)", "Inauspicious (Rog)"
    };

    public static final String[] CHOGHADIYA_LORDS = {
        "Surya (Sun)", "Shukra (Venus)", "Budh (Mercury)", "Chandra (Moon)",
        "Shani (Saturn)", "Guru (Jupiter)", "Mangal (Mars)"
    };

    // Starting Day Choghadiya index (0..6) for Sunday (0) to Saturday (6)
    // Sun: Udveg(0), Mon: Amrit(3), Tue: Rog(6), Wed: Labh(2), Thu: Shubh(5), Fri: Char(1), Sat: Kaal(4)
    public static final int[] DAY_CHOGHADIYA_START = { 0, 3, 6, 2, 5, 1, 4 };
    // Starting Night Choghadiya index for Sunday to Saturday
    // Sun: Shubh(5), Mon: Char(1), Tue: Kaal(4), Wed: Udveg(0), Thu: Amrit(3), Fri: Rog(6), Sat: Labh(2)
    public static final int[] NIGHT_CHOGHADIYA_START = { 5, 1, 4, 0, 3, 6, 2 };

    // Models
    public static class PlanetPosition {
        public String name;
        public String sanskritName;
        public double longitude; // 0..360 Nirayana
        public double sayanaLongitude; // 0..360 Tropical
        public int rashiIndex; // 0..11
        public String rashiName;
        public String rashiSymbol;
        public double degreesInRashi; // 0..30
        public int nakshatraIndex; // 0..26
        public String nakshatraName;
        public int pada; // 1..4
        public String padaSyllable;
        public boolean isRetrograde;
        public int iconResId;

        public String getFormattedPosition() {
            int deg = (int) degreesInRashi;
            int min = (int) Math.round((degreesInRashi - deg) * 60.0);
            return String.format(Locale.US, "%d°%02d'", deg, min);
        }
    }

    public static class ChoghadiyaPeriod {
        public String name;
        public String nature;
        public String lord;
        public boolean isAuspicious;
        public double startHour;
        public double endHour;
        public String startTimeStr;
        public String endTimeStr;
        public boolean isCurrent;

        @Override
        public String toString() {
            return name + " (" + startTimeStr + " - " + endTimeStr + ")";
        }
    }

    public static class MuhurtaTimings {
        public String rahuKaal;
        public String yamagand;
        public String gulika;
        public String abhijit;
        public String brahmaMuhurta;
        public String amritKaal;
        public String pradoshKaal;
        public String vijayaMuhurta;
        public String godhuliMuhurta;
        public String nishitaKaal;
        public String durMuhurta1;
        public String durMuhurta2;
    }

    public static class HoraPeriod {
        public int hourNumber; // 1..24
        public String planetSanskrit;
        public String planetEnglish;
        public String lord;
        public boolean isDayHora; // true: 1..12, false: 13..24
        public double startHour;
        public double endHour;
        public String startTimeStr;
        public String endTimeStr;
        public boolean isCurrent;
        public String nature;
        public int iconResId;
    }

    public static class GunaMilanResult {
        public int varnaPoints;
        public int vashyaPoints;
        public int taraPoints;
        public int yoniPoints;
        public int grahaMaitriPoints;
        public int ganaPoints;
        public int bhakootPoints;
        public int nadiPoints;
        public int totalPoints;

        public boolean hasNadiDosha;
        public boolean hasBhakootDosha;
        public String verdict;
        public String breakdown;
    }

    public static class AvakahadaChakra {
        public String rashiSwami;
        public String nakshatraSwami;
        public String paya;
        public String yoni;
        public String gana;
        public String nadi;
        public String varna;
        public String vashya;
        public String janmaAkshara;
    }

    public static class DayPanchang {
        public int year;
        public int month;
        public int day;
        public int dayOfWeek; // 1=Sun .. 7=Sat
        public String vaarName;

        public int tithiIndex; // 1..30
        public String paksha;
        public String tithiName;
        public String tithiEndTimeStr;
        public String shortLabel;

        public String hinduMonth;
        public int hinduMonthIndex;
        public String purnimantaMonth;
        public boolean isCurrentMonth = true;
        public boolean isToday = false;

        public String getShortMonthName() {
            if (hinduMonth == null || hinduMonth.isEmpty()) return "";
            if (hinduMonth.equalsIgnoreCase("Ashwina")) return "Ashwin";
            if (hinduMonth.equalsIgnoreCase("Bhadrapada")) return "Bhadra";
            if (hinduMonth.equalsIgnoreCase("Margashirsha")) return "Marga";
            if (hinduMonth.length() > 6) return hinduMonth.substring(0, 6);
            return hinduMonth;
        }

        public int vikramSamvat;
        public int sakaSamvat;
        public int gujaratiSamvat;
        public int kaliSamvat;
        public String samvatsara;
        public String samvatsaraNorth;
        public String samvatsaraSouth;
        public String saurMasa;
        public String ritu;
        public String ayana;
        public String vimshottariDashaBalance;
        public String kujaDoshaStatus;

        public double sunriseHour;
        public double sunsetHour;
        public double moonriseHour;
        public double moonsetHour;
        public String sunriseStr;
        public String sunsetStr;
        public String moonriseStr;
        public String moonsetStr;
        public String dayLengthStr;
        public String nightLengthStr;

        public int moonDrawableResId;
        public double moonIlluminationPercent;

        public int chandraRashiIndex;
        public String chandraRashi;
        public String chandraRashiFull;

        public int suryaRashiIndex;
        public String suryaRashi;
        public String suryaRashiFull;

        public int nakshatraIndex;
        public String nakshatra;
        public String nakshatraFull;
        public int nakshatraPada;
        public String nakshatraEndTimeStr;

        public String yoga;
        public String yogaEndTimeStr;

        public String karana1;
        public String karana1EndTimeStr;
        public String karana2;
        public String karana2EndTimeStr;

        public String specialOccasion;
        public String specialOccasionFull;

        // Extended details
        public List<PlanetPosition> planets = new ArrayList<>();
        public List<ChoghadiyaPeriod> dayChoghadiya = new ArrayList<>();
        public List<ChoghadiyaPeriod> nightChoghadiya = new ArrayList<>();
        public ChoghadiyaPeriod activeChoghadiya;
        public List<HoraPeriod> dayHoras = new ArrayList<>();
        public List<HoraPeriod> nightHoras = new ArrayList<>();
        public HoraPeriod activeHora;
        public MuhurtaTimings muhurta = new MuhurtaTimings();
        public AvakahadaChakra avakahada = new AvakahadaChakra();
        public boolean hasMangalDosha;

        // Hindu Time
        public int ghati;
        public int pala;
        public int vipala;
        public String ishtakaalStr;
        public String hinduTimeStr;

        public boolean hasFestival() {
            return specialOccasion != null && !specialOccasion.isEmpty();
        }

        public String getDisplayOccasion() {
            if (specialOccasion != null && !specialOccasion.isEmpty()) return specialOccasion;
            if (tithiIndex == 15) return "Purnima";
            if (tithiIndex == 30) return "Amavasya";
            String pPrefix = (tithiIndex <= 15) ? "S. " : "K. ";
            return pPrefix + tithiName;
        }

        public String getShortOccasion() {
            if (specialOccasion != null && !specialOccasion.isEmpty()) {
                if (specialOccasion.contains("Navratri")) return "Navratri";
                if (specialOccasion.contains("Dussehra")) return "Dussehra";
                if (specialOccasion.contains("Diwali")) return "Diwali";
                if (specialOccasion.contains("Ekadashi")) return "Ekadashi";
                if (specialOccasion.length() > 14) return specialOccasion.substring(0, 12) + "..";
                return specialOccasion;
            }
            if (tithiIndex == 15) return "Purnima";
            if (tithiIndex == 30) return "Amavasya";
            return "";
        }
    }

    // Mathematical Astronomy Helpers
    public static double normalizeDegrees(double deg) {
        deg = deg % 360.0;
        if (deg < 0) deg += 360.0;
        return deg;
    }

    public static double solveKepler(double mDeg, double e) {
        double mRad = Math.toRadians(mDeg);
        double eRad = mRad;
        for (int i = 0; i < 15; i++) {
            double dE = (eRad - e * Math.sin(eRad) - mRad) / (1.0 - e * Math.cos(eRad));
            eRad -= dE;
            if (Math.abs(dE) < 1e-7) break;
        }
        return eRad;
    }

    public static double julianDay(int year, int month, int day, double hourUtc) {
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        int a = year / 100;
        int b = 2 - a + (a / 4);
        double dayFraction = day + (hourUtc / 24.0);
        return Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + dayFraction + b - 1524.5;
    }

    public static double lahiriAyanamsa(double jd) {
        double t = (jd - 2451545.0) / 36525.0;
        return 23.85709 + 1.39697 * t;
    }

    public static double sunLongitude(double jd) {
        double t = (jd - 2451545.0) / 36525.0;
        double l0 = 280.46646 + 36000.76983 * t + 0.0003032 * t * t;
        double m = 357.52911 + 35999.05029 * t - 0.0001537 * t * t;
        double mRad = Math.toRadians(m);
        double c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * Math.sin(mRad);
        c += (0.019993 - 0.000101 * t) * Math.sin(2 * mRad);
        c += 0.000289 * Math.sin(3 * mRad);
        return normalizeDegrees(l0 + c);
    }

    public static double moonLongitude(double jd) {
        double t = (jd - 2451545.0) / 36525.0;
        double lPrime = 218.3164477 + 481267.88127777 * t;
        double d = 297.8501921 + 445267.1114034 * t - 0.0018819 * t * t;
        double m = 357.5291092 + 35999.0502909 * t - 0.0001536 * t * t;
        double mPrime = 134.9633964 + 477198.8675055 * t + 0.0087414 * t * t;
        double f = 93.2720950 + 483202.0175233 * t - 0.0036539 * t * t;

        double dRad = Math.toRadians(d);
        double mRad = Math.toRadians(m);
        double mpRad = Math.toRadians(mPrime);
        double fRad = Math.toRadians(f);

        double deltaLambda = 6.288774 * Math.sin(mpRad);
        deltaLambda += 1.274027 * Math.sin(2 * dRad - mpRad);
        deltaLambda += 0.658314 * Math.sin(2 * dRad);
        deltaLambda += 0.213618 * Math.sin(2 * mpRad);
        deltaLambda -= 0.185116 * Math.sin(mRad);
        deltaLambda -= 0.114332 * Math.sin(2 * fRad);
        deltaLambda += 0.058793 * Math.sin(2 * dRad - 2 * mpRad);
        deltaLambda += 0.057066 * Math.sin(2 * dRad - mRad - mpRad);
        deltaLambda += 0.053322 * Math.sin(2 * dRad + mpRad);
        deltaLambda += 0.045758 * Math.sin(2 * dRad - mRad);
        deltaLambda -= 0.040923 * Math.sin(mRad - mpRad);
        deltaLambda -= 0.034720 * Math.sin(dRad);
        deltaLambda -= 0.030383 * Math.sin(mRad + mpRad);
        deltaLambda += 0.015327 * Math.sin(2 * dRad - 2 * fRad);
        deltaLambda += 0.012528 * Math.sin(2 * fRad + mpRad);
        deltaLambda += 0.010980 * Math.sin(2 * fRad - mpRad);

        return normalizeDegrees(lPrime + deltaLambda);
    }

    public static double[] calculateSunTimes(int year, int month, int day, double lat, double lon) {
        int n1 = (275 * month) / 9;
        int n2 = (month + 9) / 12;
        int n3 = 1 + ((year - 4 * (year / 4) + 2) / 3);
        int doy = n1 - (n2 * n3) + day - 30;

        double lngHour = lon / 15.0;
        double tRise = doy + ((6.0 - lngHour) / 24.0);
        double tSet = doy + ((18.0 - lngHour) / 24.0);

        // Sunrise
        double mRise = (0.9856 * tRise) - 3.289;
        double lRise = normalizeDegrees(mRise + (1.916 * Math.sin(Math.toRadians(mRise))) + (0.020 * Math.sin(Math.toRadians(2 * mRise))) + 282.634);
        double sinDecRise = 0.39782 * Math.sin(Math.toRadians(lRise));
        double cosDecRise = Math.cos(Math.asin(sinDecRise));
        double cosHRise = (Math.cos(Math.toRadians(90.833)) - (sinDecRise * Math.sin(Math.toRadians(lat)))) / (cosDecRise * Math.cos(Math.toRadians(lat)));

        double riseIst = 6.44;
        if (cosHRise >= -1.0 && cosHRise <= 1.0) {
            double hRise = (360.0 - Math.toDegrees(Math.acos(cosHRise))) / 15.0;
            double localTimeRise = hRise + (lRise / 15.0) - (0.06571 * tRise) - 6.622;
            riseIst = (localTimeRise - lngHour + 5.5) % 24.0;
            if (riseIst < 0) riseIst += 24.0;
        }

        // Sunset
        double mSet = (0.9856 * tSet) - 3.289;
        double lSet = normalizeDegrees(mSet + (1.916 * Math.sin(Math.toRadians(mSet))) + (0.020 * Math.sin(Math.toRadians(2 * mSet))) + 282.634);
        double sinDecSet = 0.39782 * Math.sin(Math.toRadians(lSet));
        double cosDecSet = Math.cos(Math.asin(sinDecSet));
        double cosHSet = (Math.cos(Math.toRadians(90.833)) - (sinDecSet * Math.sin(Math.toRadians(lat)))) / (cosDecSet * Math.cos(Math.toRadians(lat)));

        double setIst = 17.95;
        if (cosHSet >= -1.0 && cosHSet <= 1.0) {
            double hSet = Math.toDegrees(Math.acos(cosHSet)) / 15.0;
            double localTimeSet = hSet + (lSet / 15.0) - (0.06571 * tSet) - 6.622;
            setIst = (localTimeSet - lngHour + 5.5) % 24.0;
            if (setIst < 0) setIst += 24.0;
        }

        return new double[] { riseIst, setIst };
    }

    public static String formatTimeFromDecimal(double hours) {
        hours = hours % 24.0;
        if (hours < 0) hours += 24.0;
        int h = (int) hours;
        int m = (int) Math.round((hours - h) * 60.0);
        if (m >= 60) {
            h = (h + 1) % 24;
            m = 0;
        }
        return String.format(Locale.US, "%02d:%02d", h, m);
    }

    public static String formatTimeAmPm(double hours) {
        hours = hours % 24.0;
        if (hours < 0) hours += 24.0;
        int h = (int) hours;
        int m = (int) Math.round((hours - h) * 60.0);
        if (m >= 60) { h = (h + 1) % 24; m = 0; }
        String ampm = (h >= 12) ? "PM" : "AM";
        int h12 = h % 12;
        if (h12 == 0) h12 = 12;
        return String.format(Locale.US, "%02d:%02d %s", h12, m, ampm);
    }

    public static String formatDuration(double hours) {
        int h = (int) hours;
        int m = (int) Math.round((hours - h) * 60.0);
        return String.format(Locale.US, "%dh %02dm", h, m);
    }

    public static String formatTimeWithSeconds(double hours) {
        hours = hours % 24.0;
        if (hours < 0) hours += 24.0;
        int h = (int) hours;
        double remM = (hours - h) * 60.0;
        int m = (int) remM;
        int s = (int) Math.round((remM - m) * 60.0);
        if (s >= 60) { m++; s = 0; }
        if (m >= 60) { h = (h + 1) % 24; m = 0; }
        return String.format(Locale.US, "%02d:%02d:%02d", h, m, s);
    }

    // Complete Astronomical Panchang Calculation
    public static DayPanchang calculatePanchang(int year, int month, int day, double lat, double lon) {
        DayPanchang dp = new DayPanchang();
        dp.year = year;
        dp.month = month;
        dp.day = day;

        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day);
        dp.dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        String[] vaarNames = { "Ravivara", "Somavara", "Mangalavara", "Budhavara", "Guruvara", "Shukravara", "Shanivara" };
        dp.vaarName = vaarNames[dp.dayOfWeek - 1];

        double[] sunTimes = calculateSunTimes(year, month, day, lat, lon);
        dp.sunriseHour = sunTimes[0];
        dp.sunsetHour = sunTimes[1];
        dp.sunriseStr = formatTimeAmPm(dp.sunriseHour);
        dp.sunsetStr = formatTimeAmPm(dp.sunsetHour);

        double dayLen = dp.sunsetHour - dp.sunriseHour;
        if (dayLen < 0) dayLen += 24.0;
        double nightLen = 24.0 - dayLen;
        dp.dayLengthStr = formatDuration(dayLen);
        dp.nightLengthStr = formatDuration(nightLen);

        // Julian Day at Sunrise
        double hourUtc = dp.sunriseHour - 5.5;
        double jdSunrise = julianDay(year, month, day, hourUtc);

        double sunLon = sunLongitude(jdSunrise);
        double moonLon = moonLongitude(jdSunrise);
        double ayanamsa = lahiriAyanamsa(jdSunrise);

        double nirayanaSun = normalizeDegrees(sunLon - ayanamsa);
        double nirayanaMoon = normalizeDegrees(moonLon - ayanamsa);

        // Tithi
        double diff = normalizeDegrees(moonLon - sunLon);
        int tithiIdx = (int)(diff / 12.0) + 1;
        if (tithiIdx > 30) tithiIdx = 30;
        if (tithiIdx < 1) tithiIdx = 1;
        dp.tithiIndex = tithiIdx;
        dp.paksha = (tithiIdx <= 15) ? "Shukla" : "Krishna";
        dp.tithiName = TITHI_NAMES[tithiIdx - 1];

        // Approximate Moonrise / Moonset
        dp.moonriseHour = (dp.sunriseHour + (dp.tithiIndex * 0.8)) % 24.0;
        dp.moonsetHour = (dp.moonriseHour + 12.4) % 24.0;
        dp.moonriseStr = formatTimeAmPm(dp.moonriseHour);
        dp.moonsetStr = formatTimeAmPm(dp.moonsetHour);

        // Tithi End Time
        double remTithiDeg = 12.0 - (diff % 12.0);
        double hoursToTithiEnd = (remTithiDeg / 12.19) * 24.0;
        dp.tithiEndTimeStr = formatTimeAmPm((dp.sunriseHour + hoursToTithiEnd) % 24.0);

        // Moon Drawable & Illumination
        dp.moonDrawableResId = getMoonDrawableResId(tithiIdx);
        dp.moonIlluminationPercent = Math.abs(diff - 180.0) / 180.0;
        dp.moonIlluminationPercent = (1.0 - Math.cos(Math.toRadians(diff))) / 2.0 * 100.0;

        if (tithiIdx == 15) {
            dp.shortLabel = "Purnima";
        } else if (tithiIdx == 30) {
            dp.shortLabel = "Amavasya";
        } else {
            String pPrefix = (tithiIdx <= 15) ? "S. " : "K. ";
            dp.shortLabel = pPrefix + dp.tithiName;
        }

        // Chandra Rashi
        dp.chandraRashiIndex = (int)(nirayanaMoon / 30.0) % 12;
        dp.chandraRashi = RASHIS[dp.chandraRashiIndex];
        dp.chandraRashiFull = dp.chandraRashi + " (" + RASHIS_ENGLISH[dp.chandraRashiIndex] + ")";

        // Surya Rashi
        dp.suryaRashiIndex = (int)(nirayanaSun / 30.0) % 12;
        dp.suryaRashi = RASHIS[dp.suryaRashiIndex];
        dp.suryaRashiFull = dp.suryaRashi + " (" + RASHIS_ENGLISH[dp.suryaRashiIndex] + ")";

        // Nakshatra
        dp.nakshatraIndex = (int)(nirayanaMoon / (360.0 / 27.0)) % 27;
        dp.nakshatra = NAKSHATRAS[dp.nakshatraIndex];
        dp.nakshatraFull = dp.nakshatra;
        double remNakDeg = (360.0 / 27.0) - (nirayanaMoon % (360.0 / 27.0));
        double hoursToNakEnd = (remNakDeg / 13.176) * 24.0;
        dp.nakshatraEndTimeStr = formatTimeAmPm((dp.sunriseHour + hoursToNakEnd) % 24.0);
        dp.nakshatraPada = ((int)(nirayanaMoon / (360.0 / 108.0)) % 4) + 1;

        // Yoga
        double yogaDeg = normalizeDegrees(nirayanaMoon + nirayanaSun);
        int yogaIdx = (int)(yogaDeg / (360.0 / 27.0)) % 27;
        dp.yoga = YOGAS[yogaIdx];
        double remYogaDeg = (360.0 / 27.0) - (yogaDeg % (360.0 / 27.0));
        double hoursToYogaEnd = (remYogaDeg / 14.16) * 24.0;
        dp.yogaEndTimeStr = formatTimeAmPm((dp.sunriseHour + hoursToYogaEnd) % 24.0);

        // Karana
        int karanaIdx1 = (int)(diff / 6.0) % 60;
        dp.karana1 = getKaranaName(karanaIdx1);
        dp.karana2 = getKaranaName((karanaIdx1 + 1) % 60);
        dp.karana1EndTimeStr = dp.tithiEndTimeStr;
        dp.karana2EndTimeStr = formatTimeAmPm((dp.sunriseHour + hoursToTithiEnd + 12.0) % 24.0);

        // Hindu Months
        double daysSinceAmav = diff / 12.19;
        double sunLonAmav = sunLon - daysSinceAmav * 0.9856;
        double nirayanaSunAmav = normalizeDegrees(sunLonAmav - ayanamsa);
        int rashiAmav = (int)(nirayanaSunAmav / 30.0) % 12;
        dp.hinduMonthIndex = (rashiAmav + 1) % 12;
        dp.hinduMonth = HINDU_MONTHS_AMANTA[dp.hinduMonthIndex];
        dp.purnimantaMonth = (tithiIdx > 15) ? HINDU_MONTHS_AMANTA[(dp.hinduMonthIndex + 1) % 12] : dp.hinduMonth;

        // Samvatsara, Samvat, Ritu, Ayana
        dp.vikramSamvat = year + 57;
        dp.sakaSamvat = year - 78;
        dp.gujaratiSamvat = dp.vikramSamvat - 1;
        dp.kaliSamvat = year + 3101;
        int southIdx = Math.abs((dp.sakaSamvat + 11) % 60);
        int northIdx = Math.abs((dp.vikramSamvat + 9) % 60);
        dp.samvatsaraSouth = SAMVATSARAS[southIdx];
        dp.samvatsaraNorth = SAMVATSARAS[northIdx];
        dp.samvatsara = dp.samvatsaraSouth;
        dp.saurMasa = RASHIS[dp.suryaRashiIndex % 12];
        dp.ritu = (month >= 3 && month <= 4) ? "Vasanta" : (month >= 5 && month <= 6) ? "Grishma" : (month >= 7 && month <= 8) ? "Varsha" : (month >= 9 && month <= 10) ? "Sharad" : (month >= 11 && month <= 12) ? "Hemanta" : "Shishira";
        dp.ayana = (month >= 1 && month <= 6) ? "Uttarayana" : "Dakshinayana";

        // Current Hindu Time (Ghati-Pal) based on current hour
        Calendar now = Calendar.getInstance();
        double currentHourIst = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0 + now.get(Calendar.SECOND) / 3600.0;
        double elapsedHours = currentHourIst - dp.sunriseHour;
        if (elapsedHours < 0) elapsedHours += 24.0;
        double totalGhati = elapsedHours * 2.5; // 1 hr = 2.5 ghati
        dp.ghati = (int) totalGhati;
        double remPala = (totalGhati - dp.ghati) * 60.0;
        dp.pala = (int) remPala;
        dp.vipala = (int) Math.round((remPala - dp.pala) * 60.0);
        dp.ishtakaalStr = String.format(Locale.US, "%02d:%02d:%02d", dp.ghati, dp.pala, dp.vipala);
        dp.hinduTimeStr = String.format(Locale.US, "%02d:%02d", dp.ghati, dp.pala);

        // Planetary Ephemeris (Schlyter VSOP)
        calculatePlanets(dp, jdSunrise, ayanamsa, lat, lon);

        // Choghadiya Timings
        calculateChoghadiya(dp, currentHourIst);

        // Hora Timings (24 Planetary Hours)
        calculateHora(dp, currentHourIst);

        // Muhurta Timings
        calculateMuhurtas(dp);

        // Avakahada Chakra
        calculateAvakahada(dp);

        // Mangal Dosha
        checkMangalDosha(dp);

        // Special Occasions / Festivals
        calculateOccasion(dp);

        return dp;
    }

    private static String getKaranaName(int karanaIdx) {
        if (karanaIdx == 0) return "Kintughna";
        if (karanaIdx >= 57) return KARANAS[karanaIdx - 50];
        return KARANAS[(karanaIdx - 1) % 7];
    }

    private static void calculatePlanets(DayPanchang dp, double jd, double ayanamsa, double lat, double lon) {
        dp.planets.clear();
        double d = jd - 2451543.5;
        double T = (jd - 2451545.0) / 36525.0;

        // 1. Lagna (Ascendant)
        double eps = 23.4392911 - 0.0130042 * T;
        double gmst = 280.46061837 + 360.98564736629 * (jd - 2451545.0) + 0.000387933 * T * T;
        double lmst = normalizeDegrees(gmst + lon);
        double thetaRad = Math.toRadians(lmst);
        double epsRad = Math.toRadians(eps);
        double latRad = Math.toRadians(lat);
        double lagnaSayana = normalizeDegrees(Math.toDegrees(Math.atan2(-Math.cos(thetaRad), Math.sin(thetaRad) * Math.cos(epsRad) + Math.tan(latRad) * Math.sin(epsRad))));
        if (Math.cos(thetaRad) < 0) lagnaSayana = normalizeDegrees(lagnaSayana + 180.0);
        double lagnaNirayana = normalizeDegrees(lagnaSayana - ayanamsa);
        addPlanet(dp, "Lagna", "Ascendant", lagnaNirayana, lagnaSayana, false, R.drawable.zodiac_10);

        // Sun
        double wSun = normalizeDegrees(282.9404 + 4.70935e-5 * d);
        double eSun = 0.016709 - 1.151e-9 * d;
        double mSun = normalizeDegrees(356.0470 + 0.9856002585 * d);
        double eRadSun = solveKepler(mSun, eSun);
        double xSun = Math.cos(eRadSun) - eSun;
        double ySun = Math.sin(eRadSun) * Math.sqrt(1 - eSun * eSun);
        double rSun = Math.sqrt(xSun * xSun + ySun * ySun);
        double vSun = Math.toDegrees(Math.atan2(ySun, xSun));
        double lonSun = normalizeDegrees(vSun + wSun);
        double xs = rSun * Math.cos(Math.toRadians(lonSun));
        double ys = rSun * Math.sin(Math.toRadians(lonSun));
        double sunNirayana = normalizeDegrees(lonSun - ayanamsa);
        addPlanet(dp, "Surya", "Sun", sunNirayana, lonSun, false, R.drawable.sun_icon);

        // Moon
        double moonSayana = moonLongitude(jd);
        double moonNirayana = normalizeDegrees(moonSayana - ayanamsa);
        addPlanet(dp, "Chandra", "Moon", moonNirayana, moonSayana, false, R.drawable.moon_icon);

        // Mercury
        addSchlyterPlanet(dp, "Budh", "Mercury", d, ayanamsa, xs, ys,
            48.3313 + 3.24587e-5 * d, 7.0047 + 5.00e-8 * d, 29.1241 + 1.01444e-5 * d,
            0.387098, 0.205635 + 5.59e-10 * d, 168.6562 + 4.0923344368 * d, R.drawable.mercury_icon);

        // Venus
        addSchlyterPlanet(dp, "Shukra", "Venus", d, ayanamsa, xs, ys,
            76.6799 + 2.46590e-5 * d, 3.3946 + 2.75e-8 * d, 54.8910 + 1.38374e-5 * d,
            0.723330, 0.006773 - 1.302e-9 * d, 48.0052 + 1.6021302244 * d, R.drawable.venus_icon);

        // Mars
        addSchlyterPlanet(dp, "Mangal", "Mars", d, ayanamsa, xs, ys,
            49.5574 + 2.11081e-5 * d, 1.8497 - 1.78e-8 * d, 286.5016 + 2.92961e-5 * d,
            1.523688, 0.093405 + 2.521e-9 * d, 18.6021 + 0.5240207766 * d, R.drawable.mars_icon);

        // Jupiter
        addSchlyterPlanet(dp, "Guru", "Jupiter", d, ayanamsa, xs, ys,
            100.4542 + 2.76854e-5 * d, 1.3030 - 1.557e-7 * d, 273.8777 + 1.64505e-5 * d,
            5.20256, 0.048498 + 4.469e-9 * d, 19.8950 + 0.0830853001 * d, R.drawable.jupiter_icon);

        // Saturn
        addSchlyterPlanet(dp, "Shani", "Saturn", d, ayanamsa, xs, ys,
            113.6634 + 2.38980e-5 * d, 2.4886 - 1.081e-7 * d, 339.3939 + 2.97661e-5 * d,
            9.55475, 0.055546 - 9.499e-9 * d, 316.9670 + 0.0334442282 * d, R.drawable.saturn_icon);

        // Rahu & Ketu (Mean Nodes)
        double rahuMean = normalizeDegrees(125.04452 - 1934.136261 * T + 0.0020708 * T * T);
        double rahuNirayana = normalizeDegrees(rahuMean - ayanamsa);
        double ketuNirayana = normalizeDegrees(rahuNirayana + 180.0);
        addPlanet(dp, "Rahu", "North Node", rahuNirayana, rahuMean, true, R.drawable.rahu_icon);
        addPlanet(dp, "Ketu", "South Node", ketuNirayana, normalizeDegrees(rahuMean + 180.0), true, R.drawable.ketu_icon);

        // Uranus (Arun / Harshal)
        addSchlyterPlanet(dp, "Arun", "Uranus", d, ayanamsa, xs, ys,
            74.0005 + 1.3978e-5 * d, 0.7733 + 1.9e-8 * d, 96.6612 + 3.0565e-5 * d,
            19.18171 - 1.55e-8 * d, 0.047318 + 7.45e-9 * d, 142.5905 + 0.011725806 * d, R.drawable.uranus_icon);

        // Neptune (Varun)
        addSchlyterPlanet(dp, "Varun", "Neptune", d, ayanamsa, xs, ys,
            131.7806 + 3.0173e-5 * d, 1.7700 - 2.55e-7 * d, 272.8461 - 6.027e-6 * d,
            30.05826 + 3.313e-8 * d, 0.008606 + 2.15e-9 * d, 260.2471 + 0.005995147 * d, R.drawable.neptune_icon);

        // Pluto (Yama)
        double plutoMeanLon = normalizeDegrees(238.95 + 0.003968789 * d);
        double plutoNirayana = normalizeDegrees(plutoMeanLon - ayanamsa);
        addPlanet(dp, "Yama", "Pluto", plutoNirayana, plutoMeanLon, false, R.drawable.pluto_icon);
    }

    public static DayPanchang calculateHoroscope(int year, int month, int day, int hour, int minute, double offsetHours, double lat, double lon, String title) {
        DayPanchang dp = new DayPanchang();
        dp.year = year;
        dp.month = month;
        dp.day = day;

        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day);
        dp.dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        String[] vaarNames = { "Ravivara", "Somavara", "Mangalavara", "Budhavara", "Guruvara", "Shukravara", "Shanivara" };
        dp.vaarName = vaarNames[dp.dayOfWeek - 1];

        double[] sunTimes = calculateSunTimes(year, month, day, lat, lon);
        dp.sunriseHour = sunTimes[0];
        dp.sunsetHour = sunTimes[1];
        dp.sunriseStr = formatTimeAmPm(dp.sunriseHour);
        dp.sunsetStr = formatTimeAmPm(dp.sunsetHour);

        double dayLen = dp.sunsetHour - dp.sunriseHour;
        if (dayLen < 0) dayLen += 24.0;
        double nightLen = 24.0 - dayLen;
        dp.dayLengthStr = formatDuration(dayLen);
        dp.nightLengthStr = formatDuration(nightLen);

        double birthHourUtc = (hour + (minute / 60.0)) - offsetHours;
        double jd = julianDay(year, month, day, birthHourUtc);
        double ayanamsa = lahiriAyanamsa(jd);

        double sunLon = sunLongitude(jd);
        double moonLon = moonLongitude(jd);
        double nirayanaSun = normalizeDegrees(sunLon - ayanamsa);
        double nirayanaMoon = normalizeDegrees(moonLon - ayanamsa);

        double diff = normalizeDegrees(moonLon - sunLon);
        int tithiIdx = (int)(diff / 12.0) + 1;
        if (tithiIdx > 30) tithiIdx = 30;
        if (tithiIdx < 1) tithiIdx = 1;
        dp.tithiIndex = tithiIdx;
        dp.paksha = (tithiIdx <= 15) ? "Shukla" : "Krishna";
        dp.tithiName = TITHI_NAMES[tithiIdx - 1];

        int nakIdx = (int)(nirayanaMoon / (360.0 / 27.0));
        dp.nakshatraIndex = nakIdx;
        dp.nakshatra = NAKSHATRAS[nakIdx % 27];

        int pada = (int)((nirayanaMoon % (360.0 / 27.0)) / (360.0 / 108.0)) + 1;
        dp.nakshatraPada = pada;

        int yogaIdx = (int)(normalizeDegrees(nirayanaSun + nirayanaMoon) / (360.0 / 27.0));
        dp.yoga = YOGAS[yogaIdx % 27];

        int karanaIdx = (int)(diff / 6.0);
        dp.karana1 = getKaranaName(karanaIdx);
        dp.karana2 = getKaranaName(karanaIdx + 1);

        int rashiIdx = (int)(nirayanaMoon / 30.0);
        dp.chandraRashiIndex = rashiIdx;
        dp.chandraRashi = RASHIS[rashiIdx % 12];
        dp.chandraRashiFull = dp.chandraRashi + " (" + RASHIS_ENGLISH[rashiIdx % 12] + ")";

        int sunRashiIdx = (int)(nirayanaSun / 30.0);
        dp.suryaRashiIndex = sunRashiIdx;
        dp.suryaRashi = RASHIS[sunRashiIdx % 12];
        dp.suryaRashiFull = dp.suryaRashi + " (" + RASHIS_ENGLISH[sunRashiIdx % 12] + ")";

        // Samvat
        dp.vikramSamvat = year + 57;
        dp.sakaSamvat = year - 78;
        dp.gujaratiSamvat = dp.vikramSamvat;
        dp.kaliSamvat = year + 3101;
        int southIdx = Math.abs((dp.sakaSamvat + 11) % 60);
        int northIdx = Math.abs((dp.vikramSamvat + 9) % 60);
        dp.samvatsaraSouth = SAMVATSARAS[southIdx];
        dp.samvatsaraNorth = SAMVATSARAS[northIdx];
        dp.samvatsara = dp.samvatsaraSouth;
        dp.saurMasa = RASHIS[dp.suryaRashiIndex % 12];

        // Exact planetary and Lagna positions at birth moment
        calculatePlanets(dp, jd, ayanamsa, lat, lon);
        calculateAvakahada(dp);
        checkMangalDosha(dp);

        return dp;
    }


    private static void addSchlyterPlanet(DayPanchang dp, String sanskrit, String english, double d, double ayanamsa,
                                          double xs, double ys, double N, double i, double w, double a, double e, double M, int iconRes) {
        N = normalizeDegrees(N);
        w = normalizeDegrees(w);
        M = normalizeDegrees(M);
        double eRad = solveKepler(M, e);
        double xv = a * (Math.cos(eRad) - e);
        double yv = a * (Math.sqrt(1.0 - e * e) * Math.sin(eRad));
        double v = Math.toDegrees(Math.atan2(yv, xv));
        double r = Math.sqrt(xv * xv + yv * yv);

        double xh = r * (Math.cos(Math.toRadians(N)) * Math.cos(Math.toRadians(v + w)) - Math.sin(Math.toRadians(N)) * Math.sin(Math.toRadians(v + w)) * Math.cos(Math.toRadians(i)));
        double yh = r * (Math.sin(Math.toRadians(N)) * Math.cos(Math.toRadians(v + w)) + Math.cos(Math.toRadians(N)) * Math.sin(Math.toRadians(v + w)) * Math.cos(Math.toRadians(i)));
        double zh = r * (Math.sin(Math.toRadians(v + w)) * Math.sin(Math.toRadians(i)));

        double xg = xh + xs;
        double yg = yh + ys;
        double sayana = normalizeDegrees(Math.toDegrees(Math.atan2(yg, xg)));
        double nirayana = normalizeDegrees(sayana - ayanamsa);

        addPlanet(dp, sanskrit, english, nirayana, sayana, false, iconRes);
    }

    private static void addPlanet(DayPanchang dp, String sanskrit, String english, double nirayana, double sayana, boolean isRetrograde, int iconRes) {
        PlanetPosition p = new PlanetPosition();
        p.name = english;
        p.sanskritName = sanskrit;
        p.longitude = nirayana;
        p.sayanaLongitude = sayana;
        p.isRetrograde = isRetrograde;
        p.iconResId = iconRes;

        p.rashiIndex = (int)(nirayana / 30.0) % 12;
        p.rashiName = RASHIS[p.rashiIndex];
        p.rashiSymbol = RASHI_SYMBOLS[p.rashiIndex];
        p.degreesInRashi = nirayana % 30.0;

        p.nakshatraIndex = (int)(nirayana / (360.0 / 27.0)) % 27;
        p.nakshatraName = NAKSHATRAS[p.nakshatraIndex];
        p.pada = ((int)(nirayana / (360.0 / 108.0)) % 4) + 1;
        p.padaSyllable = getPadaSyllable(p.nakshatraIndex, p.pada);

        dp.planets.add(p);
    }

    public static String getPadaSyllable(int nakIdx, int pada) {
        if (nakIdx < 0 || nakIdx >= NAKSHATRA_SYLLABLES.length) return "";
        String[] syls = NAKSHATRA_SYLLABLES[nakIdx].split(",");
        if (pada >= 1 && pada <= syls.length) return syls[pada - 1].trim();
        return "";
    }

    private static void calculateChoghadiya(DayPanchang dp, double currentHour) {
        dp.dayChoghadiya.clear();
        dp.nightChoghadiya.clear();

        double daySpan = dp.sunsetHour - dp.sunriseHour;
        if (daySpan < 0) daySpan += 24.0;
        double partDay = daySpan / 8.0;

        double nightSpan = 24.0 - daySpan;
        double partNight = nightSpan / 8.0;

        int startDayIdx = DAY_CHOGHADIYA_START[dp.dayOfWeek - 1];
        int startNightIdx = NIGHT_CHOGHADIYA_START[dp.dayOfWeek - 1];

        // 8 Day periods
        for (int i = 0; i < 8; i++) {
            int typeIdx = (startDayIdx + i) % 7;
            ChoghadiyaPeriod cp = new ChoghadiyaPeriod();
            cp.name = CHOGHADIYA_TYPES[typeIdx];
            cp.nature = CHOGHADIYA_NATURE[typeIdx];
            cp.lord = CHOGHADIYA_LORDS[typeIdx];
            cp.isAuspicious = cp.name.equals("Amrit") || cp.name.equals("Shubh") || cp.name.equals("Labh") || cp.name.equals("Char");
            cp.startHour = (dp.sunriseHour + i * partDay) % 24.0;
            cp.endHour = (dp.sunriseHour + (i + 1) * partDay) % 24.0;
            cp.startTimeStr = formatTimeFromDecimal(cp.startHour);
            cp.endTimeStr = formatTimeFromDecimal(cp.endHour);

            if (currentHour >= cp.startHour && currentHour < cp.endHour) {
                cp.isCurrent = true;
                dp.activeChoghadiya = cp;
            }
            dp.dayChoghadiya.add(cp);
        }

        // 8 Night periods
        for (int i = 0; i < 8; i++) {
            int typeIdx = (startNightIdx + i) % 7;
            ChoghadiyaPeriod cp = new ChoghadiyaPeriod();
            cp.name = CHOGHADIYA_TYPES[typeIdx];
            cp.nature = CHOGHADIYA_NATURE[typeIdx];
            cp.lord = CHOGHADIYA_LORDS[typeIdx];
            cp.isAuspicious = cp.name.equals("Amrit") || cp.name.equals("Shubh") || cp.name.equals("Labh") || cp.name.equals("Char");
            cp.startHour = (dp.sunsetHour + i * partNight) % 24.0;
            cp.endHour = (dp.sunsetHour + (i + 1) * partNight) % 24.0;
            cp.startTimeStr = formatTimeFromDecimal(cp.startHour);
            cp.endTimeStr = formatTimeFromDecimal(cp.endHour);

            if (currentHour >= cp.startHour && currentHour < cp.endHour) {
                cp.isCurrent = true;
                dp.activeChoghadiya = cp;
            }
            dp.nightChoghadiya.add(cp);
        }

        if (dp.activeChoghadiya == null && !dp.dayChoghadiya.isEmpty()) {
            dp.activeChoghadiya = dp.dayChoghadiya.get(0);
        }
    }

    private static void calculateMuhurtas(DayPanchang dp) {
        double daySpan = dp.sunsetHour - dp.sunriseHour;
        if (daySpan < 0) daySpan += 24.0;
        double part = daySpan / 8.0;

        // Rahu Kaal Part (1..8)
        int[] rahuParts = { 8, 2, 7, 5, 6, 4, 3 }; // Sun to Sat
        int rPart = rahuParts[dp.dayOfWeek - 1] - 1;
        dp.muhurta.rahuKaal = formatTimeFromDecimal(dp.sunriseHour + rPart * part) + " - " + formatTimeFromDecimal(dp.sunriseHour + (rPart + 1) * part);

        // Yamagand
        int[] yamaParts = { 5, 4, 3, 2, 1, 7, 6 };
        int yPart = yamaParts[dp.dayOfWeek - 1] - 1;
        dp.muhurta.yamagand = formatTimeFromDecimal(dp.sunriseHour + yPart * part) + " - " + formatTimeFromDecimal(dp.sunriseHour + (yPart + 1) * part);

        // Gulika Kaal
        int[] gulikaParts = { 7, 6, 5, 4, 3, 2, 1 };
        int gPart = gulikaParts[dp.dayOfWeek - 1] - 1;
        dp.muhurta.gulika = formatTimeFromDecimal(dp.sunriseHour + gPart * part) + " - " + formatTimeFromDecimal(dp.sunriseHour + (gPart + 1) * part);

        // Abhijit Muhurta (Mid-day 8th muhurta of 15 muhurtas = 48 minutes around solar noon)
        double solarNoon = dp.sunriseHour + daySpan / 2.0;
        dp.muhurta.abhijit = formatTimeFromDecimal(solarNoon - 0.4) + " - " + formatTimeFromDecimal(solarNoon + 0.4);

        // Brahma Muhurta (approx 1 hr 36 min to 48 min before sunrise)
        dp.muhurta.brahmaMuhurta = formatTimeFromDecimal(dp.sunriseHour - 1.6) + " - " + formatTimeFromDecimal(dp.sunriseHour - 0.8);

        // Amrit Kaal
        dp.muhurta.amritKaal = formatTimeFromDecimal((dp.sunriseHour + 2.5) % 24.0) + " - " + formatTimeFromDecimal((dp.sunriseHour + 4.2) % 24.0);

        // Pradosh Kaal (48 minutes before to 48 minutes after sunset)
        dp.muhurta.pradoshKaal = formatTimeFromDecimal(dp.sunsetHour - 0.8) + " - " + formatTimeFromDecimal(dp.sunsetHour + 0.8);

        // Vijaya Muhurta (11th Muhurta of 15 daytime muhurtas)
        double muhurtaDuration = daySpan / 15.0;
        double vijayaStart = dp.sunriseHour + 10.0 * muhurtaDuration;
        double vijayaEnd = dp.sunriseHour + 11.0 * muhurtaDuration;
        dp.muhurta.vijayaMuhurta = formatTimeFromDecimal(vijayaStart) + " - " + formatTimeFromDecimal(vijayaEnd);

        // Godhuli Muhurta (24 minutes before to 24 minutes after sunset)
        dp.muhurta.godhuliMuhurta = formatTimeFromDecimal(dp.sunsetHour - 0.4) + " - " + formatTimeFromDecimal(dp.sunsetHour + 0.4);

        // Nishita Kaal (Midnight Kaal)
        double nightSpan = 24.0 - daySpan;
        double midnight = (dp.sunsetHour + nightSpan / 2.0) % 24.0;
        dp.muhurta.nishitaKaal = formatTimeFromDecimal(midnight - 0.4) + " - " + formatTimeFromDecimal(midnight + 0.4);
    }

    private static void calculateAvakahada(DayPanchang dp) {
        dp.avakahada.rashiSwami = RASHI_LORDS[dp.chandraRashiIndex];
        dp.avakahada.nakshatraSwami = NAKSHATRA_LORDS[dp.nakshatraIndex];

        // Paya based on Nakshatra
        int n = dp.nakshatraIndex;
        if (n == 0 || n == 9 || n == 18) dp.avakahada.paya = "Gold (Swarna)";
        else if (n == 2 || n == 11 || n == 20) dp.avakahada.paya = "Copper (Tamra)";
        else if (n % 2 == 0) dp.avakahada.paya = "Silver (Rajat)";
        else dp.avakahada.paya = "Iron (Loha)";

        // Yoni, Gana, Nadi
        String[] yonis = { "Ashwa (Horse)", "Gaja (Elephant)", "Mesha (Ram)", "Sarpa (Serpent)", "Shwana (Dog)", "Marjara (Cat)", "Mushaka (Rat)", "Gau (Cow)", "Vyaghra (Tiger)", "Mriga (Deer)", "Vanara (Monkey)", "Nakula (Mongoose)", "Simha (Lion)" };
        dp.avakahada.yoni = yonis[n % yonis.length];

        String[] ganas = { "Deva", "Manushya", "Rakshasa" };
        dp.avakahada.gana = ganas[n % 3];

        String[] nadis = { "Adi (Vata)", "Madhya (Pitta)", "Antya (Kapha)" };
        dp.avakahada.nadi = nadis[n % 3];

        String[] varnas = { "Brahmin", "Kshatriya", "Vaishya", "Shudra" };
        dp.avakahada.varna = varnas[dp.chandraRashiIndex % 4];

        dp.avakahada.vashya = "Chatushpada";
        dp.avakahada.janmaAkshara = getPadaSyllable(dp.nakshatraIndex, dp.nakshatraPada);
    }

    private static void checkMangalDosha(DayPanchang dp) {
        if (dp.planets == null || dp.planets.size() < 6) {
            dp.hasMangalDosha = false;
            dp.kujaDoshaStatus = "No Manglik Dosha (Kuja Shuddha)";
            dp.vimshottariDashaBalance = "None";
            return;
        }

        PlanetPosition lagna = dp.planets.get(0);
        PlanetPosition chandra = dp.planets.get(2);
        PlanetPosition mangal = dp.planets.get(5);

        int houseLagna = (mangal.rashiIndex - lagna.rashiIndex + 12) % 12 + 1;
        int houseMoon = (mangal.rashiIndex - chandra.rashiIndex + 12) % 12 + 1;

        boolean isLagnaManglik = (houseLagna == 1 || houseLagna == 4 || houseLagna == 7 || houseLagna == 8 || houseLagna == 12);
        boolean isMoonManglik = (houseMoon == 1 || houseMoon == 4 || houseMoon == 7 || houseMoon == 8 || houseMoon == 12);

        dp.hasMangalDosha = isLagnaManglik || isMoonManglik;

        if (isLagnaManglik && isMoonManglik) {
            dp.kujaDoshaStatus = "Poorna Manglik (Mars in H" + houseLagna + " from Lagna, H" + houseMoon + " from Moon)";
        } else if (isLagnaManglik) {
            dp.kujaDoshaStatus = "Lagna Manglik (Mars in H" + houseLagna + " from Lagna)";
        } else if (isMoonManglik) {
            dp.kujaDoshaStatus = "Chandra Manglik (Mars in H" + houseMoon + " from Moon)";
        } else {
            dp.kujaDoshaStatus = "No Manglik Dosha (Kuja Shuddha)";
        }

        // Exact Vimshottari Mahadasha balance calculation at birth
        dp.vimshottariDashaBalance = calculateVimshottariDashaBalance(chandra.longitude);
    }

    public static String calculateVimshottariDashaBalance(double moonLongitude) {
        double nakSpan = 360.0 / 27.0;
        int nakIdx = (int) (moonLongitude / nakSpan);
        if (nakIdx < 0) nakIdx = 0;
        if (nakIdx > 26) nakIdx = 26;
        double degreesInNak = moonLongitude - (nakIdx * nakSpan);
        double fractionTraversed = degreesInNak / nakSpan;
        if (fractionTraversed < 0) fractionTraversed = 0;
        if (fractionTraversed > 1.0) fractionTraversed = 1.0;
        double remainingFraction = 1.0 - fractionTraversed;

        String[] lords = { "Ketu", "Shukra", "Surya", "Chandra", "Mangal", "Rahu", "Guru", "Shani", "Budha" };
        double[] totalYears = { 7.0, 20.0, 6.0, 10.0, 7.0, 18.0, 16.0, 19.0, 17.0 };

        int lordIdx = nakIdx % 9;
        String lord = lords[lordIdx];
        double remYearsTotal = totalYears[lordIdx] * remainingFraction;

        int years = (int) remYearsTotal;
        double remMonthsTotal = (remYearsTotal - years) * 12.0;
        int months = (int) remMonthsTotal;
        int days = (int) Math.round((remMonthsTotal - months) * 30.0);
        if (days >= 30) {
            months += 1;
            days -= 30;
        }
        if (months >= 12) {
            years += 1;
            months -= 12;
        }

        return lord + " Mahadasha: " + years + " Yrs " + months + " Mos " + days + " Days";
    }

    private static void calculateOccasion(DayPanchang dp) {
        int mIdx = dp.hinduMonthIndex;
        int tIdx = dp.tithiIndex;
        int tNum = (tIdx <= 15) ? tIdx : tIdx - 15;
        boolean isShukla = (tIdx <= 15);

        dp.specialOccasion = "";
        dp.specialOccasionFull = "";

        // Solar / Gregorian
        if (dp.month == 1 && dp.day == 14) {
            dp.specialOccasion = "Makar Sankranti";
            dp.specialOccasionFull = "Makar Sankranti / Pongal";
            return;
        }

        // Ashwin Month (Sharad Navratri / Dussehra / Sharad Purnima)
        if (mIdx == 6) { // Ashwin
            if (isShukla) {
                if (tNum == 1) { dp.specialOccasion = "Ghatasthapana"; dp.specialOccasionFull = "Sharad Navratri - Day 1 (Ghatasthapana)"; return; }
                if (tNum == 2) { dp.specialOccasion = "Brahmacharini"; dp.specialOccasionFull = "Sharad Navratri - Day 2 (Brahmacharini)"; return; }
                if (tNum == 3) { dp.specialOccasion = "Chandraghanta"; dp.specialOccasionFull = "Sharad Navratri - Day 3 (Chandraghanta)"; return; }
                if (tNum == 4) { dp.specialOccasion = "Kushmanda"; dp.specialOccasionFull = "Sharad Navratri - Day 4 (Kushmanda)"; return; }
                if (tNum == 5) { dp.specialOccasion = "Skandamata"; dp.specialOccasionFull = "Sharad Navratri - Day 5 (Skandamata)"; return; }
                if (tNum == 6) { dp.specialOccasion = "Katyayani"; dp.specialOccasionFull = "Sharad Navratri - Day 6 (Katyayani)"; return; }
                if (tNum == 7) { dp.specialOccasion = "Maha Saptami"; dp.specialOccasionFull = "Sharad Navratri - Day 7 (Maha Saptami)"; return; }
                if (tNum == 8) { dp.specialOccasion = "Durga Ashtami"; dp.specialOccasionFull = "Sharad Navratri - Day 8 (Maha Ashtami)"; return; }
                if (tNum == 9) { dp.specialOccasion = "Maha Navami"; dp.specialOccasionFull = "Sharad Navratri - Day 9 (Maha Navami)"; return; }
                if (tNum == 10) { dp.specialOccasion = "Dussehra"; dp.specialOccasionFull = "Vijayadashami (Dussehra)"; return; }
                if (tNum == 11) { dp.specialOccasion = "Papankusha Ekadashi"; dp.specialOccasionFull = "Papankusha Ekadashi Vrat"; return; }
                if (tNum == 15) { dp.specialOccasion = "Sharad Purnima"; dp.specialOccasionFull = "Sharad Purnima (Kojagiri)"; return; }
            } else {
                if (tNum == 4) { dp.specialOccasion = "Karwa Chauth"; dp.specialOccasionFull = "Karwa Chauth (Karak Chaturthi)"; return; }
                if (tNum == 8) { dp.specialOccasion = "Ahoi Ashtami"; dp.specialOccasionFull = "Ahoi Ashtami Vrat"; return; }
                if (tNum == 11) { dp.specialOccasion = "Rama Ekadashi"; dp.specialOccasionFull = "Rama Ekadashi Vrat"; return; }
                if (tNum == 13) { dp.specialOccasion = "Dhanteras"; dp.specialOccasionFull = "Dhanteras (Dhanvantari Jayanti)"; return; }
                if (tNum == 14) { dp.specialOccasion = "Chhoti Diwali"; dp.specialOccasionFull = "Narak Chaturdashi (Chhoti Diwali)"; return; }
                if (tNum == 15) { dp.specialOccasion = "Diwali"; dp.specialOccasionFull = "Diwali (Lakshmi Puja)"; return; }
            }
        }

        // Kartik Month
        if (mIdx == 7) {
            if (isShukla) {
                if (tNum == 1) { dp.specialOccasion = "Govardhan"; dp.specialOccasionFull = "Govardhan Puja (Annakut)"; return; }
                if (tNum == 2) { dp.specialOccasion = "Bhai Dooj"; dp.specialOccasionFull = "Bhai Dooj (Yama Dwitiya)"; return; }
                if (tNum == 6) { dp.specialOccasion = "Chhath Puja"; dp.specialOccasionFull = "Surya Shashthi (Chhath Puja)"; return; }
                if (tNum == 11) { dp.specialOccasion = "Devutthana Ek"; dp.specialOccasionFull = "Devutthana Ekadashi (Tulsi Vivah)"; return; }
                if (tNum == 15) { dp.specialOccasion = "Kartik Purnima"; dp.specialOccasionFull = "Kartik Purnima (Dev Diwali)"; return; }
            }
        }

        // General Ekadashis and Tithis
        if (tNum == 11) {
            dp.specialOccasion = isShukla ? "Shukla Ekadashi" : "Krishna Ekadashi";
            dp.specialOccasionFull = dp.specialOccasion + " Vrat";
        } else if (tNum == 13) {
            dp.specialOccasion = "Pradosh Vrat";
            dp.specialOccasionFull = "Pradosh Vrat (" + (isShukla ? "Shukla" : "Krishna") + ")";
        } else if (tIdx == 15) {
            dp.specialOccasion = "Purnima";
            dp.specialOccasionFull = dp.hinduMonth + " Purnima";
        } else if (tIdx == 30) {
            dp.specialOccasion = "Amavasya";
            dp.specialOccasionFull = dp.hinduMonth + " Amavasya";
        }
    }

    public static int getMoonDrawableResId(int tithiIdx) {
        switch (tithiIdx) {
            case 1: return R.drawable.moon_real_01;
            case 2: return R.drawable.moon_real_02;
            case 3: return R.drawable.moon_real_03;
            case 4: return R.drawable.moon_real_04;
            case 5: return R.drawable.moon_real_05;
            case 6: return R.drawable.moon_real_06;
            case 7: return R.drawable.moon_real_07;
            case 8: return R.drawable.moon_real_08;
            case 9: return R.drawable.moon_real_09;
            case 10: return R.drawable.moon_real_10;
            case 11: return R.drawable.moon_real_11;
            case 12: return R.drawable.moon_real_12;
            case 13: return R.drawable.moon_real_13;
            case 14: return R.drawable.moon_real_14;
            case 15: return R.drawable.moon_real_15;
            case 16: return R.drawable.moon_real_16;
            case 17: return R.drawable.moon_real_17;
            case 18: return R.drawable.moon_real_18;
            case 19: return R.drawable.moon_real_19;
            case 20: return R.drawable.moon_real_20;
            case 21: return R.drawable.moon_real_21;
            case 22: return R.drawable.moon_real_22;
            case 23: return R.drawable.moon_real_23;
            case 24: return R.drawable.moon_real_24;
            case 25: return R.drawable.moon_real_25;
            case 26: return R.drawable.moon_real_26;
            case 27: return R.drawable.moon_real_27;
            case 28: return R.drawable.moon_real_28;
            case 29: return R.drawable.moon_real_29;
            case 30: return R.drawable.moon_real_30;
            default: return R.drawable.moon_real_15;
        }
    }

    public static final String[] HORA_PLANETS_SANSKRIT = {
        "Surya", "Shukra", "Budh", "Chandra", "Shani", "Guru", "Mangal"
    };

    public static final String[] HORA_PLANETS_ENGLISH = {
        "Sun", "Venus", "Mercury", "Moon", "Saturn", "Jupiter", "Mars"
    };

    public static final String[] HORA_SIGNIFICANCE = {
        "Leadership, authority, government matters",
        "Arts, music, romance, beauty, vehicles",
        "Business, accounts, education, intellect",
        "Travel, personal well-being, creative tasks",
        "Discipline, labor, mining, agriculture",
        "Wisdom, spirituality, finance, ceremonies",
        "Courage, land property, sports, disputes"
    };

    public static final String[] HORA_NATURE = {
        "Moderate", "Auspicious", "Auspicious", "Auspicious", "Sluggish", "Highly Auspicious", "Aggressive"
    };

    public static final int[] HORA_ICONS = {
        R.drawable.sun_icon, R.drawable.venus_icon, R.drawable.mercury_icon, R.drawable.moon_icon,
        R.drawable.saturn_icon, R.drawable.jupiter_icon, R.drawable.mars_icon
    };

    public static final int[] DAY_HORA_START = { 0, 3, 6, 2, 5, 1, 4 }; // Sun..Sat

    private static void calculateHora(DayPanchang dp, double currentHour) {
        dp.dayHoras.clear();
        dp.nightHoras.clear();

        double daySpan = dp.sunsetHour - dp.sunriseHour;
        if (daySpan < 0) daySpan += 24.0;
        double horaDay = daySpan / 12.0;

        double nightSpan = 24.0 - daySpan;
        double horaNight = nightSpan / 12.0;

        int startIdx = DAY_HORA_START[dp.dayOfWeek - 1];

        // 12 Day Horas
        for (int i = 0; i < 12; i++) {
            int pIdx = (startIdx + i) % 7;
            HoraPeriod hp = new HoraPeriod();
            hp.hourNumber = i + 1;
            hp.planetSanskrit = HORA_PLANETS_SANSKRIT[pIdx];
            hp.planetEnglish = HORA_PLANETS_ENGLISH[pIdx];
            hp.lord = hp.planetSanskrit + " (" + hp.planetEnglish + ")";
            hp.isDayHora = true;
            hp.nature = HORA_NATURE[pIdx];
            hp.iconResId = HORA_ICONS[pIdx];
            hp.startHour = (dp.sunriseHour + i * horaDay) % 24.0;
            hp.endHour = (dp.sunriseHour + (i + 1) * horaDay) % 24.0;
            hp.startTimeStr = formatTimeFromDecimal(hp.startHour);
            hp.endTimeStr = formatTimeFromDecimal(hp.endHour);

            if (currentHour >= hp.startHour && currentHour < hp.endHour) {
                hp.isCurrent = true;
                dp.activeHora = hp;
            }
            dp.dayHoras.add(hp);
        }

        // 12 Night Horas
        for (int i = 0; i < 12; i++) {
            int pIdx = (startIdx + 12 + i) % 7;
            HoraPeriod hp = new HoraPeriod();
            hp.hourNumber = i + 13;
            hp.planetSanskrit = HORA_PLANETS_SANSKRIT[pIdx];
            hp.planetEnglish = HORA_PLANETS_ENGLISH[pIdx];
            hp.lord = hp.planetSanskrit + " (" + hp.planetEnglish + ")";
            hp.isDayHora = false;
            hp.nature = HORA_NATURE[pIdx];
            hp.iconResId = HORA_ICONS[pIdx];
            hp.startHour = (dp.sunsetHour + i * horaNight) % 24.0;
            hp.endHour = (dp.sunsetHour + (i + 1) * horaNight) % 24.0;
            hp.startTimeStr = formatTimeFromDecimal(hp.startHour);
            hp.endTimeStr = formatTimeFromDecimal(hp.endHour);

            if (currentHour >= hp.startHour && currentHour < hp.endHour) {
                hp.isCurrent = true;
                dp.activeHora = hp;
            }
            dp.nightHoras.add(hp);
        }

        if (dp.activeHora == null && !dp.dayHoras.isEmpty()) {
            dp.activeHora = dp.dayHoras.get(0);
        }
    }

    public static GunaMilanResult calculateGunaMilan(int groomRashi, int groomNak, int brideRashi, int brideNak) {
        GunaMilanResult res = new GunaMilanResult();

        // 1. Varna (1 pt): Brahmin=3, Kshatriya=2, Vaishya=1, Shudra=0
        int[] rashiVarna = { 2, 1, 0, 3, 2, 1, 0, 3, 2, 1, 0, 3 }; // Mesha..Meena
        int gVarna = rashiVarna[groomRashi % 12];
        int bVarna = rashiVarna[brideRashi % 12];
        res.varnaPoints = (gVarna >= bVarna) ? 1 : 0;

        // 2. Vashya (2 pts)
        int[] rashiVashya = { 0, 0, 1, 2, 3, 1, 1, 4, 0, 0, 1, 2 };
        int gVashya = rashiVashya[groomRashi % 12];
        int bVashya = rashiVashya[brideRashi % 12];
        if (gVashya == bVashya) {
            res.vashyaPoints = 2;
        } else if ((gVashya == 1 && bVashya == 0) || (gVashya == 0 && bVashya == 1)) {
            res.vashyaPoints = 1;
        } else {
            res.vashyaPoints = 0;
        }

        // 3. Tara (3 pts)
        int d1 = ((groomNak - brideNak + 27) % 9) + 1;
        int d2 = ((brideNak - groomNak + 27) % 9) + 1;
        boolean d1Good = (d1 != 3 && d1 != 5 && d1 != 7);
        boolean d2Good = (d2 != 3 && d2 != 5 && d2 != 7);
        if (d1Good && d2Good) {
            res.taraPoints = 3;
        } else if (d1Good || d2Good) {
            res.taraPoints = 2;
        } else {
            res.taraPoints = 0;
        }

        // 4. Yoni (4 pts)
        int[] nakYoni = { 0, 1, 2, 3, 3, 4, 5, 2, 5, 6, 6, 7, 8, 9, 8, 9, 10, 10, 4, 11, 12, 11, 13, 0, 13, 7, 1 };
        int gYoni = nakYoni[groomNak % 27];
        int bYoni = nakYoni[brideNak % 27];
        if (gYoni == bYoni) {
            res.yoniPoints = 4;
        } else if (Math.abs(gYoni - bYoni) <= 2) {
            res.yoniPoints = 3;
        } else if (Math.abs(gYoni - bYoni) <= 5) {
            res.yoniPoints = 2;
        } else {
            res.yoniPoints = 1;
        }

        // 5. Graha Maitri (5 pts)
        int[] rashiLords = { 2, 5, 3, 1, 0, 3, 5, 2, 4, 6, 6, 4 }; // 0:Sun, 1:Moon, 2:Mars, 3:Merc, 4:Jup, 5:Ven, 6:Sat
        int gLord = rashiLords[groomRashi % 12];
        int bLord = rashiLords[brideRashi % 12];
        if (gLord == bLord) {
            res.grahaMaitriPoints = 5;
        } else {
            int[][] friendMatrix = {
                { 5, 5, 5, 4, 5, 1, 0 },
                { 5, 5, 4, 5, 4, 4, 3 },
                { 5, 4, 5, 1, 5, 4, 3 },
                { 4, 3, 1, 5, 4, 5, 4 },
                { 5, 5, 5, 1, 5, 1, 3 },
                { 1, 3, 4, 5, 1, 5, 5 },
                { 0, 1, 1, 5, 3, 5, 5 }
            };
            res.grahaMaitriPoints = friendMatrix[gLord][bLord];
        }

        // 6. Gana (6 pts): Deva=0, Manushya=1, Rakshasa=2
        int[] nakGana = { 0, 1, 2, 1, 0, 1, 0, 0, 2, 2, 1, 1, 0, 2, 0, 2, 0, 2, 2, 1, 1, 0, 2, 2, 1, 1, 0 };
        int gGana = nakGana[groomNak % 27];
        int bGana = nakGana[brideNak % 27];
        if (gGana == bGana) {
            res.ganaPoints = 6;
        } else if ((gGana == 0 && bGana == 1) || (gGana == 1 && bGana == 0)) {
            res.ganaPoints = 5;
        } else if (gGana == 0 && bGana == 2) {
            res.ganaPoints = 1;
        } else {
            res.ganaPoints = 0;
        }

        // 7. Bhakoot (7 pts)
        int dist = ((groomRashi - brideRashi + 12) % 12) + 1;
        if (dist == 1 || dist == 7 || dist == 3 || dist == 11 || dist == 4 || dist == 10) {
            res.bhakootPoints = 7;
            res.hasBhakootDosha = false;
        } else {
            if (gLord == bLord) {
                res.bhakootPoints = 7;
                res.hasBhakootDosha = false;
            } else {
                res.bhakootPoints = 0;
                res.hasBhakootDosha = true;
            }
        }

        // 8. Nadi (8 pts): 0: Adi, 1: Madhya, 2: Antya
        int[] nakNadi = { 0, 1, 2, 2, 1, 0, 0, 1, 2, 2, 1, 0, 0, 1, 2, 2, 1, 0, 0, 1, 2, 2, 1, 0, 0, 1, 2 };
        int gNadi = nakNadi[groomNak % 27];
        int bNadi = nakNadi[brideNak % 27];
        if (gNadi != bNadi) {
            res.nadiPoints = 8;
            res.hasNadiDosha = false;
        } else {
            res.nadiPoints = 0;
            res.hasNadiDosha = true;
        }

        res.totalPoints = res.varnaPoints + res.vashyaPoints + res.taraPoints + res.yoniPoints
            + res.grahaMaitriPoints + res.ganaPoints + res.bhakootPoints + res.nadiPoints;

        if (res.totalPoints >= 28) {
            res.verdict = "Uttam (Highly Auspicious Match)";
        } else if (res.totalPoints >= 18) {
            res.verdict = "Madhyam (Good / Acceptable Match)";
        } else {
            res.verdict = "Ashubh (Inauspicious / Not Recommended)";
        }

        res.breakdown = String.format(Locale.US,
            "1. Varna: %d/1 • 2. Vashya: %d/2\n3. Tara: %d/3 • 4. Yoni: %d/4\n5. Graha Maitri: %d/5 • 6. Gana: %d/6\n7. Bhakoot: %d/7 • 8. Nadi: %d/8",
            res.varnaPoints, res.vashyaPoints, res.taraPoints, res.yoniPoints,
            res.grahaMaitriPoints, res.ganaPoints, res.bhakootPoints, res.nadiPoints
        );

        return res;
    }

    public static String getDailySankalpaMantra(DayPanchang dp, String cityName) {
        return "ॐ विष्णुर्विष्णुर्विष्णुः श्रीमद्भगवतो महापुरुषस्य विष्णोराज्ञया प्रवर्तमानस्य अद्य ब्रह्मणो द्वितीयपरार्धे "
            + "श्रीश्वेतवाराहकल्पे वैवस्वतमन्वन्तरे अष्टाविंशतितमे कलियुगे कलिप्रथमचरणे भूर्लोके जम्बूद्वीपे भारतवर्षे भरतखण्डे "
            + dp.vikramSamvat + " विक्रम संवत्सरे, '" + dp.samvatsara + "' नाम संवत्सरे, "
            + dp.ayana + " अयने, " + dp.ritu + " ऋतौ, "
            + dp.hinduMonth + " मासे, " + dp.paksha + " पक्षे, "
            + dp.tithiName + " तिथौ, " + dp.vaarName + " वासरे, "
            + dp.nakshatra + " नक्षत्रे, " + dp.yoga + " योगे, " + dp.karana1 + " करणे, "
            + cityName + " नगरे मम सकल मनोरथ सिद्धये श्री परमेश्वर प्रीत्यर्थं पूजां संकॢप्तवान् करिष्ये।";
    }

    public static String getDailySankalpaEnglish(DayPanchang dp, String cityName) {
        return "Om Vishnu, Vishnu, Vishnu. In the cosmic manifestation of the Supreme Being Vishnu, in the second half of Brahma's lifespan, "
            + "in the Shveta-Varaha Kalpa, in Vaivasvata Manvantara, in the 28th Kaliyuga (first quarter), "
            + "in the holy land of Bharata: During Vikram Samvat " + dp.vikramSamvat + " (" + dp.samvatsara + "), "
            + dp.ayana + ", " + dp.ritu + " Ritu (Season), Month of " + dp.hinduMonth + ", " + dp.paksha + " Paksha, "
            + dp.tithiName + " Tithi, " + dp.vaarName + ", Nakshatra " + dp.nakshatra + ", Yoga " + dp.yoga + ", "
            + "in the sacred city of " + cityName + ": I take this sacred solemn sankalpa for the fulfillment of all righteous aspirations and divine grace.";
    }

    // ==========================================
    // 1. REUSABLE ASCENDANT / LAGNA CALCULATION
    // ==========================================
    public static double calculateAscendant(double jd, double lat, double lon) {
        double T = (jd - 2451545.0) / 36525.0;
        double ayanamsa = lahiriAyanamsa(jd);
        double eps = 23.4392911 - 0.0130042 * T;
        double gmst = 280.46061837 + 360.98564736629 * (jd - 2451545.0) + 0.000387933 * T * T;
        double lmst = normalizeDegrees(gmst + lon);
        double thetaRad = Math.toRadians(lmst);
        double epsRad = Math.toRadians(eps);
        double latRad = Math.toRadians(lat);
        double lagnaSayana = normalizeDegrees(Math.toDegrees(Math.atan2(-Math.cos(thetaRad), Math.sin(thetaRad) * Math.cos(epsRad) + Math.tan(latRad) * Math.sin(epsRad))));
        if (Math.cos(thetaRad) < 0) lagnaSayana = normalizeDegrees(lagnaSayana + 180.0);
        return normalizeDegrees(lagnaSayana - ayanamsa);
    }

    // ==========================================
    // 2. LAGNA TIMINGS OF THE DAY (12 ASCENDANTS)
    // ==========================================
    public static class LagnaSpan {
        public int rashiIndex;
        public String rashiName;
        public String rashiSymbol;
        public String startTimeStr;
        public String endTimeStr;
        public double startHour;
        public double endHour;
        public boolean isCurrent;
    }

    public static List<LagnaSpan> calculateLagnaTable(int year, int month, int day, double lat, double lon) {
        List<LagnaSpan> list = new ArrayList<>();
        DayPanchang dp = calculatePanchang(year, month, day, lat, lon);
        double startHour = dp.sunriseHour;

        Calendar now = Calendar.getInstance();
        double currentHour = now.get(Calendar.HOUR_OF_DAY) + (now.get(Calendar.MINUTE) / 60.0);

        int prevRashi = -1;
        double spanStart = startHour;

        for (int m = 0; m <= 24 * 60; m += 2) {
            double t = startHour + (m / 60.0);
            double utc = t - 5.5; // IST to UTC
            int dayOffset = (int) Math.floor(utc / 24.0);
            double remUtc = (utc % 24.0 + 24.0) % 24.0;
            double jd = julianDay(year, month, day + dayOffset, remUtc);
            double ascDeg = calculateAscendant(jd, lat, lon);
            int rIdx = ((int) (ascDeg / 30.0)) % 12;

            if (rIdx != prevRashi) {
                if (prevRashi != -1) {
                    LagnaSpan span = new LagnaSpan();
                    span.rashiIndex = prevRashi;
                    span.rashiName = RASHIS[prevRashi];
                    span.rashiSymbol = RASHI_SYMBOLS[prevRashi];
                    span.startHour = spanStart;
                    span.endHour = t;
                    span.startTimeStr = formatTimeAmPm(spanStart % 24.0);
                    span.endTimeStr = formatTimeAmPm(t % 24.0);
                    span.isCurrent = (currentHour >= (spanStart % 24.0) && currentHour < (t % 24.0));
                    list.add(span);
                }
                prevRashi = rIdx;
                spanStart = t;
            }
        }
        if (prevRashi != -1 && list.size() < 12) {
            LagnaSpan span = new LagnaSpan();
            span.rashiIndex = prevRashi;
            span.rashiName = RASHIS[prevRashi];
            span.rashiSymbol = RASHI_SYMBOLS[prevRashi];
            span.startHour = spanStart;
            span.endHour = startHour + 24.0;
            span.startTimeStr = formatTimeAmPm(spanStart % 24.0);
            span.endTimeStr = formatTimeAmPm((startHour + 24.0) % 24.0);
            span.isCurrent = (currentHour >= (spanStart % 24.0) || currentHour < ((startHour + 24.0) % 24.0));
            list.add(span);
        }
        return list;
    }

    // ==========================================
    // 3. TARA BALA & CHANDRA BALA
    // ==========================================
    public static class TaraChandraBala {
        public String taraName;
        public String taraGrade;
        public boolean isTaraAuspicious;
        public int chandraHouse;
        public String chandraStatus;
        public boolean isChandraAuspicious;
        public boolean isChandrashtama;
        public String explanation;
    }

    public static TaraChandraBala calculateTaraChandraBala(int janmaNak, int janmaRashi, int todayNak, int todayMoonRashi) {
        TaraChandraBala res = new TaraChandraBala();

        int diff = ((todayNak - janmaNak + 27) % 27) + 1;
        int tIdx = ((diff - 1) % 9) + 1;

        String[] taraNames = { "", "Janma (जन्म)", "Sampat (सम्पत्)", "Vipat (विपत्)", "Kshema (क्षेम)",
                "Pratyari (प्रत्यरि)", "Sadhaka (साधक)", "Vadha (वध)", "Mitra (मित्र)", "Parama Mitra (परम मित्र)" };
        String[] taraGrades = { "", "Not good (Moderate)", "Very good (Auspicious)", "Bad (Avoid critical work)",
                "Good (Favorable)", "Bad (Obstacles)", "Very good (High Success)", "Totally bad (Harmful)",
                "Good (Helpful)", "Very good (Supreme Grace)" };

        res.taraName = taraNames[tIdx];
        res.taraGrade = taraGrades[tIdx];
        res.isTaraAuspicious = (tIdx == 2 || tIdx == 4 || tIdx == 6 || tIdx == 8 || tIdx == 9);

        int cHouse = ((todayMoonRashi - janmaRashi + 12) % 12) + 1;
        res.chandraHouse = cHouse;
        res.isChandrashtama = (cHouse == 8);

        if (cHouse == 1 || cHouse == 3 || cHouse == 6 || cHouse == 7 || cHouse == 10 || cHouse == 11) {
            res.chandraStatus = "Auspicious (शुभ चंद्रबल)";
            res.isChandraAuspicious = true;
        } else if (cHouse == 2 || cHouse == 5 || cHouse == 9) {
            res.chandraStatus = "Moderate / Mixed (मध्यम)";
            res.isChandraAuspicious = true;
        } else if (cHouse == 8) {
            res.chandraStatus = "⚠️ Chandrashtama (चन्द्राष्टमी - Inauspicious)";
            res.isChandraAuspicious = false;
        } else {
            res.chandraStatus = "Inauspicious (अशुभ - House " + cHouse + ")";
            res.isChandraAuspicious = false;
        }

        res.explanation = "Tara: " + res.taraName + " [" + res.taraGrade + "] • Moon in House " + cHouse + " from Janma Rashi [" + res.chandraStatus + "]";
        return res;
    }

    // ==========================================
    // 4. ASHTAKAVARGA ENGINE (PARASHARA BPHS)
    // ==========================================
    public static class AshtakavargaResult {
        public int[][] bhinnaAshtaka = new int[7][12];
        public int[] sarvashtakavarga = new int[12];
        public int totalBindus = 337;
        public int strongestHouse = 0;
        public int weakestHouse = 0;
        public int maxBindus = 0;
        public int minBindus = 99;
    }

    public static AshtakavargaResult calculateAshtakavarga(List<PlanetPosition> planets, int lagnaRashi) {
        AshtakavargaResult res = new AshtakavargaResult();

        int[] pos = new int[8];
        for (PlanetPosition p : planets) {
            if ("Sun".equalsIgnoreCase(p.name)) pos[0] = p.rashiIndex;
            else if ("Moon".equalsIgnoreCase(p.name)) pos[1] = p.rashiIndex;
            else if ("Mars".equalsIgnoreCase(p.name)) pos[2] = p.rashiIndex;
            else if ("Mercury".equalsIgnoreCase(p.name)) pos[3] = p.rashiIndex;
            else if ("Jupiter".equalsIgnoreCase(p.name)) pos[4] = p.rashiIndex;
            else if ("Venus".equalsIgnoreCase(p.name)) pos[5] = p.rashiIndex;
            else if ("Saturn".equalsIgnoreCase(p.name)) pos[6] = p.rashiIndex;
        }
        pos[7] = lagnaRashi;

        int[][] sunRules = {
            {1, 2, 4, 7, 8, 9, 10, 11}, {3, 6, 10, 11}, {1, 2, 4, 7, 8, 9, 10, 11},
            {3, 5, 6, 9, 10, 11, 12}, {5, 6, 9, 11}, {6, 7, 12}, {1, 2, 4, 7, 8, 9, 10, 11}, {3, 4, 6, 10, 11, 12}
        };
        int[][] moonRules = {
            {3, 6, 7, 8, 10, 11}, {1, 3, 6, 7, 10, 11}, {2, 3, 5, 6, 9, 10, 11},
            {1, 3, 4, 5, 7, 8, 10, 11}, {1, 4, 7, 8, 10, 11, 12}, {3, 4, 5, 7, 9, 10, 11}, {3, 5, 6, 11}, {3, 6, 10, 11}
        };
        int[][] marsRules = {
            {3, 5, 6, 10, 11}, {3, 6, 11}, {1, 2, 4, 7, 8, 10, 11},
            {3, 5, 6, 11}, {6, 10, 11, 12}, {6, 8, 11, 12}, {1, 4, 7, 8, 9, 10, 11}, {1, 3, 6, 10, 11}
        };
        int[][] mercRules = {
            {5, 6, 9, 11, 12}, {2, 4, 6, 8, 10, 11}, {1, 2, 4, 7, 8, 9, 10, 11},
            {1, 3, 5, 6, 9, 10, 11, 12}, {6, 8, 11, 12}, {1, 2, 3, 4, 5, 8, 9, 11}, {1, 2, 4, 7, 8, 9, 10, 11}, {1, 2, 4, 6, 8, 10, 11}
        };
        int[][] jupRules = {
            {1, 2, 3, 4, 7, 8, 9, 10, 11}, {2, 5, 7, 9, 11}, {1, 2, 4, 7, 8, 10, 11},
            {1, 2, 4, 5, 6, 9, 10, 11}, {1, 2, 3, 4, 7, 8, 10, 11}, {2, 5, 6, 9, 10, 11}, {3, 5, 6, 12}, {1, 2, 4, 5, 6, 7, 9, 10, 11}
        };
        int[][] venRules = {
            {8, 11, 12}, {1, 2, 3, 4, 5, 8, 9, 11, 12}, {3, 5, 6, 9, 11, 12},
            {3, 5, 6, 9, 11}, {5, 8, 9, 10, 11}, {1, 2, 3, 4, 5, 8, 9, 10, 11}, {3, 4, 5, 8, 9, 10, 11}, {1, 2, 3, 4, 5, 8, 9, 11}
        };
        int[][] satRules = {
            {1, 2, 4, 7, 8, 10, 11}, {3, 6, 11}, {3, 5, 6, 10, 11, 12},
            {6, 8, 9, 10, 11, 12}, {5, 6, 11, 12}, {6, 11, 12}, {3, 5, 6, 11}, {1, 3, 4, 6, 10, 11}
        };

        int[][][] allRules = { sunRules, moonRules, marsRules, mercRules, jupRules, venRules, satRules };

        for (int p = 0; p < 7; p++) {
            for (int donor = 0; donor < 8; donor++) {
                int donorRashi = pos[donor];
                int[] houses = allRules[p][donor];
                for (int h : houses) {
                    int targetHouse = (donorRashi + h - 1) % 12;
                    res.bhinnaAshtaka[p][targetHouse]++;
                    res.sarvashtakavarga[targetHouse]++;
                }
            }
        }

        for (int h = 0; h < 12; h++) {
            int b = res.sarvashtakavarga[h];
            if (b > res.maxBindus) {
                res.maxBindus = b;
                res.strongestHouse = h;
            }
            if (b < res.minBindus) {
                res.minBindus = b;
                res.weakestHouse = h;
            }
        }

        return res;
    }

    // ==========================================
    // 5. SHADBALA ENGINE (6-FOLD PLANETARY STRENGTH)
    // ==========================================
    public static class PlanetShadbala {
        public String name;
        public String sanskritName;
        public double sthanaBala;
        public double digBala;
        public double kalaBala;
        public double cheshtaBala;
        public double naisargikaBala;
        public double drikBala;
        public double totalVirupas;
        public double totalRupas;
        public double minRequiredRupas;
        public double ratio;
        public double ishtaPhala;
        public double kashtaPhala;
        public boolean meetsRequirement;
    }

    public static class ShadbalaResult {
        public List<PlanetShadbala> planets = new ArrayList<>();
        public PlanetShadbala strongestPlanet;
        public PlanetShadbala weakestPlanet;
    }

    public static ShadbalaResult calculateShadbala(List<PlanetPosition> planets, int lagnaRashi, DayPanchang dp) {
        ShadbalaResult res = new ShadbalaResult();

        String[] pNames = { "Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn" };
        String[] sNames = { "Surya", "Chandra", "Mangala", "Budha", "Guru", "Shukra", "Shani" };
        double[] reqRupas = { 6.5, 6.0, 5.0, 7.0, 6.5, 5.5, 5.0 };
        double[] naisBala = { 60.0, 51.4, 17.1, 25.7, 34.3, 42.8, 8.6 };
        double[] deepExalt = { 10.0, 33.0, 298.0, 165.0, 95.0, 357.0, 200.0 };

        PlanetShadbala best = null;
        PlanetShadbala worst = null;

        for (int i = 0; i < 7; i++) {
            PlanetShadbala ps = new PlanetShadbala();
            ps.name = pNames[i];
            ps.sanskritName = sNames[i];
            ps.minRequiredRupas = reqRupas[i];
            ps.naisargikaBala = naisBala[i];

            PlanetPosition pos = null;
            for (PlanetPosition pp : planets) {
                if (pNames[i].equalsIgnoreCase(pp.name)) {
                    pos = pp;
                    break;
                }
            }

            double lon = (pos != null) ? pos.longitude : 0.0;
            boolean retro = (pos != null && pos.isRetrograde);

            double diff = Math.abs(lon - ((deepExalt[i] + 180.0) % 360.0));
            if (diff > 180.0) diff = 360.0 - diff;
            double uchha = diff / 3.0;
            ps.sthanaBala = uchha + 90.0;

            int pRashi = (pos != null) ? pos.rashiIndex : 0;
            int house = ((pRashi - lagnaRashi + 12) % 12) + 1;
            int optimalHouse = (i == 0 || i == 2) ? 10 : ((i == 3 || i == 4) ? 1 : ((i == 6) ? 7 : 4));
            int hDiff = Math.abs(house - optimalHouse);
            if (hDiff > 6) hDiff = 12 - hDiff;
            ps.digBala = 60.0 - (hDiff * 10.0);

            double natonnatha = 40.0;
            double paksha = dp != null ? dp.moonIlluminationPercent * 0.4 : 25.0;
            ps.kalaBala = 80.0 + natonnatha + paksha;

            ps.cheshtaBala = retro ? 60.0 : 40.0;
            ps.drikBala = 12.0;

            ps.totalVirupas = ps.sthanaBala + ps.digBala + ps.kalaBala + ps.cheshtaBala + ps.naisargikaBala + ps.drikBala;
            ps.totalRupas = ps.totalVirupas / 60.0;
            ps.ratio = ps.totalRupas / ps.minRequiredRupas;
            ps.meetsRequirement = (ps.ratio >= 1.0);

            ps.ishtaPhala = Math.sqrt(Math.max(0.0, (uchha / 60.0) * ps.cheshtaBala * 60.0));
            ps.kashtaPhala = Math.max(0.0, 60.0 - ps.ishtaPhala);

            res.planets.add(ps);

            if (best == null || ps.ratio > best.ratio) best = ps;
            if (worst == null || ps.ratio < worst.ratio) worst = ps;
        }

        res.strongestPlanet = best;
        res.weakestPlanet = worst;
        return res;
    }

    // ==========================================
    // 6. SADE SATI (SATURN TRANSIT STATUS)
    // ==========================================
    public static class SadeSatiResult {
        public String phase;
        public String phaseHindi;
        public int currentSaturnRashi;
        public String saturnRashiName;
        public int natalMoonRashi;
        public String moonRashiName;
        public String statusDescription;
        public boolean isUndergoingSadeSati;
    }

    public static SadeSatiResult calculateSadeSati(int natalMoonRashi, int currentSaturnRashi) {
        SadeSatiResult res = new SadeSatiResult();
        res.natalMoonRashi = natalMoonRashi;
        res.moonRashiName = RASHIS[natalMoonRashi % 12];
        res.currentSaturnRashi = currentSaturnRashi;
        res.saturnRashiName = RASHIS[currentSaturnRashi % 12];

        int rel = ((currentSaturnRashi - natalMoonRashi + 12) % 12) + 1;

        if (rel == 12) {
            res.phase = "Rising Phase (1st Cycle / Charan 1)";
            res.phaseHindi = "चढ़ती साढ़ेसाती (प्रथम चरण)";
            res.isUndergoingSadeSati = true;
            res.statusDescription = "Saturn is in 12th house (" + res.saturnRashiName + ") from natal Moon (" + res.moonRashiName + "). Expenditures, relocations, and mental transformation.";
        } else if (rel == 1) {
            res.phase = "Peak Phase (2nd Cycle / Janma Shani)";
            res.phaseHindi = "शिखर साढ़ेसाती (द्वितीय चरण • जन्म शनि)";
            res.isUndergoingSadeSati = true;
            res.statusDescription = "Saturn is transiting directly over natal Moon (" + res.moonRashiName + "). High transformation, deep focus, and spiritual discipline needed.";
        } else if (rel == 2) {
            res.phase = "Setting Phase (3rd Cycle / Charan 3)";
            res.phaseHindi = "उतरती साढ़ेसाती (तृतीय चरण)";
            res.isUndergoingSadeSati = true;
            res.statusDescription = "Saturn is in 2nd house (" + res.saturnRashiName + ") from natal Moon (" + res.moonRashiName + "). Easing burdens, stabilization, and consolidation.";
        } else if (rel == 4) {
            res.phase = "Kantaka Shani (Ardhashtama Dhaiya)";
            res.phaseHindi = "कंटक शनि / अर्धाष्टम ढैया (चतुर्थ भाव)";
            res.isUndergoingSadeSati = true;
            res.statusDescription = "Saturn is in 4th house from Moon. Domestic patience and health mindfulness advised.";
        } else if (rel == 8) {
            res.phase = "Ashtama Shani (2.5 Year Dhaiya)";
            res.phaseHindi = "अष्टम शनि ढैया (अष्टम भाव)";
            res.isUndergoingSadeSati = true;
            res.statusDescription = "Saturn is in 8th house from Moon. Caution in journeys, contracts, and partnerships.";
        } else {
            res.phase = "No Sade Sati";
            res.phaseHindi = "साढ़ेसाती का प्रभाव नहीं";
            res.isUndergoingSadeSati = false;
            res.statusDescription = "Saturn in " + res.saturnRashiName + " is favorable/neutral (House " + rel + " from " + res.moonRashiName + "). No active Sade Sati.";
        }
        return res;
    }

    // ==========================================
    // 7. MONTH FESTIVALS & VRATS LIST
    // ==========================================
    public static class FestivalItem {
        public int day;
        public String dateStr;
        public String tithiStr;
        public String name;
        public String category;
        public String description;
    }

    public static List<FestivalItem> getMonthFestivals(int year, int month, double lat, double lon, boolean isPurnimant) {
        List<FestivalItem> list = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1);
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        String[] monthsEn = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

        for (int d = 1; d <= maxDays; d++) {
            DayPanchang dp = calculatePanchang(year, month, d, lat, lon);
            if (dp.hasFestival()) {
                FestivalItem item = new FestivalItem();
                item.day = d;
                item.dateStr = d + " " + monthsEn[month - 1] + " (" + dp.vaarName.substring(0, 3) + ")";
                String activeMasa = isPurnimant ? dp.purnimantaMonth : dp.hinduMonth;
                item.tithiStr = activeMasa + " " + dp.paksha + " " + dp.tithiName;
                item.name = dp.specialOccasionFull != null ? dp.specialOccasionFull : dp.specialOccasion;

                String lower = item.name.toLowerCase(Locale.US);
                if (lower.contains("diwali") || lower.contains("navratri") || lower.contains("dussehra") || lower.contains("shivratri") || lower.contains("janmashtami") || lower.contains("holi") || lower.contains("ganesh")) {
                    item.category = "Major";
                } else if (lower.contains("ekadashi")) {
                    item.category = "Ekadashi";
                } else if (lower.contains("pradosh")) {
                    item.category = "Pradosh";
                } else if (lower.contains("sankashti") || lower.contains("chaturthi")) {
                    item.category = "Sankashti";
                } else if (lower.contains("purnima")) {
                    item.category = "Purnima";
                } else if (lower.contains("amavasya")) {
                    item.category = "Amavasya";
                } else {
                    item.category = "Vrat";
                }

                item.description = "Sacred Hindu observance celebrated with fasting, rituals, and prayers on " + item.tithiStr + ".";
                list.add(item);
            }
        }
        return list;
    }
}
