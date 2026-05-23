package naumen.daniilmezev.systemproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TaskRequest {

    @NotBlank(message = "Task name is required")
    @Size(min = 3, max = 150, message = "Task name must be between 3 and 150 characters")
    private String name;

    @NotBlank(message = "Task description is required")
    @Size(min = 5, max = 2000, message = "Task description must be between 5 and 2000 characters")
    private String description;

    @NotNull(message = "Project is required")
    private Long projectId;

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
