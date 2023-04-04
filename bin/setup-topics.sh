#!/usr/bin/env bash -eux

# Topic with single partition is useful for debugging
kafka-topics --bootstrap-server localhost:9092 --create --topic pageviews-single-partition

# default pageviews topic is intended for Avro data
kafka-topics --bootstrap-server localhost:9092 --create --topic pageviews \
             --partitions 3

# pageviews avro topic with compaction enabled
# it keeps only recent values for the same key
kafka-topics --bootstrap-server localhost:9092 --create --topic pageviews-compacted \
             --partitions 3 \
             --config cleanup.policy=compact \
             --config delete.retention.ms=100 \
             --config segment.ms=100 \
             --config min.cleanable.dirty.ratio=0.01

kafka-topics --bootstrap-server localhost:9092 --create --topic pageviews-json \
             --partitions 3
