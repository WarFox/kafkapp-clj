(ns kafkapp.systems
  (:require
   [integrant.core :as ig]
   [kafkapp.config :as config]))

;; Initialize Consumer system with consumer properties and input parameters
(defn consumer
  "classification can be avro, json or transactional"
  [classification topic]
  {:kafkapp.consumer/group-id   [classification topic]
   :kafkapp.consumer/properties [classification (ig/ref :kafkapp.consumer/group-id)]
   :kafkapp.consumer/instance   (ig/ref :kafkapp.consumer/properties)
   :kafkapp.consumer/run!       [(ig/ref :kafkapp.consumer/instance) topic]})

;; Initialize Producer system with producer properties and input parameters
(defn producer
  [classification topic]
  {:kafkapp.producer/transactional-id [classification topic]
   :kafkapp.producer/properties       [classification (ig/ref :kafkapp.producer/transactional-id)]
   :kafkapp.producer/instance         (ig/ref :kafkapp.producer/properties)
   :kafkapp.producer/schema           config/pageviews-schema
   :kafkapp.producer/message-kvs      config/messages
   :kafkapp.producer/run!              [classification
                                (ig/ref :kafkapp.producer/instance)
                                (ig/ref :kafkapp.producer/schema)
                                topic
                                (ig/ref :kafkapp.producer/message-kvs)]})
