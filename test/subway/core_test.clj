(ns subway.core-test
  (:use clojure.test
        subway.core
        subway.geo))

(deftest geo
  (testing "coord transforms"
    (is
     (= (map int (to-webm 45.2 -82.1))
        [-9139330 5653062]))

    (is
     (= (map #(int (* 100 %)) (to-latlng -9139330 5653062))
        [4519 -8209]))))

(deftest interp
  (testing "interpolate since segment"
    (is
     (= (interpolate-distance-on-line-segment
         5.0
         (line-segment (point 0 0) (point 10 0)))
        (point 5.0 0.0)))

    (def p1 (interpolate-distance-on-line-segment
            5.0
            (line-segment (point 5 0) (point 20 30))))

    (def p2 (interpolate-distance-on-line-segment
            -2.0
            (line-segment (point 5 0) (point 20 30))))

    (def p3 (interpolate-distance-on-line-segment
            50.0
            (line-segment (point 5 0) (point 20 30))))
    (is 
     (= {:x (int (* 10000 (:x p1))) :y (int (* 10000 (:y p1)))}
        {:x 72360, :y 44721}))
    
    (is p2 (point 5 0))
    (is p3 (point 20 30)))

  (testing "bulk distances"

    ; Not a great test, but easy to reason about
    (def segs [(line-segment (point 0.0 0.0) (point 10.0 0.0))
               (line-segment (point 10.0 0.0) (point 20.0 20.0))])
    (def dists [0 3 6 9 10 (+ 10 (/ (Math/sqrt 500) 2)) (+ 10 (Math/sqrt 500))])

    (is
     (= (linear-distance-to-points segs dists)
        [(point 0.0 0.0) (point 3.0 0.0) (point 6.0 0.0) (point 9.0 0.0)
         (point 10.0 0.0) (point 15.0 10.0) (point 20.0 20.0)]))))

(deftest frame
  (testing "generating frames"
    (defn fd [f d] {:frame f :dist d})
    (def gen (generate-frames [{:frame 0 :dist 0.0}
                           {:frame 10 :dist 100.0}
                           {:frame 20 :dist 300.0}]))
    (is (= (range 0 20) (map #(:frame %) gen)))
    (is (= (concat 
            (map #(* 10.0 %) (range 0 10))
            (map #(+ 100 (* 20.0 %)) (range 0 10)))
           (map #(:dist %) gen)))))

(deftest geom
  (testing "p2p distance"
    (is (= (point-to-point-dist (point 0.0 0.0) (point 0.0 10.0)) 10.0))
    (is (= (int (* 1000 (point-to-point-dist (point 1.0 5.0) (point 3.0 8.0)))) 3605)))

  (testing "intersection"
    (is (= (lines-intersection (line 1.5 0.0) (line -1/3 5.5)) (point 3.0 4.5)))
    (is (= (lines-intersection (line -13.0 60.0) (line 13.0 -70.0)) (point 5.0 -5.0))))

  (testing "point on line"
    (is (= (point-on-line? (point 9.0 23.0) (line 2.0 5.0)) true))
    (is (= (point-on-line? (point 12.0 29.0) (line 2.0 5.0)) true))
    (is (= (point-on-line? (point 9.0 24.0) (line 2.0 5.0)) false)))

  (testing "point on line segment"
    (is (= (point-on-line-segment? (point 9.0 23.0) (line-segment (point 0.0 5.0) (point 11.0 27.0))) true))
    (is (= (point-on-line-segment? (point 12.0 29.0) (line-segment (point 0.0 5.0) (point 11.0 27.0))) false))
    (is (= (point-on-line-segment? (point 9.0 24.0) (line-segment (point 0.0 5.0) (point 11.0 27.0))) false)))

  (testing "Simple lines"
    (def l1 (line 1.0 1.0))
    (is (= (eval-line l1 2.0) 3.0))
    (is (= (eval-line l1 0.0) 1.0))
    (is (= (eval-line l1 -2.0) -1.0))))
