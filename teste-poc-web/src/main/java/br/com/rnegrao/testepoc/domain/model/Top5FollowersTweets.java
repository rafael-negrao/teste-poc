package br.com.rnegrao.testepoc.domain.model;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Map;

@Table("top5followerstweets")
public class Top5FollowersTweets {

    @PrimaryKeyColumn(name = "id", type = PrimaryKeyType.PARTITIONED)
    private String id;

    @Column("users")
    private Map<Integer, String> users;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<Integer, String> getUsers() {
        return users;
    }

    public void setUsers(Map<Integer, String> users) {
        this.users = users;
    }
}
