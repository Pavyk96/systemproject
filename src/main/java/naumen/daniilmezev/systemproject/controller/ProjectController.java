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
import naumen.daniilmezev.systemproject.dto.ProjectRequest;
import naumen.daniilmezev.systemproject.service.ProjectService;
import naumen.daniilmezev.systemproject.service.UserService;
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
@RequestMapping("/projects")
@Tag(name = "Projects", description = "Project listing, forms, and membership management.")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Open projects page", description = "Returns the projects page with projects visible to the current user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projects page rendered", content = @Content(mediaType = "text/html")),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public String projects(@Parameter(hidden = true) Authentication authentication, @Parameter(hidden = true) Model model) {
        boolean admin = isAdmin(authentication);
        model.addAttribute("isAdmin", admin);
        model.addAttribute("projects", projectService.findProjectsVisibleTo(authentication.getName(), admin));
        if (admin) {
            model.addAttribute("allUsers", userService.findAllOrdered());
        }
        return "projects";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    @Operation(summary = "Open create project form", description = "Returns the HTML form for creating a new project. Available to administrators.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project form rendered", content = @Content(mediaType = "text/html")),
            @ApiResponse(responseCode = "403", description = "Administrator role required")
    })
    public String createForm(@Parameter(hidden = true) Model model) {
        model.addAttribute("projectRequest", new ProjectRequest());
        model.addAttribute("formTitle", "Create project");
        model.addAttribute("submitLabel", "Create");
        model.addAttribute("formAction", "/projects");
        return "project-form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(
            summary = "Create project",
            description = "Creates a new project from a form submission. Available to administrators.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/x-www-form-urlencoded",
                            schema = @Schema(implementation = ProjectRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Project created, redirected to projects page"),
            @ApiResponse(responseCode = "200", description = "Validation error, project form rendered again", content = @Content(mediaType = "text/html")),
            @ApiResponse(responseCode = "403", description = "Administrator role required")
    })
    public String createProject(
            @Valid @ModelAttribute("projectRequest") ProjectRequest projectRequest,
            @Parameter(hidden = true) BindingResult bindingResult,
            @Parameter(hidden = true) Model model
    ) {
        if (bindingResult.hasErrors()) {
            fillForm(model, "Create project", "Create", "/projects");
            return "project-form";
        }

        projectService.createProject(projectRequest);
        return "redirect:/projects";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    @Operation(summary = "Open edit project form", description = "Returns the HTML form for editing an existing project. Available to administrators.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project edit form rendered", content = @Content(mediaType = "text/html")),
            @ApiResponse(responseCode = "403", description = "Administrator role required")
    })
    public String editForm(@Parameter(description = "Project identifier.") @PathVariable Long id, @Parameter(hidden = true) Model model) {
        model.addAttribute("projectRequest", projectService.getProjectRequest(id));
        fillForm(model, "Edit project", "Save", "/projects/" + id);
        return "project-form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}")
    @Operation(
            summary = "Update project",
            description = "Updates an existing project from a form submission. Available to administrators.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/x-www-form-urlencoded",
                            schema = @Schema(implementation = ProjectRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Project updated, redirected to projects page"),
            @ApiResponse(responseCode = "200", description = "Validation error, project form rendered again", content = @Content(mediaType = "text/html")),
            @ApiResponse(responseCode = "403", description = "Administrator role required")
    })
    public String updateProject(
            @Parameter(description = "Project identifier.") @PathVariable Long id,
            @Valid @ModelAttribute("projectRequest") ProjectRequest projectRequest,
            @Parameter(hidden = true) BindingResult bindingResult,
            @Parameter(hidden = true) Model model
    ) {
        if (bindingResult.hasErrors()) {
            fillForm(model, "Edit project", "Save", "/projects/" + id);
            return "project-form";
        }

        projectService.updateProject(id, projectRequest);
        return "redirect:/projects";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    @Operation(summary = "Delete project", description = "Deletes the selected project. Available to administrators.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Project deleted, redirected to projects page"),
            @ApiResponse(responseCode = "403", description = "Administrator role required")
    })
    public String deleteProject(@Parameter(description = "Project identifier.") @PathVariable Long id) {
        projectService.deleteProject(id);
        return "redirect:/projects";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/members")
    @Operation(summary = "Add member to project", description = "Adds a user to the selected project. Available to administrators.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Member added, redirected to projects page"),
            @ApiResponse(responseCode = "403", description = "Administrator role required")
    })
    public String addMember(
            @Parameter(description = "Project identifier.") @PathVariable Long id,
            @Parameter(description = "User identifier to add to the project.") @RequestParam Long userId
    ) {
        projectService.addMember(id, userId);
        return "redirect:/projects";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{projectId}/members/{userId}/remove")
    @Operation(summary = "Remove member from project", description = "Removes a user from the selected project. Available to administrators.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Member removed, redirected to projects page"),
            @ApiResponse(responseCode = "403", description = "Administrator role required")
    })
    public String removeMember(
            @Parameter(description = "Project identifier.") @PathVariable Long projectId,
            @Parameter(description = "User identifier to remove from the project.") @PathVariable Long userId
    ) {
        projectService.removeMember(projectId, userId);
        return "redirect:/projects";
    }

    private void fillForm(Model model, String formTitle, String submitLabel, String formAction) {
        model.addAttribute("formTitle", formTitle);
        model.addAttribute("submitLabel", submitLabel);
        model.addAttribute("formAction", formAction);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
