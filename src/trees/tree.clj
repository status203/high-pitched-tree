(ns trees.tree
  (:require [clojure.zip :as z]
            [trees.util :as u]))

;; Note that base angle is 90 for trunk
(def vertical-trunk (constantly 0))

(defn with-vertical-trunk
  "Returns a fn that uses vertical-trunk for depth 1, otherwise uses the 
   provided angle-fn"
  [angle-fn]
  (fn branch-angle? [loc]
    (if (= 1 (u/depth loc))
      (vertical-trunk loc)
      (angle-fn loc))))

(defn base-angle
  "Takes a zipper to a new branch and returns the angle
   from which it should be calculating it's relative angle.
   i.e. previous sibling (if it has one), parent otherwise, default
   to 90 if it's the trunk."
  [loc]
  (if-let [base-loc (or (z/right loc) (z/up loc))]
    (-> base-loc z/node :abs-angle)
    90))

(defn- convert-angle
  "Takes a clockwise angle from West in degrees and converts it to an anti-clockwise
   angle from East in radians"
  [deg]
  (-> (- 180 deg)
      (mod 360)
      Math/toRadians))

(defn calc-end
  [[s1 s2] degs length]
  (let [rads (convert-angle degs)]
    [(+ s1 (* length (Math/cos rads)))
     (+ s2 (* length (Math/sin rads)))]))

(defn insert-placeholder-branch
  "Inserts a placeholder branch under the current location in the zipper, or
   at the root if nil, and returns the new zipper location
   .
   The placeholder branch knows only it's start point (the end of the parent 
   branch or [0 0] for the trunk), and has an empty children list.
   Returns the new zipper location."
  [loc]
  (if loc
    (-> loc
        (z/insert-child {:start (-> loc z/node :end)
                         :children  '()})
        z/down)
    (u/tree-zipper {:start [0 0]
                    :children  '()})))

(defn finalise-branch
  "Given a zipper location at a branch, populates the branch's
   :end, :rel-angle, :abs-angle, :length, :width and :colour from the
   provided algorithm fns. Width and colour are optional and default to
   1 and black respectively. Returns the updated zipper location."
  [loc {:keys [branch-angle branch-length branch-width branch-colour]}]
  ;; helpers can use previously set properties if desired. Properties are set in
  ;; the order:
  ;; angle->length->width->colour 
 (letfn [(apply-angle [zloc]
            (let [node  (z/node zloc)
                  base  (base-angle zloc)
                  rel   (branch-angle zloc)
                  abs   (+ base rel)]
              (z/replace zloc (assoc node
                                     :rel-angle rel
                                     :abs-angle abs))))

          (apply-length [zloc]
            (let [node  (z/node zloc)
                  start (:start node)
                  abs   (:abs-angle node)
                  len   (branch-length zloc)
                  end   (calc-end start abs len)]
              (z/replace zloc (assoc node
                                     :length len
                                     :end    end))))

          (apply-width [zloc]
            (let [node     (z/node zloc)
                  width-fn (u/as-fn branch-width 1)
                  w        (width-fn zloc)]
              (z/replace zloc (assoc node :width w))))

          (apply-colour [zloc]
            (let [node       (z/node zloc)
                  colour-fn (u/as-fn branch-colour [0 0 0 255])
                  c         (colour-fn zloc)]
              (z/replace zloc (assoc node :colour c))))]

    (-> loc
        (apply-angle)
        (apply-length)
        (apply-width)
        (apply-colour))))

(defn grow
  "opts (all are mandatory):
   
     :branch-angle - [child-zipper] -> degrees clockwise
     called to calculate the *relative* clockwise angle in degrees of a new
     child from a branch's last child or from the branch's parent angle if it's
     the first child (the trunk's parent is assumed to have an angle of 90°).

     :branch-length - [child-zipper] -> length
     called to calculate the length of a new child branch

     :add-child? - [ptb-zipper] -> bool
     Called on a (potential) parent-to-be node repeatedly until it returns false

   A branch is a map of 
     :start     [x y]
     :end       [x y]
     :rel-angle clockwise-degrees
     :abs-angle clockwise-degrees-from-West
     :length    length
     :children  (...branches)
   Note that, within :children, branches are stored from the most recent as
   z/insert-child -> z/down is more efficient.
    
   The trunk is considered to have a depth of 1.
   The parent zipper of the trunk is nil and considered depth 0."
  ([opts]
   (-> (insert-placeholder-branch nil)
       (finalise-branch opts)
       (grow opts)))
  ([loc {:keys [add-child?] :as opts}]
   (cond
     (add-child? loc)
     (let [loc' (-> loc
                    (insert-placeholder-branch)
                    (finalise-branch opts))]
       (recur loc' opts))

     (z/up loc) (recur (z/up loc) opts)

     :else (z/root loc))))
