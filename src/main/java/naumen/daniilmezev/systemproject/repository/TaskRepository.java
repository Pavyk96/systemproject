package naumen.daniilmezev.systemproject.repository;

import naumen.daniilmezev.systemproject.entity.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = {"project", "assignee"})
    List<Task> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = {"project", "assignee"})
    List<Task> findByAssigneeUsernameOrderByIdAsc(String username);

    @EntityGraph(attributePaths = {"project", "assignee"})
    Optional<Task> findWithProjectAndAssigneeById(Long id);

    List<Task> findByProjectIdAndAssigneeId(Long projectId, Long assigneeId);
}
