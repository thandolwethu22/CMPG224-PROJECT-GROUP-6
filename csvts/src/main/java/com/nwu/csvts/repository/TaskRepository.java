// TaskRepository.java
package com.nwu.csvts.repository;

import com.nwu.csvts.model.Task;
import com.nwu.csvts.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    List<Task> findByDueDateBefore(java.time.LocalDate date);
    
    // Custom query for tasks assigned to a specific volunteer
    @Query("SELECT t FROM Task t JOIN Assignment a ON t.taskId = a.task.taskId WHERE a.volunteer.volunteerId = :volunteerId")
    List<Task> findTasksByVolunteerId(@Param("volunteerId") Long volunteerId);
}