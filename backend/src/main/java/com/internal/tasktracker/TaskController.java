package com.internal.tasktracker;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/api/tasks")
    public ResponseEntity<?> searchTasks(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        // Validate pagination parameters
        if (page < 1 || pageSize < 1 || pageSize > 100) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "error", "Invalid pagination parameters",
                            "message", "Page must be >= 1 and pageSize must be between 1 and 100"
                    )
            );
        }

        // Normalize search query
        String query = q == null ? "" : q.trim();
        String searchTerm = "%" + query.toLowerCase(Locale.ROOT) + "%";

        // Validate status
        String normalizedStatus = null;

        if (status != null && !status.isBlank()) {
            try {
                normalizedStatus = TaskStatus.valueOf(
                        status.trim().toUpperCase(Locale.ROOT)
                ).name();

            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "error", "Invalid status",
                                "message", "Unknown task status: " + status
                        )
                );
            }
        }

        // Fetch filtered tasks
        List<Task> allResults =
                taskRepository.searchTasks(searchTerm, normalizedStatus);

        // Calculate pagination safely
        long start = ((long) page - 1) * pageSize;
        long end = Math.min(start + pageSize, allResults.size());

        List<Task> pageResults =
                start < allResults.size()
                        ? allResults.subList((int) start, (int) end)
                        : Collections.emptyList();

        // Build response
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("items", pageResults);
        response.put("total", allResults.size());
        response.put("page", page);
        response.put("pageSize", pageSize);

        return ResponseEntity.ok(response);
    }
}