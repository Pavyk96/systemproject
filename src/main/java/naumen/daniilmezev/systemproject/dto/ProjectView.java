package naumen.daniilmezev.systemproject.dto;

import java.util.List;

public class ProjectView {

    private final Long id;
    private final String name;
    private final String description;
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

    public static class MemberView {

        private final Long id;
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
