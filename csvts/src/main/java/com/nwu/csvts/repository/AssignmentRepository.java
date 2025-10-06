// AssignmentRepository.java
package com.nwu.csvts.repository;

import com.nwu.csvts.model.Assignment;
import com.nwu.csvts.model.Task;
import com.nwu.csvts.model.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    
    // Find assignments by volunteer
    List<Assignment> findByVolunteer(Volunteer volunteer);
    
    // Find assignments by task
    List<Assignment> findByTask(Task task);
    
    // Find specific assignment by volunteer and task
    Optional<Assignment> findByVolunteerAndTask(Volunteer volunteer, Task task);
    
    // Find assignments by status
    List<Assignment> findByStatus(String status);
    
    // Find assignments for a volunteer with specific status
    List<Assignment> findByVolunteerAndStatus(Volunteer volunteer, String status);
    
    // Check if a volunteer is already assigned to a task
    boolean existsByVolunteerAndTask(Volunteer volunteer, Task task);
}
