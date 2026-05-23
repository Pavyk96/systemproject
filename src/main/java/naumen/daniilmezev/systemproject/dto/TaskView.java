package naumen.daniilmezev.systemproject.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import naumen.daniilmezev.systemproject.entity.TaskStatus;

@Schema(name = "TaskView", description = "Task data rendered on application pages.")
public class TaskView {

    @Schema(description = "Task identifier.", example = "10")
    private final Long id;
    @Schema(description = "Task title.", example = "Prepare sprint report")
    private final String name;
    @Schema(description = "Task description.", example = "Collect metrics and prepare the sprint status report.")
    private final String description;
    @Schema(description = "Current task status.")
    private final TaskStatus status;
    @Schema(description = "Identifier of the parent project.", example = "1")
    private final Long projectId;
    @Schema(description = "Name of the parent project.", example = "CRM Migration")
    private final String projectName;
    @Schema(description = "Identifier of the assignee.", example = "2", nullable = true)
    private final Long assigneeId;
    @Schema(description = "Username of the assignee.", example = "manager", nullable = true)
    private final String assigneeUsername;

    public TaskView(
            Long id,
            String name,
            String description,
            TaskStatus status,
            Long projectId,
            String projectName,
            Long assigneeId,
            String assigneeUsername
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.projectId = projectId;
        this.projectName = projectName;
        this.assigneeId = assigneeId;
        this.assigneeUsername = assigneeUsername;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public String getAssigneeUsername() {
        return assigneeUsername;
    }
}
