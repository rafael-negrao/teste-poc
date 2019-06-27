package br.com.rnegrao.testepoc.application.service.impl;

import br.com.rnegrao.testepoc.application.exception.SemResultadoException;
import br.com.rnegrao.testepoc.application.service.TweetsService;
import br.com.rnegrao.testepoc.domain.model.CountHashtags;
import br.com.rnegrao.testepoc.domain.model.CountTweets;
import br.com.rnegrao.testepoc.domain.model.Top5FollowersTweets;
import br.com.rnegrao.testepoc.repository.CountHashtagsRepository;
import br.com.rnegrao.testepoc.repository.CountTweetsRepository;
import br.com.rnegrao.testepoc.repository.Top5FollowersTweetsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TweetsServiceImpl implements TweetsService {

    @Autowired
    private CountHashtagsRepository countHashtagsRepository;

    @Autowired
    private CountTweetsRepository countTweetsRepository;

    @Autowired
    private Top5FollowersTweetsRepository top5FollowersTweetsRepository;

    @Override
    public List<CountHashtags> buscarContagemHashtags() {
        final List<CountHashtags> list = countHashtagsRepository.findAll();
        if (list.isEmpty()) {
            throw new SemResultadoException();
        }
        return list;
    }

    @Override
    public List<CountTweets> buscarContagemTweetsPorHora() {
        List<CountTweets> list = countTweetsRepository.findAll();
        if (list.isEmpty()) {
            throw new SemResultadoException();
        }
        return list;
    }

    @Override
    public List<Top5FollowersTweets> buscarTop5Usuarios() {
        List<Top5FollowersTweets> list = top5FollowersTweetsRepository.findAll();
        if (list.isEmpty()) {
            throw new SemResultadoException();
        }
        return list;
    }
}
