package naumen.daniilmezev.systemproject.repository;

import naumen.daniilmezev.systemproject.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @EntityGraph(attributePaths = "members")
    List<Project> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "members")
    List<Project> findDistinctByMembersUsernameOrderByIdAsc(String username);

    @EntityGraph(attributePaths = "members")
    Optional<Project> findWithMembersById(Long id);
}
