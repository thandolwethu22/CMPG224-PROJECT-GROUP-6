package com.nwu.csvts.repository;

public interface VolunteerHoursProjection {
    Long getVolunteerId();
    String getFirstName();
    String getLastName();
    Double getTotalHours();
}