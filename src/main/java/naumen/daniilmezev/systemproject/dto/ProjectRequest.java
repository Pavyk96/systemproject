package naumen.daniilmezev.systemproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ProjectRequest", description = "Form payload for creating or updating a project.")
public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(min = 3, max = 150, message = "Project name must be between 3 and 150 characters")
    @Schema(description = "Project name shown in lists and forms.", example = "CRM Migration", minLength = 3, maxLength = 150)
    private String name;

    @NotBlank(message = "Project description is required")
    @Size(min = 5, max = 2000, message = "Project description must be between 5 and 2000 characters")
    @Schema(description = "Detailed business description of the project.", example = "Migration of the internal CRM to the new platform.", minLength = 5, maxLength = 2000)
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
