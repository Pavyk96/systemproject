package naumen.daniilmezev.systemproject.service;

import naumen.daniilmezev.systemproject.dto.TaskRequest;
import naumen.daniilmezev.systemproject.dto.TaskView;
import naumen.daniilmezev.systemproject.entity.Project;
import naumen.daniilmezev.systemproject.entity.Task;
import naumen.daniilmezev.systemproject.entity.TaskStatus;
import naumen.daniilmezev.systemproject.entity.User;
import naumen.daniilmezev.systemproject.repository.ProjectRepository;
import naumen.daniilmezev.systemproject.repository.TaskRepository;
import naumen.daniilmezev.systemproject.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskView> findTasksVisibleTo(String username, boolean admin) {
        List<Task> tasks = admin
                ? taskRepository.findAllByOrderByIdAsc()
                : taskRepository.findByAssigneeUsernameOrderByIdAsc(username);
        return tasks.stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public long countAllTasks() {
        return taskRepository.count();
    }

    @Transactional(readOnly = true)
    public TaskRequest getTaskRequest(Long id) {
        Task task = getTask(id);
        TaskRequest request = new TaskRequest();
        request.setName(task.getName());
        request.setDescription(task.getDescription());
        request.setProjectId(task.getProject().getId());
        request.setAssigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null);
        return request;
    }

    @Transactional
    public Task createTask(TaskRequest request) {
        Task task = new Task();
        applyRequest(task, request);
        task.setStatus(TaskStatus.NEW);
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Long id, TaskRequest request) {
        Task task = getTask(id);
        applyRequest(task, request);
        return task;
    }

    @Transactional
    public void updateStatus(Long id, TaskStatus status, String username, boolean admin) {
        Task task = getTask(id);
        if (!admin) {
            if (task.getAssignee() == null || !username.equals(task.getAssignee().getUsername())) {
                throw new IllegalArgumentException("You can change only your own tasks");
            }
        }
        task.setStatus(status);
    }

    private void applyRequest(Task task, TaskRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + request.getProjectId()));
        User assignee = request.getAssigneeId() != null
                ? userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getAssigneeId()))
                : null;

        if (assignee != null && project.getMembers().stream().noneMatch(member -> member.getId().equals(assignee.getId()))) {
            throw new IllegalArgumentException("Assignee must be a project member");
        }

        task.setName(request.getName().trim());
        task.setDescription(request.getDescription().trim());
        task.setProject(project);
        task.setAssignee(assignee);
    }

    @Transactional
    public void unassignUserFromProjectTasks(Long projectId, Long userId) {
        List<Task> tasks = taskRepository.findByProjectIdAndAssigneeId(projectId, userId);
        for (Task task : tasks) {
            task.setAssignee(null);
        }
    }

    private TaskView toView(Task task) {
        return new TaskView(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getStatus(),
                task.getProject().getId(),
                task.getProject().getName(),
                task.getAssignee() != null ? task.getAssignee().getId() : null,
                task.getAssignee() != null ? task.getAssignee().getUsername() : null
        );
    }

    private Task getTask(Long id) {
        return taskRepository.findWithProjectAndAssigneeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
    }
}
