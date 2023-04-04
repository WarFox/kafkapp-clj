(ns kafkapp.cli
  (:require
   [clojure.string :as str]
   [clojure.tools.cli :as cli]))

(def cli-options
  [["-c" "--classification CLASSIFICATION" "Avro, Json or transactional"
    :parse-fn #(keyword %)
    :validate [#(#{:avro :json :transactional} %) "Unsupported classification"]]
   ["-t" "--topic TOPIC_NAME" "Topic name"]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["kafkapp-clj is a collection of sample Kafka applications written in Clojure"
        ""
        "Usage: kafkapp [options] consumer"
        ""
        "Options:"
        options-summary
        ""
        "Commands:"
        "  consumer    Start a consumer"
        "  producer    Start a producer"
        ""
        "Please refer to the manual page for more information."]
       (str/join \newline)))


(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn parse-arguments
  [args]
  (let [{:keys [arguments options errors summary]} (cli/parse-opts args cli-options) ]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}

      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}

      ;; custom validation on arguments
      (and (= 1 (count arguments))
           (#{"consumer" "producer"} (first arguments)))
      {:command (first arguments) :options options}

      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))
