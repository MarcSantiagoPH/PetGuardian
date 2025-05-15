package com.example.petguardian;

public class ScheduleItem {
    private final String dateTime;
    private String text;

    public ScheduleItem(String dateTime, String text) {
        this.dateTime = dateTime;
        this.text = text;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String newText) {
        this.text = newText;
    }
}
