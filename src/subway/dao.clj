(ns subway.dao
  (:require
   [clojure.java.jdbc :as j]
   [clojure.java.jdbc.sql :as s]))

(def ^:dynamic *db-name* "prod.sqlite")
(defn- sqlite-db []
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname *db-name*})

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
  (when (not (.exists (java.io.File. *db-name*)))
    (j/with-connection (merge (sqlite-db) {:create true})
      (create-schema))))

(defn insert-or-update-frameref
  "Insert a frame reference if the id is nil, otherwise do an update"
  [fref]
  (j/with-connection (sqlite-db)
    (if (nil? (:id fref))
      (j/insert-record
       :refs
       {:frame (:frame fref) 
        :distance (:distance fref)})
      (j/update-values
       :refs
       ["id=?" (:id fref)]
       {:frame (:frame fref) 
        :distance (:distance fref)}))))

(defn get-frame-refs
  "Get a list of all framerefs ordered by distance"
  []
  (j/with-connection (sqlite-db)
    (j/with-query-results rs ["SELECT * FROM refs ORDER BY distance"]
      (doall (map #(FrameReference. (:id %) (:frame %) (:distance %)) rs)))))

