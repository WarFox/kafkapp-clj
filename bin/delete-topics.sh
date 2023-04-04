#!/usr/bin/env bash -eux

kafka-topics --bootstrap-server localhost:9092 --delete --topic pageviews-single-partition

# default pageviews topic is intended for Avro data
kafka-topics --bootstrap-server localhost:9092 --delete --topic pageviews

# pageviews avro topic with compaction enabled
# it keeps only recent values for the same key
kafka-topics --bootstrap-server localhost:9092 --delete --topic pageviews-compacted

kafka-topics --bootstrap-server localhost:9092 --delete --topic pageviews-json
