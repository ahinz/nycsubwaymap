(ns subway.routes
  (:use compojure.core)
  (:require 
   [subway.dao :as dao]
   [subway.core :as core]
   [compojure.route :as route]
   [compojure.handler :as handler]
   [compojure.response :as response]
   [clojure.data.json :as json]))

(defn json-resp [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/write-str data)})

(defn get-references []
  (json-resp (dao/get-frame-refs)))

(defroutes main-routes
  (GET "/references" [] (get-references))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site main-routes))
