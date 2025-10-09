package com.nwu.csvts.service;

import com.nwu.csvts.model.Assignment;
import com.nwu.csvts.model.Volunteer;
import com.nwu.csvts.repository.AssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AssignmentService {
    
    private final AssignmentRepository assignmentRepository;
    
    @Autowired
    public AssignmentService(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }
    
    public Optional<Assignment> getAssignmentById(Long assignmentId) {
        return assignmentRepository.findById(assignmentId);
    }
    
    public List<Assignment> getAssignmentsByVolunteer(Volunteer volunteer) {
        return assignmentRepository.findByVolunteer(volunteer);
    }
    
    public List<Assignment> getAssignmentsByVolunteerAndStatus(Volunteer volunteer, String status) {
        return assignmentRepository.findByVolunteerAndStatus(volunteer, status);
    }
    
    public List<Assignment> getOverdueAssignments() {
        return assignmentRepository.findOverdueAssignments();
    }
    
    public Assignment markAssignmentAsCompleted(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .map(assignment -> {
                    assignment.markAsCompleted();
                    return assignmentRepository.save(assignment);
                })
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));
    }
    
    public Assignment markAssignmentAsInProgress(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .map(assignment -> {
                    assignment.markAsInProgress();
                    return assignmentRepository.save(assignment);
                })
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));
    }
    
    public long getAssignmentCountByVolunteerAndStatus(Volunteer volunteer, String status) {
        return assignmentRepository.countByVolunteerAndStatus(volunteer, status);
    }
    
    public List<Assignment> getActiveAssignmentsByVolunteer(Volunteer volunteer) {
        return assignmentRepository.findByVolunteerAndStatus(volunteer, "ASSIGNED");
    }
    
    public List<Assignment> getCompletedAssignmentsByVolunteer(Volunteer volunteer) {
        return assignmentRepository.findByVolunteerAndStatus(volunteer, "COMPLETED");
    }
    
    // Add this missing method
    public void deleteAssignment(Long assignmentId) {
        if (assignmentRepository.existsById(assignmentId)) {
            assignmentRepository.deleteById(assignmentId);
        } else {
            throw new RuntimeException("Assignment not found with id: " + assignmentId);
        }
    }
}