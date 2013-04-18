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

(defn sqr [x] (* x x))
(defn point-to-point-dist [p1 p2]
  (Math/sqrt (+ (sqr (- (x p1) (x p2)))
                (sqr (- (y p1) (y p2))))))

(defrecord LineSegment [p1 p2 line dist])
(defn line-segment [p1 p2]
  (let [m (/ (- (:y p1) (:y p2))
             (- (:x p1) (:x p2)))
        b (- (:y p1) (* m (:x p1)))
        d (point-to-point-dist p1 p2)]
   (LineSegment. p1 p2 (line m b) d)))

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

(defn point-to-line-seg-dist [p ls]
  (let [ipt (point-to-line-intersection-pt p (:line ls))]
    (if (point-on-line-segment ipt ls)
      (point-to-point-dist p ipt)
      false)))

; Things we know:
; frame, distance pairs:
; P = [(f0,d0), (f1,d1), (f2,d2), ...]
; Target output:
; list matching frame to x,y
; First we assign each frame a *distance*
;; Distance Interval:
;; type: { :distance | :clear }
;
; Frame list is a list of tuples
; (frame, distance)
(defn generate-frames [reference-points]
  (reduce 
   (fn [pts [last-ref next-ref]]
     (let [start-frame (:frame last-ref)
           end-frame (:frame next-ref)
           start-dist (:dist last-ref)
           delta-distance (- (:dist next-ref) start-dist)
           delta-frame (- end-frame start-frame)
           dist-per-frame (/ delta-distance delta-frame)

           new-dists (map #(* dist-per-frame %) (range 0 (- end-frame start-frame)))
           new-pairs (map-indexed 
                      (fn [i d] {:frame (+ i start-frame) :dist (+ d start-dist)})
                      new-dists)]
       (concat pts new-pairs)))
   []
   (partition 2 1 reference-points)))
