(ns kafkapp.core
  (:gen-class)
  (:require
   [integrant.core :as ig]
   [kafkapp.systems :as systems]
   [kafkapp.cli :as cli]))

(defn run
  [command classification topic]
  (let [system (case command
                 "consumer"
                 (systems/consumer classification topic)

                 "producer"
                 (systems/producer classification topic))]

    (ig/load-namespaces system)
    (ig/init system)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{:keys [command options exit-message ok?]} (cli/parse-arguments args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)

      (run command (:classification options) (:topic options)))))
