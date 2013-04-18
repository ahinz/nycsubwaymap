(ns subway.core)

;; Datetype line
(defrecord Point [x y])
(defn point [x y] (Point. x y))
(defn x [p] (.x p))
(defn y [p] (.y p))

(defrecord Line [m b])
(defn line [m b] (Line. m b))
(defn eval-line [line x]
  (+ (* (.m line) x) (.b line)))

(defrecord LineSegment [p1 p2 line])
(defn line-segment [p1 p2]
  (let [m (/ (- (:y p1) (:y p2))
             (- (:x p1) (:x p2)))
        b (- (:y p1) (* m (:x p1)))]
   (LineSegment. p1 p2 (line m b))))


(defn sqr [x] (* x x))
(defn point-to-point-dist [p1 p2]
  (Math/sqrt (+ (sqr (- (x p1) (x p2)))
                (sqr (- (y p1) (y p2))))))

(defn lines-intersection [l1 l2]
  ;; (1) y = m1*x + b1 (2) y = m2*x + b2
  ;; m1*x + b1 = m2*x + b2 => x = (b2 - b1)/(m1 - m2)
  (let [x (/ (- (:b l2) (:b l1))
             (- (:m l1) (:m l2)))
        y (+ (* (:m l1) x) (:b l1))]
    (point x y)))

(defn point-to-line-intersection-pt [p l]
  (let [;; First find a line l' that is perpendicular to
        ;; l and goes through the point p
        ;; Line must satisfy:
        ;; p=(x,y) ; y=m'(x) + b => b = y - m'(x)
        ;; l=(m,b) ; m'=-1/m

        m' (- (/ 1 (.m l)))

        b' (- (y p) (* m' (x p)))
        l' (line m' b')]
    (lines-intersection l l')))

(defn point-on-line [p l]
  (= (eval-line l (:x p)) (:y p)))

(defn point-on-line-segment [p ls]
  (let [x (:x p)
        y (:y p)

        x1 (:x (:p1 ls))
        x2 (:x (:p2 ls))
        y1 (:y (:p1 ls))
        y2 (:y (:p2 ls))
        
        xmin (min x1 x2)
        xmax (max x1 x2)
        
        ymin (min y1 y2)
        ymax (max y1 y2)]
    (and
     (point-on-line p (:line ls))
     (and (>= y ymin) (>= x xmin)
          (<= y ymax) (<= x xmax)))))

;; (defn point-to-line-seg-dist [p ls]
;;   (let [ipt (point-to-line-intersection-pt p (:line ls))
;;         ]))
