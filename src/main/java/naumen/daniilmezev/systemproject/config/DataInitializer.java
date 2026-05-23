package naumen.daniilmezev.systemproject.config;

import naumen.daniilmezev.systemproject.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final BootstrapAdminProperties bootstrapAdminProperties;

    public DataInitializer(UserService userService, BootstrapAdminProperties bootstrapAdminProperties) {
        this.userService = userService;
        this.bootstrapAdminProperties = bootstrapAdminProperties;
    }

    @Override
    public void run(String... args) {
        userService.createAdminIfMissing(
                bootstrapAdminProperties.getUsername(),
                bootstrapAdminProperties.getPassword()
        );
    }
}
