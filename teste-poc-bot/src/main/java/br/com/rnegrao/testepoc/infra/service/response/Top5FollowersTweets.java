package br.com.rnegrao.testepoc.infra.service.response;


import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;

public class Top5FollowersTweets {

    private String id;

    private Map<Integer, String> users;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("users", users)
                .toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<Integer, String> getUsers() {
        if (isNull(users)) {
            users = new HashMap<>();
        }
        return users;
    }

    public void setUsers(Map<Integer, String> users) {
        this.users = users;
    }
}
