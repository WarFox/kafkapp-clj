(ns kafkapp.producer
  (:import
   [java.util Properties]
   [org.apache.kafka.clients.producer KafkaProducer Callback ProducerRecord RecordMetadata])
  (:require
   [clojure.java.io :as jio]
   [clojure.string :as str]
   [integrant.core :as ig]
   [kafkapp.utils :as utils]))

(def default-producer-config
  {:avro
   {"key.serializer"   org.apache.kafka.common.serialization.StringSerializer
    "value.serializer" io.confluent.kafka.serializers.KafkaAvroSerializer}

   :json
   {"key.serializer"   org.apache.kafka.common.serialization.StringSerializer
    "value.serializer" io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer}

   :transactional
   {"acks"                                  "all"
    "enable.idempotence"                    "true"
    "key.serializer"                        org.apache.kafka.common.serialization.StringSerializer
    "value.serializer"                      io.confluent.kafka.serializers.KafkaAvroSerializer
    "max.in.flight.requests.per.connection" "1"}})

(defn instance
  "creates an instance with config"
  [config]
  (let [props (with-open [cluster-config (jio/reader "resources/cluster.config")]
                (doto (Properties.)
                  (.putAll config)
                  (.load cluster-config)))]
    (KafkaProducer. props)))

(defn- send-callback
  "This is invoked after receiving ack back from broker"
  [^ProducerRecord record ^RecordMetadata metadata exception]
  (if exception
    (.printStackTrace exception)
    (utils/print! {:topic (.topic metadata)
                   :partition (.partition metadata)
                   :offset (.offset metadata)
                   :key (.key record)
                   :value (.value record)})))

(defn send-record!
  "Calls send() method and immediately returns. send() is an asynchronous method
  that returns a Future<RecordMetadata>, send() buffers the messages. Callback
  is called when the broker acknowledges the message in a separate thread."
  [producer record]
  (.send producer record
         (reify Callback
           (onCompletion [_ metadata exception]
             (send-callback record metadata exception)))))

(defn ->producer-record
  ([topic kv]
   (ProducerRecord. topic (:key kv) (:value kv)))
  ([topic schema kv]
   (let [key   (:key kv)
         value (utils/map->avro-record schema (:value kv))]
     (ProducerRecord. topic key value))))

(defn send-avro-messages!
  [producer schema topic kvs]
  (try
    (doseq [kv kvs]
      (send-record! producer
                    (->producer-record topic schema kv)))
    (catch Exception e
      (println "Handle generic exception")
      (.printStackTrace e))
    (finally
      (doto producer
        (.flush)
        (.close)))))

(defn send-json-messages!
  [producer topic kvs]
  (try
    (doseq [kv kvs]
      (send-record! producer
                    (->producer-record topic kv)))
    (catch Exception e
      (println "Handle generic exception")
      (.printStackTrace e))
    (finally
      (doto producer
        (.flush)
        (.close)))))

(defn send-transactional-messages!
  [producer schema topic kvs]
  (try
    (.initTransactions producer)
    (.beginTransaction producer)
    (doseq [kv kvs]
      (send-record! producer
                    (->producer-record topic schema kv)))
    (.commitTransaction producer) ;; commitTransaction will call the flush() operation
    (catch Exception e
      (println "Error occurred during transaction: aborting transaction")
      (.abortTransaction producer) ;; aborting a transaction will also add a transaction marker
      (.printStackTrace e))
    (finally
      (.close producer))))

(defmethod ig/init-key :kafkapp.producer/transactional-id
  [_ [classification topic]]
  (str/join "-" [(name classification) "producer" topic]))

(defmethod ig/init-key :kafkapp.producer/properties
  [_ [classification transactional-id]]
  (cond-> default-producer-config
    true (get classification)
    (= classification :transactional)  (merge {"transactional.id" transactional-id})))

(defmethod ig/init-key :kafkapp.producer/schema
  [_ avro-schema]
  (utils/parse-schema avro-schema))

(defmethod ig/init-key :kafkapp.producer/message-kvs
  [_ messages]
  (take 16 ;; increase this number when you are greedy
        (map #(assoc {}
                     :key   (str (:viewtime %))
                     :value %)
             (cycle messages))))

(defmethod ig/init-key :kafkapp.producer/instance
  [_ properties]
  (instance properties))

(defmethod ig/init-key :kafkapp.producer/run!
  [_ [classification producer schema topic kvs]]
  (case classification
    :avro
    (send-avro-messages! producer schema topic kvs)

    :json
    (send-json-messages! producer topic kvs)

    :transactional
    (send-transactional-messages! producer schema topic kvs)))
