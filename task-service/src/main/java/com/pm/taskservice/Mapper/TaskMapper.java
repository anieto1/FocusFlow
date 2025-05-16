package com.pm.taskservice.Mapper;

import com.pm.taskservice.dto.TaskResponseDTO;
import com.pm.taskservice.model.Task;

public class TaskMapper {
    public static TaskResponseDTO toTaskResponseDTO(Task task) {
        if(task == null){
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


}
