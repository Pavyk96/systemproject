package naumen.daniilmezev.systemproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import naumen.daniilmezev.systemproject.service.ProjectService;
import naumen.daniilmezev.systemproject.service.TaskService;
import naumen.daniilmezev.systemproject.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Tag(name = "Home", description = "Dashboard and overview page.")
public class HomeController {

    private final UserService userService;
    private final ProjectService projectService;
    private final TaskService taskService;

    public HomeController(UserService userService, ProjectService projectService, TaskService taskService) {
        this.userService = userService;
        this.projectService = projectService;
        this.taskService = taskService;
    }

    @GetMapping("/")
    @Operation(summary = "Open dashboard", description = "Returns the main dashboard page with user, project, and task summaries.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard page rendered", content = @Content(mediaType = "text/html")),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public String home(@Parameter(hidden = true) Authentication authentication, @Parameter(hidden = true) Model model) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        model.addAttribute("username", authentication.getName());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("users", userService.findAllOrdered());
        model.addAttribute("projects", projectService.findProjectsVisibleTo(authentication.getName(), isAdmin));
        model.addAttribute("tasks", taskService.findTasksVisibleTo(authentication.getName(), isAdmin));
        model.addAttribute("userCount", userService.countUsers());
        model.addAttribute("projectCount", projectService.countAllProjects());
        model.addAttribute("taskCount", taskService.countAllTasks());
        return "home";
    }
}
