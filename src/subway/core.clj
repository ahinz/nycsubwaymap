(ns subway.core)

; 7 line: http://www.youtube.com/watch?v=cmaLggM73a0

(defrecord Point [x y])
(defn point [x y] (Point. x y))
(defn x [p] (.x p))
(defn y [p] (.y p))

(defrecord Line [m b])
(defn line [m b] (Line. m b))
(defn eval-line [line x]
  (+ (* (.m line) x) (.b line)))

(defn sqr [x] (* x x))
(defn point-to-point-dist 
  "Distance between p1 and p2"
  [p1 p2]
  (Math/sqrt (+ (sqr (- (x p1) (x p2)))
                (sqr (- (y p1) (y p2))))))

(defrecord LineSegment [p1 p2 line dist])
(defn line-segment [p1 p2]
  (let [m (/ (- (:y p1) (:y p2))
             (- (:x p1) (:x p2)))
        b (- (:y p1) (* m (:x p1)))
        d (point-to-point-dist p1 p2)]
   (LineSegment. p1 p2 (line m b) d)))

(defn lines-intersection
  "Determine the intersection point between two lines. 
   If the lines are parallel, this method returns 'false'"
  [l1 l2]
  ;; (1) y = m1*x + b1 (2) y = m2*x + b2
  ;; m1*x + b1 = m2*x + b2 => x = (b2 - b1)/(m1 - m2)
  (if (= (:m l1) (:m l2))
    false
    (let [x (/ (- (:b l2) (:b l1))
               (- (:m l1) (:m l2)))
          y (+ (* (:m l1) x) (:b l1))]
      (point x y))))

(defn point-to-line-intersection-pt
  "Given a point (p) and a line (l) return a point on l such that l and p are colinear"
  [p l]
  (let [;; First find a line l' that is perpendicular to
        ;; l and goes through the point p
        ;; Line must satisfy:
        ;; p=(x,y) ; y=m'(x) + b => b = y - m'(x)
        ;; l=(m,b) ; m'=-1/m

        m' (- (/ 1 (.m l)))

        b' (- (y p) (* m' (x p)))
        l' (line m' b')]
    (lines-intersection l l')))

(defn point-on-line?
  "Returns true if p is on the line l"
  [p l]
  (= (eval-line l (:x p)) (:y p)))

(defn point-on-line-segment?
  "Returns true if p is on the line segment ls"
  [p ls]
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
     (point-on-line? p (:line ls))
     (and (>= y ymin) (>= x xmin)
          (<= y ymax) (<= x xmax)))))

(defn point-to-line-seg-dist
  "Get the distance from a point to a line segment. 
   If the closest point on the line segment is an end point return false"
  [p ls]
  (let [ipt (point-to-line-intersection-pt p (:line ls))]
    (if (point-on-line-segment? ipt ls)
      (point-to-point-dist p ipt)
      false)))

(defn generate-frames
  "Given a list of reference points:
  [{:frame f1 :dist d1}, {:frame f2 :dist d2}, ..., {:frame fN :dist dN}]

  Interpolate all integer frames from f1 to fN using the given distances"
  [reference-points]
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

(defn interpolate-distance-on-line-segment
  "Given a line segment from p1 to p2 and a distance between 0 and distance(p1,p2), return
   the point on the line segment d away from p1. If d > distance(p1,p2), return p1. If d < 0
   return p1"
  [d ls]
  (cond
   (>= d (:dist ls))
   (:p2 ls)

   (<= d 0)
   (:p1 ls)

   :else
   (let [pct (/ d (:dist ls))
         x1 (:x (:p1 ls))
         x2 (:x (:p2 ls))
         y1 (:y (:p1 ls))
         y2 (:y (:p2 ls))

         x (+ (* (- x2 x1) pct) x1)
         y (+ (* (- y2 y1) pct) y1)]
     (point x y))))

(defn- -linear-distance-to-points [segs distances cur-dist points]
  (if (empty? distances)
    points
    (let [distance (first distances)]
      (cond
       ;; Can't do anything, just return
       (empty? segs)
       (recur segs (rest distances) cur-dist points)

       ;; Current sample is in this segment
       ;; Calculate point and don't change segment list
       (>= (+ cur-dist (:dist (first segs))) distance)
       (recur 
        segs
        (rest distances)
        cur-dist
        (conj
         points
         (interpolate-distance-on-line-segment (- distance cur-dist) (first segs))))

       ;; Move on to the next segment
       :else
       (recur
        (rest segs) 
        distances
        (+ cur-dist (:dist (first segs)))
        points)))))

(defn linear-distance-to-points
  "Given a list of line segments and a list of distances corresponding to distance
   from the first point in the line segment list, return interpolated locations
   for each distance. Distances must be sorted."
  [line-segments distances]
  (-linear-distance-to-points line-segments distances 0.0 []))


