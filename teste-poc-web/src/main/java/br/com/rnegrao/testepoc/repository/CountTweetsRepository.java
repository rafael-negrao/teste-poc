package br.com.rnegrao.testepoc.repository;

import br.com.rnegrao.testepoc.domain.model.CountTweets;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountTweetsRepository extends CassandraRepository<CountTweets, String> {
}