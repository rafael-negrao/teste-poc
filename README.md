# Intruções

Pré requisitos:
- Java 8
- Scala 2.11.12
- Maven 3.5.2 ou superior

## Spark

- Arquivos de configuração de acesso ao banco de dados:

`./teste-poc-spark/src/main/resources/cassandra.properties`

- Arquivos de configuração de acesso ao banco de dados:

`/teste-poc-spark/src/main/resources/twitter4j.properties`

- Como configurar o tempo de captura de dados:

`br.com.rnegrao.testepoc.ConsumerTwitterStream`

```scala
  def coletarDados() = {

    // definindo o tempo de duracao de captura de dados do tweets
    
    val windowDuration: Duration = Minutes(1)

    val slideDuration: Duration = Seconds(15)

    // tempo que tarefa ficará viva
    val timeoutDuration = Minutes(5)

    val ssc = new StreamingContext(sc, Seconds(1))
```

- Para empacotar a aplicação:

`cd ./teste-poc-spark`

`mvn clean install -Pprepare-deploy`

- Para rodar a aplicação:

Substituir a palavra abaixo **TRACK** pelo termo que se deseja pesquisar no twitter 

- USUARIOS_COM_MAIS_SEGUIDORES

`$SPARK_HOME/bin/spark-submit --class br.com.rnegrao.testepoc.ConsumerTwitterStream --master local target/teste-poc-spark-1.0.0.0.jar TRACK USUARIOS_COM_MAIS_SEGUIDORES`

- HASHTAGS_MAIS_POPULARES

`$SPARK_HOME/bin/spark-submit --class br.com.rnegrao.testepoc.ConsumerTwitterStream --master local target/teste-poc-spark-1.0.0.0.jar TRACK USUARIOS_COM_MAIS_SEGUIDORES`

- HASHTAGS_POR_HORA

`$SPARK_HOME/bin/spark-submit --class br.com.rnegrao.testepoc.ConsumerTwitterStream --master local target/teste-poc-spark-1.0.0.0.jar TRACK HASHTAGS_POR_HORA`


## Cassandra

Script para criação de tabelas:

`./teste-poc-spark/script-cassandra/init_cassandra.cql`

## API Rest: Acesso a dados ao Cassandra

- Configuração de acesso ao banco de dados

`./teste-poc-web/src/main/resources/application.yml`
```yaml
cassandra:
  contactpoints: 127.0.0.1
  port: 9042
  keyspace: testepoc
```

- Para empacotar a aplicação:

`cd ./teste-poc-web`

`mvn clean install`

- Como rodar a aplicação:

`java -jar target/testepoc-api-1.0.0.jar`

- Documentação swagger da API, para o profile **local**:

`http://localhost:8080/swagger-ui.html`

## API BOT

- Configurações de Integração **API BOT** vs **API Rest: Acesso a dados ao Cassandra**

`./teste-poc-bot/src/main/resources/application.yml`
```yaml
tweets:
  url: http://localhost:8080/
```

- Configurações de Token Bot Telegram
`./teste-poc-bot/src/main/resources/application.yml`
```yaml
telegram:
  token: asdfasdfasfasdd
```

- Para empacotar a aplicação:

`cd ./teste-poc-bot`

`mvn clean install`

- Como rodar a aplicação:

`java -jar target/testepoc-api-1.0.0.jar`
