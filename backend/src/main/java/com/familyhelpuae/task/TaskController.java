package com.familyhelpuae.task;

import com.familyhelpuae.common.dto.TaskCreateRequest;
import com.familyhelpuae.common.dto.TaskDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskCreateRequest request, Authentication authentication) {
        return ResponseEntity.ok(taskService.createTask(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String priority) {
        return ResponseEntity.ok(taskService.getTasks(status, type, category, region, priority));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable String taskId) {
        try {
            return ResponseEntity.ok(taskService.getTaskById(taskId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{taskId}/accept")
    public ResponseEntity<Void> acceptTask(@PathVariable String taskId, Authentication authentication) {
        try {
            taskService.acceptTask(taskId, authentication.getName());
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<Void> completeTask(@PathVariable String taskId, Authentication authentication) {
        try {
            taskService.completeTask(taskId, authentication.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{taskId}/cancel")
    public ResponseEntity<Void> cancelTask(@PathVariable String taskId, Authentication authentication) {
        try {
            taskService.cancelTask(taskId, authentication.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
