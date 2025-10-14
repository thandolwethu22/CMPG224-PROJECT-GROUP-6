package com.nwu.csvts.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class DashboardService {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public VolunteerDashboardData getVolunteerDashboardData(Long volunteerId) {
        String sql = """
            SELECT 
                (SELECT COUNT(*) FROM assignments a WHERE a.volunteer_id = :volunteerId) as totalAssignments,
                (SELECT COUNT(*) FROM assignments a 
                 JOIN tasks t ON a.task_id = t.task_id 
                 WHERE a.volunteer_id = :volunteerId AND t.status IN ('OPEN', 'IN_PROGRESS')) as activeAssignments,
                (SELECT COALESCE(SUM(tl.hours_worked), 0) FROM time_logs tl 
                 WHERE tl.volunteer_id = :volunteerId AND tl.status = 'APPROVED') as totalHours,
                (SELECT COUNT(*) FROM assignments a 
                 JOIN tasks t ON a.task_id = t.task_id 
                 WHERE a.volunteer_id = :volunteerId AND t.status = 'COMPLETED') as completedTasks
            """;
        
        try {
            Object[] result = (Object[]) entityManager.createNativeQuery(sql)
                .setParameter("volunteerId", volunteerId)
                .getSingleResult();
            
            Long totalAssignments = ((Number) result[0]).longValue();
            Long activeAssignments = ((Number) result[1]).longValue();
            Double totalHours = ((Number) result[2]).doubleValue();
            Long completedTasks = ((Number) result[3]).longValue();
            
            Double completionRate = totalAssignments > 0 ? 
                (completedTasks.doubleValue() / totalAssignments.doubleValue()) * 100 : 0.0;
            
            return new VolunteerDashboardData(totalAssignments, activeAssignments, totalHours, completionRate);
        } catch (Exception e) {
            // Return default values in case of error
            return new VolunteerDashboardData(0L, 0L, 0.0, 0.0);
        }
    }
    
    public AdminDashboardData getAdminDashboardData() {
        String sql = """
            SELECT 
                (SELECT COUNT(*) FROM tasks) as totalTasks,
                (SELECT COUNT(*) FROM tasks WHERE status = 'COMPLETED') as completedTasks,
                (SELECT COUNT(*) FROM volunteers) as totalVolunteers,
                (SELECT COUNT(DISTINCT v.volunteer_id) FROM volunteers v 
                 JOIN assignments a ON v.volunteer_id = a.volunteer_id 
                 JOIN tasks t ON a.task_id = t.task_id 
                 WHERE t.status IN ('OPEN', 'IN_PROGRESS')) as activeVolunteers,
                (SELECT COALESCE(SUM(hours_worked), 0) FROM time_logs WHERE status = 'APPROVED') as totalHours,
                (SELECT COUNT(*) FROM time_logs WHERE status = 'PENDING') as pendingApprovals
            """;
        
        try {
            Object[] result = (Object[]) entityManager.createNativeQuery(sql).getSingleResult();
            
            return new AdminDashboardData(
                ((Number) result[0]).longValue(),
                ((Number) result[1]).longValue(),
                ((Number) result[2]).longValue(),
                ((Number) result[3]).longValue(),
                ((Number) result[4]).doubleValue(),
                ((Number) result[5]).longValue()
            );
        } catch (Exception e) {
            // Return default values in case of error
            return new AdminDashboardData(0L, 0L, 0L, 0L, 0.0, 0L);
        }
    }
    
    // Data transfer objects
    public record VolunteerDashboardData(
        Long totalAssignments, 
        Long activeAssignments, 
        Double totalHours, 
        Double completionRate
    ) {}
    
    public record AdminDashboardData(
        Long totalTasks, 
        Long completedTasks, 
        Long totalVolunteers, 
        Long activeVolunteers, 
        Double totalHours, 
        Long pendingApprovals
    ) {}
}