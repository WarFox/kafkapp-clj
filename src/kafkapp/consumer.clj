(ns kafkapp.consumer
  (:require
   [clojure.java.io :as jio]
   [kafkapp.utils :as utils]
   [integrant.core :as ig]
   [clojure.string :as str])
  (:import
   [java.time Duration]
   [java.util Properties]
   [org.apache.kafka.clients.consumer KafkaConsumer ConsumerRecords]))

;; https://www.conduktor.io/kafka/complete-kafka-consumer-with-java/

(def ^:private processed-count (atom 0))

(def default-consumer-config
  {;; we reset the offset to earliest for development
   "auto.offset.reset"  "earliest"
   ;; we are not autocommiting offsets because we want to run this many times in development mode
   ;; this can be a problem when rebalancing happens
   "enable.auto.commit" "false"
   "key.deserializer"   org.apache.kafka.common.serialization.StringDeserializer})

(defmethod ig/init-key :kafkapp.consumer/properties
  [_ [classification group-id]]
  (merge default-consumer-config
         (case classification
           :json
           {"group.id"           group-id
            "value.deserializer" io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer}

           :avro
           {"group.id"           group-id
            "value.deserializer" io.confluent.kafka.serializers.KafkaAvroDeserializer}

           :transactional
           {"group.id"           group-id
            "isolation.level"    "read_committed"
            "value.deserializer" io.confluent.kafka.serializers.KafkaAvroDeserializer})))

(defn instance
  "creates an instance with config"
  [config]
  {:pre [(config "group.id")]}
  (let [props (with-open [cluster-config (jio/reader "resources/cluster.config")]
                (doto (Properties.)
                  (.putAll config)
                  (.load cluster-config)))]
    (KafkaConsumer. props)))

(defmethod ig/init-key :kafkapp.consumer/group-id
  [_ [classification topic]]
  (str/join "-" [(name classification) "consumer" topic]))

(defmethod ig/init-key :kafkapp.consumer/instance
  [_ properties]
  (instance properties))

(defn process-record
  [record]
  (utils/print! record))

(defn poll-records
  [consumer]
  (loop [poll-count 0
         ^ConsumerRecords records (.poll consumer (Duration/ofSeconds 1))]
    (doseq [record records]
      (process-record record))
    (swap! processed-count #(+ (.count records) %))
    (println "Waiting for message in KafkaConsumer.poll," poll-count ", processed" @processed-count "messages")
    (recur
     (inc poll-count)
     (.poll consumer (Duration/ofSeconds 1)))))

(defn run-consumer!
  [consumer topic]
  (try
    (.subscribe consumer [topic]) ;; a consumer can subscribe to multiple topics
    (poll-records consumer)
    (catch org.apache.kafka.common.errors.WakeupException _
        ;; nothing to do
      )
    (catch Exception e
      (.printStackTrace e))
    (finally
      (println "Total" @processed-count " records processed")
      (.close consumer))))

(defn add-shutdown-hook
  [consumer]
  (let [main-thread (Thread/currentThread)]
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. ^Runnable (fn []
                                           (println "shutdown hook received") ;; debug
                                           (.wakeup consumer)
                                           (.join main-thread))))))

(defmethod ig/init-key :kafkapp.consumer/run!
  [_ [consumer topic]]
  (add-shutdown-hook consumer)
  (run-consumer! consumer topic))

(defmethod ig/halt-key! :kafkapp.consumer/instance
  [_ consumer]
  (.close consumer))
