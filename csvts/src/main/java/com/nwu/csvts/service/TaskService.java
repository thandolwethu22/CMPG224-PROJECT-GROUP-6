package com.nwu.csvts.service;

import com.nwu.csvts.model.Task;
import com.nwu.csvts.model.User;
import com.nwu.csvts.model.Volunteer;
import com.nwu.csvts.model.Assignment;
import com.nwu.csvts.repository.TaskRepository;
import com.nwu.csvts.repository.AssignmentRepository;
import com.nwu.csvts.repository.VolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final AssignmentRepository assignmentRepository;
    private final VolunteerRepository volunteerRepository;
    
    @Autowired
    public TaskService(TaskRepository taskRepository, 
                      AssignmentRepository assignmentRepository,
                      VolunteerRepository volunteerRepository) {
        this.taskRepository = taskRepository;
        this.assignmentRepository = assignmentRepository;
        this.volunteerRepository = volunteerRepository;
    }
    
    // Task Management Methods
    public Task createTask(Task task, User createdBy) {
        task.setCreatedBy(createdBy);
        return taskRepository.save(task);
    }
    
    public Optional<Task> getTaskById(Long taskId) {
        return taskRepository.findById(taskId);
    }
    
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    
    public List<Task> getTasksByCreator(User createdBy) {
        return taskRepository.findByCreatedBy(createdBy);
    }
    
    public List<Task> getTasksByStatus(String status) {
        return taskRepository.findByStatus(status);
    }
    
    public List<Task> searchTasks(String keyword) {
        return taskRepository.findByTitleContainingIgnoreCase(keyword);
    }
    
    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks();
    }
    
    public Task updateTask(Long taskId, Task taskDetails) {
        return taskRepository.findById(taskId)
                .map(existingTask -> {
                    existingTask.setTitle(taskDetails.getTitle());
                    existingTask.setDescription(taskDetails.getDescription());
                    existingTask.setStatus(taskDetails.getStatus());
                    existingTask.setStartDate(taskDetails.getStartDate());
                    existingTask.setDueDate(taskDetails.getDueDate());
                    return taskRepository.save(existingTask);
                })
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
    }
    
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        taskRepository.delete(task);
    }
    
    // Assignment Management Methods
    @Transactional
    public Assignment assignTaskToVolunteer(Long taskId, Long volunteerId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        
        Volunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new RuntimeException("Volunteer not found with id: " + volunteerId));
        
        // Check if already assigned
        if (assignmentRepository.existsByVolunteerAndTask(volunteer, task)) {
            throw new RuntimeException("Volunteer is already assigned to this task");
        }
        
        Assignment assignment = new Assignment(task, volunteer, "ASSIGNED");
        Assignment savedAssignment = assignmentRepository.save(assignment);
        
        // Update bidirectional relationship
        task.addAssignment(savedAssignment);
        volunteer.addAssignment(savedAssignment);
        
        return savedAssignment;
    }
    
    public List<Assignment> getTaskAssignments(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        return assignmentRepository.findByTask(task);
    }
    
    public List<Task> getTasksAssignedToVolunteer(Long volunteerId) {
        return taskRepository.findTasksByVolunteerId(volunteerId);
    }
    
    public Assignment updateAssignmentStatus(Long assignmentId, String status) {
        return assignmentRepository.findById(assignmentId)
                .map(assignment -> {
                    assignment.setStatus(status);
                    if ("COMPLETED".equals(status)) {
                        assignment.markAsCompleted();
                    }
                    return assignmentRepository.save(assignment);
                })
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));
    }
    
    public void removeAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));
        
        // Remove from bidirectional relationships
        assignment.getTask().getAssignments().remove(assignment);
        assignment.getVolunteer().getAssignments().remove(assignment);
        
        assignmentRepository.delete(assignment);
    }
    
    // Statistics and Reports
    public long getTotalTaskCount() {
        return taskRepository.count();
    }
    
    public long getTaskCountByStatus(String status) {
        return taskRepository.countByStatus(status);
    }
    
    public List<Task> getTasksDueSoon(int days) {
        LocalDate dueDate = LocalDate.now().plusDays(days);
        return taskRepository.findByDueDateBefore(dueDate);
    }
}