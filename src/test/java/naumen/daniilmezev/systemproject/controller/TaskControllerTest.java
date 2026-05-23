package naumen.daniilmezev.systemproject.controller;

import naumen.daniilmezev.systemproject.entity.Project;
import naumen.daniilmezev.systemproject.entity.Role;
import naumen.daniilmezev.systemproject.entity.Task;
import naumen.daniilmezev.systemproject.entity.TaskStatus;
import naumen.daniilmezev.systemproject.entity.User;
import naumen.daniilmezev.systemproject.repository.ProjectRepository;
import naumen.daniilmezev.systemproject.repository.TaskRepository;
import naumen.daniilmezev.systemproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        projectRepository.deleteAll();
        createUser("admin", "admin123", Role.ADMIN);
    }

    @Test
    void adminCanCreateTask() throws Exception {
        Project project = createProject("CRM");
        User assignee = createUser("alice", "secret123", Role.USER);
        addMember(project, assignee);

        mockMvc.perform(post("/tasks")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("name", "Prepare backlog")
                        .param("description", "Create backlog for the next sprint")
                        .param("projectId", project.getId().toString())
                        .param("assigneeId", assignee.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));

        Task task = taskRepository.findAllByOrderByIdAsc().getFirst();
        assertThat(task.getName()).isEqualTo("Prepare backlog");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.NEW);
        assertThat(task.getAssignee().getUsername()).isEqualTo("alice");
    }

    @Test
    void userSeesOnlyOwnTasks() throws Exception {
        Project project = createProject("Portal");
        User alice = createUser("alice", "secret123", Role.USER);
        User bob = createUser("bob", "secret123", Role.USER);
        addMember(project, alice);
        addMember(project, bob);

        createTask("Visible task", project, alice, TaskStatus.NEW);
        createTask("Hidden task", project, bob, TaskStatus.NEW);

        mockMvc.perform(get("/tasks")
                        .with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Visible task")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Hidden task"))));
    }

    @Test
    void userCanUpdateOwnTaskStatus() throws Exception {
        Project project = createProject("Mobile App");
        User alice = createUser("alice", "secret123", Role.USER);
        addMember(project, alice);
        Task task = createTask("Implement login", project, alice, TaskStatus.NEW);

        mockMvc.perform(post("/tasks/{id}/status", task.getId())
                        .with(user("alice").roles("USER"))
                        .with(csrf())
                        .param("status", "DONE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));

        Task updated = taskRepository.findWithProjectAndAssigneeById(task.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void userCannotUpdateForeignTaskStatus() throws Exception {
        Project project = createProject("Analytics");
        User alice = createUser("alice", "secret123", Role.USER);
        User bob = createUser("bob", "secret123", Role.USER);
        addMember(project, alice);
        addMember(project, bob);
        Task task = createTask("Prepare report", project, bob, TaskStatus.NEW);

        mockMvc.perform(post("/tasks/{id}/status", task.getId())
                        .with(user("alice").roles("USER"))
                        .with(csrf())
                        .param("status", "DONE"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCannotAssignTaskToUserOutsideProject() throws Exception {
        Project project = createProject("Backoffice");
        User outsider = createUser("outsider", "secret123", Role.USER);

        mockMvc.perform(post("/tasks")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("name", "Restricted task")
                        .param("description", "Should stay on the form")
                        .param("projectId", project.getId().toString())
                        .param("assigneeId", outsider.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Assignee must be a project member")));

        assertThat(taskRepository.findAll()).isEmpty();
    }

    private User createUser(String username, String rawPassword, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userRepository.save(user);
    }

    private Project createProject(String name) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(name + " description");
        return projectRepository.save(project);
    }

    private Task createTask(String name, Project project, User assignee, TaskStatus status) {
        Task task = new Task();
        task.setName(name);
        task.setDescription(name + " description");
        task.setProject(project);
        task.setAssignee(assignee);
        task.setStatus(status);
        return taskRepository.save(task);
    }

    private void addMember(Project project, User user) {
        project.getMembers().add(user);
        user.getProjects().add(project);
        projectRepository.save(project);
        userRepository.save(user);
    }
}
