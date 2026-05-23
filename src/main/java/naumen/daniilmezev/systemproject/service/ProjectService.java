package naumen.daniilmezev.systemproject.service;

import naumen.daniilmezev.systemproject.dto.ProjectRequest;
import naumen.daniilmezev.systemproject.dto.ProjectView;
import naumen.daniilmezev.systemproject.entity.Project;
import naumen.daniilmezev.systemproject.entity.User;
import naumen.daniilmezev.systemproject.repository.ProjectRepository;
import naumen.daniilmezev.systemproject.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskService taskService;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, TaskService taskService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskService = taskService;
    }

    @Transactional(readOnly = true)
    public List<ProjectView> findProjectsVisibleTo(String username, boolean admin) {
        List<Project> projects = admin
                ? projectRepository.findAllByOrderByIdAsc()
                : projectRepository.findDistinctByMembersUsernameOrderByIdAsc(username);
        return projects.stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Project> findProjectChoices() {
        return projectRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public long countAllProjects() {
        return projectRepository.count();
    }

    @Transactional(readOnly = true)
    public ProjectRequest getProjectRequest(Long id) {
        Project project = getProject(id);
        ProjectRequest request = new ProjectRequest();
        request.setName(project.getName());
        request.setDescription(project.getDescription());
        return request;
    }

    @Transactional
    public Project createProject(ProjectRequest request) {
        Project project = new Project();
        applyRequest(project, request);
        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(Long id, ProjectRequest request) {
        Project project = getProject(id);
        applyRequest(project, request);
        return project;
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = getProject(id);
        for (User member : List.copyOf(project.getMembers())) {
            member.getProjects().remove(project);
        }
        project.getMembers().clear();
        projectRepository.delete(project);
    }

    @Transactional
    public void addMember(Long projectId, Long userId) {
        Project project = getProject(projectId);
        User user = getUser(userId);
        project.getMembers().add(user);
        user.getProjects().add(project);
    }

    @Transactional
    public void removeMember(Long projectId, Long userId) {
        Project project = getProject(projectId);
        User user = getUser(userId);
        project.getMembers().remove(user);
        user.getProjects().remove(project);
        taskService.unassignUserFromProjectTasks(projectId, userId);
    }

    private void applyRequest(Project project, ProjectRequest request) {
        project.setName(request.getName().trim());
        project.setDescription(request.getDescription().trim());
    }

    private ProjectView toView(Project project) {
        List<ProjectView.MemberView> members = project.getMembers().stream()
                .sorted(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER))
                .map(member -> new ProjectView.MemberView(member.getId(), member.getUsername()))
                .toList();
        return new ProjectView(project.getId(), project.getName(), project.getDescription(), members);
    }

    private Project getProject(Long id) {
        return projectRepository.findWithMembersById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }
}
