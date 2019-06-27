package br.com.rnegrao.testepoc.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import javax.annotation.Resource;

@Configuration
@EnableCassandraRepositories(basePackages = "br.com.rnegrao.testepoc.repository")
public class CassandraConfig extends AbstractCassandraConfiguration {

    private static final Log LOGGER = LogFactory.getLog(CassandraConfig.class);

    @Resource
    private CassandraProperties cassandraProperties;

    @Override
    protected String getKeyspaceName() {
        return cassandraProperties.getKeyspace();
    }

    @Override
    @Bean
    public CassandraClusterFactoryBean cluster() {
        final CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
        cluster.setContactPoints(cassandraProperties.getContactpoints());
        cluster.setPort(cassandraProperties.getPort());
        cluster.setJmxReportingEnabled(false);
        return cluster;
    }

    @Override
    @Bean
    public CassandraMappingContext cassandraMapping() throws ClassNotFoundException {
        return new CassandraMappingContext();
    }
}