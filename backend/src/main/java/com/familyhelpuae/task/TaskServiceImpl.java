package com.familyhelpuae.task;

import com.familyhelpuae.auth.AuthAccount;
import com.familyhelpuae.auth.AuthAccountRepository;
import com.familyhelpuae.category.Category;
import com.familyhelpuae.common.dto.TaskCreateRequest;
import com.familyhelpuae.common.dto.TaskDTO;
import com.familyhelpuae.family.Family;
import com.familyhelpuae.region.Region;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final AuthAccountRepository authAccountRepository;

    public TaskServiceImpl(TaskRepository taskRepository, AuthAccountRepository authAccountRepository) {
        this.taskRepository = taskRepository;
        this.authAccountRepository = authAccountRepository;
    }

    private Family getFamilyByEmail(String email) {
        return authAccountRepository.findByEmail(email)
                .map(AuthAccount::getFamily)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    @Transactional
    public TaskDTO createTask(TaskCreateRequest request, String email) {
        Family family = getFamilyByEmail(email);

        Task task = new Task();
        task.setTaskId(UUID.randomUUID().toString());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setType(request.getType());
        task.setStatus("OPEN");
        task.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        task.setCreatedAt(LocalDateTime.now());
        task.setScheduledAt(request.getScheduledAt());
        task.setVersion(0L);

        Category cat = new Category();
        cat.setCategoryId(request.getCategory().toLowerCase().replace(" ", "-"));
        cat.setName(request.getCategory());
        task.setCategory(cat);

        Region region = new Region();
        region.setRegionId(request.getRegion().toLowerCase().replace(" ", "-"));
        region.setName(request.getRegion());
        task.setRegion(region);

        taskRepository.save(task);
        taskRepository.linkTaskToFamily(family.getFamilyId(), task.getTaskId());

        return mapToDTO(task);
    }

    @Override
    public List<TaskDTO> getTasks(String status, String type, String category, String region, String priority) {
        return taskRepository.findAll().stream()
                .filter(t -> status == null || status.equals(t.getStatus()))
                .filter(t -> type == null || type.equals(t.getType()))
                .filter(t -> category == null || (t.getCategory() != null && category.equals(t.getCategory().getName())))
                .filter(t -> region == null || (t.getRegion() != null && region.equals(t.getRegion().getName())))
                .filter(t -> priority == null || priority.equals(t.getPriority()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TaskDTO getTaskById(String taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        return mapToDTO(task);
    }

    @Override
    @Transactional
    public void acceptTask(String taskId, String email) {
        Family family = getFamilyByEmail(email);
        
        long updated = taskRepository.atomicAcceptTask(
                taskId, 
                family.getFamilyId(), 
                UUID.randomUUID().toString(), 
                LocalDateTime.now()
        );

        if (updated == 0) {
            throw new IllegalStateException("Task is either not OPEN, or you are the creator, or another concurrency issue occurred.");
        }
    }

    @Override
    @Transactional
    public void completeTask(String taskId, String email) {
        Family family = getFamilyByEmail(email);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!"IN_PROGRESS".equals(task.getStatus())) {
            throw new IllegalStateException("Task must be IN_PROGRESS to complete");
        }

        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public void cancelTask(String taskId, String email) {
        Family family = getFamilyByEmail(email);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if ("COMPLETED".equals(task.getStatus())) {
            throw new IllegalStateException("Completed tasks cannot be cancelled");
        }

        task.setStatus("CANCELLED");
        taskRepository.save(task);
    }

    private TaskDTO mapToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setTaskId(task.getTaskId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setType(task.getType());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setScheduledAt(task.getScheduledAt());
        
        if (task.getCategory() != null) {
            dto.setCategory(task.getCategory().getName());
        }
        if (task.getRegion() != null) {
            dto.setRegion(task.getRegion().getName());
        }
        
        return dto;
    }
}
