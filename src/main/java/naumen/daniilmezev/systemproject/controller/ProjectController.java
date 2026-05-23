package naumen.daniilmezev.systemproject.controller;

import jakarta.validation.Valid;
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
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    @GetMapping
    public String projects(Authentication authentication, Model model) {
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
    public String createForm(Model model) {
        model.addAttribute("projectRequest", new ProjectRequest());
        model.addAttribute("formTitle", "Create project");
        model.addAttribute("submitLabel", "Create");
        model.addAttribute("formAction", "/projects");
        return "project-form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String createProject(
            @Valid @ModelAttribute("projectRequest") ProjectRequest projectRequest,
            BindingResult bindingResult,
            Model model
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
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("projectRequest", projectService.getProjectRequest(id));
        fillForm(model, "Edit project", "Save", "/projects/" + id);
        return "project-form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}")
    public String updateProject(
            @PathVariable Long id,
            @Valid @ModelAttribute("projectRequest") ProjectRequest projectRequest,
            BindingResult bindingResult,
            Model model
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
    public String deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return "redirect:/projects";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/members")
    public String addMember(@PathVariable Long id, @RequestParam Long userId) {
        projectService.addMember(id, userId);
        return "redirect:/projects";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{projectId}/members/{userId}/remove")
    public String removeMember(@PathVariable Long projectId, @PathVariable Long userId) {
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
