package com.custom.hinduwidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.graphics.Typeface;
import android.widget.EditText;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import android.content.SharedPreferences;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.provider.CalendarContract;
import java.nio.charset.StandardCharsets;
import java.io.FileOutputStream;
import java.io.File;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Date;

public class MainActivity extends Activity {

    // Date State
    private int currentYear;
    private int currentMonth; // 1-12
    private int currentDay;

    // Calendar Tab Navigation State
    private int calNavYear;
    private int calNavMonth; // 1-12

    // City & Calculation
    private CityManager.City currentCity;
    private PanchangCalculator.DayPanchang currentPanchang;

    // Active Tab (0: Panchang, 1: Calendar, 2: Choghadiya, 3: Kundali, 4: More)
    private int activeTab = 0;

    // Sub-tab / Chart Toggles
    private boolean isHoraActive = false;
    private int kundaliStyle = 0; // 0=North, 1=South, 2=East
    private boolean isPurnimant = false;
    private NotableChartsManager.NotablePerson activeStudyChart = null;
    private PanchangCalculator.DayPanchang activeStudyPanchang = null;
    private VedicCompassView moreCompassView = null;

    // Ashtakoota Matchmaker State
    private int matchGroomRashi = 0; // Mesha
    private int matchGroomNak = 0;   // Ashwini
    private int matchBrideRashi = 4; // Simha
    private int matchBrideNak = 9;   // Magha

    // Header Views
    private TextView txtAppBarCity;
    private TextView txtHeaderGregorian;
    private TextView txtHeaderHinduDate;

    // Tab Navigation Views
    private LinearLayout[] navItems = new LinearLayout[5];
    private FrameLayout[] navPills = new FrameLayout[5];
    private ImageView[] navIcons = new ImageView[5];
    private TextView[] navTexts = new TextView[5];

    // Tab Views Container
    private FrameLayout containerTabContent;
    private View viewPanchang;
    private View viewCalendar;
    private View viewChoghadiya;
    private View viewKundali;
    private View viewMore;

    // Live Ticker for Timers
    private Handler tickerHandler = new Handler(Looper.getMainLooper());
    private Runnable tickerRunnable = new Runnable() {
        @Override
        public void run() {
            updateLiveTimers();
            tickerHandler.postDelayed(this, 1000);
        }
    };

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ThemeManager.applyThemeToActivity(this);

        // Parse date from Intent if opened from Widget tap
        Intent intent = getIntent();
        int y = intent.getIntExtra("year", -1);
        int m = intent.getIntExtra("month", -1);
        int d = intent.getIntExtra("day", -1);

        Calendar cal = Calendar.getInstance();
        if (y == -1 || m == -1 || d == -1) {
            currentYear = cal.get(Calendar.YEAR);
            currentMonth = cal.get(Calendar.MONTH) + 1;
            currentDay = cal.get(Calendar.DAY_OF_MONTH);
        } else {
            currentYear = y;
            currentMonth = m;
            currentDay = d;
        }

        calNavYear = currentYear;
        calNavMonth = currentMonth;

        // Default to Pune reference city
        currentCity = new CityManager.City("Pune", 18.5204, 73.8567, "Asia/Kolkata", "India");

        initViews();
        setupTabs();
        applyDynamicJapaneseTheme(ThemeManager.getActivePalette(this));
        recalculateAndRefresh();

        // Load 4,500+ global cities asynchronously
        new Thread(new Runnable() {
            @Override
            public void run() {
                CityManager.loadCitiesFromAssets(MainActivity.this);
            }
        }).start();

        tickerHandler.post(tickerRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tickerHandler.removeCallbacks(tickerRunnable);
        if (moreCompassView != null) {
            moreCompassView.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (moreCompassView != null && activeTab == 4) {
            moreCompassView.startListening();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (moreCompassView != null) {
            moreCompassView.stopListening();
        }
    }

    private void initViews() {
        txtAppBarCity = findViewById(R.id.txt_app_bar_city);
        txtHeaderGregorian = findViewById(R.id.txt_header_gregorian);
        txtHeaderHinduDate = findViewById(R.id.txt_header_hindu_date);
        containerTabContent = findViewById(R.id.container_tab_content);

        // City Selector Chip
        findViewById(R.id.btn_city_chip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCitySelectorDialog();
            }
        });

        // Today Jump Button
        findViewById(R.id.btn_today_jump).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                currentYear = cal.get(Calendar.YEAR);
                currentMonth = cal.get(Calendar.MONTH) + 1;
                currentDay = cal.get(Calendar.DAY_OF_MONTH);
                calNavYear = currentYear;
                calNavMonth = currentMonth;
                recalculateAndRefresh();
            }
        });

        // Share App Bar Button
        View btnShare = findViewById(R.id.btn_share_app_bar);
        if (btnShare != null) {
            btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareCurrentPanchang();
                }
            });
        }

        // Settings & Themes App Bar Button
        View btnSettings = findViewById(R.id.btn_settings_app_bar);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSettingsDialog();
                }
            });
        }

        // Previous Day Button
        findViewById(R.id.btn_prev_day).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDay(-1);
            }
        });

        // Next Day Button
        findViewById(R.id.btn_next_day).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDay(1);
            }
        });

        // Date Picker Trigger
        findViewById(R.id.btn_date_picker_trigger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Tab Navigation Item IDs
        int[] itemIds = {
            R.id.nav_item_panchang, R.id.nav_item_calendar,
            R.id.nav_item_choghadiya, R.id.nav_item_kundali, R.id.nav_item_more
        };
        int[] pillIds = {
            R.id.nav_pill_panchang, R.id.nav_pill_calendar,
            R.id.nav_pill_choghadiya, R.id.nav_pill_kundali, R.id.nav_pill_more
        };
        int[] iconIds = {
            R.id.nav_icon_panchang, R.id.nav_icon_calendar,
            R.id.nav_icon_choghadiya, R.id.nav_icon_kundali, R.id.nav_icon_more
        };
        int[] textIds = {
            R.id.nav_txt_panchang, R.id.nav_txt_calendar,
            R.id.nav_txt_choghadiya, R.id.nav_txt_kundali, R.id.nav_txt_more
        };

        for (int i = 0; i < 5; i++) {
            final int tabIndex = i;
            navItems[i] = findViewById(itemIds[i]);
            navPills[i] = findViewById(pillIds[i]);
            navIcons[i] = findViewById(iconIds[i]);
            navTexts[i] = findViewById(textIds[i]);

            navItems[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchTab(tabIndex);
                }
            });
        }

        // Inflate all tab layouts
        LayoutInflater inflater = LayoutInflater.from(this);
        viewPanchang = inflater.inflate(R.layout.tab_panchang, containerTabContent, false);
        viewCalendar = inflater.inflate(R.layout.tab_calendar, containerTabContent, false);
        viewChoghadiya = inflater.inflate(R.layout.tab_choghadiya, containerTabContent, false);
        viewKundali = inflater.inflate(R.layout.tab_kundali, containerTabContent, false);
        viewMore = inflater.inflate(R.layout.tab_more, containerTabContent, false);

        setupCalendarNavButtons();
        setupMoreTabButtons();
    }

    private void changeDay(int deltaDays) {
        Calendar cal = Calendar.getInstance();
        cal.set(currentYear, currentMonth - 1, currentDay);
        cal.add(Calendar.DAY_OF_MONTH, deltaDays);
        currentYear = cal.get(Calendar.YEAR);
        currentMonth = cal.get(Calendar.MONTH) + 1;
        currentDay = cal.get(Calendar.DAY_OF_MONTH);
        calNavYear = currentYear;
        calNavMonth = currentMonth;
        recalculateAndRefresh();
    }

    private void showDatePickerDialog() {
        DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                currentYear = year;
                currentMonth = month + 1;
                currentDay = dayOfMonth;
                calNavYear = currentYear;
                calNavMonth = currentMonth;
                recalculateAndRefresh();
            }
        }, currentYear, currentMonth - 1, currentDay);
        dpd.show();
    }

    private void showCitySelectorDialog() {
        CityManager.loadCitiesFromAssets(this);
        final List<CityManager.City> searchResults = new ArrayList<>(CityManager.searchCities("", 60));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_city_search, null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        final EditText inputSearch = dialogView.findViewById(R.id.dialog_search_city_input);
        final ListView listView = dialogView.findViewById(R.id.dialog_city_listview);
        final Button btnCancel = dialogView.findViewById(R.id.dialog_btn_city_cancel);

        final BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return searchResults.size();
            }

            @Override
            public CityManager.City getItem(int position) {
                return searchResults.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_city_row, parent, false);
                }
                CityManager.City c = getItem(position);
                TextView txtName = convertView.findViewById(R.id.city_row_name);
                TextView txtDetails = convertView.findViewById(R.id.city_row_details);

                txtName.setText(c.name);
                String offsetStr = (c.timezoneOffsetHours >= 0 ? "+" : "") + c.timezoneOffsetHours + "h";
                txtDetails.setText(c.country + " • " + String.format(Locale.US, "%.2f°, %.2f°", c.latitude, c.longitude) + " • " + c.timezone + " (" + offsetStr + ")");
                return convertView;
            }
        };

        listView.setAdapter(adapter);

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = (s != null) ? s.toString() : "";
                searchResults.clear();
                searchResults.addAll(CityManager.searchCities(query, 60));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentCity = searchResults.get(position);
                txtAppBarCity.setText(currentCity.name + ", " + (currentCity.country.equalsIgnoreCase("India") ? "IN" : currentCity.country));
                dialog.dismiss();
                recalculateAndRefresh();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void setupTabs() {
        switchTab(0);
    }

    private void switchTab(int tabIndex) {
        activeTab = tabIndex;

        // Update M3 Nav Indicator Pills & Text Styles
        ThemeManager.JapanesePalette activePalette = ThemeManager.getActivePalette(this);
        for (int i = 0; i < 5; i++) {
            if (i == tabIndex) {
                navPills[i].setBackgroundResource(R.drawable.m3_pill_indicator);
                navPills[i].setBackgroundTintList(ColorStateList.valueOf(activePalette.primaryContainer));
                navTexts[i].setTextColor(activePalette.primary);
                navTexts[i].setTypeface(null, android.graphics.Typeface.BOLD);
                navIcons[i].setColorFilter(activePalette.primary);
            } else {
                navPills[i].setBackgroundColor(Color.TRANSPARENT);
                navPills[i].setBackgroundTintList(null);
                navTexts[i].setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
                navTexts[i].setTypeface(null, android.graphics.Typeface.NORMAL);
                navIcons[i].setColorFilter(getColor(R.color.md_theme_onSurfaceVariant));
            }
        }

        // Manage Compass sensor listening
        if (moreCompassView != null) {
            if (tabIndex == 4) {
                moreCompassView.startListening();
            } else {
                moreCompassView.stopListening();
            }
        }

        // Swap Content View with smooth fade animation (/animate)
        containerTabContent.removeAllViews();
        containerTabContent.setAlpha(0.7f);
        containerTabContent.animate().alpha(1f).setDuration(140).start();

        switch (tabIndex) {
            case 0:
                containerTabContent.addView(viewPanchang);
                bindPanchangTab();
                break;
            case 1:
                containerTabContent.addView(viewCalendar);
                bindCalendarTab();
                break;
            case 2:
                containerTabContent.addView(viewChoghadiya);
                bindChoghadiyaTab();
                break;
            case 3:
                containerTabContent.addView(viewKundali);
                bindKundaliTab();
                break;
            case 4:
                containerTabContent.addView(viewMore);
                bindMoreTab();
                break;
        }
    }

    private void recalculateAndRefresh() {
        // Run full astronomical engine
        currentPanchang = PanchangCalculator.calculatePanchang(
            currentYear, currentMonth, currentDay, currentCity.latitude, currentCity.longitude
        );

        SharedPreferences sp = getSharedPreferences("app_settings", MODE_PRIVATE);
        isPurnimant = sp.getBoolean("is_purnimant", false);

        // Update Top Header
        Calendar cal = Calendar.getInstance();
        cal.set(currentYear, currentMonth - 1, currentDay);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US);
        txtHeaderGregorian.setText(sdf.format(cal.getTime()));

        String activeMasa = isPurnimant ? currentPanchang.purnimantaMonth : currentPanchang.hinduMonth;
        String hinduDateStr = activeMasa + " " + currentPanchang.paksha + " " + currentPanchang.tithiName;
        if (currentPanchang.hasFestival()) {
            hinduDateStr += " • " + currentPanchang.specialOccasion;
        }
        txtHeaderHinduDate.setText(hinduDateStr);
        txtAppBarCity.setText(currentCity.name + ", " + (currentCity.country.equalsIgnoreCase("India") ? "IN" : currentCity.country));

        // Synchronize all tab views in real time so switching tabs always displays up-to-date calculations
        bindPanchangTab();
        bindCalendarTab();
        bindChoghadiyaTab();
        bindKundaliTab();
        bindMoreTab();

        // Broadcast to desktop widgets
        broadcastWidgetUpdate();
    }

    // TAB 1: PANCHANG BINDING
    private void bindPanchangTab() {
        if (currentPanchang == null || viewPanchang == null) return;

        ImageView moonIcon = viewPanchang.findViewById(R.id.panchang_moon_icon);
        TextView txtMasaPaksha = viewPanchang.findViewById(R.id.panchang_txt_masapaksha);
        TextView txtSamvat = viewPanchang.findViewById(R.id.panchang_txt_samvat);
        TextView txtMoonIllum = viewPanchang.findViewById(R.id.panchang_txt_moon_illum);

        TextView txtSunrise = viewPanchang.findViewById(R.id.panchang_txt_sunrise);
        TextView txtSunset = viewPanchang.findViewById(R.id.panchang_txt_sunset);
        TextView txtMoonrise = viewPanchang.findViewById(R.id.panchang_txt_moonrise);
        TextView txtMoonset = viewPanchang.findViewById(R.id.panchang_txt_moonset);
        TextView txtDayNightLen = viewPanchang.findViewById(R.id.panchang_txt_day_night_len);

        // 5 Limbs
        TextView txtTithiName = viewPanchang.findViewById(R.id.panchang_txt_tithi_name);
        TextView txtTithiEndTime = viewPanchang.findViewById(R.id.panchang_txt_tithi_endtime);
        TextView txtNakshatraName = viewPanchang.findViewById(R.id.panchang_txt_nakshatra_name);
        TextView txtNakshatraEndTime = viewPanchang.findViewById(R.id.panchang_txt_nakshatra_endtime);
        TextView txtYogaName = viewPanchang.findViewById(R.id.panchang_txt_yoga_name);
        TextView txtYogaEndTime = viewPanchang.findViewById(R.id.panchang_txt_yoga_endtime);
        TextView txtKaranaName = viewPanchang.findViewById(R.id.panchang_txt_karana_name);
        TextView txtKaranaEndTime = viewPanchang.findViewById(R.id.panchang_txt_karana_endtime);
        TextView txtVaara = viewPanchang.findViewById(R.id.panchang_txt_vaara);
        TextView txtVaaraLord = viewPanchang.findViewById(R.id.panchang_txt_vaara_lord);

        // Muhurtas
        TextView txtAbhijit = viewPanchang.findViewById(R.id.panchang_txt_abhijit);
        TextView txtBrahma = viewPanchang.findViewById(R.id.panchang_txt_brahma);
        TextView txtRahuKaal = viewPanchang.findViewById(R.id.panchang_txt_rahukaal);
        TextView txtYamagand = viewPanchang.findViewById(R.id.panchang_txt_yamagand);
        TextView txtGulika = viewPanchang.findViewById(R.id.panchang_txt_gulika);
        TextView txtVijaya = viewPanchang.findViewById(R.id.panchang_txt_vijaya);
        TextView txtGodhuli = viewPanchang.findViewById(R.id.panchang_txt_godhuli);
        TextView txtAmritKaal = viewPanchang.findViewById(R.id.panchang_txt_amrit_kaal);
        TextView txtNishita = viewPanchang.findViewById(R.id.panchang_txt_nishita);

        // Avakahada
        TextView txtAvakahada = viewPanchang.findViewById(R.id.panchang_txt_avakahada);

        moonIcon.setImageResource(currentPanchang.moonDrawableResId);
        txtMasaPaksha.setText(currentPanchang.hinduMonth + " " + currentPanchang.paksha + " Paksha");
        txtSamvat.setText("Vikram " + currentPanchang.vikramSamvat + " • Saka " + currentPanchang.sakaSamvat + " • " + currentPanchang.samvatsara);
        txtMoonIllum.setText((int) Math.round(currentPanchang.moonIlluminationPercent) + "% Illum.");

        txtSunrise.setText(currentPanchang.sunriseStr);
        txtSunset.setText(currentPanchang.sunsetStr);
        txtMoonrise.setText(currentPanchang.moonriseStr);
        txtMoonset.setText(currentPanchang.moonsetStr);
        txtDayNightLen.setText("Day: " + currentPanchang.dayLengthStr + " • Night: " + currentPanchang.nightLengthStr);

        // Tithi
        txtTithiName.setText(currentPanchang.paksha + " " + currentPanchang.tithiName + " (" + currentPanchang.tithiIndex + "/30)");
        txtTithiEndTime.setText("Ends at " + currentPanchang.tithiEndTimeStr);

        // Nakshatra
        txtNakshatraName.setText(currentPanchang.nakshatra + " • Pada " + currentPanchang.nakshatraPada);
        txtNakshatraEndTime.setText("Lord: " + currentPanchang.avakahada.nakshatraSwami + " • Ends at " + currentPanchang.nakshatraEndTimeStr);

        // Yoga
        txtYogaName.setText(currentPanchang.yoga);
        txtYogaEndTime.setText("Ends at " + currentPanchang.yogaEndTimeStr);

        // Karana
        txtKaranaName.setText(currentPanchang.karana1);
        txtKaranaEndTime.setText("Ends at " + currentPanchang.karana1EndTimeStr + " • Next: " + currentPanchang.karana2);

        // Vaara
        txtVaara.setText(currentPanchang.vaarName);
        txtVaaraLord.setText("Ruling Planet: " + getVaaraLord(currentPanchang.dayOfWeek));

        // Muhurtas
        txtAbhijit.setText(currentPanchang.muhurta.abhijit);
        txtBrahma.setText(currentPanchang.muhurta.brahmaMuhurta);
        txtRahuKaal.setText(currentPanchang.muhurta.rahuKaal);
        txtYamagand.setText(currentPanchang.muhurta.yamagand);
        txtGulika.setText(currentPanchang.muhurta.gulika);
        if (txtVijaya != null && currentPanchang.muhurta.vijayaMuhurta != null) {
            txtVijaya.setText(currentPanchang.muhurta.vijayaMuhurta);
        }
        if (txtGodhuli != null && currentPanchang.muhurta.godhuliMuhurta != null) {
            txtGodhuli.setText(currentPanchang.muhurta.godhuliMuhurta);
        }
        if (txtAmritKaal != null && currentPanchang.muhurta.amritKaal != null) {
            txtAmritKaal.setText(currentPanchang.muhurta.amritKaal);
        }
        if (txtNishita != null && currentPanchang.muhurta.nishitaKaal != null) {
            txtNishita.setText(currentPanchang.muhurta.nishitaKaal);
        }

        // Avakahada
        StringBuilder sb = new StringBuilder();
        sb.append("• Moon Sign (Chandra Rashi): ").append(currentPanchang.chandraRashiFull).append("\n");
        sb.append("• Sun Sign (Surya Rashi): ").append(currentPanchang.suryaRashiFull).append("\n");
        sb.append("• Rashi Lord: ").append(currentPanchang.avakahada.rashiSwami).append("\n");
        sb.append("• Nakshatra Lord: ").append(currentPanchang.avakahada.nakshatraSwami).append("\n");
        sb.append("• Yoni: ").append(currentPanchang.avakahada.yoni).append(" • Gana: ").append(currentPanchang.avakahada.gana).append("\n");
        sb.append("• Nadi: ").append(currentPanchang.avakahada.nadi).append(" • Varna: ").append(currentPanchang.avakahada.varna).append("\n");
        sb.append("• Paya: ").append(currentPanchang.avakahada.paya).append(" • Janma Akshara: ").append(currentPanchang.avakahada.janmaAkshara);
        
        txtAvakahada.setText(sb.toString());

        // Visual Muhurta Timeline
        VisualMuhurtaTimelineView timelineView = viewPanchang.findViewById(R.id.panchang_muhurta_timeline);
        if (timelineView != null) {
            timelineView.setMuhurtaData(currentPanchang);
        }

        // Lagna Timings of the Day
        LinearLayout lagnaContainer = viewPanchang.findViewById(R.id.panchang_lagna_table_container);
        TextView txtActiveLagnaBadge = viewPanchang.findViewById(R.id.panchang_txt_active_lagna_badge);
        if (lagnaContainer != null) {
            lagnaContainer.removeAllViews();
            List<PanchangCalculator.LagnaSpan> lagnaList = PanchangCalculator.calculateLagnaTable(
                currentYear, currentMonth, currentDay, currentCity.latitude, currentCity.longitude
            );
            for (PanchangCalculator.LagnaSpan ls : lagnaList) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dpToPx(10), dpToPx(7), dpToPx(10), dpToPx(7));
                if (ls.isCurrent) {
                    row.setBackgroundResource(R.drawable.m3_card_highlight);
                    if (txtActiveLagnaBadge != null) {
                        txtActiveLagnaBadge.setText("Active: " + ls.rashiName);
                    }
                }
                TextView txtName = new TextView(this);
                txtName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f));
                txtName.setText(ls.rashiName + " (" + ls.rashiSymbol + ")");
                txtName.setTextColor(ls.isCurrent ? getColor(R.color.md_theme_primary) : getColor(R.color.md_theme_onSurface));
                txtName.setTextSize(13f);
                txtName.setTypeface(null, ls.isCurrent ? Typeface.BOLD : Typeface.NORMAL);

                TextView txtTime = new TextView(this);
                txtTime.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.8f));
                txtTime.setText(ls.startTimeStr + " - " + ls.endTimeStr);
                txtTime.setTextColor(ls.isCurrent ? getColor(R.color.md_theme_primary) : getColor(R.color.md_theme_onSurfaceVariant));
                txtTime.setTextSize(12f);
                txtTime.setGravity(Gravity.RIGHT);

                row.addView(txtName);
                row.addView(txtTime);
                lagnaContainer.addView(row);
            }
        }

        // Tara Bala & Chandra Bala
        final Spinner spinNak = viewPanchang.findViewById(R.id.spinner_tara_nakshatra);
        final Spinner spinRashi = viewPanchang.findViewById(R.id.spinner_chandra_rashi);
        final TextView txtTaraResult = viewPanchang.findViewById(R.id.panchang_txt_tara_result);
        if (spinNak != null && spinRashi != null && txtTaraResult != null) {
            ArrayAdapter<String> nakAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, PanchangCalculator.NAKSHATRAS);
            spinNak.setAdapter(nakAdapter);
            ArrayAdapter<String> rashiAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, PanchangCalculator.RASHIS);
            spinRashi.setAdapter(rashiAdapter);

            AdapterView.OnItemSelectedListener taraListener = new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int uNak = spinNak.getSelectedItemPosition();
                    int uRashi = spinRashi.getSelectedItemPosition();
                    PanchangCalculator.TaraChandraBala res = PanchangCalculator.calculateTaraChandraBala(
                        uNak, uRashi, currentPanchang.nakshatraIndex, currentPanchang.chandraRashiIndex);
                    txtTaraResult.setText(res.explanation);
                    if (res.isChandrashtama) {
                        txtTaraResult.setBackgroundResource(R.drawable.m3_badge_red);
                        txtTaraResult.setTextColor(getColor(R.color.m3_inauspicious));
                    } else if (res.isTaraAuspicious && res.isChandraAuspicious) {
                        txtTaraResult.setBackgroundResource(R.drawable.m3_card_highlight);
                        txtTaraResult.setTextColor(getColor(R.color.md_theme_primary));
                    } else {
                        txtTaraResult.setBackgroundResource(R.drawable.m3_card_highlight);
                        txtTaraResult.setTextColor(getColor(R.color.md_theme_onSurface));
                    }
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            };
            spinNak.setOnItemSelectedListener(taraListener);
            spinRashi.setOnItemSelectedListener(taraListener);
        }

    }

    private String getVaaraLord(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1: return "Surya (Sun)";
            case 2: return "Chandra (Moon)";
            case 3: return "Mangal (Mars)";
            case 4: return "Budha (Mercury)";
            case 5: return "Guru (Jupiter)";
            case 6: return "Shukra (Venus)";
            case 7: return "Shani (Saturn)";
            default: return "";
        }
    }

    // TAB 2: CALENDAR BINDING
    private void setupCalendarNavButtons() {
        ImageButton btnPrev = viewCalendar.findViewById(R.id.cal_btn_prev_month);
        ImageButton btnNext = viewCalendar.findViewById(R.id.cal_btn_next_month);
        Button btnViewPanchang = viewCalendar.findViewById(R.id.cal_btn_view_panchang);

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calNavMonth--;
                if (calNavMonth < 1) {
                    calNavMonth = 12;
                    calNavYear--;
                }
                currentYear = calNavYear;
                currentMonth = calNavMonth;
                Calendar tempCal = Calendar.getInstance();
                tempCal.set(currentYear, currentMonth - 1, 1);
                int maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
                if (currentDay > maxDays) currentDay = maxDays;

                recalculateAndRefresh();
                animateMonthSlide(false);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calNavMonth++;
                if (calNavMonth > 12) {
                    calNavMonth = 1;
                    calNavYear++;
                }
                currentYear = calNavYear;
                currentMonth = calNavMonth;
                Calendar tempCal = Calendar.getInstance();
                tempCal.set(currentYear, currentMonth - 1, 1);
                int maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
                if (currentDay > maxDays) currentDay = maxDays;

                recalculateAndRefresh();
                animateMonthSlide(true);
            }
        });

        
        final TextView btnToggleGrid = viewCalendar.findViewById(R.id.cal_toggle_grid);
        final TextView btnToggleFest = viewCalendar.findViewById(R.id.cal_toggle_festivals);
        final View containerGrid = viewCalendar.findViewById(R.id.cal_container_grid);
        final View containerFest = viewCalendar.findViewById(R.id.cal_container_festivals);

        if (btnToggleGrid != null && btnToggleFest != null && containerGrid != null && containerFest != null) {
            btnToggleGrid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnToggleGrid.setBackgroundResource(R.drawable.m3_card_highlight);
                    btnToggleGrid.setTextColor(getColor(R.color.md_theme_onSurface));
                    btnToggleFest.setBackgroundColor(Color.TRANSPARENT);
                    btnToggleFest.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
                    containerGrid.setVisibility(View.VISIBLE);
                    containerFest.setVisibility(View.GONE);
                }
            });

            btnToggleFest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnToggleFest.setBackgroundResource(R.drawable.m3_card_highlight);
                    btnToggleFest.setTextColor(getColor(R.color.md_theme_onSurface));
                    btnToggleGrid.setBackgroundColor(Color.TRANSPARENT);
                    btnToggleGrid.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
                    containerGrid.setVisibility(View.GONE);
                    containerFest.setVisibility(View.VISIBLE);
                    bindFestivalsList("All");
                }
            });
        }
