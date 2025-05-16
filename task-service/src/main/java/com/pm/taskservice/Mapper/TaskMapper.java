package com.pm.taskservice.Mapper;

import com.pm.taskservice.dto.TaskRequestDTO;
import com.pm.taskservice.dto.TaskResponseDTO;
import com.pm.taskservice.model.Task;
import com.pm.taskservice.model.TaskStatus;

import java.util.UUID;

public class TaskMapper {

    public static TaskResponseDTO toTaskResponseDTO(Task task) {
        if (task == null) {
            return null;
        }

        TaskResponseDTO taskResponseDTO = new TaskResponseDTO();
        taskResponseDTO.setTaskId(task.getTaskId().toString());
        taskResponseDTO.setUserId(task.getUserId().toString());
        taskResponseDTO.setSessionId(task.getSessionId().toString());
        taskResponseDTO.setTitle(task.getTitle());
        taskResponseDTO.setDescription(task.getDescription());
        taskResponseDTO.setStatus(task.getStatus());

        return taskResponseDTO;
    }

    public static Task toTask(TaskRequestDTO taskRequestDTO) {
        if (taskRequestDTO == null) {
            return null;
        }

        Task task = new Task();
        task.setTaskId(taskRequestDTO.getTaskId());
        task.setUserId(UUID.fromString(taskRequestDTO.getUser_id()));
        task.setSessionId(UUID.fromString(taskRequestDTO.getSessionId()));
        task.setTitle(taskRequestDTO.getTitle());
        task.setDescription(taskRequestDTO.getDescription());
        task.setStatus(taskRequestDTO.getTaskStatus());

        return task;
    }
}
