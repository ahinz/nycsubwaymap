(ns subway.geo)

(defn load-nycs-shape [line]
  (map (fn [line]
         (map #(java.lang.Double/parseDouble %)
              (butlast (rest (.split line ","))))) 
       (.split
        (slurp (.getFile (clojure.java.io/resource "shapes/nycs_7.txt")))
        "\r\n")))

(defn to-webm
  "Convert an (lat,lng) pair in geographic coords (EPSG4326) to web mercator"
  [lat lng]
  (let [origin-shift (/ (* 2.0 Math/PI (/ 6378137.0 2.0)) 180.0)]
    [(* origin-shift lng)
     (* origin-shift
        (/ (Math/log (Math/tan (*
                                (+ 90.0 lat)
                                (/ Math/PI 360.0))))
           (/ Math/PI 180.0)))]))

(defn to-latlng
  "Convert an (x,y) pair in Web Mercator into geographic coords (EPSG4326)"
  [x y]
  (let [origin-shift (/ (* 2.0 Math/PI (/ 6378137.0 2.0)) 180.0)
        d2r (/ Math/PI 180.0)
        r2d (/ 180.0 Math/PI)]
    [(* r2d (-
             (* 2.0 (Math/atan (Math/exp (* d2r (/ y origin-shift)))))
             (/ Math/PI 2.0)))
     (/ x origin-shift)]))