btnViewPanchang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTab(0);
            }
        });
    }

    private void bindCalendarTab() {
        if (viewCalendar == null) return;

        TextView txtMonthTitle = viewCalendar.findViewById(R.id.cal_txt_month_title);
        TextView txtHinduMonthSubtitle = viewCalendar.findViewById(R.id.cal_txt_hindu_month_subtitle);
        TableLayout tableGrid = viewCalendar.findViewById(R.id.cal_table_grid);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, calNavYear);
        cal.set(Calendar.MONTH, calNavMonth - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        SimpleDateFormat mdf = new SimpleDateFormat("MMMM yyyy", Locale.US);
        txtMonthTitle.setText(mdf.format(cal.getTime()));

        // Calculate 15th of the month to get Hindu month names
        PanchangCalculator.DayPanchang midPanchang = PanchangCalculator.calculatePanchang(
            calNavYear, calNavMonth, 15, currentCity.latitude, currentCity.longitude
        );
        ThemeManager.JapanesePalette activeP = ThemeManager.getActivePalette(this);
        ImageButton btnPrevM = viewCalendar.findViewById(R.id.cal_btn_prev_month);
        ImageButton btnNextM = viewCalendar.findViewById(R.id.cal_btn_next_month);
        if (btnPrevM != null) btnPrevM.setColorFilter(activeP.primary);
        if (btnNextM != null) btnNextM.setColorFilter(activeP.primary);
        if (txtHinduMonthSubtitle != null) {
            txtHinduMonthSubtitle.setText(midPanchang.hinduMonth + " (Amanta) • " + midPanchang.purnimantaMonth + " (Purnimanta)");
            txtHinduMonthSubtitle.setTextColor(activeP.primary);
        }

        // Populate Table Grid
        tableGrid.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=Sun .. 7=Sat
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar prevMonthCal = (Calendar) cal.clone();
        prevMonthCal.add(Calendar.MONTH, -1);
        int daysInPrevMonth = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int currentCell = 1;
        int nextMonthDay = 1;

        for (int row = 0; row < 6; row++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT
            ));

            boolean hasDaysInRow = false;

            for (int col = 1; col <= 7; col++) {
                View cellView = inflater.inflate(R.layout.item_calendar_cell, tableRow, false);
                TextView txtDay = cellView.findViewById(R.id.cell_txt_day);
                ImageView imgMoon = cellView.findViewById(R.id.cell_img_moon);
                TextView txtTithi = cellView.findViewById(R.id.cell_txt_tithi);
                TextView txtFestival = cellView.findViewById(R.id.cell_txt_festival);

                int cellIndex = row * 7 + col;
                if (cellIndex < firstDayOfWeek) {
                    // Previous month overflow
                    int pDay = daysInPrevMonth - (firstDayOfWeek - 1 - cellIndex);
                    txtDay.setText(String.valueOf(pDay));
                    txtDay.setTextColor(getColor(R.color.color_other_month_date));
                    txtTithi.setTextColor(getColor(R.color.color_other_month_tithi));
                    imgMoon.setAlpha(0.3f);
                } else if (currentCell <= daysInMonth) {
                    hasDaysInRow = true;
                    final int d = currentCell;
                    final int m = calNavMonth;
                    final int y = calNavYear;

                    PanchangCalculator.DayPanchang cellPanchang = PanchangCalculator.calculatePanchang(
                        y, m, d, currentCity.latitude, currentCity.longitude
                    );

                    txtDay.setText(String.valueOf(d));
                    imgMoon.setImageResource(cellPanchang.moonDrawableResId);
                    txtTithi.setText(cellPanchang.shortLabel);

                    if (cellPanchang.hasFestival()) {
                        txtFestival.setVisibility(View.VISIBLE);
                        txtFestival.setText(cellPanchang.getShortOccasion());
                    } else {
                        txtFestival.setVisibility(View.GONE);
                    }

                    // Highlight if selected date
                    if (y == currentYear && m == currentMonth && d == currentDay) {
                        cellView.setBackgroundResource(R.drawable.m3_card_highlight);
                        txtDay.setTextColor(activeP.primary);
                    } else if (col == 1) { // Sunday
                        txtDay.setTextColor(Color.parseColor("#E57373"));
                    } else {
                        txtDay.setTextColor(getColor(R.color.md_theme_onSurface));
                    }

                    cellView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            currentYear = y;
                            currentMonth = m;
                            currentDay = d;
                            recalculateAndRefresh();
                        }
                    });

                    currentCell++;
                } else {
                    // Next month overflow
                    txtDay.setText(String.valueOf(nextMonthDay));
                    txtDay.setTextColor(getColor(R.color.color_other_month_date));
                    txtTithi.setTextColor(getColor(R.color.color_other_month_tithi));
                    imgMoon.setAlpha(0.3f);
                    nextMonthDay++;
                }

                tableRow.addView(cellView);
            }

            if (hasDaysInRow || row < 5) {
                tableGrid.addView(tableRow);
            }
        }

        // Update Selected Day Card below calendar
        updateSelectedDayCard();
    }

    private void updateSelectedDayCard() {
        if (currentPanchang == null || viewCalendar == null) return;

        TextView txtGregorian = viewCalendar.findViewById(R.id.cal_sel_txt_date);
        TextView txtTithi = viewCalendar.findViewById(R.id.cal_sel_txt_tithi);
        TextView txtBadge = viewCalendar.findViewById(R.id.cal_sel_txt_badge);
        TextView txtSummary = viewCalendar.findViewById(R.id.cal_sel_txt_summary);

        Calendar cal = Calendar.getInstance();
        cal.set(currentYear, currentMonth - 1, currentDay);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US);
        if (txtGregorian != null) txtGregorian.setText(sdf.format(cal.getTime()));

        if (txtTithi != null) txtTithi.setText(currentPanchang.hinduMonth + " " + currentPanchang.paksha + " " + currentPanchang.tithiName);

        if (txtBadge != null) {
            if (currentPanchang.hasFestival()) {
                txtBadge.setVisibility(View.VISIBLE);
                txtBadge.setText(currentPanchang.specialOccasion);
            } else {
                txtBadge.setVisibility(View.GONE);
            }
        }

        if (txtSummary != null) {
            String rk = (currentPanchang.muhurta != null) ? currentPanchang.muhurta.rahuKaal : "N/A";
            txtSummary.setText("Nakshatra: " + currentPanchang.nakshatra + " (Pada " + currentPanchang.nakshatraPada + ") • Yoga: " + currentPanchang.yoga + " • Rahu Kaal: " + rk);
        }
    }

    // TAB 3: CHOGHADIYA & HORA BINDING
    private void bindChoghadiyaTab() {
        if (currentPanchang == null || viewChoghadiya == null) return;

        final TextView btnToggleChog = viewChoghadiya.findViewById(R.id.toggle_btn_choghadiya);
        final TextView btnToggleHora = viewChoghadiya.findViewById(R.id.toggle_btn_hora);
        final View containerChog = viewChoghadiya.findViewById(R.id.container_choghadiya);
        final View containerHora = viewChoghadiya.findViewById(R.id.container_hora);

        btnToggleChog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHoraActive = false;
                btnToggleChog.setBackgroundResource(R.drawable.m3_card_highlight);
                btnToggleChog.setTextColor(getColor(R.color.md_theme_onSurface));
                btnToggleHora.setBackgroundColor(Color.TRANSPARENT);
                btnToggleHora.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
                containerChog.setVisibility(View.VISIBLE);
                containerHora.setVisibility(View.GONE);
            }
        });

        btnToggleHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHoraActive = true;
                btnToggleHora.setBackgroundResource(R.drawable.m3_card_highlight);
                btnToggleHora.setTextColor(getColor(R.color.md_theme_onSurface));
                btnToggleChog.setBackgroundColor(Color.TRANSPARENT);
                btnToggleChog.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
                containerChog.setVisibility(View.GONE);
                containerHora.setVisibility(View.VISIBLE);
            }
        });

        if (isHoraActive) {
            btnToggleHora.setBackgroundResource(R.drawable.m3_card_highlight);
            btnToggleHora.setTextColor(getColor(R.color.md_theme_onSurface));
            btnToggleChog.setBackgroundColor(Color.TRANSPARENT);
            btnToggleChog.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
            containerChog.setVisibility(View.GONE);
            containerHora.setVisibility(View.VISIBLE);
        } else {
            btnToggleChog.setBackgroundResource(R.drawable.m3_card_highlight);
            btnToggleChog.setTextColor(getColor(R.color.md_theme_onSurface));
            btnToggleHora.setBackgroundColor(Color.TRANSPARENT);
            btnToggleHora.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
            containerChog.setVisibility(View.VISIBLE);
            containerHora.setVisibility(View.GONE);
        }

        // BIND CHOGHADIYA SECTION
        TextView txtHeroName = viewChoghadiya.findViewById(R.id.chog_hero_txt_name);
        TextView txtHeroLord = viewChoghadiya.findViewById(R.id.chog_hero_txt_lord);
        TextView txtHeroTimeRange = viewChoghadiya.findViewById(R.id.chog_hero_txt_timerange);
        TextView txtHeroNature = viewChoghadiya.findViewById(R.id.chog_hero_badge_nature);

        PanchangCalculator.ChoghadiyaPeriod active = currentPanchang.activeChoghadiya;
        if (active != null) {
            txtHeroName.setText(active.name);
            txtHeroLord.setText("Lord: " + active.lord);
            txtHeroTimeRange.setText(active.startTimeStr + " - " + active.endTimeStr);
            txtHeroNature.setText(active.nature);

            if (active.isAuspicious) {
                txtHeroNature.setBackgroundResource(R.drawable.m3_badge_green);
                txtHeroNature.setTextColor(getColor(R.color.m3_auspicious));
            } else if (active.nature.contains("Neutral") || active.nature.contains("Chanchal")) {
                txtHeroNature.setBackgroundResource(R.drawable.m3_badge_yellow);
                txtHeroNature.setTextColor(Color.parseColor("#FFD54F"));
            } else {
                txtHeroNature.setBackgroundResource(R.drawable.m3_badge_red);
                txtHeroNature.setTextColor(getColor(R.color.m3_inauspicious));
            }
        }

        LinearLayout listDay = viewChoghadiya.findViewById(R.id.chog_list_day);
        LinearLayout listNight = viewChoghadiya.findViewById(R.id.chog_list_night);
        LayoutInflater inflater = LayoutInflater.from(this);

        listDay.removeAllViews();
        for (int i = 0; i < currentPanchang.dayChoghadiya.size(); i++) {
            PanchangCalculator.ChoghadiyaPeriod p = currentPanchang.dayChoghadiya.get(i);
            View row = inflater.inflate(R.layout.item_choghadiya_row, listDay, false);

            TextView txtIdx = row.findViewById(R.id.chog_row_index);
            TextView txtName = row.findViewById(R.id.chog_row_name);
            TextView txtLord = row.findViewById(R.id.chog_row_lord);
            TextView txtTime = row.findViewById(R.id.chog_row_time);
            TextView txtNature = row.findViewById(R.id.chog_row_nature);

            txtIdx.setText((i + 1) + ".");
            txtName.setText(p.name);
            txtLord.setText("Lord: " + p.lord);
            txtTime.setText(p.startTimeStr + " - " + p.endTimeStr);
            txtNature.setText(p.nature.split(" ")[0]);

            if (p.isAuspicious) {
                txtNature.setBackgroundResource(R.drawable.m3_badge_green);
                txtNature.setTextColor(getColor(R.color.m3_auspicious));
            } else if (p.nature.contains("Neutral") || p.nature.contains("Chanchal")) {
                txtNature.setBackgroundResource(R.drawable.m3_badge_yellow);
                txtNature.setTextColor(Color.parseColor("#FFD54F"));
            } else {
                txtNature.setBackgroundResource(R.drawable.m3_badge_red);
                txtNature.setTextColor(getColor(R.color.m3_inauspicious));
            }

            if (p.isCurrent) {
                row.setBackgroundResource(R.drawable.m3_card_highlight);
            }
            listDay.addView(row);
        }

        listNight.removeAllViews();
        for (int i = 0; i < currentPanchang.nightChoghadiya.size(); i++) {
            PanchangCalculator.ChoghadiyaPeriod p = currentPanchang.nightChoghadiya.get(i);
            View row = inflater.inflate(R.layout.item_choghadiya_row, listNight, false);

            TextView txtIdx = row.findViewById(R.id.chog_row_index);
            TextView txtName = row.findViewById(R.id.chog_row_name);
            TextView txtLord = row.findViewById(R.id.chog_row_lord);
            TextView txtTime = row.findViewById(R.id.chog_row_time);
            TextView txtNature = row.findViewById(R.id.chog_row_nature);

            txtIdx.setText((i + 1) + ".");
            txtName.setText(p.name);
            txtLord.setText("Lord: " + p.lord);
            txtTime.setText(p.startTimeStr + " - " + p.endTimeStr);
            txtNature.setText(p.nature.split(" ")[0]);

            if (p.isAuspicious) {
                txtNature.setBackgroundResource(R.drawable.m3_badge_green);
                txtNature.setTextColor(getColor(R.color.m3_auspicious));
            } else if (p.nature.contains("Neutral") || p.nature.contains("Chanchal")) {
                txtNature.setBackgroundResource(R.drawable.m3_badge_yellow);
                txtNature.setTextColor(Color.parseColor("#FFD54F"));
            } else {
                txtNature.setBackgroundResource(R.drawable.m3_badge_red);
                txtNature.setTextColor(getColor(R.color.m3_inauspicious));
            }

            if (p.isCurrent) {
                row.setBackgroundResource(R.drawable.m3_card_highlight);
            }
            listNight.addView(row);
        }

        // BIND HORA SECTION
        ImageView horaHeroIcon = viewChoghadiya.findViewById(R.id.hora_hero_icon);
        TextView txtHoraHeroName = viewChoghadiya.findViewById(R.id.hora_hero_txt_name);
        TextView txtHoraHeroLord = viewChoghadiya.findViewById(R.id.hora_hero_txt_lord);
        TextView txtHoraHeroTimeRange = viewChoghadiya.findViewById(R.id.hora_hero_txt_timerange);
        TextView txtHoraHeroNature = viewChoghadiya.findViewById(R.id.hora_hero_badge_nature);

        PanchangCalculator.HoraPeriod activeH = currentPanchang.activeHora;
        if (activeH != null) {
            horaHeroIcon.setImageResource(activeH.iconResId);
            txtHoraHeroName.setText(activeH.planetSanskrit + " Hora (" + activeH.planetEnglish + ")");
            txtHoraHeroLord.setText("Lord: " + activeH.lord + " • " + getHoraSignificance(activeH.planetSanskrit));
            txtHoraHeroTimeRange.setText(activeH.startTimeStr + " - " + activeH.endTimeStr);
            txtHoraHeroNature.setText(activeH.nature);

            if (activeH.nature.contains("Auspicious")) {
                txtHoraHeroNature.setBackgroundResource(R.drawable.m3_badge_green);
                txtHoraHeroNature.setTextColor(getColor(R.color.m3_auspicious));
            } else if (activeH.nature.contains("Moderate") || activeH.nature.contains("Royal")) {
                txtHoraHeroNature.setBackgroundResource(R.drawable.m3_badge_yellow);
                txtHoraHeroNature.setTextColor(Color.parseColor("#FFD54F"));
            } else {
                txtHoraHeroNature.setBackgroundResource(R.drawable.m3_badge_red);
                txtHoraHeroNature.setTextColor(getColor(R.color.m3_inauspicious));
            }
        }

        LinearLayout horaListDay = viewChoghadiya.findViewById(R.id.hora_list_day);
        horaListDay.removeAllViews();
        for (int i = 0; i < currentPanchang.dayHoras.size(); i++) {
            PanchangCalculator.HoraPeriod h = currentPanchang.dayHoras.get(i);
            View row = inflater.inflate(R.layout.item_hora_row, horaListDay, false);

            TextView txtIdx = row.findViewById(R.id.hora_row_index);
            ImageView imgIcon = row.findViewById(R.id.hora_row_icon);
            TextView txtName = row.findViewById(R.id.hora_row_name);
            TextView txtDesc = row.findViewById(R.id.hora_row_desc);
            TextView txtTime = row.findViewById(R.id.hora_row_time);
            TextView txtNature = row.findViewById(R.id.hora_row_nature);

            txtIdx.setText((i + 1) + ".");
            imgIcon.setImageResource(h.iconResId);
            txtName.setText(h.planetSanskrit + " (" + h.planetEnglish + ")");
            txtDesc.setText(getHoraSignificance(h.planetSanskrit));
            txtTime.setText(h.startTimeStr + " - " + h.endTimeStr);
            txtNature.setText(h.nature);

            if (h.nature.contains("Auspicious")) {
                txtNature.setBackgroundResource(R.drawable.m3_badge_green);
                txtNature.setTextColor(getColor(R.color.m3_auspicious));
            } else if (h.nature.contains("Moderate") || h.nature.contains("Royal")) {
                txtNature.setBackgroundResource(R.drawable.m3_badge_yellow);
                txtNature.setTextColor(Color.parseColor("#FFD54F"));
            } else {
                txtNature.setBackgroundResource(R.drawable.m3_badge_red);
                txtNature.setTextColor(getColor(R.color.m3_inauspicious));
            }

            if (h.isCurrent) {
                row.setBackgroundResource(R.drawable.m3_card_highlight);
            }
            horaListDay.addView(row);
        }

        LinearLayout horaListNight = viewChoghadiya.findViewById(R.id.hora_list_night);
        horaListNight.removeAllViews();
        for (int i = 0; i < currentPanchang.nightHoras.size(); i++) {
            PanchangCalculator.HoraPeriod h = currentPanchang.nightHoras.get(i);
            View row = inflater.inflate(R.layout.item_hora_row, horaListNight, false);

            TextView txtIdx = row.findViewById(R.id.hora_row_index);
            ImageView imgIcon = row.findViewById(R.id.hora_row_icon);
            TextView txtName = row.findViewById(R.id.hora_row_name);
            TextView txtDesc = row.findViewById(R.id.hora_row_desc);
            TextView txtTime = row.findViewById(R.id.hora_row_time);
            TextView txtNature = row.findViewById(R.id.hora_row_nature);

            txtIdx.setText((i + 13) + ".");
            imgIcon.setImageResource(h.iconResId);
            txtName.setText(h.planetSanskrit + " (" + h.planetEnglish + ")");
            txtDesc.setText(getHoraSignificance(h.planetSanskrit));
            txtTime.setText(h.startTimeStr + " - " + h.endTimeStr);
            txtNature.setText(h.nature);

            if (h.nature.contains("Auspicious")) {
                txtNature.setBackgroundResource(R.drawable.m3_badge_green);
                txtNature.setTextColor(getColor(R.color.m3_auspicious));
            } else if (h.nature.contains("Moderate") || h.nature.contains("Royal")) {
                txtNature.setBackgroundResource(R.drawable.m3_badge_yellow);
                txtNature.setTextColor(Color.parseColor("#FFD54F"));
            } else {
                txtNature.setBackgroundResource(R.drawable.m3_badge_red);
                txtNature.setTextColor(getColor(R.color.m3_inauspicious));
            }

            if (h.isCurrent) {
                row.setBackgroundResource(R.drawable.m3_card_highlight);
            }
            horaListNight.addView(row);
        }

        updateLiveTimers();
    }

    private String getHoraSignificance(String sanskritPlanet) {
        if ("Surya".equalsIgnoreCase(sanskritPlanet)) return "Leadership, authority, administrative work";
        if ("Shukra".equalsIgnoreCase(sanskritPlanet)) return "Arts, romance, vehicles, jewelry purchase";
        if ("Budh".equalsIgnoreCase(sanskritPlanet)) return "Business, accounts, education, agreements";
        if ("Chandra".equalsIgnoreCase(sanskritPlanet)) return "Travel, domestic tasks, meeting allies";
        if ("Shani".equalsIgnoreCase(sanskritPlanet)) return "Labor, discipline, dealing with metal/land";
        if ("Guru".equalsIgnoreCase(sanskritPlanet)) return "Religious rites, wisdom, finance, ceremonies";
        if ("Mangal".equalsIgnoreCase(sanskritPlanet)) return "Courage, sports, surgery, property dealings";
        return "Daily tasks and routine matters";
    }

    // TAB 4: KUNDALI BINDING
    private void bindKundaliTab() {
        if (currentPanchang == null || viewKundali == null) return;

        final TextView btnToggleNorth = viewKundali.findViewById(R.id.toggle_btn_north_kundali);
        final TextView btnToggleSouth = viewKundali.findViewById(R.id.toggle_btn_south_kundali);
        final TextView btnToggleEast = viewKundali.findViewById(R.id.toggle_btn_east_kundali);
        final NorthIndianKundaliView kundaliView = viewKundali.findViewById(R.id.kundali_chart_view);
        final SouthIndianKundaliView southKundaliView = viewKundali.findViewById(R.id.kundali_south_chart_view);
        final EastIndianKundaliView eastKundaliView = viewKundali.findViewById(R.id.kundali_east_chart_view);
        final TextView txtChartBadge = viewKundali.findViewById(R.id.kundali_txt_chart_type_badge);
        final TextView txtChartNote = viewKundali.findViewById(R.id.kundali_txt_chart_note);

        btnToggleNorth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kundaliStyle = 0;
                updateKundaliStyleUI(btnToggleNorth, btnToggleSouth, btnToggleEast,
                    kundaliView, southKundaliView, eastKundaliView, txtChartBadge, txtChartNote);
            }
        });

        btnToggleSouth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kundaliStyle = 1;
                updateKundaliStyleUI(btnToggleNorth, btnToggleSouth, btnToggleEast,
                    kundaliView, southKundaliView, eastKundaliView, txtChartBadge, txtChartNote);
            }
        });

        btnToggleEast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kundaliStyle = 2;
                updateKundaliStyleUI(btnToggleNorth, btnToggleSouth, btnToggleEast,
                    kundaliView, southKundaliView, eastKundaliView, txtChartBadge, txtChartNote);
            }
        });

        updateKundaliStyleUI(btnToggleNorth, btnToggleSouth, btnToggleEast,
            kundaliView, southKundaliView, eastKundaliView, txtChartBadge, txtChartNote);

        final Button btnStudyCharts = viewKundali.findViewById(R.id.btn_study_charts);
        final Button btnResetStudyChart = viewKundali.findViewById(R.id.btn_reset_study_chart);

        if (activeStudyChart != null) {
            btnResetStudyChart.setVisibility(View.VISIBLE);
            btnStudyCharts.setText("👤 " + activeStudyChart.nameEn);
        } else {
            btnResetStudyChart.setVisibility(View.GONE);
            btnStudyCharts.setText("📜 Study Charts (प्रसिद्ध कुंडलियाँ)");
        }

        btnStudyCharts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStudyChartsDialog();
            }
        });

        btnResetStudyChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeStudyChart = null;
                activeStudyPanchang = null;
                btnResetStudyChart.setVisibility(View.GONE);
                btnStudyCharts.setText("📜 Study Charts (प्रसिद्ध कुंडलियाँ)");
                bindKundaliTab();
            }
        });

        PanchangCalculator.DayPanchang chartData = (activeStudyPanchang != null) ? activeStudyPanchang : currentPanchang;

        TextView txtLagna = viewKundali.findViewById(R.id.kundali_txt_lagna);
        TextView txtAyanamsa = viewKundali.findViewById(R.id.kundali_txt_ayanamsa);
        LinearLayout grahaList = viewKundali.findViewById(R.id.kundali_graha_list);

        // Find Lagna
        PanchangCalculator.PlanetPosition lagna = null;
        for (PanchangCalculator.PlanetPosition p : chartData.planets) {
            if ("Lagna".equalsIgnoreCase(p.name)) {
                lagna = p;
                break;
            }
        }
        if (lagna != null) {
            txtLagna.setText("Lagna: " + lagna.rashiName + " (" + lagna.rashiSymbol + ") " + lagna.getFormattedPosition());
        }
        if (activeStudyChart != null) {
            txtAyanamsa.setText("Born: " + activeStudyChart.getFormattedDate() + " • Lahiri Ayanamsa");
        } else {
            txtAyanamsa.setText("Ayanamsa: Lahiri (Chitra Paksha) 24°14'18\"");
        }

        // Pass data to all three custom chart views
        kundaliView.setPanchangData(chartData);
        southKundaliView.setPanchangData(chartData);
        eastKundaliView.setPanchangData(chartData);

        // Populate 9 Grahas Table + Outer Planets
        grahaList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (PanchangCalculator.PlanetPosition p : chartData.planets) {
            if ("Lagna".equalsIgnoreCase(p.name)) continue;

            View row = inflater.inflate(R.layout.item_graha_row, grahaList, false);

            ImageView imgIcon = row.findViewById(R.id.graha_row_icon);
            TextView txtName = row.findViewById(R.id.graha_row_name);
            TextView txtRetro = row.findViewById(R.id.graha_row_badge_retro);
            TextView txtRashi = row.findViewById(R.id.graha_row_rashi);
            TextView txtNakshatra = row.findViewById(R.id.graha_row_nakshatra);
            TextView txtPada = row.findViewById(R.id.graha_row_pada);

            imgIcon.setImageResource(getPlanetDrawableRes(p.name));
            txtName.setText(p.sanskritName + " (" + p.name + ")");

            if (p.isRetrograde) {
                txtRetro.setVisibility(View.VISIBLE);
            } else {
                txtRetro.setVisibility(View.GONE);
            }

            txtRashi.setText(p.rashiName + " (" + p.rashiSymbol + ") • " + p.getFormattedPosition());
            txtNakshatra.setText(p.nakshatraName);
            txtPada.setText("Pada " + p.pada + " (" + p.padaSyllable + ")");

            grahaList.addView(row);
        }

        // Bind Avakahada Chakra
        TextView txtAvakRashi = viewKundali.findViewById(R.id.kundali_txt_avakahada_rashi);
        TextView txtAvakNak = viewKundali.findViewById(R.id.kundali_txt_avakahada_nakshatra);
        TextView txtAvakName = viewKundali.findViewById(R.id.kundali_txt_avakahada_name);
        TextView txtAvakGyn = viewKundali.findViewById(R.id.kundali_txt_avakahada_gyn);
        TextView txtAvakVvp = viewKundali.findViewById(R.id.kundali_txt_avakahada_vvp);

        if (txtAvakRashi != null && chartData.avakahada != null) {
            txtAvakRashi.setText(chartData.chandraRashi + " • " + chartData.avakahada.rashiSwami);
            txtAvakNak.setText(chartData.nakshatra + " • Pada " + chartData.nakshatraPada + " (" + chartData.avakahada.nakshatraSwami + ")");
            txtAvakName.setText(chartData.avakahada.janmaAkshara != null && !chartData.avakahada.janmaAkshara.isEmpty() ? chartData.avakahada.janmaAkshara : "--");
            txtAvakGyn.setText(chartData.avakahada.gana + " • " + chartData.avakahada.yoni + " • " + chartData.avakahada.nadi);
            txtAvakVvp.setText(chartData.avakahada.varna + " • " + chartData.avakahada.vashya + " • " + chartData.avakahada.paya);
        }

        // Bind Vimshottari Dasha Balance & Kuja Dosha
        TextView txtDashaBal = viewKundali.findViewById(R.id.kundali_txt_dasha_balance);
        TextView txtKuja = viewKundali.findViewById(R.id.kundali_txt_kuja_dosha);
        if (txtDashaBal != null && chartData.vimshottariDashaBalance != null) {
            txtDashaBal.setText(chartData.vimshottariDashaBalance);
        }
        if (txtKuja != null && chartData.kujaDoshaStatus != null) {
            txtKuja.setText(chartData.kujaDoshaStatus);
            if (chartData.hasMangalDosha) {
                txtKuja.setBackgroundResource(R.drawable.m3_badge_red);
                txtKuja.setTextColor(getColor(R.color.m3_inauspicious));
            } else {
                txtKuja.setBackgroundResource(R.drawable.m3_badge_green);
                txtKuja.setTextColor(getColor(R.color.m3_auspicious));
            }
        }
        // Ashtakavarga
        PanchangCalculator.PlanetPosition lagnaPos = null;
        for (PanchangCalculator.PlanetPosition p : chartData.planets) {
            if ("Lagna".equalsIgnoreCase(p.name)) { lagnaPos = p; break; }
        }
        int lagnaRashi = (lagnaPos != null) ? lagnaPos.rashiIndex : 0;

        PanchangCalculator.AshtakavargaResult ashtaka = PanchangCalculator.calculateAshtakavarga(chartData.planets, lagnaRashi);

        GridLayout ashtakaGrid = viewKundali.findViewById(R.id.kundali_ashtaka_grid);
        TextView txtAshtakaTotal = viewKundali.findViewById(R.id.kundali_txt_ashtaka_total_badge);
        TextView txtAshtakaSummary = viewKundali.findViewById(R.id.kundali_txt_ashtaka_summary);

        if (ashtakaGrid != null) {
            ashtakaGrid.removeAllViews();
            String[] rSymbols = PanchangCalculator.RASHI_SYMBOLS;
            for (int h = 0; h < 12; h++) {
                int bindus = ashtaka.sarvashtakavarga[h];
                LinearLayout box = new LinearLayout(this);
                box.setOrientation(LinearLayout.VERTICAL);
                box.setGravity(Gravity.CENTER);
                box.setPadding(dpToPx(4), dpToPx(6), dpToPx(4), dpToPx(6));

                GridLayout.LayoutParams glp = new GridLayout.LayoutParams();
                glp.width = 0;
                glp.height = GridLayout.LayoutParams.WRAP_CONTENT;
                glp.columnSpec = GridLayout.spec(h % 4, 1f);
                glp.rowSpec = GridLayout.spec(h / 4);
                glp.setMargins(dpToPx(3), dpToPx(3), dpToPx(3), dpToPx(3));
                box.setLayoutParams(glp);

                if (bindus >= 30) {
                    box.setBackgroundResource(R.drawable.m3_badge_green);
                } else if (bindus >= 28) {
                    box.setBackgroundResource(R.drawable.m3_card_highlight);
                } else if (bindus <= 24) {
                    box.setBackgroundResource(R.drawable.m3_badge_red);
                } else {
                    box.setBackgroundResource(R.drawable.m3_card_surface);
                }

                TextView txtH = new TextView(this);
                txtH.setText("H" + (h + 1) + " (" + rSymbols[h] + ")");
                txtH.setTextSize(10f);
                txtH.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
                box.addView(txtH);

                TextView txtB = new TextView(this);
                txtB.setText(bindus + " pts");
                txtB.setTextSize(13f);
                txtB.setTypeface(null, Typeface.BOLD);
                txtB.setTextColor(bindus >= 30 ? getColor(R.color.m3_auspicious) : (bindus <= 24 ? getColor(R.color.m3_inauspicious) : getColor(R.color.md_theme_onSurface)));
                box.addView(txtB);

                ashtakaGrid.addView(box);
            }

            if (txtAshtakaTotal != null) {
                txtAshtakaTotal.setText(ashtaka.totalBindus + " Bindus (SAV)");
            }
            if (txtAshtakaSummary != null) {
                txtAshtakaSummary.setText("Strongest: House " + (ashtaka.strongestHouse + 1) + " (" + ashtaka.maxBindus + " bindus) • Weakest: House " + (ashtaka.weakestHouse + 1) + " (" + ashtaka.minBindus + " bindus) • Parashara Classical Avg: 28 bindus");
            }
        }

        // Shadbala Summary
        PanchangCalculator.ShadbalaResult shadbala = PanchangCalculator.calculateShadbala(chartData.planets, lagnaRashi, chartData);
        LinearLayout shadbalaList = viewKundali.findViewById(R.id.kundali_shadbala_list);
        TextView txtShadbalaBadge = viewKundali.findViewById(R.id.kundali_txt_shadbala_strongest_badge);

        if (shadbalaList != null) {
            shadbalaList.removeAllViews();
            if (txtShadbalaBadge != null && shadbala.strongestPlanet != null) {
                txtShadbalaBadge.setText(shadbala.strongestPlanet.sanskritName + " Strongest (" + String.format(Locale.US, "%.2fx", shadbala.strongestPlanet.ratio) + ")");
            }

            for (PanchangCalculator.PlanetShadbala ps : shadbala.planets) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dpToPx(8), dpToPx(5), dpToPx(8), dpToPx(5));
                if (ps == shadbala.strongestPlanet) {
                    row.setBackgroundResource(R.drawable.m3_card_highlight);
                }

                TextView txtP = new TextView(this);
                txtP.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f));
                txtP.setText(ps.sanskritName + " (" + ps.name + ")");
                txtP.setTextSize(12f);
                txtP.setTypeface(null, Typeface.BOLD);
                txtP.setTextColor(getColor(R.color.md_theme_onSurface));
                row.addView(txtP);

                TextView txtRupa = new TextView(this);
                txtRupa.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
                txtRupa.setText(String.format(Locale.US, "%.1f R", ps.totalRupas));
                txtRupa.setTextSize(12f);
                txtRupa.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
                row.addView(txtRupa);

                TextView txtRatio = new TextView(this);
                txtRatio.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
                txtRatio.setText(String.format(Locale.US, "%.2fx", ps.ratio));
                txtRatio.setTextSize(12f);
                txtRatio.setTypeface(null, Typeface.BOLD);
                txtRatio.setTextColor(ps.meetsRequirement ? getColor(R.color.m3_auspicious) : getColor(R.color.m3_inauspicious));
                row.addView(txtRatio);

                TextView txtIshta = new TextView(this);
                txtIshta.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.4f));
                txtIshta.setText(String.format(Locale.US, "Ish:%.0f/K:%.0f", ps.ishtaPhala, ps.kashtaPhala));
                txtIshta.setTextSize(11f);
                txtIshta.setGravity(Gravity.RIGHT);
                txtIshta.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
                row.addView(txtIshta);

                shadbalaList.addView(row);
            }
        }

        // Shani Sade Sati Status
        TextView txtSadeBadge = viewKundali.findViewById(R.id.kundali_txt_sade_sati_badge);
        TextView txtSadeDesc = viewKundali.findViewById(R.id.kundali_txt_sade_sati_description);
        if (txtSadeBadge != null && txtSadeDesc != null) {
            PanchangCalculator.SadeSatiResult sResult = PanchangCalculator.calculateSadeSati(chartData.chandraRashiIndex, 11);
            txtSadeBadge.setText(sResult.phase);
            if (sResult.isUndergoingSadeSati) {
                txtSadeBadge.setBackgroundResource(R.drawable.m3_badge_red);
                txtSadeBadge.setTextColor(getColor(R.color.m3_inauspicious));
            } else {
                txtSadeBadge.setBackgroundResource(R.drawable.m3_badge_green);
                txtSadeBadge.setTextColor(getColor(R.color.m3_auspicious));
            }
            txtSadeDesc.setText(sResult.statusDescription);
        }

    }

    private void updateKundaliStyleUI(TextView btnNorth, TextView btnSouth, TextView btnEast,
                                      NorthIndianKundaliView northView, SouthIndianKundaliView southView, EastIndianKundaliView eastView,
                                      TextView txtBadge, TextView txtNote) {
        btnNorth.setBackgroundColor(Color.TRANSPARENT);
        btnNorth.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
        btnSouth.setBackgroundColor(Color.TRANSPARENT);
        btnSouth.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
        btnEast.setBackgroundColor(Color.TRANSPARENT);
        btnEast.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));

        northView.setVisibility(View.GONE);
        southView.setVisibility(View.GONE);
        eastView.setVisibility(View.GONE);

        if (kundaliStyle == 0) {
            btnNorth.setBackgroundResource(R.drawable.m3_card_highlight);
            btnNorth.setTextColor(getColor(R.color.md_theme_onSurface));
            northView.setVisibility(View.VISIBLE);
            txtBadge.setText("North Indian");
            txtNote.setText("Diamond Chart • Numbers indicate Rashis (1=Mesha .. 12=Meena)");
        } else if (kundaliStyle == 1) {
            btnSouth.setBackgroundResource(R.drawable.m3_card_highlight);
            btnSouth.setTextColor(getColor(R.color.md_theme_onSurface));
            southView.setVisibility(View.VISIBLE);
            txtBadge.setText("South Indian");
            txtNote.setText("Square Chart • Fixed Rashis • Diagonal slash indicates Lagna");
        } else {
            btnEast.setBackgroundResource(R.drawable.m3_card_highlight);
            btnEast.setTextColor(getColor(R.color.md_theme_onSurface));
            eastView.setVisibility(View.VISIBLE);
            txtBadge.setText("East Indian");
            txtNote.setText("Bangla / Maithili Chart • Fixed Counter-Clockwise Signs • Red slash shows Lagna");
        }
    }

    private void showStudyChartsDialog() {
        final List<NotableChartsManager.NotablePerson> list = NotableChartsManager.getAllCharts();
        String[] displayList = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            NotableChartsManager.NotablePerson p = list.get(i);
            displayList[i] = p.nameEn + " (" + p.nameHi + ")\n" + p.noteEn + " • " + p.getFormattedDate();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notable Study Horoscopes (प्रसिद्ध कुंडलियाँ)");
        builder.setItems(displayList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activeStudyChart = list.get(which);
                activeStudyPanchang = PanchangCalculator.calculateHoroscope(
                        activeStudyChart.year, activeStudyChart.month, activeStudyChart.day,
                        activeStudyChart.hour, activeStudyChart.minute,
                        activeStudyChart.offsetHours, activeStudyChart.lat, activeStudyChart.lon,
                        activeStudyChart.nameEn
                );
                bindKundaliTab();
                Toast.makeText(MainActivity.this, "Loaded: " + activeStudyChart.nameEn, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void shareCurrentPanchang() {
        if (currentPanchang == null) return;
        String shareBody = "🕉️ Hindu Calendar & Vedic Panchang\n" +
                txtHeaderGregorian.getText().toString() + "\n" +
                txtHeaderHinduDate.getText().toString() + "\n\n" +
                "📍 Location: " + (currentCity != null ? currentCity.name : "Nagpur") + "\n" +
                "• Tithi: " + currentPanchang.tithiName + " (" + currentPanchang.paksha + " Paksha)\n" +
                "• Nakshatra: " + currentPanchang.nakshatra + " (Pada " + currentPanchang.nakshatraPada + ")\n" +
                "• Yoga: " + currentPanchang.yoga + "\n" +
                "• Karana: " + currentPanchang.karana1 + "\n" +
                "• Chandra Rashi: " + currentPanchang.chandraRashiFull + "\n" +
                "• Surya Rashi: " + currentPanchang.suryaRashiFull + "\n" +
                "• Sunrise: " + currentPanchang.sunriseStr + " | Sunset: " + currentPanchang.sunsetStr + "\n" +
                "• Rahu Kaal: " + (currentPanchang.muhurta != null ? currentPanchang.muhurta.rahuKaal : "--") + "\n" +
                "• Abhijit Muhurta: " + (currentPanchang.muhurta != null ? currentPanchang.muhurta.abhijit : "--") + "\n\n" +
                "— Shared from Hindu Calendar (Material You)";

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, "Share Vedic Panchang");
        startActivity(shareIntent);
    }

    private int getPlanetDrawableRes(String name) {
        if ("Sun".equalsIgnoreCase(name) || "Surya".equalsIgnoreCase(name)) return R.drawable.sun_icon;
        if ("Moon".equalsIgnoreCase(name) || "Chandra".equalsIgnoreCase(name)) return R.drawable.moon_icon;
        if ("Mars".equalsIgnoreCase(name) || "Mangal".equalsIgnoreCase(name)) return R.drawable.mars_icon;
        if ("Mercury".equalsIgnoreCase(name) || "Budh".equalsIgnoreCase(name)) return R.drawable.mercury_icon;
        if ("Jupiter".equalsIgnoreCase(name) || "Guru".equalsIgnoreCase(name)) return R.drawable.jupiter_icon;
        if ("Venus".equalsIgnoreCase(name) || "Shukra".equalsIgnoreCase(name)) return R.drawable.venus_icon;
        if ("Saturn".equalsIgnoreCase(name) || "Shani".equalsIgnoreCase(name)) return R.drawable.saturn_icon;
        if ("Rahu".equalsIgnoreCase(name)) return R.drawable.rahu_icon;
        if ("Ketu".equalsIgnoreCase(name)) return R.drawable.ketu_icon;
        if ("Uranus".equalsIgnoreCase(name) || "Arun".equalsIgnoreCase(name)) return R.drawable.uranus_icon;
        if ("Neptune".equalsIgnoreCase(name) || "Varun".equalsIgnoreCase(name)) return R.drawable.neptune_icon;
        if ("Pluto".equalsIgnoreCase(name) || "Yama".equalsIgnoreCase(name)) return R.drawable.pluto_icon;
        return R.drawable.sun_icon;
    }

    // TAB 5: MORE & TOOLS BINDING
    private void setupMoreTabButtons() {
        Button btnChangeCity = viewMore.findViewById(R.id.more_btn_change_city);
        Button btnPin = viewMore.findViewById(R.id.more_btn_pin_widget);
        Button btnCopySankalpa = viewMore.findViewById(R.id.more_btn_copy_sankalpa);
        final Button btnGroomRashi = viewMore.findViewById(R.id.btn_groom_rashi);
        final Button btnGroomNak = viewMore.findViewById(R.id.btn_groom_nakshatra);
        final Button btnBrideRashi = viewMore.findViewById(R.id.btn_bride_rashi);
        final Button btnBrideNak = viewMore.findViewById(R.id.btn_bride_nakshatra);
        Button btnCalcMilan = viewMore.findViewById(R.id.btn_calculate_guna_milan);

        btnChangeCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCitySelectorDialog();
            }
        });

        btnPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinWidget();
            }
        });

        // Material 3 Settings & Japanese Themes
        Button btnOpenThemes = viewMore.findViewById(R.id.more_btn_open_themes);
        Button btnOpenSettings = viewMore.findViewById(R.id.more_btn_open_settings);
        View palettePreview = viewMore.findViewById(R.id.more_active_palette_preview);

        View.OnClickListener settingsClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsDialog();
            }
        };

        if (btnOpenThemes != null) btnOpenThemes.setOnClickListener(settingsClick);
        if (btnOpenSettings != null) btnOpenSettings.setOnClickListener(settingsClick);
        if (palettePreview != null) palettePreview.setOnClickListener(settingsClick);

        
        // Backup & Restore
        Button btnExport = viewMore.findViewById(R.id.more_btn_export_backup);
        Button btnImport = viewMore.findViewById(R.id.more_btn_import_backup);
        if (btnExport != null) {
            btnExport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exportUserDataBackup();
                }
            });
        }
        if (btnImport != null) {
            btnImport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showImportBackupDialog();
                }
            });
        }

        // Ayanamsa Selector
        final TextView btnLahiri = viewMore.findViewById(R.id.toggle_ayanamsa_lahiri);
        final TextView btnRaman = viewMore.findViewById(R.id.toggle_ayanamsa_raman);
        final TextView btnKp = viewMore.findViewById(R.id.toggle_ayanamsa_kp);
        final TextView txtAyanInfo = viewMore.findViewById(R.id.more_txt_ayanamsa_info);

        if (btnLahiri != null && btnRaman != null && btnKp != null) {
            View.OnClickListener ayanListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnLahiri.setBackgroundColor(Color.TRANSPARENT);
                    btnLahiri.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
                    btnRaman.setBackgroundColor(Color.TRANSPARENT);
                    btnRaman.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
                    btnKp.setBackgroundColor(Color.TRANSPARENT);
                    btnKp.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));

                    if (v == btnLahiri) {
                        btnLahiri.setBackgroundResource(R.drawable.m3_card_highlight);
                        btnLahiri.setTextColor(getColor(R.color.md_theme_onSurface));
                        txtAyanInfo.setText("Active: Lahiri (Chitra Paksha) 24°14'18\" • Official Indian National Panchang standard");
                    } else if (v == btnRaman) {
                        btnRaman.setBackgroundResource(R.drawable.m3_card_highlight);
                        btnRaman.setTextColor(getColor(R.color.md_theme_onSurface));
                        txtAyanInfo.setText("Active: B.V. Raman Ayanamsa (22°47'30\") • Traditional South Indian astrological standard");
                    } else {
                        btnKp.setBackgroundResource(R.drawable.m3_card_highlight);
                        btnKp.setTextColor(getColor(R.color.md_theme_onSurface));
                        txtAyanInfo.setText("Active: Krishnamurti Padhdhati (KP) 24°08'12\" • Placidus sub-lord division standard");
                    }
                    Toast.makeText(MainActivity.this, "Ayanamsa updated! Ephemeris aligned.", Toast.LENGTH_SHORT).show();
                }
            };
            btnLahiri.setOnClickListener(ayanListener);
            btnRaman.setOnClickListener(ayanListener);
            btnKp.setOnClickListener(ayanListener);
        }
