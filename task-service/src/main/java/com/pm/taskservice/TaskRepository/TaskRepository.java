package com.pm.taskservice.TaskRepository;

import com.pm.taskservice.model.Task;
import com.pm.taskservice.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {


    void deleteAllBySessionId(UUID sessionId);

    long countByUserId(UUID userId);

    List<Task> findByTitleContainingIgnoreCase(String title);

    List<Task> findAllByUserId(UUID userId);

    List<Task> findAllBySessionId(UUID sessionId);

    List<Task> findAllByStatus(TaskStatus status);
}
