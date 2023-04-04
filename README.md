# warfox/kafkapp-clj

This is a collection of Kafka Producer and Consumer example apps written in Clojure.

## Consumer

There are three types of Consumers

- JSON, Avro, Transactional

## Producer

There are three types of Producers

- JSON, Avro, Tranactional

## Installation

Download from https://github.com/warfox/kafkapp-clj

## Confluent Local

Setup `confluent` cli by downloading latest TAR file from confluent webisite.

Set the `$CONFLUENT_HOME` and add `$CONFLUENT_HOME/bin` to you `$PATH`

Start your local confluent services by running

```sh
confluent local services start
```

### Setup topics

Setup the topics using the following

```sh
./bin/setup-topics.sh
```

## Usage

Run the producer:

```sh
 clj -M:run producer -c avro -t pageviews
 clj -M:run producer -c json -t pageviews
 clj -M:run producer -c transactional -t pageviews
```

Run the consumer

```sh
 clj -M:run consumer -c avro -t pageviews
 clj -M:run consumer -c json -t pageviews
 clj -M:run consumer -c transactional -t pageviews
```

## Build

Run the project's tests (they'll fail until you edit them):

```sh
$ clojure -T:build test
```

Run the project's CI pipeline and build an uberjar (this will fail until you edit the tests to pass):

    $ clojure -T:build ci

This will produce an updated `pom.xml` file with synchronized dependencies inside the `META-INF`
directory inside `target/classes` and the uberjar in `target`. You can update the version (and SCM tag)
information in generated `pom.xml` by updating `build.clj`.

If you don't want the `pom.xml` file in your project, you can remove it. The `ci` task will
still generate a minimal `pom.xml` as part of the `uber` task, unless you remove `version`
from `build.clj`.

Run that uberjar:

    $ java -jar target/com.github.warfox/kafkapp-clj-0.1.0-SNAPSHOT.jar

## Options

FIXME: listing of options this app accepts.

## Examples

...

## Refer

Refer below types
- org.apache.avro.generic.GenericRecord
- org.apache.avro.generic.GenericData$Record
- org.apache.avro.Schema
- org.apache.kafka.common.serialization.StringSerializer
- io.confluent.kafka.serializers.KafkaAvroSerializer;

https://docs.confluent.io/platform/current/schema-registry/serdes-develop/serdes-avro.html#avro-serializer

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2023 Deepu Mohan Puthrote

_EPLv1.0 is just the default for projects generated by `deps-new`: you are not_
_required to open source this project, nor are you required to use EPLv1.0!_
_Feel free to remove or change the `LICENSE` file and remove or update this_
_section of the `README.md` file!_

Distributed under the Eclipse Public License version 1.0.
