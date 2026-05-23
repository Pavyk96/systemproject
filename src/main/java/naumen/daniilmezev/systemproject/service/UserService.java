package naumen.daniilmezev.systemproject.service;

import naumen.daniilmezev.systemproject.dto.RegistrationRequest;
import naumen.daniilmezev.systemproject.entity.Role;
import naumen.daniilmezev.systemproject.entity.User;
import naumen.daniilmezev.systemproject.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegistrationRequest request) {
        String normalizedUsername = request.getUsername().trim();

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("User with this login already exists");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    @Transactional
    public User createAdminIfMissing(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User admin = new User();
                    admin.setUsername(username);
                    admin.setPassword(passwordEncoder.encode(rawPassword));
                    admin.setRole(Role.ADMIN);
                    return userRepository.save(admin);
                });
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> findAllOrdered() {
        return userRepository.findAllByOrderByUsernameAsc();
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }
}
