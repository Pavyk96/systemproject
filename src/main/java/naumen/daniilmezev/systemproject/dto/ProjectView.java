package naumen.daniilmezev.systemproject.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "ProjectView", description = "Project data rendered on application pages.")
public class ProjectView {

    @Schema(description = "Project identifier.", example = "1")
    private final Long id;
    @Schema(description = "Project name.", example = "CRM Migration")
    private final String name;
    @Schema(description = "Project description.", example = "Migration of the internal CRM to the new platform.")
    private final String description;
    @ArraySchema(schema = @Schema(implementation = MemberView.class))
    private final List<MemberView> members;

    public ProjectView(Long id, String name, String description, List<MemberView> members) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.members = members;
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

    public List<MemberView> getMembers() {
        return members;
    }

    @Schema(name = "ProjectMemberView", description = "Project member displayed in the project page.")
    public static class MemberView {

        @Schema(description = "User identifier.", example = "2")
        private final Long id;
        @Schema(description = "Username.", example = "manager")
        private final String username;

        public MemberView(Long id, String username) {
            this.id = id;
            this.username = username;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }
    }
}
