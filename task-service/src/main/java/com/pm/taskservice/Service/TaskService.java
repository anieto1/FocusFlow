package com.pm.taskservice.Service;

import com.pm.taskservice.Exception.TaskNotFoundException;
import com.pm.taskservice.Mapper.TaskMapper;
import com.pm.taskservice.TaskRepository.TaskRepository;
import com.pm.taskservice.dto.TaskRequestDTO;
import com.pm.taskservice.dto.TaskResponseDTO;
import com.pm.taskservice.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Retrieves all tasks from the repository and maps them to response DTOs.
     */
    public List<TaskResponseDTO> getAllTasks() {
        logger.info("Fetching all tasks...");
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(TaskMapper::toTaskResponseDTO)
                .collect(Collectors.toList());
    }

    public TaskResponseDTO getTaskById(UUID taskId) {
        logger.info("Fetching task with ID: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));
        return TaskMapper.toTaskResponseDTO(task);
    }

    /**
     * Creates a new task based on the provided DTO.
     */
    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO) {
        logger.info("Creating a new task for session ID: {}", taskRequestDTO.getSessionId());

        Task task = TaskMapper.toTask(taskRequestDTO);
        task.setCreatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);
        return TaskMapper.toTaskResponseDTO(savedTask);
    }

    /**
     * Updates an existing task based on the task ID and provided DTO.
     */
    @Transactional
    public TaskResponseDTO updateTask(UUID taskId, TaskRequestDTO taskRequestDTO) {
        logger.info("Updating task with ID: {}", taskId);

        Task updateTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));

        updateTask.setTitle(taskRequestDTO.getTitle());
        updateTask.setDescription(taskRequestDTO.getDescription());
        updateTask.setStatus(taskRequestDTO.getTaskStatus());

        return TaskMapper.toTaskResponseDTO(updateTask);
    }

    /**
     * Deletes a task by its ID.
     */
    @Transactional
    public void deleteTask(UUID taskId) {
        logger.info("Deleting task with ID: {}", taskId);

        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException("Task not found with ID: " + taskId);
        }

        taskRepository.deleteById(taskId);
    }
}