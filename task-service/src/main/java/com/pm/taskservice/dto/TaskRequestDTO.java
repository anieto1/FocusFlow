package com.pm.taskservice.dto;

import com.pm.taskservice.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class TaskRequestDTO {

    @NotNull
    private UUID taskId;

    @NotNull
    private String sessionId;

    @NotNull
    private String userId;

    @NotBlank(message = "Title is required")
    @Size(max = 50, message = "Title must be less than 50 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;

    @NotNull
    private TaskStatus taskStatus;


    public String getSessionId() {
        return sessionId;}

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getTaskId(){return  taskId;}

    public void setTaskId(UUID taskId){this.taskId = taskId;}

    public String getUser_id() {
        return userId;
    }

    public void setUser_id(String user_id) {
        this.userId = user_id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }
}
