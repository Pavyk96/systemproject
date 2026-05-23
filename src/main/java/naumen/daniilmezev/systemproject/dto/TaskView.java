package naumen.daniilmezev.systemproject.dto;

import naumen.daniilmezev.systemproject.entity.TaskStatus;

public class TaskView {

    private final Long id;
    private final String name;
    private final String description;
    private final TaskStatus status;
    private final Long projectId;
    private final String projectName;
    private final Long assigneeId;
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
