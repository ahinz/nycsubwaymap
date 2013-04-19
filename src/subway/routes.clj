(ns subway.routes
  (:use compojure.core)
  (:require 
   [subway.geo :as geo]
   [subway.dao :as dao]
   [subway.core :as core]
   [compojure.route :as route]
   [compojure.handler :as handler]
   [compojure.response :as response]
   [clojure.data.json :as json]))

(defn json-resp
  "Create a json response with an optional status"
  [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/write-str data)})

(defn get-references
  "Get a list of user-defined frame references"
  []
  (json-resp 
   (map #(update-in % [:pt] geo/pt-to-latlng)  
        (core/frame-refs-to-points
         (map (partial apply geo/to-webm) (geo/load-nycs-shape "7"))
         (dao/get-frame-refs)))))

(defn snap-to-route
  "Given a lat/lng pair, find the closest frameref"
  [lat lng]
  (json-resp
   (let [[x y] (geo/to-webm lat lng)
         p (core/point x y)
         segs (core/points-to-line-segments
               (map (partial apply geo/to-webm)
                    (geo/load-nycs-shape "7")))
         [smallest-seg _] 
         (reduce 
          (fn [[seg curmin] nextseg]
            (let [newmin (core/point-to-line-seg-dist p nextseg)]
              (if (or (nil? curmin) (not curmin) (and newmin (< newmin curmin)))
                [nextseg newmin]
                [seg curmin])))
          [nil nil]
          segs)
         ipt (core/point-to-line-intersection-pt p (:line smallest-seg))
         path (take-while #(not (= smallest-seg %)) segs)
         upto-dist (reduce + (map #(:dist %) (butlast path)))
         seg-dist (Math/min
                   (core/point-to-point-dist (:p1 smallest-seg) ipt)
                   (core/point-to-point-dist (:p2 smallest-seg) ipt))
         total-dist (+ upto-dist seg-dist)]
     {:pt (geo/pt-to-latlng ipt)
      :distance total-dist})))

(defroutes main-routes
  (GET "/references" [] (get-references))
  (GET "/snap-to-route" {params :params} (snap-to-route (java.lang.Double/parseDouble (:lat params))
                                                        (java.lang.Double/parseDouble (:lng params))))
  (GET "/shape" [] (json-resp (geo/load-nycs-shape "7")))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site main-routes))
