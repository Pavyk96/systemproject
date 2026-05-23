package naumen.daniilmezev.systemproject.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI systemProjectOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("System Project API")
                        .version("1.0")
                        .description("Documentation for HTML routes, form submissions, and DTO schemas in the System Project application.")
                        .contact(new Contact().name("System Project"))
                        .license(new License().name("Internal use")));
    }
}
