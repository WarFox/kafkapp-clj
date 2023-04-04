(ns kafkapp.config
  (:require
   [clojure.java.io :as jio]))

(def schema-file (jio/resource "pageviews.avsc"))

(def pageviews-schema (slurp schema-file))

(def messages
  [{:viewtime 100 :userid "User_0" :pageid "Page_0"}
   {:viewtime 101 :userid "User_1" :pageid "Page_1"}
   {:viewtime 102 :userid "User_2" :pageid "Page_2"}
   {:viewtime 103 :userid "User_3" :pageid "Page_3"}
   {:viewtime 104 :userid "User_4" :pageid "Page_4"}
   {:viewtime 105 :userid "User_5" :pageid "Page_5"}
   {:viewtime 106 :userid "User_6" :pageid "Page_6"}
   {:viewtime 107 :userid "User_7" :pageid "Page_7"}
   {:viewtime 108 :userid "User_8" :pageid "Page_8"}
   {:viewtime 109 :userid "User_9" :pageid "Page_9"}
   {:viewtime 110 :userid "User_10" :pageid "Page_10"}
   {:viewtime 111 :userid "User_11" :pageid "Page_11"}
   {:viewtime 112 :userid "User_12" :pageid "Page_12"}
   {:viewtime 113 :userid "User_13" :pageid "Page_13"}
   {:viewtime 114 :userid "User_14" :pageid "Page_14"}
   {:viewtime 115 :userid "User_15" :pageid "Page_15"}])
