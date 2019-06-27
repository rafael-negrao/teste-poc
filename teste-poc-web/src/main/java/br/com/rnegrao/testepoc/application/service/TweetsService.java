package br.com.rnegrao.testepoc.application.service;

import br.com.rnegrao.testepoc.domain.model.CountHashtags;
import br.com.rnegrao.testepoc.domain.model.CountTweets;
import br.com.rnegrao.testepoc.domain.model.Top5FollowersTweets;

import java.util.List;

public interface TweetsService {
    List<CountHashtags> buscarContagemHashtags();

    List<CountTweets> buscarContagemTweetsPorHora();

    List<Top5FollowersTweets> buscarTop5Usuarios();
}
