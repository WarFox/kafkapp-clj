(ns kafkapp.utils
  (:import
   [org.apache.avro Schema$Parser]
   [org.apache.avro.generic GenericRecordBuilder GenericData$Record]
   [org.apache.kafka.clients.consumer ConsumerRecord]
   [org.apache.kafka.clients.producer ProducerRecord]))

(defn parse-schema
  [avro-schema]
  (.parse (Schema$Parser.) avro-schema))

(defn avro-record->map
  "Converts an avro data record to a clojure map"
  [^GenericData$Record ar]
  (into {}
        (for [field (seq (.getFields (.getSchema ar)))
              :let  [field-name (.name field)
                     value (.get ar field-name)]]
          [(keyword field-name) (str value)])))

(defn map->avro-record
  "Converts a cloure map to an avro data record"
  ^GenericData$Record [schema m]
  (let [builder (GenericRecordBuilder. schema)]
    (doseq [[k v] m]
      (.set builder (name k) v))
    (.build builder)))

;; value will be of type GenericData$Record for for avro messages
(defn value->map
  [value]
  (if (= GenericData$Record (type value))
    (avro-record->map value)
    (into {} value)))

(defmulti print!
  "print! utility to prints the [:topic :key :value] map"
  (fn [record]
    (cond
      (= ConsumerRecord (type record))
      ::consumer-record

      (= ProducerRecord (type record))
      ::producer-record

      :else (type (:value record)))))

(defmethod print! ::consumer-record print-consumer-record ;; print_consumer_record will the name shown in stacktrace
  [consumer-record]
  (println {:topic (.topic consumer-record)
            :partition (.partition consumer-record)
            :offset (.offset consumer-record)
            :key (.key consumer-record)
            :value (value->map (.value consumer-record))}))

(defmethod print! GenericData$Record print-generic-record ;; print_generic_record will the name shown in stacktrace
  [{:keys [value] :as record}]
  (println (assoc record
                  :value (avro-record->map value))))

(defmethod print! java.util.LinkedHashMap print-generic-record ;; print_generic_record will the name shown in stacktrace
  [{:keys [value] :as record}]
  (println (assoc record
                  :value (avro-record->map value))))

(defmethod print! :default
  [record]
  (println record))

(def print-metadata #(printf "Produced record to topic %s partition [%d] @ offest %d\n"
                             (.topic %)
                             (.partition %)
                             (.offset %)))
