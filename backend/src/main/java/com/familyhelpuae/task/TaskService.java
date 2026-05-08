package com.familyhelpuae.task;

import com.familyhelpuae.common.dto.TaskCreateRequest;
import com.familyhelpuae.common.dto.TaskDTO;

import java.util.List;

public interface TaskService {
    TaskDTO createTask(TaskCreateRequest request, String email);
    List<TaskDTO> getTasks(String status, String type, String category, String region, String priority);
    TaskDTO getTaskById(String taskId);
    void acceptTask(String taskId, String email);
    void completeTask(String taskId, String email);
    void cancelTask(String taskId, String email);
}
