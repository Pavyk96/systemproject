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
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

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
    void adminCanCreateProject() throws Exception {
        mockMvc.perform(post("/projects")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("name", "Internal CRM")
                        .param("description", "Migration of internal CRM workflows"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));

        assertThat(projectRepository.findAllByOrderByIdAsc())
                .extracting(Project::getName)
                .containsExactly("Internal CRM");
    }

    @Test
    void userCannotCreateProject() throws Exception {
        mockMvc.perform(post("/projects")
                        .with(user("employee").roles("USER"))
                        .with(csrf())
                        .param("name", "Forbidden Project")
                        .param("description", "This request should be blocked"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAddMemberToProject() throws Exception {
        User member = createUser("alice", "secret123", Role.USER);
        Project project = new Project();
        project.setName("Portal");
        project.setDescription("Customer self-service portal");
        project = projectRepository.save(project);

        mockMvc.perform(post("/projects/{id}/members", project.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("userId", member.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));

        Project reloaded = projectRepository.findWithMembersById(project.getId()).orElseThrow();
        assertThat(reloaded.getMembers())
                .extracting(User::getUsername)
                .containsExactly("alice");
    }

    @Test
    void userSeesOnlyAssignedProjects() throws Exception {
        User member = createUser("member", "secret123", Role.USER);

        Project visible = new Project();
        visible.setName("Visible Project");
        visible.setDescription("Should be visible");
        visible.getMembers().add(member);
        member.getProjects().add(visible);

        Project hidden = new Project();
        hidden.setName("Hidden Project");
        hidden.setDescription("Should stay hidden");

        projectRepository.save(visible);
        projectRepository.save(hidden);
        userRepository.save(member);

        mockMvc.perform(get("/projects")
                        .with(user("member").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Visible Project")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Hidden Project"))));
    }

    @Test
    void removingMemberUnassignsProjectTasks() throws Exception {
        User member = createUser("alice", "secret123", Role.USER);
        Project project = new Project();
        project.setName("Operations");
        project.setDescription("Operations workspace");
        project.getMembers().add(member);
        member.getProjects().add(project);
        project = projectRepository.save(project);
        userRepository.save(member);

        Task task = new Task();
        task.setName("Check pipeline");
        task.setDescription("Check nightly pipeline");
        task.setProject(project);
        task.setAssignee(member);
        task.setStatus(TaskStatus.NEW);
        task = taskRepository.save(task);

        mockMvc.perform(post("/projects/{projectId}/members/{userId}/remove", project.getId(), member.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));

        Task updated = taskRepository.findWithProjectAndAssigneeById(task.getId()).orElseThrow();
        assertThat(updated.getAssignee()).isNull();
    }

    @Test
    void adminCanDeleteProjectWithMembers() throws Exception {
        User member = createUser("alice", "secret123", Role.USER);
        Project project = new Project();
        project.setName("Delete Me");
        project.setDescription("Project with members");
        project.getMembers().add(member);
        member.getProjects().add(project);
        project = projectRepository.save(project);
        userRepository.save(member);

        mockMvc.perform(post("/projects/{id}/delete", project.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));

        assertThat(projectRepository.findById(project.getId())).isEmpty();
        assertThat(projectRepository.findDistinctByMembersUsernameOrderByIdAsc("alice")).isEmpty();
    }

    private User createUser(String username, String rawPassword, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userRepository.save(user);
    }
}
