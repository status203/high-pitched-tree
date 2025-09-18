(ns trees.tree
  (:require [clojure.zip :as z]
            [trees.util :as u]))

(defn base-angle
  "Takes a zipper to a (growing) branch and returns the angle
   from which the next child branch should be calculated."
  [loc]
  (-> (or (-> loc z/down) loc)
      z/node
      :abs-angle))

(defn- convert-angle
  "Takes a clockwise angle from West in degrees and converts it to an anti-clockwise
   angle from East in radians"
  [deg]
  (-> deg
      (mod 360)
      Math/toRadians))

(defn calc-end
  [[s1 s2] degs length]
  (let [rads (convert-angle degs)]
    [(+ s1 (* length (Math/cos rads)))
     (+ s2 (* length (Math/sin rads)))]))

(defn grow
  "opts (all are mandatory)
     :branch-angle - [zipper] -> degrees clockwise
     called to calculate the *relative* clockwise angle in degrees of a new child
     from a branch's last child or from the branch's angle if it's the first child.

     :branch-length - [zipper] -> length
     called to calculate the length of a new child branch

     :children? - [zipper] -> bool
     called to decide whether the branch should have any children

     :add-child? - [zipper] -> bool
     if a branch should have children called to decide whether to add another
     child branch

   A branch is a map of 
     :start     [x y]
     :end       [x y]
     :rel-angle clockwise-degrees
     :abs-angle clockwise-degrees-from-West
     :length    length
     :children  (...branches)
   Note that, within :children, branches are stored from the most recent as
   z/insert-child -> z/down is more efficient.
    
   For now, assumes a single trunk (which is considered to have a depth of 1)"
  ([trunk-length trunk-angle opts]
   (let [tree {:start [0 0]
               :end       (calc-end [0 0] trunk-angle trunk-length)
               :rel-angle trunk-angle
               :abs-angle trunk-angle
               :length    trunk-length
               :children  '()}]
     (grow tree opts)))
  ([tree {:keys [add-child? branch-angle branch-length children?]}]
   (loop [loc (u/tree-zipper tree)]
     (cond
       (add-child? loc)
       (let [rel-angle (branch-angle loc)
             base      (base-angle loc)
             abs-angle (+ base rel-angle)
             length    (branch-length loc)
             start     (-> loc z/node :end)
             end       (calc-end start abs-angle length)
             child      {:start     start
                         :end       end
                         :rel-angle rel-angle
                         :abs-angle abs-angle
                         :length    length
                         :children  ()}
             loc'      (z/insert-child loc child)]
         (if (children? (z/down loc'))
           (recur (z/down loc'))
           (recur loc')))
       
       (z/up loc) (recur (z/up loc))
       
       :else (z/node loc)))))