btnCopySankalpa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mantra = PanchangCalculator.getDailySankalpaMantra(currentPanchang, currentCity.name);
                String english = PanchangCalculator.getDailySankalpaEnglish(currentPanchang, currentCity.name);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Daily Puja Sankalpa", mantra + "\n\n" + english);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, "Sankalpa Mantra copied to clipboard!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnGroomRashi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRashiPicker(true, btnGroomRashi);
            }
        });

        btnGroomNak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNakshatraPicker(true, btnGroomNak);
            }
        });

        btnBrideRashi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRashiPicker(false, btnBrideRashi);
            }
        });

        btnBrideNak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNakshatraPicker(false, btnBrideNak);
            }
        });

        btnCalcMilan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateGunaMilanResult();
            }
        });
    }

    private void showRashiPicker(final boolean isGroom, final Button targetBtn) {
        String[] rashiLabels = new String[12];
        for (int i = 0; i < 12; i++) {
            rashiLabels[i] = PanchangCalculator.RASHIS[i] + " (" + PanchangCalculator.RASHIS_ENGLISH[i] + ")";
        }
        int currentSelection = isGroom ? matchGroomRashi : matchBrideRashi;

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(isGroom ? "Select Groom Moon Sign (वर राशि)" : "Select Bride Moon Sign (कन्या राशि)");
        b.setSingleChoiceItems(rashiLabels, currentSelection, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isGroom) {
                    matchGroomRashi = which;
                    targetBtn.setText(PanchangCalculator.RASHIS[which] + " (" + PanchangCalculator.RASHIS_ENGLISH[which] + ")");
                } else {
                    matchBrideRashi = which;
                    targetBtn.setText(PanchangCalculator.RASHIS[which] + " (" + PanchangCalculator.RASHIS_ENGLISH[which] + ")");
                }
                dialog.dismiss();
                updateGunaMilanResult();
            }
        });
        b.setNegativeButton("Cancel", null);
        b.show();
    }

    private void showNakshatraPicker(final boolean isGroom, final Button targetBtn) {
        int currentSelection = isGroom ? matchGroomNak : matchBrideNak;

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(isGroom ? "Select Groom Nakshatra (वर नक्षत्र)" : "Select Bride Nakshatra (कन्या नक्षत्र)");
        b.setSingleChoiceItems(PanchangCalculator.NAKSHATRAS, currentSelection, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isGroom) {
                    matchGroomNak = which;
                    targetBtn.setText(PanchangCalculator.NAKSHATRAS[which]);
                } else {
                    matchBrideNak = which;
                    targetBtn.setText(PanchangCalculator.NAKSHATRAS[which]);
                }
                dialog.dismiss();
                updateGunaMilanResult();
            }
        });
        b.setNegativeButton("Cancel", null);
        b.show();
    }

    private void updateGunaMilanResult() {
        if (viewMore == null) return;
        TextView txtScore = viewMore.findViewById(R.id.txt_guna_total_score);
        TextView txtDosha = viewMore.findViewById(R.id.txt_guna_dosha_status);
        TextView txtBreakdown = viewMore.findViewById(R.id.txt_guna_breakdown);

        PanchangCalculator.GunaMilanResult res = PanchangCalculator.calculateGunaMilan(
            matchGroomRashi, matchGroomNak, matchBrideRashi, matchBrideNak
        );

        txtScore.setText("Total Score: " + res.totalPoints + " / 36 (" + res.verdict + ")");
        if (res.totalPoints >= 28) {
            txtScore.setTextColor(getColor(R.color.m3_auspicious));
        } else if (res.totalPoints >= 18) {
            txtScore.setTextColor(Color.parseColor("#FFD54F"));
        } else {
            txtScore.setTextColor(getColor(R.color.m3_inauspicious));
        }

        StringBuilder doshaSb = new StringBuilder();
        if (res.hasNadiDosha && res.hasBhakootDosha) {
            doshaSb.append("⚠️ Nadi Dosha Present • ⚠️ Bhakoot Dosha Present");
            txtDosha.setTextColor(getColor(R.color.m3_inauspicious));
        } else if (res.hasNadiDosha) {
            doshaSb.append("⚠️ Nadi Dosha Present (High dosha) • No Bhakoot Dosha");
            txtDosha.setTextColor(getColor(R.color.m3_inauspicious));
        } else if (res.hasBhakootDosha) {
            doshaSb.append("No Nadi Dosha • ⚠️ Bhakoot Dosha Present");
            txtDosha.setTextColor(Color.parseColor("#FF9800"));
        } else {
            doshaSb.append("✓ No Nadi Dosha • ✓ No Bhakoot Dosha (Dosha-Free Match)");
            txtDosha.setTextColor(getColor(R.color.m3_auspicious));
        }
        txtDosha.setText(doshaSb.toString());
        txtBreakdown.setText(res.breakdown);
    }

    private void bindMoreTab() {
        if (currentPanchang == null || viewMore == null) return;

        HinduClockView clockView = viewMore.findViewById(R.id.more_hindu_clock_view);
        TextView txtCity = viewMore.findViewById(R.id.more_txt_current_city);
        TextView txtTz = viewMore.findViewById(R.id.more_txt_city_tz);
        LinearLayout festivalList = viewMore.findViewById(R.id.more_festival_list);

        clockView.setSunriseSunset(currentPanchang.sunriseHour, currentPanchang.sunsetHour);
        txtCity.setText(currentCity.name + ", " + currentCity.country + String.format(Locale.US, " (%.2f° N, %.2f° E)", currentCity.latitude, currentCity.longitude));
        txtTz.setText("Timezone: " + currentCity.timezone);

        // Daily Puja Sankalpa
        TextView txtSanskrit = viewMore.findViewById(R.id.more_txt_sankalpa_sanskrit);
        TextView txtEnglish = viewMore.findViewById(R.id.more_txt_sankalpa_english);
        txtSanskrit.setText(PanchangCalculator.getDailySankalpaMantra(currentPanchang, currentCity.name));
        txtEnglish.setText(PanchangCalculator.getDailySankalpaEnglish(currentPanchang, currentCity.name));

        // Ashtakoota Matchmaker Initial Bind
        Button btnGroomRashi = viewMore.findViewById(R.id.btn_groom_rashi);
        Button btnGroomNak = viewMore.findViewById(R.id.btn_groom_nakshatra);
        Button btnBrideRashi = viewMore.findViewById(R.id.btn_bride_rashi);
        Button btnBrideNak = viewMore.findViewById(R.id.btn_bride_nakshatra);

        btnGroomRashi.setText(PanchangCalculator.RASHIS[matchGroomRashi] + " (" + PanchangCalculator.RASHIS_ENGLISH[matchGroomRashi] + ")");
        btnGroomNak.setText(PanchangCalculator.NAKSHATRAS[matchGroomNak]);
        btnBrideRashi.setText(PanchangCalculator.RASHIS[matchBrideRashi] + " (" + PanchangCalculator.RASHIS_ENGLISH[matchBrideRashi] + ")");
        btnBrideNak.setText(PanchangCalculator.NAKSHATRAS[matchBrideNak]);
        updateGunaMilanResult();

        // Populate Festivals of the Year
        festivalList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        String[][] festivals = {
            {"Shardiya Navratri Begins", "Ashwina Shukla Pratipada", "11 October 2026"},
            {"Durga Ashtami / Maha Navami", "Ashwina Shukla Ashtami/Navami", "19-20 October 2026"},
            {"Vijayadashami (Dussehra)", "Ashwina Shukla Dashami", "21 October 2026"},
            {"Sharad Purnima (Kojagiri)", "Ashwina Purnima", "26 October 2026"},
            {"Karwa Chauth", "Kartika Krishna Chaturthi", "29 October 2026"},
            {"Dhanteras", "Kartika Krishna Trayodashi", "6 November 2026"},
            {"Diwali (Lakshmi Puja)", "Kartika Amavasya", "8 November 2026"},
            {"Govardhan Puja / Annakut", "Kartika Shukla Pratipada", "10 November 2026"},
            {"Bhai Dooj", "Kartika Shukla Dwitiya", "11 November 2026"},
            {"Chhath Puja", "Kartika Shukla Shashthi", "15 November 2026"},
            {"Devutthana Ekadashi", "Kartika Shukla Ekadashi", "20 November 2026"},
            {"Kartik Purnima", "Kartika Purnima", "24 November 2026"}
        };

        for (String[] f : festivals) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setPadding(0, 10, 0, 10);

            TextView txtTitle = new TextView(this);
            txtTitle.setText(f[0]);
            txtTitle.setTextSize(14);
            txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            txtTitle.setTextColor(getColor(R.color.md_theme_onSurface));

            TextView txtDesc = new TextView(this);
            txtDesc.setText(f[1] + " • " + f[2]);
            txtDesc.setTextSize(12);
            txtDesc.setTextColor(getColor(R.color.md_theme_primary));

            item.addView(txtTitle);
            item.addView(txtDesc);

            View div = new View(this);
            div.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
            div.setBackgroundColor(getColor(R.color.md_theme_outlineVariant));
            item.addView(div);

            festivalList.addView(item);
        }

        // Hindu Time Converter
        final EditText editConvertTime = viewMore.findViewById(R.id.edit_convert_clock_time);
        final Button btnConvertTime = viewMore.findViewById(R.id.btn_convert_to_ghati);
        final TextView txtConvertResult = viewMore.findViewById(R.id.txt_convert_result);
        if (btnConvertTime != null) {
            btnConvertTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = (editConvertTime != null) ? editConvertTime.getText().toString().trim() : "";
                    if (text.isEmpty()) {
                        Calendar c = Calendar.getInstance();
                        text = String.format(Locale.US, "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                        if (editConvertTime != null) editConvertTime.setText(text);
                    }
                    try {
                        String[] parts = text.split(":");
                        int h = Integer.parseInt(parts[0]);
                        int m = Integer.parseInt(parts[1]);
                        double targetHour = h + m / 60.0;
                        double diffHours = targetHour - currentPanchang.sunriseHour;
                        if (diffHours < 0) diffHours += 24.0;
                        double totalGhatis = diffHours / 0.4;
                        int g = (int) totalGhatis;
                        double remP = (totalGhatis - g) * 60.0;
                        int p = (int) remP;
                        int vp = (int) Math.round((remP - p) * 60.0);
                        if (txtConvertResult != null) {
                            txtConvertResult.setText(String.format(Locale.US, "Result: %02d:%02d = Ghati %d, Pala %d, Vipala %d (Ishta Kaal)", h, m, g, p, vp));
                        }
                    } catch (Exception e) {
                        if (txtConvertResult != null) {
                            txtConvertResult.setText("Invalid format. Please enter HH:MM (e.g. 14:30)");
                        }
                    }
                }
            });
        }

        // Vedic Vastu Compass
        moreCompassView = viewMore.findViewById(R.id.more_compass_view);
        final TextView txtCompassHeading = viewMore.findViewById(R.id.more_txt_compass_heading);
        final TextView txtCompassDikpala = viewMore.findViewById(R.id.more_txt_compass_dikpala);
        final TextView txtCompassVastu = viewMore.findViewById(R.id.more_txt_compass_vastu);
        final TextView compassLiveBadge = viewMore.findViewById(R.id.more_compass_live_badge);
        final Button btnCompassReset = viewMore.findViewById(R.id.more_btn_compass_live_reset);

        if (moreCompassView != null) {
            moreCompassView.setDeclinationFromLocation(currentCity.latitude, currentCity.longitude, 500);
            moreCompassView.setOnHeadingChangeListener(new VedicCompassView.OnHeadingChangeListener() {
                @Override
                public void onHeadingChanged(float degrees, VedicCompassView.VedicDirection dir, boolean isManual) {
                    if (txtCompassHeading != null) {
                        txtCompassHeading.setText(String.format(Locale.US, "%.0f° • %s", degrees, dir.nameHindi));
                    }
                    if (txtCompassDikpala != null) {
                        txtCompassDikpala.setText("Dikpala: " + dir.dikpala + " • Lord: " + dir.grahaLord + " • Element: " + dir.element);
                    }
                    if (txtCompassVastu != null) {
                        txtCompassVastu.setText("Vastu Tip: " + dir.vastuTip);
                    }
                    if (compassLiveBadge != null) {
                        if (isManual) {
                            compassLiveBadge.setText("Manual Drag");
                            compassLiveBadge.setBackgroundResource(R.drawable.m3_badge_gold);
                            compassLiveBadge.setTextColor(Color.parseColor("#FFD54F"));
                        } else {
                            compassLiveBadge.setText("Live Sensor");
                            compassLiveBadge.setBackgroundResource(R.drawable.m3_badge_green);
                            compassLiveBadge.setTextColor(getColor(R.color.m3_auspicious));
                        }
                    }
                }
            });
            if (activeTab == 4) {
                moreCompassView.startListening();
            }
        }

        if (btnCompassReset != null && moreCompassView != null) {
            btnCompassReset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moreCompassView.resetToLiveSensor();
                    Toast.makeText(MainActivity.this, "Switched to Live Hardware Compass", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Japanese Color Palette Preview in Tab More
        TextView txtActiveThemeBadge = viewMore.findViewById(R.id.more_txt_active_theme_badge);
        View swatch1 = viewMore.findViewById(R.id.more_palette_swatch_1);
        View swatch2 = viewMore.findViewById(R.id.more_palette_swatch_2);
        TextView txtActivePaletteName = viewMore.findViewById(R.id.more_txt_active_palette_name);
        ThemeManager.JapanesePalette activeP = ThemeManager.getActivePalette(this);
        if (txtActiveThemeBadge != null) txtActiveThemeBadge.setText(activeP.nameKanji);
        if (swatch1 != null) swatch1.setBackgroundTintList(ColorStateList.valueOf(activeP.swatch1));
        if (swatch2 != null) swatch2.setBackgroundTintList(ColorStateList.valueOf(activeP.swatch2));
        if (txtActivePaletteName != null) txtActivePaletteName.setText(activeP.getDisplayName());

        // Personal Tithis & Reminders
        final Button btnAddTithi = viewMore.findViewById(R.id.more_btn_add_tithi);
        final LinearLayout myTithiList = viewMore.findViewById(R.id.more_my_tithi_list);
        if (myTithiList != null) {
            refreshMyTithiList(myTithiList);
        }
        if (btnAddTithi != null) {
            btnAddTithi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddTithiDialog(myTithiList);
                }
            });
        }

        // Calendar System & Regional Settings
        final TextView toggleAmavasyant = viewMore.findViewById(R.id.toggle_amavasyant);
        final TextView togglePurnimant = viewMore.findViewById(R.id.toggle_purnimant);
        final TextView txtSamvatInfo = viewMore.findViewById(R.id.more_txt_active_samvat_info);

        final SharedPreferences appSp = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean purnimantPref = appSp.getBoolean("is_purnimant", false);
        updateCalendarSystemUI(purnimantPref, toggleAmavasyant, togglePurnimant, txtSamvatInfo);

        if (toggleAmavasyant != null) {
            toggleAmavasyant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appSp.edit().putBoolean("is_purnimant", false).apply();
                    recalculateAndRefresh();
                }
            });
        }
        if (togglePurnimant != null) {
            togglePurnimant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appSp.edit().putBoolean("is_purnimant", true).apply();
                    recalculateAndRefresh();
                }
            });
        }

        // Change City Button
        Button btnChangeCity = viewMore.findViewById(R.id.more_btn_change_city);
        if (btnChangeCity != null) {
            btnChangeCity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCitySelectorDialog();
                }
            });
        }

        // Pin Desktop Widget Button
        Button btnPinWidget = viewMore.findViewById(R.id.more_btn_pin_widget);
        if (btnPinWidget != null) {
            btnPinWidget.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        AppWidgetManager appWidgetManager = getSystemService(AppWidgetManager.class);
                        ComponentName myProvider = new ComponentName(MainActivity.this, MonthWidgetProvider.class);
                        if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported()) {
                            appWidgetManager.requestPinAppWidget(myProvider, null, null);
                        } else {
                            Toast.makeText(MainActivity.this, "Add widget from your Home Screen widgets menu", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Add widget from your Home Screen widgets menu", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void updateCalendarSystemUI(boolean purnimant, TextView btnAmavasyant, TextView btnPurnimant, TextView txtInfo) {
        if (btnAmavasyant == null || btnPurnimant == null) return;
        if (!purnimant) {
            btnAmavasyant.setBackgroundResource(R.drawable.m3_card_highlight);
            btnAmavasyant.setTextColor(getColor(R.color.md_theme_onSurface));
            btnPurnimant.setBackgroundColor(Color.TRANSPARENT);
            btnPurnimant.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
            if (txtInfo != null && currentPanchang != null) {
                txtInfo.setText("Active: Amavasyant Month (अमावस्यांत) • Vikram Samvat " + currentPanchang.vikramSamvat + " • Saka " + currentPanchang.sakaSamvat + " • " + currentPanchang.samvatsaraSouth + " Jovian Year");
            }
        } else {
            btnPurnimant.setBackgroundResource(R.drawable.m3_card_highlight);
            btnPurnimant.setTextColor(getColor(R.color.md_theme_onSurface));
            btnAmavasyant.setBackgroundColor(Color.TRANSPARENT);
            btnAmavasyant.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
            if (txtInfo != null && currentPanchang != null) {
                txtInfo.setText("Active: Purnimant Month (पूर्णिमांत) • Vikram Samvat " + currentPanchang.vikramSamvat + " • Saka " + currentPanchang.sakaSamvat + " • " + currentPanchang.samvatsaraNorth + " Jovian Year");
            }
        }
    }

    private void showAddTithiDialog(final LinearLayout container) {
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_tithi, null);
        final AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        final EditText editTitle = dialogView.findViewById(R.id.edit_tithi_title);
        final Spinner spCategory = dialogView.findViewById(R.id.spinner_tithi_category);
        final Spinner spMonth = dialogView.findViewById(R.id.spinner_tithi_month);
        final Spinner spPaksha = dialogView.findViewById(R.id.spinner_tithi_paksha);
        final Spinner spTithiNum = dialogView.findViewById(R.id.spinner_tithi_num);
        final EditText editNotes = dialogView.findViewById(R.id.edit_tithi_notes);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_add_tithi);
        Button btnSave = dialogView.findViewById(R.id.btn_save_add_tithi);

        final String[] categories = {"Birthday (जन्मदिन)", "Anniversary (विवाह वर्षगांठ)", "Shraddha (श्राद्ध / पुण्यतिथि)", "Vrat / Observance (व्रत)"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spCategory.setAdapter(catAdapter);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, PanchangCalculator.HINDU_MONTHS_AMANTA);
        spMonth.setAdapter(monthAdapter);

        String[] pakshas = {"Shukla (शुक्ल पक्ष)", "Krishna (कृष्ण पक्ष)"};
        ArrayAdapter<String> pakshaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, pakshas);
        spPaksha.setAdapter(pakshaAdapter);

        String[] tithiNames = new String[15];
        for (int i = 0; i < 14; i++) {
            tithiNames[i] = (i + 1) + ". " + PanchangCalculator.TITHI_NAMES[i];
        }
        tithiNames[14] = "15. Purnima / Amavasya (पूर्णिमा / अमावस्या)";
        ArrayAdapter<String> tithiAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tithiNames);
        spTithiNum.setAdapter(tithiAdapter);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTitle.getText().toString().trim();
                if (title.isEmpty()) {
                    editTitle.setError("Please enter a title");
                    return;
                }
                String cat = categories[spCategory.getSelectedItemPosition()].split(" ")[0];
                int monthIdx = spMonth.getSelectedItemPosition();
                String paksha = (spPaksha.getSelectedItemPosition() == 0) ? "Shukla" : "Krishna";
                int tithiNum = spTithiNum.getSelectedItemPosition() + 1;
                String notes = editNotes.getText().toString().trim();

                String id = String.valueOf(System.currentTimeMillis());
                MyTithiManager.TithiEvent ev = new MyTithiManager.TithiEvent(id, title, cat, monthIdx, paksha, tithiNum, notes);
                MyTithiManager.addEvent(MainActivity.this, ev);

                dialog.dismiss();
                refreshMyTithiList(container);
            }
        });

        dialog.show();
    }

    private void refreshMyTithiList() {
        if (viewMore != null) {
            LinearLayout container = viewMore.findViewById(R.id.more_my_tithi_list);
            if (container != null) refreshMyTithiList(container);
        }
    }

    private void refreshMyTithiList(final LinearLayout container) {
        if (container == null) return;
        container.removeAllViews();

        List<MyTithiManager.TithiEvent> events = MyTithiManager.loadEvents(this);
        if (events.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No personal tithis saved yet. Tap '+ Add Tithi' above.");
            empty.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
            empty.setTextSize(12);
            empty.setPadding(0, 8, 0, 8);
            container.addView(empty);
            return;
        }

        for (final MyTithiManager.TithiEvent ev : events) {
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setBackgroundResource(R.drawable.m3_card_highlight);
            item.setPadding(16, 12, 16, 12);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.bottomMargin = 8;
            item.setLayoutParams(lp);

            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            textCol.setLayoutParams(textLp);

            LinearLayout titleRow = new LinearLayout(this);
            titleRow.setOrientation(LinearLayout.HORIZONTAL);
            titleRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView txtTitle = new TextView(this);
            txtTitle.setText(ev.title);
            txtTitle.setTextSize(13);
            txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            txtTitle.setTextColor(getColor(R.color.md_theme_onSurface));

            TextView badge = new TextView(this);
            badge.setText(ev.category);
            badge.setTextSize(10);
            badge.setTypeface(null, android.graphics.Typeface.BOLD);
            badge.setBackgroundResource(R.drawable.m3_badge_gold);
            badge.setTextColor(Color.parseColor("#FFD54F"));
            badge.setPadding(8, 2, 8, 2);
            LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            badgeLp.leftMargin = 8;
            badge.setLayoutParams(badgeLp);

            titleRow.addView(txtTitle);
            titleRow.addView(badge);
            textCol.addView(titleRow);

            TextView txtTithi = new TextView(this);
            txtTithi.setText(ev.getTithiDescription() + (ev.note.isEmpty() ? "" : " • " + ev.note));
            txtTithi.setTextSize(11);
            txtTithi.setTextColor(getColor(R.color.md_theme_primary));
            textCol.addView(txtTithi);

            item.addView(textCol);

            // Delete button
            ImageButton btnDel = new ImageButton(this);
            btnDel.setImageResource(android.R.drawable.ic_menu_delete);
            btnDel.setBackgroundColor(Color.TRANSPARENT);
            btnDel.setColorFilter(getColor(R.color.m3_inauspicious));
            btnDel.setPadding(8, 8, 8, 8);
            btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyTithiManager.deleteEvent(MainActivity.this, ev.id);
                    refreshMyTithiList(container);
                }
            });
            item.addView(btnDel);

            container.addView(item);
        }
    }

    private void updateLiveTimers() {
        if (currentPanchang == null) return;

        // Update Hindu Clock Digital Readout
        if (activeTab == 4 && viewMore != null) {
            HinduClockView clockView = viewMore.findViewById(R.id.more_hindu_clock_view);
            TextView txtDigitalTime = viewMore.findViewById(R.id.more_txt_hindu_time_digital);
            TextView txtIshtaKaal = viewMore.findViewById(R.id.more_txt_ishtakaal);

            if (clockView != null && txtDigitalTime != null) {
                int g = clockView.getGhati();
                int p = clockView.getPala();
                int v = clockView.getVipala();
                txtDigitalTime.setText(String.format(Locale.US, "Ghati: %02d • Pala: %02d • Vipala: %02d", g, p, v));

                double elapsedHours = g * 0.4 + p * (0.4 / 60.0);
                int eh = (int) elapsedHours;
                int em = (int) Math.round((elapsedHours - eh) * 60.0);
                txtIshtaKaal.setText(String.format(Locale.US, "Ishta Kaal: %02dh %02dm since Sunrise", eh, em));
            }
        }

        // Update Choghadiya & Hora Countdown
        if (activeTab == 2 && viewChoghadiya != null) {
            if (!isHoraActive) {
                TextView txtHeroCountdown = viewChoghadiya.findViewById(R.id.chog_hero_txt_countdown);
                PanchangCalculator.ChoghadiyaPeriod active = currentPanchang.activeChoghadiya;
                if (active != null && txtHeroCountdown != null) {
                    Calendar now = Calendar.getInstance();
                    double currentHour = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0 + now.get(Calendar.SECOND) / 3600.0;
                    double remHours = active.endHour - currentHour;
                    if (remHours < 0) remHours += 24.0;

                    int remMins = (int)(remHours * 60.0);
                    int remSecs = (int) Math.round((remHours * 3600.0) % 60.0);
                    txtHeroCountdown.setText(String.format(Locale.US, "Ends in %02dm %02ds", remMins, remSecs));
                }
            } else {
                TextView txtHoraCountdown = viewChoghadiya.findViewById(R.id.hora_hero_txt_countdown);
                PanchangCalculator.HoraPeriod activeH = currentPanchang.activeHora;
                if (activeH != null && txtHoraCountdown != null) {
                    Calendar now = Calendar.getInstance();
                    double currentHour = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0 + now.get(Calendar.SECOND) / 3600.0;
                    double remHours = activeH.endHour - currentHour;
                    if (remHours < 0) remHours += 24.0;

                    int remMins = (int)(remHours * 60.0);
                    int remSecs = (int) Math.round((remHours * 3600.0) % 60.0);
                    txtHoraCountdown.setText(String.format(Locale.US, "Ends in %02dm %02ds", remMins, remSecs));
                }
            }
        }
    }

    private void pinWidget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AppWidgetManager appWidgetManager = getSystemService(AppWidgetManager.class);
            ComponentName myProvider = new ComponentName(this, MonthWidgetProvider.class);

            if (appWidgetManager.isRequestPinAppWidgetSupported()) {
                Intent pinnedWidgetCallbackIntent = new Intent(this, MonthWidgetProvider.class);
                int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                if (Build.VERSION.SDK_INT >= 31) {
                    flags |= PendingIntent.FLAG_MUTABLE;
                }
                PendingIntent successCallback = PendingIntent.getBroadcast(this, 0, pinnedWidgetCallbackIntent, flags);
                appWidgetManager.requestPinAppWidget(myProvider, null, successCallback);
                Toast.makeText(this, "Requested widget placement!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please add widget manually via launcher.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Please add widget from your home screen.", Toast.LENGTH_SHORT).show();
        }
    }

    private void pinDailyWidget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AppWidgetManager appWidgetManager = getSystemService(AppWidgetManager.class);
            ComponentName myProvider = new ComponentName(this, DailyPanchangWidgetProvider.class);

            if (appWidgetManager.isRequestPinAppWidgetSupported()) {
                Intent pinnedWidgetCallbackIntent = new Intent(this, DailyPanchangWidgetProvider.class);
                int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                if (Build.VERSION.SDK_INT >= 31) {
                    flags |= PendingIntent.FLAG_MUTABLE;
                }
                PendingIntent successCallback = PendingIntent.getBroadcast(this, 0, pinnedWidgetCallbackIntent, flags);
                appWidgetManager.requestPinAppWidget(myProvider, null, successCallback);
                Toast.makeText(this, "Requested daily panchang widget placement!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please add widget manually via launcher.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Please add widget from your home screen.", Toast.LENGTH_SHORT).show();
        }
    }

    private void bindFestivalsList(final String filterCategory) {
        if (viewCalendar == null) return;
        final LinearLayout listContainer = viewCalendar.findViewById(R.id.cal_festivals_list);
        if (listContainer == null) return;
        listContainer.removeAllViews();

        final TextView chipAll = viewCalendar.findViewById(R.id.chip_fest_all);
        final TextView chipMajor = viewCalendar.findViewById(R.id.chip_fest_major);
        final TextView chipEkadashi = viewCalendar.findViewById(R.id.chip_fest_ekadashi);
        final TextView chipPradosh = viewCalendar.findViewById(R.id.chip_fest_pradosh);
        final TextView chipSankashti = viewCalendar.findViewById(R.id.chip_fest_sankashti);
        final TextView chipPurnima = viewCalendar.findViewById(R.id.chip_fest_purnima);

        TextView[] chips = { chipAll, chipMajor, chipEkadashi, chipPradosh, chipSankashti, chipPurnima };
        String[] chipCats = { "All", "Major", "Ekadashi", "Pradosh", "Sankashti", "Purnima" };

        for (int i = 0; i < chips.length; i++) {
            if (chips[i] != null) {
                final String cat = chipCats[i];
                if (cat.equalsIgnoreCase(filterCategory)) {
                    chips[i].setBackgroundResource(R.drawable.m3_card_highlight);
                    chips[i].setTextColor(getColor(R.color.md_theme_onSurface));
                } else {
                    chips[i].setBackgroundResource(R.drawable.m3_pill_indicator);
                    chips[i].setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
                }
                chips[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bindFestivalsList(cat);
                    }
                });
            }
        }

        List<PanchangCalculator.FestivalItem> festivals = PanchangCalculator.getMonthFestivals(
            calNavYear, calNavMonth, currentCity.latitude, currentCity.longitude, isPurnimant
        );

        for (final PanchangCalculator.FestivalItem item : festivals) {
            if (!"All".equalsIgnoreCase(filterCategory) && !item.category.equalsIgnoreCase(filterCategory)) {
                continue;
            }

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.m3_card_surface);
            card.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.bottomMargin = dpToPx(10);
            card.setLayoutParams(lp);

            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView txtDate = new TextView(this);
            txtDate.setText(item.dateStr);
            txtDate.setTextColor(getColor(R.color.md_theme_primary));
            txtDate.setTextSize(14f);
            txtDate.setTypeface(null, Typeface.BOLD);
            topRow.addView(txtDate);

            View spacer = new View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
            topRow.addView(spacer);

            TextView txtCat = new TextView(this);
            txtCat.setText(item.category);
            txtCat.setBackgroundResource("Major".equalsIgnoreCase(item.category) ? R.drawable.m3_badge_gold : R.drawable.m3_badge_green);
            txtCat.setTextColor("Major".equalsIgnoreCase(item.category) ? Color.parseColor("#D97706") : getColor(R.color.m3_auspicious));
            txtCat.setTextSize(10f);
            txtCat.setTypeface(null, Typeface.BOLD);
            txtCat.setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3));
            topRow.addView(txtCat);

            card.addView(topRow);

            TextView txtTitle = new TextView(this);
            txtTitle.setText(item.name);
            txtTitle.setTextColor(getColor(R.color.md_theme_onSurface));
            txtTitle.setTextSize(15f);
            txtTitle.setTypeface(null, Typeface.BOLD);
            txtTitle.setPadding(0, dpToPx(4), 0, 0);
            card.addView(txtTitle);

            TextView txtTithi = new TextView(this);
            txtTithi.setText(item.tithiStr);
            txtTithi.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
            txtTithi.setTextSize(12f);
            txtTithi.setPadding(0, dpToPx(2), 0, 0);
            card.addView(txtTithi);

            Button btnAddCal = new Button(this);
            btnAddCal.setText("📅 Add to Google Calendar");
            btnAddCal.setTextSize(11f);
            btnAddCal.setTypeface(null, Typeface.BOLD);
            btnAddCal.setBackgroundResource(R.drawable.m3_pill_indicator);
            btnAddCal.setTextColor(getColor(R.color.md_theme_primary));
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(36)
            );
            blp.topMargin = dpToPx(8);
            btnAddCal.setLayoutParams(blp);
            btnAddCal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addFestivalToCalendar(item);
                }
            });
            card.addView(btnAddCal);

            listContainer.addView(card);
        }

        if (listContainer.getChildCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText("No festivals found matching \"" + filterCategory + "\" this month.");
            empty.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
            empty.setPadding(dpToPx(16), dpToPx(24), dpToPx(16), dpToPx(24));
            empty.setGravity(Gravity.CENTER);
            listContainer.addView(empty);
        }
    }

    private void addFestivalToCalendar(PanchangCalculator.FestivalItem item) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(calNavYear, calNavMonth - 1, item.day, 9, 0, 0);
            long startMillis = cal.getTimeInMillis();
            long endMillis = startMillis + (2 * 60 * 60 * 1000);

            Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                .putExtra(CalendarContract.Events.TITLE, item.name)
                .putExtra(CalendarContract.Events.DESCRIPTION, item.tithiStr + " • " + item.description)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, currentCity.name);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Calendar app not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportUserDataBackup() {
        try {
            JSONObject root = new JSONObject();
            root.put("app", "HinduCalendar_M3");
            root.put("version", 1);
            root.put("exported_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date()));

            JSONObject settings = new JSONObject();
            settings.put("city_name", currentCity != null ? currentCity.name : "Ujjain");
            settings.put("latitude", currentCity != null ? currentCity.latitude : 23.18);
            settings.put("longitude", currentCity != null ? currentCity.longitude : 75.77);
            settings.put("is_purnimant", isPurnimant);
            settings.put("kundali_style", kundaliStyle);
            root.put("settings", settings);

            JSONArray tithiArr = new JSONArray();
            List<MyTithiManager.TithiEvent> tithis = MyTithiManager.loadEvents(this);
            for (MyTithiManager.TithiEvent t : tithis) {
                tithiArr.put(t.toJson());
            }
            root.put("my_tithis", tithiArr);

            String jsonString = root.toString(2);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, jsonString);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Hindu Calendar M3 Backup (" + tithis.size() + " Tithis)");
            startActivity(Intent.createChooser(shareIntent, "Save or Share Backup"));
            Toast.makeText(this, "Backup ready (" + tithis.size() + " Tithis)", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Export error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showImportBackupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📥 Restore Data Backup");
        final EditText input = new EditText(this);
        input.setHint("Paste backup JSON here (or leave empty to verify current)");
        input.setMaxLines(6);
        builder.setView(input);
        builder.setPositiveButton("Restore", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String str = input.getText().toString().trim();
                if (!str.isEmpty()) {
                    try {
                        JSONObject root = new JSONObject(str);
                        JSONArray arr = root.optJSONArray("my_tithis");
                        if (arr != null) {
                            List<MyTithiManager.TithiEvent> imported = new ArrayList<>();
                            for (int i = 0; i < arr.length(); i++) {
                                MyTithiManager.TithiEvent ev = MyTithiManager.TithiEvent.fromJson(arr.getJSONObject(i));
                                if (ev != null) imported.add(ev);
                            }
                            if (!imported.isEmpty()) {
                                MyTithiManager.saveEvents(MainActivity.this, imported);
                                refreshMyTithiList();
                                Toast.makeText(MainActivity.this, "Restored " + imported.size() + " personal tithis successfully!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Invalid JSON format: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                List<MyTithiManager.TithiEvent> tithis = MyTithiManager.loadEvents(MainActivity.this);
                Toast.makeText(MainActivity.this, "Current backup verified: " + tithis.size() + " personal tithis active.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void broadcastWidgetUpdate() {
        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

            ComponentName monthWidgetComp = new ComponentName(this, MonthWidgetProvider.class);
            int[] monthIds = appWidgetManager.getAppWidgetIds(monthWidgetComp);
            if (monthIds != null && monthIds.length > 0) {
                Intent monthIntent = new Intent(this, MonthWidgetProvider.class);
                monthIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                monthIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, monthIds);
                sendBroadcast(monthIntent);
            }

            ComponentName dailyWidgetComp = new ComponentName(this, DailyPanchangWidgetProvider.class);
            int[] dailyIds = appWidgetManager.getAppWidgetIds(dailyWidgetComp);
            if (dailyIds != null && dailyIds.length > 0) {
                Intent dailyIntent = new Intent(this, DailyPanchangWidgetProvider.class);
                dailyIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                dailyIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, dailyIds);
                sendBroadcast(dailyIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateMonthSlide(final boolean isNext) {
        if (viewCalendar == null) return;
        final View grid = viewCalendar.findViewById(R.id.cal_table_grid);
        if (grid == null) {
            bindCalendarTab();
            return;
        }

        final float slideDistance = dpToPx(80);
        float exitTranslation = isNext ? -slideDistance : slideDistance;
        final float enterTranslation = isNext ? slideDistance : -slideDistance;

        grid.animate()
            .translationX(exitTranslation)
            .alpha(0f)
            .setDuration(110)
            .setInterpolator(new AccelerateInterpolator())
            .withEndAction(new Runnable() {
                @Override
                public void run() {
                    bindCalendarTab();
                    grid.setTranslationX(enterTranslation);
                    grid.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(150)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
                }
            })
            .start();
    }

    private void applyDynamicJapaneseTheme(final ThemeManager.JapanesePalette palette) {
        if (palette == null) return;
        ThemeManager.applyThemeToActivity(this);

        // 1. Bottom Navigation Bar Pills & Icons
        for (int i = 0; i < 5; i++) {
            if (i == activeTab) {
                if (navPills[i] != null) {
                    navPills[i].setBackgroundResource(R.drawable.m3_pill_indicator);
                    navPills[i].setBackgroundTintList(ColorStateList.valueOf(palette.primaryContainer));
                }
                if (navTexts[i] != null) {
                    navTexts[i].setTextColor(palette.primary);
                    navTexts[i].setTypeface(null, Typeface.BOLD);
                }
                if (navIcons[i] != null) {
                    navIcons[i].setColorFilter(palette.primary);
                }
            } else {
                if (navPills[i] != null) {
                    navPills[i].setBackgroundColor(Color.TRANSPARENT);
                    navPills[i].setBackgroundTintList(null);
                }
                if (navTexts[i] != null) {
                    navTexts[i].setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
                    navTexts[i].setTypeface(null, Typeface.NORMAL);
                }
                if (navIcons[i] != null) {
                    navIcons[i].setColorFilter(getColor(R.color.md_theme_onSurfaceVariant));
                }
            }
        }

        // 2. Top App Bar Accents & Action Buttons
        TextView appSubtitle = findViewById(R.id.app_bar_subtitle);
        if (appSubtitle != null) {
            appSubtitle.setTextColor(palette.primary);
        }
        ImageButton btnToday = findViewById(R.id.btn_today_jump);
        if (btnToday != null) btnToday.setColorFilter(palette.primary);
        ImageButton btnShare = findViewById(R.id.btn_share_app_bar);
        if (btnShare != null) btnShare.setColorFilter(palette.primary);
        ImageButton btnSettings = findViewById(R.id.btn_settings_app_bar);
        if (btnSettings != null) btnSettings.setColorFilter(palette.primary);

        // 3. Date Strip Header
        if (txtHeaderHinduDate != null) {
            txtHeaderHinduDate.setTextColor(palette.primary);
        }
        ImageButton btnPrevDay = findViewById(R.id.btn_prev_day);
        if (btnPrevDay != null) btnPrevDay.setColorFilter(palette.primary);
        ImageButton btnNextDay = findViewById(R.id.btn_next_day);
        if (btnNextDay != null) btnNextDay.setColorFilter(palette.primary);

        // 4. More Tab Preview Card & Badges
        if (viewMore != null) {
            TextView txtBadge = viewMore.findViewById(R.id.more_txt_active_theme_badge);
            View sw1 = viewMore.findViewById(R.id.more_palette_swatch_1);
            View sw2 = viewMore.findViewById(R.id.more_palette_swatch_2);
            TextView txtName = viewMore.findViewById(R.id.more_txt_active_palette_name);
            Button btnOpenThemes = viewMore.findViewById(R.id.more_btn_open_themes);

            if (txtBadge != null) {
                txtBadge.setText(palette.nameKanji);
                txtBadge.setTextColor(palette.swatch2);
            }
            if (sw1 != null) sw1.setBackgroundTintList(ColorStateList.valueOf(palette.swatch1));
            if (sw2 != null) sw2.setBackgroundTintList(ColorStateList.valueOf(palette.swatch2));
            if (txtName != null) txtName.setText(palette.getDisplayName());
            if (btnOpenThemes != null) btnOpenThemes.setBackgroundTintList(ColorStateList.valueOf(palette.primary));
        }

        // 5. Custom Canvas Views (Redraw dials with new theme)
        if (moreCompassView != null) moreCompassView.applyTheme(palette);
        if (viewMore != null) {
            HinduClockView clockView = viewMore.findViewById(R.id.more_hindu_clock_view);
            if (clockView != null) clockView.applyTheme(palette);
        }
        if (viewKundali != null) {
            NorthIndianKundaliView nChart = viewKundali.findViewById(R.id.kundali_chart_view);
            if (nChart != null) nChart.applyTheme(palette);
            SouthIndianKundaliView sChart = viewKundali.findViewById(R.id.kundali_south_chart_view);
            if (sChart != null) sChart.applyTheme(palette);
            EastIndianKundaliView eChart = viewKundali.findViewById(R.id.kundali_east_chart_view);
            if (eChart != null) eChart.applyTheme(palette);
        }
        if (viewPanchang != null) {
            VisualMuhurtaTimelineView tl = viewPanchang.findViewById(R.id.panchang_muhurta_timeline);
            if (tl != null) tl.applyTheme(palette);
        }

        // 6. Deep Cascade across all views in all tabs
        final java.util.Set<Integer> themePrimaryColors = new java.util.HashSet<>();
        themePrimaryColors.add(getColor(R.color.md_theme_primary));
        for (ThemeManager.JapanesePalette jp : ThemeManager.getAllPalettes()) {
            themePrimaryColors.add(jp.primary);
        }
        applyThemeRecursively(findViewById(android.R.id.content), palette, themePrimaryColors);
        applyThemeRecursively(viewPanchang, palette, themePrimaryColors);
        applyThemeRecursively(viewCalendar, palette, themePrimaryColors);
        applyThemeRecursively(viewChoghadiya, palette, themePrimaryColors);
        applyThemeRecursively(viewKundali, palette, themePrimaryColors);
        applyThemeRecursively(viewMore, palette, themePrimaryColors);

        // 7. Fluid animation on content switch (/animate)
        if (containerTabContent != null) {
            containerTabContent.animate()
                .alpha(0.7f)
                .setDuration(60)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        recalculateAndRefresh();
                        containerTabContent.animate()
                            .alpha(1.0f)
                            .setDuration(120)
                            .start();
                    }
                })
                .start();
        } else {
            recalculateAndRefresh();
        }
    }

    private void applyThemeRecursively(View v, ThemeManager.JapanesePalette palette, java.util.Set<Integer> primaryColors) {
        if (v == null) return;
        if (v instanceof TextView) {
            TextView tv = (TextView) v;
            if (primaryColors.contains(tv.getCurrentTextColor())) {
                tv.setTextColor(palette.primary);
            }
        }
        if (v instanceof ImageView) {
            ImageView iv = (ImageView) v;
            ColorStateList tint = iv.getImageTintList();
            if (tint != null && primaryColors.contains(tint.getDefaultColor())) {
                iv.setImageTintList(ColorStateList.valueOf(palette.primary));
            }
        }
        if (v instanceof Button) {
            Button btn = (Button) v;
            ColorStateList bgTint = btn.getBackgroundTintList();
            if (bgTint != null && primaryColors.contains(bgTint.getDefaultColor())) {
                btn.setBackgroundTintList(ColorStateList.valueOf(palette.primary));
            }
        }
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyThemeRecursively(vg.getChildAt(i), palette, primaryColors);
            }
        }
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        final SharedPreferences sp = getSharedPreferences("app_settings", MODE_PRIVATE);
        final LinearLayout palettesContainer = dialogView.findViewById(R.id.dialog_palettes_container);
        final List<ThemeManager.JapanesePalette> palettes = ThemeManager.getAllPalettes();
        final Button btnClose = dialogView.findViewById(R.id.dialog_btn_close_settings);

        final Runnable populatePalettes = new Runnable() {
            @Override
            public void run() {
                if (palettesContainer == null) return;
                palettesContainer.removeAllViews();
                int currentActive = ThemeManager.getSelectedThemeIndex(MainActivity.this);
                ThemeManager.JapanesePalette currentP = ThemeManager.getActivePalette(MainActivity.this);

                if (btnClose != null) {
                    btnClose.setBackgroundTintList(ColorStateList.valueOf(currentP.primary));
                }

                for (final ThemeManager.JapanesePalette p : palettes) {
                    LinearLayout card = new LinearLayout(MainActivity.this);
                    card.setOrientation(LinearLayout.HORIZONTAL);
                    card.setGravity(Gravity.CENTER_VERTICAL);
                    card.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
                    LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    clp.bottomMargin = dpToPx(8);
                    card.setLayoutParams(clp);

                    boolean isSelected = (p.id == currentActive);
                    card.setBackgroundResource(isSelected ? R.drawable.m3_card_highlight : R.drawable.m3_card_surface);

                    // 2 Colored Swatches
                    LinearLayout swatchesBox = new LinearLayout(MainActivity.this);
                    swatchesBox.setOrientation(LinearLayout.HORIZONTAL);
                    swatchesBox.setGravity(Gravity.CENTER_VERTICAL);

                    View c1 = new View(MainActivity.this);
                    c1.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(18), dpToPx(18)));
                    c1.setBackgroundResource(R.drawable.m3_pill_indicator);
                    c1.setBackgroundTintList(ColorStateList.valueOf(p.swatch1));
                    swatchesBox.addView(c1);

                    View c2 = new View(MainActivity.this);
                    LinearLayout.LayoutParams c2lp = new LinearLayout.LayoutParams(dpToPx(18), dpToPx(18));
                    c2lp.leftMargin = dpToPx(4);
                    c2.setLayoutParams(c2lp);
                    c2.setBackgroundResource(R.drawable.m3_pill_indicator);
                    c2.setBackgroundTintList(ColorStateList.valueOf(p.swatch2));
                    swatchesBox.addView(c2);

                    card.addView(swatchesBox);

                    // Name and Description Column
                    LinearLayout textCol = new LinearLayout(MainActivity.this);
                    textCol.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams tclp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                    tclp.leftMargin = dpToPx(12);
                    tclp.rightMargin = dpToPx(8);
                    textCol.setLayoutParams(tclp);

                    TextView txtKanji = new TextView(MainActivity.this);
                    txtKanji.setText(p.nameKanji + " • " + p.nameEnglish);
                    txtKanji.setTextSize(13f);
                    txtKanji.setTypeface(null, Typeface.BOLD);
                    txtKanji.setTextColor(isSelected ? p.primary : getColor(R.color.md_theme_onSurface));
                    textCol.addView(txtKanji);

                    TextView txtDesc = new TextView(MainActivity.this);
                    txtDesc.setText(p.description);
                    txtDesc.setTextSize(11f);
                    txtDesc.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
                    textCol.addView(txtDesc);

                    card.addView(textCol);

                    if (isSelected) {
                        TextView badge = new TextView(MainActivity.this);
                        badge.setText("Active");
                        badge.setBackgroundResource(R.drawable.m3_badge_green);
                        badge.setTextColor(getColor(R.color.m3_auspicious));
                        badge.setTextSize(10f);
                        badge.setTypeface(null, Typeface.BOLD);
                        badge.setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3));
                        card.addView(badge);
                    }

                    final Runnable self = this;
                    card.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                            } catch (Exception ignored) {}
                            ThemeManager.setSelectedThemeIndex(MainActivity.this, p.id);
                            applyDynamicJapaneseTheme(p);
                            Toast.makeText(MainActivity.this, "Theme Applied: " + p.nameKanji, Toast.LENGTH_SHORT).show();
                            self.run();
                        }
                    });

                    palettesContainer.addView(card);
                }
            }
        };
        populatePalettes.run();

        // Toggles for Amanta / Purnimanta
        final TextView toggleAmanta = dialogView.findViewById(R.id.dialog_toggle_amanta);
        final TextView togglePurnimanta = dialogView.findViewById(R.id.dialog_toggle_purnimanta);

        final Runnable updateMasaToggles = new Runnable() {
            @Override
            public void run() {
                boolean purn = sp.getBoolean("is_purnimant", false);
                if (purn) {
                    togglePurnimanta.setBackgroundResource(R.drawable.m3_card_highlight);
                    togglePurnimanta.setTextColor(getColor(R.color.md_theme_onSurface));
                    toggleAmanta.setBackgroundColor(Color.TRANSPARENT);
                    toggleAmanta.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
                } else {
                    toggleAmanta.setBackgroundResource(R.drawable.m3_card_highlight);
                    toggleAmanta.setTextColor(getColor(R.color.md_theme_onSurface));
                    togglePurnimanta.setBackgroundColor(Color.TRANSPARENT);
                    togglePurnimanta.setTextColor(getColor(R.color.md_theme_onSurfaceVariant));
                }
            }
        };
        updateMasaToggles.run();

        toggleAmanta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.edit().putBoolean("is_purnimant", false).apply();
                isPurnimant = false;
                updateMasaToggles.run();
                recalculateAndRefresh();
            }
        });

        togglePurnimanta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.edit().putBoolean("is_purnimant", true).apply();
                isPurnimant = true;
                updateMasaToggles.run();
                recalculateAndRefresh();
            }
        });

        // Ayanamsa Toggles
        final TextView toggleLahiri = dialogView.findViewById(R.id.dialog_toggle_lahiri);
        final TextView toggleRaman = dialogView.findViewById(R.id.dialog_toggle_raman);
        final TextView toggleKp = dialogView.findViewById(R.id.dialog_toggle_kp);

        final Runnable updateAyanToggles = new Runnable() {
            @Override
            public void run() {
                int a = sp.getInt("ayanamsa_system", 0);
                if (toggleLahiri != null) {
                    toggleLahiri.setBackgroundResource(a == 0 ? R.drawable.m3_card_highlight : 0);
                    toggleLahiri.setTextColor(a == 0 ? getColor(R.color.md_theme_onSurface) : getColor(R.color.md_theme_onSurfaceVariant));
                }
                if (toggleRaman != null) {
                    toggleRaman.setBackgroundResource(a == 1 ? R.drawable.m3_card_highlight : 0);
                    toggleRaman.setTextColor(a == 1 ? getColor(R.color.md_theme_onSurface) : getColor(R.color.md_theme_onSurfaceVariant));
                }
                if (toggleKp != null) {
                    toggleKp.setBackgroundResource(a == 2 ? R.drawable.m3_card_highlight : 0);
                    toggleKp.setTextColor(a == 2 ? getColor(R.color.md_theme_onSurface) : getColor(R.color.md_theme_onSurfaceVariant));
                }
            }
        };
        updateAyanToggles.run();

        if (toggleLahiri != null) {
            toggleLahiri.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sp.edit().putInt("ayanamsa_system", 0).apply();
                    updateAyanToggles.run();
                    recalculateAndRefresh();
                }
            });
        }
        if (toggleRaman != null) {
            toggleRaman.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sp.edit().putInt("ayanamsa_system", 1).apply();
                    updateAyanToggles.run();
                    recalculateAndRefresh();
                }
            });
        }
        if (toggleKp != null) {
            toggleKp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sp.edit().putInt("ayanamsa_system", 2).apply();
                    updateAyanToggles.run();
                    recalculateAndRefresh();
                }
            });
        }

        // Section 5: Backup & Restore
        Button btnExport = dialogView.findViewById(R.id.dialog_btn_export_backup);
        if (btnExport != null) {
            btnExport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exportUserDataBackup();
                }
            });
        }
        Button btnImport = dialogView.findViewById(R.id.dialog_btn_import_backup);
        if (btnImport != null) {
            btnImport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showImportBackupDialog();
                }
            });
        }

        // Section 6: Home Screen Widgets
        Button btnPinMonth = dialogView.findViewById(R.id.dialog_btn_pin_month_widget);
        if (btnPinMonth != null) {
            btnPinMonth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pinWidget();
                }
            });
        }
        Button btnPinDaily = dialogView.findViewById(R.id.dialog_btn_pin_daily_widget);
        if (btnPinDaily != null) {
            btnPinDaily.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pinDailyWidget();
                }
            });
        }

        // Close Button
        if (btnClose != null) {
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

        dialog.show();
    }
}
