package br.com.rnegrao.testepoc.domain.model;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("counthashtags")
public class CountHashtags {

    @PrimaryKeyColumn(name = "hashtag", type = PrimaryKeyType.PARTITIONED)
    private String hashtag;

    @Column("counttweets")
    private Integer countTweets;

    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    public Integer getCountTweets() {
        return countTweets;
    }

    public void setCountTweets(Integer countTweets) {
        this.countTweets = countTweets;
    }
}
