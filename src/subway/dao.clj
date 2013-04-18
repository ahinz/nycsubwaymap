(ns subway.dao
  (:require
   [clojure.java.jdbc :as j]
   [clojure.java.jdbc.sql :as s]))

(def db-name "prod.sqlite")
(def sqlite-db {:classname "org.sqlite.JDBC"
                :subprotocol "sqlite"
                :subname db-name})

(defrecord FrameReference [id frame distance])

(defn- create-schema
  "Create the baseline schema"
  [] 
  (println (j/create-table
    :refs
    [:id :integer "PRIMARY KEY"]
    [:frame "real"]
    [:distance "real"])))

(defn bootstrap
  "Call this before using the database subsystem (builds the database schema)"
  []
  (when (not (.exists (java.io.File. db-name)))
    (j/with-connection (merge sqlite-db {:create true})
      (create-schema))))

