package com.example.todo.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Static utility for converting timestamps to human-readable relative time strings in templates.
 */
@Component("timeUtil")
public class TimeUtil {

    public String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();

        long years = ChronoUnit.YEARS.between(dateTime, now);
        long months = ChronoUnit.MONTHS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long seconds = ChronoUnit.SECONDS.between(dateTime, now);

        if (seconds < 0) {
            return "just now";
        }

        if (years > 0) {
            return years == 1 ? "1 year ago" : years + " years ago";
        } else if (months > 0) {
            return months == 1 ? "1 month ago" : months + " months ago";
        } else if (days > 0) {
            if (days == 1) {
                return "yesterday";
            } else if (days < 7) {
                return days + " days ago";
            } else if (days < 14) {
                return "1 week ago";
            } else if (days < 30) {
                long weeks = days / 7;
                return weeks + " weeks ago";
            } else {
                return days + " days ago";
            }
        } else if (hours > 0) {
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        } else if (minutes > 0) {
            return minutes == 1 ? "1 minute ago" : minutes + " minutes ago";
        } else {
            return "just now";
        }
    }

    public String lastEdited(LocalDateTime updatedAt) {
        String relativeTime = getRelativeTime(updatedAt);
        return relativeTime.isEmpty() ? "" : "Last updated " + relativeTime;
    }

    public String created(LocalDateTime createdAt) {
        String relativeTime = getRelativeTime(createdAt);
        return relativeTime.isEmpty() ? "" : "Created " + relativeTime;
    }


    public String getSmartTimeDisplay(LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (updatedAt == null && createdAt == null) {
            return "";
        }

        if (updatedAt == null) {
            return created(createdAt);
        }

        if (createdAt == null) {
            return lastEdited(updatedAt);
        }

        // If updated within 1 minute of creation, show "created"
        long minutesBetween = ChronoUnit.MINUTES.between(createdAt, updatedAt);
        if (minutesBetween <= 1) {
            return created(createdAt);
        }

        // Else show last edited
        return lastEdited(updatedAt);
    }
}