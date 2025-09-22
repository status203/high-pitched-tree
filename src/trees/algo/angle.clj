(ns trees.algo.angle
  (:require [clojure.zip :as z]
            [trees.util :as u]))

(defn regularly-spaced
  "Takes a spread between first and last branches, and the number of total branches
   and returns a fn that will space the branches evenly within that space."
  ([spread n]
   (let [initial-angle (- (/ spread 2))
         gap-angle     (/ spread (dec n))]
     (fn branch-angle [loc]
       (if (z/right loc) gap-angle initial-angle)))))

(defn offset
  "Returns an angle fn that returns a constant amount"
  [deg]
  (fn branch-angle [_loc] deg))

(defn scale
    "Returns a branch-angle function.
    
    Given a zipper positioned on a branch node where a child will be added,
    computes the child's relative angle as:
    
        base-angle * (scale ^ (depth - 1))
    
    Thus, each successive branch is a fixed multiple (scale) of its parent’s
    angle. A scale < 1 produces shrinking angles, while a scale > 1
    produces growing angles"
  [initial-angle scale]
  (fn branch-length [loc]
    (* initial-angle
       (Math/pow scale (dec (u/depth loc))))))
