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

(defn expand-to-frames
  "Given two frame refs (f1,d1) to (f2,d2) return a list
   of length |f2-f1| where each entry corresponds to a frame/distance"
  [ref1 ref2]
  (let [f1 (:frame ref1)
        f2 (:frame ref2)
        d1 (:distance ref1)
        d2 (:distance ref2)
        step (/ (- d2 d1) (- f2 f1))]
    (map #(+ d1 (* step %)) (range 0 (- f2 f1)))))

(defn get-all-frames
  "Get a list of all frames and the reference distance"
  []
  (let [segs 
        (map #(apply core/line-segment %)
             (partition 
              2 1
              (map 
               (partial apply geo/to-webm)
               (geo/load-nycs-shape "7"))))
        frefs (dao/get-frame-refs)]
    {:frames
     (map #(geo/to-latlng (:x %) (:y %))
          (core/linear-distance-to-points
           segs
           (mapcat #(apply expand-to-frames %)
                   (partition 2 1 frefs))))
     :start-frame (:frame (first frefs))}))

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
         upto-dist (reduce + (map #(:dist %) path))
         seg-dist (core/point-to-point-dist (:p1 smallest-seg) ipt)
         total-dist (+ upto-dist seg-dist)]
     {:pt (geo/pt-to-latlng ipt)
      :distance total-dist})))

(defroutes main-routes
  (GET "/references" [] (get-references))
  (POST "/references" {body :body} (let [data (json/read-str (slurp body))
                                         fref (dao/create-frame-ref 
                                               (get data "frame") (get data "distance"))]
                                     (dao/insert-or-update-frameref fref)))
  (GET "/snap-to-route" {params :params} (snap-to-route (java.lang.Double/parseDouble (:lat params))
                                                        (java.lang.Double/parseDouble (:lng params))))
  (GET "/shape" [] (json-resp (geo/load-nycs-shape "7")))
  (GET "/frames" [] (json-resp (get-all-frames)))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site main-routes))
