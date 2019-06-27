package br.com.rnegrao.testepoc.domain.model;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("counttweets")
public class CountTweets {

    @PrimaryKeyColumn(name = "createdat", type = PrimaryKeyType.PARTITIONED)
    private String createdAt;

    @Column("counttweets")
    private Integer countTweets;

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getCountTweets() {
        return countTweets;
    }

    public void setCountTweets(Integer countTweets) {
        this.countTweets = countTweets;
    }
}
