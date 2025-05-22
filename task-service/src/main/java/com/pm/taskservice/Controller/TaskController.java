package com.pm.taskservice.Controller;


import com.pm.taskservice.Service.TaskService;
import com.pm.taskservice.dto.TaskRequestDTO;
import com.pm.taskservice.dto.TaskResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Task API", description = "Operations related to task management")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Retrieves a list of all tasks")
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks() {
        logger.info("GET /api/v1/tasks");
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping
    @Operation(summary = "Retrieve tasks by title", description = "Returns a list of tasks matching the given title (partial, case-insensitive)")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByTitle(@RequestParam String title) {
        logger.info("GET /api/v1/tasks?title={}", title);
        List<TaskResponseDTO> tasks = taskService.getTasksByTitle(title); // Adjust service to return list
        return ResponseEntity.ok(tasks);
    }



    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task with the provided details")
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskRequestDTO taskRequestDTO) {
        logger.info("POST /api/v1/tasks");
        TaskResponseDTO createdTask = taskService.createTask(taskRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Update an existing task", description = "Updates a task by its ID")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskRequestDTO taskRequestDTO) {
        logger.info("PUT /api/v1/tasks/{}", taskId);
        TaskResponseDTO updatedTask = taskService.updateTask(taskId, taskRequestDTO);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete a task", description = "Deletes a task by its ID")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID taskId) {
        logger.info("DELETE /api/v1/tasks/{}", taskId);
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}