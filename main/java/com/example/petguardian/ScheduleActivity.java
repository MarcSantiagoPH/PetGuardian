package com.example.petguardian;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.content.pm.PackageManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class ScheduleActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RecyclerView recyclerViewSlots;
    private Button btnAdd, btnEdit, btnDelete;
    private TextView emptyView;

    private ArrayList<ScheduleItem> scheduleList;
    private ScheduleAdapter adapter;
    private String selectedDate = "";
    private int selectedPosition = -1;

    private final String PREF_KEY = "schedule_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        calendarView = findViewById(R.id.calendarView);
        recyclerViewSlots = findViewById(R.id.recyclerViewSlots);
        btnAdd = findViewById(R.id.btnAdd);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        emptyView = new TextView(this);
        emptyView.setText("No scheduled items");
        emptyView.setTextSize(16);
        emptyView.setPadding(20, 40, 20, 20);
        ((ViewGroup) recyclerViewSlots.getParent()).addView(emptyView);

        loadData();

        adapter = new ScheduleAdapter(scheduleList);
        recyclerViewSlots.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSlots.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> selectedPosition = position);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) { return false; }
            @Override public void onSwiped(RecyclerView.ViewHolder vh, int direction) {
                int pos = vh.getAdapterPosition();
                scheduleList.remove(pos);
                adapter.notifyItemRemoved(pos);
                saveData();
                updateVisibility();
            }
        }).attachToRecyclerView(recyclerViewSlots);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
            filterBySelectedDate();
        });

        btnAdd.setOnClickListener(v -> showInputDialog("Add", null));
        btnEdit.setOnClickListener(v -> {
            if (selectedPosition != -1) {
                showInputDialog("Edit", scheduleList.get(selectedPosition));
            } else {
                Toast.makeText(this, "Select an item to edit", Toast.LENGTH_SHORT).show();
            }
        });
        btnDelete.setOnClickListener(v -> {
            if (selectedPosition != -1) {
                scheduleList.remove(selectedPosition);
                adapter.notifyItemRemoved(selectedPosition);
                saveData();
                selectedPosition = -1;
                updateVisibility();
            } else {
                Toast.makeText(this, "Select an item to delete", Toast.LENGTH_SHORT).show();
            }
        });

        selectedDate = getTodayDate();
        filterBySelectedDate();
    }

    private String getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        return String.format(Locale.getDefault(), "%02d/%02d/%04d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR));
    }

    private void showInputDialog(String type, @Nullable ScheduleItem itemToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(type + " Event");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 20, 30, 10);

        EditText input = new EditText(this);
        input.setHint("Enter event details");

        TextView timeText = new TextView(this);
        timeText.setText("Tap to select time");
        timeText.setPadding(0, 20, 0, 20);

        final Calendar c = Calendar.getInstance();
        timeText.setOnClickListener(v -> {
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                timeText.setText(time);
            }, hour, minute, true).show();
        });

        if (itemToEdit != null) {
            input.setText(itemToEdit.getText());
            timeText.setText(itemToEdit.getDateTime().split(" ")[1]);
        }

        layout.addView(input);
        layout.addView(timeText);
        builder.setView(layout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String text = input.getText().toString().trim();
            String time = timeText.getText().toString();
            if (!text.isEmpty() && !selectedDate.isEmpty() && time.contains(":")) {
                String dateTime = selectedDate + " " + time;
                if (type.equals("Add")) {
                    scheduleList.add(new ScheduleItem(dateTime, text));
                } else if (itemToEdit != null) {
                    itemToEdit.setText(text);
                }
                sortScheduleList();
                scheduleAlarm(dateTime, text);
                adapter.notifyDataSetChanged();
                saveData();
                updateVisibility();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void sortScheduleList() {
        scheduleList.sort((a, b) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date dateA = sdf.parse(a.getDateTime());
                Date dateB = sdf.parse(b.getDateTime());
                return dateA.compareTo(dateB);
            } catch (Exception e) {
                return 0;
            }
        });
    }

    private void scheduleAlarm(String dateTime, String message) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = sdf.parse(dateTime);
            if (date != null && date.after(new Date())) {
                long triggerTime = date.getTime();
                Intent intent = new Intent(this, AlarmReceiver.class);
                intent.putExtra("text", message);
                int requestCode = (int) System.currentTimeMillis();
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void filterBySelectedDate() {
        sortScheduleList();
        adapter.notifyDataSetChanged();
        updateVisibility();
    }

    private void updateVisibility() {
        emptyView.setVisibility(scheduleList.stream().noneMatch(item -> item.getDateTime().startsWith(selectedDate)) ? View.VISIBLE : View.GONE);
    }


    private void saveData() {
        SharedPreferences prefs = getSharedPreferences("schedulePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_KEY, new Gson().toJson(scheduleList));
        editor.apply();
    }

    private void loadData() {
        SharedPreferences prefs = getSharedPreferences("schedulePrefs", MODE_PRIVATE);
        String json = prefs.getString(PREF_KEY, null);
        if (json != null) {
            scheduleList = new Gson().fromJson(json, new TypeToken<ArrayList<ScheduleItem>>() {}.getType());
        } else {
            scheduleList = new ArrayList<>();
        }
    }
}