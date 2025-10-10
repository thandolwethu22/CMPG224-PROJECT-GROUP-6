package com.nwu.csvts.model;

/**
 * TimeLog statuses used across the application.
 */
public enum TimeLogStatus {
    PENDING,
    APPROVED,
    REJECTED,
    TRACKING;

    /**
     * Parse a status string in a null-safe, case-insensitive way.
     * Returns PENDING if input is null or unrecognized.
     */
    public static TimeLogStatus from(String s) {
        if (s == null) return PENDING;
        try {
            return TimeLogStatus.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return PENDING;
        }
    }

    /**
     * Returns true if this status is a terminal (final) state.
     */
    public boolean isFinal() {
        return this == APPROVED || this == REJECTED;
    }
}