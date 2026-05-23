package naumen.daniilmezev.systemproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RegistrationRequest", description = "Registration form payload for creating a new user account.")
public class RegistrationRequest {

    @NotBlank(message = "Login is required")
    @Size(min = 3, max = 100, message = "Login must be between 3 and 100 characters")
    @Schema(description = "Unique username used for login.", example = "admin", minLength = 3, maxLength = 100)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Schema(description = "User password.", example = "admin123", minLength = 6, maxLength = 100)
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
