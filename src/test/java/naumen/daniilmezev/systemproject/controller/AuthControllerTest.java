package naumen.daniilmezev.systemproject.controller;

import naumen.daniilmezev.systemproject.entity.Role;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    void registerCreatesUserWithEncodedPassword() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "newuser")
                        .param("password", "secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        User user = userRepository.findByUsername("newuser").orElseThrow();
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(passwordEncoder.matches("secret123", user.getPassword())).isTrue();
    }

    @Test
    void registerRejectsDuplicateUsernameAfterTrim() throws Exception {
        User existing = new User();
        existing.setUsername("alice");
        existing.setPassword(passwordEncoder.encode("secret123"));
        existing.setRole(Role.USER);
        userRepository.save(existing);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", " alice ")
                        .param("password", "secret123"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registrationRequest", "username"));
    }
}
