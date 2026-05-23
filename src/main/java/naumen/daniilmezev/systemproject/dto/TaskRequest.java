package naumen.daniilmezev.systemproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TaskRequest", description = "Form payload for creating or updating a task.")
public class TaskRequest {

    @NotBlank(message = "Task name is required")
    @Size(min = 3, max = 150, message = "Task name must be between 3 and 150 characters")
    @Schema(description = "Short task title.", example = "Prepare sprint report", minLength = 3, maxLength = 150)
    private String name;

    @NotBlank(message = "Task description is required")
    @Size(min = 5, max = 2000, message = "Task description must be between 5 and 2000 characters")
    @Schema(description = "Detailed task description.", example = "Collect metrics and prepare the sprint status report.", minLength = 5, maxLength = 2000)
    private String description;

    @NotNull(message = "Project is required")
    @Schema(description = "Identifier of the project the task belongs to.", example = "1")
    private Long projectId;

    @Schema(description = "Identifier of the assigned user. Can be omitted for an unassigned task.", example = "2", nullable = true)
    private Long assigneeId;

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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }
}
