package com.pm.sessionservice.Controller;

import com.pm.sessionservice.DTO.*;
import com.pm.sessionservice.Service.SessionService;
import com.pm.sessionservice.model.SessionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Session Management
 * 
 * Provides complete HTTP API for collaborative pomodoro session management
 * including CRUD operations, lifecycle management, participant management,
 * pomodoro phase control, and real-time progress tracking.
 */
@RestController
@RequestMapping("/api/sessions")
@Tag(name = "Session Management", description = "Complete API for collaborative pomodoro sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final SessionService sessionService;

    // ==================== CRUD Operations ====================

    @PostMapping
    @Operation(summary = "Create new session", description = "Creates a new collaborative pomodoro session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Session created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid session data"),
        @ApiResponse(responseCode = "409", description = "User already has an active session")
    })
    public ResponseEntity<SessionResponseDTO> createSession(
            @Valid @RequestBody SessionRequestDTO sessionRequest,
            @Parameter(description = "Owner user ID") @RequestHeader("X-User-ID") UUID ownerId) {
        
        log.info("Creating session for user: {}", ownerId);
        SessionResponseDTO response = sessionService.createSession(sessionRequest, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{sessionId}")
    @Operation(summary = "Update session", description = "Updates session configuration (owner only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Session updated successfully"),
        @ApiResponse(responseCode = "404", description = "Session not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - owner only")
    })
    public ResponseEntity<SessionResponseDTO> updateSession(
            @PathVariable UUID sessionId,
            @Valid @RequestBody UpdateSessionRequestDTO updateRequest,
            @RequestHeader("X-User-ID") UUID ownerId) {
        
        SessionResponseDTO response = sessionService.updateSession(sessionId, updateRequest, ownerId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Delete session", description = "Soft deletes session (owner only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Session deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Session not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - owner only")
    })
    public ResponseEntity<Void> deleteSession(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-ID") UUID ownerId) {
        
        sessionService.deleteSession(sessionId, ownerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/current")
    @Operation(summary = "Get current active session", description = "Retrieves user's current active session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active session found"),
        @ApiResponse(responseCode = "404", description = "No active session")
    })
    public ResponseEntity<SessionResponseDTO> getCurrentActiveSession(
            @RequestHeader("X-User-ID") UUID userId) {
        
        SessionResponseDTO response = sessionService.getCurrentActiveSession(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/invite/{inviteCode}")
    @Operation(summary = "Get session by invite code", description = "Looks up session using invite code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Session found"),
        @ApiResponse(responseCode = "404", description = "Invalid invite code")
    })
    public ResponseEntity<SessionResponseDTO> getSessionByInviteCode(
            @Parameter(description = "Session invite code") @PathVariable String inviteCode) {
        
        SessionResponseDTO response = sessionService.getSessionByInviteCode(inviteCode);
        return ResponseEntity.ok(response);
    }

    // ==================== Session Lifecycle Management ====================

    @PostMapping("/{sessionId}/end")
    @Operation(summary = "End session", description = "Completes session and calculates metrics (owner only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Session ended successfully"),
        @ApiResponse(responseCode = "404", description = "Session not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - owner only")
    })
    public ResponseEntity<SessionResponseDTO> endSession(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-ID") UUID userId,
            @RequestBody EndSessionRequestDTO endRequest) {
        
        SessionResponseDTO response = sessionService.endSession(sessionId, userId, endRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/pause")
    @Operation(summary = "Pause session", description = "Pauses active session (owner only)")
    @ApiResponse(responseCode = "200", description = "Session paused successfully")
    public ResponseEntity<SessionResponseDTO> pauseSession(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        SessionResponseDTO response = sessionService.pauseSession(sessionId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/resume")
    @Operation(summary = "Resume session", description = "Resumes paused session (owner only)")
    @ApiResponse(responseCode = "200", description = "Session resumed successfully")
    public ResponseEntity<SessionResponseDTO> resumeSession(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        SessionResponseDTO response = sessionService.resumeSession(sessionId, userId);
        return ResponseEntity.ok(response);
    }

    // ==================== Participant Management ====================

    @PostMapping("/{sessionId}/invite")
    @Operation(summary = "Get invite code", description = "Returns invite code for sharing (owner only)")
    @ApiResponse(responseCode = "200", description = "Invite code returned successfully")
    public ResponseEntity<SessionResponseDTO> inviteUser(
            @PathVariable UUID sessionId,
            @RequestParam UUID inviteeId,
            @RequestHeader("X-User-ID") UUID inviterId) {
        
        SessionResponseDTO response = sessionService.inviteUser(sessionId, inviteeId, inviterId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/join")
    @Operation(summary = "Join session", description = "Join session using invite code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully joined session"),
        @ApiResponse(responseCode = "400", description = "Invalid invite code or session full"),
        @ApiResponse(responseCode = "409", description = "Already a participant")
    })
    public ResponseEntity<SessionResponseDTO> joinSession(
            @PathVariable UUID sessionId,
            @RequestParam String inviteCode,
            @RequestHeader("X-User-ID") UUID userId) {
        
        SessionResponseDTO response = sessionService.joinSession(sessionId, userId, inviteCode);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/leave")
    @Operation(summary = "Leave session", description = "Leave session (participants only, owners must delete)")
    @ApiResponse(responseCode = "204", description = "Successfully left session")
    public ResponseEntity<Void> leaveSession(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        sessionService.leaveSession(sessionId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{sessionId}/participants/{userToRemove}")
    @Operation(summary = "Remove participant", description = "Remove user from session (owner only)")
    @ApiResponse(responseCode = "200", description = "Participant removed successfully")
    public ResponseEntity<SessionResponseDTO> removeUser(
            @PathVariable UUID sessionId,
            @PathVariable UUID userToRemove,
            @RequestHeader("X-User-ID") UUID ownerId) {
        
        SessionResponseDTO response = sessionService.removeUser(sessionId, userToRemove, ownerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}/participants")
    @Operation(summary = "Get session participants", description = "List all active participants (owner/participants only)")
    @ApiResponse(responseCode = "200", description = "Participants list retrieved successfully")
    public ResponseEntity<List<UUID>> getSessionParticipants(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-ID") UUID requesterId) {
        
        List<UUID> participants = sessionService.getSessionParticipants(sessionId, requesterId);
        return ResponseEntity.ok(participants);
    }

    // ==================== Pomodoro Phase Management ====================

    @PostMapping("/{sessionId}/phases/work/start")
    @Operation(summary = "Start work phase", description = "Begin work phase of pomodoro cycle (owner only)")
    @ApiResponse(responseCode = "200", description = "Work phase started successfully")
    public ResponseEntity<SessionResponseDTO> startWorkPhase(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        SessionResponseDTO response = sessionService.startWorkPhase(sessionId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/phases/break/start")
    @Operation(summary = "Start break phase", description = "Begin break phase (short or long) (owner only)")
    @ApiResponse(responseCode = "200", description = "Break phase started successfully")
    public ResponseEntity<SessionResponseDTO> startBreakPhase(
            @PathVariable UUID sessionId,
            @RequestParam SessionType breakType,
            @RequestHeader("X-User-ID") UUID userId) {
        
        SessionResponseDTO response = sessionService.startBreakPhase(sessionId, userId, breakType);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/phases/work/complete")
    @Operation(summary = "Complete work phase", description = "Mark work phase as complete and increment counter (owner only)")
    @ApiResponse(responseCode = "200", description = "Work phase completed successfully")
    public ResponseEntity<SessionResponseDTO> completeWorkPhase(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        SessionResponseDTO response = sessionService.completeWorkPhase(sessionId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/phases/break/skip")
    @Operation(summary = "Skip break", description = "Skip break phase and return to work (owner only)")
    @ApiResponse(responseCode = "200", description = "Break phase skipped successfully")
    public ResponseEntity<SessionResponseDTO> skipBreak(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        SessionResponseDTO response = sessionService.skipBreak(sessionId, userId);
        return ResponseEntity.ok(response);
    }

    // ==================== Progress and Status ====================

    @GetMapping("/{sessionId}/progress")
    @Operation(summary = "Get session progress", description = "Real-time session progress and metrics (owner/participants only)")
    @ApiResponse(responseCode = "200", description = "Progress retrieved successfully")
    public ResponseEntity<SessionProgressDTO> getSessionProgress(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        SessionProgressDTO progress = sessionService.getSessionProgress(sessionId, userId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/{sessionId}/break-options")
    @Operation(summary = "Get break options", description = "Available break options and recommendations (owner/participants only)")
    @ApiResponse(responseCode = "200", description = "Break options retrieved successfully")
    public ResponseEntity<BreakSessionDTO> getBreakOptions(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        BreakSessionDTO breakOptions = sessionService.getBreakOptions(sessionId, userId);
        return ResponseEntity.ok(breakOptions);
    }

    // ==================== Task Management ====================

    @PostMapping("/{sessionId}/tasks/{taskId}")
    @Operation(summary = "Add task to session", description = "Associate task with session (owner only)")
    @ApiResponse(responseCode = "200", description = "Task added to session successfully")
    public ResponseEntity<SessionResponseDTO> addTaskToSession(
            @PathVariable UUID sessionId,
            @PathVariable UUID taskId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        SessionResponseDTO response = sessionService.addTaskToSession(sessionId, taskId, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{sessionId}/tasks/{taskId}")
    @Operation(summary = "Remove task from session", description = "Remove task association from session (owner only)")
    @ApiResponse(responseCode = "200", description = "Task removed from session successfully")
    public ResponseEntity<SessionResponseDTO> removeTaskFromSession(
            @PathVariable UUID sessionId,
            @PathVariable UUID taskId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        SessionResponseDTO response = sessionService.removeTaskFromSession(sessionId, taskId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/tasks/{taskId}/complete")
    @Operation(summary = "Mark task completed", description = "Mark task as completed (owner/participants)")
    @ApiResponse(responseCode = "200", description = "Task marked as completed successfully")
    public ResponseEntity<SessionResponseDTO> markTaskCompleted(
            @PathVariable UUID sessionId,
            @PathVariable UUID taskId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        SessionResponseDTO response = sessionService.markTaskCompleted(sessionId, taskId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}/tasks")
    @Operation(summary = "Get session tasks", description = "List all tasks associated with session (owner/participants)")
    @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    public ResponseEntity<List<UUID>> getSessionTasks(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        List<UUID> tasks = sessionService.getSessionTasks(sessionId, userId);
        return ResponseEntity.ok(tasks);
    }

    // ==================== Utility Methods ====================

    @GetMapping("/{sessionId}/ownership")
    @Operation(summary = "Check ownership", description = "Check if user is session owner")
    @ApiResponse(responseCode = "200", description = "Ownership status returned")
    public ResponseEntity<Boolean> isUserSessionOwner(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-ID") UUID userId) {
        
        boolean isOwner = sessionService.isUserSessionOwner(sessionId, userId);
        return ResponseEntity.ok(isOwner);
    }

    @GetMapping("/{sessionId}/can-join")
    @Operation(summary = "Check join eligibility", description = "Check if user can join session with invite code")
    @ApiResponse(responseCode = "200", description = "Join eligibility status returned")
    public ResponseEntity<Boolean> canUserJoinSession(
            @PathVariable UUID sessionId,
            @RequestParam String inviteCode,
            @RequestHeader("X-User-ID") UUID userId) {
        
        boolean canJoin = sessionService.canUserJoinSession(sessionId, userId, inviteCode);
        return ResponseEntity.ok(canJoin);
    }
}
