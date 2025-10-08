package com.linearity.utils;

public class FormatTimeUtils {
    public static String formatTimeForTimer(int totalSeconds) {

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            // Show hours if needed → e.g. 1:05:09
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            // Only minutes + seconds → e.g. 50:01 or 0:01
            return String.format("%d:%02d", minutes, seconds);
        }
    }
}
