package com.nwu.csvts.repository;

import com.nwu.csvts.model.Task;
import com.nwu.csvts.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // Find tasks created by a specific admin
    List<Task> findByCreatedBy(User createdBy);
    
    // Find tasks by status
    List<Task> findByStatus(String status);
    
    // Search tasks by title containing keyword
    List<Task> findByTitleContainingIgnoreCase(String keyword);
    
    // Find tasks due before a specific date
    List<Task> findByDueDateBefore(LocalDate date);
    
    // ADD THESE MISSING METHODS:
    @Query("SELECT t FROM Task t WHERE t.dueDate < CURRENT_DATE AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks();
    
    // UPDATED QUERY (corrected):
    @Query("SELECT DISTINCT t FROM Task t JOIN t.assignments a WHERE a.volunteer.volunteerId = :volunteerId")
    List<Task> findTasksByVolunteerId(@Param("volunteerId") Long volunteerId);
    
    long countByStatus(String status);
}