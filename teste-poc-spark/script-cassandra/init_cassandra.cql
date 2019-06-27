create keyspace testepoc with durable_writes = true
	and replication = {'class': 'org.apache.cassandra.locator.SimpleStrategy', 'replication_factor': '1'};

create table counthashtags
(
    hashtag text primary key,
    counttweets int
)
with caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    and compaction = {'max_threshold': '32', 'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    and compression = {'class': 'org.apache.cassandra.io.compress.LZ4Compressor', 'chunk_length_in_kb': '64'}
    and dclocal_read_repair_chance = 0.1;

create table counttweets
(
    createdat text primary key,
    counttweets int
)
with caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    and compaction = {'max_threshold': '32', 'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    and compression = {'class': 'org.apache.cassandra.io.compress.LZ4Compressor', 'chunk_length_in_kb': '64'}
    and dclocal_read_repair_chance = 0.1;

create table top5followerstweets
(
    id text primary key,
    users map<int, text>
)
    with caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
        and compaction = {'max_threshold': '32', 'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
        and compression = {'class': 'org.apache.cassandra.io.compress.LZ4Compressor', 'chunk_length_in_kb': '64'}
        and dclocal_read_repair_chance = 0.1;
