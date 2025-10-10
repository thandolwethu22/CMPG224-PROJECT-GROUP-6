package com.nwu.csvts.repository;

import com.nwu.csvts.model.TimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLog, Long> {

    List<TimeLog> findByStatusOrderByCreatedAtDesc(String status);

    List<TimeLog> findByVolunteerVolunteerIdOrderByCreatedAtDesc(Long volunteerId);

    List<TimeLog> findByAssignmentAssignmentIdOrderByCreatedAtDesc(Long assignmentId);

    List<TimeLog> findByAssignmentVolunteerVolunteerIdOrderByCreatedAtDesc(Long volunteerId);

    @Query("SELECT COALESCE(SUM(t.hoursWorked), 0) FROM TimeLog t WHERE t.status = 'APPROVED'")
    Double sumAllApprovedHours();

    @Query("SELECT COALESCE(SUM(t.hoursWorked), 0) FROM TimeLog t WHERE t.volunteer.volunteerId = :volunteerId AND t.status = 'APPROVED'")
    Double sumApprovedHoursByVolunteerId(@Param("volunteerId") Long volunteerId);

    @Query("SELECT COALESCE(SUM(t.hoursWorked), 0) FROM TimeLog t WHERE t.volunteer.volunteerId = :volunteerId AND t.status = 'PENDING'")
    Double sumPendingHoursByVolunteerId(@Param("volunteerId") Long volunteerId);

    Long countByStatus(String status);

    // convenience finder
    @Query("SELECT t FROM TimeLog t WHERE t.task.taskId = :taskId AND t.volunteer.volunteerId = :volunteerId")
    List<TimeLog> findByTaskIdAndVolunteerId(@Param("taskId") Long taskId, @Param("volunteerId") Long volunteerId);
}