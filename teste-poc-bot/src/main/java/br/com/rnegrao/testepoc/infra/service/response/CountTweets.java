package br.com.rnegrao.testepoc.infra.service.response;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class CountTweets {

    private String createdAt;

    private Integer countTweets;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("createdAt", createdAt)
                .append("countTweets", countTweets)
                .toString();
    }

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
