package br.com.rnegrao.testepoc.repository;

import br.com.rnegrao.testepoc.domain.model.CountHashtags;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountHashtagsRepository extends CassandraRepository<CountHashtags, String> {
}