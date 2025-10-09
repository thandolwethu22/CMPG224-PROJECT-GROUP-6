package com.nwu.csvts.controller;

import com.nwu.csvts.model.Task;
import com.nwu.csvts.model.User;
import com.nwu.csvts.model.Assignment;
import com.nwu.csvts.service.TaskService;
import com.nwu.csvts.service.UserService;
import com.nwu.csvts.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tasks")
public class TaskController {
    
    private final TaskService taskService;
    private final UserService userService;
    private final VolunteerService volunteerService;
    
    @Autowired
    public TaskController(TaskService taskService, UserService userService, VolunteerService volunteerService) {
        this.taskService = taskService;
        this.userService = userService;
        this.volunteerService = volunteerService;
    }
    
    // Helper method to get current user
    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
    
    // Admin: View all tasks
    @GetMapping("/admin")
    public String viewAllTasks(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        List<Task> tasks = taskService.getAllTasks();
        
        model.addAttribute("tasks", tasks);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("totalTasks", taskService.getTotalTaskCount());
        model.addAttribute("openTasks", taskService.getTaskCountByStatus("OPEN"));
        model.addAttribute("completedTasks", taskService.getTaskCountByStatus("COMPLETED"));
        
        return "admin/tasks";
    }
    
    // Admin: Show create task form
    @GetMapping("/admin/create")
    public String showCreateTaskForm(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        model.addAttribute("task", new Task());
        model.addAttribute("currentUser", currentUser);
        return "admin/create-task";
    }
    
    // Admin: Create new task
    @PostMapping("/admin/create")
    public String createTask(@ModelAttribute Task task, 
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser(authentication);
            Task createdTask = taskService.createTask(task, currentUser);
            
            redirectAttributes.addFlashAttribute("success", 
                "Task '" + createdTask.getTitle() + "' created successfully!");
            return "redirect:/tasks/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to create task: " + e.getMessage());
            return "redirect:/tasks/admin/create";
        }
    }
    
    // Admin: View task details
    @GetMapping("/admin/{taskId}")
    public String viewTaskDetails(@PathVariable Long taskId, 
                                Authentication authentication, 
                                Model model) {
        User currentUser = getCurrentUser(authentication);
        Optional<Task> task = taskService.getTaskById(taskId);
        
        if (task.isPresent()) {
            List<Assignment> assignments = taskService.getTaskAssignments(taskId);
            model.addAttribute("task", task.get());
            model.addAttribute("assignments", assignments);
            model.addAttribute("currentUser", currentUser);
            return "admin/task-details";
        } else {
            return "redirect:/tasks/admin?error=Task not found";
        }
    }
    
    // Admin: Show edit task form
    @GetMapping("/admin/{taskId}/edit")
    public String showEditTaskForm(@PathVariable Long taskId, 
                                 Authentication authentication, 
                                 Model model) {
        User currentUser = getCurrentUser(authentication);
        Optional<Task> task = taskService.getTaskById(taskId);
        
        if (task.isPresent()) {
            model.addAttribute("task", task.get());
            model.addAttribute("currentUser", currentUser);
            return "admin/edit-task";
        } else {
            return "redirect:/tasks/admin?error=Task not found";
        }
    }
    
    // Admin: Update task
    @PostMapping("/admin/{taskId}/edit")
    public String updateTask(@PathVariable Long taskId, 
                           @ModelAttribute Task taskDetails,
                           RedirectAttributes redirectAttributes) {
        try {
            Task updatedTask = taskService.updateTask(taskId, taskDetails);
            redirectAttributes.addFlashAttribute("success", 
                "Task '" + updatedTask.getTitle() + "' updated successfully!");
            return "redirect:/tasks/admin/" + taskId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to update task: " + e.getMessage());
            return "redirect:/tasks/admin/" + taskId + "/edit";
        }
    }
    
    // Admin: Delete task
    @PostMapping("/admin/{taskId}/delete")
    public String deleteTask(@PathVariable Long taskId, 
                           RedirectAttributes redirectAttributes) {
        try {
            Optional<Task> task = taskService.getTaskById(taskId);
            if (task.isPresent()) {
                taskService.deleteTask(taskId);
                redirectAttributes.addFlashAttribute("success", 
                    "Task '" + task.get().getTitle() + "' deleted successfully!");
            }
            return "redirect:/tasks/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to delete task: " + e.getMessage());
            return "redirect:/tasks/admin/" + taskId;
        }
    }
    
    // Search tasks
    @GetMapping("/search")
    public String searchTasks(@RequestParam String keyword, 
                            Authentication authentication, 
                            Model model) {
        User currentUser = getCurrentUser(authentication);
        List<Task> tasks = taskService.searchTasks(keyword);
        
        model.addAttribute("tasks", tasks);
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("currentUser", currentUser);
        
        if ("ADMIN".equals(currentUser.getRole())) {
            return "admin/tasks";
        } else {
            return "volunteer/my-tasks";
        }
    }
}