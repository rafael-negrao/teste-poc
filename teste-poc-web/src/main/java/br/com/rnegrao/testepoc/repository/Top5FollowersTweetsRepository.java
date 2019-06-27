package br.com.rnegrao.testepoc.repository;

import br.com.rnegrao.testepoc.domain.model.Top5FollowersTweets;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Top5FollowersTweetsRepository extends CassandraRepository<Top5FollowersTweets, String> {
}