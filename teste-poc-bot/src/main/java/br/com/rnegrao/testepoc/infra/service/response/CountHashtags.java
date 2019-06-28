package br.com.rnegrao.testepoc.infra.service.response;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class CountHashtags {

    private String hashtag;

    private Integer countTweets;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("hashtag", hashtag)
                .append("countTweets", countTweets)
                .toString();
    }

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
