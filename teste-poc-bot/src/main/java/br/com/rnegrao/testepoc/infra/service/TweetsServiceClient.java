package br.com.rnegrao.testepoc.infra.service;

import br.com.rnegrao.testepoc.infra.service.response.CountHashtags;
import br.com.rnegrao.testepoc.infra.service.response.CountTweets;
import br.com.rnegrao.testepoc.infra.service.response.Top5FollowersTweets;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "tweets", url = "${tweets.url}")
public interface TweetsServiceClient {

    @GetMapping(value = "/tweets/hashtags/count")
    List<CountHashtags> buscarContagemHashtags();

    @GetMapping(value = "/tweets/porhora/count")
    List<CountTweets> buscarContagemTweetsPorHora();

    @GetMapping(value = "/tweets/top5")
    List<Top5FollowersTweets> buscarTop5Usuarios();
}
