package naumen.daniilmezev.systemproject.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import naumen.daniilmezev.systemproject.dto.TaskRequest;
import naumen.daniilmezev.systemproject.entity.TaskStatus;
import naumen.daniilmezev.systemproject.service.ProjectService;
import naumen.daniilmezev.systemproject.service.TaskService;
import naumen.daniilmezev.systemproject.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/tasks")
@Tag(name = "Tasks", description = "Task listing, forms, and status updates.")
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final UserService userService;

    public TaskController(TaskService taskService, ProjectService projectService, UserService userService) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Open tasks page", description = "Returns the tasks page with tasks visible to the current user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks page rendered", content = @Content(mediaType = "text/html")),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public String tasks(@Parameter(hidden = true) Authentication authentication, @Parameter(hidden = true) Model model) {
        boolean admin = isAdmin(authentication);
        model.addAttribute("isAdmin", admin);
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("tasks", taskService.findTasksVisibleTo(authentication.getName(), admin));
        return "tasks";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    @Operation(summary = "Open create task form", description = "Returns the HTML form for creating a new task. Available to administrators.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task form rendered", content = @Content(mediaType = "text/html")),
            @ApiResponse(responseCode = "403", description = "Administrator role required")
    })
    public String createForm(@Parameter(hidden = true) Model model) {
        model.addAttribute("taskRequest", new TaskRequest());
        fillForm(model, "Create task", "Create", "/tasks");
        return "task-form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(
            summary = "Create task",
            description = "Creates a new task from a form submission. Available to administrators.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/x-www-form-urlencoded",
                            schema = @Schema(implementation = TaskRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Task created, redirected to tasks page"),
            @ApiResponse(responseCode = "200", description = "Validation or business error, task form rendered again", content = @Content(mediaType = "text/html")),
            @ApiResponse(responseCode = "403", description = "Administrator role required")
    })
    public String createTask(
            @Valid @ModelAttribute("taskRequest") TaskRequest taskRequest,
            @Parameter(hidden = true) BindingResult bindingResult,
            @Parameter(hidden = true) Model model
    ) {
        if (bindingResult.hasErrors()) {
            fillForm(model, "Create task", "Create", "/tasks");
            return "task-form";
        }

        try {
            taskService.createTask(taskRequest);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("formError", exception.getMessage());
            fillForm(model, "Create task", "Create", "/tasks");
            return "task-form";
        }
        return "redirect:/tasks";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    @Operation(summary = "Open edit task form", description = "Returns the HTML form for editing an existing task. Available to administrators.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task edit form rendered", content = @Content(mediaType = "text/html")),
            @ApiResponse(responseCode = "403", description = "Administrator role required")
    })
    public String editForm(@Parameter(description = "Task identifier.") @PathVariable Long id, @Parameter(hidden = true) Model model) {
        model.addAttribute("taskRequest", taskService.getTaskRequest(id));
        fillForm(model, "Edit task", "Save", "/tasks/" + id);
        return "task-form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}")
    @Operation(
            summary = "Update task",
            description = "Updates an existing task from a form submission. Available to administrators.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/x-www-form-urlencoded",
                            schema = @Schema(implementation = TaskRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Task updated, redirected to tasks page"),
            @ApiResponse(responseCode = "200", description = "Validation or business error, task form rendered again", content = @Content(mediaType = "text/html")),
            @ApiResponse(responseCode = "403", description = "Administrator role required")
    })
    public String updateTask(
            @Parameter(description = "Task identifier.") @PathVariable Long id,
            @Valid @ModelAttribute("taskRequest") TaskRequest taskRequest,
            @Parameter(hidden = true) BindingResult bindingResult,
            @Parameter(hidden = true) Model model
    ) {
        if (bindingResult.hasErrors()) {
            fillForm(model, "Edit task", "Save", "/tasks/" + id);
            return "task-form";
        }

        try {
            taskService.updateTask(id, taskRequest);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("formError", exception.getMessage());
            fillForm(model, "Edit task", "Save", "/tasks/" + id);
            return "task-form";
        }
        return "redirect:/tasks";
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Update task status", description = "Changes task status for an administrator or eligible assignee.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Status updated, redirected to tasks page"),
            @ApiResponse(responseCode = "403", description = "Task is not accessible for status update"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public String updateStatus(
            @Parameter(description = "Task identifier.") @PathVariable Long id,
            @Parameter(description = "New task status.") @RequestParam TaskStatus status,
            @Parameter(hidden = true) Authentication authentication
    ) {
        boolean admin = isAdmin(authentication);
        try {
            taskService.updateStatus(id, status, authentication.getName(), admin);
        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException(exception.getMessage());
        }
        return "redirect:/tasks";
    }

    private void fillForm(Model model, String formTitle, String submitLabel, String formAction) {
        model.addAttribute("formTitle", formTitle);
        model.addAttribute("submitLabel", submitLabel);
        model.addAttribute("formAction", formAction);
        model.addAttribute("projects", projectService.findProjectChoices());
        model.addAttribute("users", userService.findAllOrdered());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
