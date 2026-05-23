package naumen.daniilmezev.systemproject.controller;

import jakarta.validation.Valid;
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
    public String tasks(Authentication authentication, Model model) {
        boolean admin = isAdmin(authentication);
        model.addAttribute("isAdmin", admin);
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("tasks", taskService.findTasksVisibleTo(authentication.getName(), admin));
        return "tasks";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("taskRequest", new TaskRequest());
        fillForm(model, "Create task", "Create", "/tasks");
        return "task-form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String createTask(
            @Valid @ModelAttribute("taskRequest") TaskRequest taskRequest,
            BindingResult bindingResult,
            Model model
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
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("taskRequest", taskService.getTaskRequest(id));
        fillForm(model, "Edit task", "Save", "/tasks/" + id);
        return "task-form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}")
    public String updateTask(
            @PathVariable Long id,
            @Valid @ModelAttribute("taskRequest") TaskRequest taskRequest,
            BindingResult bindingResult,
            Model model
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
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status,
            Authentication authentication
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
