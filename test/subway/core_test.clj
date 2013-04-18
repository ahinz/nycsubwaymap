(ns subway.core-test
  (:use clojure.test
        subway.core))

(deftest geom
  (testing "p2p distance"
    (is (= (point-to-point-dist (point 0.0 0.0) (point 0.0 10.0)) 10.0))
    (is (= (int (* 1000 (point-to-point-dist (point 1.0 5.0) (point 3.0 8.0)))) 3605)))

  (testing "intersection"
    (is (= (lines-intersection (line 1.5 0.0) (line -1/3 5.5)) (point 3.0 4.5)))
    (is (= (lines-intersection (line -13.0 60.0) (line 13.0 -70.0)) (point 5.0 -5.0))))

  (testing "point on line"
    (is (= (point-on-line (point 9.0 23.0) (line 2.0 5.0)) true))
    (is (= (point-on-line (point 12.0 29.0) (line 2.0 5.0)) true))
    (is (= (point-on-line (point 9.0 24.0) (line 2.0 5.0)) false)))

  (testing "point on line segment"
    (is (= (point-on-line-segment (point 9.0 23.0) (line-segment (point 0.0 5.0) (point 11.0 27.0))) true))
    (is (= (point-on-line-segment (point 12.0 29.0) (line-segment (point 0.0 5.0) (point 11.0 27.0))) false))
    (is (= (point-on-line-segment (point 9.0 24.0) (line-segment (point 0.0 5.0) (point 11.0 27.0))) false)))

  (testing "Simple lines"
    (def l1 (mk-line 1.0 1.0))
    (is (= (eval-line l1 2.0) 3.0))
    (is (= (eval-line l1 0.0) 1.0))
    (is (= (eval-line l1 -2.0) -1.0))))
