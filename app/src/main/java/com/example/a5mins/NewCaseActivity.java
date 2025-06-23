package com.example.a5mins;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.a5mins.models.Case;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class NewCaseActivity extends AppCompatActivity {
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final String GEO_API_KEY = "your google api key"; // 佔位字串，請自行填入
    private EditText locationEditText;
    private EditText noteEditText;
    private Button timeButton;
    private TextView currentTimeTextView;
    private Button confirmButton;
    private Button backButton;
    private TextView titleTextView;
    private Calendar selectedTime;
    private SimpleDateFormat timeFormat;
    private boolean isTimeSelected = false;
    private String matchedAddress = "";
    private String matchedSpot = "";
    private DatabaseReference casesRef;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_case);

        // 初始化 Firebase
        casesRef = FirebaseDatabase.getInstance().getReference("cases");

        // 初始化 Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "your google api key");
        }

        // 设置 Places API 的语言为中文
        Locale locale = new Locale("zh", "TW");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // 初始化视图
        locationEditText = findViewById(R.id.locationEditText);
        noteEditText = findViewById(R.id.noteEditText);
        timeButton = findViewById(R.id.timeButton);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        confirmButton = findViewById(R.id.confirmButton);
        backButton = findViewById(R.id.backButton);
        titleTextView = findViewById(R.id.titleTextView);

        // 初始化时间格式
        timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        selectedTime = Calendar.getInstance();
        updateCurrentTimeDisplay();

        // 设置标题
        titleTextView.setText("新增車案");

        // 设置返回按钮点击事件
        backButton.setOnClickListener(v -> finish());

        // 设置时间选择按钮点击事件
        timeButton.setOnClickListener(v -> showTimePickerDialog());

        // 设置确认按钮点击事件
        confirmButton.setOnClickListener(v -> showConfirmationDialog());

        // 设置地址输入框点击事件
        locationEditText.setOnClickListener(v -> startAutocompleteActivity());
    }

    private String generateCaseId() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }
        return result.toString();
    }

    private void saveCase(String location, String note, String time) {
        String caseId = generateCaseId();
        FirebaseDatabase.getInstance().getReference("users").orderByChild("status").equalTo("接單中").get().addOnSuccessListener(dataSnapshot -> {
            String nearestUser = null;
            double minDistance = Double.MAX_VALUE;
            double caseLat = 0, caseLng = 0;
            if (matchedSpot != null && matchedSpot.contains(",")) {
                String[] latlng = matchedSpot.split(",");
                caseLat = Double.parseDouble(latlng[0]);
                caseLng = Double.parseDouble(latlng[1]);
            }
            for (com.google.firebase.database.DataSnapshot userSnap : dataSnapshot.getChildren()) {
                String user = userSnap.getKey();
                String locStr = String.valueOf(userSnap.child("location").getValue());
                if (locStr != null && locStr.contains(",")) {
                    String[] userLatLng = locStr.split(",");
                    double userLat = Double.parseDouble(userLatLng[0]);
                    double userLng = Double.parseDouble(userLatLng[1]);
                    double dist = Math.sqrt(Math.pow(caseLat - userLat, 2) + Math.pow(caseLng - userLng, 2));
                    if (dist < minDistance) {
                        minDistance = dist;
                        nearestUser = user;
                    }
                }
            }
            Case newCase = new Case(caseId, location, note, time, matchedSpot);
            newCase.setPriorityUser(nearestUser);
            casesRef.child(caseId).setValue(newCase)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "車案已成功創建", Toast.LENGTH_SHORT).show();
                    // 10秒後自動將 priorityUser 設為 null
                    handler.postDelayed(() -> {
                        casesRef.child(caseId).child("priorityUser").setValue(null);
                    }, 10000);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "創建車案失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });
    }

    private void startAutocompleteActivity() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .setCountries(Arrays.asList("TW"))  // 限制在台灣
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                if (place != null) {
                    // 立即更新地址
                    String address = place.getAddress();
                    if (address != null) {
                        matchedAddress = address;
                        locationEditText.setText(address);
                    }
                    
                    // 如果有经纬度，获取中文地址
                    if (place.getLatLng() != null) {
                        matchedSpot = place.getLatLng().latitude + "," + place.getLatLng().longitude;
                        // 使用 Geocoder 获取中文地址
                        try {
                            Geocoder geocoder = new Geocoder(this, Locale.TAIWAN);
                            List<Address> addresses = geocoder.getFromLocation(place.getLatLng().latitude, place.getLatLng().longitude, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                String chineseAddress = addresses.get(0).getAddressLine(0);
                                if (chineseAddress != null) {
                                    matchedAddress = chineseAddress;
                                    locationEditText.setText(chineseAddress);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedTime.set(Calendar.MINUTE, minute);
                isTimeSelected = true;
                updateCurrentTimeDisplay();
            },
            selectedTime.get(Calendar.HOUR_OF_DAY),
            selectedTime.get(Calendar.MINUTE),
            false  // 使用12小时制
        );
        timePickerDialog.show();
    }

    private void updateCurrentTimeDisplay() {
        String timeText;
        if (isTimeSelected) {
            timeText = timeFormat.format(selectedTime.getTime());
        } else {
            timeText = timeFormat.format(selectedTime.getTime()) + "-現在";
            currentTimeTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
        currentTimeTextView.setText(timeText);
    }

    private void showConfirmationDialog() {
        String location = matchedAddress.isEmpty() ? locationEditText.getText().toString().trim() : matchedAddress;
        String note = noteEditText.getText().toString().trim();
        String time = currentTimeTextView.getText().toString();

        if (location.isEmpty()) {
            locationEditText.setError("請輸入地點");
            return;
        }

        if (matchedSpot.isEmpty()) {
            Toast.makeText(this, "無法獲取地點坐標，請重新選擇地點", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("確認車案")
               .setMessage("地點: " + location + "\n" +
                          "備注: " + note + "\n" +
                          "預約時間: " + time)
               .setPositiveButton("確認", (dialog, which) -> {
                   saveCase(location, note, time);
               })
               .setNegativeButton("取消", null)
               .show();
    }
} 