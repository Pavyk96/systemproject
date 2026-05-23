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
import naumen.daniilmezev.systemproject.dto.RegistrationRequest;
import naumen.daniilmezev.systemproject.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Tag(name = "Authentication", description = "Login and registration pages.")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    @Operation(summary = "Open login page", description = "Returns the login HTML page for anonymous users.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login page rendered", content = @Content(mediaType = "text/html")),
            @ApiResponse(responseCode = "302", description = "Redirected to home page when already authenticated")
    })
    public String login(@Parameter(hidden = true) Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/register")
    @Operation(summary = "Open registration page", description = "Returns the registration HTML page and prepares the registration form model.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registration page rendered", content = @Content(mediaType = "text/html")),
            @ApiResponse(responseCode = "302", description = "Redirected to home page when already authenticated")
    })
    public String registerForm(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) Model model
    ) {
        if (isAuthenticated(authentication)) {
            return "redirect:/";
        }
        model.addAttribute("registrationRequest", new RegistrationRequest());
        return "register";
    }

    @PostMapping("/register")
    @Operation(
            summary = "Submit registration form",
            description = "Creates a new user account from a form submission.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/x-www-form-urlencoded",
                            schema = @Schema(implementation = RegistrationRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "User created, redirected to login page"),
            @ApiResponse(responseCode = "200", description = "Validation or duplicate username error, registration page rendered again", content = @Content(mediaType = "text/html"))
    })
    public String register(
            @Valid @ModelAttribute("registrationRequest") RegistrationRequest registrationRequest,
            @Parameter(hidden = true) BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(registrationRequest);
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("username", "duplicate", exception.getMessage());
            return "register";
        }

        return "redirect:/login?registered";
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
